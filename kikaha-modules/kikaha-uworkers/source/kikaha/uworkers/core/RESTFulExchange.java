package kikaha.uworkers.core;

import kikaha.urouting.SimpleExchange;
import kikaha.urouting.api.DefaultResponse;
import kikaha.uworkers.api.Exchange;
import kikaha.uworkers.api.Response;
import kikaha.uworkers.local.LocalExchange;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * An {@link Exchange} implementation that bridges the communication
 * between the REST interface and the uWorkers endpoint.
 */
@RequiredArgsConstructor
public class RESTFulExchange implements Exchange {

    final LocalExchange localExchange;
    final SimpleExchange exchange;
    final byte[] bytes;

    @Override
    @SuppressWarnings("unchecked")
    public <REQ> REQ request() {
        try {
            return (REQ)exchange.getRequestBody( Map.class, bytes );
        } catch (IOException e) {
            throw new IllegalStateException( e );
        }
    }

    @Override
    public <REQ> REQ requestAs(Class<REQ> targetClass) throws IOException {
        try {
            return exchange.getRequestBody( targetClass, bytes );
        } catch (IOException e) {
            throw new IllegalStateException( e );
        }
    }

    @Override
    public <RESP> RESP response() {
        return localExchange.response();
    }

    @Override
    public <RESP> RESP responseAs(Class<RESP> targetClass) {
        return localExchange.responseAs( targetClass );
    }

    @Override
    public Response then(BiConsumer<UndefinedObject, Throwable> listener) {
        return localExchange.then( listener );
    }

    @Override
    public <RESP> Exchange reply(RESP response) {
        return localExchange.reply( response );
    }

    @Override
    public Exchange reply(Throwable error) {
        return localExchange.reply( error );
    }
}

@Slf4j
@RequiredArgsConstructor( staticName = "to" )
class RESTFulUWorkerResponse implements BiConsumer<Response.UndefinedObject, Throwable> {

    final static kikaha.urouting.api.Response NO_CONTENT = DefaultResponse.noContent();
    final SimpleExchange exchange;

    @Override
    public void accept(Response.UndefinedObject undefinedObject, Throwable throwable) {
        try {
            if (throwable != null)
                exchange.sendResponse(throwable);
            else {
                final Object object = undefinedObject.get();
                if ( object == null )
                    exchange.sendResponse( NO_CONTENT );
                else
                    exchange.sendResponse( object );
            }
        } catch ( Throwable cause ) {
            log.error( "Could not send response", cause );
        } finally {
            exchange.endExchange();
        }
    }
}