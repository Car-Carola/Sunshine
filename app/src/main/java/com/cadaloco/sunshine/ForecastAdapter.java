package com.cadaloco.sunshine;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.cadaloco.sunshine.data.WeatherContract;
import com.cadaloco.sunshine.utils.LogUtil;
import com.cadaloco.sunshine.utils.Utility;

/**
 * Created by Carol on 14-Dec-16.
 * create new adapte from the top
 */

public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder> {

    Cursor cursor;

    public ForecastAdapter(Context mContext) {
        LogUtil.logMethodCalled();
    }

    // init cursor adapter

    //View Holder
    public class ForecastViewHolder extends RecyclerView.ViewHolder{

        private final View mView;
        private TextView mForecast;
        public long date;
        public String funnyText;
        public View.OnClickListener mListener;

        public ForecastViewHolder(View itemView) {
            super(itemView);
            LogUtil.logMethodCalled();
            mView = itemView;

            mListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    LogUtil.logMethodCalled();
                        Context context = view.getContext();
                        String locationSetting = Utility.getPreferredLocation(context);
                        Uri uri = WeatherContract.WeatherEntry
                                          .buildWeatherLocationWithDate(
                                                  locationSetting,
                                                  date);
                        Intent intent = new Intent(context, DetailActivity.class).setData(uri);
//                        String txt = Utility.generateSimpleText(cursor, Utility.isMetric(context));
                        Toast.makeText(context, uri.toString() + " was clicked!\n" +funnyText, Toast.LENGTH_SHORT).show();

                        context.startActivity(intent);
                }
            };
            mView.setOnClickListener(mListener);
            mForecast = (TextView) itemView.findViewById(R.id.list_item_forecast_textView);
        }

        public TextView getmForecast() {
            return mForecast;
        }

        public View getmView() {
            return mView;
        }

    }


    //Adapter Methods
    @Override
    public ForecastViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LogUtil.logMethodCalled();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_forecast, parent, false);
        return new ForecastViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ForecastViewHolder holder, int position) {
        LogUtil.logMethodCalled();
        if (cursor == null) {
            return;
        }
        // Move cursor to this position
        cursor.moveToPosition(position);

        // Set the ViewHolder
        holder.date = cursor.getLong(ForecastFragment.COL_WEATHER_DATE);
        holder.funnyText = Utility.generateSimpleText(cursor, Utility.isMetric(holder.getmView().getContext()));
        // Bind this view
        holder.getmForecast().setText(convertCursorRowToUXFormat(cursor, holder.getmView().getContext()));
//        mCursorAdapter.bindView(null, mContext, cursor);

    }

    @Override
    public int getItemCount() {
        LogUtil.logMethodCalled();
        return cursor == null ? 0 :cursor.getCount();
    }


    // Helper Methods
    /**
     * Swap the Cursor of the CursorAdapter and notify the RecyclerView.Adapter that data has
     * changed.
     * @param cursor The new Cursor representation of the data to be displayed.
     */
    public void swapCursor(Cursor cursor) {
        LogUtil.logMethodCalled();

        this.cursor = cursor;
        notifyDataSetChanged();
    }

    private String convertCursorRowToUXFormat(Cursor cursor, Context context) {

        String highAndLow = formatHighLows(
                cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP),
                cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP),
                context);

        return Utility.formatDate(cursor.getLong(ForecastFragment.COL_WEATHER_DATE)) +
                " - " + cursor.getString(ForecastFragment.COL_WEATHER_DESC) +
                " - " + highAndLow;
    }

    private String formatHighLows(double high, double low, Context context) {
        boolean isMetric = Utility.isMetric(context);
        String highLowStr = Utility.formatTemperature(high, isMetric) + "/" + Utility.formatTemperature(low, isMetric);
        return highLowStr;
    }
}
