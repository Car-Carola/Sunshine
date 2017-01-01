package com.cadaloco.sunshine;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cadaloco.sunshine.data.WeatherContract;
import com.cadaloco.sunshine.data.WeatherContract.WeatherEntry;
import com.cadaloco.sunshine.utils.LogUtil;
import com.cadaloco.sunshine.utils.SunshineDateUtils;
import com.cadaloco.sunshine.utils.SunshineWeatherUtils;

import static android.R.attr.description;

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    static final String DETAIL_URI = "URI";

    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";
    private static final int DETAIL_LOADER = 0;

    private ShareActionProvider mShareActionProvider;
    private String mForecast;
    private Uri mUri;

    private static final String[] DETAIL_COLUMNS = {
            WeatherEntry.TABLE_NAME + "." + WeatherEntry._ID,
            WeatherEntry.COLUMN_DATE,
            WeatherEntry.COLUMN_SHORT_DESC,
            WeatherEntry.COLUMN_MAX_TEMP,
            WeatherEntry.COLUMN_MIN_TEMP,
            WeatherEntry.COLUMN_HUMIDITY,
            WeatherEntry.COLUMN_PRESSURE,
            WeatherEntry.COLUMN_WIND_SPEED,
            WeatherEntry.COLUMN_DEGREES,
            WeatherEntry.COLUMN_WEATHER_ID,
            // This works because the WeatherProvider returns location data joined with
            // weather data, even though they're stored in two different tables.
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING
    };

    // These indices are tied to DETAIL_COLUMNS.  If DETAIL_COLUMNS changes, these
    // must change.
    public static final int COL_WEATHER_ID = 0;
    public static final int COL_WEATHER_DATE = 1;
    public static final int COL_WEATHER_DESC = 2;
    public static final int COL_WEATHER_MAX_TEMP = 3;
    public static final int COL_WEATHER_MIN_TEMP = 4;
    public static final int COL_WEATHER_HUMIDITY = 5;
    public static final int COL_WEATHER_PRESSURE = 6;
    public static final int COL_WEATHER_WIND_SPEED = 7;
    public static final int COL_WEATHER_DEGREES = 8;
    public static final int COL_WEATHER_CONDITION_ID = 9;

    private ImageView mIconView;
    private TextView mFriendlyDateView;
    private TextView mDateView;
    private TextView mDescriptionView;
    private TextView mHighTempView;
    private TextView mLowTempView;
    private TextView mHumidityView;
    private TextView mWindView;
    private TextView mPressureView;

/*
    private Compass mCompass;
*/


    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LogUtil.logMethodCalled();

        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DetailFragment.DETAIL_URI);
        }
        View view = inflater.inflate(R.layout.fragment_detail, container, false);

        mIconView = (ImageView) view.findViewById(R.id.detail_icon);
        mDateView = (TextView) view.findViewById(R.id.detail_date_textview);
        mFriendlyDateView = (TextView) view.findViewById(R.id.detail_day_textview);
        mDescriptionView = (TextView) view.findViewById(R.id.detail_forecast_textview);
        mHighTempView = (TextView) view.findViewById(R.id.detail_high_textview);
        mLowTempView = (TextView) view.findViewById(R.id.detail_low_textview);
        mHumidityView = (TextView) view.findViewById(R.id.detail_humidity_textview);
        mWindView = (TextView) view.findViewById(R.id.detail_wind_textview);
        mPressureView = (TextView) view.findViewById(R.id.detail_pressure_textview);

