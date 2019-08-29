package com.arrangerapp.arranger.listview_adapters;

import android.content.Context;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageButton;

import com.arrangerapp.arranger.R;
import com.arrangerapp.arranger.activities.MainActivity;
import com.arrangerapp.arranger.objects.Arrangement;
import com.arrangerapp.arranger.objects.Task;
import com.arrangerapp.arranger.tools.StorageReaderWriter;

import java.util.ArrayList;

public class ArrangementAdapter extends ArrayAdapter<Task> {
    private Context context;
    private ArrayList<Task> tasks;
    private Arrangement arrangement;
    private MainActivity mainActivity;
    private StorageReaderWriter storageReaderWriter;

    public ArrangementAdapter(ArrayList<Task> tasks, Context context, Arrangement arrangement) {
        super(context, R.layout.task_view, tasks);
        this.context = context;
        this.tasks = tasks;
        this.arrangement = arrangement;
        mainActivity = (MainActivity) context;
        storageReaderWriter = new StorageReaderWriter(mainActivity);
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
                    arrangement.removeTask(task);
                    notifyDataSetChanged();

                    // Save new task list in storage.
                    ArrayList<Arrangement> arrangementsList = storageReaderWriter.readArrangementList("arrangements.json");
                    arrangementsList.set(arrangement.getListIndex(), arrangement);
                    storageReaderWriter.writeList("arrangements.json", arrangementsList);

                    // Haptic feedback on press
                    v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                }
            });
        }

        return convertView;
    }
}
