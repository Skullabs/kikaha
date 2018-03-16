package kikaha.uworkers.core;

import kikaha.commons.Threads;
import kikaha.uworkers.api.WorkerRef;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Created by miere.teixeira on 12/08/2017.
 */
@RunWith(MockitoJUnitRunner.class)
public class PollingEndpointFactoryTest {

    @Spy StubPollingEndpointFactory factory;
    @Mock WorkerEndpointMessageListener listener;
    @Mock EndpointConfig endpointConfig;
    @Mock AtomicBoolean isShutdown;
    @Mock Threads threads;

    @Before
    public void configureMocks(){
        doReturn( "name" ).when( endpointConfig ).getEndpointName();
        //doCallRealMethod().when( factory ).listenForMessages( any(), any(), any(), any() );
    }

    @Test
    public void ensureThatUsesDefaultParallelismWhenNoDataIsAvailableForAGivenNamedConsumer() throws IOException {
        doReturn( 1 ).when( endpointConfig ).getParallelism();
        factory.listenForMessages( listener, endpointConfig, isShutdown, threads );
        verify( factory ).runInBackgroundWithParallelism( any(EndpointInboxConsumer.class), eq(1), eq( threads ) );
        verify( threads ).submit( any() );
    }

    @Test
    public void doNotDeployWhenParallelismIs0OrLower() throws IOException {
        doReturn( 0 ).when( endpointConfig ).getParallelism();
        factory.listenForMessages( listener, endpointConfig, isShutdown, threads );
        verify( factory ).runInBackgroundWithParallelism( any(EndpointInboxConsumer.class), eq(0), eq( threads ) );
        verify( threads, never() ).submit( any() );
    }
}

class StubPollingEndpointFactory implements PollingEndpointFactory {

    @Override
    public WorkerRef createWorkerRef(EndpointConfig config) {
        return null;
    }

    @Override
    public EndpointInbox createSupplier(EndpointConfig config) {
        return null;
    }
}