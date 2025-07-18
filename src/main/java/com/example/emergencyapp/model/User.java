package com.example.emergencyapp.model;

public class User {
    public String uid;
    public String email;
    public String role;
    public String room;
    public Boolean confirmed;

    public String name;

    public User() { }  // нужен для Firebase

    public User(String uid, String email, String role, String room, Boolean confirmed, String name) {
        this.uid = uid;
        this.email = email;
        this.role = role;
        this.room = room;
        this.confirmed = confirmed;
        this.name      = name;
    }
}
