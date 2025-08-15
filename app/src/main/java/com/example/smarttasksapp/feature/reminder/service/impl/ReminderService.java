package com.example.smarttasksapp.feature.reminder.service.impl;

import android.content.Context;

import com.example.smarttasksapp.feature.reminder.domain.ReminderConfig;
import com.example.smarttasksapp.feature.reminder.service.IReminderService;
import com.example.smarttasksapp.feature.reminder.service.ReminderManager;

public class ReminderService implements IReminderService {
    private final Context context;

    public ReminderService(Context context) {
        this.context = context;
    }
    @Override
    public void setReminder(ReminderConfig config) {
        ReminderManager.getInstance(context).setAlarm(config);
    }

    @Override
    public void cancelReminder(int taskId) {
        ReminderManager.getInstance(context).cancelAlarm(taskId);
    }
}
