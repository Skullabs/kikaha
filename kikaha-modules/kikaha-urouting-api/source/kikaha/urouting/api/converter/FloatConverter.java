package kikaha.urouting.api.converter;

import kikaha.urouting.api.AbstractConverter;

import javax.enterprise.inject.Typed;
import javax.inject.Singleton;

@Singleton
@Typed( AbstractConverter.class )
public class FloatConverter extends AbstractConverter<Float> {

	@Override
	public Float convert( String value ) throws ConversionException {
		return Float.valueOf( value );
	}

}
