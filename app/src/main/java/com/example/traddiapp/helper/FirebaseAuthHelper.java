package com.example.traddiapp.helper;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Patterns;

import androidx.appcompat.app.AlertDialog;

import com.example.traddiapp.MainApp;
import com.google.firebase.auth.FirebaseAuth;

public class FirebaseAuthHelper {
    private FirebaseAuth mAuth;
    private Activity activity;

    public FirebaseAuthHelper(Activity activity) {
        this.activity = activity;
        mAuth = FirebaseAuth.getInstance();
    }

    public void signIn(String email, String password) {
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            showErrorDialog("Por favor, ingrese correo y contraseña.");
            return;
        }

        if (!isValidEmail(email)) {
            showErrorDialog("Por favor, ingrese un correo válido.");
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity, task -> {
                    if (task.isSuccessful()) {
                        Intent intent = new Intent(activity.getApplicationContext(), MainApp.class);
                        activity.startActivity(intent);
                    } else {
                        showErrorDialog("Error en la autenticación: " + task.getException().getMessage());
                    }
                });
    }

    public void signUp(String email, String password) {
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            showErrorDialog("Por favor, ingrese correo y contraseña.");
            return;
        }

        if (!isValidEmail(email)) {
            showErrorDialog("Por favor, ingrese un correo válido.");
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity, task -> {
                    if (task.isSuccessful()) {
                        Intent intent = new Intent(activity.getApplicationContext(), MainApp.class);
                        activity.startActivity(intent);
                    } else {
                        showErrorDialog("Error en el registro: " + task.getException().getMessage());
                    }
                });
    }

    private boolean isValidEmail(CharSequence email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void showErrorDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Error")
                .setMessage(message)
                .setPositiveButton("Aceptar", (DialogInterface.OnClickListener) (dialog, id) -> {
                    dialog.dismiss();
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
