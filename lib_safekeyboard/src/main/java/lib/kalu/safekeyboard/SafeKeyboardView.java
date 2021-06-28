package lib.kalu.safekeyboard;

import android.app.Service;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.AttributeSet;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.RawRes;
import androidx.core.content.ContextCompat;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.List;

import lib.kalu.safekeyboard.google.Keyboard;
import lib.kalu.safekeyboard.google.KeyboardView;

/**
 * description: 自定义安全键盘
 * created by kalu on 2021-01-15
 */
@Keep
public class SafeKeyboardView extends KeyboardView implements KeyboardView.OnKeyboardActionListener {

    public static final int TYPE_LETTER = 1;
    public static final int TYPE_NUMBER = 2;
    public static final int TYPE_SYMBOL = 3;

    // 加密key
    private final String ENCRYPTION_KEY = "0123456789abcdef";
    // 加密向量
    private final String ENCRYPTION_IV = "9876543210fedcba";
    // 加密密码
    private final String ENCRYPTION_PW = "☸";

    // 字母键盘随机
    private boolean randomLetter = false;
    // 数字键盘随机
    private boolean randomNumber = false;

    @Deprecated
    public SafeKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setTag(R.id.safe_keyboard_id_input_type, TYPE_LETTER);
        setTag(R.id.safe_keyboard_id_input_text, null);


