package com.example.smarttasksapp.feature.tasks.data;

import androidx.lifecycle.LiveData;

import com.example.smarttasksapp.infrastructure.entity.Task;

import java.util.List;

public interface ITaskRepository {
    LiveData<List<Task>> observeAll();
    void addTask(String title, String description, long startTime);
    void reorder(long fromTaskId, long toTaskId, boolean placeAbove);
    void persistOrder(List<Task> ordered);
    void updateTask(long taskId, String title, String description, long startTime);
    void updateTaskCompletedStatus(long taskId, boolean isCompleted);
    void updateTaskStartTime(long taskId, long startTime);
    void deleteTask(long taskId);
}
