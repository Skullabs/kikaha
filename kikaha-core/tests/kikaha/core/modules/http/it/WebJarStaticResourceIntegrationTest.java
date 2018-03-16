package kikaha.core.modules.http.it;

import static kikaha.core.modules.http.StaticResourceModule.DEFAULT_WEBJAR_LOCATION;
import static org.junit.Assert.*;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import kikaha.core.test.KikahaServerRunner;
import kikaha.commons.SystemResource;
import okhttp3.*;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by ibratan on 16/06/2017.
 */
@RunWith(KikahaServerRunner.class)
public class WebJarStaticResourceIntegrationTest {

    static final String
        JQUERY_LOCATION = "jquery/3.2.1/jquery.min.js",
        JQUERY_CONTENT = SystemResource.readFileAsString(DEFAULT_WEBJAR_LOCATION + JQUERY_LOCATION, "UTF-8");

    final OkHttpClient client = new OkHttpClient()
            .newBuilder()
            .connectTimeout(3, TimeUnit.SECONDS)
            .readTimeout(3, TimeUnit.SECONDS)
            .writeTimeout(3, TimeUnit.SECONDS)
            .followRedirects(false).build();

    @Test
    public void ensureThatCanRetrieveJQuery() throws IOException {
        final Request.Builder url = new Request.Builder().url("http://localhost:9000/assets/" + JQUERY_LOCATION );
        final Response response = client.newCall( url.build() ).execute();
        assertEquals( 200, response.code() );
        final String responseAsString  = response.body().string();
        assertNotNull(responseAsString );
        assertEquals( JQUERY_CONTENT, responseAsString );
    }

    @Test
    public void ensureThatCanRetrieveLocalFile() throws IOException {
        final Request.Builder url = new Request.Builder().url("http://localhost:9000/assets/file.js" );
        final Response response = client.newCall( url.build() ).execute();
        assertEquals( 200, response.code() );
        final String responseAsString  = response.body().string();
        assertNotNull(responseAsString );
        assertEquals( "file.js", responseAsString );
    }
}
