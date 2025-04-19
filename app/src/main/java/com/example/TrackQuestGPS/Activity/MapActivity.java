package com.example.TrackQuestGPS.Activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.TrackQuestGPS.R;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private LatLng origin, destination;
    private TextView routeInfo;
    private RadioGroup modeSelector;
    private String currentMode = "driving";

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private String API_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        API_KEY = getString(R.string.google_maps_key);

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), API_KEY);
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.id_map);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        routeInfo = findViewById(R.id.route_info);
        modeSelector = findViewById(R.id.mode_selector);

        findViewById(R.id.driving).setOnClickListener(v -> switchMode("driving"));
        findViewById(R.id.walking).setOnClickListener(v -> switchMode("walking"));
        findViewById(R.id.bicycling).setOnClickListener(v -> switchMode("bicycling"));
        findViewById(R.id.transit).setOnClickListener(v -> switchMode("transit"));

        initSearchBar();
    }

    private void switchMode(String mode) {
        currentMode = mode;
        if (origin != null && destination != null) {
            getRoute(origin, destination);
        }
    }

    private void initSearchBar() {
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        if (autocompleteFragment != null) {
            autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
            autocompleteFragment.setHint("Search places…");

            View autocompleteView = autocompleteFragment.getView();
            if (autocompleteView != null) {
                autocompleteView.setBackgroundResource(R.drawable.autocomplete_background);
                autocompleteView.setElevation(8f);
                autocompleteView.setTranslationZ(8f);
            }

            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(@NonNull Place place) {
                    LatLng latLng = place.getLatLng();
                    if (latLng != null && mMap != null) {
                        mMap.clear();
                        mMap.addMarker(new MarkerOptions().position(latLng).title(place.getName()));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                        origin = latLng;
                        destination = null;
                        routeInfo.setText("Tap a destination to calculate route");
                    }
                }

                @Override
                public void onError(@NonNull Status status) {
                    Toast.makeText(MapActivity.this, "Search error: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }

        mMap.setOnMapClickListener(latLng -> {
            if (origin == null) {
                origin = latLng;
                mMap.addMarker(new MarkerOptions().position(latLng).title("Origin"));
            } else if (destination == null) {
                destination = latLng;
                mMap.addMarker(new MarkerOptions().position(latLng).title("Destination"));
                getRoute(origin, destination);
            } else {
                mMap.clear();
                origin = latLng;
                destination = null;
                mMap.addMarker(new MarkerOptions().position(latLng).title("Origin"));
            }
        });
    }

    private void getRoute(LatLng origin, LatLng dest) {
        String url = getDirectionsUrl(origin, dest, currentMode);

        new Thread(() -> {
            try {
                URL routeUrl = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) routeUrl.openConnection();
                conn.connect();

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();

                JSONObject json = new JSONObject(sb.toString());
                JSONArray routes = json.getJSONArray("routes");

                if (routes.length() > 0) {
                    JSONObject leg = routes.getJSONObject(0).getJSONArray("legs").getJSONObject(0);
                    String distanceText = leg.getJSONObject("distance").getString("text");
                    String durationText = leg.getJSONObject("duration").getString("text");

                    runOnUiThread(() -> routeInfo.setText("Distance: " + distanceText + "\nDuration: " + durationText));

                    JSONArray steps = leg.getJSONArray("steps");
                    PolylineOptions polylineOptions = new PolylineOptions().color(Color.BLUE).width(10);
                    for (int i = 0; i < steps.length(); i++) {
                        JSONObject step = steps.getJSONObject(i).getJSONObject("start_location");
                        LatLng point = new LatLng(step.getDouble("lat"), step.getDouble("lng"));
                        polylineOptions.add(point);
                    }
                    runOnUiThread(() -> mMap.addPolyline(polylineOptions));

                } else {
                    runOnUiThread(() -> Toast.makeText(this, "No route found", Toast.LENGTH_SHORT).show());
                }

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Failed to fetch route", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest, String mode) {
        return "https://maps.googleapis.com/maps/api/directions/json?"
                + "origin=" + origin.latitude + "," + origin.longitude
                + "&destination=" + dest.latitude + "," + dest.longitude
                + "&mode=" + mode
                + "&key=" + API_KEY;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (mMap != null) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                }
            }
        }
    }
}