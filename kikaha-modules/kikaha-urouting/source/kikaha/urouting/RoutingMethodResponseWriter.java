package kikaha.urouting;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderMap;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;
import kikaha.config.Config;
import kikaha.urouting.api.Header;
import kikaha.urouting.api.Response;
import kikaha.urouting.api.RoutingException;
import kikaha.urouting.serializers.Serializer;
import kikaha.urouting.serializers.SerializerAndUnserializerProvider;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;

/**
 * A helper class to write responses to the HTTP Client.
 */
@Singleton @Slf4j
public class RoutingMethodResponseWriter {

	@Inject
	Config kikahaConf;

	@Inject
    SerializerAndUnserializerProvider serializerAndUnserializerProvider;

	@Getter
	String defaultEncoding;

	@Getter
	String defaultContentType;

	@PostConstruct
	public void readConfig(){
		defaultEncoding = kikahaConf.getString("server.urouting.default-encoding");
		defaultContentType = kikahaConf.getString("server.urouting.default-content-type");
	}

	/**
	 * Writes a create to HTTP Client informing that no content was available.
	 *
	 * @param exchange
	 */
	public void write( final HttpServerExchange exchange ) {
		sendStatusCode( exchange, 204 );
		exchange.endExchange();
	}

	/**
	 * Serialize and send the {@code create} object to the HTTP Client.
	 *
	 * @param exchange
	 * @param response
	 * @throws IOException
	 * @see Response
	 */
	public void write( final HttpServerExchange exchange, final Response response ) throws IOException {
		write( exchange, getDefaultContentType(), response );
	}

	/**
	 * Serialize and send the {@code create} object to the HTTP Client.
	 *
	 * @param exchange
	 * @param response
	 * @throws IOException
	 * @see Response
	 */
	public void write( final HttpServerExchange exchange, final Object response )
			throws IOException {
		write( exchange, getDefaultContentType(), response );
	}

	/**
	 * Serialize and send the {@code create} object to the HTTP Client. The
	 * HTTP Status Code will always be 200, in this case. Also, it will send use
	 * <em>UTF-8</em> as default encoding.
	 *
	 * @param exchange
	 * @param contentType
	 * @param response
	 * @throws IOException
	 */
	public void write( final HttpServerExchange exchange, final String contentType, final Object response ) throws IOException {
		sendStatusCode( exchange, 200 );
		sendContentTypeHeader( exchange, contentType );
		sendBodyResponse( exchange, contentType, getDefaultEncoding(), response );
	}

	/**
	 * Serialize and send the {@code create} object to the HTTP Client.
	 *
	 * @param exchange
	 * @param defaultContentType
	 * @param response
	 * @throws RoutingException
	 * @throws IOException
	 */
	public void write( final HttpServerExchange exchange, final String defaultContentType, final Response response ) throws IOException {
		final String contentType = response.contentType() != null ? response.contentType() : defaultContentType;
		sendStatusCode( exchange, response.statusCode() );
		sendHeaders( exchange, response );
		sendContentTypeHeader( exchange, contentType );
		sendBodyResponse( exchange, contentType, response.encoding(), response.entity() );
	}

	HttpServerExchange sendStatusCode( final HttpServerExchange exchange, final Integer statusCode ) {
		exchange.setStatusCode( statusCode );
		return exchange;
	}

	void sendBodyResponse(
			final HttpServerExchange exchange, final String contentType,
			final String encoding, final Object serializable ) throws IOException
	{
		final Serializer serializer = getSerializer( contentType );
		serializer.serialize( serializable, exchange, encoding );
	}

	private Serializer getSerializer( final String contentType ) throws IOException {
		return serializerAndUnserializerProvider.getSerializerFor( contentType );
	}

	private void sendHeaders( final HttpServerExchange exchange, final Response response ) {
		final HeaderMap responseHeaders = exchange.getResponseHeaders();
		if ( response.headers() != null )
			for ( final Header header : response.headers() )
				for ( final String value : header.values() )
					sendHeader(responseHeaders, header, value);
	}

	void sendHeader(final HeaderMap responseHeaders, final Header header, final String value) {
		responseHeaders.add( new HttpString(header.name()), value );
	}

	void sendContentTypeHeader( final HttpServerExchange exchange, final String contentType ) {
		final HeaderMap responseHeaders = exchange.getResponseHeaders();
		if ( !responseHeaders.contains( Headers.CONTENT_TYPE_STRING ) && contentType != null )
			responseHeaders.add( Headers.CONTENT_TYPE, contentType );
	}
}
