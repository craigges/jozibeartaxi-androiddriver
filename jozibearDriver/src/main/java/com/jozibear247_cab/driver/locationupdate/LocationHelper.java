package com.jozibear247_cab.driver.locationupdate;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.jozibear247_cab.driver.MapActivity;
import com.jozibear247_cab.driver.R;
import com.jozibear247_cab.driver.base.BaseMapFragment;
import com.jozibear247_cab.driver.widget.MyFontTextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LocationHelper implements LocationListener,
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener {

	private LocationRequest mLocationRequest;
	private LocationClient mLocationClient;
	private LocationReceiveListener locationReceiveListener;
	private MapActivity mapActivity;
	private Context context;
	private LatLng latLong;
	private GoogleMap mMap;
	private Marker markerDriverLocation;
	private ArrayList<LatLng> points;
	private PolylineOptions lineOptions;
	private boolean bJobFragment = false;

	public LocationHelper(MapActivity context, boolean bJob) {
		this.mapActivity = context;
		this.context = context;
		this.bJobFragment = bJob;
		points = new ArrayList<LatLng>();
		mLocationRequest = LocationRequest.create();
		// Set the update interval
		mLocationRequest
				.setInterval(LocationUtils.UPDATE_INTERVAL_IN_MILLISECONDS);
		// Use high accuracy
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		// Set the interval ceiling to one minute
		mLocationRequest
				.setFastestInterval(LocationUtils.FAST_INTERVAL_CEILING_IN_MILLISECONDS);
		mLocationClient = new LocationClient(context, this, this);
	}

	public LocationHelper(Context context) {
		this.mapActivity = null;
		this.context = context;
		mLocationRequest = LocationRequest.create();
		// Set the update interval
		mLocationRequest
				.setInterval(LocationUtils.UPDATE_INTERVAL_IN_MILLISECONDS);
		// Use high accuracy
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		// Set the interval ceiling to one minute
		mLocationRequest
				.setFastestInterval(LocationUtils.FAST_INTERVAL_CEILING_IN_MILLISECONDS);
		mLocationClient = new LocationClient(context, this, this);
	}

	public void setLocationReceiveListener(LocationReceiveListener listener) {
		this.locationReceiveListener = listener;
	}

	public LatLng getCurrentLocation() {
		return latLong;
	}

	public LatLng getLastLocation() {

		// If Google Play Services is available
		if (servicesConnected()) {
			// Get the current location
			Location currentLocation = mLocationClient.getLastLocation();
			// Display the current location in the UI
			latLong = LocationUtils.getLatLng(currentLocation);
		}

		return latLong;
	}

	public void onStart() {
		mLocationClient.connect();
	}

	public void onStop() {
		// If the client is connected
		if (mLocationClient.isConnected()) {
			stopPeriodicUpdates();
		}

		// After disconnect() is called, the client is considered "dead".
		mLocationClient.disconnect();
	}

	private boolean servicesConnected() {

		// Check that Google Play services is available
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(context);

		// If Google Play services is available
		if (ConnectionResult.SUCCESS == resultCode) {
			// Continue
			return true;
			// Google Play services was not available for some reason
		} else {
			// Display an error dialog
			return false;
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		latLong = LocationUtils.getLatLng(location);

		if (locationReceiveListener != null && latLong != null) {
			locationReceiveListener.onLocationReceived(latLong);
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		if(mapActivity != null) {
			Location location = mLocationClient.getLastLocation();
			if (location != null) {
				if (mMap != null) {
					if (markerDriverLocation == null) {
						markerDriverLocation = mMap
								.addMarker(new MarkerOptions()
										.position(
												new LatLng(
														location.getLatitude(),
														location.getLongitude()))
										.icon(BitmapDescriptorFactory
												.fromResource(R.drawable.pin_driver))
										.title(mapActivity.getResources()
												.getString(
														R.string.my_location)));
						mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
								new LatLng(location.getLatitude(),
										location.getLongitude()),
								16));
					} else {
						markerDriverLocation
								.setPosition(new LatLng(location
										.getLatitude(), location
										.getLongitude()));
					}
				}
			} else {
				if(!bJobFragment)
					showLocationOffDialog();
			}
		}
		startPeriodicUpdates();
	}

	private void showLocationOffDialog() {
		AlertDialog.Builder gpsBuilder = new AlertDialog.Builder(mapActivity);
		gpsBuilder.setCancelable(false);
		gpsBuilder
				.setTitle(mapActivity.getString(R.string.dialog_no_location_service_title))
				.setMessage(mapActivity.getString(R.string.dialog_no_location_service))
				.setPositiveButton(
						mapActivity.getString(R.string.dialog_enable_location_service),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
												int which) {
								// continue with delete
								dialog.dismiss();
								Intent viewIntent = new Intent(
										Settings.ACTION_LOCATION_SOURCE_SETTINGS);
								((BaseMapFragment)locationReceiveListener).startActivity(viewIntent);

							}
						})

				.setNegativeButton(mapActivity.getString(R.string.dialog_exit),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
												int which) {
								// do nothing
								dialog.dismiss();
								mapActivity.finish();
							}
						});
		gpsBuilder.create();
		gpsBuilder.show();
	}

	public void setUpMap() {
		// Do a null check to confirm that we have not already instantiated the
		// map.
		if (mMap == null) {
			if(bJobFragment) {
				mMap = ((SupportMapFragment) mapActivity
						.getSupportFragmentManager().findFragmentById(R.id.jobMap))
						.getMap();
				initPreviousDrawPath(null);
			} else {
				mMap = ((SupportMapFragment) mapActivity
						.getSupportFragmentManager().findFragmentById(
								R.id.clientReqMap)).getMap();
				mMap.getUiSettings().setZoomControlsEnabled(false);
				mMap.setMyLocationEnabled(false);
				mMap.getUiSettings().setMyLocationButtonEnabled(false);
			}
			mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

				// Use default InfoWindow frame

				@Override
				public View getInfoWindow(Marker marker) {
					View v = mapActivity.getLayoutInflater().inflate(
							R.layout.info_window_layout, null);
					MyFontTextView title = (MyFontTextView) v
							.findViewById(R.id.markerBubblePickMeUp);
					MyFontTextView content = (MyFontTextView) v
							.findViewById(R.id.infoaddress);
					title.setText(marker.getTitle());

					getAddressFromLocation(marker.getPosition(), content);

					// ((TextView) v).setText(marker.getTitle());
					return v;
				}

				// Defines the contents of the InfoWindow

				@Override
				public View getInfoContents(Marker marker) {

					// Getting view from the layout file info_window_layout View

					// Getting reference to the TextView to set title TextView

					// Returning the view containing InfoWindow contents return
					return null;

				}

			});

			mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
				@Override
				public boolean onMarkerClick(Marker marker) {
					marker.showInfoWindow();
					return true;
				}
			});
