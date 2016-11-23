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
public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder> {

    /*
    Recycler View Implementation: https://guides.codepath.com/android/using-the-recyclerview
     */
    public static class ForecastViewHolder extends RecyclerView.ViewHolder{

        TextView forecast;

        public ForecastViewHolder(final View itemView) {
            super(itemView);
            forecast = (TextView) itemView.findViewById(R.id.list_item_forecast_textView);

            // Setup the click listener
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Triggers click upwards to the adapter on click
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(itemView, position);
                        }
                    }
                }
            });
        }

    }

    // Define listener member variable
    private static OnItemClickListener listener;
    // Define the listener interface
    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }
    // Define the method that allows the parent activity or fragment to define the listener
    public void setOnItemClickListener(OnItemClickListener listener) {
        ForecastAdapter.listener = listener;
    }

    private List<String> weekForecast;

    public ForecastAdapter(List<String> weekForecast) {

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

}
