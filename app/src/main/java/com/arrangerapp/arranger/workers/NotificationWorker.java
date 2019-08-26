package com.arrangerapp.arranger.workers;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.arrangerapp.arranger.R;
import com.arrangerapp.arranger.activities.MainActivity;

import static android.content.Context.ALARM_SERVICE;

public class NotificationWorker extends Worker {

    private Context context;
    private WorkerParameters params;

    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        this.context = context;
        this.params = params;
        createNotificationChannel();
    }

    @NonNull
    @Override
    public Result doWork() {
        // Get input
        String name = getInputData().getString("Name");
        String time = getInputData().getString("Time");
        int id = getInputData().getInt("Id", 2);

        // Build and get notification with input
        Notification notification =  getNotification(name, time, id);

        // Get the notificationManager
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Send out notification
        notificationManager.notify(id, notification);

        // Indicate whether the task finished successfully
        return Result.success();
    }

    /**
     * Given strings of name and time and int of id, a notification is created and returned.
     * @param name Task name.
     * @param time Task time.
     * @param id Task unique id.
     * @return Notification object for that task.
     */
    private Notification getNotification(String name, String time, int id) {
        // Create and explicit intent for an Activity in app
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Creating a notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, ALARM_SERVICE)
                .setSmallIcon(R.drawable.ic_notifications_grey_24dp)
                .setContentTitle(name)
                .setContentText(time)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.DEFAULT_ALL)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setVibrate(new long[] {1000, 1000, 1000})
                .setLights(Color.GREEN, 1000, 500)
                .setContentIntent(pendingIntent);

        return builder.build();
    }

    /**
     * Creates a notification channel if Android version is Oreo or higher.
     */
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.channel_name);
            String description = context.getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(ALARM_SERVICE, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
