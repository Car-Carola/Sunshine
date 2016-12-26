package com.cadaloco.sunshine;

import android.content.Context;
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

import static com.cadaloco.sunshine.ForecastFragment.COL_WEATHER_DATE;

/**
 * Created by Carol on 19-Dec-16.
 */

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.RecyclerViewHolder> {

    private static final int VIEW_TYPE_TODAY = 0;
    private static final int VIEW_TYPE_FUTURE_DAY = 1;

    private boolean mUseTodayLayout = true;
    /*
 * Below, we've defined an interface to handle clicks on items within this Adapter. In the
 * constructor of our ForecastAdapter, we receive an instance of a class that has implemented
 * said interface. We store that instance in this variable to call the onClick method whenever
 * an item is clicked in the list.
 */
    final private ForecastAdapterOnClickHandler mClickHandler;

    /**
     * The interface that receives onClick messages.
     */
    public interface ForecastAdapterOnClickHandler {
        void onItemSelected(Uri contentUri);
    }

    private Cursor mCursor;

    public RecyclerAdapter(ForecastAdapterOnClickHandler clickHandler) {
        LogUtil.logMethodCalled();
        mClickHandler = clickHandler;

    }

    //RecyclerView Methods
    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LogUtil.logMethodCalled();

        int layoutId = -1;

        switch (viewType) {
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

        holder.mDateValue = mCursor.getLong(COL_WEATHER_DATE);


        Context context = holder.mView.getContext();
        boolean isMetric = Utility.isMetric(context);
        //String text = convertCursorRowToUXFormat(mCursor, isMetric);

        String descriptionText = mCursor.getString(ForecastFragment.COL_WEATHER_DESC);
        holder.mDescription.setText(descriptionText);
        long dateInMillis = mCursor.getLong(COL_WEATHER_DATE);

        String date = Utility.getFriendlyDayString(context, dateInMillis);
        holder.mDate.setText(date);

        String highTemp = Utility.formatTemperature(context, mCursor, ForecastFragment.COL_WEATHER_MAX_TEMP, isMetric);
        holder.mHigh.setText(highTemp);

        String lowTemp = Utility.formatTemperature(context, mCursor, ForecastFragment.COL_WEATHER_MIN_TEMP, isMetric);
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
        holder.mIcon.setContentDescription(descriptionText);
        //holder.mIcon.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));
    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        mUseTodayLayout = useTodayLayout;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0 && mUseTodayLayout) ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
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

    public class RecyclerViewHolder extends RecyclerView.ViewHolder  {

        //Views
        private final View mView;
        private final ImageView mIcon;
        private final TextView mDate;
        private final TextView mDescription;
        private final TextView mHigh;
        private final TextView mLow;

        private long mDateValue;


        public RecyclerViewHolder(View itemView) {

            super(itemView);
            LogUtil.logMethodCalled();
            mView = itemView;

            mIcon = (ImageView) itemView.findViewById(R.id.list_item_icon);
            mDate = (TextView) itemView.findViewById(R.id.list_item_date_textview);
            mDescription = (TextView) itemView.findViewById(R.id.list_item_forecast_textview);
            mHigh = (TextView) itemView.findViewById(R.id.list_item_high_textview);
            mLow = (TextView) itemView.findViewById(R.id.list_item_low_textview);

            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    LogUtil.logMethodCalled();
                    Context context = view.getContext();

                    String locationSetting = Utility.getPreferredLocation(context);

                    Uri uri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                            locationSetting,
                            mDateValue);

                    view.setSelected(true);

                    mClickHandler.onItemSelected(uri);
                }
            });
        }

    }
}
