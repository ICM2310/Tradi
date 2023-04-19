package com.example.traddiapp;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import  com.example.traddiapp.databinding.ActivityIniciarSesionBinding;

public class IniciarSesionActivity extends AppCompatActivity {

    ActivityIniciarSesionBinding binding;
    private FirebaseAuthHelper firebaseAuthHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityIniciarSesionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuthHelper = new FirebaseAuthHelper(this);

        binding.buttonIniciarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = binding.editTextTextPersonName.getText().toString();
                String password = binding.editTextTextPassword.getText().toString();
                firebaseAuthHelper.signIn(email, password);
            }
        });

        binding.buttonRegistrarse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), RegistrarActivity.class));
            }
        });
    }
}