package lib.kalu.safekeyboard;

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
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.lang.ref.WeakReference;

@Keep
public class SafeKeyboardDialog extends DialogFragment implements DialogInterface.OnKeyListener, Handler.Callback {

    @Keep
    public static final String TAG = "lib.kalu.safekeyboard.safekeyboarddialog";

    @Keep
    public static final String ACTINO_KEYBOARD_DELETE = "actino_keyboard_delete";
    @Keep
    public static final String ACTINO_KEYBOARD_INPUT = "actino_keyboard_input";
    @Keep
    public static final String ACTINO_KEYBOARD_DONE = "actino_keyboard_done";
    @Keep
    public static final String ACTINO_KEYBOARD_INIT = "actino_keyboard_init";

    /**
     * logo
     */
    @Keep
    public static final String BUNDLE_KEYBOARD_LOGO = "bundle_random_letter";
    /**
     * title
     */
    @Keep
    public static final String BUNDLE_KEYBOARD_TITLE = "bundle_keyboard_title";
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
    /**
     * 回调：code
     */
    @Keep
    public static final int BUNDLE_CALLBACK_CODE = 119110120;
    /**
     * 回调：key
     */
    @Keep
    public static final String BUNDLE_CALLBACK_TYPE = "bundle_callback_type";
    /**
     * 回调：value
     */
    @Keep
    public static final String BUNDLE_CALLBACK_VALUE = "bundle_callback_value";
    /**
     * 回调：extra
     */
    @Keep
    public static final String BUNDLE_CALLBACK_EXTRA = "bundle_callback_extra";
    /**
     * 回调：edittext-id
     */
    @Keep
    public static final String BUNDLE_CALLBACK_ID = "bundle_callback_id";

    private final Handler mHandler = new Handler(this);

    @Override
    public boolean handleMessage(@NonNull Message msg) {
        if (null != msg && null != msg.obj && msg.what == 101) {
            Object[] objs = (Object[]) msg.obj;
            super.show((FragmentManager) objs[0], (String) objs[1]);
            callActivityReenter(ACTINO_KEYBOARD_INIT, null);
        }
        return false;
    }

