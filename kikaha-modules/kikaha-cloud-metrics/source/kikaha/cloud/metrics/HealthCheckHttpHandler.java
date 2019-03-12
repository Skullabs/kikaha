package kikaha.cloud.metrics;

import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.util.Map;

/**
 * Endpoint to check if the application is healthy.
 */
@Slf4j
public class HealthCheckHttpHandler implements HttpHandler {

	static final String
			HEALTHY_MSG = "The service is healthy.",
			UNHEALTHY_MSG = "The service %s is unhealthy. %s";

	@Inject HealthCheckRegistry registry;

	@Override
	public void handleRequest( final HttpServerExchange exchange ) throws Exception {
		if ( exchange.isInIoThread() )
			exchange.dispatch( this::runHealthCheck );
		else
			runHealthCheck( exchange );
	}

	void runHealthCheck( final HttpServerExchange exchange ) {
		int responseStatus = StatusCodes.OK;
		String message = HEALTHY_MSG;

		for ( final Map.Entry<String, HealthCheck.Result> entry : registry.runHealthChecks().entrySet() )
			if ( !entry.getValue().isHealthy() ) {
				responseStatus = StatusCodes.SERVICE_UNAVAILABLE;
				message = String.format( UNHEALTHY_MSG, entry.getKey(), entry.getValue().getMessage() );
				log.warn( message );
				break;
			}

		sendResponse( exchange, responseStatus, message );
	}

	void sendResponse( final HttpServerExchange exchange, final int responseStatus, final String message ) {
		exchange.setStatusCode( responseStatus );
		exchange.getResponseSender().send( message );
		exchange.endExchange();
	}
}
