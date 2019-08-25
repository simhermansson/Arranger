package com.arrangerapp.arranger.objects;

import java.util.Calendar;
import java.util.Comparator;

public class TaskComparator implements Comparator<Task> {
    @Override
    public int compare(Task o1, Task o2) {
        if (o1.getDate() != null && o2.getDate() != null) {
            Calendar c1 = Calendar.getInstance();
            c1.setTime(o1.getDate());
            Calendar c2 = Calendar.getInstance();
            c2.setTime(o2.getDate());
            return c1.get(Calendar.HOUR_OF_DAY) - c2.get(Calendar.HOUR_OF_DAY);
        } else if (o1.getDate() != null) {
            Calendar c1 = Calendar.getInstance();
            c1.setTime(o1.getDate());
            return c1.get(Calendar.HOUR_OF_DAY);
        } else if (o2.getDate() != null) {
            Calendar c2 = Calendar.getInstance();
            c2.setTime(o2.getDate());
            return 0 - c2.get(Calendar.HOUR_OF_DAY);
        } else {
            return 0;
        }
    }
}
