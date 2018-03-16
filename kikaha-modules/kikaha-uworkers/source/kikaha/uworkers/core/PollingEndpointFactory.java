package kikaha.uworkers.core;

import kikaha.commons.Threads;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A factory designed for endpoints that uses polling to retrieve and read messages.
 * Created by miere.teixeira on 12/08/2017.
 */
public interface PollingEndpointFactory extends EndpointFactory {

    @Override
    default void listenForMessages(
            WorkerEndpointMessageListener listener, EndpointConfig endpointConfig,
            AtomicBoolean isShutdown, Threads threads) {
        final EndpointInbox inbox = createSupplier( endpointConfig );
        final EndpointInboxConsumer consumer = new EndpointInboxConsumer( isShutdown, inbox, listener, endpointConfig.getEndpointName() );
        runInBackgroundWithParallelism( consumer, endpointConfig.getParallelism(), threads );
    }

    default void runInBackgroundWithParallelism(final EndpointInboxConsumer consumer, final int parallelism, Threads threads){
        for ( int i=0; i<parallelism; i++ )
            threads.submit( consumer );
    }

    /**
     * Create a {@link EndpointInbox} for a given {@code endpointName}.
     *
     * @param config - the object containing all information required to create an {@link EndpointInbox}
     * @return
     */
    EndpointInbox createSupplier(EndpointConfig config);
}
