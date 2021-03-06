package com.arrangerapp.arranger.tools;

import android.content.Context;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.arrangerapp.arranger.enums.Repeat;
import com.arrangerapp.arranger.objects.Task;
import com.arrangerapp.arranger.workers.NotificationWorker;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class NotificationSchedule {

    private Context context;

    public NotificationSchedule(Context context) {
        this.context = context;
    }

    /**
     * Schedules a notification for a task using OneTimeWorkRequest
     * provided by the androidx WorkManager library
     * @param task A task.
     */
    public void scheduleNotification(Task task) {
        // Create a Data object describing the task to send with the worker
        Data notificationData = new Data.Builder()
                .putString("Name", task.getName())
                .putString("Time", task.getTime())
                .putInt("Id", task.getId())
                .build();

        // Create a OneTimeWorkRequest object and enqueue it
        OneTimeWorkRequest enqueueNotification = new OneTimeWorkRequest.Builder(NotificationWorker.class)
                .setInputData(notificationData)
                .setInitialDelay(getNotificationDelay(task), TimeUnit.MILLISECONDS)
                .addTag(String.valueOf(task.getId()))
                .build();

        // Enqueue the OneTimeWorkRequest
        WorkManager.getInstance(context).enqueue(enqueueNotification);
    }

    /**
     * Returns the millisecond delay between now and a task date
     * @param task A task.
     * @return A long describing the millisecond delay.
     */
    private long getNotificationDelay(Task task) {
        // Get delay in milliseconds
        Calendar now = Calendar.getInstance();
        Calendar taskCalendar = Calendar.getInstance();
        taskCalendar.set(Calendar.HOUR_OF_DAY, task.getDate().getHours());
        taskCalendar.set(Calendar.MINUTE, task.getDate().getMinutes());
        taskCalendar.set(Calendar.SECOND, 0);
        return taskCalendar.getTimeInMillis() - now.getTimeInMillis();
    }

    /**
     * Cancels a scheduled task notification in the androidx WorkManager enqueue
     * @param id Integer unique id for task.
     */
    public void cancelScheduledNotification(int id) {
        // The task has a tag with its id as a tag, this cancels that task work
        WorkManager.getInstance(context).cancelAllWorkByTag(String.valueOf(id));
    }

    public void toSchedule(Task task) {
        // Get current day of week with correct Repeat indexing.
        int dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1;

        // Booleans for checking if task is scheduled for today or is a daily task.
        boolean oneTimeTask = task.getRepeats().equals(Repeat.TODAY);
        boolean scheduledForToday = task.getRepeats().equals(Repeat.values()[dayOfWeek]);
        boolean scheduledDaily = task.getRepeats().equals(Repeat.DAILY);

        if ((oneTimeTask && task.hasDate()) || scheduledForToday || scheduledDaily) {
            // Set up calendar for current time and task time.
            Calendar now = Calendar.getInstance();
            Calendar taskCalendar = Calendar.getInstance();
            taskCalendar.set(Calendar.HOUR_OF_DAY, task.getDate().getHours());
            taskCalendar.set(Calendar.MINUTE, task.getDate().getMinutes());
            taskCalendar.set(Calendar.SECOND, 0);

            // Only schedule if task is scheduled in the future.
            if (now.getTimeInMillis() < taskCalendar.getTimeInMillis()) {
                scheduleNotification(task);
            }
        }
    }
}
