package com.jozibear247_cab.driver.locationupdate;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by YNR on 2016-03-01.
 */
public interface LocationReceiveListener {
    public void onLocationReceived(LatLng latlong);
}
