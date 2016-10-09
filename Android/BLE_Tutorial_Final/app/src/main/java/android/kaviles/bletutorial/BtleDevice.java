package android.kaviles.bletutorial;

import android.bluetooth.BluetoothDevice;

/**
 * Created by Kelvin on 5/8/16.
 *
 * Seems like this class is only useful if you really want to display rssi info.
 *
 * This class enables access to the standard Android BluetoothDevice API,
 * bundled up with each devices corresponding rssi given from the original
 * `BluetoothAdapter.LeScanCallback`.
 */
public class BtleDevice {

    private BluetoothDevice bluetoothDevice;
    private int rssi;

    public BtleDevice(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
    }

    public String getAddress() {
        return bluetoothDevice.getAddress();
    }

    public String getName() {
        return bluetoothDevice.getName();
    }

    public int getRSSI() {
        return rssi;
    }

    public void setRSSI(int rssi) {
        this.rssi = rssi;
    }
}
