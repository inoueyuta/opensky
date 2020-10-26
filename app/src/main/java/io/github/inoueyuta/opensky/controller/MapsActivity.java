package io.github.inoueyuta.opensky.controller;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.VisibleRegion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.github.inoueyuta.opensky.R;
import io.github.inoueyuta.opensky.model.AirplaneManager;
import io.github.inoueyuta.opensky.model.AirplaneState;
import io.github.inoueyuta.opensky.model.AirportInfo;
import io.github.inoueyuta.opensky.model.AirportManager;
import io.github.inoueyuta.opensky.model.SelectedAirplaneManager;
import io.github.inoueyuta.opensky.model.ShowAirplaneInterface;
import io.github.inoueyuta.opensky.model.ShowAirportInterface;
import io.github.inoueyuta.opensky.model.ShowRouteInterface;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnCameraIdleListener, ShowAirplaneInterface, ShowAirportInterface, ShowRouteInterface {
    private GoogleMap mMap;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Timer getAirPlaneStateTimer;
    private Timer getAirplaceInfoTimer;

    private AirplaneManager airplaneManager;
    private AirportManager airportManager;
    private SelectedAirplaneManager selectedAirplaneManager;

    private List<Marker> airplaneMarkerArray = new ArrayList<Marker>();
    private Marker selectedAirplaneMarker;
    private List<Marker> airportMarkerArray = new ArrayList<Marker>();
    private Marker selectedAirportMarker;
    private HashMap<String, AirplaneState> airplaneStateHash = new HashMap<String, AirplaneState>();
    private Polyline departureAirportRoute;
    private Polyline arrivalAirportRoute;

    private static final String markerAirplaneTagName = "airplane";
    private static final String markerAirportTagName = "airport";

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if( requestCode == 1 ) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestSingleUpdate(locationManager.GPS_PROVIDER, locationListener, null);
                    // 現在値を表示
                    mMap.setMyLocationEnabled(true);
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        airplaneManager = new AirplaneManager();
        airplaneManager.showAirplaneInterface = this;
        airportManager = new AirportManager();
        airportManager.showAirportInterface = this;
        selectedAirplaneManager = new SelectedAirplaneManager();
        selectedAirplaneManager.showRouteInterface = this;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // 現在地ボタンを表示
        UiSettings settings = mMap.getUiSettings();
        settings.setMyLocationButtonEnabled(true);
        //markerのsnippetに改行を含めた情報を入れるためのカスタマイズ
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }
            @Override
            public View getInfoContents(Marker marker) {
                LinearLayout info = new LinearLayout(MapsActivity.this);
                info.setOrientation(LinearLayout.VERTICAL);

                TextView title = new TextView(MapsActivity.this);
                title.setTextColor(Color.BLACK);
                title.setGravity(Gravity.CENTER);
                title.setTypeface(null, Typeface.BOLD);
                title.setText(marker.getTitle());

                TextView snippet = new TextView(MapsActivity.this);
                snippet.setTextColor(Color.GRAY);
                snippet.setText(marker.getSnippet());

                info.addView(title);
                if(markerAirplaneTagName.equals(marker.getTag())) {
                    info.addView(snippet);
                }
                return info;
            }
        });
        mMap.setOnCameraIdleListener(this);
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if(markerAirplaneTagName.equals(marker.getTag())) {
                    clearRoute();
                    selectedAirplaneManager.clear();
                    AirplaneState state = airplaneStateHash.get(marker.getId());
                    selectedAirplaneManager.setAirplane(state);
                    selectedAirplaneManager.getDepartureAndArrivalAirport();
                    selectedAirplaneMarker = marker;
                    selectedAirportMarker = null;
                } else if(markerAirportTagName.equals(marker.getTag())) {
                    selectedAirportMarker = marker;
                    selectedAirplaneMarker = null;
                }
                // タップ時にカメラを移動する（デフォルト）
                return false;
            }
        });
        //現在地ボタン押下時
        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                requestLocationUpdate();
                return true;
            }
        });
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
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
        // 位置情報取得不可のときのデフォルトは東京駅
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(35.6809591, 139.7673068), 9));
        requestLocationUpdate();
    }

    private void requestLocationUpdate() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            //一度だけ位置情報を要求する
            locationManager.requestSingleUpdate(locationManager.GPS_PROVIDER, locationListener, null);
            locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, locationListener, null);
            // 現在値を表示
            mMap.setMyLocationEnabled(true);
        }
    }

    private void updateLocation(Location location) {
        clear();
        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 9));
    }

    public void clear() {
        mMap.clear();
        arrivalAirportRoute = null;
        departureAirportRoute = null;
        airportMarkerArray.clear();
        airplaneMarkerArray.clear();
        airplaneStateHash.clear();
        selectedAirplaneMarker = null;
        selectedAirportMarker = null;
        selectedAirplaneManager.clear();
    }

    @Override
    public void onCameraIdle() {
        if( selectedAirplaneMarker != null) {
            //飛行機を選択したときに表示される情報を10秒間は表示させたいため
            startGetAirplaneStateTimer(10000);
        } else {
            //ユーザがマップ操作時の負荷軽減のため待つ
            startGetAirplaneStateTimer(1000);
        }
        if( selectedAirportMarker != null) {
            //飛行場を選択したときに表示される情報を3秒間は表示させたいため
            startGetAirportInfoTimer(3000);
        } else {
            //ユーザがマップ操作時の負荷軽減のため待つ
            startGetAirportInfoTimer(1000);
        }
    }

    private void startGetAirplaneStateTimer(int delayMilliSecondTime) {
        VisibleRegion vRegion = mMap.getProjection().getVisibleRegion();
        final double topLatitude = vRegion.latLngBounds.northeast.latitude;
        final double bottomLatitude = vRegion.latLngBounds.southwest.latitude;
        final double leftLongitude = vRegion.latLngBounds.southwest.longitude;
        final double rightLongitude = vRegion.latLngBounds.northeast.longitude;

        if (getAirPlaneStateTimer != null) {
            getAirPlaneStateTimer.cancel();
        }
        getAirPlaneStateTimer = new Timer();
        getAirPlaneStateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        airplaneManager.getAirplaneInfo(topLatitude, bottomLatitude, leftLongitude, rightLongitude);
                    }
                });
            }
        }, delayMilliSecondTime, 10000);
    }

    private void startGetAirportInfoTimer(int delayMilliSecondTime) {
        VisibleRegion vRegion = mMap.getProjection().getVisibleRegion();
        final double topLatitude = vRegion.latLngBounds.northeast.latitude;
        final double bottomLatitude = vRegion.latLngBounds.southwest.latitude;
        final double leftLongitude = vRegion.latLngBounds.southwest.longitude;
        final double rightLongitude = vRegion.latLngBounds.northeast.longitude;

        if (getAirplaceInfoTimer != null) {
            getAirplaceInfoTimer.cancel();
        }
        getAirplaceInfoTimer = new Timer();
        getAirplaceInfoTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        airportManager.getAirportInfo(topLatitude, bottomLatitude, leftLongitude, rightLongitude);
                    }
                });
            }
        }, delayMilliSecondTime);
    }

    @Override
    public void showAirplane() {
        clearAirplane();
        clearRoute();
        for ( AirplaneState info : airplaneManager.airplaneStates) {
            MarkerOptions options;
            // 見栄えのため飛行機の数でアイコンサイズを変更する
            if(airplaneManager.airplaneStates.size() < 50) {
                options = new MarkerOptions().position(info.getLocation()).icon(BitmapDescriptorFactory.fromResource(R.drawable.airplane)).rotation(info.getTrue_track()).title(info.getTitle()).snippet(info.getSnippet());
            } else {
                options = new MarkerOptions().position(info.getLocation()).icon(BitmapDescriptorFactory.fromResource(R.drawable.airplane_small)).rotation(info.getTrue_track()).title(info.getTitle()).snippet(info.getSnippet());
            }
            Marker marker = mMap.addMarker(options);
            marker.setTag(markerAirplaneTagName);
            if(selectedAirplaneManager.existDepartureAndArrivalAirport() && selectedAirplaneManager.getAirplaneState().getIcao24().equals(info.getIcao24())) {
                arrivalAirportRoute = mMap.addPolyline(new PolylineOptions().geodesic(false).color(Color.YELLOW).width(10).pattern(Arrays.<PatternItem>asList(new Dash(30), new Gap(20))).add(info.getLocation(), selectedAirplaneManager.getArrivalAirport().getLocation()));
                departureAirportRoute = mMap.addPolyline(new PolylineOptions().geodesic(false).color(Color.YELLOW).width(10).add(info.getLocation(), selectedAirplaneManager.getDepartureAirport().getLocation()));
            }
            airplaneStateHash.put(marker.getId(), info);
            airplaneMarkerArray.add(marker);
        }
    }

    private void clearAirplane() {
        for (Marker airPlaneMarker: airplaneMarkerArray) {
            airPlaneMarker.remove();
        }
        airplaneMarkerArray.clear();
        airplaneStateHash.clear();
        selectedAirplaneMarker = null;
    }

    @Override
    public void showAirport() {
        clearAirport();
        for ( AirportInfo info : airportManager.airportInfos) {
            MarkerOptions options;
            if (airportManager.airportInfos.size() < 50) {
                if ("large_airport".equals(info.getType())) {
                    options = new MarkerOptions().position(info.getLocation()).icon(BitmapDescriptorFactory.fromResource(R.drawable.airport)).title(info.getName()).alpha(0.7f);
                } else {
                    options = new MarkerOptions().position(info.getLocation()).icon(BitmapDescriptorFactory.fromResource(R.drawable.airport_small)).title(info.getName()).alpha(0.7f);
                }
                Marker marker = mMap.addMarker(options);
                marker.setTag(markerAirportTagName);
                airportMarkerArray.add(marker);
            } else {
                if ("large_airport".equals(info.getType())) {
                    options = new MarkerOptions().position(info.getLocation()).icon(BitmapDescriptorFactory.fromResource(R.drawable.airport_small)).title(info.getName()).alpha(0.7f);
                    Marker marker = mMap.addMarker(options);
                    marker.setTag(markerAirportTagName);
                    airportMarkerArray.add(marker);
                }
            }

        }
    }

    private void clearAirport() {
        for (Marker airportMarker: airportMarkerArray) {
            airportMarker.remove();
        }
        airportMarkerArray.clear();
        selectedAirportMarker = null;
    }

    @Override
    public void showRoute() {
        if(selectedAirplaneManager.existDepartureAndArrivalAirport()) {
            if(selectedAirplaneMarker != null) {
                selectedAirplaneMarker.setSnippet(selectedAirplaneManager.getSnippet());
                selectedAirplaneMarker.hideInfoWindow();
                selectedAirplaneMarker.showInfoWindow();
            }
            arrivalAirportRoute = mMap.addPolyline(new PolylineOptions().geodesic(false).color(Color.YELLOW).width(10).pattern(Arrays.<PatternItem>asList(new Dash(30), new Gap(20))).add(selectedAirplaneManager.getAirplaneState().getLocation(), selectedAirplaneManager.getArrivalAirport().getLocation()));
            departureAirportRoute = mMap.addPolyline(new PolylineOptions().geodesic(false).color(Color.YELLOW).width(10).add(selectedAirplaneManager.getAirplaneState().getLocation(), selectedAirplaneManager.getDepartureAirport().getLocation()));
        }
    }

    private void clearRoute() {
        if(arrivalAirportRoute != null) {
            arrivalAirportRoute.remove();
            arrivalAirportRoute = null;
        }
        if(departureAirportRoute != null) {
            departureAirportRoute.remove();
            departureAirportRoute = null;
        }
    }
}