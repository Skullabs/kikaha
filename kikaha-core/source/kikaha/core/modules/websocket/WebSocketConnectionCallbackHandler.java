package kikaha.core.modules.websocket;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import io.undertow.websockets.WebSocketConnectionCallback;
import io.undertow.websockets.core.*;
import io.undertow.websockets.spi.WebSocketHttpExchange;
import kikaha.commons.url.URLMatcher;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class WebSocketConnectionCallbackHandler
	implements WebSocketConnectionCallback {

	final WebSocketHandler handler;
	final URLMatcher urlMatcher;
	final WebSocketSession.Serializer serializer;
	final WebSocketSession.Unserializer unserializer;
	final ExecutorService executorService;

	@Override
	public void onConnect( final WebSocketHttpExchange exchange, final WebSocketChannel channel ) {
		final WebSocketSession session = createSession( exchange, channel );
		handler.onOpen( session );
		channel.getReceiveSetter().set( createListener( session ) );
		channel.resumeReceives();
	}

	WebSocketSession createSession( final WebSocketHttpExchange exchange, final WebSocketChannel channel ) {
		return new WebSocketSession( exchange, channel, urlMatcher, serializer, unserializer, executorService );
	}

	DelegatedReceiveListener createListener( final WebSocketSession session ) {
		return new DelegatedReceiveListener( handler, session );
	}
}

@RequiredArgsConstructor
class DelegatedReceiveListener extends AbstractReceiveListener {

	final WebSocketHandler handler;
	final WebSocketSession session;

	@Override
	public void onFullTextMessage( final WebSocketChannel channel, final BufferedTextMessage message ) throws IOException {
		handler.onText( session.channel( channel ), message.getData() );
	}

	@Override
	protected void onCloseMessage( final CloseMessage cm, final WebSocketChannel channel ) {
		handler.onClose( session.channel( channel ), cm );
	}
}