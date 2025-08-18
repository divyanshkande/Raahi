package com.divyansh.aitourplanner.model;

import java.util.List;

public class TourRequest {
    private String city;
    private int days;
    private List<String> interests;

    public String getCity() {
        return city;
    }

    public int getDays() {
        return days;
    }

    public List<String> getInterests() {
        return interests;
    }
}
