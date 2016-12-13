package com.cadaloco.sunshine.activities;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.cadaloco.sunshine.R;
import com.cadaloco.sunshine.adapters.FetchWeatherTask;
import com.cadaloco.sunshine.adapters.ForecastCursorAdapter;
import com.cadaloco.sunshine.data.WeatherContract;
import com.cadaloco.sunshine.utils.LogUtil;
import com.cadaloco.sunshine.utils.Utility;

public class ForecastFragment extends Fragment {

    public static final String FORECAST_FRAGMENT = "FORECAST_FRAGMENT";
    //private RecyclerView mRecyclerView;
    private ForecastCursorAdapter mForecastAdapter;
    //private RecyclerView mRecyclerView;

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

        String locationSetting = Utility.getPreferredLocation(getActivity());

        // Sort order:  Ascending, by date.
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                locationSetting, System.currentTimeMillis());

        Cursor cur = getActivity().getContentResolver().query(weatherForLocationUri,
                null, null, null, sortOrder);

        // The CursorAdapter will take data from our cursor and populate the ListView
        // However, we cannot use FLAG_AUTO_REQUERY since it is deprecated, so we will end
        // up with an empty list the first time we run.
        mForecastAdapter = new ForecastCursorAdapter(getActivity().getApplicationContext(), cur, 0);

        View rootView = inflater.inflate(R.layout.fragment_list_main, container, false);

        // Get a reference to the ListView, and attach this adapter to it.
        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(mForecastAdapter);

        return rootView;
/*        View view = inflater.inflate(R.layout.fragment_main, container, false);

        setupRecyclerView(view);

      return view;*/
    }

/*    private void setupRecyclerView(View view) {
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerview_forecast);

        mForecastAdapter = new ForecastAdapter(new ArrayList<String>());

        mRecyclerView.setAdapter(mForecastAdapter);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(
                getActivity(),
                LinearLayoutManager.VERTICAL,
                false));

        mRecyclerView.addItemDecoration(new DividerItemDecoration(
                getActivity(), LinearLayoutManager.VERTICAL));
    }*/

    //menu
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.forecast_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
/*
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
*/

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

        FetchWeatherTask weatherTask = new FetchWeatherTask(getActivity().getApplicationContext());
        String location = Utility.getPreferredLocation(getActivity());
        weatherTask.execute(location);

/*        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        String zipCode = prefs.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));

        FetchWeatherTask weatherTask = new FetchWeatherTask(getActivity(), mForecastAdapter);
        weatherTask.execute(zipCode);
    */
    }

    @Override
    public void onStart() {
        LogUtil.logMethodCalled();
        super.onStart();
        updateWeather();

    }

}