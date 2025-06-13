package lib.kalu.input;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.text.Editable;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * description: 禁止Edittext弹出软键盘并且使光标正常显示
 * created by kalu on 2021-01-15
 */
@SuppressLint("AppCompatCustomView")
public final class CusEditText extends android.widget.EditText {

    public CusEditText(Context context) {
        super(context);
        init();
    }

    public CusEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CusEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setLines(1);
        setMaxLines(1);
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
                Field fEditor = CusEditText.class.getDeclaredField("mEditor");
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
                Toast.makeText(getContext(), "onClick", Toast.LENGTH_SHORT).show();
                //
                start();
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
//        if (keyCode == KeyEvent.KEYCODE_SPACE) {
//           // show();
//            updateSelection();
//        }
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


    private void close() {
        try {
            Activity activity = CusUtil.getCurActivity(getContext());
            if (null == activity)
                throw new Exception("warning: activity null");
            FragmentManager fragmentManager = activity.getFragmentManager();
            Fragment fragment = fragmentManager.findFragmentByTag("TvKeyboardDialog");
            if (null == fragment)
                throw new Exception("warning: fragment null");
            ((CusKeyboardDialog) fragment).setOnInputChangeListener(null);
            ((CusKeyboardDialog) fragment).dismiss();
            fragmentManager.beginTransaction().show(fragment).commit();
        } catch (Exception e) {
            CusUtil.log("CusEditText -> close -> Exception " + e.getMessage());
        }
    }

    private void start() {
        try {
            // 显示自定义键盘
            Activity activity = CusUtil.getCurActivity(getContext());
            if (null == activity)
                throw new Exception("warning: activity null");
            FragmentManager fragmentManager = activity.getFragmentManager();
            Fragment fragment = fragmentManager.findFragmentByTag("TvKeyboardDialog");
            if (null != fragment) {
                fragmentManager.beginTransaction().show(fragment).commit();
            } else {
                CusKeyboardDialog dialog = new CusKeyboardDialog();
                dialog.show(fragmentManager, "TvKeyboardDialog");
                dialog.setOnInputChangeListener(new CusKeyboardDialog.OnInputChangeListener() {
                    @Override
                    public void onInput(CharSequence text) {
                        add(text);
                    }

                    @Override
                    public void onDel() {
                        del();
                    }

                    @Override
                    public void onDismiss() {
                        close();
                    }
                });
            }
        } catch (Exception e) {
            CusUtil.log("CusEditText -> start -> Exception " + e.getMessage());
        }
    }

    private void del() {
        try {
            Editable editable = getEditableText();
            int length = editable.length();
            if (length == 0)
                throw new Exception("warining: length == 0");
            if (length == 1) {
                setText("");
            } else {
                String string = editable.toString();
                String substring = string.substring(0, length - 1);
                setText(substring);
            }
        } catch (Exception e) {
            CusUtil.log("CusEditText -> del -> Exception " + e.getMessage());
        }
    }

    private void add(CharSequence text) {
        try {

            Editable editable = getEditableText();
            String string = editable.toString();
            StringBuilder builder = new StringBuilder();
            builder.append(string);
            builder.append(text);
            setText(builder);
        } catch (Exception e) {
            CusUtil.log("CusEditText -> add -> Exception " + e.getMessage());
        }
    }

    private void updateSelection() {
        try {
            Editable editable = getEditableText();
            int length = editable.length();
            if (length == 0)
                throw new Exception("warning: length == 0");
            String string = editable.toString();
            setSelection(length);
        } catch (Exception e) {
            CusUtil.log("CusEditText -> updateSelection -> Exception " + e.getMessage());
        }
    }
}
