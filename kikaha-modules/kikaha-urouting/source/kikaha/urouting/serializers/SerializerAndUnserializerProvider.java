package kikaha.urouting.serializers;

import java.io.IOException;
import java.util.Map;
import kikaha.commons.url.URL;
import kikaha.urouting.UnsupportedMediaTypeException;
import kikaha.urouting.api.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class SerializerAndUnserializerProvider {

	final Map<String, Serializer> serializerByContentType;
	final Map<String, Unserializer> unserializerByContentType;

	public Serializer getSerializerFor( final String contentType ) throws IOException{
		Serializer serializer = serializerByContentType.get(contentType);
		if ( serializer == null ) {
			final UnsupportedMediaTypeException exception = new UnsupportedMediaTypeException(contentType);
			log.error("Could not found a serializer for " + contentType, exception);
			throw exception;
		}
		return serializer;
	}

	/**
	 * Retrieves an {@link Unserializer} for a given {@code contentType}
	 * argument. When no {@link Unserializer} is found it uses the
	 * {@code defaulConsumingContentType} argument to seek another one. It
	 * throws {@link RoutingException} when no decoder was found.
	 *
	 * @param contentType
	 * @return
	 * @throws IOException
	 */
	public Unserializer getUnserializerFor( String contentType ) throws IOException{
		contentType = URL.fixContentType(contentType, null).getFirst();

		final Unserializer serializer = unserializerByContentType.get( contentType );
		if ( serializer == null ) {
			final UnsupportedMediaTypeException exception = new UnsupportedMediaTypeException(contentType);
			log.error("Could not found an unserializer for " + contentType, exception);
			throw exception;
		}
		return serializer;
	}
}
