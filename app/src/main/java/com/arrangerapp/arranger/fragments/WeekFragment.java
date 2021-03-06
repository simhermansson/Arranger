package com.arrangerapp.arranger.fragments;

import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.arrangerapp.arranger.tools.StorageReaderWriter;
import com.arrangerapp.arranger.activities.MainActivity;
import com.arrangerapp.arranger.R;
import com.arrangerapp.arranger.enums.Repeat;
import com.arrangerapp.arranger.listview_adapters.WeekTaskExpandableAdapter;
import com.arrangerapp.arranger.objects.Task;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.HashMap;

public class WeekFragment extends Fragment {
    private MainActivity activity;
    private WeekTaskExpandableAdapter weekTaskExpandableAdapter;
    private StorageReaderWriter storageReaderWriter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.week_fragment, container, false);

        // Set title of toolbar
        activity.setTitle("Week");

        // Read tasks from storage and assign them to the correct lists
        ArrayList<Task> everyDayList = storageReaderWriter.readTaskList(Repeat.DAILY.toString() + ".json");
        ArrayList<Task> todayList = storageReaderWriter.readTaskList(Repeat.TODAY.toString() + ".json");
        ArrayList<Task> mondayList = storageReaderWriter.readTaskList(Repeat.MONDAY.toString() + ".json");
        ArrayList<Task> tuesdayList = storageReaderWriter.readTaskList(Repeat.TUESDAY.toString() + ".json");
        ArrayList<Task> wednesdayList = storageReaderWriter.readTaskList(Repeat.WEDNESDAY.toString() + ".json");
        ArrayList<Task> thursdayList = storageReaderWriter.readTaskList(Repeat.THURSDAY.toString() + ".json");
        ArrayList<Task> fridayList = storageReaderWriter.readTaskList(Repeat.FRIDAY.toString() + ".json");
        ArrayList<Task> saturdayList = storageReaderWriter.readTaskList(Repeat.SATURDAY.toString() + ".json");
        ArrayList<Task> sundayList = storageReaderWriter.readTaskList(Repeat.SUNDAY.toString() + ".json");

        // Create a HashMap and put in all the lists
        HashMap<String, ArrayList<Task>> expandableTaskList = new HashMap<>();
        expandableTaskList.put(Repeat.DAILY.toString(), everyDayList);
        expandableTaskList.put(Repeat.TODAY.toString(), todayList);
        expandableTaskList.put(Repeat.MONDAY.toString(), mondayList);
        expandableTaskList.put(Repeat.TUESDAY.toString(), tuesdayList);
        expandableTaskList.put(Repeat.WEDNESDAY.toString(), wednesdayList);
        expandableTaskList.put(Repeat.THURSDAY.toString(), thursdayList);
        expandableTaskList.put(Repeat.FRIDAY.toString(), fridayList);
        expandableTaskList.put(Repeat.SATURDAY.toString(), saturdayList);
        expandableTaskList.put(Repeat.SUNDAY.toString(), sundayList);

        // Create and sort a list of the keys in the HashMap
        ArrayList<String> expandableTitleList = new ArrayList<>();
        expandableTitleList.add(Repeat.DAILY.toString());
        expandableTitleList.add(Repeat.TODAY.toString());
        expandableTitleList.add(Repeat.MONDAY.toString());
        expandableTitleList.add(Repeat.TUESDAY.toString());
        expandableTitleList.add(Repeat.WEDNESDAY.toString());
        expandableTitleList.add(Repeat.THURSDAY.toString());
        expandableTitleList.add(Repeat.FRIDAY.toString());
        expandableTitleList.add(Repeat.SATURDAY.toString());
        expandableTitleList.add(Repeat.SUNDAY.toString());

        // Set up ListView
        ExpandableListView expandableListView = view.findViewById(R.id.weekList);
        weekTaskExpandableAdapter = new WeekTaskExpandableAdapter(expandableTaskList, expandableTitleList, activity);
        expandableListView.setAdapter(weekTaskExpandableAdapter);
        // Expand all groups
        for (int i = 0; i < weekTaskExpandableAdapter.getGroupCount(); i++) {
            expandableListView.expandGroup(i);
        }

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (MainActivity) context;
        storageReaderWriter = new StorageReaderWriter(activity);
    }
}
