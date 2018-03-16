package kikaha.urouting.api.converter;

import kikaha.urouting.api.AbstractConverter;

import javax.enterprise.inject.Typed;
import javax.inject.Singleton;

@Singleton
@Typed( AbstractConverter.class )
public class LongConverter extends AbstractConverter<Long> {

	@Override
	public Long convert( String value ) throws ConversionException {
		return Long.valueOf( value );
	}

}
