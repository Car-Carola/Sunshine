package com.cadaloco.sunshine.activities;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import android.widget.TextView;

import com.cadaloco.sunshine.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainActivityFragment extends Fragment {

    private RecyclerView recyclerView;

    public MainActivityFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        setHasOptionsMenu(true);

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview_forecast);

        String[] data = {
                "Mon 6/23 - Sunny - 31/17",
                "Tue 6/24 - Foggy - 21/8",
                "Wed 6/25 - Cloudy - 22/17",
                "Thurs 6/26 - Rainy - 18/11",
                "Fri 6/27 - Foggy - 21/10",
                "Sat 6/28 - TRAPPED IN WEATHERSTATION - 23/18",
                "Sun 6/29 - Sunny - 20/7",
                "Mon 6/30 - Sunny - 31/17",
                "Tue 7/1 - Foggy - 21/8",
                "Wed 7/2 - Cloudy - 22/17",
                "Thurs 7/3 - Rainy - 18/11",
                "Fri 7/4 - Foggy - 21/10",
                "Sat 7/5 - TRAPPED IN WEATHERSTATION - 23/18",
                "Sun 7/6 - Sunny - 20/7"
        };
        List<String> weekForecast = new ArrayList<>(Arrays.asList(data));

        ForecastEntriesAdapter adapter = new ForecastEntriesAdapter(getActivity(), weekForecast);
        recyclerView.setAdapter(adapter);

        recyclerView.setLayoutManager(new LinearLayoutManager(
                getActivity(),
                LinearLayoutManager.VERTICAL,
                false));

        return view;
    }


    //menu
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.vertical_lm:
                recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
                return true;
            case R.id.horizontal_lm:
                recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
                return true;
            case R.id.grid_lm:
                recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
                return true;
            case R.id.staggered_lm:
                recyclerView.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }    }



    //RecyclerView Classes
    public static class ForecastViewHolder extends RecyclerView.ViewHolder{
        TextView forecast;

        public ForecastViewHolder(View itemView) {
            super(itemView);
        }
    }

    public static class ForecastEntriesAdapter extends RecyclerView.Adapter<ForecastViewHolder> {

        private List<String> weekForecast;
        private LayoutInflater inflater;

        public ForecastEntriesAdapter(Context context, List<String> weekForecast){

            this.weekForecast = weekForecast;
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public ForecastViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            //Creates itemView line
            View itemView = inflater.inflate(R.layout.list_item_forecast, parent, false);
            ForecastViewHolder holder = new ForecastViewHolder(itemView);

            //initialize views in line
            holder.forecast = (TextView) itemView.findViewById(R.id.list_item_forecast_textView);

            return holder;
        }

        @Override
        public void onBindViewHolder(ForecastViewHolder holder, int position) {

            String forecastText = weekForecast.get(position);
            holder.forecast.setText(forecastText);
        }

        @Override
        public int getItemCount() {

            return weekForecast.size() - 1;
        }
    }
}
