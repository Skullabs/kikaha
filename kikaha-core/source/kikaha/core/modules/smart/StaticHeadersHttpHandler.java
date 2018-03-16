package kikaha.core.modules.smart;

import java.util.*;
import java.util.Map.Entry;
import io.undertow.server.*;
import io.undertow.util.HttpString;
import kikaha.commons.url.URLMatcher;
import lombok.RequiredArgsConstructor;

/**
 * @author: miere.teixeira
 */
@RequiredArgsConstructor
public class StaticHeadersHttpHandler implements HttpHandler {

	final HttpHandler nextHandler;
	final URLMatcher matcher;
	final Map<String, URLMatcher> headers;

	@Override
	public void handleRequest( HttpServerExchange exchange ) throws Exception {
		sendHeaders( exchange );
		nextHandler.handleRequest( exchange );
	}

	private void sendHeaders( HttpServerExchange exchange ){
		final String relativePath = exchange.getRelativePath();
		final Map<String, String> variables = new HashMap<>();
		if ( matcher.matches( relativePath, variables ) ) {
			for ( final Entry<String, URLMatcher> entry : headers.entrySet() ) {
				final HttpString header = new HttpString( entry.getKey() );
				final String value = entry.getValue().replace( variables );
				exchange.getResponseHeaders().add( header, value );
			}
		}
	}

	public static HttpHandler create( HttpHandler next, String url, Map<String, Object> headers ) {
		final Map<String, URLMatcher> convertedHeaders = new HashMap<>();
		for ( Entry<String, Object> header : headers.entrySet() ){
			final URLMatcher matcher = URLMatcher.compile( String.valueOf( header.getValue() ) );
			convertedHeaders.put( header.getKey(), matcher );
		}

		final URLMatcher urlMatcher = URLMatcher.compile( url );
		return new StaticHeadersHttpHandler( next, urlMatcher, convertedHeaders );
	}
}
