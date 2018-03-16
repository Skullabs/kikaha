package kikaha.core;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import java.util.List;
import io.undertow.server.*;
import io.undertow.util.Methods;
import kikaha.core.Entry;
import kikaha.core.SimpleRoutingHandler;
import kikaha.core.test.HttpServerExchangeStub;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class SimpleRoutingHandlerTest {

	SimpleRoutingHandler handler;
	@Mock HttpHandler httpHandler;
	@Mock HttpHandler simplerHttpHandler;

	@Before
	public void configureRouting(){
		handler = new SimpleRoutingHandler();
		handler.add(Methods.GET, "/user/{id}", simplerHttpHandler );
		handler.add(Methods.GET, "/user/{id}/details", httpHandler );
	}

	@Test
	public void simplerRulesWillBeTheLastOnes(){
		final List<Entry> entries = handler.matchersByMethod.get(Methods.GET);
		assertEquals( httpHandler, entries.get(0).getHandler() );
		assertEquals( simplerHttpHandler, entries.get(1).getHandler() );
	}

	@Test
	public void canMatchTheMostComplexRule() throws Exception {
		final HttpServerExchange exchange = HttpServerExchangeStub.createHttpExchange();
		exchange.setRelativePath( "/user/123/details" );
		handler.handleRequest( exchange );
		verify( httpHandler ).handleRequest( eq(exchange) );
	}

	@Test
	public void canMatchTheSimplerRule() throws Exception {
		final HttpServerExchange exchange = HttpServerExchangeStub.createHttpExchange();
		exchange.setRelativePath( "/user/123" );
		handler.handleRequest( exchange );
		verify( simplerHttpHandler ).handleRequest( eq(exchange) );
	}
}