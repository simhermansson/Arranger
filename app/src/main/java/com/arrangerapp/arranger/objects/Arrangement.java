package com.arrangerapp.arranger.objects;

import java.util.ArrayList;

public class Arrangement {
    private String name;
    private ArrayList<Task> tasks;

    public Arrangement(String name) {
        this.name = name;
    }

    public void addTask(Task task) {
        tasks.add(task);
    }

    public ArrayList<Task> getTasks() {
        return tasks;
    }

    public String getName() {
        return name;
    }
}
