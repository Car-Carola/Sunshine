package com.cadaloco.sunshine;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cadaloco.sunshine.data.WeatherContract;
import com.cadaloco.sunshine.utils.LogUtil;
import com.cadaloco.sunshine.utils.Utility;

/**
 * Created by Carol on 19-Dec-16.
 */

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.RecyclerViewHolder> {

    private static final int VIEW_TYPE_TODAY = 0;
    private static final int VIEW_TYPE_FUTURE_DAY = 2;
    private Cursor mCursor;

    public RecyclerAdapter(Context mContext) {
        LogUtil.logMethodCalled();
    }

    //RecyclerView Methods
    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LogUtil.logMethodCalled();

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_forecast, parent, false);

            return new RecyclerViewHolder(view);
        }

    @Override
    public void onBindViewHolder(RecyclerViewHolder holder, int position) {
        LogUtil.logMethodCalled();

        //Set The curser
        if (mCursor == null) {
            return;
        }

        mCursor.moveToPosition(position);

        holder.dateValue = mCursor.getLong(ForecastFragment.COL_WEATHER_DATE);


        boolean isMetric = Utility.isMetric(holder.mView.getContext());
        //String text = convertCursorRowToUXFormat(mCursor, isMetric);

        holder.description.setText(mCursor.getString(ForecastFragment.COL_WEATHER_DESC));
        long dateInMillis = mCursor.getLong(ForecastFragment.COL_WEATHER_DATE);

        String date = Utility.getFriendlyDayString(holder.mView.getContext(), dateInMillis);
        holder.date.setText(date);
        //holder.date.setText(Utility.formatDate(mCursor.getLong(ForecastFragment.COL_WEATHER_DATE)));
        String highTemp = Utility.formatTemperature(mCursor, ForecastFragment.COL_WEATHER_MAX_TEMP,isMetric);
        holder.high.setText(highTemp);
        String lowTemp = Utility.formatTemperature(mCursor, ForecastFragment.COL_WEATHER_MIN_TEMP,isMetric);
        holder.low.setText(lowTemp);
        int resourceId = mCursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID);
        //TODO set weather image icon
        holder.mIcon.setImageResource(R.mipmap.ic_launcher);
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0) ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
    }

    @Override
    public int getItemCount() {
        LogUtil.logMethodCalled();
        return mCursor == null ? 0 : mCursor.getCount();
    }

    public void swapCursor(Cursor cursor) {
        LogUtil.logMethodCalled();

        this.mCursor = cursor;
        notifyDataSetChanged();
    }

    public class RecyclerViewHolder extends RecyclerView.ViewHolder{

        //Views
        private final View mView;
        private ImageView mIcon;
        private TextView date;
        private TextView description;
        private TextView high;
        private TextView low;

        public long dateValue;

        public View.OnClickListener mListener;

        public RecyclerViewHolder(View itemView) {
            super(itemView);
            LogUtil.logMethodCalled();
            mView = itemView;

            //mForecast = (TextView) itemView.findViewById(R.id.list_item_forecast_textView);
            mIcon = (ImageView)itemView.findViewById(R.id.list_item_icon);
            date = (TextView) itemView.findViewById(R.id.list_item_date_textview);
            description = (TextView)itemView.findViewById(R.id.list_item_forecast_textview);
            high = (TextView) itemView.findViewById(R.id.list_item_high_textview);
            low = (TextView) itemView.findViewById(R.id.list_item_low_textview);


            //isMetric = Utility.isMetric(itemView.getContext());

            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    LogUtil.logMethodCalled();
                    Context context = view.getContext();
                    String locationSetting = Utility.getPreferredLocation(context);
                    //long dateInMillis = mCursor.getLong(ForecastFragment.COL_WEATHER_DATE);

                    Uri uri = WeatherContract.WeatherEntry
                            .buildWeatherLocationWithDate(
                                    locationSetting,
                                    dateValue);
                    Intent intent = new Intent(context, DetailActivity.class).setData(uri);
//                        String txt = Utility.generateSimpleText(cursor, Utility.isMetric(context));
                    //Toast.makeText(context, uri.toString() + " was clicked!\n" +funnyText, Toast.LENGTH_SHORT).show();

                    context.startActivity(intent);
                }
            });

        }
    }

    //Text Helper Methods
    private String convertCursorRowToUXFormat(Cursor cursor, boolean isMetric) {

        String highAndLow = formatHighLows(
                cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP),
                cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP), isMetric);

        return Utility.formatDate(cursor.getLong(ForecastFragment.COL_WEATHER_DATE)) +
                " - " + cursor.getString(ForecastFragment.COL_WEATHER_DESC) +
                " - " + highAndLow;
    }

    private String formatHighLows(double high, double low, boolean isMetric) {
        String highLowStr = Utility.formatTemperature(high, isMetric) + "/" + Utility.formatTemperature(low, isMetric);
        return highLowStr;
    }

}
