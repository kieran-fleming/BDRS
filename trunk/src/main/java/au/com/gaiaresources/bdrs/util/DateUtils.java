package au.com.gaiaresources.bdrs.util;

import java.util.Calendar;
import java.util.Date;

public final class DateUtils {
    private DateUtils() { }
    
    /**
     * The number of milliseconds in a second.
     */
    public static final long MILLISECONDS_IN_SECOND = 1000;
    /**
     * The number of seconds in a minute.
     */
    public static final long SECONDS_IN_MINUTE = 60;
    /**
     * The number of milliseconds in a minute.
     */
    public static final long MILLISECONDS_IN_MINUTE = SECONDS_IN_MINUTE * MILLISECONDS_IN_SECOND;
    /**
     * The number of minutes in an hour.
     */
    public static final long MINUTES_IN_HOUR = 60;
    /**
     * The number of seconds in an hour.
     */
    public static final long SECONDS_IN_HOUR = MINUTES_IN_HOUR * SECONDS_IN_MINUTE;
    /**
     * The number of milliseconds in a hour.
     */
    public static final long MILLISECONDS_IN_HOUR = SECONDS_IN_HOUR * MILLISECONDS_IN_SECOND;
    
    /**
     * Convert the given number of hours, minutes and seconds to milliseconds.
     * @param hours The number of hours.
     * @param minutes The number of minutes.
     * @param seconds The number of seconds.
     * @return long.
     */
    public static long convertToMilliseconds(int hours, int minutes, int seconds) {
        return (hours * MILLISECONDS_IN_HOUR) + (minutes * MILLISECONDS_IN_MINUTE) + (seconds * MILLISECONDS_IN_SECOND);
    }
    
    /**
     * Check if the given date has a time component.
     * @param d {@link Date}
     * @return {@link boolean}
     */
    public static boolean hasTimeComponent(Date d) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        return (c.get(Calendar.HOUR_OF_DAY) > 0 || c.get(Calendar.MINUTE) > 0 || c.get(Calendar.SECOND) > 0);
    }
}
