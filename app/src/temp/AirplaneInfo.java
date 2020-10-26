package io.github.inoueyuta.opensky;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class AirplaneInfo {
    private String callsign;
    private String longitude;
    private String latitude;
    private Float true_track;

    public String getCallsign() {
        return callsign;
    }
    public void setCallsign(String callsign) {
        this.callsign = callsign;
    }
    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }
    public LatLng getLocation() {
        return new LatLng( Double.valueOf(this.latitude), Double.valueOf(this.longitude));
    }
    public Float getTrue_track() {
        return true_track;
    }
    public void setTrue_track(String true_track) {
        this.true_track = Float.valueOf(true_track);
    }
}
