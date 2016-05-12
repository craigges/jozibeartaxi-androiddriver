package com.jozibear247_cab.driver.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jozibear247_cab.driver.MapActivity;
import com.jozibear247_cab.driver.R;
import com.jozibear247_cab.driver.base.BaseMapFragment;
import com.jozibear247_cab.driver.locationupdate.LocationHelper;
import com.jozibear247_cab.driver.locationupdate.LocationReceiveListener;
import com.jozibear247_cab.driver.model.Bill;
import com.jozibear247_cab.driver.model.RequestDetail;
import com.jozibear247_cab.driver.parse.AsyncTaskCompleteListener;
import com.jozibear247_cab.driver.parse.HttpRequester;
import com.jozibear247_cab.driver.parse.ParseContent;
import com.jozibear247_cab.driver.utills.AndyConstants;
import com.jozibear247_cab.driver.utills.AndyUtils;
import com.jozibear247_cab.driver.utills.AppLog;
import com.jozibear247_cab.driver.utills.PreferenceHelper;
import com.jozibear247_cab.driver.widget.MyFontTextView;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Kishan H Dhamat
 * 
 */
public class JobFragment extends BaseMapFragment implements
		AsyncTaskCompleteListener, LocationReceiveListener {

	private static final int ANIMATE_SPEEED_TURN = 2000;
	private static final int BEARING_OFFSET = 20;
	//	private GoogleMap googleMap;
	// private PolylineOptions lineOptions;
	// private BeanRoute route;
	// private ArrayList<LatLng> points;
	private MyFontTextView tvJobTime, tvJobDistance, tvJobStatus, tvClientName;
	private ImageView ivClientProfilePicture;
	private RatingBar tvClientRating;
	private ParseContent parseContent;
//	private LocationClient locationClient;
//	private Location location;
	private LocationHelper locationHelper;
	private BroadcastReceiver mReceiver;
	private AQuery aQuery;
	private RequestDetail requestDetail;
//	private ArrayList<LatLng> points;
//	private PolylineOptions lineOptions;
	private Marker markerClientLocation,
			markerClient_d_location;
	private Timer elapsedTimer;
//	private DBHelper dbHelper;
	private int jobStatus = 0;
	private String time, distance = "0";
	private final String TAG = "JobFragment";
	DecimalFormat decimalFormat;
	public static final long ELAPSED_TIME_SCHEDULE = 60 * 1000;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View jobFragmentView = inflater.inflate(R.layout.fragment_job, container, false);

		tvJobTime = (MyFontTextView) jobFragmentView
				.findViewById(R.id.tvJobTime);
		tvJobDistance = (MyFontTextView) jobFragmentView
				.findViewById(R.id.tvJobDistance);
		tvJobStatus = (MyFontTextView) jobFragmentView
				.findViewById(R.id.tvJobStatus);
		tvClientName = (MyFontTextView) jobFragmentView
				.findViewById(R.id.tvClientName);
		// tvClientPhoneNumber = (MyFontTextView) jobFragmentView
		// .findViewById(R.id.tvClientNumber);
		tvClientRating = (RatingBar) jobFragmentView
				.findViewById(R.id.tvClientRating);
		ivClientProfilePicture = (ImageView) jobFragmentView
				.findViewById(R.id.ivClientImage);

		tvJobStatus.setOnClickListener(this);
		jobFragmentView.findViewById(R.id.tvJobCallClient).setOnClickListener(
				this);

		return jobFragmentView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		parseContent = new ParseContent(mapActivity);
		decimalFormat = new DecimalFormat("0.00");
//		points = new ArrayList<LatLng>();
		aQuery = new AQuery(mapActivity);
//		dbHelper = new DBHelper(mapActivity);
		jobStatus = getArguments().getInt(AndyConstants.JOB_STATUS,
				AndyConstants.IS_WALKER_STARTED);
		requestDetail = (RequestDetail) getArguments().getSerializable(
				AndyConstants.REQUEST_DETAIL);

		if (jobStatus == AndyConstants.IS_WALK_COMPLETED) {
			startElapsedTimer();
			getPathFromServer();
		}

		setClientDetails(requestDetail);

		locationHelper = new LocationHelper(mapActivity, true);
		locationHelper.setLocationReceiveListener(this);
		locationHelper.setUpMap();
		locationHelper.onStart();

		// getDistance();
		if(jobStatus >= AndyConstants.IS_WALK_STARTED) {
			showDestinationMarkerWindow();
			locationHelper.getGoogleMap().animateCamera(CameraUpdateFactory
					.newLatLngZoom(new LatLng(Double.parseDouble(requestDetail
							.getClient_d_latitude()),
							Double.parseDouble(requestDetail
									.getClient_d_longitude())), 12));
		} else {
			showLocationMarkerWindow();
		}
	}

	/**
	 * 
	 */
	private void getPathFromServer() {
		AndyUtils.showCustomProgressDialog(mapActivity, "", getResources()
				.getString(R.string.progress_loading), false);
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(AndyConstants.URL,
				AndyConstants.ServiceType.PATH_REQUEST
						+ AndyConstants.Params.ID + "="
						+ PreferenceHelper.getInstance(mapActivity).getUserId() + "&"
						+ AndyConstants.Params.TOKEN + "="
						+ PreferenceHelper.getInstance(mapActivity).getSessionToken() + "&"
						+ AndyConstants.Params.REQUEST_ID + "="
						+ PreferenceHelper.getInstance(mapActivity).getRequestId());
		new HttpRequester(mapActivity, map,
				AndyConstants.ServiceCode.PATH_REQUEST, true, this);
	}

	private void setClientDetails(RequestDetail requestDetail) {
		tvClientName.setText(requestDetail.getClientName());
		// tvClientPhoneNumber.setText(requestDetail.getClientPhoneNumber());
		if (requestDetail.getClientRating() != 0) {
			tvClientRating.setRating(requestDetail.getClientRating());
		}
		if(requestDetail.getClientProfile()!=null)
			if(!requestDetail.getClientProfile().equals(""))
		aQuery.id(ivClientProfilePicture).progress(R.id.pBar)
				.image(requestDetail.getClientProfile());

//		if (locationHelper.getGoogleMap() == null) {
//			return;
//		}

	}

	/**
	 * it is used for seeting text for jobstatus on textview
	 */
	private void setjobStatus(int jobStatus) {

		switch (jobStatus) {
		case AndyConstants.IS_WALKER_STARTED:
			tvJobStatus.setText(mapActivity.getResources().getString(
					R.string.text_walker_started));
			break;
		case AndyConstants.IS_WALKER_ARRIVED:
			tvJobStatus.setText(mapActivity.getResources().getString(
					R.string.text_walker_arrived));
			break;
		case AndyConstants.IS_WALK_STARTED:
			tvJobStatus.setText(mapActivity.getResources().getString(
					R.string.text_walk_started));
			break;
		case AndyConstants.IS_WALK_COMPLETED:
			tvJobStatus.setText(mapActivity.getResources().getString(
					R.string.text_walk_completed));
			break;
		default:
			break;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tvJobStatus:
			switch (jobStatus) {
			case AndyConstants.IS_WALKER_STARTED:
				walkerStarted();
				break;
			case AndyConstants.IS_WALKER_ARRIVED:
				walkerArrived();
				break;
			case AndyConstants.IS_WALK_STARTED:
				walkStarted();
				break;
			case AndyConstants.IS_WALK_COMPLETED:
				walkCompleted();
				break;
			default:
				break;
			}

			break;
		case R.id.tvJobCallClient:
			if (!TextUtils.isEmpty(requestDetail.getClientPhoneNumber())) {
				Intent callIntent = new Intent(Intent.ACTION_CALL);
				callIntent.setData(Uri.parse("tel:"
						+ requestDetail.getClientPhoneNumber()));
				startActivity(callIntent);
			} else {
				Toast.makeText(
						mapActivity,
						mapActivity.getResources().getString(
								R.string.toast_number_not_found),
						Toast.LENGTH_SHORT).show();
			}
			break;
		default:
			break;
		}
	}

	/**
	 * send this when walk completed
	 */
	private void walkCompleted() {
		if (!AndyUtils.isNetworkAvailable(mapActivity)) {
			AndyUtils.showToast(
					getResources().getString(R.string.toast_no_internet),
					mapActivity);
			return;
		}

		AndyUtils.showCustomProgressDialog(mapActivity, "", getResources()
				.getString(R.string.progress_send_request), false);

		HashMap<String, String> map = new HashMap<String, String>();
		map.put(AndyConstants.URL, AndyConstants.ServiceType.WALK_COMPLETED);
		map.put(AndyConstants.Params.ID, PreferenceHelper.getInstance(mapActivity).getUserId());
		map.put(AndyConstants.Params.REQUEST_ID,
				String.valueOf(PreferenceHelper.getInstance(mapActivity).getRequestId()));
		map.put(AndyConstants.Params.TOKEN, PreferenceHelper.getInstance(mapActivity).getSessionToken());
		map.put(AndyConstants.Params.LATITUDE,
				PreferenceHelper.getInstance(mapActivity).getWalkerLatitude());
		map.put(AndyConstants.Params.LONGITUDE,
				PreferenceHelper.getInstance(mapActivity).getWalkerLongitude());
		map.put(AndyConstants.Params.DISTANCE, PreferenceHelper.getInstance(mapActivity).getDistance()
				+ "");
		map.put(AndyConstants.Params.TIME, time);
		new HttpRequester(mapActivity, map,
				AndyConstants.ServiceCode.WALK_COMPLETED, this);
	}

	/**
	 * send this when job started
	 */
	private void walkStarted() {
		if (!AndyUtils.isNetworkAvailable(mapActivity)) {
			AndyUtils.showToast(
					getResources().getString(R.string.toast_no_internet),
					mapActivity);
			return;
		}

		AndyUtils.showCustomProgressDialog(mapActivity, "", getResources()
				.getString(R.string.progress_send_request), false);

		HashMap<String, String> map = new HashMap<String, String>();
		map.put(AndyConstants.URL, AndyConstants.ServiceType.WALK_STARTED);
		map.put(AndyConstants.Params.ID, PreferenceHelper.getInstance(mapActivity).getUserId());
		map.put(AndyConstants.Params.REQUEST_ID,
				String.valueOf(PreferenceHelper.getInstance(mapActivity).getRequestId()));
		map.put(AndyConstants.Params.TOKEN, PreferenceHelper.getInstance(mapActivity).getSessionToken());
		map.put(AndyConstants.Params.LATITUDE,
				PreferenceHelper.getInstance(mapActivity).getWalkerLatitude());
		map.put(AndyConstants.Params.LONGITUDE,
				PreferenceHelper.getInstance(mapActivity).getWalkerLongitude());
		new HttpRequester(mapActivity, map,
				AndyConstants.ServiceCode.WALK_STARTED, this);
	}

	/**
	 * send this when walker arrived client's location
	 */
	private void walkerArrived() {
		if (!AndyUtils.isNetworkAvailable(mapActivity)) {
			AndyUtils.showToast(
					getResources().getString(R.string.toast_no_internet),
					mapActivity);
			return;
		}

		AndyUtils.showCustomProgressDialog(mapActivity, "", getResources()
				.getString(R.string.progress_send_request), false);

		HashMap<String, String> map = new HashMap<String, String>();
		map.put(AndyConstants.URL, AndyConstants.ServiceType.WALK_ARRIVED);
		map.put(AndyConstants.Params.ID, PreferenceHelper.getInstance(mapActivity).getUserId());
		map.put(AndyConstants.Params.REQUEST_ID,
				String.valueOf(PreferenceHelper.getInstance(mapActivity).getRequestId()));
		map.put(AndyConstants.Params.TOKEN, PreferenceHelper.getInstance(mapActivity).getSessionToken());
		map.put(AndyConstants.Params.LATITUDE,
				PreferenceHelper.getInstance(mapActivity).getWalkerLatitude());
		map.put(AndyConstants.Params.LONGITUDE,
				PreferenceHelper.getInstance(mapActivity).getWalkerLongitude());
		new HttpRequester(mapActivity, map,
				AndyConstants.ServiceCode.WALKER_ARRIVED, this);
	}

	/**
	 * send this when walker started his/her run
	 */
	private void walkerStarted() {
		if (!AndyUtils.isNetworkAvailable(mapActivity)) {
			AndyUtils.showToast(
					getResources().getString(R.string.toast_no_internet),
					mapActivity);
			return;
		}

		AndyUtils.showCustomProgressDialog(mapActivity, "", getResources()
				.getString(R.string.progress_send_request), false);

		HashMap<String, String> map = new HashMap<String, String>();
		map.put(AndyConstants.URL, AndyConstants.ServiceType.WALKER_STARTED);
		map.put(AndyConstants.Params.ID, PreferenceHelper.getInstance(mapActivity).getUserId());
		map.put(AndyConstants.Params.REQUEST_ID,
				String.valueOf(PreferenceHelper.getInstance(mapActivity).getRequestId()));
		map.put(AndyConstants.Params.TOKEN, PreferenceHelper.getInstance(mapActivity).getSessionToken());
		map.put(AndyConstants.Params.LATITUDE,
				PreferenceHelper.getInstance(mapActivity).getWalkerLatitude());
		map.put(AndyConstants.Params.LONGITUDE,
				PreferenceHelper.getInstance(mapActivity).getWalkerLongitude());
		new HttpRequester(mapActivity, map,
				AndyConstants.ServiceCode.WALKER_STARTED, this);
	}

	/* added by amal */
