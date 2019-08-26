package com.arrangerapp.arranger.tools;

import android.content.Context;

import com.arrangerapp.arranger.objects.Task;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

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

public class StorageReaderWriter {
    private Context context;

    public StorageReaderWriter(Context context) {
        this.context = context;
    }

    /**
     * Given a file name string, saves an arrayList.
     * @param fileName The name of the file or location to save the arrayList.
     * @param arrayList The arrayList to save
     */
    public void write(String fileName, ArrayList<Task> arrayList) {
        // Get filepath and use it to create file
        String filePath = context.getFilesDir() + "/" + fileName;
        File file = new File(filePath);

        // Create JsonArray
        String jsonArray = new Gson().toJson(arrayList);

        // Create FileOutputStream with jsonFile as part of constructor
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);

            // Convert JSON String to bytes and write() it
            fileOutputStream.write(jsonArray.getBytes());

            // Flush and close FileOutputStream
            fileOutputStream.flush();
            fileOutputStream.close();

        } catch (FileNotFoundException e) {
            System.out.println(e.toString());
        } catch (IOException e) {
            System.out.println(e.toString());
        }
    }

    /**
     * Given a file name string, returns a saved arrayList.
     * @param fileName The name of the saved arrayList.
     * @return ArrayList of today's tasks.
     */
    public ArrayList<Task> read(String fileName) {
        Gson gson = new Gson();
        String jsonString = "";
        try {
            // Get filepath and use it to create file
            String filePath = context.getFilesDir() + "/" + fileName;
            File file = new File(filePath);

            // Make InputStream with file in constructor
            InputStream inputStream = new FileInputStream(file);
            StringBuilder stringBuilder = new StringBuilder();

            // Check if inputStream is null
            // else make InputStreamReader to make BufferedReader and create empty string
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String recieveString = "";

            // Use while loop to append the lines from teh BufferedReader
            while ((recieveString = bufferedReader.readLine()) != null) {
                stringBuilder.append(recieveString);
            }

            // Close InputStream and save stringBuilder as string
            inputStream.close();
            jsonString = stringBuilder.toString();

            // Convert saved JsonArray of tasks into a list of tasks and return it
            Type listType = new TypeToken<List<Task>>(){}.getType();
            return gson.fromJson(jsonString, listType);

        } catch (IOException e) {
            return new ArrayList<Task>();
        }
    }

}
