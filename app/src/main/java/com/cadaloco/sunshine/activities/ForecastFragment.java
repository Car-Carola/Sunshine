package com.cadaloco.sunshine.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
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
import com.cadaloco.sunshine.adapters.ForecastAdapter;
import com.cadaloco.sunshine.utils.LogUtil;

import java.util.ArrayList;

public class ForecastFragment extends Fragment {

    public static final String FORECAST_FRAGMENT = "FORECAST_FRAGMENT";
    //private RecyclerView mRecyclerView;
    private ForecastAdapter mForecastAdapter;
    private RecyclerView mRecyclerView;

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        LogUtil.logMethodCalled();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        LogUtil.logMethodCalled();

        View view = inflater.inflate(R.layout.fragment_main, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerview_forecast);

        mForecastAdapter = new ForecastAdapter(new ArrayList<String>());

        mRecyclerView.setAdapter(mForecastAdapter);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(
                getActivity(),
                LinearLayoutManager.VERTICAL,
                false));

        mRecyclerView.addItemDecoration(new DividerItemDecoration(
                getActivity(), LinearLayoutManager.VERTICAL));


      return view;
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

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        String zipCode = prefs.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));

        FetchWeatherTask weatherTask = new FetchWeatherTask(getActivity(), mForecastAdapter);
        weatherTask.execute(zipCode);
    }


    @Override
    public void onStart() {
        LogUtil.logMethodCalled();
        super.onStart();
        updateWeather();

    }

}