package kikaha.uworkers.core;

import kikaha.commons.Threads;
import kikaha.uworkers.api.WorkerRef;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A factory to instantiate components needed to consume or produce messages
 * that are handled by Workers.
 */
public interface EndpointFactory {

    /**
     * Make the endpoint listen for messages until {@code isShutdown} is false.
     *
     * @param listener
     * @param endpointConfig
     * @param isShutdown
     * @param threads - a managed ThreadPool which can be used to read messages.
     * @see PollingEndpointFactory
     */
    void listenForMessages(
        WorkerEndpointMessageListener listener, EndpointConfig endpointConfig,
        AtomicBoolean isShutdown, Threads threads
    );

	/**
	 * Creates a reference to a Worker.
	 *
	 * @param config - the object containing all information required to create a {@link WorkerRef}
	 * @return
	 */
	WorkerRef createWorkerRef(EndpointConfig config );
}
