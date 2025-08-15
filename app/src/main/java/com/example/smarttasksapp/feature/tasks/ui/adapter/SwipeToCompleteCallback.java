package com.example.smarttasksapp.feature.tasks.ui.adapter;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smarttasksapp.infrastructure.entity.Task;

import java.util.List;

public class SwipeToCompleteCallback extends ItemTouchHelper.Callback {

    private final TaskAdapter adapter;
    private final OnSwipeListener swipeListener;
    private OnMoveListener moveListener;
    private OnDragCompleteListener dragCompleteListener;
    private boolean isSwipeEnabled = true;

    public interface OnSwipeListener {
        void onTaskStatusChanged(long taskId, boolean isCompleted);
    }

    public interface OnMoveListener {
        boolean onMove(int from, int to);
    }

    public interface OnDragCompleteListener {
        void onDragComplete(List<Task> ordered);
    }

    public SwipeToCompleteCallback(TaskAdapter adapter, OnSwipeListener listener) {
        this.adapter = adapter;
        this.swipeListener = listener;
    }

    public void setOnMoveListener(OnMoveListener listener) {
        this.moveListener = listener;
    }

    public void setOnDragCompleteListener(OnDragCompleteListener listener) {
        this.dragCompleteListener = listener;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        // 允许上下拖拽和左滑
        return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.START);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        // 处理拖拽排序
        int from = viewHolder.getAdapterPosition();
        int to = target.getAdapterPosition();
        if (from == RecyclerView.NO_POSITION || to == RecyclerView.NO_POSITION) return false;
        
        if (moveListener != null) {
            return moveListener.onMove(from, to);
        }
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        // 处理左滑：切换任务完成状态
        if (direction == ItemTouchHelper.START) {
            int position = viewHolder.getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                Task task = adapter.getCurrentList().get(position);
                if (swipeListener != null) {
                    swipeListener.onTaskStatusChanged(task.getId(), !task.isCompleted());
                }
            }
        }
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            View itemView = viewHolder.itemView;
            float height = itemView.getHeight();
            float width = itemView.getWidth();
            
            // 限制左滑距离
            if (dX < 0) {
                dX = Math.max(dX, -width * 0.3f); // 最多左滑30%
            }
            
            // 绘制左滑背景
            if (dX < 0) {
                Paint paint = new Paint();
                
                // 根据任务状态选择颜色
                int position = viewHolder.getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Task task = adapter.getCurrentList().get(position);
                    if (task.isCompleted()) {
                        paint.setColor(Color.parseColor("#FF9800")); // 已完成：橙色
                    } else {
                        paint.setColor(Color.parseColor("#4CAF50")); // 未完成：绿色
                    }
                } else {
                    paint.setColor(Color.parseColor("#4CAF50")); // 默认绿色
                }
                
                // 根据滑动距离调整透明度
                float alpha = Math.abs(dX) / (width * 0.3f);
                paint.setAlpha((int) (255 * alpha));
                
                // 绘制左滑背景矩形
                RectF background = new RectF(itemView.getRight() + dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                c.drawRect(background, paint);
                
                // 绘制图标和文字
                if (Math.abs(dX) > width * 0.15f) { // 滑动超过15%时显示图标
                    paint.setColor(Color.WHITE);
                    paint.setAlpha((int) (255 * alpha));
                    paint.setTextSize(40);
                    paint.setTextAlign(Paint.Align.CENTER);
                    
                    float iconX = itemView.getRight() + dX / 2;
                    float iconY = itemView.getTop() + height / 2 + 15;
                    
                    // 根据任务状态选择图标
                    if (position != RecyclerView.NO_POSITION) {
                        Task task = adapter.getCurrentList().get(position);
                        if (task.isCompleted()) {
                            c.drawText("↺", iconX, iconY, paint); // 未完成图标
                        } else {
                            c.drawText("✓", iconX, iconY, paint); // 完成图标
                        }
                    } else {
                        c.drawText("↺", iconX, iconY, paint); // 默认未完成图标
                    }
                }
            }
            
            // 移动itemView
            itemView.setTranslationX(dX);
        } else {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    }

    @Override
    public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
        // 拖拽完成后通知监听器
        if (dragCompleteListener != null) {
            List<Task> ordered = adapter.getCurrentList();
            dragCompleteListener.onDragComplete(ordered);
        }
    }

    @Override
    public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
        // 设置左滑阈值，需要滑动超过30%才触发
        return 0.3f;
    }

    @Override
    public float getSwipeEscapeVelocity(float defaultValue) {
        // 设置左滑逃逸速度
        return defaultValue * 0.5f;
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        super.onSelectedChanged(viewHolder, actionState);
        if (actionState == ItemTouchHelper.ACTION_STATE_IDLE) {
            // 重置itemView的translationX
            if (viewHolder != null) {
                viewHolder.itemView.setTranslationX(0);
            }
        }
    }

    public void setSwipeEnabled(boolean enabled) {
        this.isSwipeEnabled = enabled;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return isSwipeEnabled;
    }
}
