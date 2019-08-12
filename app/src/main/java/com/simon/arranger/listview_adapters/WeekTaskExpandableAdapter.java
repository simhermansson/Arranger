package com.simon.arranger.listview_adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;

import com.simon.arranger.activity.MainActivity;
import com.simon.arranger.objects.Task;

import java.util.ArrayList;

public class WeekTaskExpandableAdapter extends BaseExpandableListAdapter {
    private Context context;
    private ArrayList<Task> tasks;
    private MainActivity mainActivity;
    private static final String JSON_FILE = "tasks_today.json";

    public WeekTaskExpandableAdapter(ArrayList<Task> tasks, Context context) {
        this.context = context;
        this.tasks = tasks;
        mainActivity = (MainActivity) context;
    }

    @Override
    public int getGroupCount() {
        return 0;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 0;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return null;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return null;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return 0;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        return null;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        return null;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}
