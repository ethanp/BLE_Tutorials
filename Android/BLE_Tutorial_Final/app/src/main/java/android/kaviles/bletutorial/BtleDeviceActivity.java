package android.kaviles.bletutorial;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"LocalVariableOfConcreteClass", "ConstantConditions"})
public class BtleDeviceActivity extends AppCompatActivity implements ExpandableListView.OnChildClickListener {
    public static final String EXTRA_NAME = "android.aviles.bletutorial.BtleDeviceActivity.NAME";
    public static final String EXTRA_ADDRESS = "android.aviles.bletutorial.BtleDeviceActivity.ADDRESS";
    private final static String TAG = BtleDeviceActivity.class.getSimpleName();

    private BtleServiceListAdapter expandableListAdapter;
    private List<BluetoothGattService> services;
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection") // written but never read
    private Map<String, BluetoothGattCharacteristic> charByUuid;
    private Map<String, List<BluetoothGattCharacteristic>> charsByServiceUuid;

    private Intent serviceViewIntent;
    private BtleGattService btleGattService;
    @SuppressWarnings("unused")
    private boolean serviceIsBound;
    private GattUpdateReceiver mGattUpdateReceiver;

    private String address;

    private ServiceConnection mBtleServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            BtleGattService.BtleServiceBinder binder = (BtleGattService.BtleServiceBinder) service;
            btleGattService = binder.getService();
            serviceIsBound = true;
            if (!btleGattService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            btleGattService.connect(address);
            // Automatically connects to the device upon successful start-up initialization.
//            mBTLeService.connect(mBTLeDeviceAddress);
//            mBluetoothGatt = mBTLeService.getmBluetoothGatt();
//            mGattUpdateReceiver.setBluetoothGatt(mBluetoothGatt);
//            mGattUpdateReceiver.setBTLeService(mBTLeService);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            btleGattService = null;
            serviceIsBound = false;

//            mBluetoothGatt = null;
//            mGattUpdateReceiver.setBluetoothGatt(null);
//            mGattUpdateReceiver.setBTLeService(null);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_btle_services);
        Intent intent = getIntent();
        String name = intent.getStringExtra(BtleDeviceActivity.EXTRA_NAME);
        address = intent.getStringExtra(BtleDeviceActivity.EXTRA_ADDRESS);
        services = new ArrayList<>();
        charByUuid = new HashMap<>();
        charsByServiceUuid = new HashMap<>();
        expandableListAdapter = new BtleServiceListAdapter(
                this, services, charsByServiceUuid);
        ExpandableListView expandableListView = (ExpandableListView) findViewById(R.id.lv_expandable);
        expandableListView.setAdapter(expandableListAdapter);
        expandableListView.setOnChildClickListener(this);
        ((TextView) findViewById(R.id.tv_name)).setText(name + " Services");
        ((TextView) findViewById(R.id.tv_address)).setText(address);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mGattUpdateReceiver);
        unbindService(mBtleServiceConnection);
        serviceViewIntent = null;
    }

    /**
     * Called after onCreate(Bundle) or onRestart()
     */
    @Override
    protected void onStart() {
        super.onStart();
        mGattUpdateReceiver = new GattUpdateReceiver(this);
        registerReceiver(mGattUpdateReceiver, Utils.makeGattUpdateIntentFilter());
        serviceViewIntent = new Intent(this, BtleGattService.class);
        bindService(serviceViewIntent, mBtleServiceConnection, Context.BIND_AUTO_CREATE);
        startService(serviceViewIntent);
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        BluetoothGattCharacteristic characteristic = charsByServiceUuid.get(
                services.get(groupPosition).getUuid().toString())
                .get(childPosition);
        if (Utils.hasWriteProperty(characteristic.getProperties()) != 0) {
            String uuid = characteristic.getUuid().toString();
            Dialog_BTLE_Characteristic dialog_btle_characteristic = new Dialog_BTLE_Characteristic();
            dialog_btle_characteristic.setTitle(uuid);
            dialog_btle_characteristic.setService(btleGattService);
            dialog_btle_characteristic.setCharacteristic(characteristic);
            dialog_btle_characteristic.show(getFragmentManager(), "Dialog_BTLE_Characteristic");
        } else if (Utils.hasReadProperty(characteristic.getProperties()) != 0) {
            if (btleGattService != null) {
                btleGattService.readCharacteristic(characteristic);
            }
        } else if (Utils.hasNotifyProperty(characteristic.getProperties()) != 0) {
            if (btleGattService != null) {
                btleGattService.setCharacteristicNotification(characteristic, true);
            }
        }
        return false;
    }

    public void updateServices() {
        if (btleGattService != null) {
            services.clear();
            charByUuid.clear();
            charsByServiceUuid.clear();
            List<BluetoothGattService> servicesList = btleGattService.getSupportedGattServices();
            for (BluetoothGattService service : servicesList) {
                this.services.add(service);
                List<BluetoothGattCharacteristic> characteristicsList = service.getCharacteristics();
                ArrayList<BluetoothGattCharacteristic> newCharacteristicsList = new ArrayList<>();
                for (BluetoothGattCharacteristic characteristic : characteristicsList) {
                    charByUuid.put(characteristic.getUuid().toString(), characteristic);
                    newCharacteristicsList.add(characteristic);
                }
                charsByServiceUuid.put(service.getUuid().toString(), newCharacteristicsList);
            }
            if (servicesList != null && !servicesList.isEmpty()) {
                expandableListAdapter.notifyDataSetChanged();
            }
        }
    }

    public void updateCharacteristic() {
        expandableListAdapter.notifyDataSetChanged();
    }
}
