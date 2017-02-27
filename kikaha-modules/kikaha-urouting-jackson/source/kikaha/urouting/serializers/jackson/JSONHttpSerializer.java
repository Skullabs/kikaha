package kikaha.urouting.serializers.jackson;

import javax.inject.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.undertow.server.HttpServerExchange;
import kikaha.core.modules.http.ContentType;
import kikaha.urouting.api.*;

@ContentType(Mimes.JSON)
@Singleton
public class JSONHttpSerializer implements Serializer, Unserializer {

	@Inject
	Jackson jackson;

	@Override
	public <T> void serialize(T object, HttpServerExchange exchange, String encoding) throws IOException {
		final ByteBuffer buffer = ByteBuffer.wrap( jackson.objectMapper().writeValueAsBytes(object) );
		send(exchange, buffer);
		exchange.endExchange();
	}

	public void send(final HttpServerExchange exchange, final ByteBuffer buffer) {
		exchange.getResponseSender().send( buffer );
	}

	@Override
	public <T> T unserialize( final HttpServerExchange exchange, final Class<T> targetClass, byte[] bodyData, final String encoding ) throws IOException {
		final ObjectMapper mapper = jackson.objectMapper();
		return mapper.readValue( bodyData, targetClass );
	}
}
