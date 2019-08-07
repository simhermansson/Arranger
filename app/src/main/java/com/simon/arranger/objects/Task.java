package com.simon.arranger.objects;

public class Task {
    private String task_name;
    private String task_time;

    public Task(String task_name, String task_time) {
        this.task_name = task_name;
        this.task_time = task_time;
    }

    public String getName() {
        return task_name;
    }

    public String getTime() {
        return task_time;
    }
}
