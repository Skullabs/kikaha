package kikaha.uworkers.core;

import java.io.*;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.enterprise.inject.Typed;
import javax.inject.*;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import kikaha.core.DeploymentContext;
import kikaha.core.modules.Module;
import kikaha.commons.Threads;
import kikaha.urouting.UndertowHelper;
import kikaha.uworkers.api.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 *
 */
@Slf4j
@Getter
@Singleton
public class MicroWorkersTaskDeploymentModule implements Module {

	final AtomicBoolean isShutdown = new AtomicBoolean( false );

	Threads threads;

	@Inject
	@Typed( WorkerEndpointMessageListener.class )
	Collection<WorkerEndpointMessageListener> consumers;

	@Inject MicroWorkersContext microWorkersContext;
	@Inject UndertowHelper undertowHelper;

	@Override
	public void load( final Undertow.Builder server, final DeploymentContext context) throws IOException {
		if ( consumers.isEmpty() ) return;

		log.info( "Deploying " + consumers.size() + " workers endpoints..." );

		isShutdown.set( false );
		threads = Threads.elasticPool();
		for ( final WorkerEndpointMessageListener consumer : consumers )
			deploy( context, consumer );
	}

	void deploy( final DeploymentContext context, final WorkerEndpointMessageListener listener ) throws IOException {
		final Worker annotation = listener.getClass().getAnnotation( Worker.class );
		if ( annotation == null )
			throw new IOException( "Can't instantiate " + listener.getClass() + ": no @Worker value found." );

		final String listenerName = annotation.value();
		final EndpointConfig endpointConfig = microWorkersContext.getEndpointConfig( listenerName );
		final EndpointFactory endpointFactory = endpointConfig.getEndpointFactory();

		if ( microWorkersContext.isRestEnabled )
			deployHttpEndpoint( context, listener, endpointFactory, endpointConfig, annotation );

        endpointFactory.listenForMessages( listener, endpointConfig, isShutdown, threads );
	}

	void deployHttpEndpoint(
			DeploymentContext context, WorkerEndpointMessageListener listener,
			EndpointFactory endpointFactory, EndpointConfig endpointConfig, Worker worker )
	{
		final String httpListenerName = worker.useNameAsHttpURI() ? worker.value() : listener.getHttpEndpoint();
		final WorkerRef workerRef = endpointFactory.createWorkerRef(endpointConfig);
		final HttpHandler restEndpoint = RESTFulMicroWorkersHttpHandler.with( undertowHelper, workerRef );
		context.register( microWorkersContext.restApiPrefix + "/" + httpListenerName, "POST", restEndpoint );
	}

	@Override
	public void unload() {
		isShutdown.set( true );
		threads.shutdown();
	}
}
