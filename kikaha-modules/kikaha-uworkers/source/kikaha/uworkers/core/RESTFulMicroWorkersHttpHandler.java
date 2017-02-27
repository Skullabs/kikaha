package kikaha.uworkers.core;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import kikaha.urouting.SimpleExchange;
import kikaha.urouting.UndertowHelper;
import kikaha.urouting.api.DefaultResponse;
import kikaha.urouting.api.Response;
import kikaha.uworkers.local.LocalExchange;
import lombok.RequiredArgsConstructor;

/**
 * Exposes uWorker's endpoint as REST API.
 */
@RequiredArgsConstructor( staticName = "with" )
public class RESTFulMicroWorkersHttpHandler implements HttpHandler {

    final static Response TOO_MANY_REQUESTS = DefaultResponse.response( 429 )
            .entity( "This uWorker endpoint has reached its limit. Try again later." );

    final UndertowHelper helper;
    final RESTFulEndpointInboxSupplier inbox;

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        final SimpleExchange simpleExchange = helper.simplify(exchange);
        simpleExchange.receiveRequest( (ex, data) ->{
            final LocalExchange localExchange = LocalExchange.create().then( RESTFulUWorkerResponse.to(simpleExchange) );
            final RESTFulExchange restFulExchange = new RESTFulExchange(localExchange, simpleExchange, data);
            if ( !inbox.poll( restFulExchange ) )
                simpleExchange.sendResponse( TOO_MANY_REQUESTS );
        });
    }
}
