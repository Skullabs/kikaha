package kikaha.cloud.aws.lambda;

import kikaha.core.cdi.DefaultCDI;
import kikaha.urouting.api.Mimes;
import lombok.val;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AmazonContentTypeHandlersTest {

    @Inject AmazonContentTypeHandlers handlers;

    @Before
    public void injectDependencies(){
        DefaultCDI.newInstance().injectOn(this);
    }

    @Test
    public void canFindAContentType(){
        val foundHandler = handlers.get( Mimes.JSON );
        assertNotNull( foundHandler );
        assertEquals( JacksonJrAmazonLambdaSerializer.class, foundHandler.getClass() );
    }

    @Test( expected = AmazonRequestException.class )
    public void willFailToGetInvalidContentType(){
        handlers.get( Mimes.HTML );
    }
}
