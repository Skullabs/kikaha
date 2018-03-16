package kikaha.urouting.serializers;

import io.undertow.server.HttpServerExchange;
import kikaha.core.modules.http.ContentType;
import kikaha.urouting.api.Mimes;

import javax.enterprise.inject.Typed;
import javax.inject.Singleton;
import java.io.IOException;

@ContentType(Mimes.PLAIN_TEXT)
@Singleton
@Typed( Unserializer.class )
@SuppressWarnings("unchecked")
public class PlainTextUnserializer implements Unserializer {

	@Override
	public <T> T unserialize( HttpServerExchange input, Class<T> targetClass, byte[] body, String encoding ) throws IOException {
		if ( !String.class.equals(targetClass) ) {
			final String message = "Can't convert a plain text into " + targetClass.getCanonicalName();
			throw new UnsupportedOperationException(message);
		}
		return (T)new String( body, encoding );
	}
}
