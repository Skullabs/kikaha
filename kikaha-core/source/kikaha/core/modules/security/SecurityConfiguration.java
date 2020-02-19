package kikaha.core.modules.security;

import javax.annotation.PostConstruct;
import javax.inject.*;
import kikaha.config.Config;
import kikaha.core.cdi.CDI;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents all user defined configuration that should be available on the application.
 */
@Getter
@Setter
@Slf4j
@Singleton
public class SecurityConfiguration {

	@Inject private Config config;
	@Inject private CDI provider;

	private SecurityContextFactory factory;
	private SessionIdManager sessionIdManager;
	private SessionStore sessionStore;
	private PasswordEncoder passwordEncoder;
	private AuthenticationRequestMatcher authenticationRequestMatcher;
	private AuthenticationSuccessListener authenticationSuccessListener;
	private AuthenticationFailureListener authenticationFailureListener;
	private PermissionDeniedHandler permissionDeniedHandler;

	private List<SecurityEventListener> eventListeners = new ArrayList<>();

	@PostConstruct
	public void readConfiguration(){
		factory = loadConfiguredClass( "server.auth.security-context-factory", SecurityContextFactory.class );
		sessionIdManager = loadConfiguredClass( "server.auth.session-id-manager", SessionIdManager.class );
		sessionStore = loadConfiguredClass( "server.auth.session-store", SessionStore.class );
		passwordEncoder = loadConfiguredClass( "server.auth.password-encoder", PasswordEncoder.class );
		authenticationRequestMatcher = loadConfiguredClass( "server.auth.authentication-request-matcher", AuthenticationRequestMatcher.class );
		authenticationSuccessListener = loadConfiguredClass( "server.auth.authentication-success-listener", AuthenticationSuccessListener.class );
		authenticationFailureListener = loadConfiguredClass( "server.auth.authentication-failure-listener", AuthenticationFailureListener.class );
		permissionDeniedHandler = loadConfiguredClass( "server.auth.permission-denied-handler", PermissionDeniedHandler.class );
	}

	@SuppressWarnings( "all" )
	public <T> T loadConfiguredClass( String path, Class<T> expectedType ) {
		expectedType = (Class<T>)config.getClass( path );
		if ( expectedType == null )
			return null;
		return provider.load( expectedType );
	}

	/**
	 * Defines a new {@link AuthenticationRequestMatcher} if not previously defined.
	 * @param authenticationRequestMatcher
	 * @return
	 */
	public SecurityConfiguration setRequestMatcherIfAbsent( AuthenticationRequestMatcher authenticationRequestMatcher ) {

		if ( this.authenticationRequestMatcher == null )
			this.authenticationRequestMatcher = authenticationRequestMatcher;
		return this;
	}

	/**
	 * Defines a new {@link AuthenticationSuccessListener} if not previously defined.
	 *
	 * @param authenticationSuccessListener
	 * @return
	 */
	public SecurityConfiguration setSuccessListenerIfAbsent( AuthenticationSuccessListener authenticationSuccessListener ) {
		if ( this.authenticationSuccessListener == null )
			this.authenticationSuccessListener = authenticationSuccessListener;
		return this;
	}

	/**
	 * Defines a new {@link AuthenticationFailureListener} if not previously defined.
	 *
	 * @param authenticationFailureListener
	 * @return
	 */
	public SecurityConfiguration setFailureListenerIfAbsent( AuthenticationFailureListener authenticationFailureListener ) {
		if ( this.authenticationFailureListener == null )
			this.authenticationFailureListener = authenticationFailureListener;
		return this;
	}

	void logDetailedInformationAboutThisConfig(){
		log.info( "Defined security parameters (depending on the modules you've loaded, not all onFinish are actually in use):" );
		log.info( "  security-context-factory: " + getClassName(factory) );
		log.info( "  session-id-manager: " + getClassName(sessionIdManager) );
		log.info( "  session-store: " + getClassName(sessionStore) );
		log.info( "  password-encoder: " + getClassName(passwordEncoder) );
		log.info( "  authentication-request-matcher: " + getClassName(authenticationRequestMatcher) );
		log.info( "  authentication-success-listener: " + getClassName(authenticationSuccessListener) );
		log.info( "  authentication-failure-listener: " + getClassName(authenticationFailureListener) );
	}

	String getClassName( Object obj ) {
		return obj == null ? "null"
				: obj.getClass().getCanonicalName();
	}
}
