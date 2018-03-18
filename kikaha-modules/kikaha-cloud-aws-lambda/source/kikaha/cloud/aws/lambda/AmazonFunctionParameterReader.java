package kikaha.cloud.aws.lambda;

import kikaha.commons.Cookie;
import kikaha.commons.Reflection;
import kikaha.urouting.api.converter.ConverterFactory;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
@Slf4j
@Singleton
@SuppressWarnings( "unchecked" )
public class AmazonFunctionParameterReader {

    @Inject AmazonContentTypeHandlers contentTypeHandlers;
	@Inject ConverterFactory converterFactory;
	@Inject @Typed( AmazonContextProducer.class )
	Iterable<AmazonContextProducer> availableProducers;

	@Getter(lazy = true)
	private final Map<Class, AmazonContextProducer> contextProducers = loadAllProducers();

	public <T> T getCookieParam(final AmazonHttpRequest request, final String cookieParam, final Class<T> clazz) {
		final Cookie cookie = request.getCookies().get( cookieParam );
		if ( cookie == null )
			return null;
		return convert(cookie.getValue(), clazz);
	}

	public <T> T getHeaderParam(final AmazonHttpRequest request, final String cookieParam, final Class<T> clazz) {
		final Map<String, String> headers = request.getHeaders();
		final String cookieValue = headers.get( cookieParam );
		if (cookieValue == null)
			return null;
		return convert(cookieValue, clazz);
	}

	public <T> T getPathParam(final AmazonHttpRequest request, final String queryParam, final Class<T> clazz) {
		final String param = request.getPathParameters().get( queryParam );
		if (param == null)
			return null;
		return convert(param, clazz);
	}

	public <T> T getQueryParam(final AmazonHttpRequest request, final String queryParam, final Class<T> clazz) {
		final String param = request.getQueryStringParameters().get( queryParam );
		if (param == null)
			return null;
		return convert(param, clazz);
	}

	public <T> T getContextParam(final AmazonHttpRequest request, final Class<T> clazz ) {
		final AmazonContextProducer<T> producer = getContextProducers().get(clazz);
		if ( producer == null )
			return null;
		return producer.produce( request );
	}

	private <T> T convert( String value, Class<T> targetClass ){
		try {
			return converterFactory.getConverterFor(targetClass).convert(value);
		} catch ( Throwable cause ) {
			throw new IllegalStateException( "Could not convert to " + targetClass + ". Value: " + value, cause );
		}
	}

	public <T> T getBody(AmazonHttpRequest request, Class<T> userClass ) {
        return contentTypeHandlers.get( request.getContentType() ).unserialize( request.body, userClass );
	}

	private Map<Class, AmazonContextProducer> loadAllProducers() {
		final Map<Class, AmazonContextProducer> producers = new HashMap<>();
		for ( final AmazonContextProducer<?> producer : availableProducers ){
			final Class<?> forClazz = Reflection.getFirstGenericTypeFrom( producer, AmazonContextProducer.class );
			producers.put( forClazz, producer );
		}
		return producers;
	}
}
