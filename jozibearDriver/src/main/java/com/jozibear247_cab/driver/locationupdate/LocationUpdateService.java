package com.jozibear247_cab.driver.locationupdate;

import android.app.IntentService;
import android.content.Intent;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.google.android.gms.maps.model.LatLng;
import com.jozibear247_cab.driver.utills.AndyConstants;
import com.jozibear247_cab.driver.utills.AndyUtils;
import com.jozibear247_cab.driver.utills.AppLog;
import com.jozibear247_cab.driver.utills.PreferenceHelper;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LocationUpdateService extends IntentService implements
		LocationReceiveListener {
//	private PreferenceHelper preferenceHelper;
	private LocationHelper locationHelper;
	private String id, token, latitude, longitude;

	public LocationUpdateService() {
		this("JozibearLocationService");
	}

	public LocationUpdateService(String name) {
		super("JozibearLocationService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
//		preferenceHelper = new PreferenceHelper(getApplicationContext());
		id = PreferenceHelper.getInstance(this).getUserId();
		token = PreferenceHelper.getInstance(this).getSessionToken();
		// if (driverId.equals("")) {
		// driverId = getDriverID();
		// }
		return START_STICKY;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		locationHelper = new LocationHelper(this);
		locationHelper.setLocationReceiveListener(this);
		locationHelper.onStart();
	}

	@Override
	public void onDestroy() {
		if (locationHelper != null) {
			locationHelper.onStop();
		}
		super.onDestroy();
	}

	@Override
	public void onLocationReceived(LatLng latlong) {
//		AppLog.Log("TAG", "onLocationReceived Lat : " + latlong.latitude
//				+ " , long : " + latlong.longitude);
		if (latlong != null) {
			PreferenceHelper.getInstance(getApplicationContext())
					.putWalkerLatitude(String.valueOf(latlong.latitude));
			PreferenceHelper.getInstance(getApplicationContext()).putWalkerLongitude(String
					.valueOf(latlong.longitude));
		}
		if (!TextUtils.isEmpty(id) && !TextUtils.isEmpty(token)
				&& latlong != null) {

			latitude = String.valueOf(latlong.latitude);
			longitude = String.valueOf(latlong.longitude);
			if (!AndyUtils.isNetworkAvailable(getApplicationContext())) {
				// AndyUtils.showToast(
				// getResources().getString(R.string.toast_no_internet),
				// getApplicationContext());
				return;
			}

			if (PreferenceHelper.getInstance(getApplicationContext()).getRequestId() == AndyConstants.NO_REQUEST) {
				new UploadDataToServer().execute();
			} else {
				new UploadTripLocationData().execute();
			}

		}
	}

	private class UploadDataToServer extends AsyncTask<String, String, String> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(String... params) {
			try {
				HttpParams myParams = new BasicHttpParams();
				HttpConnectionParams.setConnectionTimeout(myParams, 100000);
				HttpConnectionParams.setSoTimeout(myParams, 100000);
				HttpClient httpClient = new DefaultHttpClient(myParams);
				ResponseHandler<String> res = new BasicResponseHandler();
				HttpPost postMethod = new HttpPost(
						AndyConstants.ServiceType.UPDATE_PROVIDER_LOCATION);
				// HttpRequest httpRequest = new HttpRequest();
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
				nameValuePairs.add(new BasicNameValuePair(
						AndyConstants.Params.ID, id));
				nameValuePairs.add(new BasicNameValuePair(
						AndyConstants.Params.TOKEN, token));
				nameValuePairs.add(new BasicNameValuePair(
						AndyConstants.Params.LATITUDE, latitude));
				nameValuePairs.add(new BasicNameValuePair(
						AndyConstants.Params.LONGITUDE, longitude));

				postMethod.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				String response = httpClient.execute(postMethod, res);
				// String response = httpRequest.postData(
				// AndyConstants.ServiceType.UPDATE_PROVIDER_LOCATION,
				// nameValuePairs);
//				AppLog.Log("TAG", "location send Response:::" + response);

				return response;
			} catch (Exception exception) {
				exception.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			stopSelf();
		}
	}

	private class UploadTripLocationData extends
			AsyncTask<String, String, String> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(String... params) {
			try {
				HttpParams myParams = new BasicHttpParams();
				HttpConnectionParams.setConnectionTimeout(myParams, 100000);
				HttpConnectionParams.setSoTimeout(myParams, 100000);
				HttpClient httpClient = new DefaultHttpClient(myParams);
				ResponseHandler<String> res = new BasicResponseHandler();
				HttpPost postMethod = new HttpPost(
						AndyConstants.ServiceType.REQUEST_LOCATION_UPDATE);
				// HttpRequest httpRequest = new HttpRequest();
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
				nameValuePairs.add(new BasicNameValuePair(
						AndyConstants.Params.ID, id));
				nameValuePairs.add(new BasicNameValuePair(
						AndyConstants.Params.TOKEN, token));
				nameValuePairs.add(new BasicNameValuePair(
						AndyConstants.Params.LATITUDE, latitude));
				nameValuePairs.add(new BasicNameValuePair(
						AndyConstants.Params.LONGITUDE, longitude));
				// nameValuePairs.add(new BasicNameValuePair(
				// AndyConstants.Params.DISTANCE, 0 + ""));
				nameValuePairs.add(new BasicNameValuePair(
						AndyConstants.Params.REQUEST_ID, PreferenceHelper.getInstance(getApplicationContext())
								.getRequestId() + ""));

				AppLog.Log("ID", id);
				AppLog.Log("Token", token);
				AppLog.Log("Latitude", latitude);
				AppLog.Log("Longitude", longitude);

				AppLog.Log("Request id", "" + PreferenceHelper.getInstance(getApplicationContext()).getRequestId());

				postMethod.setEntity(new UrlEncodedFormEntity(nameValuePairs));

				String response = httpClient.execute(postMethod, res);
				// String response = httpRequest.postData(
				// AndyConstants.ServiceType.UPDATE_PROVIDER_LOCATION,
				// nameValuePairs);
//				AppLog.Log("TAG", "request location send Response:::"
//						+ response);
				JSONObject jsonObject = new JSONObject(response);
				if (jsonObject.getBoolean("success")) {
					PreferenceHelper.getInstance(getApplicationContext()).putDistance(Float.parseFloat(jsonObject
							.getString(AndyConstants.Params.DISTANCE)));

					PreferenceHelper.getInstance(getApplicationContext()).putUnit(jsonObject.getString("unit"));

				}

				return response;
			} catch (Exception exception) {
				exception.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			stopSelf();
		}
	}
}
