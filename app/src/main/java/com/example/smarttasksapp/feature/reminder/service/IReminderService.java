package com.example.smarttasksapp.feature.reminder.service;

import com.example.smarttasksapp.feature.reminder.domain.ReminderConfig;

public interface IReminderService {
    /**
     * 设置提醒
     * @param config ReminderConfig 包含任务ID、时间和其他配置
     */
    void setReminder(ReminderConfig config);

    /**
     * 取消提醒
     * @param taskId 任务ID
     */
    void cancelReminder(int taskId);
}
