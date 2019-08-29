package com.arrangerapp.arranger.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.arrangerapp.arranger.tools.DailyTaskReschedule;
import com.arrangerapp.arranger.tools.NotificationSchedule;
import com.arrangerapp.arranger.tools.StorageReaderWriter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.AppCompatImageButton;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;

import com.arrangerapp.arranger.activities.MainActivity;
import com.arrangerapp.arranger.R;
import com.arrangerapp.arranger.enums.Repeat;
import com.arrangerapp.arranger.listview_adapters.TaskAdapter;
import com.arrangerapp.arranger.objects.Task;
import com.arrangerapp.arranger.objects.TaskComparator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;


public class TodayFragment extends Fragment {
    private MainActivity activity;
    private ArrayList<Task> taskList;
    private TaskAdapter taskAdapter;
    private StorageReaderWriter storageReaderWriter;
    private NotificationSchedule notificationSchedule;
    private DailyTaskReschedule dailyTaskReschedule;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.today_fragment, container, false);

        // Set title of toolbar
        activity.setTitle("Today");

        // Read tasks from storage and assign them to taskList
        taskList = dailyTaskReschedule.getAndScheduleTasks();

        // Set up ListView
        ListView listView = view.findViewById(R.id.taskList);
        listView.setEmptyView(view.findViewById(R.id.list_empty));
        taskAdapter = new TaskAdapter(taskList, activity);
        listView.setAdapter(taskAdapter);

        // Handle the floating action button and the popup input bar
        final FloatingActionButton floatingActionButton = view.findViewById(R.id.floatingActionButton);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                floatingActionButton.hide();
                openInputDialog(floatingActionButton);
            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (MainActivity) context;
        storageReaderWriter = new StorageReaderWriter(activity);
        notificationSchedule = new NotificationSchedule(activity);
        dailyTaskReschedule = new DailyTaskReschedule(activity);
    }

    /**
     * Opens an EditText dialog view above the keyboard.
     * @param floatingActionButton The fab that was pressed to open the dialog so that this
     *                             method can hide and show it when necessary.
     */
    private void openInputDialog(final FloatingActionButton floatingActionButton) {
        // Create dialog for EditText view
        final Dialog inputDialog = new Dialog(activity, R.style.InputDialogTheme);
        inputDialog.setContentView(R.layout.input_dialog);
        WindowManager.LayoutParams params = inputDialog.getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.BOTTOM;
        inputDialog.getWindow().setAttributes(params);
        inputDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        inputDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                // Close keyboard
                InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

                floatingActionButton.show();
            }
        });
        inputDialog.show();

        // Open keyboard
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

        // Handle dialog inputs
        final EditText inputDialogEditTaskName = inputDialog.findViewById(R.id.inputText);
        inputDialogEditTaskName.requestFocus();
        AppCompatImageButton inputDialogImageButton = inputDialog.findViewById(R.id.inputImageButton);
        inputDialogImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get input from EditText field
                String taskInput = inputDialogEditTaskName.getText().toString();
                // Check that input field is not empty
                if (taskInput.length() > 0) {
                    createTask(taskInput);
                    inputDialog.cancel();
                } else {
                    inputDialogEditTaskName.requestFocus();
                    inputDialogEditTaskName.setError("This field cannot be blank");
                }
            }
        });
    }

    /**
     * Creates and schedules a task and its notifications.
     * @param input
     */
    private void createTask(String input) {
        Task task = new Task(input);

        // Get current day of week with correct Repeat indexing.
        int dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1;

        // Booleans for checking if task is scheduled for today or is a daily task.
        boolean oneTimeTask = task.getRepeats().equals(Repeat.TODAY);
        boolean scheduledForToday = task.getRepeats().equals(Repeat.values()[dayOfWeek]);
        boolean scheduledDaily = task.getRepeats().equals(Repeat.DAILY);

        if (oneTimeTask) {
            taskList.add(task);
            if (task.hasDate()) {
                notificationSchedule.scheduleNotification(task);
            }
        } else if (scheduledForToday || scheduledDaily) {
            taskList.add(task);
            notificationSchedule.scheduleNotification(task);
        }

        // Sort the new task list.
        Collections.sort(taskList, new TaskComparator());
        taskAdapter.notifyDataSetChanged();

        // Save task list to internal storage and task to correct schedule if so needed.
        storageReaderWriter.writeList(Repeat.TODAY.toString() + ".json", taskList);

        Repeat repeat = task.getRepeats();
        boolean notScheduledForToday = !Repeat.TODAY.equals(repeat);
        if (notScheduledForToday) {
            ArrayList<Task> tasks = new StorageReaderWriter(activity).readTaskList(repeat.toString() + ".json");
            tasks.add(task);
            storageReaderWriter.writeList(repeat.toString() + ".json", tasks);
        }
    }
}