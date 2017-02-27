package kikaha.urouting.api;

import java.io.IOException;
import io.undertow.server.HttpServerExchange;

public interface Unserializer {

	<T> T unserialize( final HttpServerExchange input, final Class<T> targetClass, byte[] body, String encoding ) throws IOException;
}
