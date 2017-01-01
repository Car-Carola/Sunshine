package com.cadaloco.sunshine.utils;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;

import com.cadaloco.sunshine.R;
import com.cadaloco.sunshine.data.WeatherContract;

/**
 * Created by Carol on 01-Jan-17.
 */

public final class SunshineWeatherUtils {
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

    public static String getFormattedWind(Context context, float windSpeed, float degrees) {
      int windFormat;
      if (isMetric(context)) {
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
      } else if (weatherId == 761 || weatherId == 771 || weatherId == 781) {
        return R.drawable.ic_storm;
      } else if (weatherId == 800) {
        return R.drawable.ic_clear;
      } else if (weatherId == 801) {
        return R.drawable.ic_light_clouds;
      } else if (weatherId >= 802 && weatherId <= 804) {
        return R.drawable.ic_cloudy;
      } else if (weatherId >= 900 && weatherId <= 906) {
        return R.drawable.ic_storm;
      } else if (weatherId >= 958 && weatherId <= 962) {
        return R.drawable.ic_storm;
      } else if (weatherId >= 951 && weatherId <= 957) {
        return R.drawable.ic_clear;
      }

      return R.drawable.ic_storm;

    }

    /**
     * Helper method to provide the art resource id according to the weather condition id returned
     * by the OpenWeatherMap call.
     * @param weatherId from OpenWeatherMap API response
     * @return resource id for the corresponding image. -1 if no relation is found.
     */
    public static int getArtResourceForWeatherCondition(int weatherId) {
          /*
           * Based on weather code data for Open Weather Map.
           */
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
        return R.drawable.art_snow;
      } else if (weatherId >= 701 && weatherId <= 761) {
        return R.drawable.art_fog;
      } else if (weatherId == 761 || weatherId == 771 || weatherId == 781) {
        return R.drawable.art_storm;
      } else if (weatherId == 800) {
        return R.drawable.art_clear;
      } else if (weatherId == 801) {
        return R.drawable.art_light_clouds;
      } else if (weatherId >= 802 && weatherId <= 804) {
        return R.drawable.art_clouds;
      } else if (weatherId >= 900 && weatherId <= 906) {
        return R.drawable.art_storm;
      } else if (weatherId >= 958 && weatherId <= 962) {
        return R.drawable.art_storm;
      } else if (weatherId >= 951 && weatherId <= 957) {
        return R.drawable.art_clear;
      }

      return R.drawable.art_storm;
    }

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
}
