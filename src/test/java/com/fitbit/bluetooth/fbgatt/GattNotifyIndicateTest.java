/*
 * Copyright 2019 Fitbit, Inc. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.fitbit.bluetooth.fbgatt;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.Context;
import android.os.Looper;
import androidx.test.core.app.ApplicationProvider;
import com.fitbit.bluetooth.fbgatt.tx.mocks.SubscribeToCharacteristicNotificationsMockTransaction;
import com.fitbit.bluetooth.fbgatt.tx.mocks.UnSubscribeToGattCharacteristicNotificationsMockTransaction;
import com.fitbit.bluetooth.fbgatt.util.LooperWatchdog;
import java.util.UUID;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;

@RunWith(RobolectricTestRunner.class)
@Config(minSdk = 21)
@Ignore
public class GattNotifyIndicateTest {
  private GattConnection conn;
  private FitbitBluetoothDevice device;

  @Before
  public void before() {
    Context context = ApplicationProvider.getApplicationContext();
    FitbitGatt.getInstance().startGattClient(context);
    device = mock(FitbitBluetoothDevice.class);
    conn = spy(new GattConnection(device, ApplicationProvider.getApplicationContext().getMainLooper()));
    conn.setMockMode(true);
    FitbitGatt.getInstance().putConnectionIntoDevices(device, conn);
  }

  @Test
  public void subscribeToNotificationTest() {
    final byte[] fakeData = new byte[] {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j'};
    BluetoothGattCharacteristic characteristic =
        new BluetoothGattCharacteristic(
            UUID.randomUUID(),
            BluetoothGattCharacteristic.PROPERTY_WRITE,
            BluetoothGattDescriptor.PERMISSION_READ
                | BluetoothGattDescriptor.PERMISSION_READ_ENCRYPTED);
    SubscribeToCharacteristicNotificationsMockTransaction subscribeChar =
        new SubscribeToCharacteristicNotificationsMockTransaction(
            conn,
            GattState.ENABLE_CHARACTERISTIC_NOTIFICATION_SUCCESS,
            characteristic,
            fakeData,
            false);
    conn.runTx(
        subscribeChar,
        result -> {
          assert (result.resultStatus.equals(TransactionResult.TransactionResultStatus.SUCCESS)
              && result.resultState.equals(subscribeChar.getSuccessState()));
        });
  }

  @Test
  public void unSubscribeToNotificationTest() {
    final byte[] fakeData = new byte[] {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j'};
    BluetoothGattCharacteristic characteristic =
        new BluetoothGattCharacteristic(
            UUID.randomUUID(),
            BluetoothGattCharacteristic.PROPERTY_WRITE,
            BluetoothGattDescriptor.PERMISSION_READ
                | BluetoothGattDescriptor.PERMISSION_READ_ENCRYPTED);
    UnSubscribeToGattCharacteristicNotificationsMockTransaction unSubscribeChar =
        new UnSubscribeToGattCharacteristicNotificationsMockTransaction(
            conn,
            GattState.ENABLE_CHARACTERISTIC_NOTIFICATION_SUCCESS,
            characteristic,
            fakeData,
            false);
    conn.runTx(
        unSubscribeChar,
        result -> {
          assert (result.resultStatus.equals(TransactionResult.TransactionResultStatus.SUCCESS)
              && result.resultState.equals(unSubscribeChar.getSuccessState()));
        });
  }

  @Test
  public void failToSubscribeToNotificationTest() {
    final byte[] fakeData = new byte[] {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j'};
    BluetoothGattCharacteristic characteristic =
        new BluetoothGattCharacteristic(
            UUID.randomUUID(),
            BluetoothGattCharacteristic.PROPERTY_WRITE,
            BluetoothGattDescriptor.PERMISSION_READ
                | BluetoothGattDescriptor.PERMISSION_READ_ENCRYPTED);
    SubscribeToCharacteristicNotificationsMockTransaction subscribeChar =
        new SubscribeToCharacteristicNotificationsMockTransaction(
            conn,
            GattState.ENABLE_CHARACTERISTIC_NOTIFICATION_SUCCESS,
            characteristic,
            fakeData,
            true);
    conn.runTx(
        subscribeChar,
        result -> {
          assert (result.resultStatus.equals(TransactionResult.TransactionResultStatus.TIMEOUT));
        });
  }

  @Test
  public void failToUnSubscribeToNotificationTest() {
    final byte[] fakeData = new byte[] {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j'};
    BluetoothGattCharacteristic characteristic =
        new BluetoothGattCharacteristic(
            UUID.randomUUID(),
            BluetoothGattCharacteristic.PROPERTY_WRITE,
            BluetoothGattDescriptor.PERMISSION_READ
                | BluetoothGattDescriptor.PERMISSION_READ_ENCRYPTED);
    UnSubscribeToGattCharacteristicNotificationsMockTransaction unSubscribeChar =
        new UnSubscribeToGattCharacteristicNotificationsMockTransaction(
            conn,
            GattState.ENABLE_CHARACTERISTIC_NOTIFICATION_SUCCESS,
            characteristic,
            fakeData,
            true);
    conn.runTx(
        unSubscribeChar,
        result -> {
          assert (result.resultStatus.equals(TransactionResult.TransactionResultStatus.TIMEOUT));
        });
  }
}
