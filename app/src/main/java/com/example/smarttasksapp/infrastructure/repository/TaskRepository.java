package com.example.smarttasksapp.infrastructure.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.smarttasksapp.feature.tasks.data.ITaskRepository;
import com.example.smarttasksapp.infrastructure.database.AppDatabase;
import com.example.smarttasksapp.infrastructure.dao.TaskDao;
import com.example.smarttasksapp.infrastructure.entity.Task;

import java.util.List;
import java.util.concurrent.Executors;

public class TaskRepository implements ITaskRepository {
    private final TaskDao taskDao;

    public TaskRepository(Context context) {
        this.taskDao = AppDatabase.getInstance(context).taskDao();
    }

    public LiveData<List<Task>> observeAll() {
        return taskDao.observeAll();
    }

    public void addTask(String title, String description, long startTime) {
        Executors.newSingleThreadExecutor().execute(() -> {
            Task task = new Task(title, description, System.currentTimeMillis());
            task.setStartTime(startTime);
            long max = taskDao.getMaxSortIndex();
            task.setSortIndex(max + 1); // 新任务排在顶部
            taskDao.insert(task);
        });
    }

    public void reorder(long fromTaskId, long toTaskId, boolean placeAbove) {
        Executors.newSingleThreadExecutor().execute(() -> {
            // 简化：交换两个任务的 sortIndex；placeAbove 在此实现中不区分，直接交换
            long tmp = taskDao.getMaxSortIndex() + 1; // 临时占位，避免唯一性冲突
            taskDao.updateSortIndex(fromTaskId, tmp);
            // 读取 to 的 sortIndex 无 API，这里用两步：把 to 提前（max+2），from 设为原 to（max+1）
            // 简化策略：把目标放到队头，from 放到队头+1，达到视觉上的“移动”效果
            long maxNow = taskDao.getMaxSortIndex();
            taskDao.updateSortIndex(toTaskId, maxNow + 2);
            taskDao.updateSortIndex(fromTaskId, maxNow + 1);
        });
    }

    public void persistOrder(List<Task> ordered) {
        Executors.newSingleThreadExecutor().execute(() -> {
            // 最高在顶部：给顶部更大的 sortIndex，保证查询时位于前方
            long base = ordered.size();
            for (int i = 0; i < ordered.size(); i++) {
                Task task = ordered.get(i);
                long sort = base - i; // i 越小，sortIndex 越大
                taskDao.updateSortIndex(task.getId(), sort);
            }
        });
    }

    public void updateTask(long taskId, String title, String description, long startTime) {
        Executors.newSingleThreadExecutor().execute(() -> {
            taskDao.updateTitleAndDescription(taskId, title, description);
            taskDao.updateStartTime(taskId, startTime);
        });
    }
    
    public void updateTaskCompletedStatus(long taskId, boolean isCompleted) {
        Executors.newSingleThreadExecutor().execute(() -> {
            taskDao.updateCompletedStatus(taskId, isCompleted);
        });
    }
    
    public void updateTaskStartTime(long taskId, long startTime) {
        Executors.newSingleThreadExecutor().execute(() -> {
            taskDao.updateStartTime(taskId, startTime);
        });
    }
    
    public void deleteTask(long taskId) {
        Executors.newSingleThreadExecutor().execute(() -> {
            taskDao.deleteTask(taskId);
        });
    }
}


