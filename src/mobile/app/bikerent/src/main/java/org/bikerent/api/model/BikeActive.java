package org.bikerent.api.model;

public class BikeActive {
    String id;
    boolean active;

    public BikeActive(String id, boolean active) {
        this.id = id;
        this.active = active;
    }

    public String getId() {
        return id;
    }


    public boolean isActive() {
        return active;
    }
}
