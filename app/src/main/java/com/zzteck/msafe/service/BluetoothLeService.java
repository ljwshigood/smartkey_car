/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zzteck.msafe.service;

import java.util.List;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;


import com.zzteck.msafe.bean.MsgEvent;
import com.zzteck.msafe.impl.IDismissListener;
import com.zzteck.msafe.util.GattAttributes;

import org.simple.eventbus.EventBus;

/**
 * Service for managing connection and data communication with a GATT server
 * hosted on a given Bluetooth LE device.
 */
@SuppressLint("NewApi")
public class BluetoothLeService extends Service {
	private final static String TAG = BluetoothLeService.class.getSimpleName();

	private BluetoothManager mBluetoothManager;
	private BluetoothAdapter mBluetoothAdapter;
	private String mBluetoothDeviceAddress;
	private BluetoothGatt mBluetoothGatt;
	private int mConnectionState = STATE_DISCONNECTED;

	private static final int STATE_DISCONNECTED = 0;
	private static final int STATE_CONNECTING = 1;
	private static final int STATE_CONNECTED = 2;

	public final static String ACTION_NOTIFY_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_NOTIFY_DATA_AVAILABLE";
	public final static String ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
	public final static String ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
	public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
	public final static String ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
	public final static String EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA";

	public final static String ACTION_GATT_RSSI = "com.example.bluetooth.le.ACTION_GATT_RSSI";

	public static final UUID SERVIE_UUID = UUID.fromString("00001802-0000-1000-8000-00805f9b34fb");

	public static final UUID BATTERY_SERVICE_UUID = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb");

	public static final UUID BATTERY_CHACTER_UUID = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb") ;

	public final static UUID UUID_HEART_RATE_MEASUREMENT = UUID.fromString(GattAttributes.HEART_RATE_MEASUREMENT);

	public Context mContext;

	public final static String BATTERY_DATA = "com.example.bluetooth.le.battery" ;

	public IDismissListener mIDismissListener ;

	public IDismissListener getmIDismissListener() {
		return mIDismissListener;
	}

	public void setmIDismissListener(IDismissListener mIDismissListener) {
		this.mIDismissListener = mIDismissListener;
	}

	private void showMessage(String msg) {
		Log.e(TAG, msg);
	}

