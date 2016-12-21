/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cadaloco.sunshine.utils;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.format.Time;

import com.cadaloco.sunshine.DetailFragment;
import com.cadaloco.sunshine.ForecastFragment;
import com.cadaloco.sunshine.R;
import com.cadaloco.sunshine.data.WeatherContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;


public class Utility {

  public static String getPreferredLocation(Context context) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    return prefs.getString(context.getString(R.string.pref_location_key),
            context.getString(R.string.pref_location_default));
  }

  public static boolean isMetric(Context context) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    return prefs.getString(context.getString(R.string.pref_temp_key),
            context.getString(R.string.pref_temp_metric))
            .equals(context.getString(R.string.pref_temp_metric));
  }

/*  public static String formatTemperature(double temperature, boolean isMetric) {
    double temp;
    if ( !isMetric ) {
      temp = 9*temperature/5+32;
    } else {
      temp = temperature;
    }
    return String.format("%.0f", temp);
  }*/

  public static String formatTemperature(Context context, Cursor cursor, int weatherCol, boolean isMetric) {
    double temperature = cursor.getDouble(weatherCol);
    double temp;
    if ( !isMetric ) {
      temp = 9*temperature/5+32;
    } else {
      temp = temperature;
    }

    //return String.format("%.0f", temp);
    return context.getString(R.string.format_temperature, temp);

  }

  public static String formatDate(long dateInMillis) {
    Date date = new Date(dateInMillis);
    return DateFormat.getDateInstance().format(date);
  }

  public static String generateSimpleText(Context context, Cursor data, boolean isMetric) {
    String dateString = Utility.formatDate(
            data.getLong(DetailFragment.COL_WEATHER_DATE));

    String weatherDescription =
            data.getString(DetailFragment.COL_WEATHER_DESC);

    String high = Utility.formatTemperature(context, data, ForecastFragment.COL_WEATHER_MAX_TEMP,isMetric);
    String low = Utility.formatTemperature(context, data, ForecastFragment.COL_WEATHER_MIN_TEMP,isMetric);


    return String.format("%s - %s - %s/%s", dateString, weatherDescription, high, low);
  }

  /**
   * Take the String representing the complete forecast in JSON Format and
   * pull out the data we need to construct the Strings needed for the wireframes.
   * <p>
   * Fortunately parsing is easy:  constructor takes the JSON string and converts it
   * into an Object hierarchy for us.
   */
  public static void getWeatherDataFromJson(Context context, String forecastJsonStr, String locationSetting) {
// Now we have a String representing the complete forecast in JSON Format.
    // Fortunately parsing is easy:  constructor takes the JSON string and converts it
    // into an Object hierarchy for us.

    // These are the names of the JSON objects that need to be extracted.

    // Location information
    final String OWM_CITY = "city";
    final String OWM_CITY_NAME = "name";
    final String OWM_COORD = "coord";

    // Location coordinate
    final String OWM_LATITUDE = "lat";
    final String OWM_LONGITUDE = "lon";

    // Weather information.  Each day's forecast info is an element of the "list" array.
    final String OWM_LIST = "list";

    final String OWM_PRESSURE = "pressure";
    final String OWM_HUMIDITY = "humidity";
    final String OWM_WINDSPEED = "speed";
    final String OWM_WIND_DIRECTION = "deg";

    // All temperatures are children of the "temp" object.
    final String OWM_TEMPERATURE = "temp";
    final String OWM_MAX = "max";
    final String OWM_MIN = "min";

    final String OWM_WEATHER = "weather";
    final String OWM_DESCRIPTION = "main";
    final String OWM_WEATHER_ID = "id";

    try {
      JSONObject forecastJson = new JSONObject(forecastJsonStr);
      JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

      JSONObject cityJson = forecastJson.getJSONObject(OWM_CITY);
      String cityName = cityJson.getString(OWM_CITY_NAME);

      JSONObject cityCoord = cityJson.getJSONObject(OWM_COORD);
      double cityLatitude = cityCoord.getDouble(OWM_LATITUDE);
      double cityLongitude = cityCoord.getDouble(OWM_LONGITUDE);

      long locationId = addLocation(context, locationSetting, cityName, cityLatitude, cityLongitude);

      // Insert the new weather information into the database
      Vector<ContentValues> cVVector = new Vector<ContentValues>(weatherArray.length());

      // OWM returns daily forecasts based upon the local time of the city that is being
      // asked for, which means that we need to know the GMT offset to translate this data
      // properly.

      // Since this data is also sent in-order and the first day is always the
      // current day, we're going to take advantage of that to get a nice
      // normalized UTC date for all of our weather.

      Time dayTime = new Time();
      dayTime.setToNow();

      // we start at the day returned by local time. Otherwise this is a mess.
      int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

      // now we work exclusively in UTC
      dayTime = new Time();

      for (int i = 0; i < weatherArray.length(); i++) {
        // These are the values that will be collected.
        long dateTime;
        double pressure;
        int humidity;
        double windSpeed;
        double windDirection;

        double high;
        double low;

        String description;
        int weatherId;

        // Get the JSON object representing the day
        JSONObject dayForecast = weatherArray.getJSONObject(i);

        // Cheating to convert this to UTC time, which is what we want anyhow
        dateTime = dayTime.setJulianDay(julianStartDay + i);

        pressure = dayForecast.getDouble(OWM_PRESSURE);
        humidity = dayForecast.getInt(OWM_HUMIDITY);
        windSpeed = dayForecast.getDouble(OWM_WINDSPEED);
        windDirection = dayForecast.getDouble(OWM_WIND_DIRECTION);

        // Description is in a child array called "weather", which is 1 element long.
        // That element also contains a weather code.
        JSONObject weatherObject =
                dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
        description = weatherObject.getString(OWM_DESCRIPTION);
        weatherId = weatherObject.getInt(OWM_WEATHER_ID);

        // Temperatures are in a child object called "temp".  Try not to name variables
        // "temp" when working with temperature.  It confuses everybody.
        JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
        high = temperatureObject.getDouble(OWM_MAX);
        low = temperatureObject.getDouble(OWM_MIN);

        ContentValues weatherValues = new ContentValues();

        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_LOC_KEY, locationId);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DATE, dateTime);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_HUMIDITY, humidity);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_PRESSURE, pressure);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED, windSpeed);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DEGREES, windDirection);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP, high);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP, low);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC, description);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID, weatherId);

        cVVector.add(weatherValues);
      }

      int inserted = 0;
      // add to database
      if (cVVector.size() > 0) {
        ContentValues[] cvArray = new ContentValues[cVVector.size()];
        cVVector.toArray(cvArray);
        inserted = context.getContentResolver().bulkInsert(WeatherContract.WeatherEntry.CONTENT_URI, cvArray);
      }

      LogUtil.d("FetchWeatherTask Complete. " + inserted + " Inserted");

    } catch (JSONException e) {
      LogUtil.e(e.getMessage());
      e.printStackTrace();
    }
  }
  /**
   * Helper method to handle insertion of a new location in the weather database.
   *
   * @param locationSetting The location string used to request updates from the server.
   * @param cityName A human-readable city name, e.g "Mountain View"
   * @param lat the latitude of the city
   * @param lon the longitude of the city
   * @return the row ID of the added location.
   */
  public static long addLocation(Context context, String locationSetting, String cityName, double lat, double lon) {

    long locationId;

    Cursor locationCursor = context.getContentResolver().query(
            WeatherContract.LocationEntry.CONTENT_URI,
            new String[]{WeatherContract.LocationEntry._ID},
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ?",
            new String[]{locationSetting},
            null);
    // Otherwise, insert it using the content resolver and the base URI

    if (locationCursor.moveToFirst()) {
      int locationIdIndex = locationCursor.getColumnIndex(WeatherContract.LocationEntry._ID);
      locationId = locationCursor.getLong(locationIdIndex);
    }
    else {
      ContentValues locationValues = new ContentValues();

      locationValues.put(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING, locationSetting);
      locationValues.put(WeatherContract.LocationEntry.COLUMN_CITY_NAME, cityName);
      locationValues.put(WeatherContract.LocationEntry.COLUMN_COORD_LAT, lat);
      locationValues.put(WeatherContract.LocationEntry.COLUMN_COORD_LONG, lon);

      Uri insertedUri = context.getContentResolver().insert(WeatherContract.LocationEntry.CONTENT_URI, locationValues);
      locationId = ContentUris.parseId(insertedUri);
    }

    locationCursor.close();

    return locationId;
  }

  // Format used for storing dates in the database.  ALso used for converting those strings
  // back into date objects for comparison/processing.
  public static final String DATE_FORMAT = "yyyyMMdd";

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
    SimpleDateFormat dbDateFormat = new SimpleDateFormat(Utility.DATE_FORMAT);
    SimpleDateFormat monthDayFormat = new SimpleDateFormat("MMMM dd");
    String monthDayString = monthDayFormat.format(dateInMillis);
    return monthDayString;
  }

  public static String getFormattedWind(Context context, float windSpeed, float degrees) {
    int windFormat;
    if (Utility.isMetric(context)) {
      windFormat = R.string.format_wind_kmh;
    } else {
      windFormat = R.string.format_wind_mph;
      windSpeed = .621371192237334f * windSpeed;
    }

    // From wind direction in degrees, determine compass direction as a string (e.g NW)
    // You know what's fun, writing really long if/else statements with tons of possible
    // conditions.  Seriously, try it!
    String direction = "Unknown";
    if (degrees >= 337.5 || degrees < 22.5) {
      direction = "N";
    } else if (degrees >= 22.5 && degrees < 67.5) {
      direction = "NE";
    } else if (degrees >= 67.5 && degrees < 112.5) {
      direction = "E";
    } else if (degrees >= 112.5 && degrees < 157.5) {
      direction = "SE";
    } else if (degrees >= 157.5 && degrees < 202.5) {
      direction = "S";
    } else if (degrees >= 202.5 && degrees < 247.5) {
      direction = "SW";
    } else if (degrees >= 247.5 && degrees < 292.5) {
      direction = "W";
    } else if (degrees >= 292.5 || degrees < 22.5) {
      direction = "NW";
    }
    return String.format(context.getString(windFormat), windSpeed, direction);
  }

  /**
   * Helper method to provide the icon resource id according to the weather condition id returned
   * by the OpenWeatherMap call.
   * @param weatherId from OpenWeatherMap API response
   * @return resource id for the corresponding icon. -1 if no relation is found.
   */
  public static int getIconResourceForWeatherCondition(int weatherId) {
    // Based on weather code data found at:
    // http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
    if (weatherId >= 200 && weatherId <= 232) {
      return R.drawable.ic_storm;
    } else if (weatherId >= 300 && weatherId <= 321) {
      return R.drawable.ic_light_rain;
    } else if (weatherId >= 500 && weatherId <= 504) {
      return R.drawable.ic_rain;
    } else if (weatherId == 511) {
      return R.drawable.ic_snow;
    } else if (weatherId >= 520 && weatherId <= 531) {
      return R.drawable.ic_rain;
    } else if (weatherId >= 600 && weatherId <= 622) {
      return R.drawable.ic_snow;
    } else if (weatherId >= 701 && weatherId <= 761) {
      return R.drawable.ic_fog;
    } else if (weatherId == 761 || weatherId == 781) {
      return R.drawable.ic_storm;
    } else if (weatherId == 800) {
      return R.drawable.ic_clear;
    } else if (weatherId == 801) {
      return R.drawable.ic_light_clouds;
    } else if (weatherId >= 802 && weatherId <= 804) {
      return R.drawable.ic_cloudy;
    }
    return -1;
  }

  /**
   * Helper method to provide the art resource id according to the weather condition id returned
   * by the OpenWeatherMap call.
   * @param weatherId from OpenWeatherMap API response
   * @return resource id for the corresponding image. -1 if no relation is found.
   */
  public static int getArtResourceForWeatherCondition(int weatherId) {
    // Based on weather code data found at:
    // http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
    if (weatherId >= 200 && weatherId <= 232) {
      return R.drawable.art_storm;
    } else if (weatherId >= 300 && weatherId <= 321) {
      return R.drawable.art_light_rain;
    } else if (weatherId >= 500 && weatherId <= 504) {
      return R.drawable.art_rain;
    } else if (weatherId == 511) {
      return R.drawable.art_snow;
    } else if (weatherId >= 520 && weatherId <= 531) {
      return R.drawable.art_rain;
    } else if (weatherId >= 600 && weatherId <= 622) {
      return R.drawable.art_rain;
    } else if (weatherId >= 701 && weatherId <= 761) {
      return R.drawable.art_fog;
    } else if (weatherId == 761 || weatherId == 781) {
      return R.drawable.art_storm;
    } else if (weatherId == 800) {
      return R.drawable.art_clear;
    } else if (weatherId == 801) {
      return R.drawable.art_light_clouds;
    } else if (weatherId >= 802 && weatherId <= 804) {
      return R.drawable.art_clouds;
    }
    return -1;
  }
}