package org.lmarz.streamtest;

import java.net.ServerSocket;
import java.net.Socket;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import dji.common.error.DJIError;
import dji.common.error.DJISDKError;
import dji.sdk.base.BaseComponent;
import dji.sdk.base.BaseProduct;
import dji.sdk.camera.VideoFeeder;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKInitEvent;
import dji.sdk.sdkmanager.DJISDKManager;
import dji.sdk.sdkmanager.DJISDKManager.SDKManagerCallback;

public class MainActivity extends Activity {

	private final String TAG = "streamtest";

	private Aircraft drone;
	private Socket client;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		Log.d(TAG, "Activity created");

		class RegisterCallback implements SDKManagerCallback {
			public void onRegister(DJIError error) {
				if(error == DJISDKError.REGISTRATION_SUCCESS) {
					Log.d(TAG, "Registration success");
					DJISDKManager.getInstance().startConnectionToProduct();
				} else {
					Log.e(TAG, error.getDescription());
				}
			}

			public void onProductConnect(BaseProduct product) {
				if(product instanceof Aircraft) {
					drone = (Aircraft) product;
					Log.d(TAG, "product connected");
				} else {
					drone = null;
				}
			}

			public void onProductDisconnect() {
				drone = null;
			}

			public void onProductChanged(BaseProduct product) {
				if(product instanceof Aircraft) {
					drone = (Aircraft) product;
					Log.d(TAG, "product changed (connected)");
				} else {
					drone = null;
					Log.d(TAG, "product changed (disconnected)");
				}
			}

			public void onComponentChange(BaseProduct.ComponentKey key, BaseComponent oldComponent, BaseComponent newComponent) {}
			public void onInitProcess(DJISDKInitEvent event, int totalProcess) {}
			public void onDatabaseDownloadProgress(long current, long total) {}
		}

		DJISDKManager.getInstance().registerApp(getApplicationContext(), new RegisterCallback());

		new Thread(() -> {
			try {
				ServerSocket server = new ServerSocket(9000);
				client = server.accept();
			} catch(Exception e) {
				Log.e(TAG, "server", e);
			}
		}).start();
	}

	public void btnStream(View v) {
		if(drone == null) {
			return;
		}

		VideoFeeder.getInstance().getPrimaryVideoFeed().addVideoDataListener((data, size) -> {
			Log.d(TAG, "received " + String.valueOf(size) + " bytes");
			if(client == null) {
				return;
			}
			try {
				client.getOutputStream().write(data, 0, size);
			} catch(Exception e) {
				Log.e(TAG, "write", e);
			}
		});

		Log.d(TAG, "Started stream");
	}
}
