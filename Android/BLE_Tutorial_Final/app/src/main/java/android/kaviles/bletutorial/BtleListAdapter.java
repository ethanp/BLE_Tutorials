package android.kaviles.bletutorial;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Kelvin on 5/7/16.
 */
@SuppressWarnings("ALL")
public class BtleListAdapter extends ArrayAdapter<BtleDevice> {

    private final Activity activity;
    private final int layoutResourceID;
    private final List<BtleDevice> underlyingList;

    public BtleListAdapter(Activity activity, int layoutResource, List<BtleDevice> underlyingList) {
        super(activity.getApplicationContext(), layoutResource, underlyingList);
        this.activity = activity;
        layoutResourceID = layoutResource;
        this.underlyingList = underlyingList;
    }

    /**
     * get element at underlyingList[position] and use it to configure the cell layout
     */
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) activity
                    .getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(layoutResourceID, parent, false);
        }

        BtleDevice device = underlyingList.get(position);

        final TextView tv = (TextView) convertView.findViewById(R.id.tv_name);
        String text = device.getName() == null || device.getName().isEmpty()
                ? "No Name"
                : device.getName();
        tv.setText(text);

        final TextView rssiTv = (TextView) convertView.findViewById(R.id.tv_rssi);
        rssiTv.setText("RSSI: " + Integer.toString(device.getRSSI()));

        final TextView addrTv = (TextView) convertView.findViewById(R.id.tv_macaddr);
        String text1 = device.getAddress() == null || device.getAddress().isEmpty()
                ? "No Address"
                : device.getAddress();
        addrTv.setText(text1);

        return convertView;
    }
}
