package com.example.TrackQuestGPS.Activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.TrackQuestGPS.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private GoogleMap gMap;
    private FusedLocationProviderClient fusedLocationClient;

    private LatLng startPoint = null;
    private LatLng endPoint = null;

    private TextView routeInfoText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = SupportMapFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.id_map, mapFragment)
                .commit();

        mapFragment.getMapAsync(this);

        routeInfoText = findViewById(R.id.route_info_text);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.gMap = googleMap;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            gMap.setMyLocationEnabled(true);
            dropMarkerAtCurrentLocation();
        } else {
            Log.w("MapActivity", "Location permission not granted");
        }

        gMap.setOnMapClickListener(latLng -> {
            if (startPoint == null) {
                startPoint = latLng;
                gMap.addMarker(new MarkerOptions().position(latLng).title("Start Point"));
            } else if (endPoint == null) {
                endPoint = latLng;
                gMap.addMarker(new MarkerOptions().position(latLng).title("End Point"));

                String url = getDirectionsUrl(startPoint, endPoint);
                new FetchRouteTask().execute(url);
            } else {
                gMap.clear();
                routeInfoText.setText("Route info will show here");
                startPoint = latLng;
                endPoint = null;
                gMap.addMarker(new MarkerOptions().position(latLng).title("Start Point"));
                dropMarkerAtCurrentLocation();
            }
        });
    }

    private void dropMarkerAtCurrentLocation() {
        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null && gMap != null) {
                    LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    gMap.addMarker(new MarkerOptions().position(currentLatLng).title("You are here"));
                    gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
                }
            });
        } catch (SecurityException e) {
            Log.e("MapActivity", "Error getting location", e);
        }
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {
        String strOrigin = "origin=" + origin.latitude + "," + origin.longitude;
        String strDest = "destination=" + dest.latitude + "," + dest.longitude;
        String key = "AIzaSyAf8GQsATJD2TOKo8tXt28UAuiBluc8CE0"; // 🔑 Replace with your actual API key
        String parameters = strOrigin + "&" + strDest + "&key=" + key;
        return "https://maps.googleapis.com/maps/api/directions/json?" + parameters;
    }

    private class FetchRouteTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                return result.toString();
            } catch (Exception e) {
                Log.e("RouteFetchError", "Error fetching route", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject json = new JSONObject(result);
                JSONArray routes = json.getJSONArray("routes");

                if (routes.length() == 0) {
                    Toast.makeText(MapActivity.this, "No route found", Toast.LENGTH_SHORT).show();
                    return;
                }

                JSONObject route = routes.getJSONObject(0);
                JSONObject overviewPolyline = route.getJSONObject("overview_polyline");
                String points = overviewPolyline.getString("points");

                // Extract duration and distance
                JSONObject leg = route.getJSONArray("legs").getJSONObject(0);
                String distance = leg.getJSONObject("distance").getString("text");
                String duration = leg.getJSONObject("duration").getString("text");

                // Update the text panel
                routeInfoText.setText("🛣️ Distance: " + distance + "   ⏱️ Duration: " + duration);

                drawRoute(decodePoly(points));
            } catch (Exception e) {
                Toast.makeText(MapActivity.this, "Failed to parse route", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    private void drawRoute(List<LatLng> points) {
        gMap.addPolyline(new PolylineOptions()
                .addAll(points)
                .width(10)
                .color(Color.BLUE));
    }

    private List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;

            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;

            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            poly.add(new LatLng(lat / 1E5, lng / 1E5));
        }

        return poly;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (gMap != null) {
                    try {
                        gMap.setMyLocationEnabled(true);
                        dropMarkerAtCurrentLocation();
                    } catch (SecurityException e) {
                        Log.e("MapActivity", "SecurityException when enabling location", e);
                    }
                }
            } else {
                Log.w("MapActivity", "Location permission was denied by the user.");
            }
        }
    }
}
