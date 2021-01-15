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
import android.util.Log;

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

    @Deprecated
    public SafeKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setTag(R.id.keyboardType, TYPE_LETTER);
        setTag(R.id.keyboardText, null);


        setEnabled(true);
        setPreviewEnabled(false);
        setOnKeyboardActionListener(this);
        setKeyboard(new Keyboard(getContext(), R.xml.keyboard_letter));
    }

    public SafeKeyboardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setTag(R.id.keyboardType, TYPE_LETTER);
        setTag(R.id.keyboardText, null);

        setEnabled(true);
        setPreviewEnabled(false);
        setOnKeyboardActionListener(this);
        setKeyboard(new Keyboard(getContext(), R.xml.keyboard_letter));
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
        Log.d("safe", "drawKeyboard => keys = " + keys);
        if (null == keys || keys.size() == 0)
            return;
        Log.d("safe", "drawKeyboard => size = " + keys.size());

        for (Keyboard.Key key : keys) {

            if (null == key.codes || key.codes.length == 0)
                continue;

            // 键盘：删除
            if (key.codes[0] == -5 || key.codes[0] == -35) {

                Log.d("safe", "drawKeyboard => code = " + key.codes[0]);

                // 背景
                drawBackground(canvas, key, R.drawable.keyboard_background_normal, R.drawable.keyboard_background_press);

                // 图标
                drawIcon(canvas, key, R.raw.keyboard_delete_normal, R.raw.keyboard_delete_press);
            }
            // 键盘：大小写切换
            else if (key.codes[0] == -1) {

                Log.d("safe", "drawKeyboard => code = " + key.codes[0]);

                // 背景
                drawBackground(canvas, key, R.drawable.keyboard_background_normal, R.drawable.keyboard_background_press);

                // 图标
                drawShift(canvas, key, R.raw.keyboard_shift_normal_dafault, R.raw.keyboard_shift_normal_select, R.raw.keyboard_shift_press_dafault, R.raw.keyboard_shift_press_select);
            }
            // 键盘：切换按键
            else if (key.codes[0] == -2 || key.codes[0] == 3000) {

                Log.d("safe", "drawKeyboard => code = " + key.codes[0]);

                // 背景
                drawBackground(canvas, key, R.drawable.keyboard_background_normal, R.drawable.keyboard_background_press);

                // 文字
                drawLabel(canvas, key);
            }
        }
    }

    private void drawBackground(@NonNull Canvas canvas, @NonNull Keyboard.Key key, @DrawableRes int resIdNormal, @DrawableRes int resIdPressed) {

        int left = key.x;
        int top = key.y + getPaddingTop();
        int right = left + key.width;
        int bottom = top + key.height;

        Log.d("safe", "drawBackground => ********************************************");

        Log.d("safe", "drawBackground => left = " + left);
        Log.d("safe", "drawBackground => top = " + top);
        Log.d("safe", "drawBackground => right = " + right);
        Log.d("safe", "drawBackground => bottom = " + bottom);

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

        Log.d("safe", "drawIcon => ********************************************");

        Log.d("safe", "drawIcon => code = " + key.codes[0]);
        Log.d("safe", "drawIcon => pressed = " + key.pressed);
        Log.d("safe", "drawIcon => rawNormal = " + rawNormal);
        Log.d("safe", "drawIcon => rawPress = " + rawPress);

        Log.d("safe", "drawIcon => dstWidth = " + dstWidth);
        Log.d("safe", "drawIcon => dstHeight = " + dstHeight);

        Log.d("safe", "drawIcon => left = " + left);
        Log.d("safe", "drawIcon => top = " + top);

        Log.d("safe", "drawIcon => x = " + key.x);
        Log.d("safe", "drawIcon => y = " + key.y);

        Log.d("safe", "drawIcon => x1 = " + (key.x + key.width));
        Log.d("safe", "drawIcon => y1 = " + (key.y + key.height));

        // drawBitmap
        Bitmap icon = Bitmap.createScaledBitmap(bitmap, (int) dstWidth, (int) dstHeight, true);
        canvas.drawBitmap(icon, left, top, null);

        Log.d("safe", "drawIcon => inputStream = " + inputStream);
        if (null != inputStream) {
            try {
                inputStream.close();
                inputStream = null;
                Log.d("safe", "drawIcon => inputStream = " + inputStream);
            } catch (Exception e) {
            }
        }

        Log.d("safe", "drawIcon => bitmap = " + bitmap);
        if (null != bitmap && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
            Log.d("safe", "drawIcon => bitmap = " + bitmap);
        }

        Log.d("safe", "drawIcon => icon = " + icon);
        if (null != icon && !icon.isRecycled()) {
            icon.recycle();
            icon = null;
            Log.d("safe", "drawIcon => icon = " + icon);
        }

        // drawCircle
//        Paint paint = new Paint();
//        paint.setColor(Color.RED);
//        canvas.drawCircle(left, top, 8, paint);
    }

    private void drawShift(@NonNull Canvas canvas, @NonNull Keyboard.Key key, @RawRes int rawNormalDefault, @RawRes int rawNormalSelect, @RawRes int rawPressDefault, @RawRes int rawPressSelect) {

        Log.d("safe", "drawShift => ********************************************");

        InputStream inputStream;

        // 选中-按下
        if (getKeyboard().isShifted() && key.pressed) {
            Log.d("safe", "drawShift => 选中-按下");
            inputStream = getResources().openRawResource(rawPressSelect);
        }
        // 选中-未按下
        else if (getKeyboard().isShifted() && !key.pressed) {
            Log.d("safe", "drawShift => 选中-未按下");
            inputStream = getResources().openRawResource(rawNormalSelect);
        }
        // 未选中-按下
        else if (!getKeyboard().isShifted() && key.pressed) {
            Log.d("safe", "drawShift => 未选中-按下");
            inputStream = getResources().openRawResource(rawPressDefault);
        }
        // 未选中-未按下
        else {
            Log.d("safe", "drawShift => 未选中-未按下");
            inputStream = getResources().openRawResource(rawNormalDefault);
        }

        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

        float width = bitmap.getWidth();
        float height = bitmap.getHeight();

        float dstWidth = Math.min(key.width, key.height) * 0.6f;
        float dstHeight = height * (dstWidth / width);

        float left = key.x + key.width * 0.5f - dstWidth * 0.5f;
        float top = key.y + key.height * 0.5f - dstHeight * 0.5f;

        Log.d("safe", "drawShift => code = " + key.codes[0]);
        Log.d("safe", "drawShift => pressed = " + key.pressed);
        Log.d("safe", "drawShift => rawNormalDefault = " + rawNormalDefault);
        Log.d("safe", "drawShift => rawNormalSelect = " + rawNormalSelect);
        Log.d("safe", "drawShift => rawPressDefault = " + rawPressDefault);
        Log.d("safe", "drawShift => rawPressSelect = " + rawPressSelect);

        Log.d("safe", "drawShift => dstWidth = " + dstWidth);
        Log.d("safe", "drawShift => dstHeight = " + dstHeight);

        Log.d("safe", "drawShift => left = " + left);
        Log.d("safe", "drawShift => top = " + top);

        Log.d("safe", "drawShift => x = " + key.x);
        Log.d("safe", "drawShift => y = " + key.y);

        Log.d("safe", "drawShift => x1 = " + (key.x + key.width));
        Log.d("safe", "drawShift => y1 = " + (key.y + key.height));

        // drawBitmap
        Bitmap icon = Bitmap.createScaledBitmap(bitmap, (int) dstWidth, (int) dstHeight, true);
        canvas.drawBitmap(icon, left, top, null);

        Log.d("safe", "drawShift => inputStream = " + inputStream);
        if (null != inputStream) {
            try {
                inputStream.close();
                inputStream = null;
                Log.d("safe", "drawShift => inputStream = " + inputStream);
            } catch (Exception e) {
            }
        }

        Log.d("safe", "drawShift => bitmap = " + bitmap);
        if (null != bitmap && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
            Log.d("safe", "drawShift => bitmap = " + bitmap);
        }

        Log.d("safe", "drawShift => icon = " + icon);
        if (null != icon && !icon.isRecycled()) {
            icon.recycle();
            icon = null;
            Log.d("safe", "drawShift => icon = " + icon);
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
    private void drawLabel(@NonNull Canvas canvas, @NonNull Keyboard.Key key) {

        if (null == key.label || TextUtils.isEmpty(key.label.toString()))
            return;

        Rect bounds = new Rect();
        Paint paint = new Paint();
        paint.setTextAlign(Paint.Align.CENTER);

        paint.setAntiAlias(true);
        paint.setColor(key.pressed ? Color.BLACK : Color.WHITE);

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
            paint.setTypeface(Typeface.DEFAULT);
        }

        paint.getTextBounds(label, 0, label.length(), bounds);

        int x = key.x + (key.width / 2);
        int y = (key.y + key.height / 2) + bounds.height() / 2 + getPaddingTop();

        Log.d("safe", "drawShift => ********************************************");

        Log.d("safe", "drawShift => code = " + key.codes[0]);
        Log.d("safe", "drawShift => pressed = " + key.pressed);

        Log.d("safe", "drawShift => x = " + x);
        Log.d("safe", "drawShift => y = " + y);

        canvas.drawText(label, x, y, paint);
    }

    /************************************************************************************/

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        Log.d("safe", "onKey => primaryCode = " + primaryCode);

        switch (primaryCode) {

            // 大小写切换
            case android.inputmethodservice.Keyboard.KEYCODE_SHIFT:

                shiftKeyboard();
                break;

            // 数字与字母键盘互换
            case android.inputmethodservice.Keyboard.KEYCODE_MODE_CHANGE:

                int type1 = (int) getTag(R.id.keyboardType);
                setTag(R.id.keyboardType, type1 == TYPE_LETTER ? TYPE_NUMBER : TYPE_LETTER);
                setKeyboard(new Keyboard(getContext(), type1 == TYPE_LETTER ? R.xml.keyboard_numbers : R.xml.keyboard_letter));

                break;

            // 字母与符号切换
            case 3000:

                int type2 = (int) getTag(R.id.keyboardType);
                setTag(R.id.keyboardType, type2 == TYPE_LETTER ? TYPE_SYMBOL : TYPE_LETTER);
                setKeyboard(new Keyboard(getContext(), type2 == TYPE_LETTER ? R.xml.keyboard_symbol : R.xml.keyboard_letter));

                break;

            // 回退键,删除字符
            case -35:
            case android.inputmethodservice.Keyboard.KEYCODE_DELETE:

                delete();
                break;

            // 隐藏键盘
            case android.inputmethodservice.Keyboard.KEYCODE_CANCEL:

                break;
            // 输入键盘值
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
            vib.vibrate(20);
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
        Log.d("safe", "onText => text = " + text);
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
        Log.d("safe", "input => code = " + code);

        // 加密key
        String key = "0123456789abcdef";
        // 加密向量
        String iv = "9876543210fedcba";
        // 加密密码
        String pass = "";

        String value = SafeKeyboardUtil.encryptCode(code, key, iv);
        Log.d("safe", "input => value = " + value);

        if (TextUtils.isEmpty(value))
            return;

        if (!TextUtils.isEmpty(pass)) {

            StringBuffer buffer = new StringBuffer();
            buffer.append(pass);
            buffer.append(value);

            String toString = buffer.toString();
            value = toString;
            Log.d("safe", "input => value = " + value);
        }

        StringBuilder builder = new StringBuilder();
        if (null != getTag(R.id.keyboardText) && !TextUtils.isEmpty((String) getTag(R.id.keyboardText))) {
            builder.append((String) getTag(R.id.keyboardText));
            builder.append("☸");
        }
        builder.append(value);

        String update = builder.toString();
        Log.d("safe", "input => update = " + update);
        setTag(R.id.keyboardText, update);
    }

    /**
     * 删除字符
     */
    private void delete() {

        if (null == getTag(R.id.keyboardText) || TextUtils.isEmpty((String) getTag(R.id.keyboardText)))
            return;

        String[] split = ((String) getTag(R.id.keyboardText)).split("☸");
        Log.d("safe", "delete => split = " + split);
        Log.d("safe", "delete => splitLength = " + split.length);

        if (null == split || split.length == 0)
            return;

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < split.length - 1; i++) {
            builder.append(split[i]);
            builder.append("☸");
        }

        String update = builder.toString();
        Log.d("safe", "delete => update = " + update);
        setTag(R.id.keyboardText, update);
    }

    public String parse() {
        String parse = parse(true);
        return parse;
    }

    /**
     * 解密
     *
     * @return
     */
    public String parse(boolean clear) {

        if (null == getTag(R.id.keyboardText) || TextUtils.isEmpty((String) getTag(R.id.keyboardText)))
            return null;

        // 加密key
        String key = "0123456789abcdef";
        // 加密向量
        String iv = "9876543210fedcba";
        // 加密密码
        String pass = "";

        String[] split = ((String) getTag(R.id.keyboardText)).split("☸");

        // 强制清空
        if (clear) {
            setTag(R.id.keyboardText, null);
        }

        Log.d("safe", "parse => split = " + split);
        Log.d("safe", "parse => splitLength = " + split.length);

        if (null == split || split.length == 0)
            return null;

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < split.length; i++) {

            String decrypt = SafeKeyboardUtil.decrypt(split[i], key, iv);
            Log.d("safe", "parse => decrypt = " + decrypt);
            if (TextUtils.isEmpty(decrypt))
                continue;

            String value = String.valueOf((char) Integer.parseInt(decrypt));
            Log.d("safe", "parse => value = " + value);

            builder.append(value);
        }

        String result = builder.toString();
        return result;
    }


    public interface OnSafeKeyboardChangeListener {

    }
}
