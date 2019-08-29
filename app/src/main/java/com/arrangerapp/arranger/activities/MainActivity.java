package com.arrangerapp.arranger.activities;

import androidx.annotation.NonNull;

import com.arrangerapp.arranger.R;
import com.arrangerapp.arranger.fragments.ArrangementFragment;
import com.arrangerapp.arranger.fragments.ArrangementListFragment;
import com.arrangerapp.arranger.objects.Arrangement;
import com.arrangerapp.arranger.tools.DailyTaskReschedule;
import com.google.android.material.navigation.NavigationView;

import androidx.fragment.app.FragmentManager;
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
        TODAY, WEEK, ARRANGEMENTS, ARRANGEMENT
    }
    private State currentState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set initial state
        currentState = State.TODAY;

        // Set up toolbar
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

        // Set first fragment in FrameLayout to TodayFragment
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.placeholder, new TodayFragment());
        ft.commit();

        // Set first menu item as checked, this case the today item
        final NavigationView navigationView = findViewById(R.id.navigation);
        navigationView.getMenu().getItem(0).setChecked(true);

        // Set click listeners for menu items in drawer view
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                        // Initialize FragmentTransaction for changing of fragments
                        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

                        // Check the pressed menu item and initialize and switch to that fragment
                        menuItem.setChecked(true);
                        switch(menuItem.getItemId()) {
                            case R.id.nav_day:
                                if (!State.TODAY.equals(currentState)) {
                                    // Clear back stack.
                                    FragmentManager fm = getSupportFragmentManager();
                                    for (int i = 0; i < fm.getBackStackEntryCount(); i++) {
                                        fm.popBackStack();
                                    }

                                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                                    ft.replace(R.id.placeholder, new TodayFragment());
                                    ft.commit();
                                    currentState = State.TODAY;
                                }
                                break;
                            case R.id.nav_week:
                                if (!State.WEEK.equals(currentState)) {
                                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                                    ft.replace(R.id.placeholder, new WeekFragment());
                                    ft.addToBackStack(null);
                                    ft.commit();
                                    currentState = State.WEEK;
                                }
                                break;
                            case R.id.nav_arrangements:
                                if (!State.ARRANGEMENTS.equals(currentState)) {
                                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                                    ft.replace(R.id.placeholder, new ArrangementListFragment());
                                    ft.addToBackStack(null);
                                    ft.commit();
                                    currentState = State.ARRANGEMENTS;
                                }
                                break;
                        }

                        // Close drawer
                        DrawerLayout drawerLayout = findViewById(R.id.drawer);
                        drawerLayout.closeDrawer(GravityCompat.START);

                        return true;
                    }
                }
        );

        new DailyTaskReschedule(this).getAndScheduleTasks();
    }

    public void openArrangement(Arrangement arrangement) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.replace(R.id.placeholder, ArrangementFragment.newInstance(arrangement));
        ft.addToBackStack(null);
        ft.commit();
        currentState = State.ARRANGEMENT;
    }
}
