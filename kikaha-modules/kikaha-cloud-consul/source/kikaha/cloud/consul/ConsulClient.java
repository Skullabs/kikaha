package kikaha.cloud.consul;

import static java.lang.String.format;
import static kikaha.commons.Lang.*;
import java.io.IOException;
import java.util.*;
import javax.inject.*;
import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.type.*;
import kikaha.cloud.smart.ServiceRegistry;
import kikaha.cloud.smart.ServiceRegistry.ApplicationData;
import kikaha.config.Config;
import kikaha.commons.Tuple;
import kikaha.urouting.serializers.jackson.Jackson;
import lombok.extern.slf4j.Slf4j;

/**
 *
 */
@Slf4j
@Singleton
public class ConsulClient implements ServiceRegistry {

	final static String REQUEST_CHECK_STRING =
			"{\"ID\": \"{id}\",\"Name\": \"{name}\",\"Address\": \"{host}\",\"Port\": {port}, \"Tags\": {tags}," +
			"\"Check\": { \"DeregisterCriticalServiceAfter\": \"{deregister-after}m\", \"HTTP\": \"{health-check-url}\"," +
			"\"Interval\": \"{health-check-interval}s\"}}";

	@Inject Config config;
	@Inject Jackson jackson;

	@Override
	public void registerIntoCluster( final ApplicationData applicationData ) throws IOException {
		final String clusterName = applicationData.getName() + ":" + applicationData.getVersion();
		log.info( "Joining consul cluster '" + clusterName + "' as '" + applicationData.getMachineId() + "'..." );
		final String message = asMessage( applicationData );
		final int status = post( "/v1/agent/service/register", message );
		if ( status != 200 )
			throw new IOException( "Could not register the application on consul.io." );
	}

	String asMessage( final ApplicationData applicationData ) throws IOException {
		final Map<String, String> params = asParams(applicationData);

		String message = REQUEST_CHECK_STRING;
		for ( final Map.Entry<String, String> entry : params.entrySet() ) {
			final String value = Optional.ofNullable( entry.getValue() ).orElse( "" );
			message = message.replace("{" + entry.getKey() + "}", value );
		}

		log.debug( "Consul request: " + message );
		return message;
	}

	Map<String, String> asParams( final ApplicationData applicationData ) throws IOException {
		final Map<String, String> params = new HashMap<>();
		params.put( "id", applicationData.getMachineId() );
		params.put( "name", applicationData.getName() + ":" + applicationData.getVersion() );
		params.put( "host", applicationData.getLocalAddress() );
		params.put( "port", String.valueOf(applicationData.getLocalPort()) );
		params.put( "tags", asTagList( config.getStringList( "server.smart-server.application.tags" ) ) );
		params.put( "deregister-after", String.valueOf(config.getInteger( "server.consul.deregister-critical-service-after" )) );
		params.put( "health-check-interval", String.valueOf(config.getInteger( "server.consul.health-check-interval" )) );
		params.put( "health-check-url", getHealthCheckUrl( applicationData ) );
		return params;
	}

	String getHealthCheckUrl(ApplicationData applicationData) throws IOException {
		String healthCheckUrl = config.getString("server.smart-server.application.health-check-url" );
		if ( healthCheckUrl == null || healthCheckUrl.isEmpty() ) {
			healthCheckUrl = format( "%s://%s:%d%s",
				applicationData.isHttps() ? "https" : "http",
				applicationData.getLocalAddress(),
				applicationData.getLocalPort(),
				config.getString("server.health-check.url")
			);
		}

		log.info( "  Health check URL: " + healthCheckUrl );
		return healthCheckUrl;
	}

	private String asTagList(List<String> stringList) {
		for ( int i=0; i<stringList.size(); i++ )
			stringList.set(i, '"' + stringList.get(i) + '"');
		return "[" + String.join(" ", stringList) + "]";
	}

	int post( String url, String msg ) throws IOException {
		return Http.sendRequest( getConsulEndpointBaseURL() + url, "POST", msg );
	}

	@Override
	public void deregisterFromCluster( final ApplicationData applicationData ) throws IOException {
		log.info( "Leaving consul cluster..." );
		final int status = put( "/v1/agent/service/deregister/" + applicationData.getMachineId() );
		if ( status != 200 )
			throw new IOException( "Could not register the application on consul.io." );
	}


	int put(String url) throws IOException {
		return Http.sendRequest( getConsulEndpointBaseURL() + url, "PUT", null );
	}

	@Override
	public List<ApplicationData> locateSiblingNodesOnTheCluster(ApplicationData applicationData) throws IOException {
		final Tuple<Integer, String> response = Http.get(getConsulEndpointBaseURL() + "/v1/agent/services");
		if ( response.getFirst() != 200 )
			throw  new IOException( "Could not retrieve data from Consul: " + response.getSecond() );

		final TypeFactory typeFactory = jackson.objectMapper().getTypeFactory();
		final MapType mapType = typeFactory.constructMapType(HashMap.class, String.class, ConsulRegisteredClient.class);
		final Map<String, ConsulRegisteredClient> clients = jackson.objectMapper().readValue( response.getSecond(), mapType );
		final String name = applicationData.getName() + ":" + applicationData.getVersion();
		final String id = applicationData.getMachineId();
		final List<ConsulRegisteredClient> found = filter( clients.values(), c -> name.equals(c.service) && !id.equals( c.id ) );
		return convert( found, f->f.toApplicationData( applicationData ) );
	}

	String getConsulEndpointBaseURL(){
		final String consulHost = config.getString( "server.consul.host", "127.0.0.1" );
		final int consulPort = config.getInteger( "server.consul.port", 8500 );
		return "http://" + consulHost + ":" + consulPort;
	}
}

@JsonIgnoreProperties( ignoreUnknown = true )
class ConsulRegisteredClient {

	@JsonProperty("ID")
	String id;

	@JsonProperty("Service")
	String service;

	@JsonProperty("Address")
	String address;

	@JsonProperty("Port")
	int port;

	ApplicationData toApplicationData( ApplicationData currentApplication ){
		return ApplicationData.nodeOfSameApplication(
			currentApplication, id, address, port
		);
	}
}