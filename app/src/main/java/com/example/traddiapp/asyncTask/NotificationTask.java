package com.example.traddiapp.asyncTask;

import android.os.AsyncTask;
import android.util.Log;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NotificationTask extends AsyncTask<Void, Void, String> {

    private String token;

    private String userName;

    public NotificationTask(String token, String userName){
        this.token=token;
        this.userName=userName;
    }

    @Override
    protected String doInBackground(Void... voids) {

        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/json");
        String jsonString = "{ \n" +
                "    \"notification\": {\n" +
                "        \"title\": \"TraddiApp\"\n" +
                "        \"body\": \"Hola " + userName  + " hay un nuevo usuario conectado\"\n "+
                "      },\n" +
                "      \"to\" : \"" +token+ "\"  } ";

        System.out.println("Json enviado "+ jsonString);
        RequestBody body = RequestBody.create(mediaType, jsonString);
        Request request = new Request.Builder()
                .url("https://fcm.googleapis.com/fcm/send")
                .method("POST", body)
                .addHeader("Authorization", "key=AAAAcSFrZ_g:APA91bFrLZnrdPkOZP7cKyqkGuU3b4vgaT-ryujf2B2W3ac4fj51Kv1bbTfQZaEVLvmzngAsAnCLAb1UkKottUxHZQEZ6jLxNbJTP-BlnFARnF0OpbcrOeEQhJeY_taDxVO05qcBAXf7")
                .addHeader("Content-Type", "application/json")
                .build();

        try {
            Response response = client.newCall(request).execute();
            System.out.println("Successfully sent message to : " + token+ " Response " +response.message());

            if (response.isSuccessful()) {
                return response.body().string();
            } else {
                // La solicitud no fue exitosa
                // Manejar el error
                Log.d("NetworkTask", "Respuesta: " + response.message());
                return null;
            }
        } catch (Exception e) {

            e.printStackTrace();
            // Manejar la excepción
            return null;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        if (result != null) {
            // Manejar la respuesta exitosa
            Log.d("NetworkTask", "Respuesta: " + result);
        } else {
            // Manejar el error
            Log.e("NetworkTask", "Error en la solicitud");
        }
    }
}
