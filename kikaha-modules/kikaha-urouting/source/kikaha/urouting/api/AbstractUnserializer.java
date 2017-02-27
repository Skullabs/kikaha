package kikaha.urouting.api;

import java.io.*;
import io.undertow.server.HttpServerExchange;

public abstract class AbstractUnserializer implements Unserializer {

	@Override
	public <T> T unserialize( HttpServerExchange input, Class<T> targetClass, byte[] body, String encoding ) throws IOException {
		final String content = new String( body, encoding );
		final Reader reader = new StringReader( content );
		return unserialize(reader, targetClass);
	}

	public abstract <T> T unserialize( final Reader input, final Class<T> targetClass ) throws IOException;
}
