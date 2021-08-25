package com.fitbit.bluetooth.fbgatt;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import com.fitbit.bluetooth.fbgatt.util.BluetoothManagerFacade;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.util.collections.Sets;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class AddConnectedDevicesRunnableTest {

  private final BluetoothManagerFacade mockBtManagerFacade = mock(BluetoothManagerFacade.class);
  private final BluetoothAdapter mockBtAdapter = mock(BluetoothAdapter.class);

  private final FitbitBluetoothDevice mockFitbitBtDevice = mock(FitbitBluetoothDevice.class);
  private final GattConnection mockGattConnection = mock(GattConnection.class);

  private final BluetoothDevice mockBtDevice = mock(BluetoothDevice.class);
  private final List<BluetoothDevice> mockBtDeviceList = Collections.singletonList(mockBtDevice);
  private final Set<BluetoothDevice> mockBtDeviceSet = Sets.newSet(mockBtDevice);
  private final ConcurrentHashMap<FitbitBluetoothDevice, GattConnection> mockConnectionMap = mock(ConcurrentHashMap.class);

  private final FitbitGatt fitbitGattMock = mock(FitbitGatt.class);

  private AddKnownDevicesRunnable sut;

  @Before
  public void before() {
    Context context = ApplicationProvider.getApplicationContext();
    sut = new AddKnownDevicesRunnable(context, mockBtManagerFacade, fitbitGattMock);
  }

  @Test
  public void shouldNotCrashWhenBtManagerIsNull() {
    doReturn(null).when(mockBtManagerFacade).getAdapter();

    sut.run();

    verifyNoInteractions(mockBtAdapter);
    verifyNoInteractions(mockGattConnection);
    verifyNoInteractions(fitbitGattMock);
  }

  @Test
  public void shouldNotCrashWhenDeviceListsAreNull() {
    doReturn(mockBtAdapter).when(mockBtManagerFacade).getAdapter();
    doReturn(null).when(mockBtManagerFacade).getConnectedDevices(eq(BluetoothProfile.GATT));
    doReturn(null).when(mockBtAdapter).getBondedDevices();

    sut.run();

    verify(mockBtManagerFacade).getConnectedDevices(eq(BluetoothProfile.GATT));
    verify(fitbitGattMock, never()).notifyListenersOfConnectionAdded(any());
  }

  @Test
  public void shouldAddDeviceWhenConnectedDevicesRetrieved() {
    doReturn(mockBtAdapter).when(mockBtManagerFacade).getAdapter();
    doReturn(mockBtDeviceList).when(mockBtManagerFacade).getConnectedDevices(eq(BluetoothProfile.GATT));
    doReturn(mockGattConnection).when(mockConnectionMap).get(any(FitbitBluetoothDevice.class));
    doReturn(null).when(mockBtAdapter).getBondedDevices();
    doReturn(mockConnectionMap).when(fitbitGattMock).getConnectionMap();

    sut.run();

    verify(mockGattConnection).initGattForConnectedDevice();
  }

  @Test
  public void shouldNotifyListenersOfNewConnection() {
    doReturn(mockBtAdapter).when(mockBtManagerFacade).getAdapter();
    doReturn(mockBtDeviceList).when(mockBtManagerFacade).getConnectedDevices(eq(BluetoothProfile.GATT));
    doReturn(null).when(mockConnectionMap).get(any(FitbitBluetoothDevice.class));
    doReturn(null).when(mockBtAdapter).getBondedDevices();
    doReturn(mockConnectionMap).when(fitbitGattMock).getConnectionMap();

    sut.run();

    verify(fitbitGattMock).notifyListenersOfConnectionAdded(any());
  }

  @Test
  public void shouldAddConnectionForBondedDevice() {
    doReturn(mockBtAdapter).when(mockBtManagerFacade).getAdapter();
    doReturn(null).when(mockBtManagerFacade).getConnectedDevices(eq(BluetoothProfile.GATT));
    doReturn(null).when(mockConnectionMap).get(any(FitbitBluetoothDevice.class));
    doReturn(mockBtDeviceSet).when(mockBtAdapter).getBondedDevices();
    doReturn(mockConnectionMap).when(fitbitGattMock).getConnectionMap();

    sut.run();

    verify(fitbitGattMock).notifyListenersOfConnectionAdded(any());
  }
}