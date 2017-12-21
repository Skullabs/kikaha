package kikaha.urouting.converter;

import kikaha.urouting.api.AbstractConverter;
import kikaha.urouting.api.ConversionException;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

@Singleton
@Typed( AbstractConverter.class )
public class ZonedDateTimeConverter extends AbstractConverter<ZonedDateTime> {

	@Inject DateConverter converter;

	@Override
	public ZonedDateTime convert(String dataAsStr) throws ConversionException {
		final Date date = converter.convert( dataAsStr );
		return ZonedDateTime.ofInstant( date.toInstant(), ZoneId.systemDefault() );
	}
}
