package com.rbs.model;

import java.time.LocalDateTime;

public class Reservation {
    public enum Status { CONFIRMED, WAITLISTED, CANCELLED, REFUNDED }

    private long id;
    private long ticketId;
    private Status status;
    private LocalDateTime createdAt;
    // convenience fields populated from joined ticket/train queries
    private String pnr;
    private String trainName;
    private java.time.LocalDate journeyDate;
    private String fromStation;
    private String toStation;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getTicketId() { return ticketId; }
    public void setTicketId(long ticketId) { this.ticketId = ticketId; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getPnr() { return pnr; }
    public void setPnr(String pnr) { this.pnr = pnr; }
    public String getTrainName() { return trainName; }
    public void setTrainName(String trainName) { this.trainName = trainName; }
    public java.time.LocalDate getJourneyDate() { return journeyDate; }
    public void setJourneyDate(java.time.LocalDate journeyDate) { this.journeyDate = journeyDate; }
    public String getFromStation() { return fromStation; }
    public void setFromStation(String fromStation) { this.fromStation = fromStation; }
    public String getToStation() { return toStation; }
    public void setToStation(String toStation) { this.toStation = toStation; }
}



