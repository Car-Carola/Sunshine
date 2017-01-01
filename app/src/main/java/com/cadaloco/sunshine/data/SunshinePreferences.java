package com.cadaloco.sunshine.data;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;

import com.cadaloco.sunshine.R;

/**
 * Created by Carol on 01-Jan-17.
 */

public final class SunshinePreferences {

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
