package com.example.traddiapp;

import static android.content.ContentValues.TAG;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.traddiapp.asyncTask.GeocoderTask;
import com.example.traddiapp.asyncTask.GetMarketAsyncTask;
import com.example.traddiapp.activities.HistorialActivity;
import com.example.traddiapp.activities.IniciarSesionActivity;
import com.example.traddiapp.activities.RestaurantesGActivity;
import com.example.traddiapp.activities.ListUsuariosConectActivity;
import com.example.traddiapp.asyncTask.NotificationTask;
import com.example.traddiapp.databinding.ActivityMapBinding;
import com.example.traddiapp.model.Distancia;
import com.example.traddiapp.model.User;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;


public class MainApp extends AppCompatActivity implements OnMapReadyCallback {

    static final int PERMISSIONS_REQUEST_LOCATION = 0;
    private FirebaseAuth mAuth;
    private static GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private double latitudeUbi;
    private double longitudeUbi;
    ActivityMapBinding binding;
    private boolean settingsOK = false;
    private boolean isShowingAlert = false;

    private static FirebaseMessaging firebaseMessaging = FirebaseMessaging.getInstance();
    private static String token;

    ActivityResultLauncher<IntentSenderRequest> getLocationSettings = registerForActivityResult(
            new ActivityResultContracts.StartIntentSenderForResult(),
            result -> {
                Log.i(TAG, "Result from settings: " + result.getResultCode());
                if (result.getResultCode() == RESULT_OK) {
                    settingsOK = true;
                } else {
                    onBackPressed();
                    Toast.makeText(getApplicationContext(), "GPS desactivado", Toast.LENGTH_SHORT).show();
                }
            });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        System.out.println("------> Name user main app "+getIntent().getStringExtra("userName"));


