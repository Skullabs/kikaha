package kikaha.urouting.api.converter;

import kikaha.urouting.api.AbstractConverter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@SuppressWarnings( "rawtypes" )
@RequiredArgsConstructor
public class ConverterFactory {

	final Map<String, AbstractConverter<?>> converters;

	@SuppressWarnings( "unchecked" )
	public <T> T decode( final String value, final Class<T> clazz ) throws ConversionException {
		try {
			if ( value == null || String.class.equals( clazz ) )
				return (T)value;
			final AbstractConverter<T> converter = getConverterFor( clazz );
			return converter.convert( value );
		} catch ( InstantiationException | IllegalAccessException e ) {
			throw new ConversionException( String.format(
					"Can't convert '%s' to '%s'", value, clazz.getCanonicalName() ), e );
		}
	}

	@SuppressWarnings( "unchecked" )
	public <T> AbstractConverter<T> getConverterFor(final Class<T> clazz )
			throws InstantiationException, IllegalAccessException, ConversionException {
		final String canonicalName = clazz.getCanonicalName();
		final AbstractConverter converter = converters.get(canonicalName);
		if ( converter == null )
			throw new ConversionException( "No converters defined to " + canonicalName );
		return converter;
	}
}
