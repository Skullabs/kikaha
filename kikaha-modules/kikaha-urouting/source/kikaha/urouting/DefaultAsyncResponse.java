package kikaha.urouting;

import io.undertow.server.HttpServerExchange;
import kikaha.urouting.api.AsyncResponse;
import kikaha.urouting.api.Response;
import kikaha.urouting.api.RoutingException;
import kikaha.urouting.api.WrappedResponse;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
@AllArgsConstructor
@RequiredArgsConstructor
public class DefaultAsyncResponse implements AsyncResponse {

    final HttpServerExchange exchange;
    final RoutingMethodResponseWriter writer;
    final RoutingMethodExceptionHandler exceptionHandler;
    String contentType;

    @Override
    public void write( final Response response ) {
        try {
            writeWithTheRightContentType( response );
        } catch ( Throwable e ) {
            handleFailure( e );
        }
    }

    void writeWithTheRightContentType( Response response )
            throws RoutingException, IOException
    {
        if ( contentType != null )
            response = new WrappedResponse( response ).contentType( contentType );
        writer.write( exchange, response );
    }

    void handleFailure( Throwable e ) {
        log.error( e.getMessage(), e );
        try {
            writer.write( exchange, exceptionHandler.handle( e ) );
        } catch ( Throwable cause ) {
            log.error( cause.getMessage(), cause );
            exchange.endExchange();
        }
    }
}
