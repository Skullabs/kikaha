package kikaha.urouting.api.converter;

import kikaha.urouting.api.AbstractConverter;

import javax.enterprise.inject.Typed;
import javax.inject.Singleton;
import java.math.BigInteger;

@Singleton
@Typed(  AbstractConverter.class )
public class BigIntegerConverter extends AbstractConverter<BigInteger> {

	@Override
	public BigInteger convert( String value ) throws ConversionException {
		return new BigInteger( value );
	}

}
