package com.jozibear247_cab.driver.gcm;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gcm.GCMRegistrar;
import com.jozibear247_cab.driver.utills.AndyUtils;
import com.jozibear247_cab.driver.utills.AppLog;

public class GCMRegisterHendler {

	private Activity activity;

	public GCMRegisterHendler(Activity activity,
			BroadcastReceiver mHandleMessageReceiver) {
		try {
			this.activity = activity;
			checkNotNull(CommonUtilities.SENDER_ID, "SENDER_ID");

			// Make sure the device has the proper dependencies.
			GCMRegistrar.checkDevice(activity);
			// Make sure the manifest was properly set - comment out this line
			// while developing the app, then uncomment it when it's ready.
			GCMRegistrar.checkManifest(activity);

			// mDisplay = (TextView) findViewById(R.id.display);
			activity.registerReceiver(mHandleMessageReceiver, new IntentFilter(
					CommonUtilities.DISPLAY_MESSAGE_ACTION));
			final String regId = GCMRegistrar.getRegistrationId(activity);
			if (TextUtils.isEmpty(regId)) {
				// Automatically registers application on startup.
				GCMRegistrar.register(activity, CommonUtilities.SENDER_ID);
			} else {
				AppLog.Log("GCM", "Already Device registered: regId = " + regId);
				publishResults(regId, Activity.RESULT_OK);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void checkNotNull(Object reference, String name) {
		if (reference == null) {
			throw new NullPointerException(
					"sender id is null please recompile the app");
		}
	}

	private void publishResults(String regid, int result) {
		AndyUtils.removeSimpleProgressDialog();
		Intent intent = new Intent(CommonUtilities.DISPLAY_MESSAGE_ACTION);
		intent.putExtra(CommonUtilities.RESULT, result);
		intent.putExtra(CommonUtilities.REGID, regid);
		activity.sendBroadcast(intent);
	}
}