        setEnabled(true);
        setPreviewEnabled(false);
        setOnKeyboardActionListener(this);
        setKeyboard(new Keyboard(getContext(), R.xml.moudle_safe_keyboard_letter, randomNumber, randomLetter));
    }

    public SafeKeyboardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setTag(R.id.safe_keyboard_id_input_type, TYPE_LETTER);
        setTag(R.id.safe_keyboard_id_input_text, null);

        setEnabled(true);
        setPreviewEnabled(false);
        setOnKeyboardActionListener(this);
        setKeyboard(new Keyboard(getContext(), R.xml.moudle_safe_keyboard_letter, randomNumber, randomLetter));
    }

    @Override
    public boolean isPreviewEnabled() {
        return false;
    }

    @Override
    public void setPreviewEnabled(boolean previewEnabled) {
        super.setPreviewEnabled(false);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawKeyboard(canvas);
    }

    /************************************************************************************/

    private void drawKeyboard(Canvas canvas) {

        if (null == getKeyboard())
            return;

        List<Keyboard.Key> keys = getKeyboard().getKeys();
        SafeKeyboardLogUtil.log("drawKeyboard => keys = " + keys);
        if (null == keys || keys.size() == 0)
            return;
        SafeKeyboardLogUtil.log("drawKeyboard => size = " + keys.size());

        Typeface typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD);
        for (Keyboard.Key key : keys) {

            if (null == key.codes || key.codes.length == 0)
                continue;

            SafeKeyboardLogUtil.log("drawKeyboard => code = " + key.codes[0]);

            // 功能键：删除
            if (key.codes[0] == -24) {

                // 背景
                drawBackground(canvas, key, R.drawable.moudle_safe_keyboard_background_normal, R.drawable.moudle_safe_keyboard_background_press);

                // 图标
                drawIcon(canvas, key, R.raw.moudle_safe_keyboard_delete_normal, R.raw.moudle_safe_keyboard_delete_press);
            }
            // 功能键：切换大小写
            else if (key.codes[0] == -25) {

                // 背景
                drawBackground(canvas, key, R.drawable.moudle_safe_keyboard_background_normal, R.drawable.moudle_safe_keyboard_background_press);

                // 图标
                drawShift(canvas, key, R.raw.moudle_safe_keyboard_shift_normal_dafault, R.raw.moudle_safe_keyboard_shift_normal_select, R.raw.moudle_safe_keyboard_shift_press_dafault, R.raw.moudle_safe_keyboard_shift_press_select);
            }
            // 功能键：切换键盘类型
            else if (key.codes[0] == -21 || key.codes[0] == -22 || key.codes[0] == -23) {

                // 背景
                drawBackground(canvas, key, R.drawable.moudle_safe_keyboard_background_normal, R.drawable.moudle_safe_keyboard_background_press);

                // 文字
                drawLabel(canvas, key, typeface, Color.BLACK, Color.WHITE);
            }
            // 功能键：空格
            else if (key.codes[0] == 32) {

                // 背景
                drawBackground(canvas, key, R.drawable.moudle_safe_keyboard_background_normal, R.drawable.moudle_safe_keyboard_background_press);

                // 文字
                drawLabel(canvas, key, typeface, Color.BLACK, Color.WHITE);
            }
            // 普通键
            else {

                // 背景
                drawBackground(canvas, key, R.drawable.moudle_safe_keyboard_background_normal, R.drawable.moudle_safe_keyboard_background_normal);

                // 文字
                drawLabel(canvas, key, typeface, Color.BLACK, Color.BLACK);
            }
        }
    }

    private void drawBackground(@NonNull Canvas canvas, @NonNull Keyboard.Key key, @DrawableRes int resIdNormal, @DrawableRes int resIdPressed) {

        int left = key.x;
        int top = key.y + getPaddingTop();
        int right = left + key.width;
        int bottom = top + key.height;

        SafeKeyboardLogUtil.log("drawBackground => ********************************************");

        SafeKeyboardLogUtil.log("drawBackground => left = " + left);
        SafeKeyboardLogUtil.log("drawBackground => top = " + top);
        SafeKeyboardLogUtil.log("drawBackground => right = " + right);
        SafeKeyboardLogUtil.log("drawBackground => bottom = " + bottom);

        Context context = getContext();
        Drawable drawable = ContextCompat.getDrawable(context, key.pressed ? resIdNormal : resIdPressed);

        drawable.setBounds(left, top, right, bottom);
        drawable.draw(canvas);
    }

    private void drawIcon(@NonNull Canvas canvas, @NonNull Keyboard.Key key, @RawRes int rawNormal, @RawRes int rawPress) {

        InputStream inputStream = getResources().openRawResource(key.pressed ? rawPress : rawNormal);
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

        float width = bitmap.getWidth();
        float height = bitmap.getHeight();

        float dstWidth = Math.min(key.width, key.height) * 0.6f;
        float dstHeight = height * (dstWidth / width);

        float left = key.x + key.width * 0.5f - dstWidth * 0.5f;
        float top = key.y + key.height * 0.5f - dstHeight * 0.5f;

        SafeKeyboardLogUtil.log("drawIcon => ********************************************");

        SafeKeyboardLogUtil.log("drawIcon => code = " + key.codes[0]);
        SafeKeyboardLogUtil.log("drawIcon => pressed = " + key.pressed);
        SafeKeyboardLogUtil.log("drawIcon => rawNormal = " + rawNormal);
        SafeKeyboardLogUtil.log("drawIcon => rawPress = " + rawPress);

        SafeKeyboardLogUtil.log("drawIcon => dstWidth = " + dstWidth);
        SafeKeyboardLogUtil.log("drawIcon => dstHeight = " + dstHeight);

        SafeKeyboardLogUtil.log("drawIcon => left = " + left);
        SafeKeyboardLogUtil.log("drawIcon => top = " + top);

        SafeKeyboardLogUtil.log("drawIcon => x = " + key.x);
        SafeKeyboardLogUtil.log("drawIcon => y = " + key.y);

        SafeKeyboardLogUtil.log("drawIcon => x1 = " + (key.x + key.width));
        SafeKeyboardLogUtil.log("drawIcon => y1 = " + (key.y + key.height));

        // drawBitmap
        Bitmap icon = Bitmap.createScaledBitmap(bitmap, (int) dstWidth, (int) dstHeight, true);
        canvas.drawBitmap(icon, left, top, null);

        SafeKeyboardLogUtil.log("drawIcon => inputStream = " + inputStream);
        if (null != inputStream) {
            try {
                inputStream.close();
                inputStream = null;
                SafeKeyboardLogUtil.log("drawIcon => inputStream = " + inputStream);
            } catch (Exception e) {
            }
        }

        SafeKeyboardLogUtil.log("drawIcon => bitmap = " + bitmap);
        if (null != bitmap && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
            SafeKeyboardLogUtil.log("drawIcon => bitmap = " + bitmap);
        }

        SafeKeyboardLogUtil.log("drawIcon => icon = " + icon);
        if (null != icon && !icon.isRecycled()) {
            icon.recycle();
            icon = null;
            SafeKeyboardLogUtil.log("drawIcon => icon = " + icon);
        }

        // drawCircle
//        Paint paint = new Paint();
//        paint.setColor(Color.RED);
//        canvas.drawCircle(left, top, 8, paint);
    }

    private void drawShift(@NonNull Canvas canvas, @NonNull Keyboard.Key key, @RawRes int rawNormalDefault, @RawRes int rawNormalSelect, @RawRes int rawPressDefault, @RawRes int rawPressSelect) {

        SafeKeyboardLogUtil.log("drawShift => ********************************************");

        InputStream inputStream;

        // 选中-按下
        if (getKeyboard().isShifted() && key.pressed) {
            SafeKeyboardLogUtil.log("drawShift => 选中-按下");
            inputStream = getResources().openRawResource(rawPressSelect);
        }
        // 选中-未按下
        else if (getKeyboard().isShifted() && !key.pressed) {
            SafeKeyboardLogUtil.log("drawShift => 选中-未按下");
            inputStream = getResources().openRawResource(rawNormalSelect);
        }
        // 未选中-按下
        else if (!getKeyboard().isShifted() && key.pressed) {
            SafeKeyboardLogUtil.log("drawShift => 未选中-按下");
            inputStream = getResources().openRawResource(rawPressDefault);
        }
        // 未选中-未按下
        else {
            SafeKeyboardLogUtil.log("drawShift => 未选中-未按下");
            inputStream = getResources().openRawResource(rawNormalDefault);
        }

        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

        float width = bitmap.getWidth();
        float height = bitmap.getHeight();

        float dstWidth = Math.min(key.width, key.height) * 0.6f;
        float dstHeight = height * (dstWidth / width);

        float left = key.x + key.width * 0.5f - dstWidth * 0.5f;
        float top = key.y + key.height * 0.5f - dstHeight * 0.5f;

        SafeKeyboardLogUtil.log("drawShift => code = " + key.codes[0]);
        SafeKeyboardLogUtil.log("drawShift => pressed = " + key.pressed);
        SafeKeyboardLogUtil.log("drawShift => rawNormalDefault = " + rawNormalDefault);
        SafeKeyboardLogUtil.log("drawShift => rawNormalSelect = " + rawNormalSelect);
        SafeKeyboardLogUtil.log("drawShift => rawPressDefault = " + rawPressDefault);
        SafeKeyboardLogUtil.log("drawShift => rawPressSelect = " + rawPressSelect);

        SafeKeyboardLogUtil.log("drawShift => dstWidth = " + dstWidth);
        SafeKeyboardLogUtil.log("drawShift => dstHeight = " + dstHeight);

        SafeKeyboardLogUtil.log("drawShift => left = " + left);
        SafeKeyboardLogUtil.log("drawShift => top = " + top);

        SafeKeyboardLogUtil.log("drawShift => x = " + key.x);
        SafeKeyboardLogUtil.log("drawShift => y = " + key.y);

        SafeKeyboardLogUtil.log("drawShift => x1 = " + (key.x + key.width));
        SafeKeyboardLogUtil.log("drawShift => y1 = " + (key.y + key.height));

        // drawBitmap
        Bitmap icon = Bitmap.createScaledBitmap(bitmap, (int) dstWidth, (int) dstHeight, true);
        canvas.drawBitmap(icon, left, top, null);

        SafeKeyboardLogUtil.log("drawShift => inputStream = " + inputStream);
        if (null != inputStream) {
            try {
                inputStream.close();
                inputStream = null;
                SafeKeyboardLogUtil.log("drawShift => inputStream = " + inputStream);
            } catch (Exception e) {
            }
        }

        SafeKeyboardLogUtil.log("drawShift => bitmap = " + bitmap);
        if (null != bitmap && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
            SafeKeyboardLogUtil.log("drawShift => bitmap = " + bitmap);
        }

        SafeKeyboardLogUtil.log("drawShift => icon = " + icon);
        if (null != icon && !icon.isRecycled()) {
            icon.recycle();
            icon = null;
            SafeKeyboardLogUtil.log("drawShift => icon = " + icon);
        }

        // drawCircle
//        Paint paint = new Paint();
//        paint.setColor(Color.RED);
//        canvas.drawCircle(left, top, 8, paint);
    }

    /**
     * <Key
     * app:codes="-2"
     * app:keyLabel="123"
     * app:keyWidth="19%p" />
     *
     * @param canvas
     * @param key
     */
    private void drawLabel(@NonNull Canvas canvas, @NonNull Keyboard.Key key, @NonNull Typeface typeface, @ColorInt int colorPress, @ColorInt int colorNormal) {

        if (null == key.label || TextUtils.isEmpty(key.label.toString()))
            return;

        Rect bounds = new Rect();
        Paint paint = new Paint();
        paint.setTextAlign(Paint.Align.CENTER);

        paint.setAntiAlias(true);
        paint.setColor(key.pressed ? colorPress : colorNormal);

        String label = key.label.toString();
        Field field;

        if (label.length() > 1 && key.codes.length < 2) {
            int labelTextSize = 0;
            try {
                field = KeyboardView.class.getDeclaredField("mLabelTextSize");
                field.setAccessible(true);
                labelTextSize = (int) field.get(this);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            paint.setTextSize(labelTextSize);
            paint.setTypeface(Typeface.DEFAULT_BOLD);
        } else {
            int keyTextSize = 0;
            try {
                field = KeyboardView.class.getDeclaredField("mLabelTextSize");
                field.setAccessible(true);
                keyTextSize = (int) field.get(this);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            paint.setTextSize(keyTextSize);
            paint.setTypeface(typeface);
        }

        paint.getTextBounds(label, 0, label.length(), bounds);

        int x = key.x + (key.width / 2);
        int y = (key.y + key.height / 2) + bounds.height() / 2 + getPaddingTop();

        SafeKeyboardLogUtil.log("drawShift => ********************************************");

        SafeKeyboardLogUtil.log("drawShift => code = " + key.codes[0]);
        SafeKeyboardLogUtil.log("drawShift => pressed = " + key.pressed);

        SafeKeyboardLogUtil.log("drawShift => x = " + x);
        SafeKeyboardLogUtil.log("drawShift => y = " + y);

        canvas.drawText(label, x, y, paint);
    }

    /************************************************************************************/

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        SafeKeyboardLogUtil.log("onKey => primaryCode = " + primaryCode);

        switch (primaryCode) {

            // 切换字母键盘
            case -21:

                setTag(R.id.safe_keyboard_id_input_type, TYPE_LETTER);
                setKeyboard(new Keyboard(getContext(), R.xml.moudle_safe_keyboard_letter, randomNumber, randomLetter));

                break;

            // 切换数字键盘
            case -22:
                setTag(R.id.safe_keyboard_id_input_type, TYPE_NUMBER);
                setKeyboard(new Keyboard(getContext(), R.xml.moudle_safe_keyboard_numbers, randomNumber, randomLetter));
                break;

            // 切换符号键盘
            case -23:

                setTag(R.id.safe_keyboard_id_input_type, TYPE_SYMBOL);
                setKeyboard(new Keyboard(getContext(), R.xml.moudle_safe_keyboard_symbol, randomNumber, randomLetter));

                break;

            // 删除
            case -24:

                delete();
                break;

            // 大小写
            case -25:

                shiftKeyboard();
                break;

            // 数值
            default:

                input(primaryCode);
                break;
        }
    }

    @Override
    public void onPress(int primaryCode) {

        // 震动
        try {
            Vibrator vib = (Vibrator) getContext().getSystemService(Service.VIBRATOR_SERVICE);
            vib.vibrate(12);
        } catch (Exception e) {
        }

//        // 大小写
//        if (primaryCode == Keyboard.KEYCODE_SHIFT) {
//            setPreviewEnabled(false);
//        }
//        // 删除
//        else if (primaryCode == Keyboard.KEYCODE_DELETE) {
//            setPreviewEnabled(false);
//        } else if (primaryCode == 32 || primaryCode == -2 || primaryCode == 3000) {
//            setPreviewEnabled(false);
//        } else {
//            setPreviewEnabled(true);
//        }
    }

    @Override
    public void onText(CharSequence text) {
        SafeKeyboardLogUtil.log("onText => text = " + text);
    }

    @Override
    public void onRelease(int primaryCode) {
    }

    @Override
    public void swipeLeft() {

    }

    @Override
    public void swipeRight() {

    }

    @Override
    public void swipeDown() {

    }

    @Override
    public void swipeUp() {

    }

    /*******************************************************************************************/

    /**
     * 切换大小写
     */
    private void shiftKeyboard() {

        // 字母键盘
        if (null == getKeyboard())
            return;

        List<Keyboard.Key> keys = getKeyboard().getKeys();
        if (null == keys || keys.size() == 0)
            return;

        // 大写状态
        boolean shifted = getKeyboard().isShifted();

        if (shifted) {
            // 大写切换小写
            getKeyboard().setShifted(false);
            String temp = "abcdefghijklmnopqrstuvwxyz";
            for (Keyboard.Key key : keys) {
                if (null != key.label) {
                    String s = key.label.toString().toLowerCase();
                    boolean contains = temp.contains(s);
                    if (contains) {
                        key.label = key.label.toString().toLowerCase();
                        key.codes[0] = key.codes[0] + 32;
                    }
                }
            }
        } else {
            // 小写切换成大写
            getKeyboard().setShifted(true);
            String temp = "abcdefghijklmnopqrstuvwxyz";
            for (Keyboard.Key key : keys) {
                if (null != key.label) {
                    String s = key.label.toString().toLowerCase();
                    boolean contains = temp.contains(s);
                    if (contains) {
                        key.label = key.label.toString().toUpperCase();
                        key.codes[0] = key.codes[0] - 32;
                    }
                }
            }
        }

        // 刷新ui
        setKeyboard(getKeyboard());
    }

    /**
     * 输入加密
     *
     * @param code
     */
    private void input(int code) {
        SafeKeyboardLogUtil.log("input => code = " + code);

        String value = SafeKeyboardUtil.encryptCode(code, ENCRYPTION_KEY, ENCRYPTION_IV);
        SafeKeyboardLogUtil.log("input => value = " + value);

        if (TextUtils.isEmpty(value))
            return;

        StringBuilder builder = new StringBuilder();
        if (null != getTag(R.id.safe_keyboard_id_input_text) && !TextUtils.isEmpty((String) getTag(R.id.safe_keyboard_id_input_text))) {
            builder.append((String) getTag(R.id.safe_keyboard_id_input_text));
            builder.append(ENCRYPTION_PW);
        }
        builder.append(value);

        String update = builder.toString();
        SafeKeyboardLogUtil.log("input => update = " + update);
        setTag(R.id.safe_keyboard_id_input_text, update);

        if (null != onSafeKeyboardChangeListener) {
            onSafeKeyboardChangeListener.onInput("*");
            if (BuildConfig.DEBUG) {
                try {
                    String letter = String.valueOf((char) code);
                    String all = getInput(false);
                    onSafeKeyboardChangeListener.onInputReal(BuildConfig.DEBUG ? letter : "null", BuildConfig.DEBUG ? all : "null");
                } catch (Exception e) {
                }
            }
        }
    }

    /**
     * 删除字符
     */
    private void delete() {

        if (null == getTag(R.id.safe_keyboard_id_input_text) || TextUtils.isEmpty((String) getTag(R.id.safe_keyboard_id_input_text)))
            return;

        String[] split = ((String) getTag(R.id.safe_keyboard_id_input_text)).split(ENCRYPTION_PW);
        SafeKeyboardLogUtil.log("delete => split = " + split);

        if (null == split || split.length == 0)
            return;
        SafeKeyboardLogUtil.log("delete => splitLength = " + split.length);

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < split.length - 1; i++) {
            builder.append(split[i]);
            builder.append(ENCRYPTION_PW);
        }

        String update = builder.toString();
        SafeKeyboardLogUtil.log("delete => update = " + update);
        setTag(R.id.safe_keyboard_id_input_text, update);

        if (null != onSafeKeyboardChangeListener) {
            onSafeKeyboardChangeListener.onDelete("*");
            if (BuildConfig.DEBUG) {
                try {
                    String decryptLetter = SafeKeyboardUtil.decrypt(split[split.length - 1], ENCRYPTION_KEY, ENCRYPTION_IV);
                    String letter = String.valueOf((char) Integer.parseInt(decryptLetter));
                    StringBuilder builderAll = new StringBuilder();
                    for (int i = 0; i < split.length - 1; i++) {
                        String decrypt = SafeKeyboardUtil.decrypt(split[i], ENCRYPTION_KEY, ENCRYPTION_IV);
                        String valueOf = String.valueOf((char) Integer.parseInt(decrypt));
                        builderAll.append(valueOf);
                    }
                    String all = builderAll.toString();
                    onSafeKeyboardChangeListener.onDeleteReal(BuildConfig.DEBUG ? letter : "null", BuildConfig.DEBUG ? all : "null");
                } catch (Exception e) {
                }
            }
        }
    }

    public String getInput() {
        String parse = getInput(true);
        return parse;
    }

    /**
     * 解密
     *
     * @return
     */
    private final String getInput(boolean clear) {

        if (null == getTag(R.id.safe_keyboard_id_input_text) || TextUtils.isEmpty((String) getTag(R.id.safe_keyboard_id_input_text)))
            return null;

        String[] split = ((String) getTag(R.id.safe_keyboard_id_input_text)).split(ENCRYPTION_PW);
        SafeKeyboardLogUtil.log("parse => split = " + split);

        // 强制清空
        if (clear) {
            setTag(R.id.safe_keyboard_id_input_text, null);
        }

        if (null == split || split.length == 0)
            return null;
        SafeKeyboardLogUtil.log("parse => splitLength = " + split.length);

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < split.length; i++) {

            String decrypt = SafeKeyboardUtil.decrypt(split[i], ENCRYPTION_KEY, ENCRYPTION_IV);
            SafeKeyboardLogUtil.log("parse => decrypt = " + decrypt);
            if (TextUtils.isEmpty(decrypt))
                continue;

            String value = String.valueOf((char) Integer.parseInt(decrypt));
            SafeKeyboardLogUtil.log("parse => value = " + value);

            builder.append(value);
        }

        String result = builder.toString();
        return result;
    }

    protected void setRandomLetter(boolean randomLetter) {
        this.randomLetter = randomLetter;
    }

    protected void setRandomNumber(boolean randomNumber) {
        this.randomNumber = randomNumber;
    }

    /********************************/

    private OnSafeKeyboardChangeListener onSafeKeyboardChangeListener;

    public void setOnSafeKeyboardChangeListener(@NonNull OnSafeKeyboardChangeListener listener) {
        this.onSafeKeyboardChangeListener = listener;
    }

    public interface OnSafeKeyboardChangeListener {

        void onInput(@NonNull CharSequence letter);

        default void onInputReal(@NonNull CharSequence letter, @NonNull CharSequence news) {
        }

        void onDelete(@NonNull CharSequence letter);

        default void onDeleteReal(@NonNull CharSequence letter, @NonNull CharSequence news) {
        }
    }
}
