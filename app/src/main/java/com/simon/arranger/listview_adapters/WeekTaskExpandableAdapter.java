package com.simon.arranger.listview_adapters;

import android.content.Context;
import android.support.v7.widget.AppCompatImageButton;
import android.view.HapticFeedbackConstants;
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
        AppCompatImageButton taskRemove;
    }

    @Override
    public View getChildView(final int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final ChildViewHolderItem viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.week_group_child, parent, false);

            viewHolder = new ChildViewHolderItem();
            viewHolder.dayTask = (TextView) convertView.findViewById(R.id.taskName);
            viewHolder.taskTime = (TextView) convertView.findViewById(R.id.taskTime);
            viewHolder.taskRemove = (AppCompatImageButton) convertView.findViewById(R.id.taskRemove);
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ChildViewHolderItem) convertView.getTag();
        }

        final Task task = (Task) getChild(groupPosition, childPosition);
        if (task != null) {
            viewHolder.dayTask.setText(task.getName());
            viewHolder.taskTime.setText(task.getTime());
            viewHolder.taskRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Remove task from taskList, notify the adapter and write new list to storage
                    expandableTaskList.get(expandableTitleList.get(groupPosition)).remove(task);
                    notifyDataSetChanged();
                    mainActivity.writeToInternalStorage(expandableTitleList.get(groupPosition) + ".json",
                            expandableTaskList.get(expandableTitleList.get(groupPosition)));

                    //Haptic feedback on press
                    v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                }
            });
        }

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
