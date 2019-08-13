package com.simon.arranger.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatImageButton;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;

import com.simon.arranger.activity.MainActivity;
import com.simon.arranger.R;
import com.simon.arranger.enums.Repeat;
import com.simon.arranger.listview_adapters.TaskAdapter;
import com.simon.arranger.objects.Task;
import java.util.ArrayList;

public class TodayFragment extends Fragment {
    private MainActivity activity;
    private ArrayList<Task> taskList;
    private TaskAdapter taskAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.today_fragment, container, false);

        //Set title of toolbar
        activity.setTitle("Today");

        //Read tasks from memory and assign them to taskList
        taskList = activity.readFromInternalStorage(Repeat.TODAY.toString() + ".json");

        //Set up ListView
        ListView listView = view.findViewById(R.id.taskList);
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
                    //TODO Maybe move this down after notifyDataSetChanged if too slow
                    scheduleTask(task);
                    if (Repeat.TODAY.equals(task.getRepeats())) {
                        taskList.add(task);
                    }
                    taskAdapter.notifyDataSetChanged();

                    //Save new task to internal storage and cancel dialog
                    activity.writeToInternalStorage(Repeat.TODAY.toString() + ".json", taskList);
                    inputDialog.cancel();
                } else {
                    //TODO Dialog closes on emulator but not on phone, check on more emulators
                    inputDialogEditTaskName.requestFocus();
                    inputDialogEditTaskName.setError("This field cannot be blank");
                }
            }
        });
    }

    private void scheduleTask(Task task) {
        Repeat repeat = task.getRepeats();
        if (!Repeat.TODAY.equals(repeat)) {
            ArrayList<Task> tasks = activity.readFromInternalStorage(repeat.toString() + ".json");
            tasks.add(task);
            activity.writeToInternalStorage(repeat.toString() + ".json", tasks);
        }
    }

    private void removeTaskFromSchedule(Task task) {
        Repeat repeat = task.getRepeats();
        if (!Repeat.TODAY.equals(repeat)) {
            ArrayList<Task> tasks = activity.readFromInternalStorage(repeat.toString() + ".json");
            tasks.remove(task);
            activity.writeToInternalStorage(repeat.toString() + ".json", tasks);
        }
    }
}
