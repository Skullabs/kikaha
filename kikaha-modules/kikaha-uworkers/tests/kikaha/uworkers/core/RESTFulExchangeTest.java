package kikaha.uworkers.core;

import kikaha.urouting.SimpleExchange;
import kikaha.uworkers.api.Response;
import kikaha.uworkers.local.LocalExchange;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Map;
import java.util.function.BiConsumer;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link RESTFulExchange}.
 */
@RunWith(MockitoJUnitRunner.class)
public class RESTFulExchangeTest {

    final byte[] helloWorld = "Hello World".getBytes();

    @Mock SimpleExchange simpleExchange;
    LocalExchange localExchange;
    RESTFulExchange exchange;

    @Before
    public void setup() {
        localExchange = spy( LocalExchange.of( null ) );
        doReturn( null ).when( localExchange ).response();
        exchange = new RESTFulExchange( localExchange, simpleExchange, helloWorld );
    }

    @Test
    public void request() throws Exception {
        exchange.request();
        verify( simpleExchange ).getRequestBody( eq(Map.class), eq(helloWorld) );
    }

    @Test
    public void requestAs() throws Exception {
        exchange.requestAs( Object.class );
        verify( simpleExchange ).getRequestBody( eq(Object.class), eq(helloWorld) );
    }

    @Test
    public void response() throws Exception {
        exchange.response();
        verify( localExchange ).response();
    }

    @Test
    public void responseAs() throws Exception {
        exchange.responseAs( Object.class );
        verify( localExchange ).responseAs( eq(Object.class) );
    }

    @Test
    public void then() throws Exception {
        final BiConsumer<Response.UndefinedObject, Throwable> consumer = (r, t) -> {};
        exchange.then( consumer );
        verify( localExchange ).then( eq( consumer ) );
    }

    @Test
    public void reply() throws Exception {
        final Object response = new Object();
        exchange.reply( response );
        verify( localExchange ).reply( eq(response) );
    }

    @Test
    public void reply1() throws Exception {
        final RuntimeException response = new RuntimeException();
        exchange.reply( response );
        verify( localExchange ).reply( eq(response) );
    }

}