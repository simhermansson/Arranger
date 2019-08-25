package com.arrangerapp.arranger.activities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import androidx.annotation.NonNull;

import com.arrangerapp.arranger.R;
import com.google.android.material.navigation.NavigationView;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.arrangerapp.arranger.fragments.TodayFragment;
import com.arrangerapp.arranger.fragments.WeekFragment;

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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
}
