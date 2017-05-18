package kikaha.rocker;

import com.fizzed.rocker.runtime.RockerRuntime;
import io.undertow.server.HttpServerExchange;
import kikaha.config.Config;
import kikaha.core.test.HttpServerExchangeStub;
import kikaha.core.test.KikahaRunner;
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

    // RockerResource
    @Test
    public void testRockerResource() {
        RockerResource rockerResource = new RockerResource();
        rockerResource.renderTemplate("views/index.rocker.html");

        assertTrue(rockerResource instanceof RockerResource);
    }

    // RockerTemplate
    @Test
    public void testRockerTemplate() {
        RockerTemplate rockerTemplate = new RockerTemplate();
        rockerTemplate.paramObject = "Test";
        rockerTemplate.templateName = "index";

        assertThat( rockerTemplate.templateName, is( "index" ) );
        assertThat( rockerTemplate.paramObject, is( "Test" ) );
        assertEquals(true, rockerTemplate instanceof RockerTemplate);

    }

    // RockerResponse
    @Test
    public void testRockerResponse() {
        User user = new User();
        user.name = "Peter";
        RockerResponse response = RockerResponse.ok()
                .templateName( "index" )
                .paramObject( user  );

        assertEquals(true, response instanceof RockerResponse);
        assertTrue(response.entity.paramObject instanceof User);
        assertThat( response.entity.templateName, is( "index" ) );
    }


    // RockerSerializerFactory
    @Test
    public void testRockerSerializerFactory() {
        RockerSerializerFactory rockerSerializerFactory = new RockerSerializerFactory();
        rockerSerializerFactory.serializer();

        assertEquals(true, rockerSerializerFactory instanceof RockerSerializerFactory);
    }
}