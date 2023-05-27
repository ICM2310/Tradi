package com.example.traddiapp;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import  com.example.traddiapp.databinding.ActivityIniciarSesionBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class IniciarSesionActivity extends AppCompatActivity {

    ActivityIniciarSesionBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityIniciarSesionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mAuth = FirebaseAuth.getInstance();
        binding.buttonIniciarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUser();
            }
        });

        binding.buttonRegistrarse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), RegistrarActivity.class));
            }
        });
    }

    private void loginUser() {
        String email = binding.editTextCorreo.getText().toString().trim();
        String password = binding.editTextTextPassword.getText().toString().trim();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            readDataFromFirebaseByEmail(email, new OnDataReadCallback() {
                                @Override
                                public void onDataRead(User user) {
                                    Intent intent = new Intent(getBaseContext(), MainApp.class);
                                    intent.putExtra("userUid", user.getUid());
                                    startActivity(intent);
                                    finish();
                                }

                                @Override
                                public void onError(String errorMessage) {

                                }
                            });

                        } else {
                            Toast.makeText(getBaseContext(), "Error en el inicio de sesión.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });



    }

    public void readDataFromFirebaseByEmail(String email, OnDataReadCallback callback) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        Query query = usersRef.orderByChild("email").equalTo(email);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        User user = dataSnapshot.getValue(User.class);
                        if (user != null && user.getEmail().equals(email)) {
                            String userId = dataSnapshot.getKey();
                            user.setUid(userId); // Setear el uid del usuario obtenido
                            callback.onDataRead(user);
                            return;
                        }
                    }
                }
                callback.onError("No se encontró ningún usuario con ese email");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError(databaseError.getMessage());
            }
        });
    }


    public interface OnDataReadCallback {
        void onDataRead(User user);
        void onError(String errorMessage);
    }
}