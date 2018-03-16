package kikaha.urouting.serializers;

import io.undertow.server.HttpServerExchange;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Blocking serializer abstraction.
 */
public abstract class AbstractSerializer implements Serializer {

	@Override
	public <T> void serialize(final T object, final HttpServerExchange exchange, String encoding) throws IOException {
		if ( !exchange.isBlocking() )
			exchange.startBlocking();
		final OutputStream outputStream = exchange.getOutputStream();
		serialize( object, UncloseableWriterWrapper.wrap( outputStream ) );
		outputStream.flush();
		exchange.endExchange();
	}

	abstract public <T> void serialize( final T object, final OutputStream output ) throws IOException;

    @RequiredArgsConstructor( staticName="wrap" )
    private static class UncloseableWriterWrapper extends OutputStream {

        @Delegate( excludes=Closeable.class )
        final OutputStream outputStream;

        @Override
        public void close() {
        }
    }
}
