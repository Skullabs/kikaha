package kikaha.urouting.serializers.jackson;

import static kikaha.urouting.serializers.jackson.TestCase.readFile;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import java.io.*;
import java.nio.ByteBuffer;
import io.undertow.server.*;
import kikaha.core.cdi.*;
import kikaha.core.cdi.helpers.filter.Condition;
import kikaha.core.modules.http.ContentType;
import kikaha.core.test.HttpServerExchangeStub;
import kikaha.urouting.UndertowHelper;
import kikaha.urouting.api.*;
import kikaha.urouting.serializers.jackson.User.Address;
import lombok.SneakyThrows;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.xnio.conduits.StreamSinkConduit;

@RunWith( MockitoJUnitRunner.class )
public class SerializationTest {

	final ServiceProvider provider = new DefaultServiceProvider();
	final User user = new User( "gerolasdiwn",
			new Address( "Madison Avenue", 10 ) );

	@Spy ServerConnection connection;
	@Mock StreamSinkConduit conduit;
	@Mock BlockingHttpExchange blockingExchange;

	@Test
	@SneakyThrows
	public void grantThatSerializeItAsJSON() {
		final JSONHttpSerializer serializer = spy((JSONHttpSerializer)provider.load( Serializer.class, new JSONContentTypeCondition<>() ));
		final HttpServerExchange exchange = HttpServerExchangeStub.createHttpExchange();
		doAnswer( this::ensureThatWasCorrectlySerialized ).when(serializer).send( eq(exchange), any( ByteBuffer.class ));
		serializer.serialize( user, exchange, "UTF-8" );
	}

	Void ensureThatWasCorrectlySerialized(InvocationOnMock invocation) throws Throwable {
		final ByteBuffer buffer = invocation.getArgumentAt(1, ByteBuffer.class);
		final String expected = readFile( "serialization.expected-json.json" );
		final byte[] bytes = new byte[buffer.capacity()];
		buffer.get(bytes);
		assertEquals( expected, new String( bytes ) );
		return null;
	}

	@Test
	@SneakyThrows
	public void grantThatUnserializeJSONIntoObjectAsExpected() {
		final String json = readFile( "serialization.expected-json.json" );
		final InputStream inputStream = new ByteArrayInputStream(json.getBytes());
		doReturn( inputStream ).when(blockingExchange).getInputStream();
		final Unserializer unserializer = provider.load( Unserializer.class, new JSONContentTypeCondition<>() );
		final HttpServerExchange exchange = HttpServerExchangeStub.createHttpExchange();
		exchange.startBlocking( blockingExchange );
		final User user = unserializer.unserialize( exchange, User.class, json.getBytes(), "UTF-8" );
		assertIsValidUser( user );
	}

	void assertIsValidUser( final User user ) {
		assertNotNull( user );
		assertThat( user.name, is( "gerolasdiwn" ) );
		assertNotNull( user.addresses );
		assertThat( user.addresses.size(), is( 1 ) );
		final Address address = user.addresses.get( 0 );
		assertThat( address.street, is( "Madison Avenue" ) );
		assertThat( address.number, is( 10 ) );
	}

	class JSONContentTypeCondition<T> implements Condition<T> {

		@Override
		public boolean check(T arg0) {
			final ContentType contentType = arg0.getClass().getAnnotation( ContentType.class );
			return contentType != null && Mimes.JSON.equals( contentType.value() );
		}
	}
}
