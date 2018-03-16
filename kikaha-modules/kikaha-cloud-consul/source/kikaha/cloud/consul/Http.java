package kikaha.cloud.consul;

import kikaha.commons.Tuple;

import java.io.*;
import java.net.*;

/**
 *
 */
public interface Http {

	static int sendRequest( String url, String method, String msg ) throws IOException {
		final HttpURLConnection con = createRequest( url, method );
		con.setDoOutput(true);

		// Send post request
		if ( msg != null )
			try ( final DataOutputStream wr = new DataOutputStream(con.getOutputStream()) ) {
				wr.writeBytes(msg);
				wr.flush();
			}

		return con.getResponseCode();
	}

	static Tuple<Integer, String> get( String url ) throws IOException {
		final HttpURLConnection con = createRequest( url, "GET" );
		con.setDoInput( true );

		final int statusCode = con.getResponseCode();
		final InputStream inputStream = (200 <= statusCode && statusCode <= 299)
			? con.getInputStream() : con.getErrorStream();
		final String body = readFully( inputStream ).toString( "UTF-8" );
		return Tuple.of( statusCode, body );
	}

	static ByteArrayOutputStream readFully(InputStream inputStream) throws IOException {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int length = 0;
		while ((length = inputStream.read(buffer)) != -1) {
			baos.write(buffer, 0, length);
		}
		return baos;
	}

	static HttpURLConnection createRequest( String url, String method ) throws IOException {
		final URL obj = new URL( url );
		HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
		conn.setRequestMethod( method );
		return conn;
	}
}
