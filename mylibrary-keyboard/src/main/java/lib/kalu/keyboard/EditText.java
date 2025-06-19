package lib.kalu.keyboard;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;


public class EditText extends androidx.appcompat.widget.AppCompatEditText {

    public ArrayList<String> initSupportLanguages() {
        return new ArrayList<String>() {{
            add("英语");
            add("en");
            add("西班牙");
            add("es");
            add("巴西葡语");
            add("pt_BR");
        }};
    }

    private int NA = -100;

    private boolean mTextBold;
    // interface
    private boolean mInterfaceTop;
    private boolean mInterfaceBottom;
    private boolean mInterfaceLeft;
    private boolean mInterfaceRight;

    // rate
    private float mRateWidth;
    private float mRateWidthFocused;
    private float mRateWidthHovered;
    private float mRateHeight;
    private float mRateHeightFocused;
    private float mRateHeightHovered;


    private float mFocusScale = 1f;
    private int mFocusScaleDuration = 0;

    private int mStrokeWidth = 0;
    private int mStrokeColor = -1;
    private int mStrokeColorHovered = -1;
    private int mStrokeColorFocused = -1;

    // corner
    private int mCorner = -1;
    private int mCornerFocused = -1;
    private int mCornerHovered = -1;
    private int mCornerTopLeft = -1;
    private int mCornerTopLeftFocused = -1;
    private int mCornerTopLeftHovered = -1;
    private int mCornerTopRight = -1;
    private int mCornerTopRightFocused = -1;
    private int mCornerTopRightHovered = -1;
    private int mCornerBottomLeft = -1;
    private int mCornerBottomLeftFocused = -1;
    private int mCornerBottomLeftHovered = -1;
    private int mCornerBottomRight = -1;
    private int mCornerBottomRightFocused = -1;
    private int mCornerBottomRightHovered = -1;

    private int mBackgroundMode = 0;
    // 单色
    private int mBackgroundColor = -1;
    private int mBackgroundColorHovered = -1;
    private int mBackgroundColorFocused = -1;
    // 渐变色
    private int mBackgroundColorArray = -1;
    private int mBackgroundColorArrayHovered = -1;
    private int mBackgroundColorArrayFocused = -1;

    // 边界
    private int mBackgroundInsetLeft = 0;
    private int mBackgroundInsetTop = 0;
    private int mBackgroundInsetRight = 0;
    private int mBackgroundInsetBottom = 0;

    // text
    private int mTextColor = NA;
    private int mTextColorHovered = NA;
    private int mTextColorFocused = NA;

    // drawable
    private int mTextDrawableLeft = 0;
    private int mTextDrawableLeftHovered = 0;
    private int mTextDrawableLeftFocused = 0;
    private int mTextDrawableLeftChecked = 0;
    private int mTextDrawableLeftWidth = 0;
    private int mTextDrawableLeftHeight = 0;
    private int mTextDrawableTop = 0;
    private int mTextDrawableTopHovered = 0;
    private int mTextDrawableTopFocused = 0;
    private int mTextDrawableTopChecked = 0;
    private int mTextDrawableTopWidth = 0;
    private int mTextDrawableTopHeight = 0;
    private int mTextDrawableRight = 0;
    private int mTextDrawableRightHovered = 0;
    private int mTextDrawableRightFocused = 0;
    private int mTextDrawableRightChecked = 0;
    private int mTextDrawableRightWidth = 0;
    private int mTextDrawableRightHeight = 0;
    private int mTextDrawableBottom = 0;
    private int mTextDrawableBottomHovered = 0;
    private int mTextDrawableBottomFocused = 0;
    private int mTextDrawableBottomChecked = 0;
    private int mTextDrawableBottomWidth = 0;
    private int mTextDrawableBottomHeight = 0;

    public EditText(Context context) {
        super(context);
        init(context, null);
    }

