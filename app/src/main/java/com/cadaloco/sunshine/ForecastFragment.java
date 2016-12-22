package com.cadaloco.sunshine;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.cadaloco.sunshine.data.WeatherContract;
import com.cadaloco.sunshine.utils.LogUtil;
import com.cadaloco.sunshine.utils.Utility;

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
    //private ForecastAdapter mForecastAdapter;

    private RecyclerView mRecyclerView;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        void onItemSelected(Uri dateUri);
    }

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

        return rootView;
    }

    private void setupRecyclerView(View view) {
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerview_forecast);

        //mForecastAdapter = new ForecastRecyclerViewAdapter(getActivity());

        //mForecastAdapter = new ForecastAdapter(getActivity());
        mForecastAdapter = new RecyclerAdapter((RecyclerAdapter.ForecastAdapterOnClickHandler) getActivity());
        mRecyclerView.setAdapter(mForecastAdapter);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(
                getActivity(),
                LinearLayoutManager.VERTICAL,
                false));

        mRecyclerView.addItemDecoration(new DividerItemDecoration(
                getActivity(), LinearLayoutManager.VERTICAL));
    }

    //menu
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.forecast_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //layout manager
            case R.id.vertical_lm:
                mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
                return true;
            case R.id.horizontal_lm:
                mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
                return true;
            case R.id.grid_lm:
                mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
                return true;
            case R.id.staggered_lm:
                mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL));
                return true;

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
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        LogUtil.logMethodCalled();

        String locationSetting = Utility.getPreferredLocation(getActivity());

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
/*
                return new AsyncTaskLoader<Cursor>(getActivity()) {

                  //  Cursor curser;
                    @Override
                    public Cursor loadInBackground() {
                        LogUtil.logMethodCalled();

                        String location ;

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

                            location = Utility.getPreferredLocation(getActivity());

                            Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                                    .appendQueryParameter(QUERY_PARAM, location)
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

                            Utility.getWeatherDataFromJson(getContext(),forecastJsonStr, location);

                        } catch (IOException e) {
                            e.printStackTrace();
                            LogUtil.e("IOException: " + url.toString() + " " + e.getMessage());
                        } catch (JSONException e) {
                            LogUtil.e("Error getting JSON from: " + url.toString() + " " + e.getMessage());
                            e.printStackTrace();
                        }

                        return null;
                    }

                };
*/
                return new FetchWeatherTaskLoader<Cursor>(getActivity(), locationSetting);

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

            default:
                throw new UnsupportedOperationException("Unknown loader id: " + loader.getId());
        }
    }
}