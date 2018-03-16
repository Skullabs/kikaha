package kikaha.urouting.api.converter;

import kikaha.urouting.api.AbstractConverter;

import javax.enterprise.inject.Typed;
import javax.inject.Singleton;

@Singleton
@Typed( AbstractConverter.class )
public class ShortConverter extends AbstractConverter<Short> {

	@Override
	public Short convert( String value ) throws ConversionException {
		return Short.valueOf( value );
	}

}
