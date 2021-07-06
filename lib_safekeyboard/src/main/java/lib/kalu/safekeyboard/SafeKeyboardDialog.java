package lib.kalu.safekeyboard;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.lang.ref.WeakReference;

@Keep
public class SafeKeyboardDialog extends DialogFragment implements DialogInterface.OnKeyListener, Handler.Callback {

    @Keep
    public static final String TAG = "lib.kalu.safekeyboard.safekeyboarddialog";

    @Keep
    public static final int INTENT_CALLBACK_CODE = 119110120;
    @Keep
    public static final String INTENT_CALLBACK_TYPE = "intent_callback_type";
    @Keep
    public static final String INTENT_CALLBACK_VALUE = "intent_callback_value";

    @Keep
    public static final String KEYBOARD_DELETE = "keyboard_delete";
    @Keep
    public static final String KEYBOARD_INPUT = "keyboard_input";
    @Keep
    public static final String KEYBOARD_SURE = "keyboard_sure";
    @Keep
    public static final String KEYBOARD_DISMISS = "keyboard_dismiss";
    @Keep
    public static final String KEYBOARD_CANCEL = "keyboard_cancel";

    /**
     * 字母键盘随机
     */
    @Keep
    public static final String BUNDLE_RANDOM_LETTER = "bundle_random_letter";
    /**
     * 数字键盘随机
     */
    @Keep
    public static final String BUNDLE_RANDOM_NUMBER = "bundle_random_number";
    /**
     * 点击外部取消
     */
    @Keep
    public static final String BUNDLE_OUTSIDE_CANCLE = "bundle_outside_cancle";
    /**
     * 延迟时间
     */
    @Keep
    public static final String BUNDLE_DELAY_TIME = "bundle_delay_time";

    private final Handler mHandler = new Handler(this);

    @Override
    public boolean handleMessage(@NonNull Message msg) {
        if (null != msg && null != msg.obj && msg.what == 101) {
            Object[] objs = (Object[]) msg.obj;
            super.show((FragmentManager) objs[0], (String) objs[1]);
        }
        return false;
    }

    @Override
    public void show(@NonNull FragmentManager manager, @Nullable String tag) {

        // 延迟显示安全键盘
        long delayTime = 80;
        Bundle arguments = getArguments();
        if (null != arguments) {
            delayTime = arguments.getLong(BUNDLE_DELAY_TIME, 80);
        }

        Object[] objects = new Object[2];
        objects[0] = manager;
        objects[1] = tag;
        Message message = Message.obtain();
        message.what = 101;
        message.obj = objects;
        mHandler.sendMessageDelayed(message, delayTime);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        WeakReference<Activity> weakReference = new WeakReference<>(getActivity());
        Dialog dialog = new Dialog(weakReference.get()) {
            @Override
            protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setStyle(STYLE_NORMAL, R.style.ThemeMoudleSafeKeyboard);
            }
        };

        boolean isCancle = false;
        Bundle arguments = getArguments();
        if (null != arguments) {
            isCancle = arguments.getBoolean(BUNDLE_OUTSIDE_CANCLE, false);
        }

        dialog.setContentView(R.layout.moudle_safe_keyboard_dialog);
        dialog.setCancelable(isCancle);
        dialog.setCanceledOnTouchOutside(isCancle);
        dialog.setOnKeyListener(this);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // 窗口边框
        Window window = getDialog().getWindow();
        window.getDecorView().setPadding(0, 0, 0, 0);

        // 禁止录屏和截屏
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE);

        // 窗口位置
        WindowManager.LayoutParams windowParams = window.getAttributes();
        windowParams.dimAmount = 0f;
        windowParams.gravity = Gravity.BOTTOM;

        window.setAttributes(windowParams);
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        // window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);

//        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
//        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);


        // 确定
        getDialog().findViewById(R.id.moudle_id_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SafeKeyboardView safe = getDialog().findViewById(R.id.moudle_id_safe);
                String input = safe.getInput();

                Intent intent = new Intent();
                intent.putExtra(INTENT_CALLBACK_TYPE, KEYBOARD_SURE);
                intent.putExtra(INTENT_CALLBACK_VALUE, input);

                Activity activity = getActivity();
                activity.onActivityReenter(INTENT_CALLBACK_CODE, intent);

                dismiss();
            }
        });

        boolean randomLetter = false;
        boolean randomNumber = false;
        if (null != getArguments()) {
            randomLetter = getArguments().getBoolean(BUNDLE_RANDOM_LETTER, false);
            randomNumber = getArguments().getBoolean(BUNDLE_RANDOM_NUMBER, false);
        }

        // 安全键盘
        SafeKeyboardView safeKeyboardView = getDialog().findViewById(R.id.moudle_id_safe);
        safeKeyboardView.setRandomLetter(randomLetter);
        safeKeyboardView.setRandomNumber(randomNumber);
        safeKeyboardView.setOnSafeKeyboardChangeListener(new SafeKeyboardView.OnSafeKeyboardChangeListener() {
            @Override
            public void onInput(@NonNull CharSequence letter) {
                SafeKeyboardLogUtil.log("onInput => letter = " + letter);

                Intent intent = new Intent();
                intent.putExtra(INTENT_CALLBACK_TYPE, KEYBOARD_INPUT);
                intent.putExtra(INTENT_CALLBACK_VALUE, letter);

                Activity activity = getActivity();
                activity.onActivityReenter(INTENT_CALLBACK_CODE, intent);
            }

            @Override
            public void onInputReal(@NonNull CharSequence letter, @NonNull CharSequence news) {
                SafeKeyboardLogUtil.log("onInputReal => letter = " + letter + ", news = " + news);
            }

            @Override
            public void onDelete(@NonNull CharSequence letter) {
                SafeKeyboardLogUtil.log("onDelete => letter = " + letter);

                Intent intent = new Intent();
                intent.putExtra(INTENT_CALLBACK_TYPE, KEYBOARD_DELETE);
                intent.putExtra(INTENT_CALLBACK_VALUE, letter);

                Activity activity = getActivity();
                activity.onActivityReenter(INTENT_CALLBACK_CODE, intent);
            }

            @Override
            public void onDeleteReal(@NonNull CharSequence letter, @NonNull CharSequence news) {
                SafeKeyboardLogUtil.log("onDeleteReal => letter = " + letter + ", news = " + news);
            }
        });

        return super.onCreateView(inflater, container, savedInstanceState);
    }


    @Override
    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
        return !isCancelable();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        // 默认文字大小
        if (newConfig.fontScale != 1) {
            newConfig.setToDefaults();
            Resources resources = getResources();
            resources.updateConfiguration(newConfig, resources.getDisplayMetrics());
        }
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void dismiss() {
        super.dismiss();

        Intent intent = new Intent();
        intent.putExtra(INTENT_CALLBACK_TYPE, KEYBOARD_DISMISS);

        Activity activity = getActivity();
        activity.onActivityReenter(INTENT_CALLBACK_CODE, intent);
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);

        Intent intent = new Intent();
        intent.putExtra(INTENT_CALLBACK_TYPE, KEYBOARD_CANCEL);

        Activity activity = getActivity();
        activity.onActivityReenter(INTENT_CALLBACK_CODE, intent);
    }
}
