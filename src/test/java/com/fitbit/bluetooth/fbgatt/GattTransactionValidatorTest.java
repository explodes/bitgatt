/*
 * Copyright 2019 Fitbit, Inc. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.fitbit.bluetooth.fbgatt;

import androidx.test.core.app.ApplicationProvider;
import com.fitbit.bluetooth.fbgatt.tx.SetClientConnectionStateTransaction;
import com.fitbit.bluetooth.fbgatt.tx.mocks.GattConnectMockTransaction;
import com.fitbit.bluetooth.fbgatt.tx.mocks.GattDisconnectMockTransaction;
import com.fitbit.bluetooth.fbgatt.tx.mocks.GattServerDisconnectMockTransaction;
import com.fitbit.bluetooth.fbgatt.tx.mocks.ReadGattCharacteristicMockTransaction;
import com.fitbit.bluetooth.fbgatt.tx.mocks.WriteGattCharacteristicMockTransaction;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Handler;
import android.os.Looper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.UUID;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowBluetoothDevice;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class GattTransactionValidatorTest {

    private static final String MOCK_ADDRESS = "02:00:00:00:00:00";
    private GattConnection conn;
    private GattServerConnection serverConnection;
    private FitbitBluetoothDevice device;

    @Before
    public void before() {
        Handler mockHandler = new Handler();
        Looper mockLooper = ApplicationProvider.getApplicationContext().getMainLooper();

        device = new FitbitBluetoothDevice(MOCK_ADDRESS, "fooDevice", ShadowBluetoothDevice.newInstance("02:00:00:00:00:00"));
        conn = spy(new GattConnection(device, mockLooper));
        conn.setMockMode(true);
        when(conn.getMainHandler()).thenReturn(mockHandler);
        FitbitGatt.getInstance().putConnectionIntoDevices(device, conn);
        serverConnection = spy(new GattServerConnection(null, mockLooper));
        serverConnection.setMockMode(true);
        when(serverConnection.getMainHandler()).thenReturn(mockHandler);
        FitbitGatt.getInstance().setGattServerConnection(serverConnection);
        FitbitGatt.getInstance().setStarted(true);
        FitbitGatt.getInstance().setAppContext(ApplicationProvider.getApplicationContext());
    }

    @After
    public void after() {
        FitbitGatt.setInstance(null);
    }

    @Test
    public void testCanNotEnterTransactionWithBluetoothOff() {
        conn.resetState();
        conn.setState(GattState.BT_OFF);
        GattStateTransitionValidator<GattClientTransaction> validator = new GattStateTransitionValidator<GattClientTransaction>();
        GattConnectMockTransaction tx = new GattConnectMockTransaction(conn, GattState.CONNECTED, false);
        GattStateTransitionValidator.GuardState guardState = validator.checkTransaction(conn.getGattState(), tx);
        Assert.assertEquals(GattStateTransitionValidator.GuardState.INVALID_TARGET_STATE, guardState);
    }

    @Test
    public void testConnectionTransactionValidator() {
        conn.resetState();
        GattStateTransitionValidator<GattClientTransaction> validator = new GattStateTransitionValidator<GattClientTransaction>();
        GattConnectMockTransaction tx = new GattConnectMockTransaction(conn, GattState.CONNECTED, false);
        GattStateTransitionValidator.GuardState guardState = validator.checkTransaction(conn.getGattState(), tx);
        Assert.assertEquals(GattStateTransitionValidator.GuardState.OK, guardState);
    }

    @Test
    public void testConnectionWhileDisconnectedTransactionValidator() {
        conn.resetState();
        GattStateTransitionValidator<GattClientTransaction> validator = new GattStateTransitionValidator<GattClientTransaction>();
        GattConnectMockTransaction tx = new GattConnectMockTransaction(conn, GattState.CONNECTED, false);
        GattStateTransitionValidator.GuardState guardState = validator.checkTransaction(conn.getGattState(), tx);
        Assert.assertEquals(GattStateTransitionValidator.GuardState.OK, guardState);
    }

    @Test
    public void testDisconnectWhileConnectedTransactionValidator() {
        conn.setState(GattState.IDLE);
        GattStateTransitionValidator<GattClientTransaction> validator = new GattStateTransitionValidator<GattClientTransaction>();
        GattDisconnectMockTransaction tx = new GattDisconnectMockTransaction(conn, GattState.DISCONNECTED, false);
        GattStateTransitionValidator.GuardState guardState = validator.checkTransaction(conn.getGattState(), tx);
        Assert.assertEquals(GattStateTransitionValidator.GuardState.OK, guardState);
    }

    @Test
    public void testConnectAfterDisconnectedTransactionValidator() {
        conn.setState(GattState.DISCONNECTED);
        GattStateTransitionValidator<GattClientTransaction> validator = new GattStateTransitionValidator<GattClientTransaction>();
        GattConnectMockTransaction tx = new GattConnectMockTransaction(conn, GattState.CONNECTED, false);
        GattStateTransitionValidator.GuardState guardState = validator.checkTransaction(conn.getGattState(), tx);
        Assert.assertEquals(GattStateTransitionValidator.GuardState.OK, guardState);
    }

    @Test
    @Ignore
    public void testAnythingOtherThanConnectWhileDisconnectedTransactionValidator() {
        conn.resetState();
        GattStateTransitionValidator<GattClientTransaction> validator = new GattStateTransitionValidator<GattClientTransaction>();
        ReadGattCharacteristicMockTransaction tx = new ReadGattCharacteristicMockTransaction(conn,
                GattState.READ_CHARACTERISTIC_SUCCESS,
                new BluetoothGattCharacteristic(UUID.randomUUID(),
                        BluetoothGattCharacteristic.PERMISSION_READ,
                        BluetoothGattCharacteristic.PROPERTY_INDICATE),
                new byte[]{0x12, 0x14},
                false);
        GattStateTransitionValidator.GuardState guardState = validator.checkTransaction(conn.getGattState(), tx);
        Assert.assertEquals(GattStateTransitionValidator.GuardState.INVALID_TARGET_STATE, guardState);
    }

    @Test
    public void testSetStateInErrorConditionValidatorTest() {
        conn.resetState();
        conn.setState(GattState.WRITE_CHARACTERISTIC_FAILURE);
        GattStateTransitionValidator<GattClientTransaction> validator = new GattStateTransitionValidator<GattClientTransaction>();
        SetClientConnectionStateTransaction tx = new SetClientConnectionStateTransaction(conn, GattState.GATT_CONNECTION_STATE_SET_SUCCESSFULLY, GattState.IDLE);
        GattStateTransitionValidator.GuardState guardState = validator.checkTransaction(conn.getGattState(), tx);
        Assert.assertEquals(GattStateTransitionValidator.GuardState.OK, guardState);
    }

    @Test
    public void testServerDisconnectionTransactionValidator() {
        conn.resetState();
        conn.setState(GattState.CONNECTED);
        GattStateTransitionValidator<GattServerTransaction> validator = new GattStateTransitionValidator<GattServerTransaction>();
        GattServerDisconnectMockTransaction tx = new GattServerDisconnectMockTransaction(serverConnection, GattState.DISCONNECTED, device, false);
        GattStateTransitionValidator.GuardState guardState = validator.checkTransaction(conn.getGattState(), tx);
        Assert.assertEquals(GattStateTransitionValidator.GuardState.OK, guardState);
    }

    @Test
    @Ignore
    public void testSettingTransactionTimeout(){
        conn.resetState();
        conn.setState(GattState.CONNECTED);
        WriteGattCharacteristicMockTransaction writeGattCharacteristicMockTransaction = new WriteGattCharacteristicMockTransaction(conn, GattState.WRITE_CHARACTERISTIC_SUCCESS, new BluetoothGattCharacteristic(UUID.randomUUID(),
                BluetoothGattCharacteristic.PERMISSION_READ,
                BluetoothGattCharacteristic.PROPERTY_INDICATE),
                new byte[] { 0x12, 0x14},
                false, 100L);
        Assert.assertEquals(100L, writeGattCharacteristicMockTransaction.getTimeout());
    }
}
