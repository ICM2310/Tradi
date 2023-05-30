package com.example.traddiapp.model;


import java.util.HashMap;
import java.util.List;

public class User {
        private String name;
        private String profileImageUrl;
        private String email;
        private double  latitude;
        private double longitude;
        private boolean disponible;
        private String token;
       // private HashMap<String,List<String>> mensajes;

   /* public HashMap<String, List<String>> getMensajes() {
        return mensajes;
    }

    public void setMensajes(HashMap<String, List<String>> mensajes) {
        this.mensajes = mensajes;
    }*/

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    private String uid;



        public User() {
            // Constructor vacío requerido para Firebase
        }

        public User(String name, String profileImageUrl, String uid) {
            this.name = name;
            this.profileImageUrl = profileImageUrl;
            this.uid = uid;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getProfileImageUrl() {
            return profileImageUrl;
        }

        public void setProfileImageUrl(String profileImageUrl) {
            this.profileImageUrl = profileImageUrl;
        }

        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }


}


