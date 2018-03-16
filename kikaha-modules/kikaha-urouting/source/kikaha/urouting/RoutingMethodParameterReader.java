package kikaha.urouting;

import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.Cookie;
import io.undertow.server.handlers.form.FormData;
import io.undertow.server.handlers.form.FormData.FormValue;
import io.undertow.server.handlers.form.FormDataParser;
import io.undertow.util.HeaderMap;
import io.undertow.util.Headers;
import io.undertow.util.PathTemplateMatch;
import kikaha.config.Config;
import kikaha.urouting.api.*;
import kikaha.urouting.api.converter.ConversionException;
import kikaha.urouting.api.converter.ConverterFactory;
import kikaha.urouting.serializers.SerializerAndUnserializerProvider;
import kikaha.urouting.serializers.Unserializer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.Map;
import java.util.Queue;
import java.util.function.Function;

import static kikaha.urouting.RoutingMethodParameterReader.ContentTypePriority.CONFIG;

/**
 * Provides data to a routing method.
 */
@Slf4j
@Singleton
public class RoutingMethodParameterReader {

	@Inject Config kikahaConf;
	@Inject
    ConverterFactory converterFactory;
	@Inject
    ContextProducerFactory contextProducerFactory;
	@Inject
    SerializerAndUnserializerProvider serializerAndUnserializerProvider;

	@Getter String defaultEncoding;
	@Getter String defaultContentType;

	Function<HeaderMap, String> contentTypeSupplier;

	@PostConstruct
	public void readConfig() {
		defaultEncoding = kikahaConf.getString("server.urouting.default-encoding");
		defaultContentType = kikahaConf.getString("server.urouting.default-content-type");
		final String contentTypePriority = kikahaConf.getString( "server.urouting.content-type-priority" );

		contentTypeSupplier = ContentTypePriority.from( contentTypePriority ).equals( CONFIG )
			? this::getDefaultContentType
			: this::getContentFromRequest;

		log.info( "Micro Routing API" );
		log.info( "  default-encoding: " + defaultEncoding );
		log.info( "  default-content-type: " + defaultContentType );
		log.info( "  content-type-priority: " + contentTypePriority );
	}

	String getContentFromRequest( HeaderMap headerMap ){
		final String contentType = headerMap.getFirst(Headers.CONTENT_TYPE_STRING);
		return headerMap == null ? getDefaultContentType() : contentType;
	}

	String getDefaultContentType( HeaderMap headerMap ){
		return getDefaultContentType();
	}

	/**
	 * Get a cookie from request converted to the {@code <T>} type as defined by
	 * {@code clazz} argument.
	 *
	 * @param exchange
	 * @param cookieParam
	 * @param clazz
	 * @return
	 * @throws ConversionException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public <T> T getCookieParam(final HttpServerExchange exchange, final String cookieParam, final Class<T> clazz)
			throws ConversionException,
			InstantiationException, IllegalAccessException {
		final Cookie cookie = exchange.getRequestCookies().get(cookieParam);
		if (cookie == null)
			return null;
		final String value = cookie.getValue();
		return converterFactory.getConverterFor(clazz).convert(value);
	}

	/**
	 * Get a query parameter from request converted to the {@code <T>} type as
	 * defined by {@code clazz} argument.
	 *
	 * @param exchange
	 * @param queryParam
	 * @param clazz
	 * @return
	 * @throws ConversionException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public <T> T getQueryParam(final HttpServerExchange exchange, final String queryParam, final Class<T> clazz)
			throws ConversionException, InstantiationException, IllegalAccessException {
		final Queue<String> queryParams = exchange.getQueryParameters().get(queryParam);
		if (queryParams == null)
			return null;
		final String value = queryParams.peek();
		return converterFactory.getConverterFor(clazz).convert(value);
	}

	@SuppressWarnings("unchecked")
	public <T> T getFormParam(final HttpServerExchange exchange, final String formParam, final Class<T> clazz)
			throws ConversionException, InstantiationException, IllegalAccessException {
		final FormData form = exchange.getAttachment( FormDataParser.FORM_DATA );
		if (form == null)
			throw new IllegalAccessException( "Could not found the FormData." );
		final FormValue formValue = form.getFirst(formParam);
		if (formValue == null)
			return null;
		if (formValue.isFile())
			return (T) formValue.getPath().toFile();
		final String value = formValue.getValue();
		return converterFactory.getConverterFor(clazz).convert(value);
	}

	/**
	 * Get a header parameter from request converted to the {@code <T>} type as
	 * defined by {@code clazz} argument.
	 *
	 * @param exchange
	 * @param headerParam
	 * @param clazz
	 * @return
	 * @throws ConversionException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public <T> T getHeaderParam(final HttpServerExchange exchange, final String headerParam, final Class<T> clazz)
			throws ConversionException, InstantiationException, IllegalAccessException {
		final String value = exchange.getRequestHeaders().getFirst(headerParam);
		if (value == null)
			return null;
		return converterFactory.getConverterFor(clazz).convert(value);
	}

	/**
	 * Get a path parameter from request converted to the {@code <T>} type as
	 * defined by {@code clazz} argument.
	 *
	 * @param exchange
	 * @param pathParam
	 * @param clazz
	 * @return
	 * @throws ConversionException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public <T> T getPathParam(final HttpServerExchange exchange, final String pathParam, final Class<T> clazz)
			throws ConversionException, InstantiationException, IllegalAccessException {
		final String value = getPathParams(exchange).get(pathParam);
		return converterFactory.getConverterFor(clazz).convert(value);
	}

	/**
	 * Retrieve all path parameters related to this request.
	 *
	 * @param exchange
	 * @return
	 */
	public Map<String, String> getPathParams( final HttpServerExchange exchange ){
		final PathTemplateMatch pathTemplate = exchange.getAttachment( PathTemplateMatch.ATTACHMENT_KEY );
		return pathTemplate.getParameters();
	}

