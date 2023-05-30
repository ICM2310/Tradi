package com.example.traddiapp.activities;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.traddiapp.R;
import com.example.traddiapp.asyncTask.GetMarketAsyncTask;
import com.example.traddiapp.model.User;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.ExecutionException;

public class UbicacionUsuarioActivity extends AppCompatActivity implements OnMapReadyCallback  {

    static final int PERMISSIONS_REQUEST_LOCATION = 0;

    private static GoogleMap mMap;
    MapView mapView;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private double latitudeUbi;
    private double longitudeUbi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ubi_usuario);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapView);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);


        String uid = getIntent().getStringExtra("userUid");

        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User myUser = dataSnapshot.getValue(User.class);
                assert myUser != null;
                 latitudeUbi = myUser.getLatitude();
                 longitudeUbi = myUser.getLongitude();

                GetMarketAsyncTask a = new GetMarketAsyncTask(getBaseContext(),  mMap);
                a.execute(latitudeUbi, longitudeUbi);
                LatLng lastUbi = new LatLng(latitudeUbi, longitudeUbi);

                try {
                    mMap.addMarker(a.get());
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(lastUbi));
                    mMap.moveCamera(CameraUpdateFactory.zoomTo(12));
                } catch (ExecutionException | InterruptedException e) {
                    throw new RuntimeException(e);
                }

                Log.i(TAG, "Encontró ubicación: " + latitudeUbi + ", " + longitudeUbi);

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "error en la consulta", databaseError.toException());
            }
        });


    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap)
    {
        mMap = googleMap;
    }



    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause(){
        super.onPause();

    }
}