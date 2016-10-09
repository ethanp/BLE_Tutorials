package android.kaviles.bletutorial;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ScrollView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("ALL")
public class MainActivity extends AppCompatActivity implements
        // "scan" button
        View.OnClickListener,
        // dynamic list of bluetooth devices
        AdapterView.OnItemClickListener {

    public static final int REQUEST_ENABLE_BT = 1;
    public static final int BTLE_SERVICES = 2;

    private HashMap<String, BtleDevice> mBtDevicesByName;
    private List<BtleDevice> mBtDevicesList;
    private BtleListAdapter btleListAdapter;
    private Button scanButton;

    private CreateToastOnBtAdptrStateChange btAdaptorStateChangeListener;
    private BtleScanner mBtleScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Use this check to determine whether BLE is supported on the device. Then
        // you can selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Utils.toast(getApplicationContext(), "BLE not supported");
            finish();
        }

        btAdaptorStateChangeListener = new CreateToastOnBtAdptrStateChange(getApplicationContext());
        final int scanPeriodFiveSeconds = 5000;
        final int minSignalStrength = -75;
        mBtleScanner = new BtleScanner(this, scanPeriodFiveSeconds, minSignalStrength);
        mBtDevicesByName = new HashMap<>();
        mBtDevicesList = new ArrayList<>();
        btleListAdapter = new BtleListAdapter(this, R.layout.btle_device_list_item, mBtDevicesList);

        // Just the built-in ListView.
        // This Activity is the Context for the ListView as well as click listener.
        ListView listView = new ListView(this);
        listView.setAdapter(btleListAdapter);
        listView.setOnItemClickListener(this);

        scanButton = (Button) findViewById(R.id.btn_scan);
        scanButton.setOnClickListener(this);

        // ui note: we load the configured ListView into the DOMs existing ScrollView
        ((ScrollView) findViewById(R.id.scrollView)).addView(listView);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(btAdaptorStateChangeListener);
        stopScan();
    }

    public void stopScan() {
        scanButton.setText("Scan Again");
        mBtleScanner.stop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to.
        // These are the response codes that we asked for when calling "startActivityForResult"
        if (requestCode == REQUEST_ENABLE_BT) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                Utils.toast(getApplicationContext(), "Thank you for turning on Bluetooth");
            } else if (resultCode == RESULT_CANCELED) {
                Utils.toast(getApplicationContext(), "Please turn on Bluetooth");
            }
        } else if (requestCode == BTLE_SERVICES) {
            // not implemented
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopScan();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // this intent is globally unique, so we just look for it
        IntentFilter changedFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(btAdaptorStateChangeListener, changedFilter);
    }

    /**
     * user clicked on a specific bluetooth device in the list
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Context context = view.getContext();
        Utils.toast(context, "List Item clicked");
        // do something with the text views and start the next activity.
        stopScan();

        String name = mBtDevicesList.get(position).getName();
        String address = mBtDevicesList.get(position).getAddress();

        Intent intent = new Intent(this, BtleDeviceActivity.class);
        intent.putExtra(BtleDeviceActivity.EXTRA_NAME, name);
        intent.putExtra(BtleDeviceActivity.EXTRA_ADDRESS, address);
        startActivityForResult(intent, BTLE_SERVICES);
    }

    /**
     * user clicked on the scan button
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_scan:
                Utils.toast(getApplicationContext(), "Scan Button Pressed");
                if (!mBtleScanner.isScanning()) {
                    startScan();
                } else {
                    stopScan();
                }
                break;
            default:
                break;
        }
    }

    public void startScan() {
        scanButton.setText("Scanning...");
        mBtDevicesList.clear();
        mBtDevicesByName.clear();
        mBtleScanner.start();
    }

    public void addDeviceOrUpdateRssi(BluetoothDevice device, int rssi) {
        // this check is the reason we have mBtDevicesByName
        if (!mBtDevicesByName.containsKey(device.getAddress())) {
            addDevice(device, rssi);
        } else {
            mBtDevicesByName.get(device.getAddress()).setRSSI(rssi);
        }
        btleListAdapter.notifyDataSetChanged();
    }

    private void addDevice(BluetoothDevice device, int rssi) {
        BtleDevice btleDevice = new BtleDevice(device);
        btleDevice.setRSSI(rssi);
        mBtDevicesByName.put(device.getAddress(), btleDevice);
        mBtDevicesList.add(btleDevice);
    }
}
