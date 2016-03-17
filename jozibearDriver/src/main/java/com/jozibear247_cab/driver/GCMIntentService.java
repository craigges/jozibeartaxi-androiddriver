/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jozibear247_cab.driver;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;
import com.jozibear247_cab.driver.gcm.CommonUtilities;
import com.jozibear247_cab.driver.utills.AndyConstants;
import com.jozibear247_cab.driver.utills.AppLog;
import com.jozibear247_cab.driver.utills.PreferenceHelper;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * IntentService responsible for handling GCM messages.
 */
public class GCMIntentService extends GCMBaseIntentService {

	private static final String TAG = "GCMIntentService";
//	private PreferenceHelper preferenceHelper;

	public GCMIntentService() {
		super(CommonUtilities.SENDER_ID);
	}

	@Override
	protected void onRegistered(Context context, String registrationId) {
		AppLog.Log(TAG, "Device registered: regId = " + registrationId);
		CommonUtilities.displayMessage(context, "Device Registerd");
		PreferenceHelper.getInstance(context).putDeviceToken(registrationId);
		publishResults(registrationId, Activity.RESULT_OK);
		// GCMRegisterHendler.onRegComplete(registrationId);
	}

	@Override
	protected void onUnregistered(Context context, String registrationId) {
		Log.i(TAG, "Device unregistered");
		CommonUtilities.displayMessage(context, "Device Unregistered");
		if (GCMRegistrar.isRegisteredOnServer(context)) {

		} else {
			// This callback results from the call to unregister made on
			// ServerUtilities when the registration to the server failed.
			AppLog.Log(TAG, "Ignoring unregister callback");
		}
	}

	@Override
	protected void onMessage(Context context, Intent intent) {
		AppLog.Log(TAG, "Received bundle : " + intent.getExtras());
		// String message = getString(R.string.gcm_message);
		String message = intent.getExtras().getString("message");
		AppLog.Log(TAG, "Message is: --->" + message);
		  Log.d("mahi", "push notification "+intent.getExtras());
		String team = intent.getExtras().getString("team");
		Intent pushIntent = new Intent(AndyConstants.NEW_REQUEST);
		pushIntent.putExtra(AndyConstants.NEW_REQUEST, team);
		// String messageBedge = intent.getExtras().getString("bedge");
		CommonUtilities.displayMessage(context, message);
		// notifies user
		try {
			JSONObject jsonObject = new JSONObject(team);
			if (jsonObject.getInt(AndyConstants.Params.UNIQUE_ID) == 1) {
				generateNotification(context, message);
			} else if (jsonObject.getInt(AndyConstants.Params.UNIQUE_ID) == 2) {
//				preferenceHelper = PreferenceHelper.getInstance(context);
				PreferenceHelper.getInstance(context).clearRequestData();
				Intent i = new Intent("CANCEL_REQUEST");
				context.sendBroadcast(i);
				generateNotification(context, message);
			} else if (jsonObject.getInt(AndyConstants.Params.UNIQUE_ID) == 4) {
				generateNotification(context, message);
			}
			else {
//				preferenceHelper = PreferenceHelper.getInstance(context);
				PreferenceHelper.getInstance(context).clearRequestData();
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		LocalBroadcastManager.getInstance(context).sendBroadcast(pushIntent);
	}

	@Override
	protected void onDeletedMessages(Context context, int total) {
		AppLog.Log(TAG, "Received deleted messages notification");
		String message = "message deleted " + total;
		CommonUtilities.displayMessage(context, message);
		// notifies user
		generateNotification(context, message);
	}

	@Override
	public void onError(Context context, String errorId) {
		AppLog.Log(TAG, "Received error: " + errorId);
		// displayMessage(context, getString(R.string.gcm_error, errorId));
	}

	@Override
	protected boolean onRecoverableError(Context context, String errorId) {
		// log message
		AppLog.Log(TAG, "Received recoverable error: " + errorId);
		return super.onRecoverableError(context, errorId);
	}

	/**
	 * Issues a notification to inform the user that server has sent a message.
	 */
	public static void generateNotification(Context context, String message) {
		int icon = R.drawable.ic_launcher;
		long when = System.currentTimeMillis();
		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification(icon, message, when);
		String title = context.getString(R.string.app_name);
		Intent notificationIntent = new Intent(context, MapActivity.class);
		notificationIntent.putExtra("fromNotification", "notification");
		// set intent so it does not start a new activity
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent intent = PendingIntent.getActivity(context, 0,
				notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		notification.setLatestEventInfo(context, title, message, intent);
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		System.out.println("notification====>" + message);
		notification.defaults |= Notification.DEFAULT_SOUND;
		notification.defaults |= Notification.DEFAULT_VIBRATE;
		// notification.defaults |= Notification.DEFAULT_LIGHTS;
		notification.flags |= Notification.FLAG_SHOW_LIGHTS;
		notification.ledARGB = 0x00000000;
		notification.ledOnMS = 0;
		notification.ledOffMS = 0;
		notificationManager.notify(AndyConstants.NOTIFICATION_ID, notification);
		PowerManager pm = (PowerManager) context
				.getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock wakeLock = pm.newWakeLock(
				PowerManager.FULL_WAKE_LOCK
						| PowerManager.ACQUIRE_CAUSES_WAKEUP
						| PowerManager.ON_AFTER_RELEASE, "WakeLock");
		wakeLock.acquire();
		wakeLock.release();
	}

	private void publishResults(String regid, int result) {
		Intent publishIntent = new Intent(
				CommonUtilities.DISPLAY_MESSAGE_REGISTER);
		publishIntent.putExtra(CommonUtilities.RESULT, result);
		publishIntent.putExtra(CommonUtilities.REGID, regid);
		System.out.println("sending broad cast");
		sendBroadcast(publishIntent);
	}

}
