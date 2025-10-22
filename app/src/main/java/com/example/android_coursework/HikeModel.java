package com.example.android_coursework;

public class HikeModel {
    private int id;
    private String title;
    private String location;
    private String date;
    private String parking;
    private double length;
    private String difficulty;
    private String description;
    private String weather;
    private String companions;
    private String imageUri;

    // create constructor full data
    public HikeModel(int id, String title, String location, String date, String parking,
                     double length, String difficulty, String description,
                     String weather, String companions, String imageUri) {
        this.id = id;
        this.title = title;
        this.location = location;
        this.date = date;
        this.parking = parking;
        this.length = length;
        this.difficulty = difficulty;
        this.description = description;
        this.weather = weather;
        this.companions = companions;
        this.imageUri = imageUri;
    }

    // constructor with few data
    public HikeModel(String imageUri, String title, double length) {
        this.imageUri = imageUri;
        this.title = title;
        this.length = length;
    }

    // Function get data
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getLocation() { return location; }
    public String getDate() { return date; }
    public String getParking() { return parking; }
    public double getLength() { return length; }
    public String getDifficulty() { return difficulty; }
    public String getDescription() { return description; }
    public String getWeather() { return weather; }
    public String getCompanions() { return companions; }
    public String getImageUri() { return imageUri; }

    // function set data
    public void setId(int id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setLocation(String location) { this.location = location; }
    public void setDate(String date) { this.date = date; }
    public void setParking(String parking) { this.parking = parking; }
    public void setLength(double length) { this.length = length; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public void setDescription(String description) { this.description = description; }
    public void setWeather(String weather) { this.weather = weather; }
    public void setCompanions(String companions) { this.companions = companions; }
    public void setImageUri(String imageUri) { this.imageUri = imageUri; }


}
