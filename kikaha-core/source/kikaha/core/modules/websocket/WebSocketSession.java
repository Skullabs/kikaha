package kikaha.core.modules.websocket;

import java.io.IOException;
import java.security.Principal;
import java.util.*;
import java.util.concurrent.*;
import io.undertow.websockets.core.*;
import io.undertow.websockets.spi.WebSocketHttpExchange;
import kikaha.commons.url.URLMatcher;
import lombok.*;
import lombok.experimental.Accessors;

@Getter
@Accessors( fluent = true )
@RequiredArgsConstructor
public class WebSocketSession {

	final WebSocketHttpExchange originalExchange;
	final Map<String, List<String>> requestHeaders;
	final Map<String, List<String>> responseHeaders;
	final Map<String, String> requestParameters;
	final URLMatcher urlMatcher;
	final String requestURI;
	final Principal userPrincipal;
	final WebSocketChannel channel;
	final Iterable<WebSocketChannel> peerConnections;
	final Serializer serializer;
	final Unserializer unserializer;
	final ExecutorService executorService;

	public WebSocketSession(final WebSocketHttpExchange originalExchange, final WebSocketChannel channel, final URLMatcher urlMatcher, Serializer serializer, Unserializer unserializer, ExecutorService executorService) {
		this.originalExchange = originalExchange;
		this.urlMatcher = urlMatcher;
		this.channel = channel;
		this.requestHeaders = originalExchange.getRequestHeaders();
		this.responseHeaders = originalExchange.getResponseHeaders();
		this.userPrincipal = originalExchange.getUserPrincipal();
		this.requestURI = channel.getUrl();
		this.peerConnections = retrievePeerConnectionsForCurrentURLRequest( channel );
		this.requestParameters = extractRequestParameters( channel );
		this.serializer = serializer;
		this.unserializer = unserializer;
		this.executorService = executorService;
	}

	Map<String, String> extractRequestParameters( final WebSocketChannel channel ) {
		final String url = channel.getUrl();
		final Map<String, String> foundParameters = new HashMap<>();
		if ( !urlMatcher.matches( url, foundParameters ) )
			throw new UnsupportedOperationException( "There was a huge mistake on implementation: it doesn't matched URL: " + url );
		return foundParameters;
	}

	/**
	 * Create a new {@link WebSocketSession} instance holding only
	 * {@code peerConnections} related to {@link WebSocketChannel#getUrl()}
	 * data.
	 *
	 * @param channel
	 * @return
	 */
	public WebSocketSession channel( final WebSocketChannel channel ) {
		final List<WebSocketChannel> hashSet = retrievePeerConnectionsForCurrentURLRequest( channel );
		return cloneWith( channel, channel.getUrl(), hashSet );
	}

	List<WebSocketChannel> retrievePeerConnectionsForCurrentURLRequest( final WebSocketChannel channel ) {
		final List<WebSocketChannel> hashSet = new ArrayList<>();
		for ( final WebSocketChannel peer : channel.getPeerConnections() )
			if ( channel.getUrl().equals( peer.getUrl() ) )
				hashSet.add( peer );
		return hashSet;
	}

	WebSocketSession cloneWith( final WebSocketChannel channel, final String requestURI, final Iterable<WebSocketChannel> peerConnections ) {
		final Map<String, String> requestParameters = extractRequestParameters( channel );
		return new WebSocketSession(
			null, requestHeaders, null, requestParameters, urlMatcher, requestURI,
			userPrincipal, channel, peerConnections, serializer, unserializer, executorService );
	}

	/**
	 * Runs the {@code runnable} parameter in a worker thread. Useful to avoid
	 * IO bound to block the execution at the Undertow's IO threads.
	 * @param runnable
	 */
	public Future<?> runInWorkerThreads(Runnable runnable ){
		return executorService.submit( runnable );
	}

	/**
	 * Prepare to send a message to someone.
	 *
	 * @param message
	 * @return
	 */
	public Sender send( final String message ) {
		return new Sender( message );
	}

	/**
	 * Prepare to send an object as message to someone. The {@code object} will
	 * be serialized before send to the listening Peers.
	 *
	 * @param message
	 * @return
	 */
	public Sender send( final Object message ) {
		try {
			return send( serializer.serialize( message ) );
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Send a message to all Peer Connections to current {@code requestURI}.
	 *
	 * @param message
	 */
	public void broadcast( final String message ) {
		for ( final WebSocketChannel peer : peerConnections() )
			WebSockets.sendText( message, peer, null );
	}

	/**
	 * Send a message to all Peer Connections to current {@code requestURI}.
	 *
	 * @param message
	 */
	public void broadcast( final Object message ) {
		for ( final WebSocketChannel peer : peerConnections() )
			send( message ).to( peer );
	}

	/**
	 * A message holder object that allows developers to send message to one
	 * or more Peer.
	 */
	@RequiredArgsConstructor
	public class Sender {

		final String message;

		/**
		 * Send the prepared message to respective {@code peers}.
		 *
		 * @param peers
		 */
		public void to( final WebSocketChannel... peers ) {
			for ( final WebSocketChannel peer : peers )
				WebSockets.sendText( message, peer, null );
		}
	}

	/**
	 * Unserializers allows module developers to provide a simple way to
	 * unserialize the incoming request data into a more convenient object.
	 */
	public interface Unserializer {
		<T> T unserialize( String data, Class<T> expectedClass ) throws IOException;
	}

	/**
	 * Serializers convert
	 */
	public interface Serializer {
		String serialize( Object object ) throws IOException;
	}
}