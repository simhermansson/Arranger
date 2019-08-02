package com.simon.arranger.listview_adapters;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import com.simon.arranger.R;
import com.simon.arranger.objects.Task;
import java.util.ArrayList;

public class TaskAdapter extends ArrayAdapter<Task> {
    private Context context;
    private ArrayList<Task> tasks;

    public TaskAdapter(ArrayList<Task> tasks, Context context) {
        super(context, R.layout.task_view, tasks);
        this.context = context;
        this.tasks = tasks;
    }

    @Override
    public Task getItem(int position) {
        return tasks.get(position);
    }

    @Override
    public int getCount() {
        return tasks.size();
    }

    static class ViewHolderItem {
        TextView taskName;
        TextView taskDate;
        Button taskCheck;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        final ViewHolderItem viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.task_view, parent, false);

            viewHolder = new ViewHolderItem();
            viewHolder.taskName = (TextView) convertView.findViewById(R.id.taskName);
            viewHolder.taskDate = (TextView) convertView.findViewById(R.id.taskDate);
            viewHolder.taskCheck = (Button) convertView.findViewById(R.id.taskCheck);
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolderItem) convertView.getTag();
        }

        final Task task = getItem(position);
        if (task != null) {
            viewHolder.taskName.setText(task.getName());
            viewHolder.taskDate.setText(task.getDate().toString());
        }

        return convertView;
    }
}
