package kikaha.core.modules.websocket;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.websockets.WebSocketConnectionCallback;
import kikaha.config.Config;
import kikaha.core.DeploymentContext;
import kikaha.core.modules.Module;
import kikaha.core.modules.http.ContentType;
import kikaha.core.modules.http.WebResource;
import kikaha.commons.url.URL;
import kikaha.commons.url.URLMatcher;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

/**
 * A module that deploy {@link WebSocketHandler}s.
 */
@Slf4j
@Singleton
public class WebSocketModule implements Module {

	@Inject
	@Typed( WebSocketHandler.class )
	Iterable<WebSocketHandler> handlers;

	@Inject
	@Typed( WebSocketSession.Serializer.class )
	Collection<WebSocketSession.Serializer> webSocketSerializers;

	@Inject
	@Typed( WebSocketSession.Unserializer.class )
	Collection<WebSocketSession.Unserializer> webSocketUnserializers;

	@Inject
	Config config;

	@NonNull @Getter
	WebSocketSession.Serializer serializer;

	@NonNull @Getter
	WebSocketSession.Unserializer unserializer;

	ExecutorService executorService;

	@PostConstruct
	public void configureModule(){
		loadSerializersAndUnserializers();
		loadWorkersThreadPool();
	}

	private void loadSerializersAndUnserializers(){
		final Config webSocketConfig = config.getConfig( "server.websocket" );

		final Map<String, WebSocketSession.Serializer> serializers = webSocketSerializers.stream().collect(toMap(this::extractContentType, identity(), (s1,s2) -> s2));
		serializer = serializers.get( webSocketConfig.getString("default-serializer") );
		if ( serializer != null )
			log.debug( "Default WebSocket serializer: " + serializer.getClass().getCanonicalName() );

		final Map<String, WebSocketSession.Unserializer> unserializers = webSocketUnserializers.stream().collect(toMap(this::extractContentType, identity(), (s1,s2) -> s2));
		unserializer = unserializers.get( webSocketConfig.getString("default-unserializer") );
		if ( unserializer != null )
			log.debug( "Default WebSocket unserializer: " + unserializer.getClass().getCanonicalName() );
	}

	public void loadWorkersThreadPool(){
		final int size = config.getInteger("server.websocket.worker-threads");
		if ( size > 0 )
			executorService = Executors.newFixedThreadPool( size );
		else executorService = Executors.newCachedThreadPool();
	}

	String extractContentType( Object object ) {
		final Class<?> clazz = object.getClass();
		final ContentType annotation = clazz.getAnnotation(ContentType.class);
		if ( annotation == null ){
			final String msg = clazz + " should be annotated with @" + ContentType.class;
			throw new UnsupportedOperationException( msg );
		}
		return annotation.value();
	}

	@Override
	public void load( Undertow.Builder server, final DeploymentContext context ) {
		for ( final WebSocketHandler handler : handlers )
			deploy( context, handler );
	}

	void deploy( final DeploymentContext context, final WebSocketHandler handler ) {
		final WebResource webResource = handler.getClass().getAnnotation( WebResource.class );
		if ( webResource == null ) {
			log.warn( "No WebResource annotation found for " + handler.getClass().getCanonicalName() + ": Skipped!" );
			return;
		}
		context.register( webResource.path(), "GET", wrappedWebsocketHandlerFrom( handler, webResource ) );
	}

	HttpHandler wrappedWebsocketHandlerFrom( final WebSocketHandler handler, final WebResource webResource ) {
		final String url = URL.removeTrailingCharacter( webResource.path() );
		final URLMatcher urlMatcher = URLMatcher.compile( "{protocol}://{host}" + url );
		final WebSocketConnectionCallback callbackHandler = new WebSocketConnectionCallbackHandler(
				handler, urlMatcher ,serializer, unserializer, executorService );
		return Handlers.websocket( callbackHandler );
	}

	@Override
	public void unload() {
		if ( executorService != null ) {
			executorService.shutdown();
			try {
				executorService.awaitTermination(30, TimeUnit.SECONDS);
			} catch (InterruptedException e) {}
			if (!executorService.isTerminated())
				executorService.shutdownNow();
		}
	}
}