package com.arrangerapp.arranger.listview_adapters;

import android.content.Context;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageButton;

import com.arrangerapp.arranger.R;
import com.arrangerapp.arranger.activities.MainActivity;
import com.arrangerapp.arranger.enums.Repeat;
import com.arrangerapp.arranger.objects.Arrangement;
import com.arrangerapp.arranger.objects.Task;
import com.arrangerapp.arranger.tools.NotificationSchedule;
import com.arrangerapp.arranger.tools.StorageReaderWriter;

import java.util.ArrayList;

public class ArrangementAdapter extends ArrayAdapter<Arrangement> {
    private Context context;
    private ArrayList<Arrangement> arrangements;
    private MainActivity mainActivity;
    private StorageReaderWriter storageReaderWriter;
    private NotificationSchedule notificationSchedule;
    private boolean visibleCheckBoxes;

    public ArrangementAdapter(ArrayList<Arrangement> arrangements, Context context) {
        super(context, R.layout.task_view, arrangements);
        this.context = context;
        this.arrangements = arrangements;
        mainActivity = (MainActivity) context;
        storageReaderWriter = new StorageReaderWriter(mainActivity);
        notificationSchedule = new NotificationSchedule(mainActivity);
        visibleCheckBoxes = false;
    }

    @Override
    public Arrangement getItem(int position) {
        return arrangements.get(position);
    }

    @Override
    public int getCount() {
        return arrangements.size();
    }

    static class ViewHolderItem {
        TextView arrangementName;
        TextView numberOfTasks;
        CheckBox checkBox;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        final ViewHolderItem viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.arrangement_view, parent, false);

            viewHolder = new ViewHolderItem();
            viewHolder.arrangementName = (TextView) convertView.findViewById(R.id.arrangementName);
            viewHolder.numberOfTasks = (TextView) convertView.findViewById(R.id.numberOfTasks);
            viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.checkBox);
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolderItem) convertView.getTag();
        }

        final Arrangement arrangement = getItem(position);
        if (arrangement != null) {
            viewHolder.arrangementName.setText(arrangement.getName());
            viewHolder.numberOfTasks.setText(arrangement.getNumberOfTasks() + " Tasks");
            if (visibleCheckBoxes) {
                viewHolder.checkBox.setVisibility(View.VISIBLE);
                if (arrangement.isChecked()) {
                    viewHolder.checkBox.setChecked(true);
                } else {
                    viewHolder.checkBox.setChecked(false);
                }
                viewHolder.checkBox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (viewHolder.checkBox.isChecked()) {
                            arrangement.check(true);
                        } else {
                            arrangement.check(false);
                        }
                    }
                });
            } else {
                viewHolder.checkBox.setVisibility(View.INVISIBLE);
            }
        }

        return convertView;
    }

    public void setCheckBoxes(boolean visible) {
        visibleCheckBoxes = visible;
    }

    public void removeCheckedItems() {
        ArrayList<Arrangement> toBeRemoved = new ArrayList<>();
        for (Arrangement arrangement : arrangements) {
            if (arrangement.isChecked()) {
                toBeRemoved.add(arrangement);
            }
        }
        arrangements.removeAll(toBeRemoved);
        notifyDataSetChanged();
        storageReaderWriter.writeList("arrangements.json", arrangements);
    }

    public void moveCheckedItems() {
        ArrayList<Task> toBeMoved = new ArrayList<>();
        for (Arrangement arrangement : arrangements) {
            if (arrangement.isChecked()) {
                toBeMoved.addAll(arrangement.getTasks());
            }
        }
        ArrayList<Task> todayList = storageReaderWriter.readTaskList(Repeat.TODAY.toString() + ".json");
        todayList.addAll(toBeMoved);
        storageReaderWriter.writeList(Repeat.TODAY.toString() + ".json", todayList);
    }

    public void uncheckAll() {
        for (Arrangement arrangement : arrangements) {
            if (arrangement.isChecked()) {
                arrangement.check(false);
            }
        }
        notifyDataSetChanged();
    }
}
