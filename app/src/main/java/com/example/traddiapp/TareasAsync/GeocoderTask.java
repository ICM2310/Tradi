package com.example.traddiapp.TareasAsync;



import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.widget.Toast;

import androidx.core.content.ContextCompat;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


import java.io.IOException;
import java.util.List;


public class GeocoderTask extends AsyncTask<String, Void, LatLng> {

    private Context mContext;
    private GoogleMap googleMap;

    public GeocoderTask(Context context, GoogleMap googleMapIn ) {
        mContext = context;
        googleMap = googleMapIn;
    }

    @Override
    protected LatLng doInBackground(String... params) {
        Geocoder geocoder = new Geocoder(mContext);
        String address = params[0];
        try {
            List<Address> addresses = geocoder.getFromLocationName(address, 1);
            if (addresses.size() > 0) {
                Address result = addresses.get(0);
                double latitude = result.getLatitude();
                double longitude = result.getLongitude();
                return new LatLng(latitude, longitude);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(LatLng result) {
        if (result != null) {
            if (googleMap != null) {
                Geocoder geocoder = new Geocoder(mContext);
                String address = getAddressFromLatLng(result, geocoder);
                googleMap.addMarker(new MarkerOptions().position(result).title(address));
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(result, 15));
            }
        } else {
            Toast.makeText(mContext, "No se encontró el lugar ingresado", Toast.LENGTH_SHORT).show();
        }
    }

    private String getAddressFromLatLng(LatLng latLng, Geocoder geocoder) {
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    sb.append(address.getAddressLine(i));
                    if (i < address.getMaxAddressLineIndex()) {
                        sb.append(", ");
                    }
                }
                return sb.toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Dirección desconocida";
    }
}
