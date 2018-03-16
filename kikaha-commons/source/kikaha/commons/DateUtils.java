package kikaha.commons;

import lombok.RequiredArgsConstructor;
import lombok.val;

import java.text.ParsePosition;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.Date;
import java.util.Locale;

/**
 * Utility for parsing and generating dates
 *
 * Based on Stuart Douglas' {@code DateUtils} code.
 */
public interface DateUtils {

    String
        ASCITIME_PATTERN = "EEE MMM d HH:mm:ss yyyyy",
        OLD_COOKIE_PATTERN = "EEEE, dd-MMM-yy HH:mm:ss z",
        OLD_COOKIE_PATTERN2 = "EEEE"
    ;

    DateParser
        RFC_1123 = new DateParser( DateTimeFormatter.RFC_1123_DATE_TIME ),
        ASCITIME = new DateParser( DateTimeFormatter.ofPattern( ASCITIME_PATTERN ) ),
        OLDSCHOOL =  new DateParser( new DateTimeFormatterBuilder()
                .appendPattern( OLD_COOKIE_PATTERN )
                //.appendValue(ChronoField.DAY_OF_WEEK)
                .toFormatter( Locale.ENGLISH ) )
    ;

    /**
     * Attempts to pass a HTTP date.
     *
     * @param date The date to parse
     * @return The parsed date, or null if parsing failed
     */
    static Date parseDate(final String date) {
        final int semicolonIndex = date.indexOf(';');
        final String trimmedDate = semicolonIndex >= 0 ? date.substring(0, semicolonIndex) : date;

        LocalDateTime parsed = RFC_1123.parse( trimmedDate );
        if ( parsed != null )
            return toDate( parsed );

        parsed = OLDSCHOOL.parse( trimmedDate );
        if ( parsed != null )
            return toDate( parsed );

        parsed = ASCITIME.parse( trimmedDate );
        if ( parsed != null )
            return toDate( parsed );

        return null;
    }

    static Date toDate( LocalDateTime dateTime ) {
        return Date.from( dateTime.toInstant( ZoneOffset.UTC ) );
    }

    @RequiredArgsConstructor
    class DateParser {

        final DateTimeFormatter formatter;

        public LocalDateTime parse( String date ) {
            try {
                val pp = new ParsePosition(0);
                val parsed = formatter.parse( date, pp );
                if (pp.getIndex() == date.length())
                    return LocalDateTime.from( parsed );
            } catch ( DateTimeParseException cause ) {
            }

            return null;
        }
    }
}