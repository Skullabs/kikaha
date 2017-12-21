package kikaha.urouting.converter;

import kikaha.urouting.api.AbstractConverter;
import kikaha.urouting.api.ConversionException;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Singleton
@Typed( AbstractConverter.class )
public class LocalDateTimeConverter extends AbstractConverter<LocalDateTime> {

	@Inject DateConverter converter;

	@Override
	public LocalDateTime convert(String dataAsStr) throws ConversionException {
		final Date date = converter.convert( dataAsStr );
		return LocalDateTime.ofInstant( date.toInstant(), ZoneId.systemDefault() );
	}
}
