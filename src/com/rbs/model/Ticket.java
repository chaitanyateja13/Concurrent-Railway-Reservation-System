package com.rbs.model;

import java.time.LocalDate;
import java.util.List;

public class Ticket {
    private long id;
    private long userId;
    private long trainId;
    private String pnr;
    private String travelClass; // SL, 3A, 2A, 1A
    private String category; // General, Tatkal
    private LocalDate journeyDate;
    private List<String> passengers; // simple names list for placeholder
    private double fare;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getPnr() { return pnr; }
    public void setPnr(String pnr) { this.pnr = pnr; }
    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }
    public long getTrainId() { return trainId; }
    public void setTrainId(long trainId) { this.trainId = trainId; }
    public String getTravelClass() { return travelClass; }
    public void setTravelClass(String travelClass) { this.travelClass = travelClass; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public LocalDate getJourneyDate() { return journeyDate; }
    public void setJourneyDate(LocalDate journeyDate) { this.journeyDate = journeyDate; }
    public List<String> getPassengers() { return passengers; }
    public void setPassengers(List<String> passengers) { this.passengers = passengers; }
    public double getFare() { return fare; }
    public void setFare(double fare) { this.fare = fare; }
}



