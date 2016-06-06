package ch.bbcag.bespin.smsscheduler;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * The Custom adapter.
 */
public class CustomAdapter extends BaseAdapter {

    /**
     * The Context.
     */
    Context context;
    /**
     * The Row items.
     */
    List<RowItem> rowItems;

    /**
     * Instantiates a new Custom adapter.
     *
     * @param context  - The context
     * @param rowItems - The row items
     */
    CustomAdapter(Context context, List<RowItem> rowItems) {
        this.context = context;
        this.rowItems = rowItems;
    }

    @Override
    public int getCount() {
        return rowItems.size();
    }

    @Override
    public Object getItem(int position) {
        return rowItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return rowItems.indexOf(getItem(position));
    }

    /**
     * Get a View that displays the data at the specified position in the data set.
     *
     * @param position    - The position of the item within the adapter's data set of the item whose
     *                    view we want.
     * @param convertView - The old view to reuse, if possible. Note: You should check that this
     *                    view is non-null and of an appropriate type before using. If it is not
     *                    possible to convert this view to display the correct data, this method
     *                    can create a new view. Heterogeneous lists can specify their number of
     *                    view types, so that this View is always of the right type
     *                    (see getViewTypeCount() and getItemViewType(int)).
     * @param parent      - The parent that this view will eventually be attached to
     * @return
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        RowItem row_pos = rowItems.get(position);

        if (convertView == null && !rowItems.isEmpty()) {

            convertView = mInflater.inflate(R.layout.list_item, null);
            holder = new ViewHolder();

            holder.title = (TextView) convertView.findViewById(R.id.listTitle);
            holder.timestamp = (TextView) convertView.findViewById(R.id.listTimestamp);
            holder.phoneNr = (TextView) convertView.findViewById(R.id.listPhoneNr);

            holder.title.setText(row_pos.title);
            holder.phoneNr.setText(row_pos.phoneNr);

            if (row_pos.timestamp == 0) {
                holder.timestamp.setText("");
            } else {
                String date = dateToString(new Date(row_pos.timestamp));
                holder.timestamp.setText(date);
            }

            convertView.setTag(holder);
        } else {
            assert convertView != null;
            holder = (ViewHolder) convertView.getTag();
        }

        return convertView;
    }

    /**
     * Converts a date into a String
     *
     * @param date - Date to convert
     * @return The datestring
     */
    private String dateToString(Date date) {
        SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.GERMAN);
        return df.format(date);
    }

    /**
     * private view holder class
     */
    private class ViewHolder {
        /**
         * The Title.
         */
        // ImageView profile_pic;
        TextView title;
        /**
         * The Timestamp.
         */
        TextView timestamp;
        /**
         * The Phone nr.
         */
        TextView phoneNr;
    }
}
