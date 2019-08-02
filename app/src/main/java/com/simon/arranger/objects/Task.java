package com.simon.arranger.objects;

import java.time.LocalTime;
import java.util.Date;

public class Task {
    private String name;
    private Date date;

    public Task(String name, Date date) {
        this.name = name;
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public Date getDate() {
        return date;
    }
}
