package android.kaviles.bletutorial;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Handler;

/**
 * Created by Kelvin on 4/20/16.
 */
@SuppressWarnings("ALL")
public class BtleScanner {

    private MainActivity mainActivity;

    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;

    private long scanPeriod;
    private int signalStrength;

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    final int new_rssi = rssi;
                    if (rssi > signalStrength) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mainActivity.addDevice(device, new_rssi);
                            }
                        });
                    }
                }
            };

    public BtleScanner(MainActivity mainActivity, long scanPeriod, int minSignalStrength) {
        this.mainActivity = mainActivity;
        mHandler = new Handler();
        this.scanPeriod = scanPeriod;
        this.signalStrength = minSignalStrength;
        final BluetoothManager bluetoothManager =
                (BluetoothManager) this.mainActivity.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
    }

    public boolean isScanning() {
        return mScanning;
    }

    public void start() {
        if (!Utils.checkBluetooth(mBluetoothAdapter)) {
            Utils.requestUserBluetooth(mainActivity);
            mainActivity.stopScan();
        } else {
            scanLeDevice(true);
        }
    }

    // If you want to scan for only specific types of peripherals,
    // you can instead call startLeScan(UUID[], BluetoothAdapter.LeScanCallback),
    // providing an array of UUID objects that specify the GATT services your app supports.
    private void scanLeDevice(final boolean enable) {
        if (enable && !mScanning) {
            Utils.toast(mainActivity.getApplicationContext(), "Starting BLE scan...");

            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Utils.toast(mainActivity.getApplicationContext(), "Stopping BLE scan...");
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    mainActivity.stopScan();
                }
            }, scanPeriod);
            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
//            mBluetoothAdapter.startLeScan(uuids, mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    public void stop() {
        scanLeDevice(false);
    }
}