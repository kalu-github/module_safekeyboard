package lib.kalu.input.core;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.util.Arrays;
import java.util.List;

import lib.kalu.input.CusUtil;
import lib.kalu.input.R;

public class MiniKeyboardView extends View {

    public interface OnKeyboardActionListener {

        void onPress(int primaryCode);

        void onRelease(int primaryCode);

        void onKey(int primaryCode, MiniKeyboard.Key key);
//        void onText();
    }

    private static final int NOT_A_KEY = -1;
    private MiniKeyboard mMiniKeyboard;
    private int mCurrentKeyIndex = NOT_A_KEY;
    private int mKeyTextSize;
    private int mKeyTextColor;
    private float mShadowRadius;
    private int mShadowColor;
    private MiniKeyboard.Key[] mKeys;

    private OnKeyboardActionListener mKeyboardActionListener;

    private static final int DEBOUNCE_TIME = 70;

    private int mVerticalCorrection;
    private int mProximityThreshold;

    private int mLastX;
    private int mLastY;

    private Paint mPaint;
    private Rect mPadding;

    private long mDownTime;
    private long mLastMoveTime;
    private int mLastKey;
    private int mLastCodeX;
    private int mLastCodeY;
    private int mCurrentKey = NOT_A_KEY;
    private long mLastKeyTime;
    private long mCurrentKeyTime;
    private int[] mKeyIndices = new int[12];
    private int mRepeatKeyIndex = NOT_A_KEY;
    private boolean mAbortKey;
    private MiniKeyboard.Key mInvalidatedKey;
    private Rect mClipRegion = new Rect(0, 0, 0, 0);

    private Drawable mKeyBackground;

    private static int MAX_NEARBY_KEYS = 12;

    /**
     * Whether the keyboard bitmap needs to be redrawn before it's blitted.
     **/
    private boolean mDrawPending;
    /**
     * The dirty region in the keyboard bitmap
     */
    private Rect mDirtyRect = new Rect();
    /**
     * The keyboard bitmap for faster updates
     */
    private Bitmap mBuffer;
    /**
     * Notes if the keyboard just changed, so that we could possibly reallocate the mBuffer.
     */
    private boolean mKeyboardChanged;
    /**
     * The canvas for the above mutable keyboard bitmap
     */
    private Canvas mCanvas;