//			addMarker();
			if(mMap == null) {
				setUpMap();
			}
		}
	}
	/* added by amal */

	private void getAddressFromLocation(final LatLng latlng,
										final MyFontTextView et) {
		String strAddress = null;
		Geocoder gCoder = new Geocoder(mapActivity);
		Log.d("hey", String.valueOf(strAddress));
		try {
			final List<Address> list = gCoder.getFromLocation(latlng.latitude,
					latlng.longitude, 1);
			if (list != null && list.size() > 0) {
				Address address = list.get(0);
				StringBuilder sb = new StringBuilder();
				if (address.getAddressLine(0) != null) {

					sb.append(address.getAddressLine(0)).append(", ");
				}
				sb.append(address.getLocality()).append(", ");
				// sb.append(address.getPostalCode()).append(",");
				sb.append(address.getCountryName());
				strAddress = sb.toString();

				strAddress = strAddress.replace(",null", "");
				strAddress = strAddress.replace("null", "");
				strAddress = strAddress.replace("Unnamed", "");
				if (!TextUtils.isEmpty(strAddress)) {

					et.setText(strAddress);

				}
			}
			Log.d("hey", strAddress);

		} catch (IOException exc) {
			exc.printStackTrace();
		}

	}

	public void initPreviousDrawPath(ArrayList<LatLng> points) {
		if(points == null) {
			points = new ArrayList<LatLng>();
		}
		this.points = points;
		// points = dbHelper.getLocations();
		lineOptions = new PolylineOptions();
		lineOptions.addAll(this.points);
		lineOptions.width(15);
		lineOptions.color(context.getResources().getColor(R.color.skyblue));
		mMap.addPolyline(lineOptions);
		this.points.clear();
	}

	public void drawTrip(LatLng latlng) {

		if (mMap != null) {
			// setMarker(latlng);
			this.points.add(latlng);
			// dbHelper.addLocation(latlng);
			lineOptions = new PolylineOptions();
			lineOptions.addAll(this.points);
			lineOptions.width(15);
			lineOptions.color(context.getResources().getColor(R.color.skyblue));

			mMap.addPolyline(lineOptions);
		}
	}

	public GoogleMap getGoogleMap() {
		return mMap;
	}

	public void setGoogleMap(GoogleMap map) {
		this.mMap = map;
	}

	public Marker getMarkerDriverLocation() {
		return markerDriverLocation;
	}

	public void setMarkerDriverLocation(Marker marker) {
		this.markerDriverLocation = marker;
	}

	@Override
	public void onDisconnected() {
	}

	private void startPeriodicUpdates() {
		mLocationClient.requestLocationUpdates(mLocationRequest, this);
	}

	/**
	 * In response to a request to stop updates, send a request to Location
	 * Services
	 */
	private void stopPeriodicUpdates() {
		mLocationClient.removeLocationUpdates(this);
	}

	public static class ErrorDialogFragment extends DialogFragment {

		// Global field to contain the error dialog
		private Dialog mDialog;

		/**
		 * Default constructor. Sets the dialog field to null
		 */
		public ErrorDialogFragment() {
			super();
			mDialog = null;
		}

		/**
		 * Set the dialog to display
		 * 
		 * @param dialog
		 *            An error dialog
		 */
		public void setDialog(Dialog dialog) {
			mDialog = dialog;
		}

		/*
		 * This method must return a Dialog to the DialogFragment.
		 */
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return mDialog;
		}
	}
}
