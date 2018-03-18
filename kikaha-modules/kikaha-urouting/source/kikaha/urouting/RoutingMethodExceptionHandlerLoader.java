package kikaha.urouting;

import java.util.HashMap;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.inject.Singleton;

import kikaha.commons.Reflection;
import kikaha.config.Config;
import kikaha.core.cdi.CDI;
import kikaha.urouting.api.ExceptionHandler;

@SuppressWarnings( { "rawtypes", "unchecked" } )
@Singleton
public class RoutingMethodExceptionHandlerLoader {

	@Inject
	@Typed(ExceptionHandler.class)
	Iterable<ExceptionHandler> availableHandlers;

	@Inject
	Config kikahaConf;

	@Inject
	CDI provider;

	RoutingMethodExceptionHandler exceptionHandler;
	ExceptionHandler<Throwable> fallbackHandler;

	@PostConstruct
	public void loadData() {
		fallbackHandler = loadFallbackHandler();
		final HashMap<Class<?>, ExceptionHandler<?>> loadHandlers = loadHandlers();
		exceptionHandler = new RoutingMethodExceptionHandler( loadHandlers, fallbackHandler );
	}

	private ExceptionHandler<Throwable> loadFallbackHandler() {
		final Class<?> handlerClass = kikahaConf.getClass( "server.urouting.exception-handler" );
		return (ExceptionHandler<Throwable>)provider.load( handlerClass );
	}

	private HashMap<Class<?>, ExceptionHandler<?>> loadHandlers() {
		final HashMap<Class<?>, ExceptionHandler<?>> handlers = new HashMap<Class<?>, ExceptionHandler<?>>();
		for ( final ExceptionHandler handler : availableHandlers ) {
			final Class<?> throwableClass = getGenericClass( handler );
			handlers.put( throwableClass, handler );
		}
		return handlers;
	}

	private <T extends Throwable> Class<T> getGenericClass( final ExceptionHandler<T> handler ) {
		return (Class<T>) Reflection.getFirstGenericTypeFrom( handler, ExceptionHandler.class );
	}

	@Produces
	public RoutingMethodExceptionHandler produceExceptionHandler() {
		return exceptionHandler;
	}
}
