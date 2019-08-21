package com.arrangerapp.arranger.objects;

import java.util.Comparator;

public class TaskComparator implements Comparator<Task> {
    @Override
    public int compare(Task o1, Task o2) {
        if (o1.getDate() != null && o2.getDate() != null) {
            int o1_hours = o1.getDate().getHours();
            int o2_hours = o2.getDate().getHours();

            if (o1_hours == o2_hours) {
                return o1.getDate().getMinutes() - o2.getDate().getMinutes();
            } else {
                return o1_hours - o2_hours;
            }
        } else {
            return 0;
        }
    }
}
