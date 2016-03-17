package com.jozibear247_cab.driver.fragment;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;

import com.androidquery.AQuery;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jozibear247_cab.driver.R;
import com.jozibear247_cab.driver.base.BaseMapFragment;
import com.jozibear247_cab.driver.locationupdate.LocationHelper;
import com.jozibear247_cab.driver.locationupdate.LocationReceiveListener;
import com.jozibear247_cab.driver.model.RequestDetail;
import com.jozibear247_cab.driver.parse.AsyncTaskCompleteListener;
import com.jozibear247_cab.driver.parse.HttpRequester;
import com.jozibear247_cab.driver.utills.AndyConstants;
import com.jozibear247_cab.driver.utills.AndyUtils;
import com.jozibear247_cab.driver.utills.AppLog;
import com.jozibear247_cab.driver.utills.PreferenceHelper;
import com.jozibear247_cab.driver.widget.MyFontButton;
import com.jozibear247_cab.driver.widget.MyFontTextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Kishan H Dhamat
 * 
 */

public class ClientRequestFragment extends BaseMapFragment implements
		AsyncTaskCompleteListener, LocationReceiveListener {
//	private GoogleMap mMap;
	private final String TAG = "ClientRequestFragment";
	private LinearLayout llAcceptReject;
	private View llUserDetailView;
	// private ProgressBar pbTimeLeft;
	private MyFontButton btnClientAccept, btnClientReject,
			btnClientReqRemainTime;
	// private RelativeLayout rlTimeLeft;
	private boolean isContinueRequest, isAccepted = false;
	private Timer timer;
	private SeekbarTimer seekbarTimer;
	private RequestDetail requestDetail;
	private Marker markerClientLocation,
			markerClient_d_location;
//	private LocationClient locationClient;
//	private Location location;
	private LocationHelper locationHelper;
	private MyFontTextView tvClientName;// , tvClientPhoneNumber;
	private RatingBar tvClientRating;
	private ImageView ivClientProfilePicture;
	private AQuery aQuery;
	private newRequestReciever requestReciever;
	private boolean selector = false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View clientRequestView = inflater.inflate(
				R.layout.fragment_client_requests, container, false);

		llAcceptReject = (LinearLayout) clientRequestView
				.findViewById(R.id.llAcceptReject);
		llUserDetailView = (View) clientRequestView
				.findViewById(R.id.clientDetailView);
		btnClientAccept = (MyFontButton) clientRequestView
				.findViewById(R.id.btnClientAccept);
		btnClientReject = (MyFontButton) clientRequestView
				.findViewById(R.id.btnClientReject);
		// pbTimeLeft = (ProgressBar) clientRequestView
		// .findViewById(R.id.pbClientReqTime);
		// rlTimeLeft = (RelativeLayout) clientRequestView
		// .findViewById(R.id.rlClientReqTimeLeft);
		btnClientReqRemainTime = (MyFontButton) clientRequestView
				.findViewById(R.id.btnClientReqRemainTime);
		tvClientName = (MyFontTextView) clientRequestView
				.findViewById(R.id.tvClientName);
		// tvClientPhoneNumber = (MyFontTextView) clientRequestView
		// .findViewById(R.id.tvClientNumber);

		tvClientRating = (RatingBar) clientRequestView
				.findViewById(R.id.tvClientRating);

		ivClientProfilePicture = (ImageView) clientRequestView
				.findViewById(R.id.ivClientImage);

		btnClientAccept.setOnClickListener(this);
		btnClientReject.setOnClickListener(this);

		clientRequestView.findViewById(R.id.btnMyLocation).setOnClickListener(
				this);

		return clientRequestView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		aQuery = new AQuery(mapActivity);

		locationHelper = new LocationHelper(mapActivity, false);
		locationHelper.setLocationReceiveListener(this);
		locationHelper.setUpMap();
		locationHelper.onStart();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		IntentFilter filter = new IntentFilter(AndyConstants.NEW_REQUEST);
		requestReciever = new newRequestReciever();
		LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
				requestReciever, filter);
	}

