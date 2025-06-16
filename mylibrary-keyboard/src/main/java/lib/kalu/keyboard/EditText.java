package lib.kalu.keyboard;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;


public class EditText extends androidx.appcompat.widget.AppCompatEditText {

    public EditText(Context context) {
        super(context);
        init();
    }

    public EditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setSingleLine(true);
        setClickable(true);
        setLongClickable(false);
        setCursorVisible(true);
        setSelectAllOnFocus(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ColorDrawable colorDrawable = new ColorDrawable(Color.TRANSPARENT);
            setTextSelectHandle(colorDrawable);
            setTextSelectHandleLeft(colorDrawable);
            setTextSelectHandleRight(colorDrawable);
        } else {
            try {
                Field fEditor = EditText.class.getDeclaredField("mEditor");
                fEditor.setAccessible(true);
                Object editor = fEditor.get(this);

                Field fSelectHandleLeft = editor.getClass().getDeclaredField("mSelectHandleLeft");
                Field fSelectHandleRight =
                        editor.getClass().getDeclaredField("mSelectHandleRight");
                Field fSelectHandleCenter =
                        editor.getClass().getDeclaredField("mSelectHandleCenter");

                fSelectHandleLeft.setAccessible(true);
                fSelectHandleRight.setAccessible(true);
                fSelectHandleCenter.setAccessible(true);

                ColorDrawable colorDrawable = new ColorDrawable(Color.TRANSPARENT);
                fSelectHandleLeft.set(editor, colorDrawable);
                fSelectHandleRight.set(editor, colorDrawable);
                fSelectHandleCenter.set(editor, colorDrawable);
            } catch (final Exception ignored) {
            }
        }

        if (android.os.Build.VERSION.SDK_INT <= 10) {
            setInputType(InputType.TYPE_NULL);
        } else {
            Class<android.widget.EditText> cls = android.widget.EditText.class;
            Method method;
            try {
                method = cls.getMethod("setShowSoftInputOnFocus", boolean.class);
                method.setAccessible(true);
                method.invoke(this, false);
            } catch (Exception e) {
            }

            try {
                method = cls.getMethod("setSoftInputShownOnFocus", boolean.class);
                method.setAccessible(true);
                method.invoke(this, false);
            } catch (Exception e) {
            }
        }

        //
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                //
                show(null);
            }
        });
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);
        updateSelection();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            remove();
//           // show();
//            updateSelection();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);

        if (focused) {
            // 关闭系统键盘
            InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
            //
            updateSelection();
        }
    }

    private void show(String language) {
        try {
            Activity activity = LogUtil.getCurActivity(getContext());
            if (null == activity)
                throw new Exception("warning: activity null");
            FragmentManager fragmentManager = activity.getFragmentManager();
            if (null == fragmentManager)
                throw new Exception("warning: fragmentManager null");
            Bundle bundle = new Bundle();
            bundle.putString(KeyboardDialog.BUNDLE_LANGUAGE, language);
            KeyboardDialog dialog = new KeyboardDialog();
            dialog.setArguments(bundle);
            dialog.show(fragmentManager, KeyboardDialog.TAG);
            dialog.setOnInputChangeListener(new KeyboardDialog.OnInputChangeListener() {
                @Override
                public void onAppend(CharSequence text) {
                    add(text);
                }

                @Override
                public void onDelete() {
                    del();
                }

                @Override
                public void onDismiss() {
                }
            });
        } catch (Exception e) {
            LogUtil.log("CusEditText -> show -> Exception " + e.getMessage());
        }
    }

    private void del() {
        try {
            Editable editable = getEditableText();
            if (null == editable)
                throw new Exception("error: editable null");
            int length = editable.length();
            if (length == 0)
                throw new Exception("warining: length == 0");
            int start = length - 1;
            editable.delete(start, length);
        } catch (Exception e) {
            LogUtil.log("CusEditText -> del -> Exception " + e.getMessage());
        }
    }

    private void add(CharSequence text) {
        try {
            Editable editable = getEditableText();
            if (null == editable)
                throw new Exception("error: editable null");
            editable.append(text);
        } catch (Exception e) {
            LogUtil.log("CusEditText -> add -> Exception " + e.getMessage());
        }
    }

    private void updateSelection() {
        try {
            Editable editable = getEditableText();
            if (null == editable)
                throw new Exception("error: editable null");
            int length = editable.length();
            if (length == 0)
                throw new Exception("warning: length == 0");
            setSelection(length);
        } catch (Exception e) {
            LogUtil.log("CusEditText -> updateSelection -> Exception " + e.getMessage());
        }
    }
}
