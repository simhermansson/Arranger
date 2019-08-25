package com.arrangerapp.arranger.workers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.arrangerapp.arranger.tools.DailyTaskReschedule;

public class TaskReschedulerWorker extends Worker {

    private Context context;
    private WorkerParameters params;

    public TaskReschedulerWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        this.context = context;
        this.params = params;
    }

    @NonNull
    @Override
    public Result doWork() {
        // Do the work here
        DailyTaskReschedule dailyTaskReschedule = new DailyTaskReschedule(context);
        dailyTaskReschedule.getAndScheduleTasks();
        dailyTaskReschedule.scheduleNextWork();

        // Indicate whether the task finished successfully
        return Result.success();
    }
}
