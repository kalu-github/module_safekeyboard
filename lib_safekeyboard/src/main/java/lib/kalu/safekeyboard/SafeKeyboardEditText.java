package lib.kalu.safekeyboard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Keep;

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
        setClickable(true);
        setLongClickable(false);
        setSelectAllOnFocus(false);
        setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        setCursorVisible(true);
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

                Resources res = getResources();

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

            // 回调点击事件
            callOnClick();
        } else {

            // 强制关闭安全键盘
            SafeKeyboardFragmentManager.forceDismiss();
        }
    }

    public String getReal() {
        Object tag = getTag(R.id.safe_keyboard_id_input_text);
        if (null == tag)
            return null;

        return tag.toString();
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
