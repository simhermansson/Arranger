package com.arrangerapp.arranger.tools;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.arrangerapp.arranger.enums.Repeat;
import com.arrangerapp.arranger.enums.WeekDays;
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

    public ArrayList<Task> getAndScheduleTasks() {
        //Read tasks from storage and assign them to taskList
        ArrayList<Task> taskList = storageReaderWriter.read(Repeat.TODAY.toString() + ".json");

        //Add scheduled tasks to that taskList if not already done so today
        int dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK); //Get current day
        String day = WeekDays.values()[dayOfWeek-1].toString();
        SharedPreferences sb = context.getSharedPreferences("imported_tasks", 0); // 0 Default Private
        if (!sb.getBoolean(day, false)) {
            String previousDay;
            if (dayOfWeek == 1) {
                previousDay = WeekDays.values()[6].toString();
            } else {
                previousDay = WeekDays.values()[dayOfWeek-2].toString();
            }

            //Daily tasks
            for (Task task : storageReaderWriter.read(Repeat.DAILY.toString() + ".json")) {
                taskList.add(task);
                if (task.hasDate()) {
                    notificationSchedule.scheduleNotification(task);
                }
            }

            //Scheduled tasks
            for (Task task : storageReaderWriter.read(day + ".json")) {
                taskList.add(task);
                if (task.hasDate()) {
                    notificationSchedule.scheduleNotification(task);
                }
            }

            //Write new tasks for today to storage
            Collections.sort(taskList, new TaskComparator());
            storageReaderWriter.write(Repeat.TODAY.toString() + ".json", taskList);

            //Edit
            SharedPreferences.Editor editor = sb.edit();
            editor.putBoolean(day, true);
            editor.putBoolean(previousDay, false);
            editor.commit();
        }

        return taskList;
    }
}
