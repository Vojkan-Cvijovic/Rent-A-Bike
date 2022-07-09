package org.bikerent.api.model;

public class Bike {
    String id;
    String location;
    String manufacturer;
    boolean used;
    boolean active;

    public Bike(String id, String location, String manufacturer, boolean used, boolean active) {
        this.id = id;
        this.location = location;
        this.manufacturer = manufacturer;
        this.used = used;
        this.active = active;
    }

    public Bike(String id, boolean used) {
        this.id = id;
        this.used = used;
    }
}