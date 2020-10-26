package io.github.inoueyuta.opensky.model;

import com.google.android.gms.maps.model.LatLng;

public class AirplaneState {
    public static final String airplaneInfoTemplate = "出発: %s\n"
            + "到着: %s\n"
            + "機体番号: %s\n"
            + "国名: %s\n"
            + "気圧高度: %sm\n"
            + "幾何学的高度: %sm\n"
            + "速度: %sm/s\n"
            + "上下の速度: %sm/s";
    //期待番号
    private String icao24;
    //便名
    private String callsign;
    private String longitude;
    private String latitude;
    //北からの角度
    private float true_track;
    //国
    private String origin_country;
    //気圧高度
    private float baro_altitude;
    //速度　m/s
    private float velocity;
    //上下方向の速度 m/s
    private  float vertical_rate;
    //高度
    private float geo_altitude;

    public String getTitle() {
        return "便名: " + getCallsign();
    }
    public String getSnippet() {
        return String.format(airplaneInfoTemplate, "", "",
                getIcao24(), getOrigin_country(), getBaro_altitude(), getGeo_altitude(), getVelocity(), getVertical_rate());
    }
    public String getIcao24() {
        return icao24;
    }
    public void setIcao24(String icao24) {
        this.icao24 = icao24;
    }
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
    public float getTrue_track() {
        return true_track;
    }
    public void setTrue_track(String true_track) {
        this.true_track = Float.valueOf(true_track);
    }
    public String getOrigin_country() {
        return origin_country;
    }
    public void setOrigin_country(String origin_country) {
        this.origin_country = origin_country;
    }
    public float getBaro_altitude() {
        return baro_altitude;
    }
    public void setBaro_altitude(String baro_altitude) {
        if(!"null".equals(baro_altitude)){
            this.baro_altitude = Float.valueOf(baro_altitude);
        }
    }
    public float getVelocity() {
        return velocity;
    }
    public void setVelocity(String velocity) {
        if(!"null".equals(velocity)){
            this.velocity = Float.valueOf(velocity);
        }
    }
    public float getVertical_rate() {
        return vertical_rate;
    }
    public void setVertical_rate(String vertical_rate) {
        if(!"null".equals(vertical_rate)){
            this.vertical_rate = Float.valueOf(vertical_rate);
        }
    }
    public float getGeo_altitude() {
        return geo_altitude;
    }
    public void setGeo_altitude(String geo_altitude) {
        if(!"null".equals(geo_altitude)){
            this.geo_altitude = Float.valueOf(geo_altitude);
        }
    }
}
