package com.arrangerapp.arranger.tools;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.arrangerapp.arranger.enums.Repeat;
import com.arrangerapp.arranger.objects.Task;
import com.arrangerapp.arranger.objects.TaskComparator;
import com.arrangerapp.arranger.workers.TaskRescheduleWorker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class DailyTaskReschedule {
    private static final String WORK_NAME = "DailyTaskReschedule";

    private Context context;
    private StorageReaderWriter storageReaderWriter;
    private NotificationSchedule notificationSchedule;

    public DailyTaskReschedule(Context context) {
        this.context = context;
        storageReaderWriter = new StorageReaderWriter(context);
        notificationSchedule = new NotificationSchedule(context);
    }

    /**
     * Schedules work at midnight so that TaskRescheduleWorker can call method getAndScheduleTasks().
     */
    public void scheduleNextWork() {
        // Create Calendar for time when work should be executed
        Calendar current = Calendar.getInstance();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        // Create a OneTimeWorkRequest object and enqueue it
        OneTimeWorkRequest enqueueNotification = new OneTimeWorkRequest.Builder(TaskRescheduleWorker.class)
                .setInitialDelay(calendar.getTimeInMillis() - current.getTimeInMillis(), TimeUnit.MILLISECONDS)
                .build();

        // Enqueue the OneTimeWorkRequest
        WorkManager.getInstance(context).enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, enqueueNotification);
    }

    /**
     * Imports and schedules all tasks for today and returns a list of today's tasks.
     * @return An arrayList of today's tasks.
     */
    public ArrayList<Task> getAndScheduleTasks() {
        // Read tasks from storage and assign them to taskList
        ArrayList<Task> taskList = storageReaderWriter.readTaskList(Repeat.TODAY.toString() + ".json");

        // Get current day in int and String form
        int dayOfWeekInt = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1; //Get current day indexed for Repeat
        String day = Repeat.values()[dayOfWeekInt].toString();

        // Create SharedPreferences object and check if tasks scheduled for today has been imported
        SharedPreferences sb = context.getSharedPreferences("imported_tasks", 0);
        // If scheduled tasks has not been imported today
        if (!sb.getBoolean(day, false)) {
            // Import and schedule notifications for daily tasks
            for (Task task : storageReaderWriter.readTaskList(Repeat.DAILY.toString() + ".json")) {
                taskList.add(task);
                if (task.hasDate()) {
                    notificationSchedule.scheduleNotification(task);
                }
            }

            // Import and schedule notifications for scheduled tasks
            for (Task task : storageReaderWriter.readTaskList(day + ".json")) {
                taskList.add(task);
                if (task.hasDate()) {
                    notificationSchedule.scheduleNotification(task);
                }
            }

            // Write new tasks for today to storage
            Collections.sort(taskList, new TaskComparator());
            storageReaderWriter.writeList(Repeat.TODAY.toString() + ".json", taskList);

            // Get String for previous day
            String previousDay;
            if (dayOfWeekInt == 0) {
                previousDay = Repeat.values()[6].toString();
            } else {
                previousDay = Repeat.values()[dayOfWeekInt-1].toString();
            }

            // Edit sp such that tasks imported today is true and previous day false
            SharedPreferences.Editor editor = sb.edit();
            editor.putBoolean(day, true);
            editor.putBoolean(previousDay, false);
            editor.apply();

            // Start midnight update loop
            scheduleNextWork();
        }

        return taskList;
    }
}
