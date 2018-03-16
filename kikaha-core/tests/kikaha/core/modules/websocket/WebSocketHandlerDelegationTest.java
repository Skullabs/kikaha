package kikaha.core.modules.websocket;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import java.util.concurrent.ExecutorService;
import io.undertow.websockets.core.BufferedTextMessage;
import io.undertow.websockets.core.CloseMessage;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.spi.WebSocketHttpExchange;
import kikaha.commons.url.URLMatcher;
import lombok.SneakyThrows;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.xnio.ChannelListener.Setter;

/**
 * Scope: Ensure that is possible to wrap a {@link WebSocketHandler} object and
 * delegate all WebSocket events to it.
 *
 * @author Miere Teixeira
 */
@RunWith( MockitoJUnitRunner.class )
public class WebSocketHandlerDelegationTest {

	@Mock
	Setter<WebSocketChannel> setter;

	@Mock
	WebSocketHandler delegated;

	@Mock
	WebSocketHttpExchange exchange;

	@Mock
	WebSocketChannel channel;

	@Mock
	WebSocketSession session;

	@Mock
	WebSocketSession.Serializer serializer;

	@Mock
	WebSocketSession.Unserializer unserializer;

	@Mock
	ExecutorService executorService;

	WebSocketConnectionCallbackHandler callbackHandler;

	@Before
	public void setup() {
		doReturn( "/websocket" ).when( exchange ).getRequestURI();
		doReturn( setter ).when( channel ).getReceiveSetter();
		callbackHandler = spy( new WebSocketConnectionCallbackHandler(
				delegated, URLMatcher.compile( "" ), serializer, unserializer, executorService ) );
	}

	@Test
	public void ensureThatCanDelegateOnOpenEventToWebSocketHandlerObject() {
		doReturn( session ).when( callbackHandler ).createSession( eq( exchange ), eq( channel ) );
		callbackHandler.onConnect( exchange, channel );
		verify( delegated ).onOpen( eq( session ) );
		verify( setter ).set( isA( DelegatedReceiveListener.class ) );
	}

	@Test
	@SneakyThrows
	public void ensureThatCanDeleteOnTextEventToWebSocketHandlerObject() {
		doReturn( session ).when( session ).channel( any( WebSocketChannel.class ) );
		final BufferedTextMessage message = mock( BufferedTextMessage.class );
		final DelegatedReceiveListener listener = new DelegatedReceiveListener( delegated, session );
		listener.onFullTextMessage( channel, message );
		verify( delegated ).onText( eq( session ), anyString() );
	}

	@Test
	public void ensureThatCanDeleteOnCloseEventToWebSocketHandlerObject() {
		doReturn( session ).when( session ).channel( any( WebSocketChannel.class ) );
		final CloseMessage message = mock( CloseMessage.class );
		final DelegatedReceiveListener listener = new DelegatedReceiveListener( delegated, session );
		listener.onCloseMessage( message, channel );
		verify( delegated ).onClose( eq( session ), eq( message ) );
	}
}