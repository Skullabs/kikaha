package kikaha.core.modules.security;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.*;
import java.util.*;
import javax.inject.Inject;
import io.undertow.server.*;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;
import kikaha.config.Config;
import kikaha.core.cdi.CDI;
import kikaha.core.test.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.*;

@RunWith( KikahaRunner.class )
public class AuthenticationRunnerTest {

	@Inject Config config;
	@Inject CDI provider;
	@Inject AuthenticationEndpoints authenticationEndpoints;

	@Mock SecurityContext securityContext;
	@Mock HttpHandler rootHandler;

	AuthenticationRunner authHandler;
	HttpServerExchange exchange;
	AuthenticationRule matchedRule;
	DefaultPermissionDeniedHandler permissionDeniedHandler;

	@Test
	public void ensureThatCouldCallTheTargetHttpHandlerWhenIsAuthenticated() throws Exception {
		doReturn( true ).when( securityContext ).isAuthenticationRequired();
		when( securityContext.authenticate() ).thenReturn( true );
		when( securityContext.isAuthenticated() ).thenReturn( true );
		when( securityContext.getAuthenticatedAccount() ).thenReturn( new FixedUsernameAndRolesAccount( createExpectedRoles(), null ) );
		authHandler.run();
		verify( rootHandler ).handleRequest(exchange);
	}

	@Test
	public void ensureThatNotCallTheTargetHttpHandlerWhenDoesntMatchExpectedRoles() throws Exception {
		exchange.setRequestURI( "/forbidden" );
		exchange.setQueryString( "current-page=login" );
		doReturn( true ).when( securityContext ).isAuthenticationRequired();
		doNothing().when( authHandler ).endCommunicationWithClient();
		when( securityContext.authenticate() ).thenReturn( true );
		when( securityContext.isAuthenticated() ).thenReturn( true );
		FixedUsernameAndRolesAccount accountWithUnexpectedRoles = new FixedUsernameAndRolesAccount( new HashSet<>(), null );
		when( securityContext.getAuthenticatedAccount() ).thenReturn( accountWithUnexpectedRoles );
		authHandler.run();
		verify( rootHandler, never() ).handleRequest( exchange );
		verify( permissionDeniedHandler ).handle( anyObject() );
		verify( permissionDeniedHandler ).redirectToPermissionDeniedPage( anyObject() );
	}

	@Test
	public void ensureThatNotCallTheTargetHttpHandleWhenWasNotAuthenticatedRequests() throws Exception {
		doReturn( true ).when( securityContext ).isAuthenticationRequired();
		doNothing().when( authHandler ).endCommunicationWithClient();
		when( securityContext.authenticate() ).thenReturn( false );
		authHandler.run();
		verify( rootHandler, never() ).handleRequest(exchange);
		verify( authHandler ).endCommunicationWithClient();
	}

	@Test
	public void ensureThatIsAbleToHandleExceptionsInRunMethod() throws Exception {
		doReturn( true ).when( securityContext ).isAuthenticationRequired();
		doThrow( new IllegalStateException() ).when( securityContext ).authenticate();
		doNothing().when( authHandler ).handleException( any( Throwable.class ) );
		authHandler.run();
		verify( rootHandler, never() ).handleRequest(exchange);
		verify( authHandler ).handleException( isA( IllegalStateException.class ) );
	}

	@Test
	public void ensureCanRedirectToForbiddenPageReplacingCurrentPageUrl(){
		exchange.setRequestURI( "/hello-world" );
		exchange.setQueryString( "message=hello world" );
		authenticationEndpoints.setPermissionDeniedPage("/forbidden-page?current-page={current-page}");
		when( securityContext.authenticate() ).thenReturn( true );
		when( securityContext.isAuthenticated() ).thenReturn( true );
		when( securityContext.getAuthenticatedAccount() ).thenReturn( new FixedUsernameAndRolesAccount( new HashSet<>(), null ) );
		authHandler.run();
		assertEquals( StatusCodes.SEE_OTHER, exchange.getStatusCode() );
		assertEquals("/forbidden-page?current-page=%2Fhello-world%3Fmessage%3Dhello+world", exchange.getResponseHeaders().getFirst(Headers.LOCATION));
	}

	@Before
	public void initializeMocks() {
		MockitoAnnotations.initMocks( this );
		initializeExchange();
		initializeAuthHandler();
	}

	void initializeExchange() {
		exchange = HttpServerExchangeStub.createHttpExchange();
	}

	void initializeAuthHandler() {
		AuthenticationRuleMatcher matcher = mockAuthRuleMatcher();
		permissionDeniedHandler = spy(new DefaultPermissionDeniedHandler());
		permissionDeniedHandler.authenticationEndpoints = authenticationEndpoints;
		matchedRule = spy( matcher.retrieveAuthenticationRuleForUrl( "/user" ) );
		authHandler = spy( new AuthenticationRunner( exchange, rootHandler,
				securityContext, createExpectedRoles(), permissionDeniedHandler ) );
	}

	AuthenticationRuleMatcher mockAuthRuleMatcher() {
		return spy( new AuthenticationRuleMatcher( provider, config.getConfig("server.auth"), authenticationEndpoints) );
	}

	Set<String> createExpectedRoles() {
		Set<String> roles = new HashSet<>();
		roles.add( "Expected" );
		return roles;
	}
}
