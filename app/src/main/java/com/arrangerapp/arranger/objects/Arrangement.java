package com.arrangerapp.arranger.objects;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class Arrangement implements Parcelable {
    private String name;
    private ArrayList<Task> tasks;
    private boolean checked;
    private int listIndex;

    public Arrangement(String name) {
        this.name = name;
        tasks = new ArrayList<>();
        checked = false;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeList(tasks);
        dest.writeByte((byte) (checked ? 1 : 0));
        dest.writeInt(listIndex);
    }

    public static final Parcelable.Creator<Arrangement> CREATOR
            = new Parcelable.Creator<Arrangement>() {
        @Override
        public Arrangement createFromParcel(Parcel in) {
            return new Arrangement(in);
        }

        @Override
        public Arrangement[] newArray(int size) {
            return new Arrangement[size];
        }
    };

    private Arrangement(Parcel in) {
        name = in.readString();
        in.readList(tasks, Task.class.getClassLoader());
        checked = in.readByte() != 0;
        listIndex = in.readInt();
    }

    public void setListIndex(int index) {
        listIndex = index;
    }

    public int getListIndex() {
        return listIndex;
    }

    public void addTask(Task task) {
        tasks.add(task);
    }

    public void check(boolean check) {
        checked = check;
    }

    public boolean isChecked() {
        return checked;
    }

    public ArrayList<Task> getTasks() {
        return tasks;
    }

    public int getNumberOfTasks() {
        return tasks.size();
    }

    public String getName() {
        return name;
    }
}
