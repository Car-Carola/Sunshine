package com.cadaloco.sunshine.activities;

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

import com.cadaloco.sunshine.R;
import com.cadaloco.sunshine.adapters.FetchWeatherTask;
import com.cadaloco.sunshine.adapters.ForecastRecyclerViewAdapter;
import com.cadaloco.sunshine.adapters.RecyclerViewCursorAdapter;
import com.cadaloco.sunshine.data.WeatherContract;
import com.cadaloco.sunshine.utils.LogUtil;
import com.cadaloco.sunshine.utils.Utility;

public class ForecastFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int FORECAST_LOADER = 0;

    private RecyclerViewCursorAdapter mForecastAdapter;

    private RecyclerView mRecyclerView;

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

        mForecastAdapter = new ForecastRecyclerViewAdapter(getActivity().getApplicationContext());
        mRecyclerView.setAdapter(mForecastAdapter);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(
                getActivity(),
                LinearLayoutManager.VERTICAL,
                false));

        mRecyclerView.addItemDecoration(new DividerItemDecoration(
                getActivity(), LinearLayoutManager.VERTICAL));
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        LogUtil.logMethodCalled();
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
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

        FetchWeatherTask weatherTask = new FetchWeatherTask(getActivity().getApplicationContext());
        String location = Utility.getPreferredLocation(getActivity());
        weatherTask.execute(location);
    }

    @Override
    public void onStart() {
        LogUtil.logMethodCalled();
        super.onStart();
        updateWeather();

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        LogUtil.logMethodCalled();

        String locationSetting = Utility.getPreferredLocation(getActivity());

        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                locationSetting, System.currentTimeMillis());

        switch(id) {
            case FORECAST_LOADER:
                return new CursorLoader(
                        getActivity().getApplicationContext(),
                        weatherForLocationUri,
                        null,
                        null,
                        null,
                        sortOrder
                );
            default:
                throw new UnsupportedOperationException("Unknown loader id: " + id);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        LogUtil.logMethodCalled();

        switch(loader.getId()) {
            case FORECAST_LOADER:
                mForecastAdapter.swapCursor(data);
                break;
            default:
                throw new UnsupportedOperationException("Unknown loader id: " + loader.getId());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        LogUtil.logMethodCalled();

        switch(loader.getId()) {
            case FORECAST_LOADER:
                mForecastAdapter.swapCursor(null);
                break;
            default:
                throw new UnsupportedOperationException("Unknown loader id: " + loader.getId());
        }
    }
}