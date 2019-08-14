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
    private WeekTaskExpandableAdapter weekTaskExpandableAdapter;

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
        ArrayList<Task> todayList = activity.readFromInternalStorage(Repeat.TODAY.toString() + ".json");
        ArrayList<Task> everyDayList = activity.readFromInternalStorage(Repeat.DAILY.toString() + ".json");
        ArrayList<Task> mondayList = activity.readFromInternalStorage(Repeat.MONDAY.toString() + ".json");
        ArrayList<Task> tuesdayList = activity.readFromInternalStorage(Repeat.TUESDAY.toString() + ".json");
        ArrayList<Task> wednesdayList = activity.readFromInternalStorage(Repeat.WEDNESDAY.toString() + ".json");
        ArrayList<Task> thursdayList = activity.readFromInternalStorage(Repeat.THURSDAY.toString() + ".json");
        ArrayList<Task> fridayList = activity.readFromInternalStorage(Repeat.FRIDAY.toString() + ".json");
        ArrayList<Task> saturdayList = activity.readFromInternalStorage(Repeat.SATURDAY.toString() + ".json");
        ArrayList<Task> sundayList = activity.readFromInternalStorage(Repeat.SUNDAY.toString() + ".json");

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

        //Create and sort a list of the keys in the HashMap
        ArrayList<String> expandableTitleList = new ArrayList<>();
        expandableTitleList.add(Repeat.TODAY.toString());
        expandableTitleList.add(Repeat.DAILY.toString());
        expandableTitleList.add(Repeat.MONDAY.toString());
        expandableTitleList.add(Repeat.TUESDAY.toString());
        expandableTitleList.add(Repeat.WEDNESDAY.toString());
        expandableTitleList.add(Repeat.THURSDAY.toString());
        expandableTitleList.add(Repeat.FRIDAY.toString());
        expandableTitleList.add(Repeat.SATURDAY.toString());
        expandableTitleList.add(Repeat.SUNDAY.toString());

        //Set up ListView
        ExpandableListView expandableListView = view.findViewById(R.id.weekList);
        weekTaskExpandableAdapter = new WeekTaskExpandableAdapter(expandableTaskList, expandableTitleList, activity);
        expandableListView.setAdapter(weekTaskExpandableAdapter);
        //Expand all groups
        for (int i = 0; i < weekTaskExpandableAdapter.getGroupCount(); i++) {
            expandableListView.expandGroup(i);
        }

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (MainActivity) context;
    }
}
