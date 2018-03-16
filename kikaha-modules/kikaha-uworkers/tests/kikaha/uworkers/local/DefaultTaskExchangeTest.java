package kikaha.uworkers.local;

import kikaha.commons.Threads;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.Value;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static kikaha.uworkers.local.CounterInterceptor.andCount;
import static org.junit.Assert.assertEquals;

/**
 * Unit test for {@link LocalExchange}.
 */
public class DefaultTaskExchangeTest {

	static final Ping ping = new Ping();
	static final Pong pong = new Pong( ping );

	@Test(timeout = 15000)
	@SneakyThrows
	public void receiveAsyncResponseWhenListenerIsSetBeforeResponseIsDefined() {
		try ( Threads threads = Threads.fixedPool( 1 )) {
			final CountDownLatch counter = new CountDownLatch(1);
			final LocalExchange exchange = LocalExchange.of(ping);

			exchange.then((e, t) -> {
				assertEquals(ping, e.as(Pong.class).getPing());
				counter.countDown();
			});

			threads.submit(() -> exchange.reply(pong));
			counter.await();
		}
	}

	@Test(timeout = 15000)
	@SneakyThrows
	public void receiveAsyncResponseWhenListenerIsSetAfterResponseIsDefined() {
		try ( Threads threads = Threads.fixedPool( 1 )) {
			final CountDownLatch counter = new CountDownLatch(1);
			final LocalExchange exchange = LocalExchange.of(ping);

			threads.submit(() -> exchange.reply(pong));

			exchange.then((e, t) -> {
				assertEquals(ping, e.as(Pong.class).getPing());
				counter.countDown();
			});

			counter.await();
		}
	}

	@Test(timeout = 15000)
	public void readSyncResponseWhenValueIsSetAlmostAtSameTimeItIsRequested() {
		try ( Threads threads = Threads.fixedPool( 1 ) ) {
			final LocalExchange exchange = LocalExchange.of(ping);
			threads.submit(() -> exchange.reply(pong));
			final Pong pong = exchange.response();
			assertEquals(ping, pong.getPing());
		}
	}

	@Test(timeout = 15000)
	public void readSyncResponseWhenValueIsSetBeforeItIsRequested() {
		try ( Threads threads = Threads.fixedPool( 1 ) ) {
			final LocalExchange exchange = LocalExchange.of(ping);
			threads.submit(() -> exchange.reply(pong));
			sleep(1);
			final Pong pong = exchange.response();
			assertEquals(ping, pong.getPing());
		}
	}

	@Test(timeout = 15000)
	public void readSyncResponseWhenValueIsSetAfterItIsRequested() {
		try ( Threads threads = Threads.fixedPool( 1 ) ) {
			final LocalExchange exchange = LocalExchange.of(ping);
			threads.submit(() -> {
				sleep(1);
				exchange.reply(pong);
			});
			final Pong pong = exchange.response();
			assertEquals(ping, pong.getPing());
		}
	}

	@Test( timeout = 30000 )
	public void doIntenseStressTest() throws InterruptedException {
		final CountDownLatch counter = new CountDownLatch(90);
		try ( Threads threads = Threads.elasticPool() ) {
			for (int i = 0; i < 18; i++) {
				threads.submit(andCount(counter, this::readSyncResponseWhenValueIsSetAlmostAtSameTimeItIsRequested));
				threads.submit(andCount(counter, this::readSyncResponseWhenValueIsSetBeforeItIsRequested));
				threads.submit(andCount(counter, this::readSyncResponseWhenValueIsSetAfterItIsRequested));
				threads.submit(andCount(counter, this::receiveAsyncResponseWhenListenerIsSetAfterResponseIsDefined));
				threads.submit(andCount(counter, this::receiveAsyncResponseWhenListenerIsSetBeforeResponseIsDefined));
			}

			counter.await();
		}
	}

	static void sleep( int secs ){
		try {
			Thread.sleep(secs * 1000l);
		} catch (InterruptedException e) {
			throw new IllegalStateException(e);
		}
	}
}

class CounterInterceptor implements Runnable {

	CountDownLatch counter;
	Runnable runnable;

	@Override
	public void run() {
		try { runnable.run(); } catch( Exception e ) { e.printStackTrace(); }
		counter.countDown();
	}

	public static CounterInterceptor andCount( CountDownLatch counter, Runnable runnable ) {
		final CounterInterceptor interceptor = new CounterInterceptor();
		interceptor.counter = counter;
		interceptor.runnable = runnable;
		return interceptor;
	}

	@Override
	public String toString() {
		return runnable.toString();
	}
}

final class Ping {}

@Value
@Getter
final class Pong {
	final Ping ping;
}