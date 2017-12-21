package kikaha.urouting.converter;

import kikaha.config.Config;
import kikaha.urouting.api.AbstractConverter;
import kikaha.urouting.api.ConversionException;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Singleton
@Typed( AbstractConverter.class )
public class DateConverter extends AbstractConverter<Date> {

	@Inject
	Config config;

	String dateFormat;

	@PostConstruct
	public void loadConfig(){
		dateFormat = config.getString("server.urouting.date-format", "yyyy-MM-dd HH:mm:ss");
	}

	@Override
	public Date convert( String value ) throws ConversionException {
		Date convertedDate = tryToConvertFromLong( value );
		if ( convertedDate == null )
			convertedDate = tryToConvertFromInternationDateFormat( value );
		if ( convertedDate == null )
			throwCantConvertValueToDate( value );
		return convertedDate;
	}

	private void throwCantConvertValueToDate( String value ) throws ConversionException {
		throw new ConversionException( String.format( "Can't convert '%s' to java.util.Date.", value ) );
	}

	private Date tryToConvertFromLong( String value ) {
		try {
			final long parsedLong = Long.parseLong( value );
			return new Date( parsedLong );
		} catch ( final NumberFormatException e ) {
			return null;
		}
	}

	private Date tryToConvertFromInternationDateFormat( String value ) {
		try {
			return new SimpleDateFormat( dateFormat ).parse( value );
		} catch ( final ParseException e ) {
			return null;
		}
	}
}
