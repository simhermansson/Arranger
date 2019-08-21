package com.arrangerapp.arranger.broadcast_recievers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class JobReciever extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        context.sendBroadcast(new Intent("SCHEDULE_NOTIFICATIONS"));
    }
}
