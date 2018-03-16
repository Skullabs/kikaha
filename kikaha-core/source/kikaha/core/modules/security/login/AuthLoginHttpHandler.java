package kikaha.core.modules.security.login;

import java.util.*;
import java.util.Map.Entry;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import io.undertow.server.*;
import io.undertow.util.*;
import kikaha.core.modules.security.*;
import kikaha.commons.SystemResource;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 *
 */
@Slf4j
public class AuthLoginHttpHandler implements HttpHandler {

	static final String
			CONTENT_HTML = "text/html",
		FAILURE_MSG = "Can't send message: "
	;

	@Inject
	AuthenticationEndpoints config;

	@Inject
	@Typed( ConfigurationHook.class )
	Iterable<ConfigurationHook> configurationHooks;

	@Getter( lazy = true )
	private final String html = readAndParseTemplate();

	String readAndParseTemplate(){
		final Map<String, Object> templateVariables = readTemplateVariables();
		final String loginTemplatePage = config.getLoginTemplate();
		final String template = SystemResource.readFileAsString( loginTemplatePage, "UTF-8" );
		return applyVariables( template, templateVariables );
	}

	private Map<String, Object> readTemplateVariables(){
		final Map<String, Object> templateVariables = new HashMap<>();
		for ( final ConfigurationHook hook : configurationHooks ) {
			final Map<String, Object> extraParameters = hook.getExtraParameters();
			if ( extraParameters != null )
				templateVariables.putAll(extraParameters);
		}
		return templateVariables;
	}

	@Override
	public void handleRequest( final HttpServerExchange exchange ) throws Exception
	{
		try {
			final SecurityContext securityContext = (SecurityContext) exchange.getSecurityContext();
			final Session session = securityContext.getCurrentSession();

			final Map<String, Object> templateVariables = new HashMap<>();
			for ( ConfigurationHook hook : configurationHooks )
				templateVariables.putAll( hook.configure( exchange, session ) );

			exchange.setStatusCode( StatusCodes.OK );
			exchange.getResponseHeaders().put( Headers.CONTENT_TYPE, CONTENT_HTML );
			exchange.getResponseSender().send( applyVariables( getHtml(), templateVariables ) );
		} catch ( Throwable cause ) {
			handleFailure( exchange, cause );
		}
	}

	private String applyVariables( String template, Map<String, Object> templateVariables ){
		for ( final Entry<String, Object> var : templateVariables.entrySet() )
			template = template.replace( "{{"+ var.getKey() +"}}", var.getValue().toString() );
		return template;
	}

	private void handleFailure( HttpServerExchange exchange, Throwable cause ) {
		log.error( FAILURE_MSG, cause );
		if ( !exchange.isResponseStarted() ) {
			exchange.setStatusCode( StatusCodes.OK );
			exchange.getResponseHeaders().put( Headers.CONTENT_TYPE, CONTENT_HTML );
			exchange.getResponseSender().send( FAILURE_MSG );
		}
	}

	/**
	 * Configure how the Login Page should behave.
	 */
	public interface ConfigurationHook {

		/**
		 * Retrieve extra parameters that should be available for injection on the template.
		 * This parameters will be available after the application deployment.
		 *
		 * @return
		 */
		Map<String, Object> getExtraParameters();

		/**
		 * Configure the login request.
		 *
		 * @param exchange
		 * @param session
		 * @return new parameters that should be defined on the template. Developers should return an empty map
		 *      if no parameter should be rendered for this request.
		 */
		Map<String, Object> configure( HttpServerExchange exchange, Session session );
	}
}
