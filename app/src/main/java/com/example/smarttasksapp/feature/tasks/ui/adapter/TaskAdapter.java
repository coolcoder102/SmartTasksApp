package com.example.smarttasksapp.feature.tasks.ui.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smarttasksapp.R;
import com.example.smarttasksapp.infrastructure.entity.Task;
import com.example.smarttasksapp.feature.tasks.ui.view.TaskDetailBottomSheet;
import com.google.android.material.card.MaterialCardView;

public class TaskAdapter extends ListAdapter<Task, TaskAdapter.TaskViewHolder> {

    private OnTaskStatusChangeListener statusChangeListener;

    public interface OnTaskStatusChangeListener {
        void onTaskStatusChanged(long taskId, boolean isCompleted);
    }

    public TaskAdapter() {
        super(DIFF);
    }

    public void setOnTaskStatusChangeListener(OnTaskStatusChangeListener listener) {
        this.statusChangeListener = listener;
    }

    private static final DiffUtil.ItemCallback<Task> DIFF = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull Task oldItem, @NonNull Task newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Task oldItem, @NonNull Task newItem) {
            String ot = oldItem.getTitle();
            String nt = newItem.getTitle();
            String od = oldItem.getDescription();
            String nd = newItem.getDescription();
            return ((ot == null && nt == null) || (ot != null && ot.equals(nt)))
                    && ((od == null && nd == null) || (od != null && od.equals(nd)))
                    && oldItem.getCreatedAt() == newItem.getCreatedAt()
                    && oldItem.isCompleted() == newItem.isCompleted();
        }
    };

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = getItem(position);
        holder.bind(task);
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        final MaterialCardView taskCard;
        final TextView title;
        final TextView desc;
        final LinearLayout leftSwipeBackground;
        final ImageView completeIcon;
        final TextView completeText;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskCard = itemView.findViewById(R.id.taskCard);
            title = itemView.findViewById(R.id.tvTitle);
            desc = itemView.findViewById(R.id.tvDesc);
            leftSwipeBackground = itemView.findViewById(R.id.leftSwipeBackground);
            completeIcon = itemView.findViewById(R.id.ivCompleteIcon);
            completeText = itemView.findViewById(R.id.tvCompleteText);
        }

        void bind(Task task) {
            title.setText(task.getTitle());
            desc.setText(task.getDescription() == null ? "" : task.getDescription());
            
            // 根据完成状态设置视觉样式
            if (task.isCompleted()) {
                // 已完成：灰色样式
                taskCard.setCardBackgroundColor(Color.LTGRAY);
                title.setTextColor(Color.DKGRAY);
                desc.setTextColor(Color.GRAY);
                completeIcon.setImageResource(android.R.drawable.ic_menu_revert);
                completeText.setText("未完成");
                leftSwipeBackground.setBackgroundColor(Color.parseColor("#FF9800")); // 橙色
            } else {
                // 未完成：正常样式
                taskCard.setCardBackgroundColor(Color.WHITE);
                title.setTextColor(Color.BLACK);
                desc.setTextColor(Color.DKGRAY);
                completeIcon.setImageResource(android.R.drawable.ic_menu_send);
                completeText.setText("完成");
                leftSwipeBackground.setBackgroundColor(Color.parseColor("#4CAF50")); // 绿色
            }

            // 设置点击事件
            taskCard.setOnClickListener(v ->
                    TaskDetailBottomSheet.newInstance(task)
                            .show(((androidx.fragment.app.FragmentActivity) v.getContext()).getSupportFragmentManager(), "taskDetail")
            );

            // 左滑背景现在由ItemTouchHelper处理，不需要点击事件
        }
    }
}


