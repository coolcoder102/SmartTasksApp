package com.example.smarttasksapp.infrastructure.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "tasks")
public class Task {
    @PrimaryKey(autoGenerate = true)
    private long id;

    private String title;
    private String description;
    private long createdAt;

    // 用于自定义排序
    private long sortIndex;
    
    // 任务完成状态
    private boolean isCompleted;
    
    // 任务开始时间
    private long startTime;

    // Room 需要空构造函数
    public Task() {}

    @Ignore
    public Task(String title, String description, long createdAt) {
        this.title = title;
        this.description = description;
        this.createdAt = createdAt;
        this.isCompleted = false; // 默认未完成
        this.startTime = 0; // 默认无开始时间
    }

    public long getId() { return id; }

    public void setId(long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getSortIndex() { return sortIndex; }
    public void setSortIndex(long sortIndex) { this.sortIndex = sortIndex; }
    
    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
    
    public long getStartTime() { return startTime; }
    public void setStartTime(long startTime) { this.startTime = startTime; }
}


