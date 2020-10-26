package io.github.inoueyuta.opensky;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnCameraIdleListener {

    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;
    Timer getPlaneLocationTimer;
    private List<Marker> airPlaneMarkerArray = new ArrayList<Marker>();

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if( requestCode == 1 ) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 10000, 1000, locationListener);
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 1000, locationListener);
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnCameraIdleListener(this);
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Toast.makeText(MapsActivity.this, location.toString(), Toast.LENGTH_SHORT).show();
                updateLocation(location);
            }
            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
            }
            @Override
            public void onProviderEnabled(String s) {
            }
            @Override
            public void onProviderDisabled(String s) {
            }
        };

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 10000, 1000, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 1000, locationListener);
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLocation == null) {
                lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            if (lastKnownLocation != null) {
                updateLocation(lastKnownLocation);
            }
        }
    }

    private void updateLocation(Location location) {
        mMap.clear();
        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.addMarker(new MarkerOptions().position(userLocation).title("Your Location!"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 10));
    }

    @Override
    public void onCameraIdle() {
        Projection proj = mMap.getProjection();
        VisibleRegion vRegion = proj.getVisibleRegion();
        final double topLatitude = vRegion.latLngBounds.northeast.latitude;
        final double bottomLatitude = vRegion.latLngBounds.southwest.latitude;
        final double leftLongitude = vRegion.latLngBounds.southwest.longitude;
        final double rightLongitude = vRegion.latLngBounds.northeast.longitude;

        if(getPlaneLocationTimer != null) {
            getPlaneLocationTimer.cancel();
        }
        getPlaneLocationTimer = new Timer();
        getPlaneLocationTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new DownloadTask().execute("https://opensky-network.org/api/states/all?lamin=" + String.valueOf(bottomLatitude) +"&lomin=" + String.valueOf(leftLongitude)+ "&lamax=" + String.valueOf(topLatitude) + "&lomax=" + String.valueOf(rightLongitude));
                    }
                });
            }
        }, 0, 10000);
    }

    public class DownloadTask extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... urls) {
            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;
            try {
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();
                while (data != -1) {
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }
                return result;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            for (int i = 0 ; i < airPlaneMarkerArray.size() ; i++){
                airPlaneMarkerArray.get(i).remove();
            }
            airPlaneMarkerArray.clear();
            try {
                JSONObject jsonObject = new JSONObject(s);
                String airplaneInfos = jsonObject.getString("states");
                if(airplaneInfos != "null") {
                    JSONArray arr = new JSONArray(airplaneInfos);
                    for (int i = 0; i < arr.length(); i++) {
                        JSONArray planeInfo = arr.getJSONArray(i);
                        Log.i("callsign", (String) planeInfo.get(1));
                        String longitude = String.valueOf(planeInfo.get(5));
                        String latitude = String.valueOf(planeInfo.get(6));
                        Log.i("longitude", longitude);
                        Log.i("latitude", latitude);
                        String true_track = String.valueOf(planeInfo.get(10));
                        Log.i("true_track", true_track);
                        LatLng planeLocation = new LatLng(Double.valueOf(latitude), Double.valueOf(longitude));
                        MarkerOptions options = new MarkerOptions().position(planeLocation).icon(BitmapDescriptorFactory.fromResource(R.drawable.airplane)).rotation( Float.valueOf(true_track)).title((String) planeInfo.get(1));
                        Marker marker = mMap.addMarker(options);
                        airPlaneMarkerArray.add(marker);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}