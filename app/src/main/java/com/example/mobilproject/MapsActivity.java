package com.example.mobilproject;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    SharedPreferences sharedpreferences, sharedpreferencesCalendar;
    public static final String MyPREFERENCES = "Map", MyPREFERENCESCalendar = "Calendar";
    private SharedPreferences.Editor editor, editorCalendar;
    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;
    Double latitude, newLatitude;
    Double longitude,newlongitude;
    String activityId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        sharedpreferencesCalendar = getSharedPreferences(MyPREFERENCESCalendar, Context.MODE_PRIVATE);
        editorCalendar = sharedpreferencesCalendar.edit();
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        editor = sharedpreferences.edit();


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        if (getIntent().getStringExtra("map") != null) {
            if (getIntent().getStringExtra("map").equals("Yes")) {
                latitude = Double.parseDouble(getIntent().getStringExtra("latitude"));
                longitude = Double.parseDouble(getIntent().getStringExtra("longitude"));
                activityId = getIntent().getStringExtra("reminder_id");
                Toast.makeText(getApplicationContext(), "Konum güncellemek için bir konuma basılı tutunuz.", Toast.LENGTH_SHORT).show();
            }
        }
        if (getIntent().getStringExtra("map") == null) {
            Toast.makeText(getApplicationContext(), "Konum eklemek için bir konuma basılı tutunuz.", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if(!sharedpreferencesCalendar.getString("theme","Default").equals("Default")){
            try {
                googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.style_json));
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
        mMap = googleMap;
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };

        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                if (getIntent().getStringExtra("map") != null) {
                    LatLng location = new LatLng(latitude, longitude);
                    mMap.addMarker(new MarkerOptions().title("Konumunuz").position(location));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
                } else {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                    Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    LatLng userLastLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                    mMap.addMarker(new MarkerOptions().title("Konum olmadığı için son konumunuz").position(userLastLocation));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLastLocation, 15));
                }
            }
        } else {
            if (getIntent().getStringExtra("map") != null) {
                LatLng location = new LatLng(latitude, longitude);
                mMap.addMarker(new MarkerOptions().title("Konumunuz").position(location));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                LatLng userLastLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                mMap.addMarker(new MarkerOptions().title("Konum olmadığı için son konumunuz").position(userLastLocation));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLastLocation, 15));
            }
        }


        mMap.setOnMapLongClickListener(this);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (grantResults.length > 0) {
            if (requestCode == 1) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        mMap.clear();

        if (getIntent().getStringExtra("map") == null) {
            editor.putString("map", "Yes");
            editor.putString("longitude", (latLng.longitude + ""));
            editor.putString("latitude", (latLng.latitude + ""));
            editor.apply();
            System.out.println("La:" + latLng.latitude + ",Lo:" + latLng.longitude);
            Toast.makeText(getApplicationContext(), "Konum alındı", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MapsActivity.this, SetReminderActivity.class);
            startActivity(intent);
        }else{
            newlongitude = latLng.longitude;
            newLatitude = latLng.latitude;
            AlertDialog.Builder builder1 = createAlertDialog();
            builder1.setTitle("Konum Değişikliği");
            builder1.setMessage("Konumu değiştirmek istediğinizden emin misiniz?");
            builder1.setPositiveButton(
                    "Evet, değiştir",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            editor.putString("map", "Yes");
                            editor.putString("longitude", ( newlongitude+ ""));
                            editor.putString("latitude", ( newLatitude+ ""));
                            editor.apply();
                            System.out.println("hi:"+newlongitude+" , "+newLatitude);
                            Toast.makeText(getApplicationContext(), "Konum bilgisi güncellendi", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(MapsActivity.this, UpdateReminderActivity.class);
                            intent.putExtra("reminder_id", activityId);
                            startActivity(intent);
                        }
                    });

            builder1.setNegativeButton(
                    "Hayır, geri dön",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert11 = builder1.create();
            alert11.show();
        }
    }

    public AlertDialog.Builder createAlertDialog(){
        if(sharedpreferencesCalendar.getString("theme","Default").equals("Default")){
            return new AlertDialog.Builder(MapsActivity.this);
        }else{
            return new AlertDialog.Builder(MapsActivity.this, 4);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
