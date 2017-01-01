package com.cadaloco.sunshine.sync;

import android.content.ContentValues;
import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.cadaloco.sunshine.utils.LogUtil;
import com.cadaloco.sunshine.utils.NetworkUtils;

import java.net.URL;

import static com.cadaloco.sunshine.data.SunshinePreferences.getPreferredLocation;
import static com.cadaloco.sunshine.utils.NetworkUtils.buildUrlWithLocationQuery;
import static com.cadaloco.sunshine.utils.NetworkUtils.getResponseFromHttpUrlOK;
import static com.cadaloco.sunshine.utils.OpenWeatherJsonUtils.getWeatherDataFromJson;

/**
 * Created by Carol on 01-Jan-17.
 */

public class SunshineSyncTask<Cursor> extends AsyncTaskLoader<Cursor> {

    private Context mContext;

    public SunshineSyncTask(Context context) {
        super(context);
        mContext = context;
    }

    //synchronized public static void syncWeather(Context context){
    @Override
    public Cursor loadInBackground() {
        LogUtil.logMethodCalled();

        URL url = null;

        String location = getPreferredLocation(mContext);

        String forecastJsonStr = null;

        try {

            url = buildUrlWithLocationQuery(location);

            forecastJsonStr = getResponseFromHttpUrlOK(url);

            ContentValues[] weatherValues = getWeatherDataFromJson(mContext, forecastJsonStr, location);

            NetworkUtils.updateDbWithFreshData(mContext, weatherValues);

        } catch (Exception e) {
            LogUtil.e("Error getting JSON from: " + url.toString() + " " + e.getMessage());
             /* Server probably invalid */
            e.printStackTrace();
        }
        return null;
    }

}
