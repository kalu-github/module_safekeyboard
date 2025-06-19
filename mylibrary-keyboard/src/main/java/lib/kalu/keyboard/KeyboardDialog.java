package lib.kalu.keyboard;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
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
import android.view.Window;
import android.view.WindowManager;

import java.lang.ref.WeakReference;

@SuppressLint("ValidFragment")
final class KeyboardDialog extends DialogFragment implements DialogInterface.OnKeyListener {

    public static final String BUNDLE_SUPPORT_LANGUAGES = "bundle_support_languages";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        //
        Activity activity = getActivity();
        Context context = activity.getApplicationContext();

        //
        View inflate = LayoutInflater.from(context).inflate(R.layout.res_keyboard_layout_dialog, null);
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

        //
        AlertDialog.Builder builder = new AlertDialog.Builder(new WeakReference<>(activity).get());
        builder.setView(inflate);
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnKeyListener(this);

        //
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window window = dialog.getWindow();
        //去掉dialog默认的padding
        window.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        //设置dialog的位置在底部
        lp.gravity = Gravity.BOTTOM;
        //设置dialog的动画
        lp.windowAnimations = R.style.Res_Keyboadrd_Style;
        // 设置透明度
        lp.dimAmount = 0f;
        window.setAttributes(lp);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return dialog;
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