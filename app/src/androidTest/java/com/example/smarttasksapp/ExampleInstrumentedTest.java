package com.example.smarttasksapp;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import com.example.smarttasksapp.feature.reminder.domain.ReminderConfig;
import com.example.smarttasksapp.feature.reminder.service.impl.ReminderService;
import com.example.smarttasksapp.infrastructure.entity.Task;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.example.smarttasksapp", appContext.getPackageName());
    }

    @Test
    public void test_alarm() {
        // 在任务模块中
        ReminderService reminderService = new ReminderService(ApplicationProvider.getApplicationContext());

        // 添加任务并设置提醒
        Task task = new Task();
        task.setTitle("完成报告");
        task.setStartTime(System.currentTimeMillis() + 60 * 1000); // 1分钟后提醒


        ReminderConfig config = new ReminderConfig(
                1,
                task.getTitle(),
                task.getStartTime(),
                false  // 不重复
        );
        reminderService.setReminder(config);
    }
}