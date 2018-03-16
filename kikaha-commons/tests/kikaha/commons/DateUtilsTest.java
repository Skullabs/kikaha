package kikaha.commons;

import kikaha.commons.DateUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateUtilsTest {

    @Test
    public void testParseOldSchoolDateFormat(){
        String oldSchoolDateString = "Friday, 16-Mar-18 23:12:40 GMT";
        Date oldSchoolDate = DateUtils.parseDate(oldSchoolDateString);

        Assert.assertNotNull(oldSchoolDate);

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        calendar.set(2018, Calendar.MARCH, 16, 23, 12, 40);
        calendar.set(Calendar.MILLISECOND, 0);

        Assert.assertEquals(calendar.getTime(), oldSchoolDate);
    }

    @Test
    public void testParseFirefoxDate() {

        String firefoxHeader = "Mon, 31 Mar 2014 09:24:49 GMT";
        Date firefoxDate = DateUtils.parseDate(firefoxHeader);

        Assert.assertNotNull(firefoxDate);

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        calendar.set(2014, Calendar.MARCH, 31, 9, 24, 49);
        calendar.set(Calendar.MILLISECOND, 0);

        Assert.assertEquals(calendar.getTime(), firefoxDate);
    }

    @Test
    public void testParseChromeDate() {

        String chromeHeader = "Mon, 31 Mar 2014 09:44:00 GMT";
        Date chromeDate = DateUtils.parseDate(chromeHeader);

        Assert.assertNotNull(chromeDate);

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        calendar.set(2014, Calendar.MARCH, 31, 9, 44, 00);
        calendar.set(Calendar.MILLISECOND, 0);

        Assert.assertEquals(calendar.getTime(), chromeDate);
    }

    @Test
    public void testParseIE9Date() {

        String ie9Header = "Wed, 12 Feb 2014 04:43:29 GMT; length=142951";

        Date ie9Date = DateUtils.parseDate(ie9Header);
        Assert.assertNotNull(ie9Date);

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        calendar.set(2014, Calendar.FEBRUARY, 12, 4, 43, 29);
        calendar.set(Calendar.MILLISECOND, 0);

        Assert.assertEquals(calendar.getTime(), ie9Date);
    }

    @Test
    @Ignore("This test can fail if the machine pauses/swaps at the wrong time")
    public void testPerformance() {

        String ie9Header = "Wed, 12 Feb 2014 04:43:29 GMT; length=142951";

        long timestamp = System.currentTimeMillis();
        for (int i=0; i < 1000; i++) {
            ie9Header.replaceAll(";.*$", "");
        }
        long ts1 = System.currentTimeMillis() - timestamp;

        timestamp = System.currentTimeMillis();

        for (int i=0; i < 1000; i++) {
            int index = ie9Header.indexOf(';');
            final String trimmedDate = index >=0 ? ie9Header.substring(0, index) : ie9Header;
        }

        long ts2 = System.currentTimeMillis() - timestamp;

        Assert.assertTrue(ts2 < ts1);
    }
}
