package com.example.smarttasksapp.feature.reminder.domain;

public class ReminderConfig {
    private final int taskId;           // 唯一任务ID
    private final String taskTitle;      // 任务名称
    private final long triggerTime;     // 提醒时间（时间戳）
    private final boolean repeat;       // 是否重复提醒

    public ReminderConfig(int taskId, String taskTitle, long triggerTime, boolean repeat) {
        this.taskId = taskId;
        this.taskTitle = taskTitle;
        this.triggerTime = triggerTime;
        this.repeat = repeat;
    }

    public int getTaskId() {
        return taskId;
    }

    public String getTaskTitle() {
        return taskTitle;
    }

    public long getTriggerTime() {
        return triggerTime;
    }

    public boolean isRepeat() {
        return repeat;
    }
}
