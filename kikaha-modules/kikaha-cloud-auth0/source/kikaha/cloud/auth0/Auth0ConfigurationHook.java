package kikaha.cloud.auth0;

import static kikaha.cloud.auth0.Auth0.STATE;
import java.util.Map;
import javax.inject.*;
import io.undertow.server.HttpServerExchange;
import kikaha.core.modules.security.*;
import kikaha.core.modules.security.login.AuthLoginHttpHandler;
import kikaha.commons.ChainedMap;
import lombok.extern.slf4j.Slf4j;

/**
 *
 */
@Slf4j
@Singleton
@SuppressWarnings( "unchecked" )
public class Auth0ConfigurationHook implements AuthLoginHttpHandler.ConfigurationHook {

	@Inject Auth0.AuthConfig authConfig;
	@Inject kikaha.core.modules.security.AuthenticationEndpoints formConfig;

	@Override
	public Map<String, Object> getExtraParameters() {
		return (Map)ChainedMap.with( "clientId", authConfig.clientId )
				.and( "clientDomain", authConfig.clientDomain );
	}

	@Override
	public Map<String, Object> configure( HttpServerExchange exchange, Session session ) {
		final String redirectUrl = exchange.getRequestScheme() + "://" + exchange.getHostAndPort() + formConfig.getCallbackUrl();
		final String state = SessionIdGenerator.generate() + "&nonce=" + SessionIdGenerator.generate();
		session.setAttribute( STATE, state );
		return (Map)ChainedMap.with( "state", state )
				.and( "authenticationCallbackUrl", redirectUrl );
	}
}
