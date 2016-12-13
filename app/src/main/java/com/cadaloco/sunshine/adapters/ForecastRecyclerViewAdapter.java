package com.cadaloco.sunshine.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cadaloco.sunshine.R;
import com.cadaloco.sunshine.data.WeatherContract;
import com.cadaloco.sunshine.utils.Utility;

/**
Iplements abstract RVCA class
 */
public class ForecastRecyclerViewAdapter extends RecyclerViewCursorAdapter<ForecastRecyclerViewAdapter.ForecastViewHolder>{

    /**
     * Column projection for the query to pull Movies from the database.
     */
/*    public static final String[] MOVIE_COLUMNS = new String[] {
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_NAME
    };*/

    /**
     * Index of the name column.
     */
    protected static final int NAME_INDEX = 1;

    /**
     * Constructor.
     * @param context The Context the Adapter is displayed in.
     */
    public ForecastRecyclerViewAdapter(Context context) {
        super(context);

        setupCursorAdapter(null, 0, R.layout.list_item_forecast, false);
    }

    /**
     * Returns the ViewHolder to use for this adapter.
     */
    @Override
    public ForecastViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ForecastViewHolder(mCursorAdapter.newView(mContext, mCursorAdapter.getCursor(), parent));
    }

    /**
     * Moves the Cursor of the CursorAdapter to the appropriate position and binds the view for
     * that item.
     */
    @Override
    public void onBindViewHolder(ForecastViewHolder holder, int position) {
        // Move cursor to this position
        mCursorAdapter.getCursor().moveToPosition(position);

        // Set the ViewHolder
        setViewHolder(holder);

        // Bind this view
        mCursorAdapter.bindView(null, mContext, mCursorAdapter.getCursor());
    }

    public class ForecastViewHolder extends RecyclerViewCursorAdapter.RecyclerViewCursorViewHolder {

        public final TextView mForecast;

        public ForecastViewHolder(View view) {
            super(view);

            mForecast = (TextView) view.findViewById(R.id.list_item_forecast_textView);
        }

        @Override
        public void bindCursor(Cursor cursor) {
            //mForecast.setText(cursor.getString(ForecastRecyclerViewAdapter.NAME_INDEX));
            mForecast.setText(convertCursorRowToUXFormat(cursor));

        }

    }

    private String convertCursorRowToUXFormat(Cursor cursor) {
        // get row indices for our cursor
        int idx_max_temp = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP);
        int idx_min_temp = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP);
        int idx_date = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATE);
        int idx_short_desc = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC);

        String highAndLow = formatHighLows(
                cursor.getDouble(idx_max_temp),
                cursor.getDouble(idx_min_temp));

        return Utility.formatDate(cursor.getLong(idx_date)) +
                " - " + cursor.getString(idx_short_desc) +
                " - " + highAndLow;
    }

    private String formatHighLows(double high, double low) {
        boolean isMetric = Utility.isMetric(mContext);
        String highLowStr = Utility.formatTemperature(high, isMetric) + "/" + Utility.formatTemperature(low, isMetric);
        return highLowStr;
    }
}
