package kikaha.urouting.serializers;

import java.io.IOException;
import io.undertow.server.HttpServerExchange;

public interface Serializer {

	<T> void serialize(final T object, final HttpServerExchange output, String encoding) throws IOException;
}