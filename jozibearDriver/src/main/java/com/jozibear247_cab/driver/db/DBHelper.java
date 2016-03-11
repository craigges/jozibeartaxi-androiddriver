package com.jozibear247_cab.driver.db;

import android.content.Context;

import com.jozibear247_cab.driver.model.User;

public class DBHelper {

	public static DBHelper instance = null;
	private DBAdapter dbAdapter;

	private DBHelper(Context context) {
		dbAdapter = new DBAdapter(context);
	}

	public static synchronized DBHelper getInstance(Context context) {
		if(instance == null) {
			instance = new DBHelper(context);
		}
//		instance.dbAdapter = new DBAdapter(context);

		return instance;
	}

	// public void addLocation(LatLng latLng) {
	// dbAdapter.open();
	// dbAdapter.addLocation(latLng);
	// dbAdapter.close();
	// }
	//
	// public ArrayList<LatLng> getLocations() {
	// dbAdapter.open();
	// ArrayList<LatLng> points = dbAdapter.getLocations();
	// dbAdapter.close();
	// return points;
	// }

	// public int deleteAllLocations() {
	// int count = 0;
	// dbAdapter.open();
	// count = dbAdapter.deleteAllLocations();
	// dbAdapter.close();
	// return count;
	//
	// }

	// public boolean isLocationsExists() {
	// boolean isExists = false;
	// dbAdapter.open();
	// isExists = dbAdapter.isLocationsExists();
	// dbAdapter.close();
	// return isExists;
	// }

	public long createUser(User user) {
		long count = 0;
		dbAdapter.open();
		count = dbAdapter.createUser(user);
		dbAdapter.close();
		return count;

	}

	public User getUser() {
		User user = null;
		dbAdapter.open();
		user = dbAdapter.getUser();
		dbAdapter.close();
		return user;
	}

	public int deleteUser() {
		int count = 0;
		dbAdapter.open();
		count = dbAdapter.deleteUser();
		dbAdapter.close();
		return count;
	}

}
