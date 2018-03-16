package kikaha.urouting.unit.samples;

import javax.inject.Singleton;
import java.io.IOException;
import io.undertow.server.HttpServerExchange;
import kikaha.core.modules.http.ContentType;
import kikaha.urouting.serializers.Unserializer;
import lombok.Getter;

/**
 *
 */
@Singleton
@ContentType("text/hello")
public class HelloWorldUnserializer implements Unserializer {

	@Getter
	volatile boolean methodCalled;

	@Override
	@SuppressWarnings( "unchecked" )
	public <T> T unserialize( HttpServerExchange input, Class<T> targetClass, byte[] body, String encoding ) throws IOException {
		methodCalled = true;
		return (T)new String( body, encoding );
	}
}
