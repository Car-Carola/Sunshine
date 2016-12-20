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
    private static final int VIEW_TYPE_FUTURE_DAY = 1;
    private Cursor mCursor;

    public RecyclerAdapter(Context mContext) {
        LogUtil.logMethodCalled();
    }

    //RecyclerView Methods
    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LogUtil.logMethodCalled();

        View view = null;

        switch (viewType){
            case VIEW_TYPE_TODAY:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_forecast_today, parent, false);
                break;
            case VIEW_TYPE_FUTURE_DAY:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_forecast, parent, false);
                break;
            default:
                throw new UnsupportedOperationException("Unknown VIEW TYPE: " + viewType);

        }

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

            mIcon = (ImageView)itemView.findViewById(R.id.list_item_icon);
            date = (TextView) itemView.findViewById(R.id.list_item_date_textview);
            description = (TextView)itemView.findViewById(R.id.list_item_forecast_textview);
            high = (TextView) itemView.findViewById(R.id.list_item_high_textview);
            low = (TextView) itemView.findViewById(R.id.list_item_low_textview);


            mListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    LogUtil.logMethodCalled();
                    Context context = view.getContext();

                    String locationSetting = Utility.getPreferredLocation(context);

                    Uri uri = WeatherContract.WeatherEntry
                            .buildWeatherLocationWithDate(
                                    locationSetting,
                                    dateValue);

                    Intent intent = new Intent(context, DetailActivity.class).setData(uri);

                    context.startActivity(intent);
                }
            };
            mView.setOnClickListener(mListener);

        }
    }
}
