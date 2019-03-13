package kikaha.urouting.converter;

import kikaha.core.util.Lang;
import kikaha.urouting.api.AbstractConverter;

import javax.enterprise.inject.Typed;
import javax.inject.Singleton;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Singleton
@Typed( AbstractConverter.class )
public class ZonedDateTimeConverter extends AbstractConverter<ZonedDateTime> {

	@Override
	public ZonedDateTime convert(String dateAsString) {
		if (Lang.isUndefined(dateAsString) )
			return null;
		return ZonedDateTime.parse( dateAsString, DateTimeFormatter.ISO_ZONED_DATE_TIME );
	}

}
