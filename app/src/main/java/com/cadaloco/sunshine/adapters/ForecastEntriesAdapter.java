package com.cadaloco.sunshine.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cadaloco.sunshine.R;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Carol on 22-Nov-16.
 */

//RecyclerView Classes
public class ForecastEntriesAdapter extends RecyclerView.Adapter<ForecastEntriesAdapter.ForecastViewHolder> {


    private List<String> weekForecast;

    public ForecastEntriesAdapter(List<String> weekForecast) {

        this.weekForecast = weekForecast;
    }

    @Override
    public ForecastViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_forecast, parent, false);

        return new ForecastViewHolder(itemView);

    }

    @Override
    public void onBindViewHolder(ForecastViewHolder holder, int position) {

        String item = weekForecast.get(position);
        holder.forecast.setText(item);
    }

    @Override
    public int getItemCount() {

        return weekForecast.size() ;
    }

    public void swapData(List<String> newList) {

        weekForecast = checkNotNull(newList);
        notifyDataSetChanged();
    }

    public static class ForecastViewHolder extends RecyclerView.ViewHolder{

        TextView forecast;

        public ForecastViewHolder(View itemView) {
            super(itemView);
            forecast = (TextView) itemView.findViewById(R.id.list_item_forecast_textView);
        }

    }
}
