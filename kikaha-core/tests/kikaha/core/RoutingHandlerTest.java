package kikaha.core;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.RoutingHandler;
import io.undertow.util.HttpString;
import kikaha.core.test.HttpServerExchangeStub;
import lombok.NonNull;
import lombok.SneakyThrows;

import org.junit.Test;

public class RoutingHandlerTest {

	final HttpServerExchange exchange = createExchange();

	private HttpServerExchange createExchange() {
		final HttpServerExchange exchange = HttpServerExchangeStub.createHttpExchange();
		exchange.setRequestMethod( new HttpString( "GET" ) );
		exchange.setRelativePath( "/sameurl" );
		return exchange;
	}

	@Test
	public void ensureItsPossibleToHandlePostAndGetToSameURLWithRoutingHandler() {
		final MyHandler myHandler = new MyHandler();
		final RoutingHandler router = new RoutingHandler();
		router.add( "GET", "/sameurl", myHandler );
		router.add( "POST", "/sameurl", myHandler );
		router.add( "GET", "/sameurl/{id}/", myHandler );

		doStressedRequestTest( router );
	}

	/**
	 * Repetitive test to ensure that some JIT have been applied to this test
	 * context and avoid comparing each other with different optimizations.
	 */
	@Test
	public void ensureItsPossibleToHandlePostAndGetToSameURLWithRoutingHandler2() {
		ensureItsPossibleToHandlePostAndGetToSameURLWithRoutingHandler();
	}

	@Test
	@SneakyThrows
	public void ensureItsPossibleToHandlePostAndGetToSameURLWithSimpleRoutingHandler() {
		final HttpHandler failureHandler = mock( HttpHandler.class );
		final HttpHandler successHandler = mock( HttpHandler.class );

		final SimpleRoutingHandler router = new SimpleRoutingHandler();
		router.setFallbackHandler( failureHandler );

		router.add( "POST", "/sameurl/", failureHandler );
		router.add( "GET", "/sameurl/{id}", failureHandler );
		router.add( "GET", "/sameurl", successHandler );

		router.handleRequest( exchange );
		verify( failureHandler, never() ).handleRequest( any( HttpServerExchange.class ) );
		verify( successHandler ).handleRequest( any( HttpServerExchange.class ) );
	}

	@SneakyThrows
	private void doStressedRequestTest( @NonNull final HttpHandler router ) {
		for ( int i = 0; i < 1000000; i++ )
			router.handleRequest( exchange );
	}
}

class MyHandler implements HttpHandler {

	@Override
	public void handleRequest( final HttpServerExchange exchange ) throws Exception {
	}
}