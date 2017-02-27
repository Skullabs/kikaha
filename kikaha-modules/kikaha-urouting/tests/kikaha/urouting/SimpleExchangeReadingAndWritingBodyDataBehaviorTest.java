package kikaha.urouting;

import io.undertow.server.HttpServerExchange;
import kikaha.core.test.HttpServerExchangeStub;
import kikaha.urouting.api.DefaultResponse;
import kikaha.urouting.api.Serializer;
import kikaha.urouting.api.Unserializer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

/**
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class SimpleExchangeReadingAndWritingBodyDataBehaviorTest {

	private static final String CONTENT_TYPE = "any-content-type";
	final byte[] bytes = "Hello World".getBytes();

	@Spy RoutingMethodParameterReader parameterReader;
	@Spy RoutingMethodResponseWriter responseWriter;
	RoutingMethodExceptionHandler exceptionHandler = new RoutingMethodExceptionHandler( null, null );

	@Mock SerializerAndUnserializerProvider serializerAndUnserializerProvider;
	@Mock Unserializer unserializer;
	@Mock Serializer serializer;

	@Before
	public void configureRequestReaderAndResponseWriter() throws IOException {
		doReturn( unserializer ).when( serializerAndUnserializerProvider ).getUnserializerFor( eq(CONTENT_TYPE) );
		parameterReader.serializerAndUnserializerProvider = serializerAndUnserializerProvider;
		doReturn( serializer ).when( serializerAndUnserializerProvider ).getSerializerFor( eq(CONTENT_TYPE) );
		responseWriter.serializerAndUnserializerProvider = serializerAndUnserializerProvider;
	}

	@Test
	public void ensureThatIsAbleToReadAllPostData() throws IOException {
		final HttpServerExchange request = createExchange();
		final SimpleExchange exchange = SimpleExchange.wrap( request, parameterReader, responseWriter, exceptionHandler );
		doReturn( "Hello World" ).when( unserializer ).unserialize( eq(request), eq(String.class), eq(bytes), anyString() );
		assertEquals( "Hello World", exchange.getRequestBody( String.class, bytes, CONTENT_TYPE ) );
	}

	@Test
	public void ensureThatIsAbleToSendResponse() throws IOException {
		final HttpServerExchange request = createExchange();
		final SimpleExchange exchange = SimpleExchange.wrap( request, parameterReader, responseWriter, exceptionHandler );
		exchange.sendResponse( DefaultResponse.ok( "Hello World" ).contentType( CONTENT_TYPE ) );
		verify( serializer ).serialize( eq("Hello World"), eq(request), anyString() );
	}

	@Test
	public void ensureThatIsAbleToSendResponseWithRawObject() throws IOException {
		final HttpServerExchange request = createExchange();
		final SimpleExchange exchange = SimpleExchange.wrap( request, parameterReader, responseWriter, exceptionHandler );
		exchange.sendResponse("Hello World", CONTENT_TYPE);
		verify( serializer ).serialize( eq("Hello World"), eq(request), anyString() );
	}

	private HttpServerExchange createExchange() {
		return HttpServerExchangeStub.createHttpExchange();
	}
}