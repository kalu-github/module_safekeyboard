package lib.kalu.safekeyboard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.text.Editable;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * description: 禁止Edittext弹出软键盘并且使光标正常显示
 * created by kalu on 2021-01-15
 */
@SuppressLint("AppCompatCustomView")
@Keep
public final class SafeKeyboardEditText extends EditText {

    public SafeKeyboardEditText(Context context) {
        super(context);
        init();
    }

    public SafeKeyboardEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SafeKeyboardEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public SafeKeyboardEditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setLines(1);
        setMaxLines(1);
        setSingleLine(true);
        setClickable(true);
        setLongClickable(false);
        setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        setCursorVisible(true);
        setSelectAllOnFocus(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ColorDrawable colorDrawable = new ColorDrawable(Color.TRANSPARENT);
            setTextSelectHandle(colorDrawable);
            setTextSelectHandleLeft(colorDrawable);
            setTextSelectHandleRight(colorDrawable);
        } else {
            try {
                Field fEditor = SafeKeyboardEditText.class.getDeclaredField("mEditor");
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
        disableShowSoftInput();
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);
        try {
            setSelection(getEditableText().toString().length());
        } catch (Exception e) {
        }
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        SafeKeyboardLogUtil.log("onFocusChanged => focused = " + focused + ", ids = " + getId());

        if (focused) {
            // getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

            // 强制关闭安全键盘
            SafeKeyboardDialog dialog = SafeKeyboardFragmentManager.get();
            if (null != dialog && null != dialog.getArguments() && getId() != dialog.getArguments().getInt(SafeKeyboardDialog.BUNDLE_EDITTEXT_ID, -1)) {
                SafeKeyboardFragmentManager.forceDismiss();
            }

            // 强制关闭系统键盘
            InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);

            // 强制清空内容
            getEditableText().clear();
            setTag(R.id.safe_keyboard_id_input_text, null);

            // 回调点击事件
            callOnClick();
        } else {

            // 强制关闭安全键盘
            SafeKeyboardFragmentManager.forceDismiss();
        }
    }

    void delete() {

      //  SafeKeyboardLogUtil.log("delete =>");
        if (null == getEditableText() || getEditableText().length() <= 0)
            return;

        Editable editable = getEditableText();
        int start = getSelectionStart();
        int end = getSelectionEnd();
        boolean isLast;
        if (null == editable || editable.length() <= 0) {
            isLast = true;
        } else if (start == end && start == editable.length()) {
            isLast = true;
        } else {
            isLast = false;
        }
      //  SafeKeyboardLogUtil.log("delete => start = " + start + ", end = " + end);

        // 显示
        String textTemp = null;
        if (null != editable) {
            textTemp = editable.toString();
        }
        if (null != textTemp && textTemp.length() > 0) {
            int abs = isLast ? editable.length() - 1 : editable.length() - Math.abs(end - start);
            String substring = textTemp.substring(0, abs);
            setText(substring);
        }
        try {
            setSelection(getEditableText().length());
        } catch (Exception e) {
            SafeKeyboardLogUtil.log("delete => " + e.getMessage());
        }

        // 真实
        String real = null;
        Object tag = getTag(R.id.safe_keyboard_id_input_text);

        setTag(R.id.safe_keyboard_id_input_text, null);
        if (null != tag) {
            real = tag.toString();
        }
        try {
            JSONArray del = new JSONArray();
            JSONArray jsonArray = new JSONArray(real);
            for (int i = 0; i < jsonArray.length(); i++) {

                if (!isLast && i == start - 1)
                    continue;

                if (isLast && i == jsonArray.length() - 1)
                    continue;

                String temp = jsonArray.optString(i, null);
                if (null == temp || temp.length() <= 0)
                    continue;

                del.put(temp);
            }

            String s = del.toString();
            setTag(R.id.safe_keyboard_id_input_text, s);

        } catch (Exception e) {
            SafeKeyboardLogUtil.log("delete => " + e.getMessage());
        }
    }

    void input(@NonNull int text, @NonNull int real) {
       // SafeKeyboardLogUtil.log("input => text = " + text + ", real = " + real);

        // 显示
        String textTemp = null;
        Editable editable = getEditableText();
        if (null != editable) {
            textTemp = editable.toString();
        }
        if (null == textTemp || textTemp.length() <= 0) {
            setText(String.valueOf(text));
        } else {
            setText(textTemp + text);
        }
        try {
            setSelection(getEditableText().length());
        } catch (Exception e) {
        }

        // 真实
        String realTemp;
        Object tag = getTag(R.id.safe_keyboard_id_input_text);
        setTag(R.id.safe_keyboard_id_input_text, null);
        if (null != tag) {
            realTemp = tag.toString();
        } else {
            JSONArray array = new JSONArray();
            realTemp = array.toString();
        }
     //   SafeKeyboardLogUtil.log("input => realTemp = " + realTemp);
        try {
            JSONArray jsonArray = new JSONArray(realTemp);
            String value = SafeKeyboardUtil.encryptCode(real, SafeKeyboardView.ENCRYPTION_KEY, SafeKeyboardView.ENCRYPTION_IV);
            jsonArray.put(value);
            String s = jsonArray.toString();
            setTag(R.id.safe_keyboard_id_input_text, s);
        } catch (Exception e) {
            SafeKeyboardLogUtil.log("input => " + e.getMessage());
        }
    }

    public String getReal() {
        Object tag = getTag(R.id.safe_keyboard_id_input_text);
        if (null == tag)
            return null;

        String real = tag.toString();
        try {
            StringBuilder builder = new StringBuilder();
            JSONArray jsonArray = new JSONArray(real);
            for (int i = 0; i < jsonArray.length(); i++) {
                String temp = jsonArray.optString(i, null);
           //     SafeKeyboardLogUtil.log("getReal => i = " + i + ", text " + temp);
                if (null == temp || temp.length() <= 0)
                    continue;

                String decrypt = SafeKeyboardUtil.decrypt(temp, SafeKeyboardView.ENCRYPTION_KEY, SafeKeyboardView.ENCRYPTION_IV);
            //    SafeKeyboardLogUtil.log("getReal => i = " + i + ", decrypt " + decrypt);
                if (null == decrypt || decrypt.length() <= 0)
                    continue;

                String value = String.valueOf((char) Integer.parseInt(decrypt));
                SafeKeyboardLogUtil.log("getReal => i = " + i + ", value " + value);
                builder.append(value);
            }

            String s = builder.toString();
            return s;

        } catch (Exception e) {
            SafeKeyboardLogUtil.log("getReal => " + e.getMessage());
            return null;
        }
    }

    private final void disableShowSoftInput() {
        if (android.os.Build.VERSION.SDK_INT <= 10) {
            setInputType(InputType.TYPE_NULL);
        } else {
            Class<EditText> cls = EditText.class;
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
    }
}
