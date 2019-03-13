package kikaha.urouting.it.params;

import com.fasterxml.jackson.databind.ObjectMapper;
import kikaha.core.test.KikahaServerRunner;
import kikaha.urouting.it.Http;
import kikaha.urouting.it.Http.EmptyText;
import lombok.Data;
import lombok.val;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author: miere.teixeira
 */
@RunWith( KikahaServerRunner.class )
public class QueryParametersIntegrationTest {

	final Request.Builder request = Http.url( "http://localhost:19999/it/parameters/query?id=12" +
			"&localDate=2020-10-10" +
			"&localDateTime=2020-10-10T10:15:30" +
			"&zonedDateTime=2020-10-10T10:15:30%2B08:00" );

	final Request.Builder requestWithEmptyParams = Http.url( "http://localhost:19999/it/parameters/query?id=" +
			"&localDate=" +
			"&localDateTime=" +
			"&zonedDateTime=" );

	@Test
	public void ensureCanSendGet() throws IOException {
		final Response response = Http.send( this.request.get() );
		ensureHaveTheExpectedResponse( response );
		final Response responseWithEmptyParams = Http.send( this.requestWithEmptyParams.get() );
		ensureHaveTheExpectedEmptyResponse( responseWithEmptyParams );
	}

	@Test
	public void ensureCanSendPost() throws IOException {
		final Response response = Http.send( this.request.post( new EmptyText() ) );
		ensureHaveTheExpectedResponse( response );
		final Response responseWithEmptyParams = Http.send( this.requestWithEmptyParams.post( new EmptyText() ) );
		ensureHaveTheExpectedEmptyResponse( responseWithEmptyParams );
	}

	@Test
	public void ensureCanSendPut() throws IOException {
		final Response response = Http.send( this.request.put( new EmptyText() ) );
		ensureHaveTheExpectedResponse( response );
		final Response responseWithEmptyParams = Http.send( this.requestWithEmptyParams.put( new EmptyText() ) );
		ensureHaveTheExpectedEmptyResponse( responseWithEmptyParams );
	}

	@Test
	public void ensureCanSendPatch() throws IOException {
		final Response response = Http.send( this.request.patch( new EmptyText() ) );
		ensureHaveTheExpectedResponse( response );
		final Response responseWithEmptyParams = Http.send( this.requestWithEmptyParams.patch( new EmptyText() ) );
		ensureHaveTheExpectedEmptyResponse( responseWithEmptyParams );
	}

	@Test
	public void ensureCanSendDelete() throws IOException {
		final Response response = Http.send( this.request.delete() );
		ensureHaveTheExpectedResponse( response );
		final Response responseWithEmptyParams = Http.send( this.requestWithEmptyParams.delete() );
		ensureHaveTheExpectedEmptyResponse( responseWithEmptyParams );
	}

	void ensureHaveTheExpectedResponse( final Response response ) throws IOException {
		assertEquals( 200, response.code() );
		val params = new ObjectMapper().readValue( response.body().string(), ResponseParams.class );
		assertEquals( 12, params.id );
		assertEquals( "2020-10-10", params.localDate );
		assertEquals( "2020-10-10T10:15:30", params.localDateTime );
		assertEquals( "2020-10-10T10:15:30+08:00", params.zonedDateTime );
	}

	void ensureHaveTheExpectedEmptyResponse( final Response response ) throws IOException {
		assertEquals( 200, response.code() );
		val params = new ObjectMapper().readValue( response.body().string(), ResponseParams.class );
		assertEquals( 0, params.id );
		assertNull( params.localDate );
		assertNull( params.localDateTime );
		assertNull( params.zonedDateTime );
	}

	@Data
	public static class ResponseParams {

		long id;
		String localDate;
		String localDateTime;
		String zonedDateTime;

	}
}
