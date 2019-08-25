package com.arrangerapp.arranger.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
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

        //Set title of toolbar
        activity.setTitle("Today");

        //Read tasks from storage and assign them to taskList
        taskList = dailyTaskReschedule.getAndScheduleTasks();

        //Set up ListView
        ListView listView = view.findViewById(R.id.taskList);
        listView.setEmptyView(view.findViewById(R.id.list_empty));
        taskAdapter = new TaskAdapter(taskList, activity);
        listView.setAdapter(taskAdapter);

        //Handle the floating action button and the popup input bar
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

    private void openInputDialog(final FloatingActionButton floatingActionButton) {
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
                //Close keyboard
                InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

                floatingActionButton.show();
            }
        });
        inputDialog.show();

        //Open keyboard
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

        //Handle dialog inputs
        final EditText inputDialogEditTaskName = inputDialog.findViewById(R.id.inputTaskName);
        inputDialogEditTaskName.requestFocus();
        //TODO create listener for highlighting input text
        AppCompatImageButton inputDialogImageButton = inputDialog.findViewById(R.id.inputImageButton);
        inputDialogImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String taskInput = inputDialogEditTaskName.getText().toString();
                //Check that input field is not empty
                if (taskInput.length() > 0) {
                    //Create and add task to taskList and tell taskAdapter to update
                    Task task = new Task(taskInput);
                    int dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
                    if (Repeat.TODAY.equals(task.getRepeats())) {
                        taskList.add(task);
                        if (task.hasDate()) {
                            notificationSchedule.scheduleNotification(task);
                        }
                    } else if (((dayOfWeek != 1 && task.getRepeats().equals(Repeat.values()[dayOfWeek])) ||
                            (dayOfWeek == 1 && task.getRepeats().equals(Repeat.SUNDAY))) ||
                            task.getRepeats().equals(Repeat.DAILY)) {
                        taskList.add(task);
                        if (task.hasDate()) {
                            notificationSchedule.scheduleNotification(task);
                        }
                    }
                    Collections.sort(taskList, new TaskComparator());
                    taskAdapter.notifyDataSetChanged();
                    saveTaskToStorage(task);

                    //Save new task to internal storage and cancel dialog
                    storageReaderWriter.write(Repeat.TODAY.toString() + ".json", taskList);
                    inputDialog.cancel();
                } else {
                    inputDialogEditTaskName.requestFocus();
                    inputDialogEditTaskName.setError("This field cannot be blank");
                }
            }
        });
    }

    private void saveTaskToStorage(Task task) {
        Repeat repeat = task.getRepeats();
        if (!Repeat.TODAY.equals(repeat)) {
            ArrayList<Task> tasks = new StorageReaderWriter(activity).read(repeat.toString() + ".json");
            tasks.add(task);
            new StorageReaderWriter(activity).write(repeat.toString() + ".json", tasks);
        }
    }
}