package kikaha.core.modules.security.login;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import java.util.*;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import io.undertow.server.HttpServerExchange;
import kikaha.core.modules.security.*;
import kikaha.core.test.*;
import kikaha.commons.ChainedMap;
import kikaha.commons.SystemResource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;

/**
 * Unit tests for AuthLoginHttpHandler.
 */
@RunWith( KikahaRunner.class )
public class AuthLoginHttpHandlerTest {

	static final String EXPECTED_PARSED_TEMPLATE = SystemResource.readFileAsString( "expected-parsed-template.html", "UTF-8" );

	@Inject AuthLoginHttpHandler handler;
	@Mock Session currentSession;
	@Mock SecurityContext securityContext;
	HttpServerExchange httpExchange;

	@PostConstruct
	public void loadMocks(){
		MockitoAnnotations.initMocks( this );
		doReturn( currentSession ).when( securityContext ).getCurrentSession();
		httpExchange = HttpServerExchangeStub.createHttpExchange();
		httpExchange.setSecurityContext( securityContext );
		handler.configurationHooks = asList( new CustomConfigurationHook() );
	}

	@Test
	public void ensureCanPreComputeTheLoginHTMLTemplate(){
		final String parsedTemplate = handler.readAndParseTemplate();
		assertEquals( EXPECTED_PARSED_TEMPLATE, parsedTemplate );
	}

	@Test
	public void ensureCanSendLoginPageAsResponse() throws Exception {
		handler.handleRequest( httpExchange );

		verify( currentSession ).setAttribute( eq(CustomConfigurationHook.KEY), eq(CustomConfigurationHook.VALUE) );
		verify( httpExchange.getResponseSender() ).send( eq(handler.getHtml()) );
		assertEquals( 200, httpExchange.getStatusCode() );
	}
}

class CustomConfigurationHook implements AuthLoginHttpHandler.ConfigurationHook {

	final static String KEY = "KEY", VALUE = "VALUE";

	@Override
	public Map<String, Object> getExtraParameters() {
		return ChainedMap.with( KEY, VALUE );
	}

	@Override
	public Map<String, Object> configure( HttpServerExchange exchange, Session session ) {
		session.setAttribute( KEY, VALUE );
		return Collections.emptyMap();
	}
}