    public MiniKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs, R.attr.keyboardViewStyle);
        init(context, attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public MiniKeyboardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, 0);
        init(context, attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public MiniKeyboardView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {

        try {

            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.KeyboardView);
            int keyTextSize = 0;
            int n = a.getIndexCount();
            for (int i = 0; i < n; i++) {
                int attr = a.getIndex(i);

                if (attr == R.styleable.KeyboardView_keyBackground) {
                    mKeyBackground = a.getDrawable(attr);
                } else if (attr == R.styleable.KeyboardView_verticalCorrection) {
                    mVerticalCorrection = a.getDimensionPixelOffset(attr, 0);
                } else if (attr == R.styleable.KeyboardView_keyTextSize) {
                    mKeyTextSize = a.getDimensionPixelSize(attr, 18);
                } else if (attr == R.styleable.KeyboardView_keyTextColor) {
                    mKeyTextColor = a.getColor(attr, 0xFF000000);
                } else if (attr == R.styleable.KeyboardView_shadowColor) {
                    mShadowColor = a.getColor(attr, 0);
                } else if (attr == R.styleable.KeyboardView_shadowRadius) {
                    mShadowRadius = a.getFloat(attr, 0f);
                }
            }

            mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setTextSize(keyTextSize);
            mPaint.setTextAlign(Align.CENTER);
            mPaint.setAlpha(255);

            mPadding = new Rect(0, 0, 0, 0);
            mKeyBackground.getPadding(mPadding);
        } catch (Exception e) {
        }
    }

    public void setOnKeyboardActionListener(OnKeyboardActionListener listener) {
        mKeyboardActionListener = listener;
    }

    protected OnKeyboardActionListener getOnKeyboardActionListener() {
        return mKeyboardActionListener;
    }

    public void setKeyboard(MiniKeyboard miniKeyboard) {
        if (mMiniKeyboard != null) {
            showPreview(NOT_A_KEY);
        }
        mMiniKeyboard = miniKeyboard;
        List<MiniKeyboard.Key> keys = mMiniKeyboard.getKeys();
        mKeys = keys.toArray(new MiniKeyboard.Key[keys.size()]);
        requestLayout();
        // Hint to reallocate the buffer if the size changed
        mKeyboardChanged = true;
        invalidateAllKeys();
        computeProximityThreshold(miniKeyboard);
        mAbortKey = true; // Until the next ACTION_DOWN
    }

    public MiniKeyboard getKeyboard() {
        return mMiniKeyboard;
    }

    public boolean setShifted(boolean shifted) {
        if (mMiniKeyboard != null) {
            if (mMiniKeyboard.setShifted(shifted)) {
                // The whole keyboard probably needs to be redrawn
                invalidateAllKeys();
                return true;
            }
        }
        return false;
    }

    public boolean isShifted() {
        if (mMiniKeyboard != null) {
            return mMiniKeyboard.isShifted();
        }
        return false;
    }

    private CharSequence adjustCase(CharSequence label) {
        if (mMiniKeyboard.isShifted() && label != null && label.length() < 3
                && Character.isLowerCase(label.charAt(0))) {
            label = label.toString().toUpperCase();
        }
        return label;
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Round up a little
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();
        if (mMiniKeyboard == null) {
            setMeasuredDimension(paddingLeft + paddingRight, paddingTop + paddingBottom);
        } else {
            int width = mMiniKeyboard.getMinWidth() + paddingLeft + paddingRight;
            if (MeasureSpec.getSize(widthMeasureSpec) < width + 10) {
                width = MeasureSpec.getSize(widthMeasureSpec);
            }
            setMeasuredDimension(width, mMiniKeyboard.getHeight() + paddingTop + paddingBottom);
        }
    }

    private void computeProximityThreshold(MiniKeyboard miniKeyboard) {
        if (miniKeyboard == null) return;
        final MiniKeyboard.Key[] keys = mKeys;
        if (keys == null) return;
        int length = keys.length;
        int dimensionSum = 0;
        for (int i = 0; i < length; i++) {
            MiniKeyboard.Key key = keys[i];
            dimensionSum += Math.min(key.width, key.height) + key.hzGap;
        }
        if (dimensionSum < 0 || length == 0) return;
        mProximityThreshold = (int) (dimensionSum * 1.4f / length);
        mProximityThreshold *= mProximityThreshold; // Square it
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mMiniKeyboard != null) {
            mMiniKeyboard.resize(w, h);
        }
        // Release the buffer, if any and it will be reallocated on the next draw
        mBuffer = null;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mDrawPending || mBuffer == null || mKeyboardChanged) {
            onBufferDraw();
        }
        canvas.drawBitmap(mBuffer, 0, 0, null);
    }

    private void onBufferDraw() {
        if (mBuffer == null || mKeyboardChanged) {
            if (mBuffer == null || mKeyboardChanged &&
                    (mBuffer.getWidth() != getWidth() || mBuffer.getHeight() != getHeight())) {
                // Make sure our bitmap is at least 1x1
                final int width = Math.max(1, getWidth());
                final int height = Math.max(1, getHeight());
                mBuffer = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                mCanvas = new Canvas(mBuffer);
            }
            invalidateAllKeys();
            mKeyboardChanged = false;
        }

        if (mMiniKeyboard == null) return;

        mCanvas.save();
        final Canvas canvas = mCanvas;
        canvas.clipRect(mDirtyRect);

        final Paint paint = mPaint;
        final Drawable keyBackground = mKeyBackground;
        final Rect clipRegion = mClipRegion;
        final Rect padding = mPadding;
        final int kbdPaddingLeft = getPaddingLeft();
        final int kbdPaddingTop = getPaddingTop();
        final MiniKeyboard.Key[] keys = mKeys;
        final MiniKeyboard.Key invalidKey = mInvalidatedKey;

        paint.setColor(mKeyTextColor);
        boolean drawSingleKey = false;
        if (invalidKey != null && canvas.getClipBounds(clipRegion)) {
            // Is clipRegion completely contained within the invalidated key?
            if (invalidKey.x + kbdPaddingLeft - 1 <= clipRegion.left &&
                    invalidKey.y + kbdPaddingTop - 1 <= clipRegion.top &&
                    invalidKey.x + invalidKey.width + kbdPaddingLeft + 1 >= clipRegion.right &&
                    invalidKey.y + invalidKey.height + kbdPaddingTop + 1 >= clipRegion.bottom) {
                drawSingleKey = true;
            }
        }
        canvas.drawColor(0x00000000, PorterDuff.Mode.CLEAR);
        final int keyCount = keys.length;
        for (int i = 0; i < keyCount; i++) {
            final MiniKeyboard.Key key = keys[i];
            if (drawSingleKey && invalidKey != key) {
                continue;
            }
            int[] drawableState = key.getCurrentDrawableState();
            keyBackground.setState(drawableState);

            // Switch the character to uppercase if shift is pressed
            String text = key.text == null ? null : adjustCase(key.text).toString();

            final Rect bounds = keyBackground.getBounds();
            if (key.width != bounds.right ||
                    key.height != bounds.bottom) {
                keyBackground.setBounds(0, 0, key.width, key.height);
            }
            canvas.translate(key.x + kbdPaddingLeft, key.y + kbdPaddingTop);
            keyBackground.draw(canvas);

            if (key.icon != null) {

                Rect iconBounds = key.icon.getBounds();
                int widthBounds = iconBounds.width();
                int heightBounds = iconBounds.height();
                CusUtil.log("MiniKeyboardView -> onBufferDraw -> widthBounds = " + widthBounds + ", heightBounds = " + heightBounds);
                int width = key.width;
                int height = key.height;
                CusUtil.log("MiniKeyboardView -> onBufferDraw -> key.width = " + width + ", key.height = " + height);

                final int drawableX = (key.width - padding.left - padding.right
                        - key.icon.getIntrinsicWidth()) / 2 + padding.left;
                final int drawableY = (key.height - padding.top - padding.bottom
                        - key.icon.getIntrinsicHeight()) / 2 + padding.top;
                canvas.translate(drawableX, drawableY);

                int left = width / 2 - widthBounds / 2;
                int top = height / 2 - heightBounds / 2;
                int right = left + widthBounds;
                int bottom = top + heightBounds;
                CusUtil.log("MiniKeyboardView -> onBufferDraw -> left = " + left + ", top = " + top + ", right = " + right + ", bottom = " + bottom);
                key.icon.setBounds(left, top, right, bottom);
                key.icon.draw(canvas);
                canvas.translate(-drawableX, -drawableY);
            } else if (text != null) {
//                // For characters, use large font. For labels like "Done", use small font.
//                if (label.length() > 1 && key.codes.length < 2) {
//                    paint.setTextSize(mLabelTextSize);
//                    paint.setTypeface(Typeface.DEFAULT_BOLD);
//                } else {

                if (key.testSize != -1) {
                    paint.setTextSize(key.testSize);
                } else {
                    paint.setTextSize(mKeyTextSize);
                }

                paint.setTypeface(Typeface.DEFAULT);
//                }
                // Draw a drop shadow for the text
                paint.setShadowLayer(mShadowRadius, 0, 0, mShadowColor);
                // Draw the text
                canvas.drawText(text,
                        (key.width - padding.left - padding.right) / 2
                                + padding.left,
                        (key.height - padding.top - padding.bottom) / 2
                                + (paint.getTextSize() - paint.descent()) / 2 + padding.top,
                        paint);
                // Turn off drop shadow
                paint.setShadowLayer(0, 0, 0, 0);
            }
            canvas.translate(-key.x - kbdPaddingLeft, -key.y - kbdPaddingTop);
        }
        mInvalidatedKey = null;

        mCanvas.restore();
        mDrawPending = false;
        mDirtyRect.setEmpty();
    }

    public void invalidateAllKeys() {
        mDirtyRect.union(0, 0, getWidth(), getHeight());
        mDrawPending = true;
        invalidate();
    }

    public void invalidateKey(int keyIndex) {
        if (mKeys == null) return;
        if (keyIndex < 0 || keyIndex >= mKeys.length) {
            return;
        }
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        final MiniKeyboard.Key key = mKeys[keyIndex];
        mInvalidatedKey = key;
        mDirtyRect.union(key.x + paddingLeft, key.y + paddingTop,
                key.x + key.width + paddingLeft, key.y + key.height + paddingTop);
        onBufferDraw();
        invalidate(key.x + paddingLeft, key.y + paddingTop,
                key.x + key.width + paddingLeft, key.y + key.height + paddingTop);
    }


    private boolean onModifiedTouchEvent(MotionEvent me, boolean possiblePoly) {
        int touchX = (int) me.getX() - getPaddingLeft();
        int touchY = (int) me.getY() - getPaddingTop();
        if (touchY >= -mVerticalCorrection)
            touchY += mVerticalCorrection;
        final int action = me.getAction();
        final long eventTime = me.getEventTime();
        int keyIndex = getKeyIndices(touchX, touchY, null);

        // Ignore all motion events until a DOWN.
        if (mAbortKey && action != MotionEvent.ACTION_DOWN && action != MotionEvent.ACTION_CANCEL) {
            return true;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mAbortKey = false;
                mLastCodeX = touchX;
                mLastCodeY = touchY;
                mLastKeyTime = 0;
                mCurrentKeyTime = 0;
                mLastKey = NOT_A_KEY;
                mCurrentKey = keyIndex;
                mDownTime = me.getEventTime();
                mLastMoveTime = mDownTime;
                mKeyboardActionListener.onPress(keyIndex != NOT_A_KEY ?
                        mKeys[keyIndex].code : 0);
                if (mCurrentKey >= 0 && mKeys[mCurrentKey].repeatable) {
                    mRepeatKeyIndex = mCurrentKey;
                    // Delivering the key could have caused an abort
                    if (mAbortKey) {
                        mRepeatKeyIndex = NOT_A_KEY;
                        break;
                    }
                }
                if (mCurrentKey != NOT_A_KEY) {
                }
                showPreview(keyIndex);
                break;

            case MotionEvent.ACTION_MOVE:
                if (keyIndex != NOT_A_KEY) {
                    if (mCurrentKey == NOT_A_KEY) {
                        mCurrentKey = keyIndex;
                        mCurrentKeyTime = eventTime - mDownTime;
                    } else {
                        if (keyIndex == mCurrentKey) {
                            mCurrentKeyTime += eventTime - mLastMoveTime;
                        } else if (mRepeatKeyIndex == NOT_A_KEY) {
                            mLastKey = mCurrentKey;
                            mLastCodeX = mLastX;
                            mLastCodeY = mLastY;
                            mLastKeyTime =
                                    mCurrentKeyTime + eventTime - mLastMoveTime;
                            mCurrentKey = keyIndex;
                            mCurrentKeyTime = 0;
                        }
                    }
                }
                showPreview(mCurrentKey);
                mLastMoveTime = eventTime;
                break;

            case MotionEvent.ACTION_UP:
                if (keyIndex == mCurrentKey) {
                    mCurrentKeyTime += eventTime - mLastMoveTime;
                } else {
                    mLastKey = mCurrentKey;
                    mLastKeyTime = mCurrentKeyTime + eventTime - mLastMoveTime;
                    mCurrentKey = keyIndex;
                    mCurrentKeyTime = 0;
                }
                if (mCurrentKeyTime < mLastKeyTime && mCurrentKeyTime < DEBOUNCE_TIME
                        && mLastKey != NOT_A_KEY) {
                    mCurrentKey = mLastKey;
                    touchX = mLastCodeX;
                    touchY = mLastCodeY;
                }
                showPreview(NOT_A_KEY);
                Arrays.fill(mKeyIndices, NOT_A_KEY);
                // If we're not on a repeating key (which sends on a DOWN event)
                if (mRepeatKeyIndex == NOT_A_KEY && !mAbortKey) {
                    detectAndSendKey(mCurrentKey, touchX, touchY, eventTime);
                }
                invalidateKey(keyIndex);
                mRepeatKeyIndex = NOT_A_KEY;
                break;
            case MotionEvent.ACTION_CANCEL:
                mAbortKey = true;
                showPreview(NOT_A_KEY);
                invalidateKey(mCurrentKey);
                break;
        }
        mLastX = touchX;
        mLastY = touchY;
        return true;
    }

    public void closing() {
        mBuffer = null;
        mCanvas = null;
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        closing();
    }

    /**************************/

    private int getKeyIndices(int x, int y, int[] allKeys) {
        final MiniKeyboard.Key[] keys = mKeys;
        int primaryIndex = NOT_A_KEY;
        int[] nearestKeyIndices = mMiniKeyboard.getNearestKeys(x, y);
        final int keyCount = nearestKeyIndices.length;
        for (int i = 0; i < keyCount; i++) {
            final MiniKeyboard.Key key = keys[nearestKeyIndices[i]];
            boolean isInside = key.isInside(x, y);
            if (isInside) {
                primaryIndex = nearestKeyIndices[i];
            }
        }
        return primaryIndex;
    }

    /**
     * 点击背景色变色 ，可以当作获取焦点改造
     *
     * @param keyIndex
     */
    private void showPreview(int keyIndex) {
        CusUtil.log("MiniKeyboardView -> showPreview -> mCurrentKeyIndex = " + mCurrentKeyIndex + ", keyIndex = " + keyIndex);

        int oldKeyIndex = mCurrentKeyIndex;

        mCurrentKeyIndex = keyIndex;
        // Release the old key and press the new key
        final MiniKeyboard.Key[] keys = mKeys;
        if (oldKeyIndex != mCurrentKeyIndex) {
            if (oldKeyIndex != NOT_A_KEY && keys.length > oldKeyIndex) {
                MiniKeyboard.Key oldKey = keys[oldKeyIndex];
                oldKey.onReleased(mCurrentKeyIndex == NOT_A_KEY);
                invalidateKey(oldKeyIndex);
            }
            if (mCurrentKeyIndex != NOT_A_KEY && keys.length > mCurrentKeyIndex) {
                MiniKeyboard.Key newKey = keys[mCurrentKeyIndex];
                newKey.onPressed();
                invalidateKey(mCurrentKeyIndex);
            }
        }
    }

    /**
     * call onKey
     *
     * @param index
     * @param x
     * @param y
     * @param eventTime
     */
    private void detectAndSendKey(int index, int x, int y, long eventTime) {
        if (index != NOT_A_KEY && index < mKeys.length) {
            final MiniKeyboard.Key key = mKeys[index];
//            if (key.text != null) {
//                // TODO: 2025/5/30
////                mKeyboardActionListener.onText(key.text);
//                mKeyboardActionListener.onRelease(NOT_A_KEY);
//            } else {
            int code = key.code;

            //TextEntryState.keyPressedAt(key, x, y);
            int[] codes = new int[MAX_NEARBY_KEYS];
            Arrays.fill(codes, NOT_A_KEY);
            getKeyIndices(x, y, codes);
            mKeyboardActionListener.onKey(code, key);
            mKeyboardActionListener.onRelease(code);
//            }
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        try {
            final int action = event.getAction();
            boolean result;
            final long now = event.getEventTime();

            MotionEvent down = MotionEvent.obtain(now, now, MotionEvent.ACTION_DOWN,
                    event.getX(), event.getY(), event.getMetaState());
            result = onModifiedTouchEvent(down, false);
            down.recycle();
            // If it's an up action, then deliver the up as well.
            if (action == MotionEvent.ACTION_UP) {
                result = onModifiedTouchEvent(event, true);
            }

            return result;
        } catch (Exception e) {
            return super.dispatchTouchEvent(event);
        }
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, @Nullable Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        CusUtil.log("MiniKeyboardView -> onFocusChanged -> gainFocus = " + gainFocus);
    }

    private int focusedIndex = -1;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        CusUtil.log("MiniKeyboardView -> onKeyDown -> keyCode = " + keyCode + ", mCurrentKey = " + mCurrentKey + ", mCurrentKeyIndex = " + mCurrentKeyIndex);

        // action_down -> keycode_dpad_down
        if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            int nextIndex = focusedIndex + 10;
            if (nextIndex < mKeys.length) {
                focusedIndex = nextIndex;
                showPreview(focusedIndex);
            }
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            if (focusedIndex > 10) {
                int nextIndex = focusedIndex - 10;
                focusedIndex = nextIndex;
                showPreview(focusedIndex);
            }
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            int nextIndex = focusedIndex + 1;
            if (nextIndex < mKeys.length) {
                focusedIndex = nextIndex;
                showPreview(focusedIndex);
            }
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            if (focusedIndex > 0) {
                int nextIndex = focusedIndex - 1;
                focusedIndex = nextIndex;
                showPreview(focusedIndex);
            }
        } else if (keyCode == KeyEvent.KEYCODE_ENTER) {
            MiniKeyboard.Key mKey = mKeys[focusedIndex];
            detectAndSendKey(focusedIndex, mKey.x, mKey.y, 0);
        }

        return super.onKeyDown(keyCode, event);
    }

//    @Override
//    public boolean dispatchKeyEvent(KeyEvent event) {
//
////        return super.dispatchKeyEvent(event);
//
//
//        return true;
//    }
}