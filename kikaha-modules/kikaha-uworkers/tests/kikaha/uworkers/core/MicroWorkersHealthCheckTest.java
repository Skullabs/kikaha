package kikaha.uworkers.core;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;

import com.codahale.metrics.health.HealthCheck.Result;
import kikaha.commons.Threads;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Unit tests for MicroWorkersHealthCheck.
 */
@RunWith( MockitoJUnitRunner.class )
public class MicroWorkersHealthCheckTest {

	MicroWorkersHealthCheck healthCheck = new MicroWorkersHealthCheck();
	@Mock
    Threads threads;

	@Before
	public void configureMocks(){
		final MicroWorkersTaskDeploymentModule module = new MicroWorkersTaskDeploymentModule();
		module.threads = threads;
		healthCheck.module = module;
	}

	@Test
	public void ensureIsHealthIfNoThreadHavePrematurelyDied(){
		doReturn( 19 ).when( threads ).getTotalOfActiveTasks();
		doReturn( 19 ).when( threads ).getTotalOfScheduledTasks();

		final Result result = healthCheck.execute();
		assertNull( result.getMessage() );
		assertTrue( result.isHealthy() );
	}

	@Test
	public void ensureIsNotHealthIfAtLeastOneThreadHavePrematurelyDied(){
		doReturn( 19 ).when( threads ).getTotalOfActiveTasks();
		doReturn( 20 ).when( threads ).getTotalOfScheduledTasks();

		final Result result = healthCheck.execute();
		assertFalse( result.isHealthy() );
	}
}