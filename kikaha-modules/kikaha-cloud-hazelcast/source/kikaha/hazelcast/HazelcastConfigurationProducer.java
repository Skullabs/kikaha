package kikaha.hazelcast;

import com.hazelcast.config.*;
import kikaha.cloud.smart.ServiceRegistry.ApplicationData;
import kikaha.commons.SystemResource;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;

import static kikaha.commons.Lang.convert;
import static kikaha.commons.Lang.isUndefined;

/**
 * Created by miere.teixeira on 16/06/2017.
 */
@Slf4j
@Singleton
public class HazelcastConfigurationProducer {

    @Inject kikaha.config.Config config;
    @Inject ApplicationData applicationData;

    @Inject
    @Typed( HazelcastConfigurationListener.class )
    Iterable<HazelcastConfigurationListener> configurationListeners;

    @Getter(lazy = true) private final
    Config hazelcastConfig = createHazelcastConfig();

    @Produces Config produceHazelcastConfig(){
        return getHazelcastConfig();
    }

    private Config createHazelcastConfig() {
        try {
            final String configFile = config.getString("server.hazelcast.config");
            final Config hazelcastConfig = isUndefined(configFile) ? readDefaultProgrammaticConfig() : readHazelcastXMLConfig(configFile);

            hazelcastConfig.setProperty("hazelcast.logging.type", "slf4j");
            for (HazelcastConfigurationListener listener : configurationListeners)
                listener.onConfigurationLoaded(hazelcastConfig);

            return hazelcastConfig;
        } catch ( IOException cause ) {
            throw new IllegalStateException( "Can't read Hazelcast configuration", cause );
        }
    }

    private Config readHazelcastXMLConfig( String configFile ){
        final InputStream inputStream = SystemResource.openFile( configFile );
        return new XmlConfigBuilder( inputStream ).build();
    }

    private Config readDefaultProgrammaticConfig() throws IOException {
        final Config hazelcastConfig = new Config();
        configureClusterGroup( hazelcastConfig );
        connectToClusterMembers( hazelcastConfig );
        configOtherNetworkSettings( hazelcastConfig );
        return hazelcastConfig;
    }

    private void configureClusterGroup( Config hazelcastConfig ) {
        final GroupConfig groupConfig = hazelcastConfig.getGroupConfig();
        groupConfig.setName( applicationData.getCanonicalName() );

        final String groupPassword = config.getString("server.hazelcast.group-password");
        if ( isUndefined( groupPassword ) )
            log.warn( "No Hazelcast's Group Password defined. You may face issues connecting to other nodes." );
        else
            groupConfig.setPassword( groupPassword );
    }

    private void connectToClusterMembers( Config hazelcastConfig ) throws IOException {
        if ( config.getBoolean( "server.hazelcast.connect-to-cluster-members" ) ) {
            final JoinConfig join = hazelcastConfig.getNetworkConfig().getJoin();
            final TcpIpConfig tcpIpConfig = join.getTcpIpConfig().setEnabled(true);
            convert( applicationData.getSiblingNodesOnTheCluster(), ApplicationData::getLocalAddress)
                    .forEach(tcpIpConfig::addMember);
        }
    }

    private void configOtherNetworkSettings( Config hazelcastConfig ){
        final JoinConfig join = hazelcastConfig.getNetworkConfig().getJoin();
        join.getMulticastConfig().setEnabled( config.getBoolean( "server.hazelcast.enable-multicast" ) );
    }
}
