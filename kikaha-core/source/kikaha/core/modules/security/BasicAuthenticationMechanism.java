package kikaha.core.modules.security;

import io.undertow.security.idm.Credential;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.FlexBase64;
import io.undertow.util.HeaderValues;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import kikaha.config.Config;
import kikaha.commons.url.StringCursor;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

@Slf4j
@Singleton
public class BasicAuthenticationMechanism implements SimplifiedAuthenticationMechanism {

	private static final String BASIC_PREFIX = Headers.BASIC + " ";
	private static final int PREFIX_LENGTH = BASIC_PREFIX.length();
	private static final char COLON = ':';

	private String challenge;

	@Inject Config config;

	@PostConstruct
	public void defineARealm() {
		final String applicationName = config.getString("server.smart-server.application.name");
		this.challenge = BASIC_PREFIX + "realm=\"" + applicationName + "\"";
	}

	@Override
	public Credential readCredential(HttpServerExchange exchange) throws IOException {
		final StringCursor decodedCredentials = getDecodedCredentialsFromHeader( exchange );
		if ( decodedCredentials == null )
			return null;
		return convertToCredential( decodedCredentials );
	}

	private UsernameAndPasswordCredential convertToCredential( StringCursor decodedCredentials ) {
		decodedCredentials.shiftCursorToNextChar( COLON );
		final String username = decodedCredentials.substringFromLastMark(1);
		decodedCredentials.mark();
		decodedCredentials.end();
		final String password = decodedCredentials.substringFromLastMark();
		return new UsernameAndPasswordCredential( username, password );
	}

	private StringCursor getDecodedCredentialsFromHeader( HttpServerExchange exchange ) {
		StringCursor decodedCredentials = null;
		final StringCursor headerValue = getAuthenticationHeader( exchange );
		if ( headerValue != null ) {
			final String authString = getAuthString( headerValue );
			decodedCredentials = decode( authString );
		}
		return decodedCredentials;
	}

	private StringCursor decode( String authString ) {
		try {
			final ByteBuffer decode = FlexBase64.decode( authString );
			final String string = new String( decode.array(), decode.arrayOffset(), decode.limit(), StandardCharsets.UTF_8 );
			return new StringCursor( string );
		} catch ( final IOException cause ) {
			log.warn( "Ignoring exception during Base64 decoding.", cause );
			return null;
		}
	}

	private String getAuthString( StringCursor headerValue ) {
		headerValue.cursorAt( PREFIX_LENGTH );
		headerValue.mark();
		headerValue.end();
		return headerValue.substringFromLastMark();
	}

	private StringCursor getAuthenticationHeader( HttpServerExchange exchange ) {
		final HeaderValues authHeaders = exchange.getRequestHeaders().get( Headers.AUTHORIZATION );
		StringCursor headerValue = null;
		if( authHeaders != null )
			for ( final String value : authHeaders )
				if ( value.startsWith( BASIC_PREFIX ) )
					headerValue = new StringCursor( value );
		return headerValue;
	}

	@Override
	public boolean sendAuthenticationChallenge(HttpServerExchange exchange, Session session) {
		exchange.setStatusCode( StatusCodes.UNAUTHORIZED );
		exchange.getResponseHeaders().add( Headers.WWW_AUTHENTICATE, challenge );
		return true;
	}
}
