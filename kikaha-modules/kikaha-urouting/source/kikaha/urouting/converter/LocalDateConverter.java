package kikaha.urouting.converter;

import kikaha.core.util.Lang;
import kikaha.urouting.api.AbstractConverter;

import javax.enterprise.inject.Typed;
import javax.inject.Singleton;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Singleton
@Typed( AbstractConverter.class )
public class LocalDateConverter extends AbstractConverter<LocalDate> {

	@Override
	public LocalDate convert(String dateAsString) {
		if (Lang.isUndefined(dateAsString) )
			return null;
		return  LocalDate.parse( dateAsString, DateTimeFormatter.ISO_LOCAL_DATE );
	}

}
