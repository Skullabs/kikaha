package kikaha.urouting;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import javax.inject.Inject;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.*;
import kikaha.core.test.KikahaRunner;
import kikaha.urouting.samples.HelloWorldUnserializer;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 */
@RunWith(KikahaRunner.class)
public class RoutingMethodParameterReaderTest {
	final byte[] bytes = "Hello World".getBytes();

	@Inject
	HelloWorldUnserializer unserializer;

	@Inject
	RoutingMethodParameterReader paramReader;

	@Test
	public void ensureThatIsAbleToUnserializeACustomContentType() throws IOException {
		final HttpServerExchange exchange = createExchange( "text/hello" );
		final String readParameter = paramReader.getBody(exchange, String.class, bytes);
		assertEquals( "Hello World", readParameter );
	}

	@Test
	public void ensureThatHaveCalledTheCustomContentTypeUnserializer() throws IOException {
		final HttpServerExchange exchange = createExchange( "text/hello" );
		paramReader.getBody(exchange, String.class, bytes);
		assertTrue( unserializer.isMethodCalled() );
	}

	@Test
	public void ensureThatCanFindTheUnserializerForAGivenContentTypeWithEncodingSuffix() throws IOException {
		final HttpServerExchange exchange = createExchange( "text/hello;charset=UTF-8" );
		paramReader.getBody(exchange, String.class, bytes);
		assertTrue( unserializer.isMethodCalled() );
	}

	private HttpServerExchange createExchange(String contentType){
		final HeaderMap requestHeaders = new HeaderMap();
		final HttpServerExchange exchange = new HttpServerExchange( null, requestHeaders, new HeaderMap(), 0);
		exchange.setRequestScheme( "http" );
		exchange.getRequestHeaders().add( Headers.CONTENT_TYPE, contentType );
		return exchange;
	}
}
