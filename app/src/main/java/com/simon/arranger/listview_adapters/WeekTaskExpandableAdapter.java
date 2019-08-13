package com.simon.arranger.listview_adapters;

import android.content.Context;
import android.support.v7.widget.AppCompatImageButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.simon.arranger.R;
import com.simon.arranger.activity.MainActivity;
import com.simon.arranger.objects.Task;

import java.util.ArrayList;
import java.util.HashMap;

public class WeekTaskExpandableAdapter extends BaseExpandableListAdapter {
    private Context context;
    private HashMap<String, ArrayList<Task>> expandableTaskList;
    private ArrayList<String> expandableTitleList;
    private MainActivity mainActivity;
    private static final String JSON_FILE = "tasks_today.json";

    public WeekTaskExpandableAdapter(HashMap<String, ArrayList<Task>> expandableTaskList, ArrayList<String> expandableTitleList, Context context) {
        this.context = context;
        this.expandableTaskList = expandableTaskList;
        this.expandableTitleList = expandableTitleList;
        mainActivity = (MainActivity) context;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return this.expandableTaskList.get(this.expandableTitleList.get(groupPosition))
                .get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    static class ChildViewHolderItem {
        TextView dayTask;
        TextView taskTime;
        AppCompatImageButton taskCheck;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final ChildViewHolderItem viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.week_group_child, parent, false);

            viewHolder = new ChildViewHolderItem();
            viewHolder.dayTask = (TextView) convertView.findViewById(R.id.taskName);
            viewHolder.taskTime = (TextView) convertView.findViewById(R.id.taskTime);
            viewHolder.taskCheck = (AppCompatImageButton) convertView.findViewById(R.id.taskCheck);
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ChildViewHolderItem) convertView.getTag();
        }

        viewHolder.dayTask.setText(((Task)getChild(groupPosition, childPosition)).getName());
        viewHolder.taskTime.setText(((Task)getChild(groupPosition, childPosition)).getTime());

        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this.expandableTaskList.get(this.expandableTitleList.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.expandableTaskList.get(this.expandableTitleList.get(groupPosition));
    }

    @Override
    public int getGroupCount() {
        return this.expandableTitleList.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    static class GroupViewHolderItem {
        TextView dayName;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        final GroupViewHolderItem viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.week_group_header, parent, false);

            viewHolder = new GroupViewHolderItem();
            viewHolder.dayName = (TextView) convertView.findViewById(R.id.dayName);
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (GroupViewHolderItem) convertView.getTag();
        }

        viewHolder.dayName.setText(this.expandableTitleList.get(groupPosition));

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
