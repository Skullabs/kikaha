package kikaha.urouting;

import java.util.*;
import java.util.function.Function;
import javax.annotation.PostConstruct;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.*;
import javax.inject.*;

import kikaha.core.util.Lang;
import kikaha.urouting.api.*;

@Singleton
@SuppressWarnings("rawtypes")
public class ConverterFactoryLoader {

	@Inject
	@Typed( AbstractConverter.class )
	Iterable<AbstractConverter> availableConverters;
	
	ConverterFactory factory;

	@PostConstruct
	public void onStartup() {
		factory = new ConverterFactory( loadAllConverters() );
	}
	
	@Produces
	public ConverterFactory produceFactory(){
		return factory;
	}

	public Map<String, AbstractConverter<?>> loadAllConverters() {
		final Map<String, AbstractConverter<?>> converters = loadPrimitiveConverters();
		for ( final AbstractConverter converter : availableConverters ){
			final String canonicalName = converter.getGenericClass().getCanonicalName();
			converters.put(canonicalName, converter);
		}
		return converters;
	}

	static private Map<String, AbstractConverter<?>> loadPrimitiveConverters(){
		final Map<String, AbstractConverter<?>> primitiveConverters = new HashMap<>();
		converterFrom( primitiveConverters, int.class, 0, Integer::parseInt );
		converterFrom( primitiveConverters, byte.class, (byte)0, Byte::parseByte );
		converterFrom( primitiveConverters, float.class, 0f, Float::parseFloat );
		converterFrom( primitiveConverters, double.class, 0.0, Double::parseDouble );
		converterFrom( primitiveConverters, long.class, 0L, Long::parseLong );
		converterFrom( primitiveConverters, short.class, (short)0, Short::parseShort );
		converterFrom( primitiveConverters, boolean.class, Boolean.FALSE, Boolean::parseBoolean );
		return primitiveConverters;
	}

	static private <T> void converterFrom(
			Map<String, AbstractConverter<?>> primitiveConverters,
			Class<T> primitiveType, T defaultValue, Function<String, T> converter)
	{
		primitiveConverters.put(
			primitiveType.getCanonicalName(),
			new AbstractConverter<T>() {
				@Override
				public T convert(String value) throws ConversionException {
					if (Lang.isUndefined(value))
						return defaultValue;
					return converter.apply(value);
				}
				@Override
				public Class<T> getGenericClass() { return primitiveType; }
			}
		);
	}
}
