package com.github.rishil.crimewiz.core.objects;

public class Crime{
    // Crime location
    private Double latitude, longitude;
    private int streetId;
    private String streetName;

    // Crime info
    private int crimeId;
    private String date;
    private String category;
    private String outcome;

    // Get/Set Location
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public int getStreetId() { return streetId; }
    public void setStreetId(int streetId) { this.streetId = streetId; }

    public String getStreetName() { return streetName; }
    public void setStreetName(String streetName) { this.streetName = streetName; }

    // Get/Set Crime info
    public int getCrimeId() { return crimeId; }
    public void setCrimeid(int crimeId) { this.crimeId = crimeId; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getCategory(){ return category; }
    public void setCategory(String category){this.category = category; }

    public String getOutcome() { return outcome; }
    public void setOutcome(String outcome) { this.outcome = outcome; }



}
