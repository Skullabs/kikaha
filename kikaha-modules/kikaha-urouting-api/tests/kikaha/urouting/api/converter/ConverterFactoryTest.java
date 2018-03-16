package kikaha.urouting.api.converter;

import kikaha.core.cdi.DefaultCDI;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ConverterFactoryTest {

	@Inject ConverterFactory factory;

	@Before
    public void injectDependencies(){
        DefaultCDI.newInstance().injectOn(this);
    }

	@Test
	public void grantThatCouldConvertSomeDataFromString() throws ConversionException {
		assertThat( convertTo( "1.6", Double.class ), is( 1.6D ) );
		assertThat( convertTo( "true", Boolean.class ), is( true ) );
	}

	@Test
	public void grantThatCouldConvertStringFromPrimitives() throws ConversionException {
		assertThat( convertTo( "1.6", Double.TYPE ), is( 1.6D ) );
		assertThat( convertTo( "true", Boolean.TYPE ), is( true ) );
	}

	public <T> T convertTo( String value, Class<T> targetClass ) throws ConversionException {
		return factory.decode( value, targetClass );
	}
}
