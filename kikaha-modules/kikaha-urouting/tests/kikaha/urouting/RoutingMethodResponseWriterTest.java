package kikaha.urouting;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.*;
import kikaha.core.test.HttpServerExchangeStub;
import kikaha.core.test.KikahaRunner;
import kikaha.urouting.api.Header;
import kikaha.urouting.api.Mimes;
import kikaha.urouting.api.Response;
import kikaha.urouting.unit.samples.TodoResource;
import kikaha.urouting.unit.samples.TodoResource.Todo;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@Slf4j
@SuppressWarnings("unchecked")
@RunWith( KikahaRunner.class )
public class RoutingMethodResponseWriterTest {

	static final String DEFAULT_CONFIGURED_MIME = "configured/mime";

	final HttpServerExchange exchange = createHttpExchange();

	@Inject RoutingMethodResponseWriter writer;
	@Inject TodoResource resource;

	@Before
	public void setup() {
		writer.defaultContentType = DEFAULT_CONFIGURED_MIME;
		writer = spy( writer );
	}

	private HttpServerExchange createHttpExchange() {
		final HttpServerExchange httpServerExchange = HttpServerExchangeStub.createHttpExchange();
		httpServerExchange.setRequestMethod(new HttpString("GET"));
		httpServerExchange.setProtocol(Protocols.HTTP_1_1);
		return httpServerExchange;
	}

	@Test
	@SneakyThrows
	public void ensureThatCanCallPOSTResourceAndHaveItsHeadersAndStatusSentAsExpected() {
		doNothing().when(writer).sendBodyResponse( any(),any(),any(),any() );

		final Todo todo = new Todo( "Frankenstein" );
		final Response response = resource.persistTodo(todo);
		write(response);
		verify( writer ).sendStatusCode( any(), eq(201) );
		verify( writer, atLeastOnce() ).sendHeader( any(HeaderMap.class), any(Header.class), any(String.class) );
		verify( writer ).sendBodyResponse( eq( exchange ), eq( DEFAULT_CONFIGURED_MIME ), anyString(), eq( response.entity() ) );

		final HeaderValues contentType = exchange.getResponseHeaders().get(Headers.CONTENT_TYPE);
		assertEquals( DEFAULT_CONFIGURED_MIME, contentType.getFirst() );
	}

	@SneakyThrows
	private void write(final Response response) {
		try {
			exchange.startBlocking();
			writer.write(exchange, response );
		} catch ( final NullPointerException cause ) {
			log.error( cause.getMessage(), cause );
		}
	}

	@Test
	@SneakyThrows
	public void ensureThatCanCallGETResourceAndHaveItsHeadersAndStatusSentAsExpected() {
		doNothing().when(writer).sendBodyResponse( any(),any(),any(),any() );

		final Todo todo = new Todo( "Frankenstein" );
		resource.persistTodo(todo);
		final Todo persistedTodo = resource.getTodo(todo.getId());
		write( persistedTodo );
		verify( writer ).sendStatusCode( any(), eq(200) );
		verify( writer, atLeastOnce() ).sendContentTypeHeader( any(), eq(Mimes.PLAIN_TEXT));
	}

	@SneakyThrows
	private void write(final Todo response) {
		try {
			exchange.startBlocking();
			writer.write(exchange, Mimes.PLAIN_TEXT, response );
		} catch ( final NullPointerException cause ) {
			log.error( cause.getMessage() );
		}
	}
}