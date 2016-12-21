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

        int layoutId = -1;

        switch (viewType){
            case VIEW_TYPE_TODAY:
                layoutId = R.layout.list_item_forecast_today;
                break;
            case VIEW_TYPE_FUTURE_DAY:
                layoutId = R.layout.list_item_forecast;
                break;
            default:
                throw new UnsupportedOperationException("Unknown VIEW TYPE: " + viewType);

        }

        return new RecyclerViewHolder(LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false));
        }

    @Override
    public void onBindViewHolder(RecyclerViewHolder holder, int position) {
        LogUtil.logMethodCalled();

        //Set The curser
        if (mCursor == null) {
            return;
        }

        mCursor.moveToPosition(position);

        holder.mDateValue = mCursor.getLong(ForecastFragment.COL_WEATHER_DATE);


        Context context = holder.mView.getContext();
        boolean isMetric = Utility.isMetric(context);
        //String text = convertCursorRowToUXFormat(mCursor, isMetric);

        holder.mDescription.setText(mCursor.getString(ForecastFragment.COL_WEATHER_DESC));
        long dateInMillis = mCursor.getLong(ForecastFragment.COL_WEATHER_DATE);

        String date = Utility.getFriendlyDayString(context, dateInMillis);
        holder.mDate.setText(date);

        String highTemp = Utility.formatTemperature(context, mCursor, ForecastFragment.COL_WEATHER_MAX_TEMP,isMetric);
        holder.mHigh.setText(highTemp);

        String lowTemp = Utility.formatTemperature(context, mCursor, ForecastFragment.COL_WEATHER_MIN_TEMP,isMetric);
        holder.mLow.setText(lowTemp);

        int weatherId = mCursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID);
        int viewType = getItemViewType(position);
        int imageResource = -1;

        switch (viewType) {
            case VIEW_TYPE_TODAY: {
                // Get weather icon
                imageResource = Utility.getArtResourceForWeatherCondition(
                        mCursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID));
                break;
            }
            case VIEW_TYPE_FUTURE_DAY: {
                // Get weather icon
                imageResource = Utility.getIconResourceForWeatherCondition(
                        mCursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID));
                break;
            }
        }

        holder.mIcon.setImageResource(imageResource);
        //holder.mIcon.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));
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
        private final ImageView mIcon;
        private final TextView mDate;
        private final TextView mDescription;
        private final TextView mHigh;
        private final TextView mLow;

        private long mDateValue;

        private final View.OnClickListener mListener;

        public RecyclerViewHolder(View itemView) {
            super(itemView);
            LogUtil.logMethodCalled();
            mView = itemView;

            mIcon = (ImageView)itemView.findViewById(R.id.list_item_icon);
            mDate = (TextView) itemView.findViewById(R.id.list_item_date_textview);
            mDescription = (TextView)itemView.findViewById(R.id.list_item_forecast_textview);
            mHigh = (TextView) itemView.findViewById(R.id.list_item_high_textview);
            mLow = (TextView) itemView.findViewById(R.id.list_item_low_textview);


            mListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    LogUtil.logMethodCalled();
                    Context context = view.getContext();

                    String locationSetting = Utility.getPreferredLocation(context);

                    Uri uri = WeatherContract.WeatherEntry
                            .buildWeatherLocationWithDate(
                                    locationSetting,
                                    mDateValue);

                    Intent intent = new Intent(context, DetailActivity.class).setData(uri);

                    context.startActivity(intent);
                }
            };
            mView.setOnClickListener(mListener);

        }
    }
}