	public void writeCharacter(String address) {

		BluetoothGattService alarmService =  null;

		BluetoothGattCharacteristic alarmCharacter = null;

		if(mBluetoothGatt != null){
			alarmService = mBluetoothGatt.getService(SERVIE_UUID);
		}
		if (alarmService == null) {
			showMessage("link loss Alert service not found!");
			return;
		}
		alarmCharacter = alarmService.getCharacteristic(UUID.fromString("00002a06-0000-1000-8000-00805f9b34fb"));
		if (alarmCharacter == null) {
			connect(address);
			showMessage("link loss Alert Level charateristic not found!");
			return;
		}
		alarmCharacter.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
		byte[] value = { 0x01 };
		alarmCharacter.setValue(value);
		mBluetoothGatt.writeCharacteristic(alarmCharacter);
	}

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if(mIDismissListener != null){
				mIDismissListener.dismiss();
				//Toast.makeText(BluetoothLeService.this, "try again!", 1).show();
			}
		}

	};

	public void readBatteryCharacteristic(){
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			Log.w(TAG, "BluetoothAdapter not initialized");
			return;
		}
		BluetoothGattService alarmService = null;

		BluetoothGattCharacteristic batteryCharacter = null;

		if(mBluetoothGatt != null){
			alarmService = mBluetoothGatt.getService(BATTERY_SERVICE_UUID);
		}

		if(alarmService != null){
			batteryCharacter = alarmService.getCharacteristic(UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb"));
		}

		if(batteryCharacter != null){
			mBluetoothGatt.readCharacteristic(batteryCharacter);
		}
	}

	private void broadcastDeviceBleUpdate(final String action, String device) {
		final Intent intent = new Intent(action);
		intent.putExtra(BluetoothDevice.EXTRA_DEVICE, device);
		sendBroadcast(intent);
	}

	public final static String ACTION_READ_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_READ_DATA_AVAILABLE";

	private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status,int newState) {
			String intentAction;
			System.out.println("=======status:" + status);
			if (newState == BluetoothProfile.STATE_CONNECTED) {
				intentAction = ACTION_GATT_CONNECTED;
				mConnectionState = STATE_CONNECTED;
				broadcastUpdate(intentAction);
				Log.i(TAG, "Connected to GATT server.");
				// Attempts to discover services after successful connection.
				Log.i(TAG, "Attempting to start service discovery:"+ mBluetoothGatt.discoverServices());

			} else if(status == 133){
				mHandler.sendEmptyMessage(0);
			}else if (status != 133 && newState == BluetoothProfile.STATE_DISCONNECTED) {
				intentAction = ACTION_GATT_DISCONNECTED;
				mConnectionState = STATE_DISCONNECTED;
				Log.e(TAG, "Disconnected from GATT server.");
				close();
				broadcastDeviceBleUpdate(intentAction, gatt.getDevice().getAddress());
			}
		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				EventBus.getDefault().post(new MsgEvent("",6));
				broadcastDeviceBleUpdate(ACTION_GATT_SERVICES_DISCOVERED,gatt.getDevice().getAddress());
			} else {
				Log.w(TAG, "onServicesDiscovered received: " + status);
			}
		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt,
										 BluetoothGattCharacteristic characteristic, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				broadcastUpdate(ACTION_READ_DATA_AVAILABLE, characteristic,gatt.getDevice().getAddress());
			}
		}

		@Override
		public void onDescriptorWrite(BluetoothGatt gatt,
									  BluetoothGattDescriptor descriptor, int status) {
		}

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt,BluetoothGattCharacteristic characteristic) {
			broadcastUpdate(ACTION_NOTIFY_DATA_AVAILABLE, characteristic,gatt.getDevice().getAddress());
		}

		@Override
		public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
			broadcastRssi(ACTION_GATT_RSSI, rssi);
		}

		int i;
		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt,
										  BluetoothGattCharacteristic characteristic, int status) {
		};
	};

	private void broadcastRssi(String action, int rssi) {
		Intent intent = new Intent(action);
		intent.putExtra(EXTRA_DATA, rssi);
		sendBroadcast(intent);
	}

	private void broadcastUpdateAndAddress(final String action,String address) {
		final Intent intent = new Intent(action);
		sendBroadcast(intent);
	}


	private void broadcastUpdate(final String action) {
		final Intent intent = new Intent(action);
		sendBroadcast(intent);
	}

	private void broadcastUpdate(final String action,final BluetoothGattCharacteristic characteristic,String device) {
		final Intent intent = new Intent(action);
		if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
			int flag = characteristic.getProperties();
			int format = -1;
			if ((flag & 0x01) != 0) {
				format = BluetoothGattCharacteristic.FORMAT_UINT16;
				Log.d(TAG, "Heart rate format UINT16.");
			} else {
				format = BluetoothGattCharacteristic.FORMAT_UINT8;
				Log.d(TAG, "Heart rate format UINT8.");
			}
			final int heartRate = characteristic.getIntValue(format, 1);
			System.out.println("Received heart rate: %d" + heartRate);
			Log.d(TAG, String.format("Received heart rate: %d", heartRate));
			intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));
		} else if(BATTERY_CHACTER_UUID.equals(characteristic.getUuid())){
			final byte[] data = characteristic.getValue();
			if (data != null && data.length > 0) {
				final StringBuilder stringBuilder = new StringBuilder(data.length);
				for (byte byteChar : data)
					stringBuilder.append(String.format("%02X ", byteChar));

				Log.e(TAG,"############################broadcastUpdate : "+data[0]);
				intent.putExtra(BATTERY_DATA,data);
				intent.putExtra(BluetoothDevice.EXTRA_DEVICE, device);
			}
		}else {
			// For all other profiles, writes the data formatted in HEX.
			/*final byte[] data = characteristic.getValue();
			for (int i = 0; i < data.length; i++) {
				Log.v("ww", "sa:" + data[i]);
			}
			if (data != null && data.length > 0) {
				intent.putExtra(EXTRA_DATA, data);
			}*/

			final byte[] data = characteristic.getValue();
			if (data != null && data.length > 0) {
				final StringBuilder stringBuilder = new StringBuilder(data.length);
				for (byte byteChar : data)
					stringBuilder.append(String.format("%02X ", byteChar));
				intent.putExtra(EXTRA_DATA, stringBuilder.toString());
				intent.putExtra(BluetoothDevice.EXTRA_DEVICE, device);
			}

		}
		sendBroadcast(intent);
	}

	public class LocalBinder extends Binder {
		public BluetoothLeService getService() {
			return BluetoothLeService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		close();
		return super.onUnbind(intent);
	}

	private final IBinder mBinder = new LocalBinder();

	/**
	 * Initializes a reference to the local Bluetooth adapter.
	 *
	 * @return Return true if the initialization is successful.
	 */
	public boolean initialize() {
		if (mBluetoothManager == null) {
			mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
			if (mBluetoothManager == null) {
				Log.e(TAG, "Unable to initialize BluetoothManager.");
				return false;
			}
		}

		mBluetoothAdapter = mBluetoothManager.getAdapter();
		if (mBluetoothAdapter == null) {
			Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
			return false;
		}

		return true;
	}

	/**
	 * Connects to the GATT server hosted on the Bluetooth LE device.
	 *
	 * @param address
	 *            The device address of the destination device.
	 *
	 * @return Return true if the connection is initiated successfully. The
	 *         connection result is reported asynchronously through the
	 *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
	 *         callback.
	 */
	public boolean connect(final String address) {
		if (mBluetoothAdapter == null || address == null) {
			Log.w(TAG,
					"BluetoothAdapter not initialized or unspecified address.");
			return false;
		}

		// Previously connected device. Try to reconnect.
		if (mBluetoothDeviceAddress != null
				&& address.equals(mBluetoothDeviceAddress)
				&& mBluetoothGatt != null) {
			Log.d(TAG,
					"Trying to use an existing mBluetoothGatt for connection.");
			if (mBluetoothGatt.connect()) {
				mConnectionState = STATE_CONNECTING;
				return true;
			} else {
				return false;
			}
		}

		final BluetoothDevice device = mBluetoothAdapter
				.getRemoteDevice(address);
		if (device == null) {
			Log.w(TAG, "Device not found.  Unable to connect.");
			return false;
		}
		// We want to directly connect to the device, so we are setting the
		// autoConnect
		// parameter to false.
		mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
		Log.d(TAG, "Trying to create a new connection.");
		mBluetoothDeviceAddress = address;
		mConnectionState = STATE_CONNECTING;
		return true;
	}

	/**
	 * Disconnects an existing connection or cancel a pending connection. The
	 * disconnection result is reported asynchronously through the
	 * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
	 * callback.
	 */
	public void disconnect() {
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			Log.w(TAG, "BluetoothAdapter not initialized");
			return;
		}
		mBluetoothGatt.disconnect();
	}

	/**
	 * After using a given BLE device, the app must call this method to ensure
	 * resources are released properly.
	 */
	public void close() {
		if (mBluetoothGatt == null) {
			return;
		}
		mBluetoothGatt.close();
		mBluetoothGatt = null;
	}
	int count;
	public boolean wirteCharacteristic(BluetoothGattCharacteristic characteristic) {

		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			Log.w(TAG, "BluetoothAdapter not initialized");
			return false;
		}
		count++;
		Log.w("qh", "qh:+count:" + count);
		//	boolean flag = mBluetoothGatt.executeReliableWrite();

		mBluetoothGatt.writeCharacteristic(characteristic);

		return true;
	}

	/**
	 * Request a read on a given {@code BluetoothGattCharacteristic}. The read
	 * result is reported asynchronously through the
	 * {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
	 * callback.
	 *
	 * @param characteristic
	 *            The characteristic to read from.
	 */
	public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			Log.w(TAG, "BluetoothAdapter not initialized");
			return;
		}
		mBluetoothGatt.readCharacteristic(characteristic);
	}

	/**
	 * Enables or disables notification on a give characteristic.
	 *
	 * @param characteristic
	 *            Characteristic to act on.
	 * @param enabled
	 *            If true, enable notification. False otherwise.
	 */
	public void setCharacteristicNotification(
			BluetoothGattCharacteristic characteristic, boolean enabled) {
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			Log.w(TAG, "BluetoothAdapter not initialized");
			return;
		}
		mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
		BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID
				.fromString(GattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
		if (descriptor != null) {
			System.out.println("write descriptor");
			descriptor
					.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
			mBluetoothGatt.writeDescriptor(descriptor);
		}
	}

	/**
	 * Retrieves a list of supported GATT services on the connected device. This
	 * should be invoked only after {@code BluetoothGatt#discoverServices()}
	 * completes successfully.
	 *
	 * @return A {@code List} of supported services.
	 */
	public List<BluetoothGattService> getSupportedGattServices() {
		if (mBluetoothGatt == null)
			return null;

		return mBluetoothGatt.getServices();
	}

	/**
	 * Read the RSSI for a connected remote device.
	 * */
	public boolean getRssiVal() {
		if (mBluetoothGatt == null)
			return false;

		return mBluetoothGatt.readRemoteRssi();
	}

	public boolean isConnect(){
		boolean isRet = true ;
		if(mBluetoothGatt == null || !mBluetoothGatt.connect()){
			isRet = false ;
		}
		return isRet ;
	}

}
