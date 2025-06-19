package lib.kalu.keyboard;

import android.annotation.SuppressLint;
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
import java.util.ArrayList;

@SuppressLint("ValidFragment")
final class KeyboardDialog extends DialogFragment implements DialogInterface.OnKeyListener {

    public static final String TAG = "KeyboardDialog";
    public static final String BUNDLE_SUPPORT_LANGUAGES = "bundle_support_languages";

    @Override
    public void onStart() {
        super.onStart();
        // 双重确认，确保动画被禁用
        Dialog dialog = getDialog();
        if (dialog != null) {
            Window window = dialog.getWindow();
            if (window != null) {
                window.setWindowAnimations(0);
                // 可选：调整窗口属性，避免系统默认动画
                window.getAttributes().windowAnimations = 0;
                window.setBackgroundDrawableResource(android.R.color.transparent);
            }
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // LogUtil.log("CusKeyboardDialog -> onCreateDialog ->");


        View inflate = LayoutInflater.from(getActivity()).inflate(R.layout.res_keyboard_layout_dialog, null);
        KeyboardView keyboardView = inflate.findViewById(R.id.moudle_safe_id_keyboard);
        keyboardView.setTag(getArguments().getStringArrayList(BUNDLE_SUPPORT_LANGUAGES));
        keyboardView.setOnKeyChangeListener(new KeyboardView.OnKeyChangeListener() {
            @Override
            public void onInput(CharSequence text) {
                if (null != mOnInputChangeListener) {
                    mOnInputChangeListener.onAppend(text);
                }
            }

            @Override
            public void onDelete() {
                if (null != mOnInputChangeListener) {
                    mOnInputChangeListener.onDelete();
                }
            }
        });

        Dialog dialog = new Dialog(new WeakReference<>(getActivity()).get(), R.style.Res_Keyboadrd_Style);
        dialog.setContentView(inflate);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnKeyListener(this);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LogUtil.log("CusKeyboardDialog -> onCreateView ->");
        try {

            // 窗口边框
            Window window = getDialog().getWindow();
            window.getDecorView().setPadding(0, 0, 0, 0);

            // 动画残留问题：如果禁用动画后仍有轻微过渡效果，可能是系统或设备特定的行为，可以尝试结合以下属性进一步消除：
            window.getAttributes().windowAnimations = 0;

            // 禁止录屏和截屏
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE);

            // 窗口位置
            WindowManager.LayoutParams windowParams = window.getAttributes();
            windowParams.dimAmount = 0f;
            windowParams.gravity = Gravity.BOTTOM;

            /**
             * FLAG_NOT_FOCUSABLE：窗口完全不接收焦点，点击窗口内的元素也不会触发焦点变化。
             * FLAG_ALT_FOCUSABLE_IM：控制输入法（软键盘）的焦点行为，与输入法窗口交互时使用。
             * FLAG_LOCAL_FOCUS_MODE：窗口内部的焦点独立管理，不影响其他窗口。
             */
            window.setFlags(WindowManager.LayoutParams.FLAG_LOCAL_FOCUS_MODE, WindowManager.LayoutParams.FLAG_LOCAL_FOCUS_MODE);

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
    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
        LogUtil.log("CusKeyboardDialog -> onKey -> action = " + event.getAction() + ", keyCode = " + event.getKeyCode());

        // 返回键
        if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
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

    /*************/

    private OnInputChangeListener mOnInputChangeListener;

    public void setOnInputChangeListener(OnInputChangeListener listener) {
        this.mOnInputChangeListener = listener;
    }

    public interface OnInputChangeListener {

        void onAppend(CharSequence text);

        void onDelete();
    }
}