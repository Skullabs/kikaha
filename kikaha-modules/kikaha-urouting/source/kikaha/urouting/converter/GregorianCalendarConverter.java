package kikaha.urouting.converter;

import kikaha.urouting.api.AbstractConverter;
import kikaha.urouting.api.ConversionException;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Date;
import java.util.GregorianCalendar;

@Singleton
@Typed( AbstractConverter.class )
public class GregorianCalendarConverter extends AbstractConverter<GregorianCalendar> {

	@Inject DateConverter dateConverter;

	@Override
	public GregorianCalendar convert( final String value ) throws ConversionException {
		final Date date = dateConverter.convert( value );
		final GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime( date );
		return calendar;
	}
}
