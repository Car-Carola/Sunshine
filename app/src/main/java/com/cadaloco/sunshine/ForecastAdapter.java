package com.cadaloco.sunshine;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.widget.CursorAdapter;
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

    protected final Context mContext;

    protected CursorAdapter mCursorAdapter;

    private ForecastViewHolder mViewHolder;

    public ForecastAdapter(Context mContext) {
        LogUtil.logMethodCalled();
        this.mContext = mContext;

        initCursorAdapter(mContext);
    }

    // init cursor adapter
    private void initCursorAdapter(final Context mContext) {
        LogUtil.logMethodCalled();

        mCursorAdapter = new CursorAdapter(mContext, null, false) {
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                LogUtil.logMethodCalled();

                return LayoutInflater.from(context).inflate(R.layout.list_item_forecast, parent, false);

            }

            @Override
            public void bindView(View view, Context context, final Cursor cursor) {
                LogUtil.logMethodCalled();

                mViewHolder.getmForecast().setText(convertCursorRowToUXFormat(cursor));

                mViewHolder.getmView().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        LogUtil.logMethodCalled();
                        if (cursor != null) {
                            Context context = view.getContext();
                            String locationSetting = Utility.getPreferredLocation(context);
                            Uri uri = WeatherContract.WeatherEntry
                                    .buildWeatherLocationWithDate(
                                            locationSetting,
                                            cursor.getLong(ForecastFragment.COL_WEATHER_DATE));
                            Intent intent = new Intent(context, DetailActivity.class).setData(uri);
                            String txt = Utility.generateSimpleText(cursor, Utility.isMetric(context));
                            Toast.makeText(context, uri.toString() + " was clicked!\n" +txt, Toast.LENGTH_SHORT).show();

                            context.startActivity(intent);
                        }
                    }
                });
            }
        };
    }

    //View Holder
    public class ForecastViewHolder extends RecyclerView.ViewHolder{

        private final View mView;
        private TextView mForecast;

        public ForecastViewHolder(View itemView) {
            super(itemView);
            LogUtil.logMethodCalled();
            mView = itemView;

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
        return new ForecastViewHolder(mCursorAdapter.newView(mContext, mCursorAdapter.getCursor(), parent));
    }

    @Override
    public void onBindViewHolder(ForecastViewHolder holder, int position) {
        LogUtil.logMethodCalled();
        // Move cursor to this position
        mCursorAdapter.getCursor().moveToPosition(position);

        // Set the ViewHolder
        this.mViewHolder = holder;

        // Bind this view
        mCursorAdapter.bindView(null, mContext, mCursorAdapter.getCursor());

    }

    @Override
    public int getItemCount() {
        LogUtil.logMethodCalled();
        return mCursorAdapter.getCount();
    }


    // Helper Methods
    /**
     * Swap the Cursor of the CursorAdapter and notify the RecyclerView.Adapter that data has
     * changed.
     * @param cursor The new Cursor representation of the data to be displayed.
     */
    public void swapCursor(Cursor cursor) {
        LogUtil.logMethodCalled();

        this.mCursorAdapter.swapCursor(cursor);
        notifyDataSetChanged();
    }

    private String convertCursorRowToUXFormat(Cursor cursor) {

        String highAndLow = formatHighLows(
                cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP),
                cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP));

        return Utility.formatDate(cursor.getLong(ForecastFragment.COL_WEATHER_DATE)) +
                " - " + cursor.getString(ForecastFragment.COL_WEATHER_DESC) +
                " - " + highAndLow;
    }

    private String formatHighLows(double high, double low) {
        boolean isMetric = Utility.isMetric(mContext);
        String highLowStr = Utility.formatTemperature(high, isMetric) + "/" + Utility.formatTemperature(low, isMetric);
        return highLowStr;
    }
}