    public EditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public EditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (mInterfaceLeft && event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT) {
            return true;
        } else if (mInterfaceRight && event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
            return true;
        } else if (mInterfaceTop && event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
            return true;
        } else if (mInterfaceBottom && event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    private void init(Context context, AttributeSet attrs) {
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
                show();
            }
        });


        TypedArray typedArray = null;
        try {
            typedArray = context.obtainStyledAttributes(attrs, R.styleable.EditText);

            mTextBold = typedArray.getBoolean(R.styleable.EditText_et_text_bold, false);

            mInterfaceTop = typedArray.getBoolean(R.styleable.EditText_et_intercept_top, mInterfaceTop);
            mInterfaceBottom = typedArray.getBoolean(R.styleable.EditText_et_intercept_bottom, mInterfaceBottom);
            mInterfaceLeft = typedArray.getBoolean(R.styleable.EditText_et_intercept_left, mInterfaceLeft);
            mInterfaceRight = typedArray.getBoolean(R.styleable.EditText_et_intercept_right, mInterfaceRight);

            mRateWidth = typedArray.getFloat(R.styleable.EditText_et_rate_width, 0f);
            mRateWidthFocused = typedArray.getFloat(R.styleable.EditText_et_rate_width_focused, 0f);
            mRateWidthHovered = typedArray.getFloat(R.styleable.EditText_et_rate_width_hovered, 0f);
            mRateHeight = typedArray.getFloat(R.styleable.EditText_et_rate_height, 0f);
            mRateHeightFocused = typedArray.getFloat(R.styleable.EditText_et_rate_height_focused, 0f);
            mRateHeightHovered = typedArray.getFloat(R.styleable.EditText_et_rate_height_hovered, 0f);

            mFocusScale = typedArray.getFloat(R.styleable.EditText_et_focus_scale, 1f);
            mFocusScaleDuration = typedArray.getInt(R.styleable.EditText_et_focus_scale_duration, 0);

            mStrokeWidth = typedArray.getDimensionPixelOffset(R.styleable.EditText_et_stroke_width, mStrokeWidth);
            mStrokeColor = typedArray.getColor(R.styleable.EditText_et_stroke_color, mStrokeColor);
            mStrokeColorHovered = typedArray.getColor(R.styleable.EditText_et_stroke_color_hovered, mStrokeColorHovered);
            mStrokeColorFocused = typedArray.getColor(R.styleable.EditText_et_stroke_color_focused, mStrokeColorFocused);

            mCorner = typedArray.getDimensionPixelOffset(R.styleable.EditText_et_corner, mCorner);
            mCornerFocused = typedArray.getDimensionPixelOffset(R.styleable.EditText_et_corner_focused, mCornerFocused);
            mCornerHovered = typedArray.getDimensionPixelOffset(R.styleable.EditText_et_corner_hovered, mCornerHovered);
            mCornerTopLeft = typedArray.getDimensionPixelOffset(R.styleable.EditText_et_corner_top_left, mCornerTopLeft);
            mCornerTopLeftFocused = typedArray.getDimensionPixelOffset(R.styleable.EditText_et_corner_top_left_focused, mCornerTopLeftFocused);
            mCornerTopLeftHovered = typedArray.getDimensionPixelOffset(R.styleable.EditText_et_corner_top_left_hovered, mCornerTopLeftHovered);
            mCornerTopRight = typedArray.getDimensionPixelOffset(R.styleable.EditText_et_corner_top_right, mCornerTopRight);
            mCornerTopRightFocused = typedArray.getDimensionPixelOffset(R.styleable.EditText_et_corner_top_right_focused, mCornerTopRightFocused);
            mCornerTopRightHovered = typedArray.getDimensionPixelOffset(R.styleable.EditText_et_corner_top_right_hovered, mCornerTopRightHovered);
            mCornerBottomLeft = typedArray.getDimensionPixelOffset(R.styleable.EditText_et_corner_bottom_left, mCornerBottomLeft);
            mCornerBottomLeftFocused = typedArray.getDimensionPixelOffset(R.styleable.EditText_et_corner_bottom_left_focused, mCornerBottomLeftFocused);
            mCornerBottomLeftHovered = typedArray.getDimensionPixelOffset(R.styleable.EditText_et_corner_bottom_left_hovered, mCornerBottomLeftHovered);
            mCornerBottomRight = typedArray.getDimensionPixelOffset(R.styleable.EditText_et_corner_bottom_right, mCornerBottomRight);
            mCornerBottomRightFocused = typedArray.getDimensionPixelOffset(R.styleable.EditText_et_corner_bottom_right_focused, mCornerBottomRightFocused);
            mCornerBottomRightHovered = typedArray.getDimensionPixelOffset(R.styleable.EditText_et_corner_bottom_right_hovered, mCornerBottomRightHovered);

            // text
            mTextColor = typedArray.getColor(R.styleable.EditText_et_text_color, mTextColor);
            mTextColorHovered = typedArray.getColor(R.styleable.EditText_et_text_color_hovered, mTextColorHovered);
            mTextColorFocused = typedArray.getColor(R.styleable.EditText_et_text_color_focused, mTextColorFocused);

            // drawableLeft
            mTextDrawableLeft = typedArray.getResourceId(R.styleable.EditText_et_text_drawable_left, 0);
            mTextDrawableLeftChecked = typedArray.getResourceId(R.styleable.EditText_et_text_drawable_left_checked, 0);
            mTextDrawableLeftHovered = typedArray.getResourceId(R.styleable.EditText_et_text_drawable_left_hovered, 0);
            mTextDrawableLeftFocused = typedArray.getResourceId(R.styleable.EditText_et_text_drawable_left_focused, 0);
            mTextDrawableLeftWidth = typedArray.getDimensionPixelOffset(R.styleable.EditText_et_text_drawable_left_width, 0);
            mTextDrawableLeftHeight = typedArray.getDimensionPixelOffset(R.styleable.EditText_et_text_drawable_left_height, 0);
            // drawableTop
            mTextDrawableTop = typedArray.getResourceId(R.styleable.EditText_et_text_drawable_top, 0);
            mTextDrawableTopChecked = typedArray.getResourceId(R.styleable.EditText_et_text_drawable_top_checked, 0);
            mTextDrawableTopHovered = typedArray.getResourceId(R.styleable.EditText_et_text_drawable_top_hovered, 0);
            mTextDrawableTopFocused = typedArray.getResourceId(R.styleable.EditText_et_text_drawable_top_focused, 0);
            mTextDrawableTopWidth = typedArray.getDimensionPixelOffset(R.styleable.EditText_et_text_drawable_top_width, 0);
            mTextDrawableTopHeight = typedArray.getDimensionPixelOffset(R.styleable.EditText_et_text_drawable_top_height, 0);
            // drawableRight
            mTextDrawableRight = typedArray.getResourceId(R.styleable.EditText_et_text_drawable_right, 0);
            mTextDrawableRightChecked = typedArray.getResourceId(R.styleable.EditText_et_text_drawable_right_checked, 0);
            mTextDrawableRightHovered = typedArray.getResourceId(R.styleable.EditText_et_text_drawable_right_hovered, 0);
            mTextDrawableRightFocused = typedArray.getResourceId(R.styleable.EditText_et_text_drawable_right_focused, 0);
            mTextDrawableRightWidth = typedArray.getDimensionPixelOffset(R.styleable.EditText_et_text_drawable_right_width, 0);
            mTextDrawableRightHeight = typedArray.getDimensionPixelOffset(R.styleable.EditText_et_text_drawable_right_height, 0);
            // drawableBottom
            mTextDrawableBottom = typedArray.getResourceId(R.styleable.EditText_et_text_drawable_bottom, 0);
            mTextDrawableBottomChecked = typedArray.getResourceId(R.styleable.EditText_et_text_drawable_bottom_checked, 0);
            mTextDrawableBottomHovered = typedArray.getResourceId(R.styleable.EditText_et_text_drawable_bottom_hovered, 0);
            mTextDrawableBottomFocused = typedArray.getResourceId(R.styleable.EditText_et_text_drawable_bottom_focused, 0);
            mTextDrawableBottomWidth = typedArray.getDimensionPixelOffset(R.styleable.EditText_et_text_drawable_bottom_width, 0);
            mTextDrawableBottomHeight = typedArray.getDimensionPixelOffset(R.styleable.EditText_et_text_drawable_bottom_height, 0);

            mBackgroundMode = typedArray.getInt(R.styleable.EditText_et_background_mode, mBackgroundMode);
            // 单色
            mBackgroundColor = typedArray.getColor(R.styleable.EditText_et_background_color, mBackgroundColor);
            mBackgroundColorHovered = typedArray.getColor(R.styleable.EditText_et_background_color_hovered, mBackgroundColorHovered);
            mBackgroundColorFocused = typedArray.getColor(R.styleable.EditText_et_background_color_focused, mBackgroundColorFocused);
            // 渐变色
            mBackgroundColorArray = typedArray.getResourceId(R.styleable.EditText_et_background_color_array, mBackgroundColorArray);
            mBackgroundColorArrayHovered = typedArray.getResourceId(R.styleable.EditText_et_background_color_array_hovered, mBackgroundColorArrayHovered);
            mBackgroundColorArrayFocused = typedArray.getResourceId(R.styleable.EditText_et_background_color_array_focused, mBackgroundColorArrayFocused);
            // 边界
            mBackgroundInsetLeft = typedArray.getDimensionPixelOffset(R.styleable.EditText_et_background_inset_left, mBackgroundInsetLeft);
            mBackgroundInsetTop = typedArray.getDimensionPixelOffset(R.styleable.EditText_et_background_inset_top, mBackgroundInsetTop);
            mBackgroundInsetRight = typedArray.getDimensionPixelOffset(R.styleable.EditText_et_background_inset_right, mBackgroundInsetRight);
            mBackgroundInsetBottom = typedArray.getDimensionPixelOffset(R.styleable.EditText_et_background_inset_bottom, mBackgroundInsetBottom);
        } catch (Exception e) {
        }

        if (null != typedArray) {
            typedArray.recycle();
        }

        // setBackground
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            Drawable drawable = initBackground(mBackgroundMode,
                    mBackgroundColor, mBackgroundColorFocused, mBackgroundColorHovered,
                    mBackgroundColorArray, mBackgroundColorArrayFocused, mBackgroundColorArrayHovered,
                    mBackgroundInsetLeft, mBackgroundInsetTop, mBackgroundInsetRight, mBackgroundInsetBottom,
                    0,
                    -1, -1, -1,
                    mCorner, mCornerFocused, mCornerHovered,
                    mCornerTopLeft, mCornerTopLeftFocused, mCornerTopLeftHovered,
                    mCornerTopRight, mCornerTopRightFocused, mCornerTopRightHovered,
                    mCornerBottomRight, mCornerBottomRightFocused, mCornerBottomRightHovered,
                    mCornerBottomLeft, mCornerBottomLeftFocused, mCornerBottomLeftHovered);
            if (null != drawable) {
                setBackground(drawable);
            }
        }

        // setForeground
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Drawable foreground = initForeground(
                    mBackgroundInsetLeft, mBackgroundInsetTop, mBackgroundInsetRight, mBackgroundInsetBottom,
                    mStrokeWidth,
                    mStrokeColor, mStrokeColorFocused, mStrokeColorHovered,
                    mCorner, mCornerFocused, mCornerHovered,
                    mCornerTopLeft, mCornerTopLeftFocused, mCornerTopLeftHovered,
                    mCornerTopRight, mCornerTopRightFocused, mCornerTopRightHovered,
                    mCornerBottomRight, mCornerBottomRightFocused, mCornerBottomRightHovered,
                    mCornerBottomLeft, mCornerBottomLeftFocused, mCornerBottomLeftHovered);
            if (null != foreground) {
                setForeground(foreground);
            }
        }

        // setTextColor
        ColorStateList textColor = initTextColor(mTextColor, mTextColorFocused, mTextColorHovered);
        if (null != textColor) {
            setTextColor(textColor);
        }

        if (mTextBold) {
            getPaint().setFakeBoldText(true);
            getPaint().setStrokeJoin(Paint.Join.ROUND);
            getPaint().setStrokeCap(Paint.Cap.ROUND);
        }

        // setCompoundDrawables
        for (int i = 0; i < 4; i++) {
            Drawable drawable = initDrawable(
                    i,
                    mTextDrawableLeftWidth, mTextDrawableLeftHeight, mTextDrawableLeft, mTextDrawableLeftHovered, mTextDrawableLeftChecked, mTextDrawableLeftFocused,
                    mTextDrawableTopWidth, mTextDrawableTopHeight, mTextDrawableTop, mTextDrawableTopHovered, mTextDrawableTopChecked, mTextDrawableTopFocused,
                    mTextDrawableRightWidth, mTextDrawableRightHeight, mTextDrawableRight, mTextDrawableRightHovered, mTextDrawableRightChecked, mTextDrawableRightFocused,
                    mTextDrawableBottomWidth, mTextDrawableBottomHeight, mTextDrawableBottom, mTextDrawableBottomHovered, mTextDrawableBottomChecked, mTextDrawableBottomFocused);
            if (null != drawable) {
                Drawable[] drawables = getCompoundDrawables();
                // left
                if (i == 0) {
                    setCompoundDrawables(drawable, drawables[1], drawables[2], drawables[3]);
                }
                // top
                else if (i == 1) {
                    setCompoundDrawables(drawables[0], drawable, drawables[2], drawables[3]);
                }
                // right
                else if (i == 2) {
                    setCompoundDrawables(drawables[0], drawables[1], drawable, drawables[3]);
                }
                // bottom
                else {
                    setCompoundDrawables(drawables[0], drawables[1], drawables[2], drawable);
                }
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        try {
            int specW = measureSpecWidth(widthMeasureSpec, heightMeasureSpec, mRateWidth, mRateWidthFocused, mRateWidthHovered);
            int specH = measureSpecHeight(widthMeasureSpec, heightMeasureSpec, mRateHeight, mRateHeightFocused, mRateHeightHovered);
            super.onMeasure(specW, specH);
        } catch (Exception e) {
        }
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

    private void show() {
        try {
            Activity activity = getCurActivity(getContext());
            if (null == activity)
                throw new Exception("warning: activity null");
            FragmentManager fragmentManager = activity.getFragmentManager();
            if (null == fragmentManager)
                throw new Exception("warning: fragmentManager null");

//            // fix 启动动画
//            FragmentTransaction ft = fragmentManager.beginTransaction();
//            ft.setCustomAnimations(0, 0, 0, 0); // 进入、退出、进入下一个、退出下一个动画都设为0
//            dialog.show(ft, null);

            KeyboardDialog dialog = new KeyboardDialog();
            Bundle bundle = new Bundle();
            bundle.putStringArrayList(KeyboardDialog.BUNDLE_SUPPORT_LANGUAGES, initSupportLanguages());
            dialog.setArguments(bundle);
            dialog.show(fragmentManager, null);

            dialog.setOnInputChangeListener(new KeyboardDialog.OnInputChangeListener() {
                @Override
                public void onAppend(CharSequence text) {
                    add(text);
                }

                @Override
                public void onDelete() {
                    del();
                }
            });
        } catch (Exception e) {
            LogUtil.log("EditText -> show -> Exception " + e.getMessage());
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
            LogUtil.log("EditText -> del -> Exception " + e.getMessage());
        }
    }

    private void add(CharSequence text) {
        try {
            Editable editable = getEditableText();
            if (null == editable)
                throw new Exception("error: editable null");
            editable.append(text);
        } catch (Exception e) {
            LogUtil.log("EditText -> add -> Exception " + e.getMessage());
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
            LogUtil.log("EditText -> updateSelection -> Exception " + e.getMessage());
        }
    }

    private static Activity getCurActivity(Context context) {
        try {
            if (context instanceof Activity) {
                return (Activity) context;
            } else if (context instanceof ContextWrapper) {
                return getCurActivity(((ContextWrapper) context).getBaseContext());
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    /***************/

    private Drawable initBackground(int backgroundMode,
                                    int backgroundColor, int backgroundColorFocused, int backgroundColorHovered,
                                    int backgroundColorArray, int backgroundColorArrayFocused, int backgroundColorArrayHovered,
                                    int backgroundInsetLeft, int backgroundInsetTop, int backgroundInsetRight, int backgroundInsetBottom,
                                    int strokeWidth,
                                    int strokeColor, int strokeColorFocused, int strokeColorHoverd,
                                    int corner, int cornerFocused, int cornerHoverd,
                                    int cornerTopLeft, int cornerTopLeftFocused, int cornerTopLeftHoverd,
                                    int cornerTopRight, int cornerTopRightFocused, int cornerTopRightHoverd,
                                    int cornerBottomRight, int cornerBottomRightFocused, int cornerBottomRightHoverd,
                                    int cornerBottomLeft, int cornerBottomLeftFocused, int cornerBottomLeftHoverd) {


        try {

            StateListDrawable listDrawable = null;

            // focused hovered normal
            for (int i = 0; i < 3; i++) {

                int bColor;
                int sColor;
                boolean gradient;

                int cornetTL;
                int cornetTR;
                int cornetBL;
                int cornetBR;

                // focused
                if (i == 0) {
                    sColor = strokeColorFocused;
                    bColor = (backgroundColorArrayFocused != -1 ? backgroundColorArrayFocused : backgroundColorFocused);
                    gradient = backgroundColorArrayFocused != -1;
                    cornetTL = cornerTopLeftFocused != -1 ? cornerTopLeftFocused : cornerTopLeft;
                    cornetTR = cornerTopRightFocused != -1 ? cornerTopRightFocused : cornerTopRight;
                    cornetBL = cornerBottomLeftFocused != -1 ? cornerBottomLeftFocused : cornerBottomLeft;
                    cornetBR = cornerBottomRightFocused != -1 ? cornerBottomRightFocused : cornerBottomRight;
                    if (cornetTL == -1) {
                        cornetTL = cornerFocused;
                    }
                    if (cornetTR == -1) {
                        cornetTR = cornerFocused;
                    }
                    if (cornetBL == -1) {
                        cornetBL = cornerFocused;
                    }
                    if (cornetBR == -1) {
                        cornetBR = cornerFocused;
                    }
                }
                // hovered
                else if (i == 1) {
                    sColor = strokeColorHoverd;
                    bColor = (backgroundColorArrayHovered != -1 ? backgroundColorArrayHovered : backgroundColorHovered);
                    gradient = backgroundColorArrayHovered != -1;
                    cornetTL = cornerTopLeftHoverd != -1 ? cornerTopLeftHoverd : cornerTopLeft;
                    cornetTR = cornerTopRightHoverd != -1 ? cornerTopRightHoverd : cornerTopRight;
                    cornetBL = cornerBottomLeftHoverd != -1 ? cornerBottomLeftHoverd : cornerBottomLeft;
                    cornetBR = cornerBottomRightHoverd != -1 ? cornerBottomRightHoverd : cornerBottomRight;
                    if (cornetTL == -1) {
                        cornetTL = cornerHoverd;
                    }
                    if (cornetTR == -1) {
                        cornetTR = cornerHoverd;
                    }
                    if (cornetBL == -1) {
                        cornetBL = cornerHoverd;
                    }
                    if (cornetBR == -1) {
                        cornetBR = cornerHoverd;
                    }
                }
                // normal
                else {
                    sColor = strokeColor;
                    bColor = (backgroundColorArray != -1 ? backgroundColorArray : backgroundColor);
                    gradient = backgroundColorArray != -1;
                    cornetTL = cornerTopLeft;
                    cornetTR = cornerTopRight;
                    cornetBL = cornerBottomLeft;
                    cornetBR = cornerBottomRight;
                    if (cornetTL == -1) {
                        cornetTL = corner;
                    }
                    if (cornetTR == -1) {
                        cornetTR = corner;
                    }
                    if (cornetBL == -1) {
                        cornetBL = corner;
                    }
                    if (cornetBR == -1) {
                        cornetBR = corner;
                    }
                }

//                if (this instanceof TextView) {
//                    LogUtil.log("CornerImpl -> text = " + ((TextView) this).getText() + ", i = " + i);
//                    LogUtil.log("CornerImpl -> sColor = " + sColor);
//                    LogUtil.log("CornerImpl -> bColor = " + bColor);
//                }
//
//                if (this instanceof CornerRelativeLayout) {
//                    LogUtil.log("CornerImpl -> text = " + "CornerRelativeLayout" + ", i = " + i);
//                    LogUtil.log("CornerImpl -> sColor = " + sColor);
//                    LogUtil.log("CornerImpl -> bColor = " + bColor);
//                }

                // fix
                if (sColor != -1 && bColor == -1 && backgroundColorArray != -1) {
                    bColor = backgroundColorArray;
                    gradient = true;
                }
                if (sColor != -1 && bColor == -1 && backgroundColor != -1) {
                    bColor = backgroundColor;
                }
//                if (sColor != -1 && bColor == -1) {
//                    bColor = Color.TRANSPARENT;
//                }
                if (bColor == -1)
                    continue;

                //
                Drawable drawable;
                if (gradient) {
                    GradientDrawable.Orientation orientation;
                    if (backgroundMode == 1) {
                        orientation = GradientDrawable.Orientation.TOP_BOTTOM;
                    } else if (backgroundMode == 2) {
                        orientation = GradientDrawable.Orientation.TR_BL;
                    } else if (backgroundMode == 3) {
                        orientation = GradientDrawable.Orientation.RIGHT_LEFT;
                    } else if (backgroundMode == 4) {
                        orientation = GradientDrawable.Orientation.BR_TL;
                    } else if (backgroundMode == 5) {
                        orientation = GradientDrawable.Orientation.BOTTOM_TOP;
                    } else if (backgroundMode == 6) {
                        orientation = GradientDrawable.Orientation.BL_TR;
                    } else if (backgroundMode == 7) {
                        orientation = GradientDrawable.Orientation.LEFT_RIGHT;
                    } else {
                        orientation = GradientDrawable.Orientation.TL_BR;
                    }
                    @SuppressLint("ResourceType")
                    int[] ints = ((View) this).getResources().getIntArray(bColor);
                    drawable = new GradientDrawable(orientation, ints);
                } else {
                    drawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{bColor, bColor});
                }

                // setStroke
                if (strokeWidth > 0 && sColor != -1) {
                    ((GradientDrawable) drawable).setStroke(strokeWidth, sColor);
                }

                // setCornerRadii
                if (cornetTL == -1) {
                    cornetTL = corner;
                }
                if (cornetTR == -1) {
                    cornetTR = corner;
                }
                if (cornetBL == -1) {
                    cornetBL = corner;
                }
                if (cornetBR == -1) {
                    cornetBR = corner;
                }
                if (cornetTL > 0 || cornetTR > 0 || cornetBL > 0 || cornetBR > 0) {
                    float[] radii = {cornetTL, cornetTL, cornetTR, cornetTR, cornetBR, cornetBR, cornetBL, cornetBL};
                    ((GradientDrawable) drawable).setCornerRadii(radii);
                }

                // inset
                if (backgroundInsetLeft > 0 || backgroundInsetTop > 0 || backgroundInsetRight > 0 || backgroundInsetBottom > 0) {
                    LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{drawable});
                    layerDrawable.setLayerInset(0, backgroundInsetLeft, backgroundInsetTop, backgroundInsetRight, backgroundInsetBottom);
                    drawable = layerDrawable;
                }

                //
                if (null == listDrawable) {
                    listDrawable = new StateListDrawable();
                }

                //
                if (i == 0) {
//                    if (this instanceof TextView) {
//                        LogUtil.log("CornerImpl -> state_focused -> drawable = " + drawable);
//                    }
                    listDrawable.addState(new int[]{android.R.attr.state_focused}, drawable);
                } else if (i == 1) {
//                    if (this instanceof TextView) {
//                        LogUtil.log("CornerImpl -> state_hovered -> drawable = " + drawable);
//                    }
                    listDrawable.addState(new int[]{android.R.attr.state_hovered}, drawable);
                } else {
//                    if (this instanceof TextView) {
//                        LogUtil.log("CornerImpl -> state_default -> drawable = " + drawable);
//                    }
                    listDrawable.addState(new int[]{}, drawable);
                }
            }

            if (null == listDrawable)
                throw new Exception("error: listDrawable null");

            return listDrawable;

        } catch (Exception e) {
            return null;
        }
    }

    private Drawable initForeground(
            int backgroundInsetLeft, int backgroundInsetTop, int backgroundInsetRight, int backgroundInsetBottom,
            int strokeWidth,
            int strokeColor, int strokeColorFocused, int strokeColorHoverd,
            int corner, int cornerFocused, int cornerHoverd,
            int cornerTopLeft, int cornerTopLeftFocused, int cornerTopLeftHoverd,
            int cornerTopRight, int cornerTopRightFocused, int cornerTopRightHoverd,
            int cornerBottomRight, int cornerBottomRightFocused, int cornerBottomRightHoverd,
            int cornerBottomLeft, int cornerBottomLeftFocused, int cornerBottomLeftHoverd) {


        try {

            StateListDrawable listDrawable = null;

            // focused hovered normal
            for (int i = 0; i < 3; i++) {

                // fix
                if (strokeWidth <= 0)
                    continue;

                int sColor;
                int cornetTL;
                int cornetTR;
                int cornetBL;
                int cornetBR;

                // focused
                if (i == 0) {
                    sColor = strokeColorFocused;
                    cornetTL = cornerTopLeftFocused != -1 ? cornerTopLeftFocused : cornerTopLeft;
                    cornetTR = cornerTopRightFocused != -1 ? cornerTopRightFocused : cornerTopRight;
                    cornetBL = cornerBottomLeftFocused != -1 ? cornerBottomLeftFocused : cornerBottomLeft;
                    cornetBR = cornerBottomRightFocused != -1 ? cornerBottomRightFocused : cornerBottomRight;
                    if (cornetTL == -1) {
                        cornetTL = cornerFocused;
                    }
                    if (cornetTR == -1) {
                        cornetTR = cornerFocused;
                    }
                    if (cornetBL == -1) {
                        cornetBL = cornerFocused;
                    }
                    if (cornetBR == -1) {
                        cornetBR = cornerFocused;
                    }
                }
                // hovered
                else if (i == 1) {
                    sColor = strokeColorHoverd;
                    cornetTL = cornerTopLeftHoverd != -1 ? cornerTopLeftHoverd : cornerTopLeft;
                    cornetTR = cornerTopRightHoverd != -1 ? cornerTopRightHoverd : cornerTopRight;
                    cornetBL = cornerBottomLeftHoverd != -1 ? cornerBottomLeftHoverd : cornerBottomLeft;
                    cornetBR = cornerBottomRightHoverd != -1 ? cornerBottomRightHoverd : cornerBottomRight;
                    if (cornetTL == -1) {
                        cornetTL = cornerHoverd;
                    }
                    if (cornetTR == -1) {
                        cornetTR = cornerHoverd;
                    }
                    if (cornetBL == -1) {
                        cornetBL = cornerHoverd;
                    }
                    if (cornetBR == -1) {
                        cornetBR = cornerHoverd;
                    }
                }
                // normal
                else {
                    sColor = strokeColor;
                    cornetTL = cornerTopLeft;
                    cornetTR = cornerTopRight;
                    cornetBL = cornerBottomLeft;
                    cornetBR = cornerBottomRight;
                    if (cornetTL == -1) {
                        cornetTL = corner;
                    }
                    if (cornetTR == -1) {
                        cornetTR = corner;
                    }
                    if (cornetBL == -1) {
                        cornetBL = corner;
                    }
                    if (cornetBR == -1) {
                        cornetBR = corner;
                    }
                }

                // fix
                if (sColor == -1)
                    continue;


                //
                Drawable drawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{Color.TRANSPARENT, Color.TRANSPARENT});

                // setStroke
                ((GradientDrawable) drawable).setStroke(strokeWidth, sColor);

                // setCornerRadii
                if (cornetTL == -1) {
                    cornetTL = corner;
                }
                if (cornetTR == -1) {
                    cornetTR = corner;
                }
                if (cornetBL == -1) {
                    cornetBL = corner;
                }
                if (cornetBR == -1) {
                    cornetBR = corner;
                }
                if (cornetTL > 0 || cornetTR > 0 || cornetBL > 0 || cornetBR > 0) {
                    float[] radii = {cornetTL, cornetTL, cornetTR, cornetTR, cornetBR, cornetBR, cornetBL, cornetBL};
                    ((GradientDrawable) drawable).setCornerRadii(radii);
                }

                // inset
                if (backgroundInsetLeft > 0 || backgroundInsetTop > 0 || backgroundInsetRight > 0 || backgroundInsetBottom > 0) {
                    LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{drawable});
                    layerDrawable.setLayerInset(0, backgroundInsetLeft, backgroundInsetTop, backgroundInsetRight, backgroundInsetBottom);
                    drawable = layerDrawable;
                }

                //
                if (null == listDrawable) {
                    listDrawable = new StateListDrawable();
                }

                //
                if (i == 0) {
//                    if (this instanceof TextView) {
//                        LogUtil.log("CornerImpl -> state_focused -> drawable = " + drawable);
//                    }
                    listDrawable.addState(new int[]{android.R.attr.state_focused}, drawable);
                } else if (i == 1) {
//                    if (this instanceof TextView) {
//                        LogUtil.log("CornerImpl -> state_hovered -> drawable = " + drawable);
//                    }
                    listDrawable.addState(new int[]{android.R.attr.state_hovered}, drawable);
                } else {
//                    if (this instanceof TextView) {
//                        LogUtil.log("CornerImpl -> state_default -> drawable = " + drawable);
//                    }
                    listDrawable.addState(new int[]{}, drawable);
                }
            }

            if (null == listDrawable)
                throw new Exception("error: listDrawable null");

            return listDrawable;
        } catch (Exception e) {
            return null;
        }
    }

    private ColorStateList initTextColor(int textColor,
                                         int textColorFocused,
                                         int textColorHovered) {

        try {
            if (textColor == NA)
                throw new Exception("error: textColor = " + NA);

            int[][] states = new int[][]{
                    new int[]{android.R.attr.state_hovered},
                    new int[]{android.R.attr.state_focused},
                    new int[]{}
            };

            int[] colors = new int[]{
                    textColorHovered == NA ? textColor : textColorHovered,
                    textColorFocused == NA ? textColor : textColorFocused,
                    textColor,
            };

            return new ColorStateList(states, colors);
        } catch (Exception e) {
            return null;
        }
    }

    private Drawable initDrawable(
            int index,
            int leftWidth, int leftHeight,
            int leftDrawable, int leftDrawableHovered, int leftDrawableChecked, int leftDrawableFocused,
            int topWidth, int topHeight,
            int topDrawable, int topDrawableHovered, int topDrawableChecked, int topDrawableFocused,
            int rightWidth, int rightHeight,
            int rightDrawable, int rightDrawableHovered, int rightDrawableChecked,
            int rightDrawableFocused,
            int bottomWidth, int bottomHeight,
            int bottomDrawable, int bottomDrawableHovered, int bottomDrawableChecked,
            int bottomDrawableFocused) {

        try {

            if (!(this instanceof TextView))
                throw new Exception("error: this not instanceof TextView");

            //
            StateListDrawable listDrawable = null;

            for (int n = 0; n < 4; n++) {

                int id;
                // left focused
                if (index == 0 && n == 0) {
                    id = leftDrawableFocused;
                }
                // left hovered
                else if (index == 0 && n == 1) {
                    id = leftDrawableHovered;
                }
                // left checked
                else if (index == 0 && n == 2) {
                    id = leftDrawableChecked;
                }
                // left normal
                else if (index == 0) {
                    id = leftDrawable;
                }
                // top focused
                else if (index == 1 && n == 0) {
                    id = topDrawableFocused;
                }
                // top hovered
                else if (index == 1 && n == 1) {
                    id = topDrawableHovered;
                }
                // top checked
                else if (index == 1 && n == 2) {
                    id = topDrawableChecked;
                }
                // top normal
                else if (index == 1) {
                    id = topDrawable;
                }
                // right focused
                else if (index == 2 && n == 0) {
                    id = rightDrawableFocused;
                }
                // right hovered
                else if (index == 2 && n == 1) {
                    id = rightDrawableHovered;
                }
                // right checked
                else if (index == 2 && n == 2) {
                    id = rightDrawableChecked;
                }
                // right normal
                else if (index == 2) {
                    id = rightDrawable;
                }
                // bottom focused
                else if (n == 0) {
                    id = bottomDrawableFocused;
                }
                // bottom hovered
                else if (n == 1) {
                    id = bottomDrawableHovered;
                }
                // bottom checked
                else if (n == 2) {
                    id = bottomDrawableChecked;
                }
                // bottom normal
                else {
                    id = bottomDrawable;
                }

                Drawable drawable = null;
                if (n == 3 && id == 0) {
                    // left top right bottom
                    drawable = ((TextView) this).getCompoundDrawables()[index];
                } else if (id != 0) {
                    drawable = ResourcesCompat.getDrawable(((View) this).getResources(), id, null);
                }
                if (null == drawable)
                    continue;

                int width;
                int height;
                // left
                if (index == 0) {
                    width = leftWidth;
                    height = leftHeight;
                }
                // top
                else if (index == 1) {
                    width = topWidth;
                    height = topHeight;
                }
                // right
                else if (index == 2) {
                    width = rightWidth;
                    height = rightHeight;
                }
                // bottom
                else {
                    width = bottomWidth;
                    height = bottomHeight;
                }
                if (width <= 0 || height <= 0)
                    continue;

                if (null == listDrawable) {
                    listDrawable = new StateListDrawable();
                    listDrawable.setBounds(0, 0, width, height);
                }

//                    LogUtil.log("CornerImpl -> text = " + ((TextView) this).getText() + ", index = " + index + ", n = " + n);

                // focused
                if (n == 0) {
//                        LogUtil.log("CornerImpl -> state_focused -> drawable = " + drawable);
                    listDrawable.addState(new int[]{android.R.attr.state_focused}, drawable);
                }
                //  hovered
                else if (n == 1) {
//                        LogUtil.log("CornerImpl -> state_hovered -> drawable = " + drawable);
                    listDrawable.addState(new int[]{android.R.attr.state_hovered}, drawable);
                }
                //  checked
                else if (n == 2) {
//                        LogUtil.log("CornerImpl -> state_checked -> drawable = " + drawable);
                    listDrawable.addState(new int[]{android.R.attr.state_checked}, drawable);
                }
                //  normal
                else {
//                        LogUtil.log("CornerImpl -> state_default -> drawable = " + drawable);
                    listDrawable.addState(new int[]{}, drawable);
                }
            }

            //
            if (null == listDrawable)
                throw new Exception("error: listDrawable null");

            return listDrawable;
        } catch (Exception e) {
            return null;
        }
    }

    private int measureSpecWidth(int widthMeasureSpec, int heightMeasureSpec, float rate, float rateFocused, float rateHovered) {
        try {
//            if (widthMeasureSpec != View.MeasureSpec.UNSPECIFIED)
//                throw new Exception("warning: widthMeasureSpec not View.MeasureSpec.UNSPECIFIED");

            boolean focused = ((View) this).hasFocus();
            boolean hovered = ((View) this).isHovered();

            float value;
            // LogUtil.log("CornerLinearLayout -> onMeasure -> focused = " + focused + ", hovered = " + hovered);
            if (focused) {
                value = rateFocused > 0f ? rateFocused : rate;
            } else if (hovered) {
                value = rateHovered > 0f ? rateHovered : rate;
            } else {
                value = rate;
            }

            if (value <= 0)
                throw new Exception("warning: value<=0");
            int height = View.MeasureSpec.getSize(heightMeasureSpec);
            int width = (int) (height * value);
            return View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
        } catch (Exception e) {
            return widthMeasureSpec;
        }
    }

    private int measureSpecHeight(int widthMeasureSpec, int heightMeasureSpec, float rate, float rateFocused, float rateHovered) {
        try {
//            if (heightMeasureSpec != View.MeasureSpec.UNSPECIFIED)
//                throw new Exception("warning: heightMeasureSpec not View.MeasureSpec.UNSPECIFIED");

            boolean focused = ((View) this).hasFocus();
            boolean hovered = ((View) this).isHovered();

            float value;
            // LogUtil.log("CornerLinearLayout -> onMeasure -> focused = " + focused + ", hovered = " + hovered);
            if (focused) {
                value = rateFocused > 0f ? rateFocused : rate;
            } else if (hovered) {
                value = rateHovered > 0f ? rateHovered : rate;
            } else {
                value = rate;
            }

            if (value <= 0)
                throw new Exception("warning: value<=0");
            int width = View.MeasureSpec.getSize(widthMeasureSpec);
            int height = (int) (width * value);
            return View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);
        } catch (Exception e) {
            return heightMeasureSpec;
        }
    }
}
