package com.example.smarttasksapp.infrastructure.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.smarttasksapp.infrastructure.entity.Task;

import java.util.List;

@Dao
public interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Task task);

    @Query("SELECT * FROM tasks ORDER BY sortIndex DESC, createdAt ASC")
    LiveData<List<Task>> observeAll();

    @Query("SELECT COALESCE(MAX(sortIndex), 0) FROM tasks")
    long getMaxSortIndex();

    @Query("UPDATE tasks SET sortIndex = :sortIndex WHERE id = :taskId")
    void updateSortIndex(long taskId, long sortIndex);

    @Query("UPDATE tasks SET title = :title, description = :description WHERE id = :taskId")
    void updateTitleAndDescription(long taskId, String title, String description);
    
    @Query("UPDATE tasks SET isCompleted = :isCompleted WHERE id = :taskId")
    void updateCompletedStatus(long taskId, boolean isCompleted);
    
    @Query("UPDATE tasks SET startTime = :startTime WHERE id = :taskId")
    void updateStartTime(long taskId, long startTime);
    
    @Query("DELETE FROM tasks WHERE id = :taskId")
    void deleteTask(long taskId);

}


