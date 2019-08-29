package com.arrangerapp.arranger.objects;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class Arrangement implements Parcelable {
    private String name;
    private ArrayList<Task> tasks;
    private boolean checked;
    private int listIndex;
    private String notes;
    private boolean hasNotesEnabled;

    public Arrangement(String name) {
        this.name = name;
        tasks = new ArrayList<>();
        checked = false;
        hasNotesEnabled = false;
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
        dest.writeString(notes);
        dest.writeByte((byte) (hasNotesEnabled ? 1 : 0));
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
        notes = in.readString();
        hasNotesEnabled = in.readByte() != 0;
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

    public void removeTask(Task task) {
        tasks.remove(task);
    }

    public void addNotes(String notes) {
        this.notes = notes;
    }

    public String getNotes() {
        return notes;
    }

    public boolean hasNotes() {
        return notes != null;
    }

    public void setNotesEnabled(boolean bool) {
        hasNotesEnabled = bool;
    }

    public boolean hasNotesEnabled() {
        return hasNotesEnabled;
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
