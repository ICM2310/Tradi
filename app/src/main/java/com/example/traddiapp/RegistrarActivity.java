package com.example.traddiapp;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import com.example.traddiapp.databinding.ActivityRegistrarBinding;

public class RegistrarActivity extends AppCompatActivity {

    private FirebaseAuthHelper firebaseAuthHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Utiliza View Binding
        ActivityRegistrarBinding binding = ActivityRegistrarBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuthHelper = new FirebaseAuthHelper(this);

        binding.buttonIniciarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = binding.editTextTextPersonName2.getText().toString();
                String password = binding.editTextTextPersonName3.getText().toString();
                firebaseAuthHelper.signUp(email, password);
            }
        });

        binding.buttonVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegistrarActivity.this, IniciarSesionActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
