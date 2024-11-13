package com.sundar.devtech.WeeklyReport;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.sundar.devtech.Masters.ReportActivity;

public class EmailBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Start the ReportActivity to send email in background
        Intent reportIntent = new Intent(context, ReportActivity.class);
        reportIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(reportIntent);
    }
}

