package com.rbs.model;

public class Waitlist {
    private long id;
    private long reservationId;
    private int position;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getReservationId() { return reservationId; }
    public void setReservationId(long reservationId) { this.reservationId = reservationId; }
    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }
}



