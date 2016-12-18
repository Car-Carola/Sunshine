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

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;

import com.cadaloco.sunshine.DetailFragment;
import com.cadaloco.sunshine.R;

import java.text.DateFormat;
import java.util.Date;


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

  public static String formatTemperature(double temperature, boolean isMetric) {
    double temp;
    if ( !isMetric ) {
      temp = 9*temperature/5+32;
    } else {
      temp = temperature;
    }
    return String.format("%.0f", temp);
  }

  public static String formatDate(long dateInMillis) {
    Date date = new Date(dateInMillis);
    return DateFormat.getDateInstance().format(date);
  }

  public static String generateSimpleText(Cursor data, boolean isMetric) {
    String dateString = Utility.formatDate(
            data.getLong(DetailFragment.COL_WEATHER_DATE));

    String weatherDescription =
            data.getString(DetailFragment.COL_WEATHER_DESC);

    String high = Utility.formatTemperature(
            data.getDouble(DetailFragment.COL_WEATHER_MAX_TEMP), isMetric);

    String low = Utility.formatTemperature(
            data.getDouble(DetailFragment.COL_WEATHER_MIN_TEMP), isMetric);

    return String.format("%s - %s - %s/%s", dateString, weatherDescription, high, low);
  }
}