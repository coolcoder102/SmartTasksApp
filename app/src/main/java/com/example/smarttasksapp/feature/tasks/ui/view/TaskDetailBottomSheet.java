package com.example.smarttasksapp.feature.tasks.ui.view;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.Layout;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Consumer;
import androidx.lifecycle.ViewModelProvider;

import com.example.smarttasksapp.R;
import com.example.smarttasksapp.infrastructure.entity.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TaskDetailBottomSheet extends BottomSheetDialogFragment {

    // ---------- 常量 ----------
    private static final String ARG_ID = "arg_id";
    private static final String ARG_TITLE = "arg_title";
    private static final String ARG_DESC = "arg_desc";
    private static final String ARG_TIME = "arg_time";
    private static final String ARG_START_TIME = "arg_start_time";

    // ---------- 成员变量 ----------
    private long selectedStartTime = 0;
    private boolean isInEditMode = false;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());


    public static TaskDetailBottomSheet newInstance(Task task) {
        Bundle args = new Bundle();
        args.putLong(ARG_ID, task.getId());
        args.putString(ARG_TITLE, task.getTitle());
        args.putString(ARG_DESC, task.getDescription());
        args.putLong(ARG_TIME, task.getCreatedAt());
        args.putLong(ARG_START_TIME, task.getStartTime());

        TaskDetailBottomSheet fragment = new TaskDetailBottomSheet();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.bottom_sheet_task_detail, null, false);
        dialog.setContentView(view);

        // 初始化
        ViewHolder holder = initViewHolder(view);
        bindArguments(holder);
        setupTitleLineLimit(holder.editTitleField);
        setupClickListeners(holder);
        setupBottomSheetBehavior(dialog);

        return dialog;
    }

    private ViewHolder initViewHolder(View view) {
        return new ViewHolder(view);
    }

    private void bindArguments(ViewHolder viewHolder) {
        Bundle args = getArguments();
        if (args == null) return;

        String title = args.getString(ARG_TITLE, "");
        String desc = args.getString(ARG_DESC, "");
        long createdTime = args.getLong(ARG_TIME, 0);
        long startTime = args.getLong(ARG_START_TIME, 0);

        viewHolder.viewTitleText.setText(title);
        viewHolder.viewDescText.setText(desc);
        viewHolder.viewTimeText.setText(dateFormat.format(new Date(createdTime)));

        if (startTime > 0) {
            viewHolder.viewStartTimeText.setText(dateFormat.format(new Date(startTime)));
            viewHolder.editStartTimeField.setText(dateFormat.format(new Date(startTime)));
            selectedStartTime = startTime;
        } else {
            viewHolder.viewStartTimeText.setText("未设置");
            viewHolder.editStartTimeField.setText("选择时间");
        }

        viewHolder.editTitleField.setText(title);
        viewHolder.editDescField.setText(desc);

        toggleEdit(viewHolder.root, false);
    }

    // ---------- 标题限制 ----------
    private void setupTitleLineLimit(TextInputEditText editTitle) {
        editTitle.setHorizontallyScrolling(false);
        editTitle.setMaxLines(2);

        final String[] lastValidTitle = { editTitle.getText() == null ? "" : editTitle.getText().toString() };
        final boolean[] restoring = { false };

        editTitle.addTextChangedListener(simpleTextWatcher(s -> {
            if (restoring[0] || !isInEditMode) {
                lastValidTitle[0] = editTitle.getText().toString();
                return;
            }
            editTitle.post(() -> {
                Layout layout = editTitle.getLayout();
                if (layout != null && layout.getLineCount() > 2) {
                    restoring[0] = true;
                    editTitle.setText(lastValidTitle[0]);
                    editTitle.setSelection(editTitle.length());
                    restoring[0] = false;
                    Toast.makeText(requireContext(), "标题最多两行", Toast.LENGTH_SHORT).show();
                } else {
                    lastValidTitle[0] = editTitle.getText().toString();
                }
            });
        }));
    }

    // ---------- 点击事件 ----------
    private void setupClickListeners(ViewHolder viewHolder) {
        viewHolder.editStartTimeField.setOnClickListener(v -> showDateTimePicker(viewHolder.editStartTimeField));
        viewHolder.btnDelete.setOnClickListener(v -> showDeleteConfirmDialog());

        viewHolder.btnEdit.setOnClickListener(v -> {
            viewHolder.editTitleField.setText(viewHolder.viewTitleText.getText());
            viewHolder.editTitleField.setSelection(viewHolder.editTitleField.length());
            viewHolder.editDescField.setText(viewHolder.viewDescText.getText());

            toggleEdit(viewHolder.root, true);
            viewHolder.btnEdit.setVisibility(View.GONE);
            viewHolder.btnSave.setVisibility(View.VISIBLE);
        });

        viewHolder.btnSave.setOnClickListener(v -> {
            if (getArguments() == null) return;

            long id = getArguments().getLong(ARG_ID, 0);
            if (id == 0) return;

            String newTitle = viewHolder.editTitleField.getText().toString().trim();
            String newDesc = viewHolder.editDescField.getText().toString().trim();

            new ViewModelProvider(requireActivity())
                    .get(com.example.smarttasksapp.feature.tasks.ui.viewmodel.TaskViewModel.class)
                    .updateTask(id, newTitle, newDesc, selectedStartTime);

            viewHolder.viewTitleText.setText(newTitle);
            viewHolder.viewDescText.setText(newDesc);
            viewHolder.viewStartTimeText.setText(selectedStartTime > 0 ? dateFormat.format(new Date(selectedStartTime)) : "未设置");

            toggleEdit(viewHolder.root, false);
            viewHolder.btnSave.setVisibility(View.GONE);
            viewHolder.btnEdit.setVisibility(View.VISIBLE);
        });
    }

    // ---------- 底部弹窗行为 ----------
    private void setupBottomSheetBehavior(BottomSheetDialog dialog) {
        dialog.setOnShowListener(dlg -> {
            View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                int halfHeight = getHalfScreenHeight();
                bottomSheet.getLayoutParams().height = halfHeight;
                bottomSheet.requestLayout();

                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setSkipCollapsed(true);
                behavior.setHideable(false);
                behavior.setDraggable(false);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
    }

    // ---------- 时间选择 ----------
    private void showDateTimePicker(TextView editStartTime) {
        final Calendar calendar = Calendar.getInstance();
        if (selectedStartTime > 0) {
            calendar.setTimeInMillis(selectedStartTime);
        }
        new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            showTimePicker(calendar, editStartTime);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker(Calendar calendar, TextView editStartTime) {
        new TimePickerDialog(requireContext(), (view, hourOfDay, minute) -> {
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            selectedStartTime = calendar.getTimeInMillis();
            editStartTime.setText(dateFormat.format(new Date(selectedStartTime)));
            editStartTime.setTextColor(requireContext().getColor(android.R.color.black));
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
    }

    // ---------- 删除任务 ----------
    private void showDeleteConfirmDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("确认删除")
                .setMessage("确定要删除这个任务吗？删除后无法恢复。")
                .setPositiveButton("确认删除", (dialog, which) -> deleteTask())
                .setNegativeButton("取消", (dialog, which) -> dialog.dismiss())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void deleteTask() {
        if (getArguments() == null) return;
        long taskId = getArguments().getLong(ARG_ID, 0);
        if (taskId <= 0) return;

        new ViewModelProvider(requireActivity())
                .get(com.example.smarttasksapp.feature.tasks.ui.viewmodel.TaskViewModel.class)
                .deleteTask(taskId);

        dismiss();
        Toast.makeText(requireContext(), "任务已删除", Toast.LENGTH_SHORT).show();
    }

    // ---------- 编辑模式切换 ----------
    private void toggleEdit(View root, boolean editMode) {
        isInEditMode = editMode;
        root.findViewById(R.id.viewModeContainer).setVisibility(editMode ? View.GONE : View.VISIBLE);
        root.findViewById(R.id.editModeContainer).setVisibility(editMode ? View.VISIBLE : View.GONE);
    }

    // ---------- 工具 ----------
    private TextWatcher simpleTextWatcher(Consumer<Editable> afterTextChanged) {
        return new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) { afterTextChanged.accept(s); }
        };
    }

    private int getHalfScreenHeight() {
        DisplayMetrics dm = new DisplayMetrics();
        requireActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        return (int) (dm.heightPixels * 0.5f);
    }

    // ---------- ViewHolder ----------
    private static class ViewHolder {
        final View root;
        final TextView viewTitleText, viewDescText, viewTimeText, viewStartTimeText;

        final TextInputEditText editTitleField, editDescField;
        final TextView editStartTimeField;
        final View btnEdit, btnSave, btnDelete;

        ViewHolder(View view) {
            root = view;
            viewTitleText = view.findViewById(R.id.tvDetailTitle);
            viewDescText = view.findViewById(R.id.tvDetailDesc);
            viewTimeText = view.findViewById(R.id.tvDetailTime);
            viewStartTimeText = view.findViewById(R.id.tvDetailStartTime);
            editTitleField = view.findViewById(R.id.etDetailTitle);
            editDescField = view.findViewById(R.id.etDetailDesc);
            editStartTimeField = view.findViewById(R.id.etDetailStartTime);
            btnEdit = view.findViewById(R.id.btnEditTop);
            btnSave = view.findViewById(R.id.btnSaveTop);
            btnDelete = view.findViewById(R.id.btnDeleteTop);
        }
    }
}