        binding = ActivityMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mAuth = FirebaseAuth.getInstance();
        IntentFilter filter = new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION);
        registerReceiver(gpsLocationReceiver, filter);
        actualizarDisponibilidadUsuario(false);
        locationRequest = createLocationRequest();
        Spinner spinnerPedir = findViewById(R.id.spinnerPedir);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = view.findViewById(android.R.id.text1);
                textView.setTextColor(Color.WHITE); // Establecer el color del texto en blanco
                return view;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPedir.setAdapter(adapter);

        List<String> opciones = new ArrayList<>();
        opciones.add("Pedir");
        opciones.add("Andrés Carne de Res");
        opciones.add("Harry Sasson");
        opciones.add("Criterion");

        adapter.addAll(opciones);
        adapter.notifyDataSetChanged();

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

                    GeocoderTask task = new GeocoderTask(getBaseContext(), mMap);
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
                        GetMarketAsyncTask a = new GetMarketAsyncTask(getBaseContext(), mMap);
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
                GetMarketAsyncTask a = new GetMarketAsyncTask(getBaseContext(), mMap);

                if (Distancia.distance(latitudeLast, longitudeLast, latitudeUbi, longitudeUbi) > 0.3) {

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

        FirebaseApp fp = FirebaseApp.initializeApp(getBaseContext());
        //System.out.println("--->  "+fp.getName());
        //firebaseMessaging = FirebaseMessaging.getInstance();
        if(fp!= null) {
            Task<String> task = firebaseMessaging.getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                @Override
                public void onComplete(@NonNull Task<String> task) {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    // Get new FCM registration token
                    token = task.getResult();
                    System.out.println("Token ------->" + token);
                    // Log and toast
                    String msg = token;
                    Log.d(TAG, msg);
                    //Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT).show();
                    actualizarTokenUsuario(token);
                }
            });




        }

       /* firebaseMessaging.subscribeToTopic("ReadyToChat")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "Subscribed";
                        if (!task.isSuccessful()) {
                            msg = "Subscribe failed";
                        }
                        Log.d(TAG, msg);
                        System.out.println("Subscribed ------>>>>: "+ "From: " );

                        Toast.makeText(MainApp.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });*/

    }

    private final BroadcastReceiver gpsLocationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                if (!isShowingAlert) {
                    checkLocationSettings();
                    isShowingAlert = true;
                }
            } else {
                isShowingAlert = false;
            }
        }
    };

    private void checkLocationSettings() {
        LocationSettingsRequest.Builder builder = new
                LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                Log.i(TAG, "GPS is ON");
                settingsOK = true;
            }
        });
        task.addOnFailureListener(e -> {
            if (((ApiException) e).getStatusCode() == CommonStatusCodes.RESOLUTION_REQUIRED) {
                ResolvableApiException resolvable = (ResolvableApiException) e;
                IntentSenderRequest isr = new IntentSenderRequest.Builder(resolvable.getResolution()).build();
                getLocationSettings.launch(isr);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem item = menu.findItem(R.id.menu_checkbox);
        CheckBox checkBox = (CheckBox) item.getActionView();
        checkBox.setText("Disponible");
        checkBox.setTextColor(Color.WHITE);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    actualizarDisponibilidadUsuario(true);
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
                    ref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                User user = dataSnapshot.getValue(User.class);
                                user.setUid(dataSnapshot.getKey());
                                assert user != null;
                                if(user.isDisponible()) {
                                    String userUid = getIntent().getStringExtra("userUid");
                                    if (!user.getUid().equalsIgnoreCase(userUid)&&
                                            user.getToken()!=null) {

                                      //  showNotification("HOLAAA","JEJEJEJ");

                                        NotificationTask networkTask = new NotificationTask(user.getToken(),user.getName());
                                        networkTask.execute();

                                    }
                                }
                            }

                        }


                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("UserListActivity", "Error al obtener datos de usuarios", error.toException());
                        }
                    });


                } else {
                    actualizarDisponibilidadUsuario(false);
                }
            }
        });
        return true;
    }

    private void showNotification(String title, String message) {
        // Crea un Intent para abrir la actividad cuando se hace clic en la notificación
        Intent intent = new Intent(getBaseContext(), IniciarSesionActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);


        // Construye la notificación
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getBaseContext(), "channel_id")
                .setSmallIcon(R.drawable.noticon)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                 .setContentIntent(pendingIntent);

        // Muestra la notificación
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notificationBuilder.build());
    }


    public void actualizarDisponibilidadUsuario(boolean isDisponible) {
        String userUid = getIntent().getStringExtra("userUid");
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("users").child(userUid);

        Map<String, Object> updates = new HashMap<>();
        updates.put("disponible", isDisponible);

        myRef.updateChildren(updates, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    Log.w(TAG, "Error al actualizar la disponibilidad del usuario", databaseError.toException());
                } else {
                    Log.i(TAG, "Disponibilidad del usuario actualizada correctamente");
                }
            }
        });
    }

    public void actualizarTokenUsuario(String token) {
        String userUid = getIntent().getStringExtra("userUid");
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("users").child(userUid);

        Map<String, Object> updates = new HashMap<>();
        updates.put("token", token);

        myRef.updateChildren(updates, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    Log.w(TAG, "Error al actualizar el token del usuario", databaseError.toException());
                } else {
                    Log.i(TAG, "Token del usuario actualizada correctamente");
                }
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemClicked = item.getItemId();
        if (itemClicked == R.id.menuLogOut) {
            mAuth.signOut();
            Intent intent = new Intent(getBaseContext(), IniciarSesionActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else if (itemClicked == R.id.btolistUsers) {
            Intent intent = new Intent(this, ListUsuariosConectActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
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
        checkLocationSettings();
        startLocationUpdates();
    }

    @Override
    protected void onPause(){
        super.onPause();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(gpsLocationReceiver);
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


    @SuppressLint("StaticFieldLeak")
    private class GetRouteTask extends AsyncTask<ArrayList<GeoPoint>, Void, Road> {

        @SafeVarargs
        @Override
        protected final Road doInBackground(ArrayList<GeoPoint>... params) {
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
                        .width(7);

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