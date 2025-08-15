package com.example.smarttasksapp.feature.tasks.ui.view;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.Editable;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Consumer;
import androidx.lifecycle.ViewModelProvider;

import com.example.smarttasksapp.R;
import com.example.smarttasksapp.feature.tasks.ui.viewmodel.TaskViewModel;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddTaskBottomSheet extends BottomSheetDialogFragment {
    // 每次进程重启都会归零，满足"每次重新打开应用后只提醒前两次"的需求
    private static int sTitleWarnCount = 0;
    
    private TextView mTvStartTime;
    private long selectedStartTime = 0;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
    
    // 缓存相关常量
    private static final String PREF_NAME = "add_task_cache";
    private static final String KEY_TITLE = "cached_title";
    private static final String KEY_DESCRIPTION = "cached_description";
    private static final String KEY_START_TIME = "cached_start_time";
    
    private EditText mEtTitle;
    private EditText mEtDesc;
    private SharedPreferences mSharedPreferences;
    private TaskViewModel mTaskViewModel;
    private Button mBtnSubmit;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.bottom_sheet_add_task, null, false);
        dialog.setContentView(view);

        initViewModel();

        initViews(view);

        initSharedPreferences();


        // 设置时间选择器点击事件
        mTvStartTime.setOnClickListener(v -> showDateTimePicker());

        // 加载缓存的内容
        loadCachedContent();

        setupTitleInput();

        setupDescriptionInput();

        setupSubmitButton();

        setupBottomSheetBehavior(dialog);

        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        return dialog;
    }

    private void initViewModel(){
        mTaskViewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);
    }

    private void initViews(View view) {
        mEtTitle = view.findViewById(R.id.etTitle);
        mEtDesc = view.findViewById(R.id.etDesc);
        mBtnSubmit = view.findViewById(R.id.btnSubmit);
        mTvStartTime = view.findViewById(R.id.tvStartTime);
    }

    private void initSharedPreferences() {
        mSharedPreferences = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    private void setupTitleInput() {
        // 限制标题最多两行（按视觉换行计算）
        mEtTitle.setHorizontallyScrolling(false);
        mEtTitle.setMaxLines(2);

        final String[] lastValid = { mEtTitle.getText().toString() };
        final boolean[] restoring = { false };

        mEtTitle.addTextChangedListener(simpleTextWatcher(s -> {
            if (restoring[0]) return;
            mEtTitle.post(() -> {
                Layout layout = mEtTitle.getLayout();
                if (layout != null && layout.getLineCount() > 2) {
                    restoring[0] = true;
                    mEtTitle.setText(lastValid[0]);
                    mEtTitle.setSelection(mEtTitle.getText().length());
                    restoring[0] = false;
                    if (sTitleWarnCount < 2) {
                        sTitleWarnCount++;
                        Toast.makeText(requireContext(), "标题最多两行", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    lastValid[0] = mEtTitle.getText().toString();
                    // 缓存标题内容
                    cacheContent();
                }
            });
        }));
    }

    private void setupDescriptionInput() {
        // 描述框固定高度为屏幕的三分之一，并允许内部滚动
        int oneThird = getOneThirdScreenHeight();
        mEtDesc.setMaxHeight(oneThird);
        mEtDesc.setVerticalScrollBarEnabled(true);
        mEtDesc.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
        mEtDesc.setMovementMethod(new ScrollingMovementMethod());
        mEtDesc.setGravity(Gravity.TOP | Gravity.START);

        mEtDesc.setOnTouchListener((v, event) -> {
            v.getParent().requestDisallowInterceptTouchEvent(true);
            return false;
        });

        // 为描述添加文本变化监听器，实现缓存
        mEtDesc.addTextChangedListener(simpleTextWatcher(s -> cacheContent()));
    }

    private void setupSubmitButton() {

        mBtnSubmit.setOnClickListener(v -> {
            String title = mEtTitle.getText().toString().trim();
            String desc = mEtDesc.getText().toString().trim();

            if (TextUtils.isEmpty(title)) {
                mEtTitle.setError("标题不能为空");
                mEtTitle.requestFocus();
                return;
            }

            mTaskViewModel.addTask(title, TextUtils.isEmpty(desc) ? null : desc, selectedStartTime);
            clearCache();
            dismiss();
        });
    }

    private void setupBottomSheetBehavior(BottomSheetDialog dialog) {

        dialog.setOnShowListener(dlg -> {
            View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                // 设定容器高度为全屏，使 peekHeight 半屏生效
                int half = getHalfScreenHeight();
                ViewGroup.LayoutParams lp = bottomSheet.getLayoutParams();
                lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
                bottomSheet.setLayoutParams(lp);

                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setPeekHeight(half, true);
                behavior.setDraggable(true);
                behavior.setHideable(true);
                behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

    }

    /**
     * 加载缓存的内容
     */
    private void loadCachedContent() {
        String cachedTitle = mSharedPreferences.getString(KEY_TITLE, "");
        String cachedDescription = mSharedPreferences.getString(KEY_DESCRIPTION, "");
        long cachedStartTime = mSharedPreferences.getLong(KEY_START_TIME, 0);
        
        if (!TextUtils.isEmpty(cachedTitle)) {
            mEtTitle.setText(cachedTitle);
            mEtTitle.setSelection(cachedTitle.length());
        }
        
        if (!TextUtils.isEmpty(cachedDescription)) {
            mEtDesc.setText(cachedDescription);
        }
        
        if (cachedStartTime > 0) {
            selectedStartTime = cachedStartTime;
            mTvStartTime.setText(dateFormat.format(new Date(cachedStartTime)));
            mTvStartTime.setTextColor(requireContext().getColor(android.R.color.black));
        }
    }

    /**
     * 缓存当前输入的内容
     */
    private void cacheContent() {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(KEY_TITLE, mEtTitle.getText().toString());
        editor.putString(KEY_DESCRIPTION, mEtDesc.getText().toString());
        editor.putLong(KEY_START_TIME, selectedStartTime);
        editor.apply();
    }

    /**
     * 清除缓存内容
     */
    private void clearCache() {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    private void showDateTimePicker() {
        final Calendar calendar = Calendar.getInstance();
        
        // 如果已经选择了时间，使用已选择的时间
        if (selectedStartTime > 0) {
            calendar.setTimeInMillis(selectedStartTime);
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(
            requireContext(),
            (view, year, month, dayOfMonth) -> {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                
                // 日期选择完成后显示时间选择器
                showTimePicker(calendar);
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        );
        
        datePickerDialog.show();
    }

    private void showTimePicker(final Calendar calendar) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
            requireContext(),
            (view, hourOfDay, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                
                selectedStartTime = calendar.getTimeInMillis();
                mTvStartTime.setText(dateFormat.format(new Date(selectedStartTime)));
                mTvStartTime.setTextColor(requireContext().getColor(android.R.color.black));
                
                // 缓存选择的时间
                cacheContent();
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        );
        
        timePickerDialog.show();
    }

    private int getHalfScreenHeight() {
        WindowManager wm = requireActivity().getWindowManager();
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        return (int) (dm.heightPixels * 0.5f);
    }

    private int getOneThirdScreenHeight() {
        WindowManager wm = requireActivity().getWindowManager();
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        return (int) (dm.heightPixels * (1f / 3f));
    }

    /**
     * 工具方法
     * @param afterTextChangedAction 设置改变文本内容后的行为
     * @return TextWatcher
     */
    private TextWatcher simpleTextWatcher(Consumer<Editable> afterTextChangedAction) {
        return new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                afterTextChangedAction.accept(s);
            }
        };
    }
}


