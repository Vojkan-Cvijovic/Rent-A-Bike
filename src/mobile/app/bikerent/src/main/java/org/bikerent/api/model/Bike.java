package org.bikerent.api.model;

public class Bike {
    String id;
    String location;
    String manufacturer;
    String year;
    boolean used;
    boolean active;

    public Bike(String id, String location, String manufacturer, String year, boolean used, boolean active) {
        this.id = id;
        this.location = location;
        this.manufacturer = manufacturer;
        this.year = year;
        this.used = used;
        this.active = active;
    }

    public Bike(String id, boolean used) {
        this.id = id;
        this.used = used;
    }

    public Bike(String manufacturer, String year, String location) {
        this.location = location;
        this.manufacturer = manufacturer;
        this.year = year;
        this.used = false;
        this.active = true;
    }

    public String getId() {
        return id;
    }

    public String getLocation() {
        return location;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public String getYear() {
        return year;
    }

    public boolean isUsed() {
        return used;
    }

    public boolean isActive() {
        return active;
    }


}