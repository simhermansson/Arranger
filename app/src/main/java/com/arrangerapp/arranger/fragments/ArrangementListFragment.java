package com.arrangerapp.arranger.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
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


public class ArrangementListFragment extends Fragment {
    private MainActivity activity;
    private ArrayList<Arrangement> arrangementsList;
    private ArrangementAdapter arrangementAdapter;
    private StorageReaderWriter storageReaderWriter;
    private MenuItem toolbarEdit;
    private MenuItem toolbarRemove;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.arrangement_list_fragment, container, false);

        // Set title of toolbar
        activity.setTitle("Arrangements");

        // Read arrangements from storage and assign them to arrangementsList
        arrangementsList = storageReaderWriter.readArrangementList("arrangements.json");

        // Set up arrangements list view and its adapter.
        ListView listView = view.findViewById(R.id.arrangementsList);
        listView.setEmptyView(view.findViewById(R.id.arrangements_empty));
        arrangementAdapter = new ArrangementAdapter(arrangementsList, activity);
        listView.setAdapter(arrangementAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                activity.openArrangement(arrangementsList.get(position));
            }
        });

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
        toolbarRemove = menu.findItem(R.id.remove_arrangements);
        toolbarRemove.setVisible(false);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.edit_arrangements) {
            if (item.isChecked()) {
                item.setChecked(false);
                // Remove checkboxes from arrangements
                arrangementAdapter.setCheckBoxes(false);
                arrangementAdapter.notifyDataSetChanged();
                toolbarRemove.setVisible(false);
            } else {
                item.setChecked(true);
                // Add checkboxes to arrangements
                arrangementAdapter.setCheckBoxes(true);
                arrangementAdapter.notifyDataSetChanged();
                toolbarRemove.setVisible(true);
            }
            return true;
        } else if (item.getItemId() == R.id.remove_arrangements) {
            // Remove checked items
            arrangementAdapter.removeCheckedItems();
        }
        return false;
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
        final EditText inputDialogEditArrangement = inputDialog.findViewById(R.id.inputText);
        inputDialogEditArrangement.setHint(getString(R.string.arrangement_hint));
        inputDialogEditArrangement.requestFocus();
        AppCompatImageButton inputDialogImageButton = inputDialog.findViewById(R.id.inputImageButton);
        inputDialogImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get input from EditText field
                String name = inputDialogEditArrangement.getText().toString();
                // Check that input field is not empty
                if (name.length() > 0) {
                    arrangementsList.add(new Arrangement(name));
                    arrangementAdapter.notifyDataSetChanged();
                    storageReaderWriter.writeList("arrangements.json", arrangementsList);
                    inputDialog.cancel();
                } else {
                    inputDialogEditArrangement.requestFocus();
                    inputDialogEditArrangement.setError("This field cannot be blank");
                }
            }
        });
    }
}