package kikaha.urouting.serializers.jackson;

import java.io.IOException;
import java.nio.ByteBuffer;
import javax.inject.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.undertow.server.HttpServerExchange;
import kikaha.core.modules.http.ContentType;
import kikaha.urouting.api.*;
import kikaha.urouting.serializers.Serializer;
import kikaha.urouting.serializers.Unserializer;

@ContentType(Mimes.JSON)
@Singleton
public class JSONHttpSerializer implements Serializer, Unserializer {

	@Inject
	Jackson jackson;

	@Override
	public <T> void serialize(T object, HttpServerExchange exchange, String encoding) throws IOException {
		final byte[] bytes = jackson.objectMapper().writeValueAsBytes(object);
		send(exchange, ByteBuffer.wrap(bytes));
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
