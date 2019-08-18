package com.arrangerapp.arranger.fragments;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.AppCompatImageButton;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;

import com.arrangerapp.arranger.activity.MainActivity;
import com.arrangerapp.arranger.R;
import com.arrangerapp.arranger.enums.Repeat;
import com.arrangerapp.arranger.listview_adapters.TaskAdapter;
import com.arrangerapp.arranger.broadcast_recievers.NotificationPublisher;
import com.arrangerapp.arranger.objects.Task;

import java.util.ArrayList;
import java.util.Calendar;

import static android.content.Context.ALARM_SERVICE;

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

        //Read tasks from storage and assign them to taskList
        taskList = activity.readFromInternalStorage(Repeat.TODAY.toString() + ".json");

        //Add scheduled tasks to that taskList if not already done so today
        int dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK); //Get current day
        String day = Repeat.values()[dayOfWeek].toString();
        SharedPreferences sb = activity.getSharedPreferences("imported_tasks", 0); // 0 Default Private
        if (!sb.getBoolean(day, false)) {
            String previousDay;
            if (dayOfWeek == 2) {
                previousDay = Repeat.values()[8].toString();
            } else {
                previousDay = Repeat.values()[dayOfWeek-1].toString();
            }

            //Daily tasks
            for (Task task :activity.readFromInternalStorage(Repeat.DAILY.toString() + ".json")) {
                taskList.add(task);
                if (task.hasDate()) {
                    scheduleNotification(getNotification(task), getNotificationDelay(task));
                }
            }

            //Scheduled tasks
            for (Task task : activity.readFromInternalStorage(day + ".json")) {
                taskList.add(task);
                if (task.hasDate()) {
                    scheduleNotification(getNotification(task), getNotificationDelay(task));
                }
            }

            //Write new tasks for today to storage
            activity.writeToInternalStorage(Repeat.TODAY.toString() + ".json", taskList);

            //Edit
            SharedPreferences.Editor editor = sb.edit();
            editor.putBoolean(day, true);
            editor.putBoolean(previousDay, false);
            editor.commit();
        }

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
                    int dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
                    if (Repeat.TODAY.equals(task.getRepeats())) {
                        taskList.add(task);
                    } else if (task.getRepeats().equals(Repeat.values()[dayOfWeek]) ||
                            task.getRepeats().equals(Repeat.DAILY)) {
                        taskList.add(task);
                    }
                    taskAdapter.notifyDataSetChanged();
                    scheduleTask(task);
                    if (task.hasDate()) {
                        scheduleNotification(getNotification(task), getNotificationDelay(task));
                    }

                    //Save new task to internal storage and cancel dialog
                    activity.writeToInternalStorage(Repeat.TODAY.toString() + ".json", taskList);
                    inputDialog.cancel();
                } else {
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

    private void scheduleNotification(Notification notification, long delay) {
        Intent notificationIntent = new Intent(activity, NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, 1);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(activity, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        long futureInMillis = SystemClock.elapsedRealtime() + delay;
        AlarmManager alarmManager = (AlarmManager) activity.getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);
    }

    private long getNotificationDelay(Task task) {
        // Get delay in milliseconds
        Calendar todayCalendar = Calendar.getInstance();
        Calendar taskCalendar = Calendar.getInstance();
        taskCalendar.set(Calendar.HOUR_OF_DAY, task.getDate().getHours());
        taskCalendar.set(Calendar.MINUTE, task.getDate().getMinutes());
        taskCalendar.set(Calendar.SECOND, 0);
        return taskCalendar.getTimeInMillis() - todayCalendar.getTimeInMillis();
    }

    private Notification getNotification(Task task) {
        // Create and explicit intent for an Activity in app
        Intent intent = new Intent(activity, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(activity, 0, intent, 0);

        //Creating a notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(activity, ALARM_SERVICE)
                .setSmallIcon(R.drawable.ic_notifications_grey_24dp)
                .setContentTitle(task.getName())
                .setContentText(task.getTime())
                .setPriority(NotificationCompat.DEFAULT_ALL)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        return builder.build();
    }
}