	/**
	 * Get the body of current request and convert to {@code <T>} type as
	 * defined by {@code clazz} argument.
	 *
	 * @param exchange
	 * @param clazz
	 * @return
	 * @throws IOException
	 */
	public <T> T getBody(final HttpServerExchange exchange, final Class<T> clazz, final byte[] bodyData) throws IOException {
		return getBody(exchange, clazz, bodyData, getDefaultContentType());
	}

	/**
	 * Get the body of current request and convert to {@code <T>} type as
	 * defined by {@code clazz} argument.<br>
	 * <br>
	 * <p>
	 * It searches for {@link Unserializer} implementations to convert sent data
	 * from client into the desired object. The "Content-Type" header is the
	 * information needed to define which {@link Unserializer} should be used to
	 * decode the sent data into an object. When no {@link Unserializer} is
	 * found it uses the {@code fallbackConsumingContentType} argument to seek
	 * another one. It throws {@link IOException} when no decoder was
	 * found.
	 *
	 * @param exchange
	 * @param clazz
	 * @param fallbackConsumingContentType
	 * @return
	 * @throws IOException
	 */
	public <T> T getBody(final HttpServerExchange exchange, final Class<T> clazz, final byte[] bodyData, final String fallbackConsumingContentType)
			throws IOException {
		final HeaderMap requestHeaders = exchange.getRequestHeaders();
		String contentEncoding = requestHeaders.getFirst(Headers.CONTENT_ENCODING_STRING);
		if (contentEncoding == null)
			contentEncoding = getDefaultEncoding();
		final String contentType = contentTypeSupplier.apply( requestHeaders );
		return unserializeReceivedBodyStream(exchange, clazz, bodyData, contentEncoding, contentType);
	}

	private <T> T unserializeReceivedBodyStream(
			final HttpServerExchange exchange, final Class<T> clazz, final byte[] bodyData,
            final String contentEncoding, final String contentType) throws IOException
	{
		if (!exchange.isBlocking())
			exchange.startBlocking();
		final Unserializer unserializer = serializerAndUnserializerProvider.getUnserializerFor(contentType);
		return unserializer.unserialize(exchange, clazz, bodyData, contentEncoding );
	}

	/**
	 * Retrieve ( or produces ) a request-time object.
	 *
	 * @param exchange
	 * @param clazz
	 * @return
	 * @throws RoutingException
	 */
	public <T> T getData(final HttpServerExchange exchange, final Class<T> clazz) throws RoutingException {
		final ContextProducer<T> producerFor = contextProducerFactory.producerFor(clazz);
		if (producerFor != null)
			return producerFor.produce(exchange);
		throw new RoutingException("No context provider for " + clazz.getCanonicalName());
	}

	enum ContentTypePriority {
		CONFIG, REQUEST;

		static ContentTypePriority from( String contentTypePriority ) {
			try {
				if ( contentTypePriority != null && !contentTypePriority.isEmpty() )
					return valueOf( contentTypePriority );
			} catch ( Throwable cause ) {
				log.error( "Can't identify Content-Type priority", cause );
			}
			return REQUEST;
		}
	}
}
