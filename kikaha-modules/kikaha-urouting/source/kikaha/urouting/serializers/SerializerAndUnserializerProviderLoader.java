package kikaha.urouting.serializers;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.inject.Singleton;

import kikaha.core.modules.http.ContentType;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class SerializerAndUnserializerProviderLoader {

	@Inject
	@Typed(Serializer.class)
	Iterable<Serializer> availableSerializers;

	@Inject
	@Typed(Unserializer.class)
	Iterable<Unserializer> availableUnserializers;
	
	SerializerAndUnserializerProvider serializerAndUnserializerProvider;

	@PostConstruct
	public void onStartup() {
		final Map<String, Serializer> serializers = loadAllSerializers();
		log.trace( "Found Content Type serializers: " + serializers );
		final Map<String, Unserializer> unserializers = loadAllUnserializers();
		log.trace( "Found Content Type unserializers: " + unserializers );
		serializerAndUnserializerProvider
			= new SerializerAndUnserializerProvider(serializers, unserializers);
	}
	
	@Produces
	public SerializerAndUnserializerProvider produceSerializerAndUnserializerProvider(){
		return serializerAndUnserializerProvider;
	}

	private Map<String, Serializer> loadAllSerializers() {
		final Map<String,Serializer> serializers = new HashMap<>();
		for ( final Serializer serializer : availableSerializers )
			serializers.put( extractContentTypeFrom(serializer), serializer );
		return serializers;
	}

	private Map<String, Unserializer> loadAllUnserializers() {
		final Map<String,Unserializer> unserializers = new HashMap<>();
		for ( final Unserializer unserializer : availableUnserializers )
			unserializers.put( extractContentTypeFrom(unserializer), unserializer );
		return unserializers;
	}

	String extractContentTypeFrom( final Object target ){
		final Class<?> clazz = target.getClass();
		final ContentType contentType = clazz.getAnnotation(ContentType.class);
		if ( contentType!=null )
			return contentType.value();
		throw new UnsupportedOperationException( clazz +  " must be annotated with @" + ContentType.class.getSimpleName() );
	}
}
