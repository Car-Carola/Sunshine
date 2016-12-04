package com.cadaloco.sunshine.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cadaloco.sunshine.R;
import com.cadaloco.sunshine.activities.DetailActivity;
import com.cadaloco.sunshine.utils.LogUtil;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Carol on 22-Nov-16.
 */

//RecyclerView Classes
public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder> {


    public static class ForecastViewHolder extends RecyclerView.ViewHolder{

        private final View mView;
        private TextView forecast;

        public ForecastViewHolder(final View itemView) {
            super(itemView);
            mView = itemView;

            forecast = (TextView) itemView.findViewById(R.id.list_item_forecast_textView);

        }

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
    public void onBindViewHolder(final ForecastViewHolder holder, final int position) {

        String item = weekForecast.get(position);
        holder.forecast.setText(item);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = v.getContext();
                LogUtil.logMethodCalled();
                String forecast = getItem(position);
                //Toast.makeText(getActivity().getApplicationContext(), forecast + " was clicked!", Toast.LENGTH_SHORT).show();

                Intent intent = DetailActivity.createIntent(context, forecast);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {

        return weekForecast.size() ;
    }

    public void swapData(List<String> newList) {

        weekForecast = checkNotNull(newList);
        notifyDataSetChanged();
    }

    public String getItem(int position) {
        return weekForecast.get(position);
    }
}
