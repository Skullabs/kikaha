package kikaha.core.modules.smart;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

import java.util.Map;

import kikaha.commons.url.StringCursor;
import kikaha.commons.url.URLMatcher;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class VirtualHostMatcher implements RequestMatcher {

	final URLMatcher virtualHost;

	@Override
	public Boolean apply( final HttpServerExchange exchange, final Map<String, String> properties )
	{
		val hostHeader = exchange.getRequestHeaders().getFirst( Headers.HOST );
		if ( hostHeader == null ) {
			log.warn( "No HOST header found." );
			return true;
		}
		val host = stripHostFromHeader( hostHeader );
		return virtualHost.matches( host, properties );
	}

	String stripHostFromHeader( final String hostHeader )
	{
		val cursor = new StringCursor( hostHeader );
		if ( !cursor.shiftCursorToNextChar( ':' ) )
			cursor.end();
		else
			cursor.prev();
		val host = cursor.substringUntilCursor();
		return host;
	}

	@Override
	public String toString() {
		return "virtual-host=" + virtualHost;
	}

	public static RequestMatcher from(final String path )
	{
		return new VirtualHostMatcher( URLMatcher.compile( path ) );
	}
}
