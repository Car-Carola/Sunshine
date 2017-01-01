package com.cadaloco.sunshine;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.cadaloco.sunshine.data.WeatherContract;
import com.cadaloco.sunshine.sync.SunshineSyncTask;
import com.cadaloco.sunshine.utils.LogUtil;
import com.cadaloco.sunshine.utils.SunshineWeatherUtils;

public class ForecastFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int FORECAST_LOADER_DB = 0;
    private static final int FORECAST_LOADER_WEB = 1;

    // For the forecast view we're showing only a small subset of the stored data.
    // Specify the columns we need.
    private static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    protected static final int COL_WEATHER_ID = 0;
    public static final int COL_WEATHER_DATE = 1;
    public static final int COL_WEATHER_DESC = 2;
    public static final int COL_WEATHER_MAX_TEMP = 3;
    public static final int COL_WEATHER_MIN_TEMP = 4;
    protected static final int COL_LOCATION_SETTING = 5;
    protected static final int COL_WEATHER_CONDITION_ID = 6;
    protected static final int COL_COORD_LAT = 7;
    protected static final int COL_COORD_LONG = 8;

    private RecyclerAdapter mForecastAdapter;

    private RecyclerView mRecyclerView;

    private int mPosition = RecyclerView.NO_POSITION;

    private static final String SELECTED_KEY = "selected_position";

    private boolean mUseTodayLayout;

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        LogUtil.logMethodCalled();
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        LogUtil.logMethodCalled();

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        setupRecyclerView(rootView);


        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            // The listview probably hasn't even been populated yet.  Actually perform the
            // swapout in onLoadFinished.
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }
        return rootView;
    }

    private void setupRecyclerView(View view) {
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerview_forecast);

        mForecastAdapter = new RecyclerAdapter((RecyclerAdapter.ForecastAdapterOnClickHandler) getActivity());
        mRecyclerView.setAdapter(mForecastAdapter);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(
                getActivity(),
                LinearLayoutManager.VERTICAL,
                false));
/*
        mRecyclerView.addItemDecoration(new DividerItemDecoration(
                getActivity(), LinearLayoutManager.VERTICAL));*/

        mForecastAdapter.setUseTodayLayout(mUseTodayLayout);
    }

    //menu
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.forecast_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh: {
                updateWeather();
                return true;
            }
           default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateWeather() {
        LogUtil.logMethodCalled();

       /* FetchWeatherTask weatherTask = new FetchWeatherTask(getActivity().getApplicationContext());
        String location = Utility.getPreferredLocation(getActivity());
        weatherTask.execute(location);*/
        Loader<Cursor> cursorLoader = getLoaderManager().initLoader(FORECAST_LOADER_WEB, null, this);
        cursorLoader.forceLoad();
       // SunshineSyncTask.syncWeather(getContext());

    }

    void onLocationChanged( ) {
        LogUtil.logMethodCalled();
        updateWeather();
        getLoaderManager().restartLoader(FORECAST_LOADER_DB, null, this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        LogUtil.logMethodCalled();
        getLoaderManager().initLoader(FORECAST_LOADER_DB, null, this);
        super.onActivityCreated(savedInstanceState);
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        // When tablets rotate, the currently selected list item needs to be saved.
        // When no item is selected, mPosition will be set to Listview.INVALID_POSITION,
        // so check for that before storing.
        if (mPosition != RecyclerView.NO_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        LogUtil.logMethodCalled();

        String locationSetting = SunshineWeatherUtils.getPreferredLocation(getActivity());

        switch(id) {
            case FORECAST_LOADER_DB:

                String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
                Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                        locationSetting, System.currentTimeMillis());
                return new CursorLoader(
                        getActivity().getApplicationContext(),
                        weatherForLocationUri,
                        FORECAST_COLUMNS,
                        null,
                        null,
                        sortOrder
                );
            case FORECAST_LOADER_WEB:

                return new SunshineSyncTask<Cursor>(getActivity());

            default:
                throw new UnsupportedOperationException("Unknown loader id: " + id);
        }
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        LogUtil.logMethodCalled();

        switch(loader.getId()) {
            case FORECAST_LOADER_DB:
                mForecastAdapter.swapCursor(data);
                if (mPosition == RecyclerView.NO_POSITION)
                    mPosition = 0;
                mRecyclerView.smoothScrollToPosition(mPosition);


                break;
            case FORECAST_LOADER_WEB:
                break;
            default:

                throw new UnsupportedOperationException("Unknown loader id: " + loader.getId());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        LogUtil.logMethodCalled();

        switch(loader.getId()) {
            case FORECAST_LOADER_DB:
                mForecastAdapter.swapCursor(null);
                break;
            case FORECAST_LOADER_WEB:
                break;
            default:
                throw new UnsupportedOperationException("Unknown loader id: " + loader.getId());
        }
    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        mUseTodayLayout = useTodayLayout;
        if (mForecastAdapter != null) {
            mForecastAdapter.setUseTodayLayout(mUseTodayLayout);
        }
    }
}