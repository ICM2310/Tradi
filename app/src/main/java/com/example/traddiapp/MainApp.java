package com.example.traddiapp;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;


import com.example.traddiapp.TareasAsync.GeocoderTask;
import com.example.traddiapp.TareasAsync.GetMarketAsyncTask;
import com.example.traddiapp.databinding.ActivityMapBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.osmdroid.bonuspack.routing.MapQuestRoadManager;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;

public class MainApp extends FragmentActivity implements OnMapReadyCallback {
    static final int PERMISSIONS_REQUEST_LOCATION = 0;

    private static GoogleMap mMap;

    private FusedLocationProviderClient fusedLocationClient;

    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private double latitudeUbi;
    private double longitudeUbi;

    ActivityMapBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        locationRequest = createLocationRequest();

        Spinner spinnerPedir = findViewById(R.id.spinnerPedir);

        List<String> opciones = new ArrayList<>();
        opciones.add("Pedir");
        opciones.add("Andrés Carne de Res");
        opciones.add("Harry Sasson");
        opciones.add("Criterion");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, opciones);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerPedir.setAdapter(adapter);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapView);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        binding.btoRestaurantes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), RestaurantesGActivity.class);
                startActivity(intent);
            }
        });

        binding.btoHistorial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), HistorialActivity.class);
                startActivity(intent);
            }
        });




        binding.editTextBuscar.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {

                    String address = binding.editTextBuscar.getText().toString();

                    GeocoderTask task = new GeocoderTask(getBaseContext(),mMap);
                    task.execute(address);
                    binding.editTextBuscar.setText("");

                    return true;
                }

                return false;
            }
        });

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        latitudeUbi = location.getLatitude();
                        longitudeUbi = location.getLongitude();
                        GetMarketAsyncTask a = new GetMarketAsyncTask(getBaseContext(),  mMap);
                        a.execute(latitudeUbi, longitudeUbi);
                        LatLng lastUbi = new LatLng(latitudeUbi, longitudeUbi);

                        try {
                            mMap.clear();
                            mMap.addMarker(a.get());
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(lastUbi));
                            mMap.moveCamera(CameraUpdateFactory.zoomTo(15));

                        } catch (ExecutionException | InterruptedException e) {
                            throw new RuntimeException(e);
                        }

                    }
                }
            });
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_LOCATION);
        }


        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location = locationResult.getLastLocation();

                double latitudeLast = location.getLatitude();
                double longitudeLast = location.getLongitude();
                GetMarketAsyncTask a = new GetMarketAsyncTask(getBaseContext(),  mMap);

                if ( Distancia.distance(latitudeLast, longitudeLast, latitudeUbi, longitudeUbi ) > 0.3){

                    a.execute(latitudeLast, longitudeLast);
                    LatLng lastUbi = new LatLng(latitudeLast, longitudeLast);

                    try {
                        mMap.clear();
                        mMap.addMarker(a.get());
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(lastUbi));
                        mMap.moveCamera(CameraUpdateFactory.zoomTo(15));

                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                    latitudeUbi = latitudeLast;
                    longitudeUbi = longitudeLast;

                }

                Log.i("LOCATION", "Location update in the callback: " + location);
            }
        };

        spinnerPedir.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                String opcionSeleccionada = (String) adapterView.getItemAtPosition(position);

                switch (opcionSeleccionada) {
                    case "Andrés Carne de Res": {

                        mMap.clear();
                        LatLng puntoInicio = new LatLng(latitudeUbi, longitudeUbi);
                        mMap.addMarker(new MarkerOptions().position(puntoInicio).title("Mi Ubicaicon"));
                        String address = "Carrera 6 # 12-20, Chía, Cundinamarca, Colombia";
                        GeocoderTask task = new GeocoderTask(getBaseContext(), mMap);
                        task.execute(address);
                        LatLng puntoDestino = null;
                        try {
                            puntoDestino = new LatLng(task.get().latitude, task.get().longitude);
                        } catch (ExecutionException | InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        GeoPoint geoPuntoInicio = new GeoPoint(puntoInicio.latitude, puntoInicio.longitude);
                        GeoPoint geoPuntoDestino = new GeoPoint(puntoDestino.latitude, puntoDestino.longitude);

                        ArrayList<GeoPoint> waypoints = new ArrayList<>();
                        waypoints.add(geoPuntoInicio);
                        waypoints.add(geoPuntoDestino);
                        new GetRouteTask().execute(waypoints);

                        break;
                    }
                    case "Harry Sasson": {

                        mMap.clear();
                        LatLng puntoInicio = new LatLng(latitudeUbi, longitudeUbi);
                        mMap.addMarker(new MarkerOptions().position(puntoInicio).title("Mi Ubicaicon"));
                        String address = "Carrera 9 # 75-70, Bogotá, Colombia";
                        GeocoderTask task = new GeocoderTask(getBaseContext(), mMap);
                        task.execute(address);
                        LatLng puntoDestino = null;
                        try {
                            puntoDestino = new LatLng(task.get().latitude, task.get().longitude);
                        } catch (ExecutionException | InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        GeoPoint geoPuntoInicio = new GeoPoint(puntoInicio.latitude, puntoInicio.longitude);
                        GeoPoint geoPuntoDestino = new GeoPoint(puntoDestino.latitude, puntoDestino.longitude);

                        ArrayList<GeoPoint> waypoints = new ArrayList<>();
                        waypoints.add(geoPuntoInicio);
                        waypoints.add(geoPuntoDestino);
                        new GetRouteTask().execute(waypoints);

                        break;
                    }
                    case "Criterion": {

                        mMap.clear();
                        LatLng puntoInicio = new LatLng(latitudeUbi, longitudeUbi);
                        mMap.addMarker(new MarkerOptions().position(puntoInicio).title("Mi Ubicaicon"));
                        String address = "Calle 69A # 5-75, Bogotá, Colombia";
                        GeocoderTask task = new GeocoderTask(getBaseContext(), mMap);
                        task.execute(address);
                        LatLng puntoDestino = null;
                        try {
                            puntoDestino = new LatLng(task.get().latitude, task.get().longitude);
                        } catch (ExecutionException | InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        GeoPoint geoPuntoInicio = new GeoPoint(puntoInicio.latitude, puntoInicio.longitude);
                        GeoPoint geoPuntoDestino = new GeoPoint(puntoDestino.latitude, puntoDestino.longitude);

                        ArrayList<GeoPoint> waypoints = new ArrayList<>();
                        waypoints.add(geoPuntoInicio);
                        waypoints.add(geoPuntoDestino);
                        new GetRouteTask().execute(waypoints);

                        break;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Acción cuando no se selecciona ninguna opción
            }
        });

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap){

        mMap = googleMap;
        mMap.clear();
        LatLng bogota = new LatLng(latitudeUbi, longitudeUbi);
        mMap.addMarker(new MarkerOptions().position(bogota).title("Marker Current Ubication"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(bogota));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(15));


    }

    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();
    }

    @Override
    protected void onPause(){
        super.onPause();

    }

    @NonNull
    private LocationRequest createLocationRequest() {

        return LocationRequest.create()
                .setInterval(10000)
                .setFastestInterval(5000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


    }

    private void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates( locationRequest, locationCallback, null);
        }
    }

    private class GetRouteTask extends AsyncTask<ArrayList<GeoPoint>, Void, Road> {

        @Override
        protected Road doInBackground(ArrayList<GeoPoint>... params) {
            ArrayList<GeoPoint> waypoints = params[0];

            // Crear una instancia de RoadManager
            RoadManager roadManager = new OSRMRoadManager(MainApp.this, "MapQuest");

            // Obtener la ruta utilizando RoadManager
            return roadManager.getRoad(waypoints);
        }

        @Override
        protected void onPostExecute(Road road) {
            if (road != null && road.mStatus == Road.STATUS_OK) {
                ArrayList<GeoPoint> rutaGeoPoints = road.mRouteHigh;

                // Pintar la ruta en el mapa
                PolylineOptions polylineOptions = new PolylineOptions()
                        .color(Color.RED)
                        .width(5);

                for (GeoPoint point : rutaGeoPoints) {
                    LatLng latLng = new LatLng(point.getLatitude(), point.getLongitude());
                    polylineOptions.add(latLng);
                }

                mMap.addPolyline(polylineOptions);

                // Ajustar la cámara para mostrar la ruta completa
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (GeoPoint point : rutaGeoPoints) {
                    LatLng latLng = new LatLng(point.getLatitude(), point.getLongitude());
                    builder.include(latLng);
                }
                LatLngBounds bounds = builder.build();
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 130));
            }
        }
    }

}