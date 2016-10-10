package android.kaviles.bletutorial;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Kelvin on 5/8/16.
 */
public class GattUpdateReceiver extends BroadcastReceiver {

    private boolean mConnected = false;

    private BtleDeviceActivity activity;

    public GattUpdateReceiver(BtleDeviceActivity activity) {
        this.activity = activity;
    }

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device. This can be a
    // result of read or notification operations.

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        if (BtleGattService.ACTION_GATT_CONNECTED.equals(action)) {
            mConnected = true;
        } else if (BtleGattService.ACTION_GATT_DISCONNECTED.equals(action)) {
            mConnected = false;
            Utils.toast(activity.getApplicationContext(), "Disconnected From Device");
            activity.finish();
        } else if (BtleGattService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
            activity.updateServices();
        } else if (BtleGattService.ACTION_DATA_AVAILABLE.equals(action)) {

//            String uuid = intent.getStringExtra(BtleGattService.EXTRA_UUID);
//            String data = intent.getStringExtra(BtleGattService.EXTRA_DATA);

            activity.updateCharacteristic();
        }

        return;
    }
}