/*
        mCompass = (Compass)view.findViewById(R.id.mycompass);
*/

        return view;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.detail_fragment, menu);

        MenuItem item = menu.findItem(R.id.action_share);

        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        if(null != mForecast)
            mShareActionProvider.setShareIntent(createShareForecastIntent());
    }

    private Intent createShareForecastIntent() {



        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        intent.putExtra(Intent.EXTRA_TEXT, mForecast + FORECAST_SHARE_HASHTAG);
        intent.setType("text/plain");
        return intent;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        LogUtil.logMethodCalled();
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    public void onLocationChanged(String newLocation) {
// replace the uri, since the location has changed
        Uri uri = mUri;
        if (null != uri) {
            long date = WeatherContract.WeatherEntry.getDateFromUri(uri);
            Uri updatedUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(newLocation, date);
            mUri = updatedUri;
            getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        LogUtil.logMethodCalled();

        if ( null != mUri ) {

            switch (id) {
                case DETAIL_LOADER:
                    // Now create and return a CursorLoader that will take care of
                    // creating a Cursor for the data being displayed.
                    return new CursorLoader(
                            getActivity(),
                            mUri,
                            DETAIL_COLUMNS,
                            null,
                            null,
                            null
                    );
                default:
                    throw new UnsupportedOperationException("Unknown loader id: " + id);
            }
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        LogUtil.logMethodCalled();

        if (!data.moveToFirst()) {
            return;
        }

        switch(loader.getId()) {
            case DETAIL_LOADER:

                boolean isMetric = SunshineWeatherUtils.isMetric(getContext());

                int weatherId = data.getInt(COL_WEATHER_CONDITION_ID);
                mIconView.setImageResource(SunshineWeatherUtils.getArtResourceForWeatherCondition(weatherId));

                mDateView.setText(data.getString(COL_WEATHER_DATE));

                // Read date from cursor and update views for day of week and date
                long date = data.getLong(COL_WEATHER_DATE);
                String friendlyDateText = SunshineDateUtils.getDayName(getActivity(), date);
                String dateText = SunshineDateUtils.getFormattedMonthDay(getActivity(), date);
                mFriendlyDateView.setText(friendlyDateText);
                mDateView.setText(dateText);

                String descriptionText = data.getString(ForecastFragment.COL_WEATHER_DESC);
                mDescriptionView.setText(descriptionText);
                mIconView.setContentDescription(descriptionText);

                String highTemp = SunshineWeatherUtils.formatTemperature(getActivity(),data, COL_WEATHER_MAX_TEMP, isMetric);
                mHighTempView.setText(highTemp);

                String lowTemp = SunshineWeatherUtils.formatTemperature(getActivity(),data, COL_WEATHER_MIN_TEMP, isMetric);
                mLowTempView.setText(lowTemp);

                float humidity = data.getFloat(COL_WEATHER_HUMIDITY);
                mHumidityView.setText(getActivity().getString(R.string.format_humidity, humidity));

                float wind = data.getFloat(COL_WEATHER_WIND_SPEED);
                mHumidityView.setText(getActivity().getString(R.string.format_humidity, humidity));

                // Read wind speed and direction from cursor and update view
                float windSpeedStr = data.getFloat(COL_WEATHER_WIND_SPEED);
                float windDirStr = data.getFloat(COL_WEATHER_DEGREES);
                mWindView.setText(SunshineWeatherUtils.getFormattedWind(getActivity(), windSpeedStr, windDirStr));

/*
                mCompass.update(windDirStr);
*/

                float pressure = data.getFloat(COL_WEATHER_PRESSURE);
                mPressureView.setText(getActivity().getString(R.string.format_pressure, pressure));

                // We still need this for the share intent
                mForecast = String.format("%s - %s - %s/%s", dateText, description, highTemp, lowTemp);

                // If onCreateOptionsMenu has already happened, we need to update the share intent now.
                if (mShareActionProvider != null) {
                    mShareActionProvider.setShareIntent(createShareForecastIntent());
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown loader id: " + loader.getId());
        }
    }

     @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        LogUtil.logMethodCalled();
         switch(loader.getId()) {
             case DETAIL_LOADER:
                 //loader = null;
                 break;

             default:
                 throw new UnsupportedOperationException("Unknown loader id: " + loader.getId());
         }

  }
}
