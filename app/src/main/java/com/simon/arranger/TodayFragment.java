package com.simon.arranger;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageButton;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.simon.arranger.listview_adapters.TaskAdapter;
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
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class TodayFragment extends Fragment {
    private AppCompatActivity activity;
    private static final String JSON_FILE = "tasks_today.json";
    private List<Task> taskList = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.today_fragment, container, false);

        //Temporary here
        taskList = readFromInternalStorage();

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 2019);
        cal.set(Calendar.MONTH, 7);
        cal.set(Calendar.DAY_OF_MONTH, 6);
        Date date = cal.getTime();

        //Handle the listview
        ArrayList<Task> tasks = new ArrayList<>();
        tasks.add(new Task("Pushups", date.toString()));
        ListView listView = view.findViewById(R.id.taskList);
        TaskAdapter taskAdapter = new TaskAdapter(tasks, activity);
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
        activity = (AppCompatActivity) context;
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
        final EditText inputDialogEditTaskTime = inputDialog.findViewById(R.id.inputTaskTime);
        AppCompatImageButton inputDialogImageButton = inputDialog.findViewById(R.id.inputImageButton);
        inputDialogImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String taskName = inputDialogEditTaskName.getText().toString();
                String taskTime = inputDialogEditTaskTime.getText().toString();
                writeToInternalStorage(taskName, taskTime);
                inputDialog.cancel();
            }
        });
    }

    private void writeToInternalStorage(String taskName, String taskTime) {
        //Get filepath and use it to create file
        String filePath = activity.getFilesDir() + "/" + JSON_FILE;
        File file = new File(filePath);

        //Create new Task
        Task task = new Task(taskName, taskTime);
        taskList.add(task);

        //Create JsonArray
        String jsonArray = new Gson().toJson(taskList);

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

    private List<Task> readFromInternalStorage() {
        Gson gson = new Gson();
        String jsonString = "";
        try {
            //Get filepath and use it to create file
            String filePath = activity.getFilesDir() + "/" + JSON_FILE;
            File file = new File(filePath);

            //Make InputStream with file in constructor
            InputStream inputStream = new FileInputStream(file);
            StringBuilder stringBuilder = new StringBuilder();

            //Check if inputStream is null
            //else make InputStreamReader to make BufferedReader and crate empty string
            if (inputStream != null) {
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
            }

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
