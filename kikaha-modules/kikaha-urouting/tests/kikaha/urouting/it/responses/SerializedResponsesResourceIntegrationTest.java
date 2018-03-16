package kikaha.urouting.it.responses;

import static org.junit.Assert.*;

import kikaha.core.test.KikahaServerRunner;
import kikaha.urouting.api.Mimes;
import kikaha.urouting.it.Http;
import okhttp3.*;
import okhttp3.Request.Builder;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Unit tests for SerializedResponsesResource.
 */
@RunWith( KikahaServerRunner.class )
public class SerializedResponsesResourceIntegrationTest {

	static final String YML_RESPONSE = "{id: 12}\n", PLAIN_TEXT_RESPONSE = "{id=12}";

	@Test
	public void ensureNoContent() throws Exception {
		final Request.Builder request = request( "http://localhost:19999/it/parameters/serialized/no-content" );
		final Response response = Http.send( request );
		assertEquals( 204, response.code() );
	}

	@Test
	public void ensureSerializingNativeObjectAsDefaultType() throws Exception {
		final Request.Builder request = request( "http://localhost:19999/it/parameters/serialized/native-as-default-type" );
		final Response response = Http.send( request );
		assertEquals( 200, response.code() );
		assertEquals( Mimes.PLAIN_TEXT, response.header( "Content-Type" ) );
		assertEquals( PLAIN_TEXT_RESPONSE, response.body().string() );
	}

	@Test
	public void ensureSerializingNativeObjectAsYaml() throws Exception {
		final Request.Builder request = request( "http://localhost:19999/it/parameters/serialized/native-as-yaml" );
		final Response response = Http.send( request );
		assertEquals( 200, response.code() );
		assertEquals( YmlSerializer.MIME, response.header( "Content-Type" ) );
		assertEquals( YML_RESPONSE, response.body().string() );
	}

	@Test
	public void ensureSerializingResponseObjectAsDefaultType() throws Exception {
		final Request.Builder request = request( "http://localhost:19999/it/parameters/serialized/create-as-default-type" );
		final Response response = Http.send( request );
		assertEquals( 200, response.code() );
		assertEquals( Mimes.PLAIN_TEXT, response.header( "Content-Type" ) );
		assertEquals( PLAIN_TEXT_RESPONSE, response.body().string() );
	}

	@Test
	public void ensureSerializingResponseObjectAsYaml() throws Exception {
		final Request.Builder request = request( "http://localhost:19999/it/parameters/serialized/create-as-yaml" );
		final Response response = Http.send( request );
		assertEquals( 200, response.code() );
		assertEquals( YmlSerializer.MIME, response.header( "Content-Type" ) );
		assertEquals( YML_RESPONSE, response.body().string() );
	}

	@Test
	public void ensureSerializingResponseObjectAsYaml2() throws Exception {
		final Request.Builder request = request( "http://localhost:19999/it/parameters/serialized/create-as-yaml2" );
		final Response response = Http.send( request );
		assertEquals( 200, response.code() );
		assertEquals( YmlSerializer.MIME, response.header( "Content-Type" ) );
		assertEquals( YML_RESPONSE, response.body().string() );

	}

	@Test
	public void ensureSerializingResponseObjectAsYaml3() throws Exception {
		final Request.Builder request = request( "http://localhost:19999/it/parameters/serialized/create-as-yaml3" );
		final Response response = Http.send( request );
		assertEquals( 200, response.code() );
		assertEquals( YmlSerializer.MIME, response.header( "Content-Type" ) );
		assertEquals( YML_RESPONSE, response.body().string() );
	}

	@Test
	public void ensureRaiseAnException() throws Exception {
		final Request.Builder request = request( "http://localhost:19999/it/parameters/serialized/failure" );
		final Response response = Http.send( request );
		assertEquals( 500, response.code() );
	}

	static Request.Builder request( String url ){
		return new Builder().url( url ).get();
	}
}