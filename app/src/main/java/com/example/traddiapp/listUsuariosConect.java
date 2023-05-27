package com.example.traddiapp;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class listUsuariosConect extends AppCompatActivity {

    private ListView mListView;
    private List<User> mUserList;
    private UserListAdapter mAdapter;

    private DatabaseReference mDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_usuarios_conect);

        mListView = findViewById(R.id.listaUsuarios);
        mUserList = new ArrayList<>();
        mAdapter = new UserListAdapter(this, mUserList);
        mListView.setAdapter(mAdapter);

        mDatabaseReference = FirebaseDatabase.getInstance().getReference("users");

        // Aquí obtienes los datos de los usuarios desde Firebase y los agregas a la lista
        // en el método onDataChange del Listener
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);
                    assert user != null;
                    user.setUid(dataSnapshot.getKey()); // Agrega el Uid del usuario a la variable user
                    mUserList.add(user);
                }
                mAdapter.notifyDataSetChanged();
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("UserListActivity", "Error al obtener datos de usuarios", error.toException());
            }
        });
    }

    private class UserListAdapter extends ArrayAdapter<User> {

        public UserListAdapter(Context context, List<User> objects) {
            super(context, 0, objects);
        }

        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.elemento_lista, parent, false);
            }

            User user = getItem(position);

            if (user == null) {
                Log.e(TAG, "getView: user is null for position " + position);
                return convertView;
            }

            TextView nameTextView = convertView.findViewById(R.id.user_name);
            Button locationButton = convertView.findViewById(R.id.user_button);
            Button chatButton = convertView.findViewById(R.id.chat_button);

            // Estableces el nombre del usuario en el TextView
            nameTextView.setText(user.getName());

            locationButton.setTag(position);
            locationButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Obtener la posición del botón
                    int position = (int) v.getTag();

                    // Obtener el usuario correspondiente a la posición
                    User user = getItem(position);

                    // Obtener el UID del usuario
                    String userUid = user.getUid();

                    // Abrir el mapa con la ubicación del usuario correspondiente
                    Intent intent = new Intent(getBaseContext(), ubiUsuario.class);
                    intent.putExtra("userUid", userUid);
                    startActivity(intent);
                }
            });

            chatButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Acción para el otro botón
                }
            });

            return convertView;
        }

    }
}
