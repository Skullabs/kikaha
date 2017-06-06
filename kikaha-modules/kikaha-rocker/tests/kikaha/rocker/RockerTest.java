package kikaha.rocker;

import com.fizzed.rocker.runtime.RockerRuntime;
import io.undertow.server.HttpServerExchange;
import kikaha.config.Config;
import kikaha.core.test.HttpServerExchangeStub;
import kikaha.core.test.KikahaRunner;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;

import static junit.framework.TestCase.assertTrue;
import static kikaha.core.cdi.DefaultCDI.newInstance;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

@RunWith( KikahaRunner.class )
public class RockerTest {

    static private final Logger log = LoggerFactory.getLogger(RockerTest.class);

    static private final HttpServerExchange exchange = HttpServerExchangeStub.createHttpExchange();

    @Inject
    RockerSerializer serializer;

    @Mock
    Config config;

    @Before
    public void provideDependencies() {
        MockitoAnnotations.initMocks(this);
        doReturn( new File( "tests-resources" ).getAbsolutePath() ).when( config ).getString( "server.static.location" );
        serializer = spy( serializer );
        serializer.config = config;

        //enable rocker reloading bootstrap
        RockerRuntime.getInstance().setReloading(true);
    }

    // RockerTemplate
    @Test
    public void testRockerTemplate() {
        RockerTemplate rockerTemplate = new RockerTemplate();

        // set template name and objects.
        assertTrue(rockerTemplate.setObjects("Hello there") instanceof RockerTemplate);
        assertTrue(rockerTemplate.setTemplateName("index") instanceof RockerTemplate);

        // get template name.
        assertTrue( rockerTemplate.templateName instanceof String);
        assertThat( rockerTemplate.getTemplateName(), is( "index" ));

        // get objects.
        assertTrue( rockerTemplate.getObjects() instanceof Object);
        assertThat( rockerTemplate.getObjects(), is( new String[]{"Hello there"}));

        // Is rocker template?
        assertEquals(true, rockerTemplate instanceof RockerTemplate);

    }

    // RockerSerializerFactory
    @Test
    public void testRockerSerializerFactory() {
        RockerSerializerFactory rockerSerializerFactory = new RockerSerializerFactory();
        rockerSerializerFactory.serializer();

        assertEquals(true, rockerSerializerFactory instanceof RockerSerializerFactory);
    }

    // RockerResponse
    @Test
    public void testRockerResponse() {
        RockerResponse response = testSimulatedRockerResponse("Peter");

        assertEquals(true, response instanceof RockerResponse);
        assertTrue(response.entity.objects instanceof Object);
        assertThat( response.entity.templateName, is( "dummy.template" ) );
    }

    // RockerResponse simulated call.
    RockerResponse testSimulatedRockerResponse(String name) {
        User user = new User();
        user.name = name;
        RockerResponse response = RockerResponse.ok()
                .templateName( "dummy.template" )
                .objects( user );
        return response;
    }

}

