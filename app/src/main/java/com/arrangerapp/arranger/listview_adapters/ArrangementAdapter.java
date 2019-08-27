package com.arrangerapp.arranger.listview_adapters;

import android.content.Context;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageButton;

import com.arrangerapp.arranger.R;
import com.arrangerapp.arranger.activities.MainActivity;
import com.arrangerapp.arranger.enums.Repeat;
import com.arrangerapp.arranger.objects.Arrangement;
import com.arrangerapp.arranger.objects.Task;
import com.arrangerapp.arranger.tools.NotificationSchedule;
import com.arrangerapp.arranger.tools.StorageReaderWriter;

import java.util.ArrayList;

public class ArrangementAdapter extends ArrayAdapter<Arrangement> {
    private Context context;
    private ArrayList<Arrangement> arrangements;
    private MainActivity mainActivity;
    private StorageReaderWriter storageReaderWriter;
    private NotificationSchedule notificationSchedule;

    public ArrangementAdapter(ArrayList<Arrangement> arrangements, Context context) {
        super(context, R.layout.task_view, arrangements);
        this.context = context;
        this.arrangements = arrangements;
        mainActivity = (MainActivity) context;
        storageReaderWriter = new StorageReaderWriter(mainActivity);
        notificationSchedule = new NotificationSchedule(mainActivity);
    }

    @Override
    public Arrangement getItem(int position) {
        return arrangements.get(position);
    }

    @Override
    public int getCount() {
        return arrangements.size();
    }

    static class ViewHolderItem {
        TextView arrangementName;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        final ViewHolderItem viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.arrangement_view, parent, false);

            viewHolder = new ViewHolderItem();
            viewHolder.arrangementName = (TextView) convertView.findViewById(R.id.arrangementName);
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolderItem) convertView.getTag();
        }

        final Arrangement arrangement = getItem(position);
        if (arrangement != null) {
            viewHolder.arrangementName.setText(arrangement.getName());
        }

        return convertView;
    }
}
