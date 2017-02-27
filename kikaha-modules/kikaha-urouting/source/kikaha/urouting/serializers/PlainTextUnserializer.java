package kikaha.urouting.serializers;

import io.undertow.server.HttpServerExchange;
import kikaha.urouting.api.AbstractUnserializer;
import kikaha.core.modules.http.ContentType;
import kikaha.urouting.api.Mimes;
import kikaha.urouting.api.Unserializer;

import javax.enterprise.inject.Typed;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.Reader;

@ContentType(Mimes.PLAIN_TEXT)
@Singleton
@Typed( Unserializer.class )
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
