package kikaha.urouting.api.converter;

import kikaha.urouting.api.AbstractConverter;

import javax.enterprise.inject.Typed;
import javax.inject.Singleton;

@Singleton
@Typed( AbstractConverter.class )
public class IntegerConverter extends AbstractConverter<Integer> {

	@Override
	public Integer convert( String value ) throws ConversionException {
		return Integer.valueOf( value );
	}

}
