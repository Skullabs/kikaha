package kikaha.cloud.smart.tracer;

import kikaha.config.Config;
import kikaha.core.DeploymentContext;
import kikaha.core.test.KikahaRunner;
import kikaha.urouting.ContextProducer;
import kikaha.urouting.ContextProducerFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import javax.inject.Inject;

import java.io.IOException;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link TraceIdModule}.
 */
@RunWith(KikahaRunner.class)
public class TraceIdModuleTest {

    @Inject TraceIdModule module;
    @Inject ContextProducerFactory factory;

    @Mock Config config;

    @Before
    public void configureMocks(){
        module.config = config = mock( Config.class );
        module.contextProducerFactory = factory = spy( factory );
        doReturn( "X-Trace-Id" ).when( config ).getString( eq("server.smart-server.trace-id.header-name") );
    }

    @Test
    public void ensureWillNotDeployTraceIdHttpHandlerIfModuleIsDisabled() throws IOException {
        doReturn( false ).when( config ).getBoolean( eq("server.smart-server.trace-id.enabled") );

        final DeploymentContext deploymentContext = spy( new DeploymentContext() );
        module.load( null, deploymentContext );

        verify( deploymentContext, never() ).rootHandler( any() );
        verify( deploymentContext, never() ).rootHandler();
        verify( factory, never() ).registerProducer( any(), any() );
    }

    @Test
    public void ensureWillNotDeployTraceIdHttpHandlerIfAProducerIsAlreadyRegisteredForTraceId() throws IOException {
        doReturn( true ).when( config ).getBoolean( eq("server.smart-server.trace-id.enabled") );
        doReturn( false ).when( factory ).registerProducer( eq( TraceId.class ), any(ContextProducer.class) );

        final DeploymentContext deploymentContext = spy( new DeploymentContext() );
        module.load( null, deploymentContext );

        verify( deploymentContext, never() ).rootHandler( any() );
        verify( deploymentContext, never() ).rootHandler();
        verify( factory, only() ).registerProducer( any(), any() );
    }

    @Test
    public void ensureWillDeployTraceIdHttpHandler() throws IOException {
        doReturn( true ).when( config ).getBoolean( eq("server.smart-server.trace-id.enabled") );

        final DeploymentContext deploymentContext = spy( new DeploymentContext() );
        module.load( null, deploymentContext );

        verify( deploymentContext ).rootHandler( any() );
        verify( deploymentContext ).rootHandler();
        verify( factory, only() ).registerProducer( any(), any() );
    }
}