//	private void addMarker() {
//		if (mMap == null) {
//			setUpMap();
//			return;
//		}
//		locationClient = new LocationClient(mapActivity,
//				new ConnectionCallbacks() {
//
//					@Override
//					public void onDisconnected() {
//
//					}
//
//					@Override
//					public void onConnected(Bundle arg0) {
//						location = locationClient.getLastLocation();
//						if (location != null) {
//							if (mMap != null) {
//								if (markerDriverLocation == null) {
//									markerDriverLocation = mMap
//											.addMarker(new MarkerOptions()
//													.position(
//															new LatLng(
//																	location.getLatitude(),
//																	location.getLongitude()))
//													.icon(BitmapDescriptorFactory
//															.fromResource(R.drawable.pin_driver))
//													.title(getResources()
//															.getString(
//																	R.string.my_location)));
//									mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
//											new LatLng(location.getLatitude(),
//													location.getLongitude()),
//											16));
//								} else {
//									markerDriverLocation
//											.setPosition(new LatLng(location
//													.getLatitude(), location
//													.getLongitude()));
//								}
//							}
//						} else {
//							showLocationOffDialog();
//						}
//					}
//				}, new OnConnectionFailedListener() {
//
//					@Override
//					public void onConnectionFailed(ConnectionResult arg0) {
//
//					}
//				});
//		locationClient.connect();
//	}

//	public void showLocationOffDialog() {
//
//		AlertDialog.Builder gpsBuilder = new AlertDialog.Builder(mapActivity);
//		gpsBuilder.setCancelable(false);
//		gpsBuilder
//				.setTitle(getString(R.string.dialog_no_location_service_title))
//				.setMessage(getString(R.string.dialog_no_location_service))
//				.setPositiveButton(
//						getString(R.string.dialog_enable_location_service),
//						new DialogInterface.OnClickListener() {
//							public void onClick(DialogInterface dialog,
//									int which) {
//								// continue with delete
//								dialog.dismiss();
//								Intent viewIntent = new Intent(
//										Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//								startActivity(viewIntent);
//
//							}
//						})
//
//				.setNegativeButton(getString(R.string.dialog_exit),
//						new DialogInterface.OnClickListener() {
//							public void onClick(DialogInterface dialog,
//									int which) {
//								// do nothing
//								dialog.dismiss();
//								mapActivity.finish();
//							}
//						});
//		gpsBuilder.create();
//		gpsBuilder.show();
//	}