//	private String strAddress = null;
//
//	private void getAddressFromLocation(final LatLng latlng,
//			final MyFontTextView et) {
//
//		/*
//		 * et.setText("Waiting for Address"); et.setTextColor(Color.GRAY);
//		 */
//		/*
//		 * new Thread(new Runnable() {
//		 *
//		 * @Override public void run() { // TODO Auto-generated method stub
//		 */
//
//		Geocoder gCoder = new Geocoder(getActivity());
//		try {
//			final List<Address> list = gCoder.getFromLocation(latlng.latitude,
//					latlng.longitude, 1);
//			if (list != null && list.size() > 0) {
//				Address address = list.get(0);
//				StringBuilder sb = new StringBuilder();
//				if (address.getAddressLine(0) != null) {
//
//					sb.append(address.getAddressLine(0)).append(", ");
//				}
//				sb.append(address.getLocality()).append(", ");
//				// sb.append(address.getPostalCode()).append(",");
//				sb.append(address.getCountryName());
//				strAddress = sb.toString();
//
//				strAddress = strAddress.replace(",null", "");
//				strAddress = strAddress.replace("null", "");
//				strAddress = strAddress.replace("Unnamed", "");
//				if (!TextUtils.isEmpty(strAddress)) {
//
//					et.setText(strAddress);
//
//				}
//			}
//			/*
//			 * getActivity().runOnUiThread(new Runnable() {
//			 *
//			 * @Override public void run() { // TODO Auto-generated method stub
//			 * if (!TextUtils.isEmpty(strAddress)) {
//			 *
//			 * et.setText(strAddress);
//			 *
//			 *
//			 * } else { et.setText("");
//			 *
//			 * }
//			 *
//			 * } });
//			 */
//
//		} catch (IOException exc) {
//			exc.printStackTrace();
//		}
//		// }
//		// }).start();
//
//	}

