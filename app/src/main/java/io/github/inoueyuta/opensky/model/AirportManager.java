package io.github.inoueyuta.opensky.model;

import org.json.JSONArray;
import org.json.JSONObject;


import java.util.ArrayList;
import java.util.List;

public class AirportManager {
    public ShowAirportInterface showAirportInterface;
    public static List<AirportInfo> airportInfos = new ArrayList<AirportInfo>();

    public void getAirportInfo(double topLatitude, double bottomLatitude, double leftLongitude, double rightLongitude) {
        new getAirportInfoTask().execute("https://opensky-network.org/api/airports/region?lamin=" + String.valueOf(bottomLatitude) +"&lomin=" + String.valueOf(leftLongitude)+ "&lamax=" + String.valueOf(topLatitude) + "&lomax=" + String.valueOf(rightLongitude));
    }

    public class getAirportInfoTask extends requestAPITask {
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                JSONArray array = new JSONArray(s);
                airportInfos.clear();
                for (int i = 0; i < array.length(); i++) {
                    JSONObject jsonResult = array.getJSONObject(i);
                    AirportInfo info  = new AirportInfo();
                    info.setIcao(jsonResult.getString("icao"));
                    info.setIata(jsonResult.getString("iata"));
                    info.setName(jsonResult.getString("name"));
                    info.setCity(jsonResult.getString("city"));
                    info.setType(jsonResult.getString("type"));
                    JSONObject position = jsonResult.getJSONObject("position");
                    info.setLatitude(position.getDouble("latitude"));
                    info.setLongitude(position.getDouble("longitude"));
                    airportInfos.add(info);
                }
                showAirportInterface.showAirport();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}