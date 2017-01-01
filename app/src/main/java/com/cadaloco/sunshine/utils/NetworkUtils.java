package com.cadaloco.sunshine.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.cadaloco.sunshine.BuildConfig;
import com.cadaloco.sunshine.data.WeatherContract;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Carol on 01-Jan-17.
 */

public final class NetworkUtils {

    private static final String format = "json";
    private static final String units = "metric";

    private static final int numDays = 14;

    private static final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
    private static final String QUERY_PARAM = "q";
    private static final String FORMAT_PARAM = "mode";
    private static final String UNITS_PARAM = "units";
    private static final String DAYS_PARAM = "cnt";
    private static final String APPID_PARAM = "APPID";

    /**
     * Builds the URL used to talk to the weather server using a location. This location is based
     * on the query capabilities of the weather provider that we are using.
     *
     * @param locationQuery The location that will be queried for.
     * @return The URL to use to query the weather server.
     */
    public static URL buildUrlWithLocationQuery(String locationQuery) {
        Uri weatherQueryUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                .appendQueryParameter(QUERY_PARAM, locationQuery)
                .appendQueryParameter(FORMAT_PARAM, format)
                .appendQueryParameter(UNITS_PARAM, units)
                .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                .appendQueryParameter(APPID_PARAM, BuildConfig.OPEN_WEATHER_MAP_API_KEY)
                .build();

        try {
            URL weatherQueryUrl = new URL(weatherQueryUri.toString());
            LogUtil.v("URL: " + weatherQueryUrl);
            return weatherQueryUrl;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * This method returns the entire result from the HTTP response.
     *
     * @param url The URL to fetch the HTTP response from.
     * @return The contents of the HTTP response, null if no response
     * @throws IOException Related to network and stream reading
     */
    public static String getResponseFromHttpUrlScanner(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            String response = null;
            if (hasInput) {
                response = scanner.next();
            }
            scanner.close();
            LogUtil.v(response);

            return response;
        } finally {
            urlConnection.disconnect();
        }
    }

    @NonNull
    public static String getResponseFromHttpUrlOK(URL url) throws IOException {
        OkHttpClient client;
        String responseStr;
        client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        Response response = client.newCall(request).execute();
        responseStr = response.body().string();

        LogUtil.v(responseStr);

        return responseStr;
    }

    public static void updateDbWithFreshData(Context context, ContentValues[] weatherValues) {
        if (weatherValues != null && weatherValues.length != 0) {
           int inserted, deleted = 0;
            ContentResolver sunshineContentResolver = context.getContentResolver();

            deleted = sunshineContentResolver.delete(
                    WeatherContract.WeatherEntry.CONTENT_URI, null, null);

            inserted = sunshineContentResolver.bulkInsert(
                    WeatherContract.WeatherEntry.CONTENT_URI, weatherValues);
            LogUtil.d("FetchWeatherTask Complete. " + inserted + " Inserted, " + deleted + "deleted");
        }
    }
}
