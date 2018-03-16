package kikaha.urouting.api.converter;

import kikaha.urouting.api.AbstractConverter;

import javax.enterprise.inject.Typed;
import javax.inject.Singleton;

@Singleton
@Typed( AbstractConverter.class )
public class StringConverter extends AbstractConverter<String> {

	@Override
	public String convert( String value ) throws ConversionException {
		return value;
	}
}
