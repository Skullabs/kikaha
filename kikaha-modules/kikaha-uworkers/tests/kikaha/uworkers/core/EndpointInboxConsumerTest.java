package kikaha.uworkers.core;

import kikaha.commons.Threads;
import kikaha.uworkers.api.Exchange;
import lombok.SneakyThrows;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.Thread.sleep;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link EndpointInboxConsumer}.
 */
@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class EndpointInboxConsumerTest {

	final AtomicBoolean isShutdown = new AtomicBoolean( false );

	Threads threads;

    @Spy StubEndpointInboxSupplier supplier;
    @Mock Exchange exchange;
	@Mock WorkerEndpointMessageListener listener;

	EndpointInboxConsumer consumer;

	@Before
	public void createConsumer() throws Exception {
		threads = Threads.fixedPool(1);
		consumer = new EndpointInboxConsumer( isShutdown, supplier, listener, "default" );
	}

	@After
	public void shutdownThreads(){
		isShutdown.set( true );
		threads.shutdown();
	}

	@Test
	@SneakyThrows
	public void ensureCanListenerCanReceiveAMessageFromInbox(){
		doReturn( exchange ).when( supplier ).receiveMessage();

		threads.submit( consumer );
		sleep( 200 );

		verify( listener, atLeastOnce() ).onMessage( eq( exchange ) );
	}

	@Test
	@SneakyThrows
	public void ensureCanHandleFailuresRaisedFromWorkerTaskExecution(){
		final IOException exception = new IOException();
		doThrow( exception ).when( listener ).onMessage( eq( exchange ) );
		doReturn( exchange ).when( supplier ).receiveMessage();

		threads.submit( consumer );
		sleep( 200 );

		verify( exchange, atLeastOnce() ).reply( eq( exception ) );
	}

	@Test
	@SneakyThrows
	public void ensureFailuresWhenReceivesTheMessageWillStopTheListener(){
		final IOException exception = new IOException();
		doThrow( exception ).when( supplier ).receiveMessage();

		threads.submit( consumer );
		sleep( 200 );

		verify( exchange, never() ).reply( eq( exception ) );
	}
}

class StubEndpointInboxSupplier implements EndpointInboxSupplier {

    @Override
    public Exchange receiveMessage() throws InterruptedException, IOException, EndpointInboxConsumerTimeoutException {
        return null;
    }
}