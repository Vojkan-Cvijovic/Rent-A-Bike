package org.bikerent.api.model;

public class BikeStatus {
    String id;
    boolean used;

    public BikeStatus(String id, boolean used) {
        this.id = id;
        this.used = used;
    }

    public String getId() {
        return id;
    }

    public boolean isUsed() {
        return used;
    }
}
