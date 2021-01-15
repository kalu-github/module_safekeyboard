package lib.kalu.safekeyboard;

import android.app.Activity;
import android.app.Dialog;
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
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.lang.ref.WeakReference;

@Keep
public class SafeKeyboardDialog extends DialogFragment implements DialogInterface.OnKeyListener {

    public static String TAG = "lib.kalu.safekeyboard.safekeyboarddialog";

    @Override
    public void onStart() {
        if (null != getActivity().getCurrentFocus() && null != getActivity().getCurrentFocus().getWindowToken()) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
        }
        super.onStart();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        WeakReference<Activity> weakReference = new WeakReference<>(getActivity());
        Dialog dialog = new Dialog(weakReference.get()) {
            @Override
            protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setStyle(STYLE_NORMAL, R.style.safeKeyboardTheme);
            }
        };

        dialog.setContentView(R.layout.keyboard_dialog);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setOnKeyListener(this);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // 窗口边框
        Window window = getDialog().getWindow();
        window.getDecorView().setPadding(0, 0, 0, 0);

        // 窗口位置
        WindowManager.LayoutParams windowParams = window.getAttributes();
        windowParams.dimAmount = 0f;
        windowParams.gravity = Gravity.BOTTOM;

        window.setAttributes(windowParams);
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        // window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);

        // 监听
        getDialog().findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SafeKeyboardView safe = getDialog().findViewById(R.id.safe);
                String parse = safe.parse();
                Toast.makeText(getContext(), parse, Toast.LENGTH_SHORT).show();

                dismiss();
            }
        });

        return super.onCreateView(inflater, container, savedInstanceState);
    }


    @Override
    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
        return !isCancelable() && keyCode == KeyEvent.KEYCODE_BACK;
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
}
