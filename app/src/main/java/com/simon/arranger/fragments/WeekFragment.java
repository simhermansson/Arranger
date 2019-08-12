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
import com.simon.arranger.listview_adapters.WeekTaskExpandableAdapter;
import com.simon.arranger.objects.Task;

import java.util.ArrayList;

public class WeekFragment extends Fragment {
    private MainActivity activity;
    private static final String JSON_FILE = "tasks_today.json";
    private ArrayList<Task> taskList;
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

        //Read tasks from memory and assign them to taskList
        taskList = activity.readFromInternalStorage(JSON_FILE);

        //Set up ListView
        ExpandableListView expandableListView = view.findViewById(R.id.weekList);
        weekTaskExpandableAdapter = new WeekTaskExpandableAdapter(taskList, activity);
        expandableListView.setAdapter(weekTaskExpandableAdapter);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (MainActivity) context;
    }
}
