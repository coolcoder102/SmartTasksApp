package com.example.smarttasksapp.feature.tasks.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.smarttasksapp.feature.reminder.domain.ReminderConfig;
import com.example.smarttasksapp.feature.tasks.data.ITaskRepository;
import com.example.smarttasksapp.infrastructure.repository.TaskRepository;
import com.example.smarttasksapp.infrastructure.entity.Task;

import java.util.List;

public class TaskViewModel extends AndroidViewModel {
    private final ITaskRepository repository;
    private final LiveData<List<Task>> tasks;

    public TaskViewModel(@NonNull Application application) {
        super(application);
        this.repository = new TaskRepository(application);
        this.tasks = repository.observeAll();
    }

    public LiveData<List<Task>> getTasks() {
        return tasks;
    }

    public void addTask(String title, String description, long startTime) {
        repository.addTask(title, description, startTime);
        if (startTime > 0) {
            ReminderConfig reminderConfig = new ReminderConfig();
        }
    }

    public void reorder(long fromTaskId, long toTaskId, boolean placeAbove) {
        repository.reorder(fromTaskId, toTaskId, placeAbove);
    }

    public void persistOrder(java.util.List<Task> ordered) {
        repository.persistOrder(ordered);
    }

    public void updateTask(long taskId, String title, String description, long startTime) {
        repository.updateTask(taskId, title, description, startTime);
    }
    
    public void updateTaskCompletedStatus(long taskId, boolean isCompleted) {
        repository.updateTaskCompletedStatus(taskId, isCompleted);
    }
    
    public void updateTaskStartTime(long taskId, long startTime) {
        repository.updateTaskStartTime(taskId, startTime);
    }
    
    public void deleteTask(long taskId) {
        repository.deleteTask(taskId);
    }
}


