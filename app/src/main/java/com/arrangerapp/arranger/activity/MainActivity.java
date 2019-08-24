package com.arrangerapp.arranger.activity;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.arrangerapp.arranger.broadcast_recievers.JobReciever;
import com.arrangerapp.arranger.broadcast_recievers.NotificationPublisher;
import com.arrangerapp.arranger.enums.Repeat;
import com.arrangerapp.arranger.objects.TaskComparator;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.arrangerapp.arranger.R;
import com.arrangerapp.arranger.fragments.TodayFragment;
import com.arrangerapp.arranger.fragments.WeekFragment;
import com.arrangerapp.arranger.objects.Task;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private enum State {
        TODAY, WEEK
    }
    private State currentState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Set initial state
        currentState = State.TODAY;

        //Create notification channel
        createNotificationChannel();

        //Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
        getSupportActionBar().setHomeButtonEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DrawerLayout drawerLayout = findViewById(R.id.drawer);
                drawerLayout.openDrawer(GravityCompat.START);
                TextView headerText = (TextView) drawerLayout.findViewById(R.id.drawerHeaderText);
                headerText.setText(R.string.app_name);
            }
        });

        //Set first fragment in FrameLayout to TodayFragment
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.placeholder, new TodayFragment());
        ft.commit();

        //Set first menu item as checked, this case the today item
        final NavigationView navigationView = findViewById(R.id.navigation);
        navigationView.getMenu().getItem(0).setChecked(true);

        //Set click listeners for menu items in drawer view
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                        //Initialize FragmentTransaction for changing of fragments
                        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

                        //Check the pressed menu item
                        //And initialize and switch to that fragment
                        menuItem.setChecked(true);
                        switch(menuItem.getItemId()) {
                            case R.id.nav_day:
                                if (!State.TODAY.equals(currentState)) {
                                    ft.replace(R.id.placeholder, new TodayFragment());
                                    ft.commit();
                                    currentState = State.TODAY;
                                }
                                break;
                            case R.id.nav_week:
                                if (!State.WEEK.equals(currentState)) {
                                    ft.replace(R.id.placeholder, new WeekFragment());
                                    ft.commit();
                                    currentState = State.WEEK;
                                }
                                break;
                        }

                        //Close drawer
                        DrawerLayout drawerLayout = findViewById(R.id.drawer);
                        drawerLayout.closeDrawer(GravityCompat.START);

                        return true;
                    }
                }
        );

        //Update notifications at midnight
        registerReceiver(broadcastReceiver, new IntentFilter("SCHEDULE_NOTIFICATIONS"));
        createAlarm();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    private void createNotificationChannel() {
        //Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(ALARM_SERVICE, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void writeToInternalStorage(String fileName, ArrayList<Task> arrayList) {
        //Get filepath and use it to create file
        String filePath = getFilesDir() + "/" + fileName;
        File file = new File(filePath);

        //Create JsonArray
        String jsonArray = new Gson().toJson(arrayList);

        //Create FileOutputStream with jsonFile as part of constructor
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);

            //Convert JSON String to bytes and write() it
            fileOutputStream.write(jsonArray.getBytes());

            //Flush and close FileOutputStream
            fileOutputStream.flush();
            fileOutputStream.close();

        } catch (FileNotFoundException e) {
            System.out.println(e.toString());
        } catch (IOException e) {
            System.out.println(e.toString());
        }
    }

    public ArrayList<Task> readFromInternalStorage(String fileName) {
        Gson gson = new Gson();
        String jsonString = "";
        try {
            //Get filepath and use it to create file
            String filePath = getFilesDir() + "/" + fileName;
            File file = new File(filePath);

            //Make InputStream with file in constructor
            InputStream inputStream = new FileInputStream(file);
            StringBuilder stringBuilder = new StringBuilder();

            //Check if inputStream is null
            //else make InputStreamReader to make BufferedReader and create empty string
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String recieveString = "";

            //Use while loop to append the lines from teh BufferedReader
            while ((recieveString = bufferedReader.readLine()) != null) {
                stringBuilder.append(recieveString);
            }

            //Close InputStream and save stringBuilder as string
            inputStream.close();
            jsonString = stringBuilder.toString();

            //Convert saved JsonArray of tasks into a list of tasks and return it
            Type listType = new TypeToken<List<Task>>(){}.getType();
            ArrayList<Task> storageList = gson.fromJson(jsonString, listType);
            return storageList;

        } catch (IOException e) {
            return new ArrayList<Task>();
        }
    }

    public ArrayList<Task> getAndScheduleTasks() {
        //Read tasks from storage and assign them to taskList
        ArrayList<Task> taskList = readFromInternalStorage(Repeat.TODAY.toString() + ".json");

        //Add scheduled tasks to that taskList if not already done so today
        int dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK); //Get current day
        String day = Repeat.values()[dayOfWeek].toString();
        SharedPreferences sb = getSharedPreferences("imported_tasks", 0); // 0 Default Private
        if (!sb.getBoolean(day, false)) {
            String previousDay;
            if (dayOfWeek == 2) {
                previousDay = Repeat.values()[8].toString();
            } else {
                previousDay = Repeat.values()[dayOfWeek - 1].toString();
            }

            //Daily tasks
            for (Task task : readFromInternalStorage(Repeat.DAILY.toString() + ".json")) {
                taskList.add(task);
                if (task.hasDate()) {
                    scheduleNotification(task);
                }
            }

            //Scheduled tasks
            for (Task task : readFromInternalStorage(day + ".json")) {
                taskList.add(task);
                if (task.hasDate()) {
                    scheduleNotification(task);
                }
            }

            //Write new tasks for today to storage
            Collections.sort(taskList, new TaskComparator());
            writeToInternalStorage(Repeat.TODAY.toString() + ".json", taskList);

            //Edit
            SharedPreferences.Editor editor = sb.edit();
            editor.putBoolean(day, true);
            editor.putBoolean(previousDay, false);
            editor.commit();
        }

        return taskList;
    }

    public void scheduleNotification(Task task) {
        Notification notification = getNotification(task);
        long taskTimeInMillis = getNotificationDelay(task);

        Intent notificationIntent = new Intent(this, NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, NotificationPublisher.NOTIFICATION_INT_ID);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, task.getId(), notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, taskTimeInMillis, pendingIntent);
    }

    public long getNotificationDelay(Task task) {
        // Get delay in milliseconds
        Calendar taskCalendar = Calendar.getInstance();
        taskCalendar.set(Calendar.HOUR_OF_DAY, task.getDate().getHours());
        taskCalendar.set(Calendar.MINUTE, task.getDate().getMinutes());
        taskCalendar.set(Calendar.SECOND, 0);
        return taskCalendar.getTimeInMillis();
    }

    public Notification getNotification(Task task) {
        // Create and explicit intent for an Activity in app
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, NotificationPublisher.NOTIFICATION_INT_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        //Creating a notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, ALARM_SERVICE)
                .setSmallIcon(R.drawable.ic_notifications_grey_24dp)
                .setContentTitle(task.getName())
                .setContentText(task.getTime())
                .setAutoCancel(true)
                .setPriority(NotificationCompat.DEFAULT_ALL)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setVibrate(new long[] {1000, 1000, 1000})
                .setLights(Color.GREEN, 1000, 500)
                .setContentIntent(pendingIntent);

        return builder.build();
    }

    public void cancelScheduledNotification(int id) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent notificationIntent = new Intent(this, NotificationPublisher.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, id, notificationIntent, 0);
        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            getAndScheduleTasks();
            createAlarm();
        }
    };

    public void createAlarm() {
        //System request code
        int DATA_FETCHER_RC = 123;
        //Create Alarm Manager
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        //Create time of day of alarm
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);

        //Create an intent that points to the reciever.
        //The system will notify the app about the current time, and send a broadcast to the app
        Intent intent = new Intent(this, JobReciever.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, DATA_FETCHER_RC, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        //Set alarm
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
    }
}
