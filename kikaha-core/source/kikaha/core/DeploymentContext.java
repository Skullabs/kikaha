package kikaha.core;

import javax.annotation.PostConstruct;
import javax.inject.*;
import io.undertow.server.HttpHandler;
import io.undertow.util.Methods;
import kikaha.commons.url.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Accessors( fluent = true )
@Singleton
public class DeploymentContext {

	final SimpleRoutingHandler routingHandler = new SimpleRoutingHandler();

	@NonNull
	@Setter
	HttpHandler rootHandler = routingHandler;

	@Inject
	NotFoundHandler notFoundHandler;

	@PostConstruct
	public void ensureThatUseNotFoundHandlerAsDefaultFallbackHandler(){
		routingHandler.setFallbackHandler( notFoundHandler );
	}

	public DeploymentContext register(final String uri, final HttpHandler handler ) {
		register( uri, "PATCH", handler );
		register( uri, Methods.PUT_STRING, handler );
		register( uri, Methods.POST_STRING, handler );
		register( uri, Methods.DELETE_STRING, handler );
		return register( uri, Methods.GET_STRING, handler );
	}

	public DeploymentContext register(String uri, final String method, final HttpHandler handler ) {
		uri = URL.removeTrailingCharacter( uri );
		log.info( "Registering route: " + method + ":" + uri );
		log.debug( "  > " + handler.toString() + " (" + handler.getClass().getCanonicalName() + ")" );
		this.routingHandler.add( method, uri, handler );
		return this;
	}

	public DeploymentContext fallbackHandler(final HttpHandler fallbackHandler ) {
		routingHandler.setFallbackHandler( fallbackHandler );
		return this;
	}

	public HttpHandler fallbackHandler(){
		return routingHandler.getFallbackHandler();
	}
}