package com.cadaloco.sunshine;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.cadaloco.sunshine.data.WeatherContract;
import com.cadaloco.sunshine.data.WeatherContract.WeatherEntry;
import com.cadaloco.sunshine.utils.LogUtil;
import com.cadaloco.sunshine.utils.Utility;

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";
    private static final int DETAIL_LOADER = 0;

    private ShareActionProvider mShareActionProvider;
    private String mForecast;

    private static final String[] FORECAST_COLUMNS = {
            WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherEntry.COLUMN_DATE,
            WeatherEntry.COLUMN_SHORT_DESC,
            WeatherEntry.COLUMN_MAX_TEMP,
            WeatherEntry.COLUMN_MIN_TEMP,
    };

    // these constants correspond to the projection defined above, and must change if the
    // projection changes
    private static final int COL_WEATHER_ID = 0;
    public static final int COL_WEATHER_DATE = 1;
    public static final int COL_WEATHER_DESC = 2;
    public static final int COL_WEATHER_MAX_TEMP = 3;
    public static final int COL_WEATHER_MIN_TEMP = 4;
    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LogUtil.logMethodCalled();

        View view = inflater.inflate(R.layout.fragment_detail, container, false);

        extractToolbar(view);

/*        Intent intent = getActivity().getIntent();

*//*        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
            mForecast = intent.getStringExtra(Intent.EXTRA_TEXT);
            ((TextView) view.findViewById(R.id.tv_fd_forecast)).setText(mForecast);
        }*//*
        if (intent != null) {
            mForecast = intent.getDataString();
        }

        if (null != mForecast) {
            ((TextView) view.findViewById(R.id.tv_fd_forecast)).setText(mForecast);
        }*/

        return view;
    }

    private void extractToolbar(View view) {
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.tb_fd);
        if (toolbar != null) {
            toolbar.setTitle(R.string.app_name);
            toolbar.setNavigationIcon(R.mipmap.ic_launcher);
            //toolbar.setLogo(R.mipmap.ic_launcher);
            ((AppCompatActivity)getActivity()).setSupportActionBar( toolbar);
        }
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

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        LogUtil.logMethodCalled();

        Intent intent = getActivity().getIntent();
        if (intent == null) {
            return null;
        }

        switch(id) {
            case DETAIL_LOADER:
                // Now create and return a CursorLoader that will take care of
                // creating a Cursor for the data being displayed.
                return new CursorLoader(
                        getActivity(),
                        intent.getData(),
                        FORECAST_COLUMNS,
                        null,
                        null,
                        null
                );
            default:
                throw new UnsupportedOperationException("Unknown loader id: " + id);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        LogUtil.logMethodCalled();

        if (!data.moveToFirst()) {
            return;
        }

        switch(loader.getId()) {
            case DETAIL_LOADER:


                mForecast = Utility.generateSimpleText(data, Utility.isMetric(getActivity()));

                TextView detailTextView = (TextView)getView().findViewById(R.id.tv_fd_forecast);
                detailTextView.setText(mForecast);
                Toast.makeText(getContext(), mForecast,Toast.LENGTH_LONG).show();

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

  }
}
