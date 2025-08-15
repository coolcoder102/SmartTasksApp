package com.example.smarttasksapp.feature.reminder.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import com.example.smarttasksapp.feature.reminder.domain.ReminderConfig;
import com.example.smarttasksapp.feature.reminder.service.reciver.AlarmReceiver;

public class ReminderManager {
    private static volatile ReminderManager instance;
    private final AlarmManager alarmManager;
    private final Context context;

    private ReminderManager(Context context) {
        this.context = context.getApplicationContext();
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    public static ReminderManager getInstance(Context context) {
        if (instance == null ){
            synchronized(ReminderManager.class){
                if (instance == null) {
                    instance = new ReminderManager(context);
                }
            }
        }
        return instance;
    }

    /**
     * 设置闹钟提醒
     */
    public void setAlarm(ReminderConfig config) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("taskTitle", config.getTaskTitle());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                config.getTaskId(),
                intent,
                PendingIntent.FLAG_MUTABLE
        );

        if (config.isRepeat()) {
            // 设置重复提醒
            alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    config.getTriggerTime(),
                    AlarmManager.INTERVAL_DAY,  // 每天重复
                    pendingIntent
            );
        } else {

            // 设置单次提醒
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()){
                requestExactAlarmPermission(context);
                // 直接返回，等待用户授权后再尝试设置闹钟
                return;
            }

            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    config.getTriggerTime(),
                    pendingIntent
            );

        }
    }

    /**
     * 取消闹钟提醒
     */
    public void cancelAlarm(int taskId) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                taskId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        alarmManager.cancel(pendingIntent);
    }

    private void requestExactAlarmPermission(Context context) {
        Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
        intent.setData(Uri.parse("package:" + context.getPackageName()));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // 添加 FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent);
    }
}
