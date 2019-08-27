package com.arrangerapp.arranger.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.fragment.app.Fragment;

import com.arrangerapp.arranger.R;
import com.arrangerapp.arranger.activities.MainActivity;
import com.arrangerapp.arranger.listview_adapters.ArrangementAdapter;
import com.arrangerapp.arranger.objects.Arrangement;
import com.arrangerapp.arranger.tools.StorageReaderWriter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;


public class ArrangementsFragment extends Fragment {
    private MainActivity activity;
    private ArrayList<Arrangement> arrangementsList;
    private ArrangementAdapter arrangementAdapter;
    private StorageReaderWriter storageReaderWriter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.arrangements_fragment, container, false);

        // Set title of toolbar
        activity.setTitle("Arrangements");

        // Read arrangements from storage and assign them to arrangementsList
        arrangementsList = storageReaderWriter.readArrangementList("arrangements.json");

        // Set up ListView
        ListView listView = view.findViewById(R.id.arrangementsList);
        listView.setEmptyView(view.findViewById(R.id.arrangements_empty));
        arrangementAdapter = new ArrangementAdapter(arrangementsList, activity);
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

        // Open keyboard
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

        // Handle dialog inputs
        final EditText inputDialogEditTaskName = inputDialog.findViewById(R.id.inputTaskName);
        inputDialogEditTaskName.setHint(getString(R.string.arrangement_hint));
        inputDialogEditTaskName.requestFocus();
        AppCompatImageButton inputDialogImageButton = inputDialog.findViewById(R.id.inputImageButton);
        inputDialogImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get input from EditText field
                String name = inputDialogEditTaskName.getText().toString();
                // Check that input field is not empty
                if (name.length() > 0) {
                    arrangementsList.add(new Arrangement(name));
                    arrangementAdapter.notifyDataSetChanged();
                    storageReaderWriter.writeList("arrangements.json", arrangementsList);
                    inputDialog.cancel();
                } else {
                    inputDialogEditTaskName.requestFocus();
                    inputDialogEditTaskName.setError("This field cannot be blank");
                }
            }
        });
    }
}