//	private void setUpMap() {
//		// Do a null check to confirm that we have not already instantiated the
//		// map.
//		if (googleMap == null) {
//			googleMap = ((SupportMapFragment) getActivity()
//					.getSupportFragmentManager().findFragmentById(R.id.jobMap))
//					.getMap();
//			initPreviousDrawPath();
//
//			googleMap.setInfoWindowAdapter(new InfoWindowAdapter() {
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
//					// ((MyFontTextView) v).setText(marker.getTitle());
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
//			googleMap.setOnMarkerClickListener(new OnMarkerClickListener() {
//				@Override
//				public boolean onMarkerClick(Marker marker) {
//					marker.showInfoWindow();
//					return true;
//				}
//			});
//
//			addMarker();
//
//		}
//	}

	// It will add marker on map of walker location
//	private void addMarker() {
//		if (googleMap == null) {
//			setUpMap();
//			return;
//		}
//
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
//							if (googleMap != null) {
//								if (markerDriverLocation == null) {
//									markerDriverLocation = googleMap
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
//									googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
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

	public void onDestroyView() {
		SupportMapFragment f = (SupportMapFragment) getFragmentManager()
				.findFragmentById(R.id.jobMap);
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

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (locationHelper != null) {
			locationHelper.onStop();
		}
		stopElapsedTimer();
	}

	@Override
	public void onTaskCompleted(String response, int serviceCode) {
		AndyUtils.removeCustomProgressDialog();
		switch (serviceCode) {
		case AndyConstants.ServiceCode.WALKER_STARTED:
			AppLog.Log(TAG, "walker started response " + response);
			if (parseContent.isSuccess(response)) {
				jobStatus = AndyConstants.IS_WALKER_ARRIVED;
				setjobStatus(jobStatus);
			}

			break;
		case AndyConstants.ServiceCode.WALKER_ARRIVED:
			AppLog.Log(TAG, "walker arrived response " + response);
			if (parseContent.isSuccess(response)) {
				jobStatus = AndyConstants.IS_WALK_STARTED;
				setjobStatus(jobStatus);
			}
			showDestinationMarkerWindow();
			locationHelper.getGoogleMap().animateCamera(CameraUpdateFactory
					.newLatLngZoom(new LatLng(Double.parseDouble(requestDetail
							.getClient_d_latitude()),
							Double.parseDouble(requestDetail
									.getClient_d_longitude())), 12));
			break;
		case AndyConstants.ServiceCode.WALK_STARTED:
			AppLog.Log(TAG, "walk started response " + response);
			Log.d("amal", response);
			if (parseContent.isSuccess(response)) {
				PreferenceHelper.getInstance(mapActivity).putIsTripStart(true);
				jobStatus = AndyConstants.IS_WALK_COMPLETED;
				setjobStatus(jobStatus);
				// getDistance();
				try {
					TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
					Date date = new Date();
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
							Locale.US);
					String strDateTime = sdf.format(date);
					long curTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
							Locale.US).parse(strDateTime).getTime();
					PreferenceHelper.getInstance(mapActivity).putRequestTime(curTime);
				} catch (ParseException e) {
					e.printStackTrace();
				}
				if (markerClientLocation != null) {
					markerClientLocation.setTitle(mapActivity.getResources()
							.getString(R.string.job_start_location));
					markerClientLocation.remove();
				}
				startElapsedTimer();
			}

			break;
		case AndyConstants.ServiceCode.WALK_COMPLETED:
			AppLog.Log(TAG, "walk completed response " + response);
			Log.d("mahi", "response done" +response);
			if (parseContent.isSuccess(response)) {
				FeedbackFragment feedbackFragment = new FeedbackFragment();
				Bundle bundle = new Bundle();
				bundle.putSerializable(AndyConstants.REQUEST_DETAIL,
						requestDetail);
				Bill bill = parseContent.parseBillWhenWalkComplete(response);
				bundle.putSerializable("bill",  bill);
				// bundle.putString(
				// AndyConstants.Params.TIME,
				// time
				// + " "
				// + mapActivity.getResources().getString(
				// R.string.text_mins));
				// bundle.putString(
				// AndyConstants.Params.DISTANCE,
				// decimalFormat.format(preferenceHelper.getDistance())
				// // / (1000 * 1.62))
				// + " "
				// + mapActivity.getResources().getString(
				// R.string.text_miles));

				requestDetail.setTime(time);
				requestDetail.setDistance(" " + PreferenceHelper.getInstance(mapActivity).getDistance());
				requestDetail.setUnit(PreferenceHelper.getInstance(mapActivity).getUnit());
				feedbackFragment.setArguments(bundle);
				mapActivity.addFragment(feedbackFragment, false,
						AndyConstants.FEEDBACK_FRAGMENT_TAG, true);
			}
			break;

		case AndyConstants.ServiceCode.GET_ROUTE:
			// if (parseContent.isSuccess(response)) {
			// jobStatus = AndyConstants.;
			// setjobStatus(jobStatus);
			// }
			break;
		case AndyConstants.ServiceCode.PATH_REQUEST:
			AppLog.Log(TAG, "Path request :" + response);
			if (parseContent.isSuccess(response)) {
				ArrayList<LatLng> points = new ArrayList<LatLng>();
				parseContent.parsePathRequest(response, points);
				locationHelper.initPreviousDrawPath(points);
			}
			break;
		default:
			break;
		}
	}

	private void animateMarker(final Marker marker, final LatLng toPosition,
								final boolean hideMarker) {
		final Handler handler = new Handler();
		final long start = SystemClock.uptimeMillis();
		Projection proj = locationHelper.getGoogleMap().getProjection();
		Point startPoint = proj.toScreenLocation(marker.getPosition());
		final LatLng startLatLng = proj.fromScreenLocation(startPoint);
		final long duration = 2000;

		final Interpolator interpolator = new LinearInterpolator();
		handler.post(new Runnable() {
			@Override
			public void run() {
				long elapsed = SystemClock.uptimeMillis() - start;
				float t = interpolator.getInterpolation((float) elapsed
						/ duration);
//				double lng = t * toPosition.longitude + (1 - t)
//						* startLatLng.longitude;
//				double lat = t * toPosition.latitude + (1 - t)
//						* startLatLng.latitude;
//				marker.setPosition(new LatLng(lat, lng));

				if (t < 1.0) {
					// Post again 16ms later.
					handler.postDelayed(this, 16);
				} else {
					if (hideMarker) {
						marker.setVisible(false);
					} else {
						marker.setVisible(true);
					}
					float bearingL = bearingBetweenLatLngs(startLatLng, toPosition);

					CameraPosition cameraPosition =
							new CameraPosition.Builder()
									.target(toPosition)
									.bearing(bearingL + BEARING_OFFSET)
									.tilt(90)
									.build();

					locationHelper.getGoogleMap().animateCamera(
							CameraUpdateFactory.newCameraPosition(cameraPosition),
							ANIMATE_SPEEED_TURN,
							null
					);
				}
			}
		});
	}

	private Location convertLatLngToLocation(LatLng latLng) {
		Location location = new Location("someLoc");
		location.setLatitude(latLng.latitude);
		location.setLongitude(latLng.longitude);
		return location;
	}

	private float bearingBetweenLatLngs(LatLng beginLatLng,LatLng endLatLng) {
		Location beginLocation = convertLatLngToLocation(beginLatLng);
		Location endLocation = convertLatLngToLocation(endLatLng);
		return beginLocation.bearingTo(endLocation);
	}

	/**
	 * 
	 */
	private void startElapsedTimer() {
		if (elapsedTimer != null) {
			elapsedTimer.cancel();
			elapsedTimer = null;
		}
		elapsedTimer = new Timer();
		elapsedTimer.scheduleAtFixedRate(new TimerRequestStatus(),
				AndyConstants.DELAY, ELAPSED_TIME_SCHEDULE);
	}

	private void stopElapsedTimer() {
		if (elapsedTimer != null) {
			elapsedTimer.cancel();
			elapsedTimer = null;
		}
	}

	private class TimerRequestStatus extends TimerTask {
		@Override
		public void run() {
			// isContinueRequest = false;
			AppLog.Log(TAG, "In elapsed time timer");
			mapActivity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (isVisible()) {
						try {
							TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
							Date date = new Date();
							SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
									Locale.US);
							String strDateTime = sdf.format(date);
							long curTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
									Locale.US).parse(strDateTime).getTime();
							if (PreferenceHelper.getInstance(mapActivity).getRequestTime() == AndyConstants.NO_TIME) {
								PreferenceHelper.getInstance(mapActivity).putRequestTime(curTime);
							}
							time = String.valueOf((curTime - PreferenceHelper.getInstance(mapActivity)
									.getRequestTime())
									/ (1000 * 60));
							tvJobTime.setText(time
									+ " "
									+ mapActivity.getResources().getString(
									R.string.text_mins));
						} catch (ParseException e) {
							e.printStackTrace();
						}
					}
				}
			});

		}
	}

	private void showLocationMarkerWindow() {
		if (markerClientLocation == null) {
			if (requestDetail.getClientLatitude() != null
					&& requestDetail.getClientLongitude() != null) {
				markerClientLocation = locationHelper.getGoogleMap().addMarker(new MarkerOptions()
						.position(
								new LatLng(Double.parseDouble(requestDetail
										.getClientLatitude()), Double
										.parseDouble(requestDetail
												.getClientLongitude()))).icon(
								BitmapDescriptorFactory
										.fromResource(R.drawable.pin_client)));
				if (jobStatus == AndyConstants.IS_WALK_COMPLETED) {
					markerClientLocation.setTitle(mapActivity.getResources()
							.getString(R.string.job_start_location));
				} else {
					markerClientLocation.setTitle(mapActivity.getResources()
							.getString(R.string.client_location));
				}
				markerClientLocation.showInfoWindow();
			}
		} else {
			markerClientLocation.showInfoWindow();
		}
		locationHelper.getGoogleMap().animateCamera(CameraUpdateFactory
				.newLatLngZoom(new LatLng(Double.parseDouble(requestDetail
						.getClientLatitude()), Double
						.parseDouble(requestDetail
								.getClientLongitude())), 14));
	}

	private void showDestinationMarkerWindow() {
		if (markerClient_d_location == null) {
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
										.fromResource(R.drawable.pin_client_dest))
								.title("Destination"));
				markerClient_d_location.showInfoWindow();
			}
		} else {
			markerClient_d_location.showInfoWindow();
		}
	}

	@Override
	public void onLocationReceived(LatLng latlong) {
		if (locationHelper.getGoogleMap() == null) {
			return;
		}

		showDestinationMarkerWindow();

		if (jobStatus < AndyConstants.IS_WALK_STARTED) {
			showLocationMarkerWindow();
		}

		if (latlong != null) {
			if (locationHelper.getGoogleMap() != null) {
				if (locationHelper.getMarkerDriverLocation() == null) {
					Marker marker = locationHelper.getGoogleMap()
							.addMarker(new MarkerOptions()
									.position(
											new LatLng(latlong.latitude,
													latlong.longitude))
									.icon(BitmapDescriptorFactory
											.fromResource(R.drawable.pin_driver))
									.title(getResources().getString(
											R.string.my_location)));
					locationHelper.setMarkerDriverLocation(marker);
				} else {
					locationHelper.getMarkerDriverLocation().setPosition(new LatLng(
							latlong.latitude, latlong.longitude));
					if (jobStatus == AndyConstants.IS_WALK_COMPLETED) {
						locationHelper.drawTrip(new LatLng(latlong.latitude, latlong.longitude));

						// distance = decimalFormat.format(distanceMeter / (1000
						// * 1.62));

						// tvJobDistance.setText(decimalFormat
						// .format(preferenceHelper.getDistance()
						// / (1000 * 1.62))
						// + " "
						// + mapActivity.getResources().getString(
						// R.string.text_miles));

						tvJobDistance.setText(decimalFormat
								.format(PreferenceHelper.getInstance(mapActivity).getDistance()
								// / (1000 * 1.62))
								) + " " + PreferenceHelper.getInstance(mapActivity).getUnit());

					}
				}
				// getDistance();
			}
		}
		setjobStatus(jobStatus);
	}

	// private void getDistance() {
	// if (googleMap == null || markerDriverLocation == null) {
	// return;
	// }
	// if (jobStatus == AndyConstants.IS_WALK_COMPLETED) {
	//
	// ArrayList<LatLng> latLngs = dbHelper.getLocations();
	// int distanceMeter = 0;
	// if (latLngs.size() >= 2) {
	// for (int i = 0; i < latLngs.size() - 1; i++) {
	// Location location1 = new Location("");
	// Location location2 = new Location("");
	// location1.setLatitude(latLngs.get(i).latitude);
	// location1.setLongitude(latLngs.get(i).longitude);
	// location2.setLatitude(latLngs.get(i +
	// 1).latitude);googleMap.setInfoWindowAdapter(this);
	// location2.setLongitude(latLngs.get(i + 1).longitude);
	// distanceMeter = distanceMeter
	// + (int) location1.distanceTo(location2);
	//
	// }
	// }
	// // AndyUtils.showToast("Meter:" + distanceMeter, mapActivity);
	// DecimalFormat decimalFormat = new DecimalFormat("0.00");
	// distance = decimalFormat.format(distanceMeter / (1000 * 1.62));
	// tvJobDistance
	// .setText(distance
	// + " "
	// + mapActivity.getResources().getString(
	// R.string.text_miles));
	// // Location jobStartLocation = new Location("");
	// // Location currentLocation = new Location("");
	// // jobStartLocation.setLatitude(Double.parseDouble(requestDetail
	// // .getClientLatitude()));
	// // jobStartLocation.setLongitude(Double.parseDouble(requestDetail
	// // .getClientLongitude()));
	// // currentLocation
	// // .setLatitude(markerDriverLocation.getPosition().latitude);
	// // currentLocation
	// // .setLongitude(markerDriverLocation.getPosition().longitude);
	// // AppLog.Log(TAG, jobStartLocation.distanceTo(currentLocation)
	// // + " METERS ");
	// // int distanceMeter = (int) jobStartLocation
	// // .distanceTo(currentLocation);
	// // DecimalFormat decimalFormat = new DecimalFormat("0.0");
	// // distance = decimalFormat.format(distanceMeter / (1000 * 1.62));
	// // tvJobDistance
	// // .setText(distance
	// // + " "
	// // + mapActivity.getResources().getString(
	// // R.string.text_miles));
	// }
	// }
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		registerCancelReceiver();
	}

	private void registerCancelReceiver(){
		IntentFilter intentFilter = new IntentFilter("CANCEL_REQUEST");
		mReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				AppLog.Log("JobFragment", "CANCEL_REQUEST");
				stopElapsedTimer();
				mapActivity.startActivity(new Intent(mapActivity,
						MapActivity.class));
				mapActivity.finish();
			}
		};
		mapActivity.registerReceiver(mReceiver, intentFilter);
	}
}
