package com.example.hitrivia.Classes;

public class User {
    private String firstName;
    private String lastName;
    private String id;
    private String email;
    private int highScore;

    // Default constructor for firebase getValue function
    public  User() {

    }

    public User(String firstName, String lastName, String id, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.id = id;
        this.email = email;
        this.highScore = 0;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public  int getHighScore() {
        return highScore;
    }

}
