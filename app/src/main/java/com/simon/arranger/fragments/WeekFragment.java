package com.simon.arranger.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.simon.arranger.activity.MainActivity;
import com.simon.arranger.R;
import com.simon.arranger.enums.Repeat;
import com.simon.arranger.listview_adapters.WeekTaskExpandableAdapter;
import com.simon.arranger.objects.Task;

import java.util.ArrayList;
import java.util.HashMap;

public class WeekFragment extends Fragment {
    private MainActivity activity;
    private static final String JSON_FILE = "tasks_today.json";
    private WeekTaskExpandableAdapter weekTaskExpandableAdapter;

    private ArrayList<Task> todayList;
    private ArrayList<Task> everyDayList;
    private ArrayList<Task> mondayList;
    private ArrayList<Task> tuesdayList;
    private ArrayList<Task> wednesdayList;
    private ArrayList<Task> thursdayList;
    private ArrayList<Task> fridayList;
    private ArrayList<Task> saturdayList;
    private ArrayList<Task> sundayList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.week_fragment, container, false);

        //Set title of toolbar
        activity.setTitle("Week");

        //Read tasks from storage and assign them to the correct lists
        todayList = activity.readFromInternalStorage(JSON_FILE);
        everyDayList = activity.readFromInternalStorage(Repeat.DAILY.toString() + ".json");
        mondayList = activity.readFromInternalStorage(Repeat.MONDAY.toString() + ".json");
        tuesdayList = activity.readFromInternalStorage(Repeat.TUESDAY.toString() + ".json");
        wednesdayList = activity.readFromInternalStorage(Repeat.WEDNESDAY.toString() + ".json");
        thursdayList = activity.readFromInternalStorage(Repeat.THURSDAY.toString() + ".json");
        fridayList = activity.readFromInternalStorage(Repeat.FRIDAY.toString() + ".json");
        saturdayList = activity.readFromInternalStorage(Repeat.SATURDAY.toString() + ".json");
        sundayList = activity.readFromInternalStorage(Repeat.SUNDAY.toString() + ".json");

        //Create a HashMap and put in all the lists
        HashMap<String, ArrayList<Task>> expandableTaskList = new HashMap<>();
        expandableTaskList.put(Repeat.TODAY.toString(), todayList);
        expandableTaskList.put(Repeat.DAILY.toString(), everyDayList);
        expandableTaskList.put(Repeat.MONDAY.toString(), mondayList);
        expandableTaskList.put(Repeat.TUESDAY.toString(), tuesdayList);
        expandableTaskList.put(Repeat.WEDNESDAY.toString(), wednesdayList);
        expandableTaskList.put(Repeat.THURSDAY.toString(), thursdayList);
        expandableTaskList.put(Repeat.FRIDAY.toString(), fridayList);
        expandableTaskList.put(Repeat.SATURDAY.toString(), saturdayList);
        expandableTaskList.put(Repeat.SUNDAY.toString(), sundayList);

        //Set up ListView
        ExpandableListView expandableListView = view.findViewById(R.id.weekList);
        weekTaskExpandableAdapter = new WeekTaskExpandableAdapter(expandableTaskList, activity);
        expandableListView.setAdapter(weekTaskExpandableAdapter);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (MainActivity) context;
    }
}
