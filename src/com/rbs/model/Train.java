package com.rbs.model;

import java.time.LocalTime;
import java.util.List;

public class Train {
    private long id;
    private String name;
    private String number;
    private List<String> runDays; // MTWTFS
    private String fromStation;
    private String toStation;
    private LocalTime departure;
    private LocalTime arrival;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getNumber() { return number; }
    public void setNumber(String number) { this.number = number; }
    public List<String> getRunDays() { return runDays; }
    public void setRunDays(List<String> runDays) { this.runDays = runDays; }
    public String getFromStation() { return fromStation; }
    public void setFromStation(String fromStation) { this.fromStation = fromStation; }
    public String getToStation() { return toStation; }
    public void setToStation(String toStation) { this.toStation = toStation; }
    public LocalTime getDeparture() { return departure; }
    public void setDeparture(LocalTime departure) { this.departure = departure; }
    public LocalTime getArrival() { return arrival; }
    public void setArrival(LocalTime arrival) { this.arrival = arrival; }
}



