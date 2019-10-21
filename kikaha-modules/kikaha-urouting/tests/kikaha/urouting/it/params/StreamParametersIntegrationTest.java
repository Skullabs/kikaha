package kikaha.urouting.it.params;

import kikaha.core.test.KikahaServerRunner;
import kikaha.urouting.it.Http;
import okhttp3.MediaType;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * @author: ronei.gebert
 */
@RunWith( KikahaServerRunner.class )
public class StreamParametersIntegrationTest {

	final Builder request = new Builder()
			.url( "http://localhost:19999/it/parameters/is" );

	final RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain"), "12");

	@Test
	public void ensureCanSendPost() throws IOException {
		final Response response = Http.send( this.request.post(requestBody) );
		ensureHaveTheExpectedResponse( response );
	}

	@Test
	public void ensureCanSendPut() throws IOException {
		final Response response = Http.send( this.request.put(requestBody) );
		ensureHaveTheExpectedResponse( response );
	}

	void ensureHaveTheExpectedResponse( final Response response ) throws IOException {
		assertEquals( 200, response.code() );
		assertEquals( 912, Integer.valueOf( response.body().string() ), 0 );
	}
}
