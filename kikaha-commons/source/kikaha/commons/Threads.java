package kikaha.commons;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

import lombok.*;

/**
 *
 */
@SuppressWarnings("unchecked")
public class Threads implements AutoCloseable {

	final Queue<Future<?>> asyncJobs = new ArrayDeque<>();
	final ExecutorService executorService;

	public Threads( final ExecutorService executorService ) {
		this.executorService = executorService;
		Runtime.getRuntime().addShutdownHook( new Thread( this::shutdown ) );
	}

	public static Threads elasticPool() {
		return new Threads( Executors.newCachedThreadPool() );
	}

	public static Threads fixedPool( int numberOfThreads ) {
		return new Threads( Executors.newFixedThreadPool( numberOfThreads ) );
	}

	public void submit( Runnable runnable ) {
		final Future<?> future = executorService.submit(runnable);
		asyncJobs.add( future );
	}

	public <R> BackgroundJob<R>  background() {
		return new BackgroundJob<> ();
	}

	public <R> BackgroundJob<R>  computeInBackground( Class<R> responseType ) {
		return new BackgroundJob<> ();
	}

	public synchronized void shutdown(){
		try {
			executorService.shutdown();
			shutdownNow();
		} catch ( final Exception cause ) {
			throw new RuntimeException( cause );
		}
	}

	private void shutdownNow() throws InterruptedException {
		Future<?> future;
		while ( ( future = asyncJobs.poll() ) != null )
			try {
				future.get( 100, MILLISECONDS );
			} catch ( TimeoutException | ExecutionException c ) { }
		executorService.shutdownNow();
	}

	@Override
	public void close() {
		shutdown();
	}

	/**
	 * Return the total of tasks that have been previously submitted to run in background.
	 * Note that this method does not check if the task have finished or not.
	 *
	 * @return
	 */
	public int getTotalOfScheduledTasks(){
		return asyncJobs.size();
	}

	/**
	 * Return the total os tasks previously submitted to run in background that stills running.
	 *
	 * @return
	 */
	public int getTotalOfActiveTasks() {
		int total = 0;
		for ( Future<?> future : asyncJobs )
			if ( !future.isDone() )
				total++;
		return total;
	}

	/**
	 * Represents a set of tasks that would/should be executed in background.
	 */

	public class BackgroundJob<R> implements AutoCloseable {

		final Queue<Future<?>> asyncJobs = new ArrayDeque<>();
		Consumer<Throwable> onError = Throwable::printStackTrace;
		Consumer<List<R>> onFinish = e -> {};

		public BackgroundJob run( RunnableThatMayFail runnable ) {
			final ParallelJobRunner jobRunner = new ParallelJobRunner(runnable, onError);
			return run( jobRunner );
		}

		public <T> BackgroundJob<R> compute( Callable<T> callable ) {
			final ParallelJobRunner jobRunner = new ParallelJobRunner(callable, onError);
			return run( jobRunner );
		}

		private BackgroundJob<R> run( final ParallelJobRunner jobRunner ) {
			final Future<?> future = executorService.submit(jobRunner);
			asyncJobs.add( future );
			return this;
		}

		public BackgroundJob<R> onError( Consumer<Throwable> errorHandler ){
			if ( !asyncJobs.isEmpty() )
				throw new IllegalStateException( "Cannot handle while jobs are running..." );
			this.onError = errorHandler;
			return this;
		}

		public BackgroundJob<R> onFinish(Consumer<List<R>> successHandler ) {
			if ( !asyncJobs.isEmpty() )
				throw new IllegalStateException( "Cannot handle while jobs are running..." );
			this.onFinish = successHandler;
			return this;
		}

		public List<R> awaitResponses(){
			final List<R> response = new ArrayList<>( asyncJobs.size() );
			try {
				Future<?> future;
				while ((future = asyncJobs.poll()) != null)
					response.add( (R) future.get());
				onFinish.accept( response );
			} catch ( ExecutionException | InterruptedException c ) {
				throw new IllegalStateException(c);
			}
			return response;
		}

		@Override
		public void close() {
			await();
		}

		public void await() {
			awaitResponses();
		}
	}

	@FunctionalInterface
	public interface RunnableThatMayFail extends Callable {

		default Object call() throws Exception {
			run();
			return null;
		}

		void run() throws Exception;
	}
}

@RequiredArgsConstructor
class ParallelJobRunner implements Callable {

	final Callable runnable;
	final Consumer<Throwable> onErrorHandler;

	@Override
	public Object call() {
		try {
			return runnable.call();
		} catch ( Exception e ) {
			onErrorHandler.accept( e );
		}
		return null;
	}
}