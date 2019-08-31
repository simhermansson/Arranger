package com.arrangerapp.arranger.listview_adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageButton;

import com.arrangerapp.arranger.R;
import com.arrangerapp.arranger.activities.MainActivity;
import com.arrangerapp.arranger.enums.Repeat;
import com.arrangerapp.arranger.objects.Arrangement;
import com.arrangerapp.arranger.objects.Task;
import com.arrangerapp.arranger.objects.TaskComparator;
import com.arrangerapp.arranger.tools.NotificationSchedule;
import com.arrangerapp.arranger.tools.StorageReaderWriter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

public class ArrangementListAdapter extends ArrayAdapter<Arrangement> {
    private Context context;
    private ArrayList<Arrangement> arrangements;
    private MainActivity mainActivity;
    private StorageReaderWriter storageReaderWriter;
    private NotificationSchedule notificationSchedule;
    private boolean visibleCheckBoxes;

    public ArrangementListAdapter(ArrayList<Arrangement> arrangements, Context context) {
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
        AppCompatImageButton imageButton;
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
            viewHolder.imageButton = (AppCompatImageButton) convertView.findViewById(R.id.move_single_arrangement);
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
            viewHolder.imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showMoveAlert(arrangement);
                }
            });
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
        // Load today list.
        ArrayList<Task> todayList = storageReaderWriter.readTaskList(Repeat.TODAY.toString() + ".json");

        for (Arrangement arrangement : arrangements) {
            if (arrangement.isChecked()) {
                // Move tasks to today list.
                for (Task task : arrangement.getTasks()) {
                    // Add to today list and schedule notifications if needed.
                    todayList.add(task);
                    notificationSchedule.toSchedule(task);

                    // Check if scheduled, if so; put in correct schedule list.
                    Repeat repeat = task.getRepeats();
                    boolean notScheduledForToday = !Repeat.TODAY.equals(repeat);
                    if (notScheduledForToday) {
                        ArrayList<Task> tasks = new StorageReaderWriter(mainActivity).readTaskList(repeat.toString() + ".json");
                        tasks.add(task);
                        storageReaderWriter.writeList(repeat.toString() + ".json", tasks);
                    }
                }
            }
        }

        // Sort and write new today list.
        Collections.sort(todayList, new TaskComparator());
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

    private void showMoveAlert(final Arrangement arrangement) {
        new AlertDialog.Builder(context)
                .setTitle("Copy Arrangement")
                .setMessage("Copy arrangement to today?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Move tasks to today list.
                        ArrayList<Task> todayList = storageReaderWriter.readTaskList(Repeat.TODAY.toString() + ".json");
                        for (Task task : arrangement.getTasks()) {

                            // Get current day of week with correct Repeat indexing.
                            int dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1;

                            // Booleans for checking if task is scheduled for today or is a daily task.
                            boolean oneTimeTask = task.getRepeats().equals(Repeat.TODAY);
                            boolean scheduledForToday = task.getRepeats().equals(Repeat.values()[dayOfWeek]);
                            boolean scheduledDaily = task.getRepeats().equals(Repeat.DAILY);

                            // Add task to taskList, sort the new taskList and notify listAdapter if task scheduled for today.
                            if (oneTimeTask || scheduledForToday || scheduledDaily) {
                                todayList.add(task);
                            }

                            // Schedule notifications if needed.
                            notificationSchedule.toSchedule(task);

                            // Check if scheduled, if so; put in correct schedule list.
                            Repeat repeat = task.getRepeats();
                            boolean notScheduledForToday = !Repeat.TODAY.equals(repeat);
                            if (notScheduledForToday) {
                                ArrayList<Task> tasks = new StorageReaderWriter(mainActivity).readTaskList(repeat.toString() + ".json");
                                tasks.add(task);
                                storageReaderWriter.writeList(repeat.toString() + ".json", tasks);
                            }

                        }
                        Collections.sort(todayList, new TaskComparator());
                        storageReaderWriter.writeList(Repeat.TODAY.toString() + ".json", todayList);
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
}
