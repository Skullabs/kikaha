package kikaha.core.modules.security;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;

@Slf4j
@RequiredArgsConstructor
public class AuthenticationRunner implements Runnable {

	final HttpServerExchange exchange;
	final HttpHandler next;
	final SecurityContext context;
	final Collection<String> expectedRoles;
	final PermissionDeniedHandler permissionDeniedHandler;

	@Override
	public void run() {
		try {
			if ( !context.isAuthenticationRequired() || ( context.authenticate() && context.isAuthenticated() ) ) {
				if ( !exchange.isResponseStarted() )
					tryExecuteChain();
			} else {
				endCommunicationWithClient();
			}
		// UNCHECKED: It really should handle all exceptions here
		} catch ( final Throwable cause ) {
		// CHECKED
			handleException( cause );
		}
	}

	void tryExecuteChain() throws Exception {
		if ( !context.isAuthenticated() || matchesExpectedRoles() ) {
			next.handleRequest(exchange);
		} else {
			permissionDeniedHandler.handle(exchange);
			exchange.endExchange();

		}

	}

	boolean matchesExpectedRoles() {
		int matchedRoles = 0;
		for ( String expectedRole : expectedRoles )
			for ( String role : context.getAuthenticatedAccount().getRoles() )
				if ( expectedRole.equals( role ) )
					matchedRoles++;
		return matchedRoles == expectedRoles.size();
	}

	void handleException( final Throwable cause ) {
		log.error( "Failed to execute the endpoint", cause );
		if ( !exchange.isResponseStarted() ) {
			exchange.setStatusCode( StatusCodes.INTERNAL_SERVER_ERROR );
			exchange.getResponseSender().send( "Internal Server Error: " + cause.getMessage() );
		}
		exchange.endExchange();
	}

	void endCommunicationWithClient() {
		exchange.endExchange();
	}
}
