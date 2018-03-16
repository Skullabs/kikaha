package kikaha.core.modules.http;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.server.handlers.resource.FileResourceManager;
import io.undertow.server.handlers.resource.ResourceHandler;
import io.undertow.server.handlers.resource.ResourceManager;
import io.undertow.util.AttachmentKey;
import kikaha.config.Config;
import kikaha.core.DeploymentContext;
import kikaha.core.modules.Module;
import kikaha.core.modules.undertow.BodyResponseSender;
import kikaha.commons.url.URLMatcher;
import kikaha.commons.LastValueOnlyMap;
import kikaha.commons.SystemResource;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;

/**
 *
 */
@Getter
@Slf4j
@Singleton
public class StaticResourceModule implements Module {

	public static final String DEFAULT_WEBJAR_LOCATION = "META-INF/resources/webjars/";

	@Inject
	Config config;

	@Override
	public void load(Undertow.Builder server, DeploymentContext context) throws IOException {
		final Config staticConfig = config.getConfig("server.static");
		if ( staticConfig.getBoolean("enabled") ) {
			deployWelcomeFile(staticConfig, context);
			deployDefaultStaticRouting(staticConfig, context);
			deployWebJarStaticRouting(staticConfig, context);
		}
	}

	private void deployWelcomeFile(Config staticConfig, DeploymentContext context) {
		final String welcomeFile = staticConfig.getString("welcome-file", "index.html");
		try {
			final String location = staticConfig.getString("location");
			final String encoding = staticConfig.getString("encoding", "UTF-8");
			final String fileContent = SystemResource.readFileAsString( location + "/" +  welcomeFile, encoding);
			final WelcomeFileHandler welcomeFileHandler = new WelcomeFileHandler(fileContent, context.fallbackHandler());
			context.fallbackHandler(welcomeFileHandler);
		} catch ( Throwable cause ) {
			log.debug( "Could not locate welcome file: " + welcomeFile, cause );
		}
	}

	private void deployWebJarStaticRouting(Config staticConfig, DeploymentContext context) {
		if ( staticConfig.getBoolean("webjar-enabled" ) ) {
			final String urlPrefix = staticConfig.getString("webjar-url-prefix").replaceFirst( "/$", "" );
			log.info( "Enabling static routing for webjars at '" + urlPrefix + "'" );
			final String webJarInternalLocation = staticConfig.getString("webjar-location", DEFAULT_WEBJAR_LOCATION);
			final ResourceManager resourceManager = loadResourceManagerFor( webJarInternalLocation );
			final HttpHandler webjarHandler = new WebJarHttpHandler(
				new ResourceHandler(resourceManager, new WebJarNotFound( context.fallbackHandler() ) ),
				URLMatcher.compile( urlPrefix + "/{path}" ),
					context.fallbackHandler());
			context.fallbackHandler( webjarHandler );
		}
	}

	private void deployDefaultStaticRouting(final Config staticConfig, DeploymentContext context){
		final String location = staticConfig.getString("location");
		log.info( "Enabling static routing at folder: " + location );
		final ResourceManager resourceManager = loadResourceManagerFor( location );
		final HttpHandler fallbackHandler = context.fallbackHandler();
		final ResourceHandler handler = new ResourceHandler(resourceManager, fallbackHandler);
		final String welcomeFile = staticConfig.getString("welcome-file", "index.html");
		handler.setWelcomeFiles( welcomeFile );
		context.fallbackHandler( handler );
	}

    private static ResourceManager loadResourceManagerFor(String location) {
        final File locationAsFile = new File(location);
        if ( locationAsFile.exists() )
            return new FileResourceManager(locationAsFile, 100, SystemResource.FILE_NAMES_ARE_CASE_SENSITIVE );
        final ClassLoader classLoader = SystemResource.class.getClassLoader();
        return new ClassPathResourceManager( classLoader, location );
    }
}

@RequiredArgsConstructor
class WebJarHttpHandler implements HttpHandler {

	final ResourceHandler handler;
	final URLMatcher uri;
	final HttpHandler fallbackHandler;

	@Override
	public void handleRequest(HttpServerExchange exchange) throws Exception {
		exchange.putAttachment( WebJarNotFound.RELATIVE_PATH, exchange.getRelativePath() );
		final LastValueOnlyMap<String,String> matcher = new LastValueOnlyMap<>();
		if ( uri.matches( exchange.getRelativePath(), matcher ) ) {
			exchange.setRelativePath(matcher.getValue());
			handler.handleRequest(exchange);
		} else {
			fallbackHandler.handleRequest( exchange );
		}
	}
}

@RequiredArgsConstructor
class WebJarNotFound implements HttpHandler {

	static final AttachmentKey<String> RELATIVE_PATH = AttachmentKey.create( String.class );
	final HttpHandler next;

	@Override
	public void handleRequest(HttpServerExchange exchange) throws Exception {
		final String relativePath = exchange.getAttachment(RELATIVE_PATH);
		exchange.setRelativePath( relativePath );
		next.handleRequest( exchange );
	}
}

@RequiredArgsConstructor
class WelcomeFileHandler implements HttpHandler {

	final String welcomeFileContent;
	final HttpHandler fallbackHandler;

	@Override
	public void handleRequest(HttpServerExchange httpServerExchange) throws Exception {
		if ( httpServerExchange.getRelativePath().equals( "/" ) )
			BodyResponseSender
					.response(httpServerExchange, 200, "text/html", welcomeFileContent);
		else
			fallbackHandler.handleRequest( httpServerExchange );
	}
}