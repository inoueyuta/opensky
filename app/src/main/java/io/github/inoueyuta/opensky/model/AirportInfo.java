package io.github.inoueyuta.opensky.model;

import com.google.android.gms.maps.model.LatLng;

public class AirportInfo {
    private String icao;
    private String iata;
    private String name;
    private String city;
    private String type;
    private double longitude;
    private double latitude;

    public String getShortAirportName() {
        if(name.contains(" International Airport")) {
            return name.substring(0, name.length() - " International Airport".length());
        } else if (name.contains(" Airport")) {
            return name.substring(0, name.length() - " Airport".length());
        } else {
            return name;
        }
    }
    public String getIcao() {
        return icao;
    }
    public void setIcao(String icao) {
        this.icao = icao;
    }
    public String getIata() {
        return iata;
    }
    public void setIata(String iata) {
        this.iata = iata;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getCity() {
        return city;
    }
    public void setCity(String city) {
        this.city = city;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public LatLng getLocation() {
        return new LatLng( this.latitude, this.longitude);
    }
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

}
