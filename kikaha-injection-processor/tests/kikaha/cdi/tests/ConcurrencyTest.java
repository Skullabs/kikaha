package kikaha.cdi.tests;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import kikaha.cdi.tests.concurrency.PrinterRunner;
import kikaha.core.cdi.DefaultCDI;
import lombok.SneakyThrows;
import lombok.val;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class ConcurrencyTest {

	final static int NUMBER_OF_CONSUMER = Runtime.getRuntime().availableProcessors();
	final static int NUMBER_OF_MESSAGES_PER_NODE = 2000000;
	final DefaultCDI provider = new DefaultCDI();
	final CountDownLatch counter = new CountDownLatch( NUMBER_OF_MESSAGES_PER_NODE + 1 );
	final ExecutorService executor = Executors.newFixedThreadPool( NUMBER_OF_CONSUMER );

	@SneakyThrows
	@Test( timeout = 6000 )
	public void runConcurrentStressTestInStatelessCreation() {
		val inbox = new LinkedBlockingQueue<Object>();
		sendMessagesToInbox( inbox );

		final long start = System.currentTimeMillis();
		for ( int i = 0; i < NUMBER_OF_CONSUMER; i++ )
			executor.submit( new PrinterRunner( inbox, provider, counter ) );
		counter.await();
		System.out.println( "Elapsed time: " + ( System.currentTimeMillis() - start ) + "ms" );
	}

	private void sendMessagesToInbox( final LinkedBlockingQueue<Object> inbox ) throws InterruptedException {
		for ( int i = 0; i < NUMBER_OF_MESSAGES_PER_NODE; i++ )
			inbox.put( "NEXT" );
		inbox.put( "END" );
	}

	@After
	public void shutdownConsumers() {
		executor.shutdown();
	}
}