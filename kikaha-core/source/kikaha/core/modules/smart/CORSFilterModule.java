package kikaha.core.modules.smart;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import javax.inject.*;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import kikaha.config.Config;
import kikaha.core.*;
import kikaha.core.modules.Module;
import kikaha.commons.url.URLMatcher;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 *
 */
@Slf4j
@Getter
@Singleton
public class CORSFilterModule implements Module {

	final String name = "cors";

	@Inject
	NotFoundHandler notFoundHandler;

	@Inject
	Config config;

	@Override
	public void load(Undertow.Builder server, DeploymentContext context) throws IOException {
		if ( !config.getBoolean("server.smart-routes.cors.enabled") )
			return;

		final CORSConfig corsConfig = loadCorsConfig();
		final HttpHandler rootHandler = context.rootHandler();
		final CORSFilterHttpHandler httpHandler = new CORSFilterHttpHandler( corsConfig, rootHandler, notFoundHandler);

		log.info( "Deploying CORS Smart Route: " + corsConfig );
		context.rootHandler( httpHandler );
	}

	CORSConfig loadCorsConfig(){
		final List<String> allowedOrigins = config.getStringList("server.smart-routes.cors.allowed-origins");

		return new CORSConfig(
			config.getBoolean("server.smart-routes.cors.always-allow-origin"),
			config.getBoolean("server.smart-routes.cors.always-allow-credentials"),
			asSet(config.getStringList("server.smart-routes.cors.allowed-methods")),
			asSet( allowedOrigins ), asMatcherSet( allowedOrigins ));
	}

	private static <T> Set<T> asSet( List<T> list ) {
		return new HashSet<>( list );
	}

	private static Set<URLMatcher> asMatcherSet(List<String> urls ) {
		return urls.stream()
			.map(URLMatcher::compile)
			.collect(Collectors.toCollection(HashSet::new));
	}
}
