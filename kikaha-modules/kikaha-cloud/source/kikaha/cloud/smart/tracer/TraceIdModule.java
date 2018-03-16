package kikaha.cloud.smart.tracer;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.util.HttpString;
import kikaha.config.Config;
import kikaha.core.DeploymentContext;
import kikaha.core.modules.Module;
import kikaha.urouting.ContextProducerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;

/**
 * Created by ibratan on 16/06/2017.
 */
@Singleton
public class TraceIdModule implements Module {

    @Inject Config config;
    @Inject ContextProducerFactory contextProducerFactory;

    @Override
    public void load(Undertow.Builder server, DeploymentContext context) throws IOException {
        if ( config.getBoolean( "server.smart-server.trace-id.enabled" ) ) {
            final HttpString headerName = new HttpString( config.getString( "server.smart-server.trace-id.header-name" ) );
            final TraceIdContextProducer contextProducer = new TraceIdContextProducer( headerName );
            if ( contextProducerFactory.registerProducer( TraceId.class, contextProducer ) ) {
                final HttpHandler httpHandler = context.rootHandler();
                final HttpHandler traceIdHandler = new TraceIdHttpHandler(headerName, httpHandler);
                context.rootHandler(traceIdHandler);
            }
        }
    }
}
