package com.example.smarttasksapp.app;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;

import com.example.smarttasksapp.R;
import com.example.smarttasksapp.feature.tasks.ui.adapter.TaskAdapter;
import com.example.smarttasksapp.feature.tasks.ui.adapter.SwipeToCompleteCallback;
import com.example.smarttasksapp.feature.tasks.ui.view.AddTaskBottomSheet;
import com.example.smarttasksapp.feature.tasks.ui.viewmodel.TaskViewModel;
import com.example.smarttasksapp.infrastructure.entity.Task;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TaskViewModel viewModel;
    private TaskAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        RecyclerView rv = findViewById(R.id.rvTasks);
        View fab = findViewById(R.id.fabAdd);

        adapter = new TaskAdapter();
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        // 设置任务状态变化监听器
        adapter.setOnTaskStatusChangeListener((taskId, isCompleted) -> {
            viewModel.updateTaskCompletedStatus(taskId, isCompleted);
        });

        viewModel = new ViewModelProvider(this).get(TaskViewModel.class);
        viewModel.getTasks().observe(this, adapter::submitList);

        fab.setOnClickListener(v -> new AddTaskBottomSheet().show(getSupportFragmentManager(), "addTask"));
        enableDrag(fab);

        attachDragSort(rv);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void enableDrag(View fab) {
        final float[] delta = new float[2];
        final float[] downRaw = new float[2];
        final boolean[] dragging = new boolean[1];
        final int touchSlop = ViewConfiguration.get(fab.getContext()).getScaledTouchSlop();

        fab.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    downRaw[0] = event.getRawX();
                    downRaw[1] = event.getRawY();
                    delta[0] = v.getX() - downRaw[0];
                    delta[1] = v.getY() - downRaw[1];
                    dragging[0] = false;
                    return true; // capture gesture
                case MotionEvent.ACTION_MOVE: {
                    float dx = event.getRawX() - downRaw[0];
                    float dy = event.getRawY() - downRaw[1];
                    if (!dragging[0] && (Math.hypot(dx, dy) > touchSlop)) {
                        dragging[0] = true;
                    }
                    if (dragging[0]) {
                        v.setX(event.getRawX() + delta[0]);
                        v.setY(event.getRawY() + delta[1]);
                    }
                    return true;
                }
                case MotionEvent.ACTION_UP:
                    if (!dragging[0]) {
                        // delegate to OnClickListener
                        v.performClick();
                    }
                    dragging[0] = false;
                    return true;
                default:
                    return false;
            }
        });
    }

    private void attachDragSort(RecyclerView recyclerView) {
        SwipeToCompleteCallback callback = new SwipeToCompleteCallback(adapter, (taskId, isCompleted) -> {
            viewModel.updateTaskCompletedStatus(taskId, isCompleted);
        });
        
        // 设置拖拽排序回调
        callback.setOnMoveListener((from, to) -> {
            if (from == RecyclerView.NO_POSITION || to == RecyclerView.NO_POSITION) return false;
            List<Task> current = new ArrayList<>(adapter.getCurrentList());
            Task moved = current.remove(from);
            current.add(to, moved);
            adapter.submitList(current);
            return true;
        });
        
        // 设置拖拽完成回调
        callback.setOnDragCompleteListener((ordered) -> {
            viewModel.persistOrder(ordered);
        });
        
        new ItemTouchHelper(callback).attachToRecyclerView(recyclerView);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}