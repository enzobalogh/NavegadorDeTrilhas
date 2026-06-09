package com.example.trilhasapp.model;

public class PontoTrilha {

    private long id;          
    private long trilhaId;    
    private double latitude;  
    private double longitude; 
    private String timestamp; 

    public PontoTrilha() {}

    public PontoTrilha(long trilhaId, double latitude, double longitude, String timestamp) {
        this.trilhaId = trilhaId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
    }

    public PontoTrilha(long id, long trilhaId, double latitude, double longitude, String timestamp) {
        this.id = id;
        this.trilhaId = trilhaId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getTrilhaId() { return trilhaId; }
    public void setTrilhaId(long trilhaId) { this.trilhaId = trilhaId; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return "PontoTrilha{" +
                "id=" + id +
                ", trilhaId=" + trilhaId +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
