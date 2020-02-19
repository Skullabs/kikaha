package kikaha.core.modules.security;

import io.undertow.server.*;
import lombok.*;

@RequiredArgsConstructor
class AuthenticationHttpHandler implements HttpHandler {

	@NonNull final AuthenticationRuleMatcher authenticationRuleMatcher;
	@NonNull final String permissionDeniedPage;
	@NonNull final HttpHandler next;
	@NonNull final SecurityConfiguration securityConfiguration;

	@Override
	public void handleRequest(final HttpServerExchange exchange) throws Exception {
		AuthenticationRule rule = retrieveRuleThatEnsureRequestShouldBeAuthenticated( exchange );
		if ( rule == null )
			rule = AuthenticationRule.EMPTY;
		final SecurityContext securityContext = getOrCreateSecurityContext(exchange, rule);
		if ( securityContext.isAuthenticated() )
			next.handleRequest(exchange);
		else
			runAuthenticationInIOThread( exchange, rule, securityContext );
	}

	private AuthenticationRule retrieveRuleThatEnsureRequestShouldBeAuthenticated( final HttpServerExchange exchange ) {
		final AuthenticationRequestMatcher authRequestMatcher = securityConfiguration.getAuthenticationRequestMatcher();
		return authRequestMatcher != null && !authRequestMatcher.matches( exchange ) ? null
			   : authenticationRuleMatcher.retrieveAuthenticationRuleForUrl( exchange.getRelativePath() );
	}

	private SecurityContext getOrCreateSecurityContext( final HttpServerExchange exchange, final AuthenticationRule rule ) {
		SecurityContext securityContext = (SecurityContext)exchange.getSecurityContext();
		if ( securityContext == null ) {
			securityContext = securityConfiguration.getFactory().createSecurityContextFor(exchange, rule, securityConfiguration);
			exchange.setSecurityContext( securityContext );
		}
		exchange.addExchangeCompleteListener( new SecurityContextAutoUpdater( securityContext ) );
		return securityContext;
	}

	void runAuthenticationInIOThread(final HttpServerExchange exchange,
			final AuthenticationRule rule, final SecurityContext context)
	{
		exchange.dispatch(
			new AuthenticationRunner(
				exchange, next, context, rule.expectedRoles(), securityConfiguration.getPermissionDeniedHandler() ) );
	}
}