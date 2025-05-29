package lib.kalu.input;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import java.lang.ref.WeakReference;

public class CusKeyboardDialog extends DialogFragment implements DialogInterface.OnKeyListener {

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

        View inflate = LayoutInflater.from(getActivity()).inflate(R.layout.moudle_safe_keyboard_dialog, null);
        CusKeyboardView keyboardView = inflate.findViewById(R.id.moudle_safe_id_keyboard);
        keyboardView.setOnKeyChangeListener(new CusKeyboardView.OnKeyChangeListener() {
            @Override
            public void onInput(CharSequence text) {
                if (null != mOnInputChangeListener) {
                    mOnInputChangeListener.onInput(text);
                }
            }

            @Override
            public void onDelete() {

                if (null != mOnInputChangeListener) {
                    mOnInputChangeListener.onDel();
                }
            }
        });


        dialog.setContentView(inflate);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnKeyListener(this);
        return dialog;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        try {
            // 窗口边框
            Window window = getDialog().getWindow();
            window.getDecorView().setPadding(0, 0, 0, 0);

            // 禁止录屏和截屏
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE);

            // 窗口位置
            WindowManager.LayoutParams windowParams = window.getAttributes();
            windowParams.dimAmount = 0f;
            windowParams.gravity = Gravity.BOTTOM;

            // 设置局部焦点模式
            window.setFlags(WindowManager.LayoutParams.FLAG_LOCAL_FOCUS_MODE, WindowManager.LayoutParams.FLAG_LOCAL_FOCUS_MODE);
//        window.setFlags(WindowManager.LayoutParams.FLAG_LOCAL_FOCUS_MODE, WindowManager.LayoutParams.FLAG_LOCAL_FOCUS_MODE);

            window.setAttributes(windowParams);
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            // window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);

//        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
//        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        } catch (Exception e) {
        }

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        CusUtil.log("CusKeyboardDialog -> onViewCreated ->");


    }

    @Override
    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
        CusUtil.log("CusKeyboardDialog -> onKey -> action = " + event.getAction() + ", keyCode = " + event.getKeyCode());

        // 返回键
        if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            dismiss();
        }

        try {
            View keyboardView = getDialog().findViewById(R.id.moudle_safe_id_keyboard);
            keyboardView.dispatchKeyEvent(event);
        } catch (Exception e) {
        }

        return true;
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

    /*************/

    private OnInputChangeListener mOnInputChangeListener;

    public void setOnInputChangeListener(OnInputChangeListener listener) {
        this.mOnInputChangeListener = listener;
    }

    public interface OnInputChangeListener {

        void onInput(CharSequence text);

        void onDel();
    }
}