//	private void setUpMap() {
//		// Do a null check to confirm that we have not already instantiated the
//		// map.
//		if (mMap == null) {
//			mMap = ((SupportMapFragment) getActivity()
//					.getSupportFragmentManager().findFragmentById(
//							R.id.clientReqMap)).getMap();
//			mMap.getUiSettings().setZoomControlsEnabled(false);
//			mMap.setMyLocationEnabled(false);
//			mMap.getUiSettings().setMyLocationButtonEnabled(false);
//
//			mMap.setInfoWindowAdapter(new InfoWindowAdapter() {
//
//				// Use default InfoWindow frame
//
//				@Override
//				public View getInfoWindow(Marker marker) {
//					View v = mapActivity.getLayoutInflater().inflate(
//							R.layout.info_window_layout, null);
//					MyFontTextView title = (MyFontTextView) v
//							.findViewById(R.id.markerBubblePickMeUp);
//					MyFontTextView content = (MyFontTextView) v
//							.findViewById(R.id.infoaddress);
//					title.setText(marker.getTitle());
//
//					getAddressFromLocation(marker.getPosition(), content);
//
//					// ((TextView) v).setText(marker.getTitle());
//					return v;
//				}
//
//				// Defines the contents of the InfoWindow
//
//				@Override
//				public View getInfoContents(Marker marker) {
//
//					// Getting view from the layout file info_window_layout View
//
//					// Getting reference to the TextView to set title TextView
//
//					// Returning the view containing InfoWindow contents return
//					return null;
//
//				}
//
//			});
//
//			mMap.setOnMarkerClickListener(new OnMarkerClickListener() {
//				@Override
//				public boolean onMarkerClick(Marker marker) {
//					marker.showInfoWindow();
//					return true;
//				}
//			});
//			addMarker();
//		}
//	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnClientAccept:
			isAccepted = true;
			cancelSeekbarTimer();
			respondRequest(1);
			break;
		case R.id.btnClientReject:
			isAccepted = false;
			cancelSeekbarTimer();
			respondRequest(0);
			selector = false;
			break;
		case R.id.btnMyLocation:

			/*
			 * Location loc = mMap.getMyLocation(); if (loc != null) { LatLng
			 * latLang = new LatLng(loc.getLatitude(), loc.getLongitude());
			 * mMap.animateCamera(CameraUpdateFactory.newLatLng(latLang)); }
			 */
			if (locationHelper.getMarkerDriverLocation().getPosition() != null)
				locationHelper.getGoogleMap().animateCamera(CameraUpdateFactory
						.newLatLng(locationHelper.getMarkerDriverLocation().getPosition()));

			break;
		default:
			break;
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (PreferenceHelper.getInstance(mapActivity).getRequestId() == AndyConstants.NO_REQUEST) {
			startCheckingUpcomingRequests();
		}
		if (locationHelper != null) {
			locationHelper.onStart();
		}
		mapActivity.setActionBarTitle(getString(R.string.app_name));
	}

	@Override
	public void onPause() {
		super.onPause();
		if (locationHelper != null) {
			locationHelper.onStop();
		}
		if (PreferenceHelper.getInstance(mapActivity).getRequestId() == AndyConstants.NO_REQUEST) {
			stopCheckingUpcomingRequests();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (locationHelper != null) {
			locationHelper.onStop();
		}
		stopCheckingUpcomingRequests();
		cancelSeekbarTimer();
		AndyUtils.removeCustomProgressDialog();
		LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(
				requestReciever);

	}

	@Override
	public void onTaskCompleted(String response, int serviceCode) {
		Log.d("mahi", "response" + response);

		switch (serviceCode) {
		case AndyConstants.ServiceCode.GET_ALL_REQUEST:
			AndyUtils.removeCustomProgressDialog();
			AppLog.Log(TAG, "getAllRequests Response :" + response);
			if (!parseContent.isSuccess(response)) {
				return;
			}

			requestDetail = parseContent.parseAllRequests(response);
			if (requestDetail != null) {
				if (selector == false) {
					selector = true;

//					GCMIntentService.generateNotification(getActivity(),
//							"New Request");
					stopCheckingUpcomingRequests();
					// startTimerForRespondRequest(requestDetail.getTimeLeft());
					setComponentVisible();
					// pbTimeLeft.setMax(requestDetail.getTimeLeft());
					btnClientReqRemainTime.setText(""
							+ requestDetail.getTimeLeft());
					// pbTimeLeft.setProgress(requestDetail.getTimeLeft());
					tvClientName.setText(requestDetail.getClientName());
					// tvClientPhoneNumber.setText(requestDetail
					// .getClientPhoneNumber());
					if (requestDetail.getClientRating() != 0) {
						tvClientRating.setRating(requestDetail
								.getClientRating());
						Log.i("Rating", "" + requestDetail.getClientRating());
					}

					if (requestDetail.getClientProfile() != null)
						if (!requestDetail.getClientProfile().equals(""))
							aQuery.id(ivClientProfilePicture)
									.progress(R.id.pBar)
									.image(requestDetail.getClientProfile());

					markerClientLocation = null;
					markerClientLocation = locationHelper.getGoogleMap().addMarker(new MarkerOptions()
							.position(
									new LatLng(Double.parseDouble(requestDetail
											.getClientLatitude()), Double
											.parseDouble(requestDetail
													.getClientLongitude())))
							.icon(BitmapDescriptorFactory
									.fromResource(R.drawable.pin_client))
							.title(mapActivity.getResources().getString(
									R.string.client_location)));

					markerClient_d_location = null;
					if (requestDetail.getClient_d_latitude() != null
							&& requestDetail.getClient_d_longitude() != null) {
						markerClient_d_location = locationHelper.getGoogleMap()
								.addMarker(new MarkerOptions()
										.position(
												new LatLng(
														Double.parseDouble(requestDetail
																.getClient_d_latitude()),
														Double.parseDouble(requestDetail
																.getClient_d_longitude())))
										.icon(BitmapDescriptorFactory
												.fromResource(R.drawable.pin_client))
										.title(mapActivity
												.getResources()
												.getString(
														R.string.client_d_location)));
					}

					seekbarTimer = new SeekbarTimer(
							requestDetail.getTimeLeft() * 1000, 1000);
					seekbarTimer.start();
				}
			}
			break;

		case AndyConstants.ServiceCode.RESPOND_REQUEST:
			AppLog.Log(TAG, "respond Request Response :" + response);
			removeNotification();
			AndyUtils.removeCustomProgressDialog();
			if (parseContent.isSuccess(response)) {
				if (isAccepted) {
					PreferenceHelper.getInstance(mapActivity).putRequestId(requestDetail.getRequestId());
					JobFragment jobFragment = new JobFragment();
					Bundle bundle = new Bundle();
					bundle.putInt(AndyConstants.JOB_STATUS,
							AndyConstants.IS_WALKER_STARTED);
					bundle.putSerializable(AndyConstants.REQUEST_DETAIL,
							requestDetail);
					jobFragment.setArguments(bundle);
					mapActivity.addFragment(jobFragment, false,
							AndyConstants.JOB_FRGAMENT_TAG, true);
				} else {
					cancelSeekbarTimer();
					setComponentInvisible();
					if (markerClientLocation != null
							&& markerClientLocation.isVisible()) {
						markerClientLocation.remove();
					}
					if (markerClient_d_location != null
							&& markerClient_d_location.isVisible()) {
						markerClient_d_location.remove();
					}
					PreferenceHelper.getInstance(mapActivity).putRequestId(AndyConstants.NO_REQUEST);
					startCheckingUpcomingRequests();
				}
			}

			// else {
			// AndyUtils.showToast(
			// mapActivity.getResources().getString(
			// R.string.toast_unable_respond_request),
			// mapActivity);
			// }
			break;

		default:
			break;

		}
	}

	private class SeekbarTimer extends CountDownTimer {

		public SeekbarTimer(long startTime, long interval) {
			super(startTime, interval);
			// pbTimeLeft.setProgressDrawable(getResources().getDrawable(
			// R.drawable.customprogress));
		}

		@Override
		public void onFinish() {
			if (seekbarTimer == null) {
				return;
			}
			AndyUtils.showToast(
					mapActivity.getResources().getString(
							R.string.toast_time_over), mapActivity);
			respondRequest(0);
			setComponentInvisible();
			PreferenceHelper.getInstance(mapActivity).clearRequestData();
			if (markerClientLocation != null
					&& markerClientLocation.isVisible()) {
				markerClientLocation.remove();
			}
			if (markerClient_d_location != null
					&& markerClient_d_location.isVisible()) {
				markerClient_d_location.remove();
			}
			removeNotification();
			startCheckingUpcomingRequests();
			this.cancel();
			seekbarTimer = null;
			selector = false;

		}

		@Override
		public void onTick(long millisUntilFinished) {
			int time = (int) (millisUntilFinished / 1000);
			if (!isVisible()) {
				return;
			}
			btnClientReqRemainTime.setText("" + time);
			try {

				MediaPlayer r = MediaPlayer.create(getActivity(), R.raw.beep);
				if (time <= 8 && time >= 0) {
					r.start();

				}
				if (time == 0)
					r.stop();

			} catch (Exception e) {
				e.printStackTrace();
			}

			// pbTimeLeft.setProgress(time);
			// if (time <= 5) {
			// pbTimeLeft.setProgressDrawable(getResources().getDrawable(
			// R.drawable.customprogressred));
			// }

		}
	}

	// if status = 1 then accept if 0 then reject
	private void respondRequest(int status) {
		if (!AndyUtils.isNetworkAvailable(mapActivity)) {
			AndyUtils.showToast(
					getResources().getString(R.string.toast_no_internet),
					mapActivity);
			return;
		}

		AndyUtils.showCustomProgressDialog(mapActivity, "", getResources()
				.getString(R.string.progress_respond_request), false);

		HashMap<String, String> map = new HashMap<String, String>();
		map.put(AndyConstants.URL, AndyConstants.ServiceType.RESPOND_REQUESTS);
		map.put(AndyConstants.Params.ID, PreferenceHelper.getInstance(mapActivity).getUserId());
		map.put(AndyConstants.Params.REQUEST_ID,
				String.valueOf(requestDetail.getRequestId()));
		map.put(AndyConstants.Params.TOKEN, PreferenceHelper.getInstance(mapActivity).getSessionToken());
		map.put(AndyConstants.Params.ACCEPTED, String.valueOf(status));
		new HttpRequester(mapActivity, map,
				AndyConstants.ServiceCode.RESPOND_REQUEST, this);

	}

	public void checkRequestStatus() {
		if (!AndyUtils.isNetworkAvailable(mapActivity)) {
			AndyUtils.showToast(
					getResources().getString(R.string.toast_no_internet),
					mapActivity);
			return;
		}
		AndyUtils.showCustomProgressDialog(mapActivity, "", getResources()
				.getString(R.string.progress_dialog_request), false);
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(AndyConstants.URL,
				AndyConstants.ServiceType.CHECK_REQUEST_STATUS
						+ AndyConstants.Params.ID + "="
						+ PreferenceHelper.getInstance(mapActivity).getUserId() + "&"
						+ AndyConstants.Params.TOKEN + "="
						+ PreferenceHelper.getInstance(mapActivity).getSessionToken() + "&"
						+ AndyConstants.Params.REQUEST_ID + "="
						+ PreferenceHelper.getInstance(mapActivity).getRequestId());
		new HttpRequester(mapActivity, map,
				AndyConstants.ServiceCode.CHECK_REQUEST_STATUS, true, this);
	}

	public void getAllRequests() {
		if (!AndyUtils.isNetworkAvailable(mapActivity) || selector) {
			return;
		}

		HashMap<String, String> map = new HashMap<String, String>();
		map.put(AndyConstants.URL,
				AndyConstants.ServiceType.GET_ALL_REQUESTS
						+ AndyConstants.Params.ID + "="
						+ PreferenceHelper.getInstance(mapActivity).getUserId() + "&"
						+ AndyConstants.Params.TOKEN + "="
						+ PreferenceHelper.getInstance(mapActivity).getSessionToken());

		new HttpRequester(mapActivity, map,
				AndyConstants.ServiceCode.GET_ALL_REQUEST, true, this);
	}

	private class TimerRequestStatus extends TimerTask {
		@Override
		public void run() {
			if (isContinueRequest) {
				// isContinueRequest = false;
				getAllRequests();
			}
		}

	}

	private void startCheckingUpcomingRequests() {
		AppLog.Log(TAG, "start checking upcomingRequests");
		isContinueRequest = true;
		timer = new Timer();
		timer.scheduleAtFixedRate(new TimerRequestStatus(),
				AndyConstants.DELAY, AndyConstants.TIME_SCHEDULE);
	}

	private void stopCheckingUpcomingRequests() {
		AppLog.Log(TAG, "stop checking upcomingRequests");
		isContinueRequest = false;
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
	}

	private void removeNotification() {
		try {
			NotificationManager manager = (NotificationManager) mapActivity
					.getSystemService(mapActivity.NOTIFICATION_SERVICE);
			manager.cancel(AndyConstants.NOTIFICATION_ID);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onLocationReceived(LatLng latlong) {
		if (latlong != null) {
			if (locationHelper.getGoogleMap() != null) {
				if (locationHelper.getMarkerDriverLocation() == null) {
					Marker marker = locationHelper.getGoogleMap().addMarker(new MarkerOptions()
							.position(
									new LatLng(latlong.latitude,
											latlong.longitude))
							.icon(BitmapDescriptorFactory
									.fromResource(R.drawable.pin_driver))
							.title(mapActivity.getResources().getString(
									R.string.my_location)));
					locationHelper.setMarkerDriverLocation(marker);
					locationHelper.getGoogleMap().animateCamera(CameraUpdateFactory
							.newLatLngZoom(new LatLng(latlong.latitude,
									latlong.longitude), 16));
				} else {
					locationHelper.getMarkerDriverLocation().setPosition(new LatLng(
							latlong.latitude, latlong.longitude));
				}
			}
		}
	}

	public void setComponentVisible() {
		llAcceptReject.setVisibility(View.VISIBLE);
		btnClientReqRemainTime.setVisibility(View.VISIBLE);
		// rlTimeLeft.setVisibility(View.VISIBLE);
		llUserDetailView.setVisibility(View.VISIBLE);
	}

	public void setComponentInvisible() {
		llAcceptReject.setVisibility(View.GONE);
		btnClientReqRemainTime.setVisibility(View.GONE);
		// rlTimeLeft.setVisibility(View.GONE);
		llUserDetailView.setVisibility(View.GONE);
	}

	public void cancelSeekbarTimer() {
		if (seekbarTimer != null) {
			seekbarTimer.cancel();
			seekbarTimer = null;
		}
	}

	public void onDestroyView() {
		SupportMapFragment f = (SupportMapFragment) getFragmentManager()
				.findFragmentById(R.id.clientReqMap);
		if (f != null) {
			try {
				getFragmentManager().beginTransaction().remove(f).commit();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		locationHelper.setGoogleMap(null);
		super.onDestroyView();
	}

	private class newRequestReciever extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			String response = intent.getStringExtra(AndyConstants.NEW_REQUEST);
			AppLog.Log(TAG, "FROM BROAD CAST-->" + response);
			try {
				JSONObject jsonObject = new JSONObject(response);
				if (jsonObject.getInt(AndyConstants.Params.UNIQUE_ID) == 1) {
					stopCheckingUpcomingRequests();
					requestDetail = parseContent.parseNotification(response);
					if (requestDetail != null) {

						if (selector == false) {
							selector = true;
							// startTimerForRespondRequest(requestDetail.getTimeLeft());

							setComponentVisible();
							// pbTimeLeft.setMax(requestDetail.getTimeLeft());
							btnClientReqRemainTime.setText(""
									+ requestDetail.getTimeLeft());
							// pbTimeLeft.setProgress(requestDetail.getTimeLeft());
							tvClientName.setText(requestDetail.getClientName());
							// tvClientPhoneNumber.setText(requestDetail
							// .getClientPhoneNumber());
							if (requestDetail.getClientRating() != 0) {
								tvClientRating.setRating(requestDetail
										.getClientRating());
								Log.i("Rating",
										"" + requestDetail.getClientRating());
							}

							if (requestDetail.getClientProfile() != null)
								if (!requestDetail.getClientProfile()
										.equals(""))
									aQuery.id(ivClientProfilePicture)
											.progress(R.id.pBar)
											.image(requestDetail
													.getClientProfile());
							markerClientLocation = null;
							markerClientLocation = locationHelper.getGoogleMap()
									.addMarker(new MarkerOptions()
											.position(
													new LatLng(
															Double.parseDouble(requestDetail
																	.getClientLatitude()),
															Double.parseDouble(requestDetail
																	.getClientLongitude())))
											.icon(BitmapDescriptorFactory
													.fromResource(R.drawable.pin_client))
											.title(mapActivity
													.getResources()
													.getString(
															R.string.client_location)));

							markerClient_d_location = null;
							Log.d("xxx",
									"he" + requestDetail.getClient_d_latitude());
							Log.d("xxx",
									"she"
											+ requestDetail
													.getClient_d_longitude());
							if (requestDetail.getClient_d_latitude() != null
									&& requestDetail.getClient_d_longitude() != null) {
								Log.d("xxx", "inside");
								markerClient_d_location = locationHelper.getGoogleMap()
										.addMarker(new MarkerOptions()
												.position(
														new LatLng(
																Double.parseDouble(requestDetail
																		.getClient_d_latitude()),
																Double.parseDouble(requestDetail
																		.getClient_d_longitude())))
												.icon(BitmapDescriptorFactory
														.fromResource(R.drawable.pin_client))
												.title(mapActivity
														.getResources()
														.getString(
																R.string.client_d_location)));
							}

							seekbarTimer = new SeekbarTimer(
									requestDetail.getTimeLeft() * 1000, 1000);
							seekbarTimer.start();
							AppLog.Log(TAG, "From broad cast recieved request");
						}
					} else {
						startCheckingUpcomingRequests();
					}
				} else {
					setComponentInvisible();
					selector = false;
					PreferenceHelper.getInstance(mapActivity).clearRequestData();
					if (markerClientLocation != null
							&& markerClientLocation.isVisible()) {
						markerClientLocation.remove();
					}
					if (markerClient_d_location != null
							&& markerClient_d_location.isVisible()) {
						markerClient_d_location.remove();
					}
					cancelSeekbarTimer();
					removeNotification();
					startCheckingUpcomingRequests();
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

		}
	}

}
