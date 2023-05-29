package com.example.traddiapp.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.example.traddiapp.R;

import java.util.ArrayList;

public class RestaurantesGActivity extends AppCompatActivity {

    // Declaración de variables
    private EditText editText;
    private ListView listView;
    private Button buttonGuardar;
    private ArrayList<String> listaElementos;
    private ArrayAdapter<String> adaptador;

    private static final int REQUEST_IMAGE_CAPTURE = 2;
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurantes_gactivity);

        // Inicialización de variables
        editText = findViewById(R.id.editText);
        listView = findViewById(R.id.listView);
        buttonGuardar = findViewById(R.id.buttonGuardar);
        listaElementos = new ArrayList<>();

        // Inicialización del adaptador para la lista
        adaptador = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaElementos);
        listView.setAdapter(adaptador);

        // Listener para el botón guardar
        buttonGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guardarLista();
            }
        });

        // Listener para la lista
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });

    }

    private void guardarLista() {
        String elemento = editText.getText().toString();
        if (!elemento.isEmpty()) {
            listaElementos.add(elemento);
            adaptador.notifyDataSetChanged();
            editText.setText("");
        }
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);

    }
}
