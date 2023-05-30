package com.example.traddiapp.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.traddiapp.R;
import com.example.traddiapp.databinding.ActivityChatBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ChatActivity extends AppCompatActivity {

    FirebaseDatabase database = FirebaseDatabase.getInstance();

    ActivityChatBinding binding;
    ArrayAdapter<String> adapter;
    DatabaseReference userMessagesRef;

    DatabaseReference userMessagesRefChatSelect;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String uidUsuarioActual = currentUser.getUid();
            String uid = getIntent().getStringExtra("userUid");
            userMessagesRef = database.getReference("users").child(uidUsuarioActual).child("mensajes");
            userMessagesRefChatSelect = database.getReference("users").child(uid).child("mensajes");


        }
        binding.buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = binding.editTextMessage.getText().toString();

                // Obtener el usuario actual
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

                if (currentUser != null) {

                    // Crear referencia al nodo del usuario actual en la base de datos

                    // Enviar el mensaje utilizando la referencia del usuario actual
                    userMessagesRef.push().setValue(message);

                    // Limpiar el campo de texto
                    binding.editTextMessage.setText("");
                }
            }
        });

        // Obtener referencia al ListView
        ListView listViewChat = findViewById(R.id.listViewChat);

        // Crear el ArrayAdapter para el ListView
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        listViewChat.setAdapter(adapter);
        userMessagesRef.addChildEventListener(new ChildEventListener() {
             @Override
             public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                 String message = dataSnapshot.getValue(String.class);
                 Log.d("Chat", "Mensaje recibido: " + message);

                 // Agregar el mensaje al ArrayAdapter y notificar cambios
                 adapter.add(message);
                 adapter.notifyDataSetChanged();

                 // Hacer scroll hacia el último mensaje
                 listViewChat.smoothScrollToPosition(adapter.getCount() - 1);
             }

             @Override
             public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                 // Implementar la lógica para manejar cambios en los hijos del nodo 'chat'
             }

             @Override
             public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                 // Implementar la lógica para manejar la eliminación de hijos del nodo 'chat'
             }

             @Override
             public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                 // Implementar la lógica para manejar el movimiento de hijos del nodo 'chat'
             }

             @Override
             public void onCancelled(@NonNull DatabaseError databaseError) {
                 // Implementar la lógica para manejar errores en la base de datos
             }
         });


        userMessagesRefChatSelect.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String message = dataSnapshot.getValue(String.class);
                Log.d("Chat", "Mensaje recibido: " + message);

                // Agregar el mensaje al ArrayAdapter y notificar cambios
                adapter.add(message);
                adapter.notifyDataSetChanged();

                // Hacer scroll hacia el último mensaje
                listViewChat.smoothScrollToPosition(adapter.getCount() - 1);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                // Implementar la lógica para manejar cambios en los hijos del nodo 'chat'
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                // Implementar la lógica para manejar la eliminación de hijos del nodo 'chat'
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                // Implementar la lógica para manejar el movimiento de hijos del nodo 'chat'
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Implementar la lógica para manejar errores en la base de datos
            }
        });
     }


    private void deleteMessagesForUser(String userId) {
        DatabaseReference userMessagesRef = FirebaseDatabase.getInstance().getReference("users")
                .child(userId)
                .child("mensajes");

        userMessagesRef.removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // La lista de mensajes del usuario se eliminó exitosamente
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Ocurrió un error al eliminar la lista de mensajes del usuario
                    }
                });
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Limpiar el ArrayAdapter
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String uidUsuarioActual = currentUser.getUid();
            String uid = getIntent().getStringExtra("userUid");

            deleteMessagesForUser(uidUsuarioActual);
            deleteMessagesForUser(uid);
        }
    }
}
