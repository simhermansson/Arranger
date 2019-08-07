package com.simon.arranger;

import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.simon.arranger.objects.Task;
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
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.placeholder, new TodayFragment());
        ft.commit();
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
            //else make InputStreamReader to make BufferedReader and crate empty string
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

        } catch (FileNotFoundException e) {
            System.out.println(e.toString());
        } catch (IOException e) {
            System.out.println(e.toString());
        }

        //Convert saved JsonArray of tasks into a list of tasks and return it
        Type listType = new TypeToken<List<Task>>(){}.getType();
        return gson.fromJson(jsonString, listType);
    }

}
