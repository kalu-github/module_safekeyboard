package lib.kalu.safekeyboard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.lang.reflect.Method;

/**
 * 禁止Edittext弹出软键盘并且使光标正常显示
 */
@SuppressLint("AppCompatCustomView")
public final class SafeKeyboardEditText extends EditText {

    public SafeKeyboardEditText(Context context) {
        super(context);
        setClickable(true);
        setLongClickable(false);
        disableShowSoftInput();
    }

    public SafeKeyboardEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        setClickable(true);
        setLongClickable(false);
        disableShowSoftInput();
    }

    public SafeKeyboardEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setClickable(true);
        setLongClickable(false);
        disableShowSoftInput();
    }

    public SafeKeyboardEditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setClickable(true);
        setLongClickable(false);
        disableShowSoftInput();
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        SafeKeyboardLogUtil.log("onFocusChanged => focused = " + focused);

        if (focused) {
            // getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

            // step1
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getWindowToken(), 0);

            // step2
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    callOnClick();
                }
            }, 80);
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
