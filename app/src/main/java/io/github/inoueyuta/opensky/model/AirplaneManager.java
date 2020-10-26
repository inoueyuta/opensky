package io.github.inoueyuta.opensky.model;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class AirplaneManager {
    public ShowAirplaneInterface showAirplaneInterface;
    public static List<AirplaneState> airplaneStates = new ArrayList<AirplaneState>();

    public void getAirplaneInfo(double topLatitude, double bottomLatitude, double leftLongitude, double rightLongitude) {
        new getAirplaneInfoTask().execute("https://opensky-network.org/api/states/all?lamin=" + String.valueOf(bottomLatitude) +"&lomin=" + String.valueOf(leftLongitude)+ "&lamax=" + String.valueOf(topLatitude) + "&lomax=" + String.valueOf(rightLongitude));
    }

    public class getAirplaneInfoTask extends requestAPITask {
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                JSONObject jsonObject = new JSONObject(s);
                String airplaneInfos = jsonObject.getString("states");
                if(airplaneInfos != "null") {
                    JSONArray arr = new JSONArray(airplaneInfos);
                    airplaneStates.clear();
                    for (int i = 0; i < arr.length(); i++) {
                        JSONArray jsonResult = arr.getJSONArray(i);
                        AirplaneState airplaneInfo  = new AirplaneState();
                        airplaneInfo.setIcao24((String) jsonResult.get(0));
                        airplaneInfo.setCallsign((String) jsonResult.get(1));
                        airplaneInfo.setLatitude(String.valueOf(jsonResult.get(6)));
                        airplaneInfo.setLongitude(String.valueOf(jsonResult.get(5)));
                        airplaneInfo.setTrue_track(String.valueOf(jsonResult.get(10)));
                        airplaneInfo.setBaro_altitude(String.valueOf(jsonResult.get(7)));
                        airplaneInfo.setGeo_altitude(String.valueOf(jsonResult.get(13)));
                        airplaneInfo.setOrigin_country(String.valueOf(jsonResult.get(2)));
                        airplaneInfo.setVelocity(String.valueOf(jsonResult.get(9)));
                        airplaneInfo.setVertical_rate(String.valueOf(jsonResult.get(11)));
                        airplaneStates.add(airplaneInfo);
                    }
                    showAirplaneInterface.showAirplane();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

