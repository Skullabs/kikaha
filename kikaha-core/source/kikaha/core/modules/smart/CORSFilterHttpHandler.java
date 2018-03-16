package kikaha.core.modules.smart;

import java.net.*;
import java.util.*;
import io.undertow.server.*;
import io.undertow.util.*;
import kikaha.commons.url.URLMatcher;
import lombok.RequiredArgsConstructor;

/**
 *
 */
@RequiredArgsConstructor
public class CORSFilterHttpHandler implements HttpHandler {

	static final HttpString ACCESS_METHOD = new HttpString( "Access-Control-Request-Method" );
	static final HttpString ACCESS_HEADERS = new HttpString( "Access-Control-Request-Headers" );
	static final HttpString ALLOWED_ORIGIN = new HttpString( "Access-Control-Allow-Origin" );
	static final HttpString ALLOWED_HEADERS = new HttpString( "Access-Control-Allow-Headers" );
	static final HttpString ALLOWED_METHOD = new HttpString( "Access-Control-Allow-Methods" );
	static final HttpString ALLOWED_CREDENTIALS = new HttpString( "Access-Control-Allow-Credentials" );
	static final Map<String, String> EMPTY_URL_PARAMS = new HashMap<>();
	static final String TRUE = "true";

	final CORSConfig config;
	final HttpHandler next;
	final HttpHandler notFound;

	@Override
	public void handleRequest( HttpServerExchange exchange ) throws Exception {
		final boolean isOption = isOption( exchange );
		final String originHost = retrieveOriginHost( exchange );
		final String method = getHttpMethod( exchange );

		if ( !isOption )
			allowOrigin( exchange, originHost, method );
		else if ( !isAllowedHttpMethod( method ) || !isHostAllowed( originHost ) )
			notFound.handleRequest(exchange);
		else
			sendRequiredHeaders(exchange, originHost, method);
	}

	private void allowOrigin( HttpServerExchange exchange, String originHost, String method ) throws Exception {
		if ( originHost != null ) {
			final HeaderMap responseHeaders = exchange.getResponseHeaders();
			sendBasicNeededHeadersToAllowRequest( responseHeaders, originHost, method );
		}
		next.handleRequest(exchange);
	}

	private boolean isHostAllowed( String originHost ) throws MalformedURLException {
		if ( config.alwaysAllowOrigin) return true;
		else
			for (URLMatcher matcher : config.allowedOriginMatchers)
				if ( matcher.matches( originHost, EMPTY_URL_PARAMS ) )
					return true;
		return false;
	}

	private String retrieveOriginHost( HttpServerExchange exchange ) throws URISyntaxException {
		final HeaderValues refererHeader = exchange.getRequestHeaders().get(Headers.ORIGIN);
		if ( refererHeader == null )
			return null;
		final String referer = refererHeader.getFirst();
		final URI url = new URI(referer);
		return url.getScheme() + "://" + url.getAuthority();
	}

	private static boolean isOption( HttpServerExchange exchange ){
		return Methods.OPTIONS.equals( exchange.getRequestMethod() );
	}

	private boolean isAllowedHttpMethod( String method ){
		return config.allowedMethods.contains( method );
	}

	private String getHttpMethod( HttpServerExchange exchange ) {
		final HeaderValues strings = exchange.getRequestHeaders().get(ACCESS_METHOD);
		return strings != null ? strings.getFirst() : "";
	}

	private void sendRequiredHeaders(HttpServerExchange exchange, String originHost, String method) {
		final HeaderMap responseHeaders = exchange.getResponseHeaders();
		sendBasicNeededHeadersToAllowRequest( responseHeaders, originHost, method );
		final HeaderValues allowedHeaders = exchange.getRequestHeaders().get( ACCESS_HEADERS );
		if ( allowedHeaders != null )
			responseHeaders.put( ALLOWED_HEADERS, String.join( ",", (CharSequence[]) allowedHeaders.toArray()) );
	}

	private void sendBasicNeededHeadersToAllowRequest(final HeaderMap responseHeaders, String originHost, String method ){
		responseHeaders.put( ALLOWED_ORIGIN, originHost );
		responseHeaders.put( ALLOWED_METHOD, method );
		if ( config.allowCredentials )
			responseHeaders.put( ALLOWED_CREDENTIALS, TRUE );
	}
}
