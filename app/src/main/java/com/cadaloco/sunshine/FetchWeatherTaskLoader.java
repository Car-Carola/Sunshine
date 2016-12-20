package com.cadaloco.sunshine;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;

import com.cadaloco.sunshine.utils.LogUtil;
import com.cadaloco.sunshine.utils.Utility;

import org.json.JSONException;

import java.io.IOException;
import java.net.URL;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Carol on 20-Dec-16.
 */

public class FetchWeatherTaskLoader<Cursor> extends AsyncTaskLoader<Cursor> {

    private String mLocation;

    public FetchWeatherTaskLoader(Context context, String location) {
        super(context);
        mLocation = location;
    }

    @Override
    public Cursor loadInBackground() {
        LogUtil.logMethodCalled();

        String location;

        String forecastJsonStr = null;
        URL url = null;
        OkHttpClient client = null;

        String format = "json";
        String units = "metric";
        int numDays = 12;
        try {
            final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
            final String QUERY_PARAM = "q";
            final String FORMAT_PARAM = "mode";
            final String UNITS_PARAM = "units";
            final String DAYS_PARAM = "cnt";
            final String APPID_PARAM = "APPID";

            //location = Utility.getPreferredLocation(mContext);

            Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                    .appendQueryParameter(QUERY_PARAM, mLocation)
                    .appendQueryParameter(FORMAT_PARAM, format)
                    .appendQueryParameter(UNITS_PARAM, units)
                    .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                    .appendQueryParameter(APPID_PARAM, BuildConfig.OPEN_WEATHER_MAP_API_KEY)
                    .build();

            url = new URL(builtUri.toString());

            LogUtil.v(builtUri.toString());

            client = new OkHttpClient();
            Request request = new Request.Builder().url(url).build();
            Response response = client.newCall(request).execute();
            forecastJsonStr = response.body().string();
            LogUtil.v(forecastJsonStr);

            Utility.getWeatherDataFromJson(getContext(), forecastJsonStr, mLocation);

        } catch (IOException e) {
            e.printStackTrace();
            LogUtil.e("IOException: " + url.toString() + " " + e.getMessage());
        } catch (JSONException e) {
            LogUtil.e("Error getting JSON from: " + url.toString() + " " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }
}

