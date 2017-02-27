package kikaha.cloud.consul;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import java.io.IOException;
import kikaha.cloud.smart.ServiceRegistry;
import kikaha.config.*;
import kikaha.core.cdi.ServiceProvider;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Unit tests for {@link ConsulClient}.
 */
@Ignore
@RunWith( MockitoJUnitRunner.class )
public class ConsulClientTest {

	final ServiceRegistry.ApplicationData applicationData = new ServiceRegistry.ApplicationData( () -> "unit01", () -> "localhost", "unit01", "1.0", 9000, false);
	final String EXPECTED_REGISTER_MSG = "{\"ID\": \"unit01\",\"Name\": \"unit01:1.0\",\"Address\": \"localhost\",\"Port\": 9000, \"Tags\": [],\"Check\": { \"DeregisterCriticalServiceAfter\": \"90m\", \"HTTP\": \"http://localhost:9000/api/internal/health-check\",\"Interval\": \"1s\"}}";

	Config config = ConfigLoader.loadDefaults();
	@Mock ServiceProvider serviceProvider;

	@InjectMocks
	@Spy ConsulClient consulClient;

	@Before
	public void configureMock(){
		consulClient.config = config;
	}

	@Test
	public void ensureCanRegisterOnConsul() throws IOException {
		consulClient.registerIntoCluster( applicationData );
		verify( consulClient ).post( eq("/v1/agent/service/register"), eq(EXPECTED_REGISTER_MSG) );
	}

	@Test
	public void ensureCanDeregisterOnConsul() throws IOException {
		ensureCanRegisterOnConsul();
		consulClient.deregisterFromCluster( applicationData );
		verify( consulClient ).put( eq("/v1/agent/service/deregister/unit01") );
	}
}