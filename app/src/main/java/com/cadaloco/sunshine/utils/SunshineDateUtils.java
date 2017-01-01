package com.cadaloco.sunshine.utils;

import android.content.Context;
import android.text.format.DateUtils;
import android.text.format.Time;

import com.cadaloco.sunshine.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Created by Carol on 01-Jan-17.
 */

public final class SunshineDateUtils {
    // Format used for storing dates in the database.  ALso used for converting those strings
    // back into date objects for comparison/processing.
    public static final String DATE_FORMAT = "yyyyMMdd";
    public static final long DAY_IN_MILLIS = TimeUnit.DAYS.toMillis(1);

    /**
     * This method returns the number of milliseconds (UTC time) for today's date at midnight in
     * the local time zone. For example, if you live in California and the day is September 20th,
     * 2016 and it is 6:30 PM, it will return 1474329600000. Now, if you plug this number into an
     * Epoch time converter, you may be confused that it tells you this time stamp represents 8:00
     * PM on September 19th local time, rather than September 20th. We're concerned with the GMT
     * date here though, which is correct, stating September 20th, 2016 at midnight.
     *
     * As another example, if you are in Hong Kong and the day is September 20th, 2016 and it is
     * 6:30 PM, this method will return 1474329600000. Again, if you plug this number into an Epoch
     * time converter, you won't get midnight for your local time zone. Just keep in mind that we
     * are just looking at the GMT date here.
     *
     * This method will ALWAYS return the date at midnight (in GMT time) for the time zone you
     * are currently in. In other words, the GMT date will always represent your date.
     *
     * Since UTC / GMT time are the standard for all time zones in the world, we use it to
     * normalize our dates that are stored in the database. When we extract values from the
     * database, we adjust for the current time zone using time zone offsets.
     *
     * @return The number of milliseconds (UTC / GMT) for today's date at midnight in the local
     * time zone
     */
    public static long getNormalizedUtcDateForToday() {

        /*
         * This number represents the number of milliseconds that have elapsed since January
         * 1st, 1970 at midnight in the GMT time zone.
         */
        long utcNowMillis = System.currentTimeMillis();

        /*
         * This TimeZone represents the device's current time zone. It provides us with a means
         * of acquiring the offset for local time from a UTC time stamp.
         */
        TimeZone currentTimeZone = TimeZone.getDefault();

        /*
         * The getOffset method returns the number of milliseconds to add to UTC time to get the
         * elapsed time since the epoch for our current time zone. We pass the current UTC time
         * into this method so it can determine changes to account for daylight savings time.
         */
        long gmtOffsetMillis = currentTimeZone.getOffset(utcNowMillis);

        /*
         * UTC time is measured in milliseconds from January 1, 1970 at midnight from the GMT
         * time zone. Depending on your time zone, the time since January 1, 1970 at midnight (GMT)
         * will be greater or smaller. This variable represents the number of milliseconds since
         * January 1, 1970 (GMT) time.
         */
        long timeSinceEpochLocalTimeMillis = utcNowMillis + gmtOffsetMillis;

        /* This method simply converts milliseconds to days, disregarding any fractional days */
        long daysSinceEpochLocal = TimeUnit.MILLISECONDS.toDays(timeSinceEpochLocalTimeMillis);

        /*
         * Finally, we convert back to milliseconds. This time stamp represents today's date at
         * midnight in GMT time. We will need to account for local time zone offsets when
         * extracting this information from the database.
         */
        long normalizedUtcMidnightMillis = TimeUnit.DAYS.toMillis(daysSinceEpochLocal);

        return normalizedUtcMidnightMillis;
    }