    @Override
    public void show(@NonNull FragmentManager manager, @Nullable String tag) {

        try {

            boolean pass = true;
            SafeKeyboardDialog temp = SafeKeyboardFragmentManager.get();
            if (null != temp && null != temp.getDialog()) {
                // SafeKeyboardLogUtil.log("show => isShowing = " + temp.getDialog().isShowing());

                int tes = -1;
                if (null != temp.getArguments()) {
                    tes = temp.getArguments().getInt(BUNDLE_CALLBACK_ID, -1);
                }

                int ids = -1;
                Bundle arguments = getArguments();
                if (null != arguments) {
                    ids = arguments.getInt(BUNDLE_CALLBACK_ID, -1);
                }
                SafeKeyboardLogUtil.log("show => ids = " + ids + ", tes = " + tes);

                if (ids != -1 && tes != -1 && ids == tes) {
                    pass = false;
                }
            }

            SafeKeyboardLogUtil.log("show => pass = " + pass);
            if (pass) {

                mHandler.removeMessages(101);
                mHandler.removeCallbacksAndMessages(null);

                // add
                SafeKeyboardFragmentManager.setFragmentManager(this);

                // 延迟显示安全键盘
                int delayTime = 60;
                Bundle arguments = getArguments();
                if (null != arguments) {
                    delayTime = arguments.getInt(BUNDLE_DELAY_TIME, 60);
                }
                if (delayTime < 60) {
                    delayTime = 60;
                }

                Object[] objects = new Object[2];
                objects[0] = manager;
                objects[1] = tag;
                Message message = Message.obtain();
                message.what = 101;
                message.obj = objects;
                mHandler.sendMessageDelayed(message, delayTime);
            }

        } catch (Exception e) {
            SafeKeyboardLogUtil.log("show => " + e.getMessage());
        }
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

        Context context = getContext().getApplicationContext();
        View inflate = LayoutInflater.from(context).inflate(R.layout.moudle_safe_keyboard_dialog, null);

        if (null != arguments) {

            try {

                // logo
                ImageView logo = inflate.findViewById(R.id.moudle_safe_id_logo);
                if (null != logo) {
                    int anInt = arguments.getInt(BUNDLE_KEYBOARD_LOGO, -1);
                    if (anInt != -1) {
                        logo.setImageResource(anInt);
                    }
                }

                // title
                TextView title = inflate.findViewById(R.id.moudle_safe_id_title);
                if (null != title) {
                    String text = arguments.getString(BUNDLE_KEYBOARD_TITLE);
                    if (null != text && text.length() > 0) {
                        title.setText(text);
                    } else {
                        int anInt = arguments.getInt(BUNDLE_KEYBOARD_TITLE, -1);
                        if (anInt != -1) {
                            title.setText(anInt);
                        }
                    }
                }

            } catch (Exception e) {
            }
        }

        dialog.setContentView(inflate);
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

        // 避免Dialog抢Activity焦点
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
//        window.setFlags(WindowManager.LayoutParams.FLAG_LOCAL_FOCUS_MODE, WindowManager.LayoutParams.FLAG_LOCAL_FOCUS_MODE);

        window.setAttributes(windowParams);
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        // window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);

//        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
//        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        // 监听activity物理返回键
        getActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

//                SafeKeyboardView safeKeyboardView = getDialog().findViewById(R.id.moudle_safe_id_keyboard);
//                String input = safeKeyboardView.getInput();
//                callActivityReenter(ACTINO_KEYBOARD_DONE, input);
                dismiss();
            }
        });

        // 确定
        getDialog().findViewById(R.id.moudle_safe_id_done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                SafeKeyboardView safeKeyboardView = getDialog().findViewById(R.id.moudle_safe_id_keyboard);
//                String input = safeKeyboardView.getInput();
//                callActivityReenter(ACTINO_KEYBOARD_DONE, input);
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
        SafeKeyboardView safeKeyboardView = getDialog().findViewById(R.id.moudle_safe_id_keyboard);
        safeKeyboardView.setRandomLetter(randomLetter);
        safeKeyboardView.setRandomNumber(randomNumber);
        safeKeyboardView.setOnSafeKeyboardChangeListener(new SafeKeyboardView.OnSafeKeyboardChangeListener() {
            @Override
            public void onInput(@NonNull CharSequence letter) {

                SafeKeyboardLogUtil.log("onInput => letter = " + letter);
                callActivityReenter(ACTINO_KEYBOARD_INPUT, letter);
            }

            @Override
            public void onInputReal(@NonNull CharSequence letter, @NonNull CharSequence news) {
                SafeKeyboardLogUtil.log("onInputReal => letter = " + letter + ", news = " + news);
            }

            @Override
            public void onDelete(@NonNull CharSequence letter) {

                SafeKeyboardLogUtil.log("onDelete => letter = " + letter);
                callActivityReenter(ACTINO_KEYBOARD_DELETE, letter);
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
        SafeKeyboardLogUtil.log("onKey => keyCode = " + keyCode + ", action = " + event.getAction());
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            dismiss();
        }
        return false;
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
    public void onDestroyView() {
        try {
            SafeKeyboardView safeKeyboardView = getDialog().findViewById(R.id.moudle_safe_id_keyboard);
            String input = safeKeyboardView.getInput();
            callActivityReenter(ACTINO_KEYBOARD_DONE, input);
            SafeKeyboardLogUtil.log("onDestroyView => input = "+input);
            super.onDestroyView();
        } catch (Exception e) {
            SafeKeyboardLogUtil.log("onDestroyView => " + e.getMessage());
        }
    }

    @Override
    public void dismiss() {
        try {
            super.dismiss();
        } catch (Exception e) {
            SafeKeyboardLogUtil.log("dismiss => " + e.getMessage());
        }
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        try {
            super.onCancel(dialog);
        } catch (Exception e) {
            SafeKeyboardLogUtil.log("onCancel => " + e.getMessage());
        }
    }

    private final void callActivityReenter(@NonNull String type, @Nullable CharSequence value) {

        Activity activity = getActivity();
        if (null == activity)
            return;

        if (ACTINO_KEYBOARD_DONE.equals(type) && null == value)
            return;

        if (ACTINO_KEYBOARD_DONE.equals(type) && null != value && value.length() == 0)
            return;

        try {
            Intent intent = new Intent();
            intent.putExtra(BUNDLE_CALLBACK_TYPE, type);

            Bundle arguments = getArguments();
            if (null != arguments) {
                String extra = arguments.getString(BUNDLE_CALLBACK_EXTRA);
                intent.putExtra(BUNDLE_CALLBACK_EXTRA, extra);
                int id = arguments.getInt(BUNDLE_CALLBACK_ID);
                intent.putExtra(BUNDLE_CALLBACK_ID, id);
            }

            if (null != value && value.length() > 0) {
                intent.putExtra(BUNDLE_CALLBACK_VALUE, value);
            }

            activity.onActivityReenter(BUNDLE_CALLBACK_CODE, intent);
        } catch (Exception e) {
            SafeKeyboardLogUtil.log("callActivityReenter => " + e.getMessage());
        }
    }
}