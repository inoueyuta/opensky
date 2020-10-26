package io.github.inoueyuta.opensky.model;

import org.json.JSONArray;
import org.json.JSONObject;

public class SelectedAirplaneManager {
    public ShowRouteInterface showRouteInterface;
    private AirportInfo departureAirport;
    private AirportInfo arrivalAirport;
    private AirplaneState airplaneState;

    public void setAirplane(AirplaneState airplaneState) {
        this.airplaneState = airplaneState;
    }

    public void clear() {
        airplaneState = null;
        departureAirport = null;
        arrivalAirport = null;
    }

    public String getSnippet() {
        return String.format(AirplaneState.airplaneInfoTemplate, departureAirport.getShortAirportName(), arrivalAirport.getShortAirportName(),
                airplaneState.getIcao24(), airplaneState.getOrigin_country(), airplaneState.getBaro_altitude(), airplaneState.getGeo_altitude(), airplaneState.getVelocity(), airplaneState.getVertical_rate());
    }
    public AirportInfo getDepartureAirport() {
        return departureAirport;
    }
    public AirportInfo getArrivalAirport() {
        return arrivalAirport;
    }
    public AirplaneState getAirplaneState() {
        return airplaneState;
    }
    public boolean existDepartureAndArrivalAirport() {
        return (arrivalAirport != null) && (departureAirport != null);
    }
    public void getDepartureAndArrivalAirport() {
        new getDepartureAirportAndArrivalAirportTask().execute("https://opensky-network.org/api/routes?callsign=" + airplaneState.getCallsign());
    }

    public class getDepartureAirportAndArrivalAirportTask extends requestAPITask {
        @Override
        protected String doInBackground(String... urls) {
            try {
                String r0 = super.requestAPI(urls[0]);
                JSONObject jsonResult = new JSONObject(r0);
                JSONArray route = jsonResult.getJSONArray("route");
                String departureAirportString = route.getString(0);
                String arrivalAirportString = route.getString(1);
                String r1 = super.requestAPI("https://opensky-network.org/api/airports/?icao=" + departureAirportString);
                departureAirport = getAirportInfo(r1);
                String r2 = super.requestAPI("https://opensky-network.org/api/airports/?icao=" + arrivalAirportString);
                arrivalAirport = getAirportInfo(r2);
                return "";
            } catch (Exception e) {
                departureAirport = null;
                arrivalAirport = null;
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            showRouteInterface.showRoute();
        }
    }

    public AirportInfo getAirportInfo(String result) {
        try {
            JSONObject jsonResult = new JSONObject(result);
            AirportInfo info  = new AirportInfo();
            info.setIcao(jsonResult.getString("icao"));
            info.setIata(jsonResult.getString("iata"));
            info.setName(jsonResult.getString("name"));
            info.setCity(jsonResult.getString("city"));
            info.setType(jsonResult.getString("type"));
            JSONObject position = jsonResult.getJSONObject("position");
            info.setLatitude(position.getDouble("latitude"));
            info.setLongitude(position.getDouble("longitude"));
            return info;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