    /**
     * Helper method to convert the database representation of the date into something to display
     * to users.  As classy and polished a user experience as "20140102" is, we can do better.
     *
     * @param context Context to use for resource localization
     * @param dateInMillis The date in milliseconds
     * @return a user-friendly representation of the date.
     */
    public static String getFriendlyDayString(Context context, long dateInMillis) {
      // The day string for forecast uses the following logic:
      // For today: "Today, June 8"
      // For tomorrow:  "Tomorrow"
      // For the next 5 days: "Wednesday" (just the day name)
      // For all days after that: "Mon Jun 8"

      Calendar calendar = Calendar.getInstance();
      int currentJulianDay = calendar.get(Calendar.DAY_OF_YEAR);
      calendar.setTimeInMillis(dateInMillis);
      int julianDay = calendar.get(Calendar.DAY_OF_YEAR);

      // If the date we're building the String for is today's date, the format
      // is "Today, June 24"
      if (julianDay == currentJulianDay) {
        String today = context.getString(R.string.today);
        int formatId = R.string.format_full_friendly_date;
        return String.format(context.getString( formatId), today, getFormattedMonthDay(context, dateInMillis));
      } else if ( julianDay < currentJulianDay + 7 ) {
        // If the input date is less than a week in the future, just return the day name.
        return getDayName(context, dateInMillis);
      } else {
        // Otherwise, use the form "Mon Jun 3"
        SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
        return shortenedDateFormat.format(dateInMillis);
      }
    }

    /**
     * Given a day, returns just the name to use for that day.
     * E.g "today", "tomorrow", "wednesday".
     *
     * @param context Context to use for resource localization
     * @param dateInMillis The date in milliseconds
     * @return
     */
    public static String getDayName(Context context, long dateInMillis) {
      // If the date is today, return the localized version of "Today" instead of the actual
      // day name.

      Calendar calendar = Calendar.getInstance();
      int currentJulianDay = calendar.get(Calendar.DAY_OF_YEAR);
      calendar.setTimeInMillis(dateInMillis);
      int julianDay = calendar.get(Calendar.DAY_OF_YEAR);

      if (julianDay == currentJulianDay) {
        return context.getString(R.string.today);
      } else if ( julianDay == currentJulianDay +1 ) {
        return context.getString(R.string.tomorrow);
      } else {
        Time time = new Time();
        time.setToNow();
        // Otherwise, the format is just the day of the week (e.g "Wednesday".
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");
        return dayFormat.format(dateInMillis);
      }
    }

    /**
     * Converts db date format to the format "Month day", e.g "June 24".
     * @param context Context to use for resource localization
     * @param dateInMillis The db formatted date string, expected to be of the form specified
     *                in Utility.DATE_FORMAT
     * @return The day in the form of a string formatted "December 6"
     */
    public static String getFormattedMonthDay(Context context, long dateInMillis ) {
      Time time = new Time();
      time.setToNow();
      SimpleDateFormat dbDateFormat = new SimpleDateFormat(DATE_FORMAT);
      SimpleDateFormat monthDayFormat = new SimpleDateFormat("MMMM dd");
      String monthDayString = monthDayFormat.format(dateInMillis);
      return monthDayString;
    }

    /**
     * This method returns the number of days since the epoch (January 01, 1970, 12:00 Midnight UTC)
     * in UTC time from the current date.
     *
     * @param utcDate A date in milliseconds in UTC time.
     *
     * @return The number of days from the epoch to the date argument.
     */
    public static long elapsedDaysSinceEpoch(long utcDate) {
      return TimeUnit.MILLISECONDS.toDays(utcDate);
    }

    /**
     * Normalizes a date (in milliseconds).
     *
     * Normalize, in our usage within Sunshine means to convert a given date in milliseconds to
     * the very beginning of the date in UTC time.
     *
     *   For example, given the time representing
     *
     *     Friday, 9/16/2016, 17:45:15 GMT-4:00 DST (1474062315000)
     *
     *   this method would return the number of milliseconds (since the epoch) that represents
     *
     *     Friday, 9/16/2016, 00:00:00 GMT (1473984000000)
     *
     * To make it easy to query for the exact date, we normalize all dates that go into
     * the database to the start of the day in UTC time. In order to normalize the date, we take
     * advantage of simple integer division, noting that any remainder is discarded when dividing
     * two integers.
     *
     *     For example, dividing 7 / 3 (when using integer division) equals 2, not 2.333 repeating
     *   as you may expect.
     *
     * @param date The date (in milliseconds) to normalize
     *
     * @return The UTC date at 12 midnight of the date
     */
    public static long normalizeDate(long date) {
      long daysSinceEpoch = elapsedDaysSinceEpoch(date);
      long millisFromEpochToTodayAtMidnightUtc = daysSinceEpoch * DateUtils.DAY_IN_MILLIS;
      return millisFromEpochToTodayAtMidnightUtc;
    }
}
