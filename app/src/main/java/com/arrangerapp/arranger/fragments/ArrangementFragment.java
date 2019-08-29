package com.arrangerapp.arranger.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.fragment.app.Fragment;

import com.arrangerapp.arranger.R;
import com.arrangerapp.arranger.activities.MainActivity;
import com.arrangerapp.arranger.enums.Repeat;
import com.arrangerapp.arranger.listview_adapters.ArrangementAdapter;
import com.arrangerapp.arranger.objects.Arrangement;
import com.arrangerapp.arranger.objects.Task;
import com.arrangerapp.arranger.objects.TaskComparator;
import com.arrangerapp.arranger.tools.StorageReaderWriter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;

public class ArrangementFragment extends Fragment {
    private MainActivity activity;
    private Arrangement arrangement;
    private ArrayList<Task> tasks;
    private ArrangementAdapter arrangementAdapter;
    private StorageReaderWriter storageReaderWriter;
    private EditText notes;
    private MenuItem toolbarMoveToToday;
    private MenuItem toolbarNotes;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        if (getArguments() != null) {
            arrangement = getArguments().getParcelable("Arrangement");
            tasks = arrangement.getTasks();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.arrangement_fragment, container, false);

        // Set title of toolbar.
        activity.setTitle(arrangement.getName());

        // Set up notes if enabled.
        notes = view.findViewById(R.id.notes);
        notes.setImeOptions(EditorInfo.IME_ACTION_DONE);
        notes.setRawInputType(InputType.TYPE_CLASS_TEXT);
        if (arrangement.hasNotes()) {
            notes.setText(arrangement.getNotes());
        }

        // Listening for done press on keyboard.
        notes.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // Hide keyboard and clear focus from notes.
                    InputMethodManager imm = (InputMethodManager) notes.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(notes.getWindowToken(), 0);
                    notes.clearFocus();

                    // Save notes to arrangement and to storage.
                    saveArrangementToStorage();
                }
                return true;
            }
        });

        // Hide notes by default.
        if (!arrangement.hasNotesEnabled()) {
            notes.setVisibility(View.GONE);
        }

        // Set up ListView.
        ListView listView = view.findViewById(R.id.arrangementTaskList);
        listView.setEmptyView(view.findViewById(R.id.arrangement_empty));
        arrangementAdapter = new ArrangementAdapter(tasks, activity, arrangement);
        listView.setAdapter(arrangementAdapter);

        // Handle the floating action button and the popup input bar
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
        activity = (MainActivity) context;
        storageReaderWriter = new StorageReaderWriter(activity);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_menu, menu);
        MenuItem toolbarRemove = menu.findItem(R.id.remove_arrangements);
        MenuItem toolbarEdit = menu.findItem(R.id.edit_arrangements);
        toolbarNotes = menu.findItem(R.id.notes);
        toolbarRemove.setVisible(false);
        toolbarEdit.setVisible(false);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.move_tasks) {
            showMoveAlert();
            return true;
        } else if (item.getItemId() == R.id.show_notes) {
            if (arrangement.hasNotesEnabled()) {
                arrangement.setNotesEnabled(false);
                notes.setVisibility(View.GONE);
                saveArrangementToStorage();
            } else {
                arrangement.setNotesEnabled(true);
                notes.setVisibility(View.VISIBLE);
                saveArrangementToStorage();
            }
            return true;
        }
        return false;
    }

    public static ArrangementFragment newInstance(Arrangement arrangement) {
        ArrangementFragment arrangementFragment = new ArrangementFragment();
        Bundle args = new Bundle();
        args.putParcelable("Arrangement", arrangement);
        arrangementFragment.setArguments(args);
        return arrangementFragment;
    }

    /**
     * Opens an EditText dialog view above the keyboard.
     * @param floatingActionButton The fab that was pressed to open the dialog so that this
     *                             method can hide and show it when necessary.
     */
    private void openInputDialog(final FloatingActionButton floatingActionButton) {
        // Create dialog for EditText view
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
                // Close keyboard
                InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

                floatingActionButton.show();
            }
        });
        inputDialog.show();

        // Open keyboard.
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

        // Handle dialog inputs.
        final EditText inputDialogEditTaskName = inputDialog.findViewById(R.id.inputText);
        inputDialogEditTaskName.setHint(getString(R.string.arrangement_task_hint));
        inputDialogEditTaskName.requestFocus();
        AppCompatImageButton inputDialogImageButton = inputDialog.findViewById(R.id.inputImageButton);
        inputDialogImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get input from EditText field
                String taskInput = inputDialogEditTaskName.getText().toString();
                // Check that input field is not empty
                if (taskInput.length() > 0) {
                    createTask(taskInput);
                    inputDialog.cancel();
                } else {
                    inputDialogEditTaskName.requestFocus();
                    inputDialogEditTaskName.setError("This field cannot be blank");
                }
            }
        });
    }

    /**
     * Creates a task.
     * @param input Task name, time and date.
     */
    private void createTask(String input) {
        Task task = new Task(input);

        // Add task to task list.
        arrangement.addTask(task);

        // Sort the new task list and notify adapter.
        Collections.sort(tasks, new TaskComparator());
        arrangementAdapter.notifyDataSetChanged();

        // Save new task list in storage.
        ArrayList<Arrangement> arrangementsList = storageReaderWriter.readArrangementList("arrangements.json");
        arrangementsList.set(arrangement.getListIndex(), arrangement);
        storageReaderWriter.writeList("arrangements.json", arrangementsList);
    }

    private void showMoveAlert() {
        new AlertDialog.Builder(activity)
                .setTitle("Copy Arrangement")
                .setMessage("Copy arrangement tasks to today?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Move tasks to today list.
                        ArrayList<Task> todayList = storageReaderWriter.readTaskList(Repeat.TODAY.toString() + ".json");
                        todayList.addAll(tasks);
                        Collections.sort(todayList, new TaskComparator());
                        storageReaderWriter.writeList(Repeat.TODAY.toString() + ".json", todayList);
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void saveArrangementToStorage() {
        arrangement.addNotes(notes.getText().toString());
        ArrayList<Arrangement> arrangementsList = storageReaderWriter.readArrangementList("arrangements.json");
        arrangementsList.set(arrangement.getListIndex(), arrangement);
        storageReaderWriter.writeList("arrangements.json", arrangementsList);
    }
}