package com.fitbit.bluetooth.fbgatt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import androidx.annotation.NonNull;
import com.fitbit.bluetooth.fbgatt.util.BluetoothManagerFacade;
import java.util.List;
import java.util.Set;
import androidx.annotation.Nullable;
import timber.log.Timber;

/**
 * Wrapper around the operation handling the initialisation of the connected device map.
 */
class AddKnownDevicesRunnable implements Runnable {

  private final Context context;
  private final BluetoothManagerFacade bluetoothManagerFacade;
  private final FitbitGatt fitbitGatt;

  AddKnownDevicesRunnable(@NonNull Context context, @NonNull BluetoothManagerFacade bluetoothManagerFacade, @NonNull FitbitGatt fitbitGatt) {
    this.context = context;
    this.bluetoothManagerFacade = bluetoothManagerFacade;
    this.fitbitGatt = fitbitGatt;
  }

  @Override
  public void run() {
    Timber.v("Adding connected or bonded devices");
    BluetoothAdapter adapter = bluetoothManagerFacade.getAdapter();
    if (adapter != null) {
      addConnectedDevices(bluetoothManagerFacade.getConnectedDevices(BluetoothProfile.GATT));
      addBondedDevices(adapter.getBondedDevices());
    }

    Timber.v("Added all connected or bonded devices");
  }

  private void addConnectedDevices(@Nullable List<BluetoothDevice> connectedDevices) {
    if (connectedDevices != null) {
      for (BluetoothDevice connectedDevice : connectedDevices) {
        FitbitBluetoothDevice fitbitBluetoothDevice = new FitbitBluetoothDevice(connectedDevice);
        fitbitBluetoothDevice.origin = FitbitBluetoothDevice.DeviceOrigin.CONNECTED;
        GattConnection connection = fitbitGatt.getConnectionMap().get(fitbitBluetoothDevice);
        if (null == connection) {
          Timber
              .v("Adding connected device named %s, with address %s", connectedDevice.getName(),
                  connectedDevice.getAddress());
          GattConnection conn = new GattConnection(fitbitBluetoothDevice,
              context.getMainLooper());
          fitbitGatt.getConnectionMap().put(fitbitBluetoothDevice, conn);
          conn.initGattForConnectedDevice();
          fitbitGatt.notifyListenersOfConnectionAdded(conn);
        } else {
          connection.initGattForConnectedDevice();
        }
      }
    } else {
      Timber.d("ConnectedDevices was null");
    }
  }

  private void addBondedDevices(@Nullable Set<BluetoothDevice> bondedDevices) {
    if (bondedDevices != null) {
      for (BluetoothDevice bondedDevice : bondedDevices) {
        FitbitBluetoothDevice fitBluetoothDevice = new FitbitBluetoothDevice(bondedDevice);
        fitBluetoothDevice.origin = FitbitBluetoothDevice.DeviceOrigin.BONDED;
        if (null == fitbitGatt.getConnectionMap().get(fitBluetoothDevice)) {
          GattConnection conn = new GattConnection(fitBluetoothDevice, context.getMainLooper());
          Timber.v("Adding bonded device named %s, with address %s", bondedDevice.getName(),
              bondedDevice.getAddress());
          fitbitGatt.getConnectionMap().put(fitBluetoothDevice, conn);
          fitbitGatt.notifyListenersOfConnectionAdded(conn);
        }
      }
    } else {
      Timber.d("BondedDevices was null");
    }
  }
}
