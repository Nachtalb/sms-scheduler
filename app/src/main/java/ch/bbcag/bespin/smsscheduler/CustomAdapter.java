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

public class CustomAdapter extends BaseAdapter {

    Context context;
    List<RowItem> rowItems;

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

    /* private view holder class */
    private class ViewHolder {
        // ImageView profile_pic;
        TextView title;
        TextView timestamp;
        TextView phoneNr;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


        ViewHolder holder = null;

        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        RowItem row_pos = rowItems.get(position);

        if (convertView == null && !rowItems.isEmpty()) {

            convertView = mInflater.inflate(R.layout.list_item, null);
            holder = new ViewHolder();

            holder.title = (TextView) convertView.findViewById(R.id.title);
            holder.timestamp = (TextView) convertView.findViewById(R.id.timestamp);
            holder.phoneNr = (TextView) convertView.findViewById(R.id.phoneNr);

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
            holder = (ViewHolder) convertView.getTag();
        }

        return convertView;
    }

    private String dateToString(Date date) {
        // Create an instance of SimpleDateFormat used for formatting
        // the string representation of date (month/day/year)

        SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        // Using DateFormat format method we can create a string
        // representation of a date with the defined format.
        String dateString = df.format(date);

        // Print what date is today!
        return dateString;
    }
}
