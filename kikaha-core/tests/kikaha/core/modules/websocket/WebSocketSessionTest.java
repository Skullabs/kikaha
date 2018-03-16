package kikaha.core.modules.websocket;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.security.Principal;
import java.util.*;
import java.util.concurrent.*;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.spi.WebSocketHttpExchange;
import kikaha.commons.url.URLMatcher;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith( MockitoJUnitRunner.class )
// UNCHECKED: too long methods
public class WebSocketSessionTest {

	static final String URL_1 = "ws://localhost/url/1";
	static final String URL_2 = "ws://127.0.0.1:81/url/2";

	@Mock
	WebSocketHttpExchange exchange;

	@Mock
	WebSocketChannel channel;

	@Mock
	WebSocketChannel channelUrl1;

	@Mock
	WebSocketChannel channelUrl2;

	@Mock
	WebSocketSession.Serializer serializer;

	@Mock
	WebSocketSession.Unserializer unserializer;

	@Mock
	Map<String, List<String>> requestHeaders;

	@Mock
	Map<String, List<String>> responseHeaders;

	@Mock
	Principal userPrincipal;

	@Mock
	ExecutorService executorService;

	final Map<String, String> requestParameters = new HashMap<>();

	@Before
	public void configurePeerSessions() {
		doReturn( URL_1 ).when( channel ).getUrl();
		doReturn( URL_1 ).when( channelUrl1 ).getUrl();
		doReturn( URL_2 ).when( channelUrl2 ).getUrl();
		populatePeerConnections( channel );
	}

	@Before
	public void populateExchange() {
		doReturn( requestHeaders ).when( exchange ).getRequestHeaders();
		doReturn( requestParameters ).when( exchange ).getRequestParameters();
		doReturn(responseHeaders).when( exchange ).getResponseHeaders();
		doReturn( userPrincipal ).when( exchange ).getUserPrincipal();
	}

	@Test
	public void shouldSubmitTaskThroughTheExecutorService() throws ExecutionException, InterruptedException {
		final Runnable runnable = mock(Runnable.class);

		final Future mockedFuture = mock(Future.class);
		doReturn( null ).when( mockedFuture ).get();
		doAnswer( a -> {
			runnable.run();
			return mockedFuture;
		}).when( executorService ).submit( eq(runnable) );

		final WebSocketSession session = createSession();
		final Future<?> future = session.runInWorkerThreads(runnable);
		future.get();

		verify( runnable ).run();
	}

	@Test
	public void ensureThatRequiredFieldsFromExchangeWasDelegatedToSession() {
		requestParameters.put( "id", "1" );
		final WebSocketSession session = createSession();
		assertThat( session.originalExchange(), is( exchange ) );
		assertThat( session.requestHeaders(), is( requestHeaders ) );
		assertThat( session.responseHeaders(), is(responseHeaders) );
		assertThat( session.userPrincipal(), is( userPrincipal ) );
		assertThatHasSameElements( session.requestParameters(), requestParameters );

		final Iterable<WebSocketChannel> connections = session.peerConnections();
		final Iterator<WebSocketChannel> iterator = connections.iterator();
		assertThat( iterator.next().getUrl(), is( URL_1 ) );
		assertThat( iterator.hasNext(), is( false ) );
	}

	@Test
	public void ensureThatExchangeAndChannelAndResponseHeadersWereCleanedAsExpected() {
		final WebSocketChannel newChannel = mock( WebSocketChannel.class );
		doReturn( URL_2 ).when( newChannel ).getUrl();
		populatePeerConnections( newChannel );

		final WebSocketSession originalSession = createSession();
		assertThat( originalSession.requestURI(), is( URL_1 ) );
		final WebSocketSession clonedSession = originalSession.channel( newChannel );
		assertNotSame( originalSession, clonedSession );
		assertThat( clonedSession.requestURI(), is( URL_2 ) );
		assertThat( clonedSession.requestHeaders(), is( requestHeaders ) );
		assertThat( clonedSession.userPrincipal(), is( userPrincipal ) );
		assertThat( clonedSession.peerConnections(), notNullValue() );
		assertThat( clonedSession.responseHeaders(), nullValue() );
		assertThat( clonedSession.originalExchange(), nullValue() );

		requestParameters.put( "id", "2" );
		assertThatHasSameElements( clonedSession.requestParameters(), requestParameters );

		final Iterable<WebSocketChannel> connections = clonedSession.peerConnections();
		final Iterator<WebSocketChannel> iterator = connections.iterator();
		assertThat( iterator.hasNext(), is( true ) );
		assertThat( iterator.next().getUrl(), is( URL_2 ) );
		assertThat( iterator.hasNext(), is( false ) );
	}

	WebSocketSession createSession() {
		final URLMatcher matcher = URLMatcher.compile( "{protocol}://{host}/url/{id}" );
		return new WebSocketSession( exchange, channel, matcher, serializer, unserializer, executorService );
	}

	void populatePeerConnections( final WebSocketChannel webSocketChannel ) {
		final Set<WebSocketChannel> peerConnections = new HashSet<>();
		peerConnections.add( channelUrl1 );
		peerConnections.add( channelUrl2 );
		doReturn( peerConnections ).when( webSocketChannel ).getPeerConnections();
	}

	void assertThatHasSameElements( final Map<String, String> current, final Map<String, String> expected ) {
		for ( final String key : expected.keySet() ) {
			final String currentKeyValue = current.get( key );
			if ( !expected.get( key ).equals( currentKeyValue ) )
				fail( "Key " + key + " from " + current + " doesn't match expected map value " + currentKeyValue );
		}
	}
}