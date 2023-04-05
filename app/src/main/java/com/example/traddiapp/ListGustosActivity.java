package com.example.traddiapp;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.traddiapp.R;
import com.example.traddiapp.databinding.ActivityListGustosBinding;

import java.io.IOException;
import java.util.ArrayList;

public class ListGustosActivity extends AppCompatActivity {

    ActivityListGustosBinding binding;
    ArrayList<String> array = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityListGustosBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        try {
            loadArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, array);
        ListView listView = findViewById(R.id.listView);
        listView.setAdapter(adapter);
    }


    public void loadArray() throws IOException {
        int cont=1;
        for (int i = 0; i < 5; i++) {
            String restaurante;
            restaurante = "Restaurante favorito " + cont;
            array.add(restaurante);
            cont++;
        }
    }

}