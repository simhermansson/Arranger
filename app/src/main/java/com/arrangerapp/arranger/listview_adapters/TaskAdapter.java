package com.arrangerapp.arranger.listview_adapters;

import android.content.Context;
import androidx.appcompat.widget.AppCompatImageButton;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.arrangerapp.arranger.tools.NotificationSchedule;
import com.arrangerapp.arranger.tools.StorageReaderWriter;
import com.arrangerapp.arranger.activities.MainActivity;
import com.arrangerapp.arranger.R;
import com.arrangerapp.arranger.enums.Repeat;
import com.arrangerapp.arranger.objects.Task;

import java.util.ArrayList;

public class TaskAdapter extends ArrayAdapter<Task> {
    private Context context;
    private ArrayList<Task> tasks;
    private MainActivity mainActivity;
    private StorageReaderWriter storageReaderWriter;
    private NotificationSchedule notificationSchedule;

    public TaskAdapter(ArrayList<Task> tasks, Context context) {
        super(context, R.layout.task_view, tasks);
        this.context = context;
        this.tasks = tasks;
        mainActivity = (MainActivity) context;
        storageReaderWriter = new StorageReaderWriter(mainActivity);
        notificationSchedule = new NotificationSchedule(mainActivity);
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
        TextView taskTime;
        AppCompatImageButton taskCheck;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        final ViewHolderItem viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.task_view, parent, false);

            viewHolder = new ViewHolderItem();
            viewHolder.taskName = (TextView) convertView.findViewById(R.id.taskName);
            viewHolder.taskTime = (TextView) convertView.findViewById(R.id.taskTime);
            viewHolder.taskCheck = (AppCompatImageButton) convertView.findViewById(R.id.taskCheck);
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolderItem) convertView.getTag();
        }

        final Task task = getItem(position);
        if (task != null) {
            viewHolder.taskName.setText(task.getName());
            viewHolder.taskTime.setText(task.getTime());
            viewHolder.taskCheck.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Remove task from taskList, notify the adapter and writeList new list to storage
                    tasks.remove(task);
                    notifyDataSetChanged();
                    if (task.hasDate()) {
                        notificationSchedule.cancelScheduledNotification(task.getId());
                    }
                    storageReaderWriter.writeList(Repeat.TODAY.toString() + ".json", tasks);

                    // Haptic feedback on press
                    v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                }
            });
        }

        return convertView;
    }
}
