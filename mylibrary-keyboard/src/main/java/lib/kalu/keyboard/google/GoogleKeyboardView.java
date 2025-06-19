package lib.kalu.keyboard.google;

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
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.util.Arrays;
import java.util.List;

import lib.kalu.keyboard.LogUtil;
import lib.kalu.keyboard.R;

public class GoogleKeyboardView extends View {

    private boolean mKeyTouch;
    private boolean mKeyFocus;
    private Drawable mKeyBackground;
    private int mKeyVerticalCorrection;
    private int mKeyTextSize;
    private int mKeyTextColor;
    private int mKeyShadowColor;
    private float mKeyShadowRadius;

    private static final int NOT_A_KEY = -1;
    private GoogleKeyboard mKeyboard;
    private int mCurrentKeyIndex = NOT_A_KEY;
    private int mFocusKeyIndex = NOT_A_KEY;
    private OnKeyboardActionListener mKeyboardActionListener;

    private static final int DEBOUNCE_TIME = 70;

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
    private GoogleKeyboard.Key mInvalidatedKey;
    private Rect mClipRegion = new Rect(0, 0, 0, 0);

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

    public GoogleKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs, R.attr.keyboardViewStyle);
        init(context, attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public GoogleKeyboardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, 0);
        init(context, attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public GoogleKeyboardView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }


    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        try {
            if (null == mKeyboard)
                throw new Exception("error: mKeyboard null");
            int paddingLeft = getPaddingLeft();
            int paddingTop = getPaddingTop();
            int paddingRight = getPaddingRight();
            int paddingBottom = getPaddingBottom();
            LogUtil.log("GoogleKeyboardView -> onMeasure -> paddingLeft = " + paddingLeft + ", paddingTop = " + paddingTop + ", paddingRight = " + paddingRight + ", paddingBottom = " + paddingBottom);


            int totalWidth = mKeyboard.getTotalWidth();
            int allWidth = paddingLeft + paddingRight + totalWidth;
            int totalHeight = mKeyboard.getTotalHeight();
            int allHeight = paddingTop + paddingBottom + totalHeight;
            LogUtil.log("GoogleKeyboardView -> onMeasure -> totalWidth = " + totalWidth + ", allWidth = " + allWidth + ", totalHeight = " + totalHeight + ", allHeight = " + allHeight);

            int specWidth = MeasureSpec.makeMeasureSpec(allWidth, MeasureSpec.EXACTLY);
            int specHeight = MeasureSpec.makeMeasureSpec(allHeight, MeasureSpec.EXACTLY);
            super.onMeasure(specWidth, specHeight);
//            if (MeasureSpec.getSize(widthMeasureSpec) < width + 10) {
//                width = MeasureSpec.getSize(widthMeasureSpec);
//            }
//            setMeasuredDimension(width, mKeyboard.getHeight() +);
        } catch (Exception e) {
            LogUtil.log("GoogleKeyboardView -> onMeasure -> Exception " + e.getMessage());
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    private void init(Context context, AttributeSet attrs) {

        TypedArray typedArray = null;
        try {
            typedArray = context.obtainStyledAttributes(attrs, R.styleable.KeyboardView);
            mKeyTouch = typedArray.getBoolean(R.styleable.KeyboardView_keyTouch, true);
            mKeyFocus = typedArray.getBoolean(R.styleable.KeyboardView_keyFocus, false);
            mKeyBackground = typedArray.getDrawable(R.styleable.KeyboardView_keyBackground);
            mKeyVerticalCorrection = typedArray.getDimensionPixelOffset(R.styleable.KeyboardView_keyVerticalCorrection, 0);
            mKeyTextSize = typedArray.getDimensionPixelSize(R.styleable.KeyboardView_keyTextSize, 18);
            mKeyTextColor = typedArray.getColor(R.styleable.KeyboardView_keyTextColor, 0xFF000000);
            mKeyShadowColor = typedArray.getColor(R.styleable.KeyboardView_keyShadowColor, 0);
            mKeyShadowRadius = typedArray.getFloat(R.styleable.KeyboardView_keyShadowRadius, 0f);
        } catch (Exception e) {
        }
        if (null != typedArray) {
            typedArray.recycle();
        }

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(mKeyTextSize);
        mPaint.setTextAlign(Align.CENTER);
        mPaint.setAlpha(255);
        mPadding = new Rect(0, 0, 0, 0);
        mKeyBackground.getPadding(mPadding);
    }

    public final void setKeyboard(GoogleKeyboard keyboard, int defaultFocusIndex) {
        if (mKeyboard != null) {
            showPreview(NOT_A_KEY);
            closing();
        }
        mKeyboard = keyboard;
//        List<GoogleKeyboard.Key> keys = mKeyboard.getKeys();
//        mKeys = keys.toArray(new GoogleKeyboard.Key[keys.size()]);
        requestLayout();
        // Hint to reallocate the buffer if the size changed
        mKeyboardChanged = true;
        invalidateAllKeys();
        computeProximityThreshold(keyboard);
        mAbortKey = true; // Until the next ACTION_DOWN

        if (defaultFocusIndex >= 0) {
            List<GoogleKeyboard.Key> keys = mKeyboard.getKeys();
            int size = keys.size();
            if (defaultFocusIndex + 1 <= size) {
                mFocusKeyIndex = defaultFocusIndex;
                //
                showPreview(mFocusKeyIndex);
            }
        }
    }

    public GoogleKeyboard getKeyboard() {
        return mKeyboard;
    }

    public boolean setShifted(boolean shifted) {
        if (mKeyboard != null) {
            if (mKeyboard.setShifted(shifted)) {
                // The whole keyboard probably needs to be redrawn
                invalidateAllKeys();
                return true;
            }
        }
        return false;
    }

    public boolean isShifted() {
        if (mKeyboard != null) {
            return mKeyboard.isShifted();
        }
        return false;
    }

    public boolean setSymbol(boolean symbol) {
        if (mKeyboard != null) {
            if (mKeyboard.setSymbol(symbol)) {
                // The whole keyboard probably needs to be redrawn
                invalidateAllKeys();
                return true;
            }
        }
        return false;
    }

    public boolean isSymbol() {
        if (mKeyboard != null) {
            return mKeyboard.isSymbol();
        }
        return false;
    }

    private void computeProximityThreshold(GoogleKeyboard keyboard) {
        if (keyboard == null) return;
        List<GoogleKeyboard.Key> keys = mKeyboard.getKeys();
        if (keys == null) return;
        int length = keys.size();
        int dimensionSum = 0;
        for (int i = 0; i < length; i++) {
            GoogleKeyboard.Key key = keys.get(i);
            dimensionSum += Math.min(key.width, key.height) + key.hzGap;
        }
        if (dimensionSum < 0 || length == 0) return;
        mProximityThreshold = (int) (dimensionSum * 1.4f / length);
        mProximityThreshold *= mProximityThreshold; // Square it
    }

    @Override
    public void onDraw(Canvas canvas) {
        LogUtil.log("GoogleKeyboardView -> onDraw ->");
//        super.onDraw(canvas);
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

        if (mKeyboard == null) return;

        mCanvas.save();
        final Canvas canvas = mCanvas;
        canvas.clipRect(mDirtyRect);

        final Paint paint = mPaint;
        final Rect clipRegion = mClipRegion;
        final Rect padding = mPadding;
        final int kbdPaddingLeft = getPaddingLeft();
        final List<GoogleKeyboard.Key> keys = mKeyboard.getKeys();
        final int kbdPaddingTop = getPaddingTop();
        final GoogleKeyboard.Key invalidKey = mInvalidatedKey;

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
        int keyCount = keys.size();
        boolean shifted = isShifted();
        for (int i = 0; i < keyCount; i++) {
            final GoogleKeyboard.Key key = keys.get(i);
            if (drawSingleKey && invalidKey != key) {
                continue;
            }

            // fix
            if (!key.focused) {
                if (null == key.icon && null == key.text) {
                    continue;
                } else if (null == key.icon && key.text.length() == 0) {
                    continue;
                }
            }

            Drawable drawable = key.background;
            // LogUtil.log("GoogleKeyboardView -> onBufferDraw -> i = " + i + ", key.text = " + key.text + ", key.width = " + key.width);
            if (null == drawable) {
                drawable = mKeyBackground;
            }


            // background
            int[] drawableState = key.getCurrentDrawableState();
//            LogUtil.log("GoogleKeyboardView -> onBufferDraw -> i = " + i + ", drawableState = " + drawableState);
            drawable.setState(drawableState);


            // LogUtil.log("GoogleKeyboardView -> onBufferDraw -> i = " + i + ", key.text = " + key.text + ", key.width = " + key.width);

            final Rect bounds = drawable.getBounds();
            if (key.width != bounds.right || key.height != bounds.bottom) {
                int left = key.marginLeft;
                int top = 0;
                int right = key.marginLeft + key.marginRight + key.width;
                int bottom = key.height;
                drawable.setBounds(left, top, right, bottom);
            }
            LogUtil.log("GoogleKeyboardView -> onBufferDraw -> i = " + i + ", key = " + key.toString());
            canvas.translate(key.x + kbdPaddingLeft, key.y + kbdPaddingTop);
            drawable.draw(canvas);

            // icon
            if (key.icon != null) {

                float x = (key.width - padding.left - padding.right) / 2
                        + padding.left;
                float y = (key.height - padding.top - padding.bottom) / 2
                        + (paint.getTextSize() - paint.descent()) / 2 + padding.top;

                Rect iconBounds = key.icon.getBounds();
                int widthBounds = iconBounds.width();
                int heightBounds = iconBounds.height();

                float halfWidthBounds = widthBounds * 0.5f;
                float halfHeightBounds = heightBounds * 0.5f;

                int gap = key.vtGap / 2;
                int marginLeft = key.marginLeft / 2;
                int left = (int) (x - halfWidthBounds) + marginLeft;
                int top = (int) (y - halfHeightBounds) - gap;
                int right = (int) (x + halfWidthBounds) + marginLeft;
                int bottom = (int) (y + halfHeightBounds) - gap;

                if (shifted && null != key.iconUpper && mFocusKeyIndex != i) {
                    key.iconUpper.setBounds(left, top, right, bottom);
                    key.iconUpper.draw(canvas);
                } else {
                    key.icon.setBounds(left, top, right, bottom);
                    key.icon.draw(canvas);
                }

//                canvas.translate(-drawableX, -drawableY);
            }
            // txt
            else if (key.text != null) {
//                // For characters, use large font. For labels like "Done", use small font.
//                if (label.length() > 1 && key.codes.length < 2) {
//                    paint.setTextSize(mLabelTextSize);
//                    paint.setTypeface(Typeface.DEFAULT_BOLD);
//                } else {

                if (key.textSize > 0) {
                    paint.setTextSize(key.textSize);

                } else {
                    paint.setTextSize(mKeyTextSize);
                }

                paint.setTypeface(Typeface.DEFAULT);
//                }
                // Draw a drop shadow for the text
                paint.setShadowLayer(mKeyShadowRadius, 0, 0, mKeyShadowColor);
                // Draw the text
                float x = (key.width - padding.left - padding.right) / 2
                        + padding.left;
                float y = (key.height - padding.top - padding.bottom) / 2
                        + (paint.getTextSize() - paint.descent()) / 2 + padding.top;


                // drawText
                boolean symbol = isSymbol();
                if (symbol && key.supportSymbel) {
                    String text = key.symbel.toString();
                    canvas.drawText(text, x, y, paint);
                } else {
                    if (shifted && key.supportUpper) {
                        if (null != key.textUpper && key.textUpper.length() > 0) {
                            String text = key.textUpper.toString();
                            canvas.drawText(text, x, y, paint);
                        } else {
                            String text = key.text.toString().toUpperCase();
                            canvas.drawText(text, x, y, paint);
                        }
                    } else {
                        String text = key.text.toString();
                        canvas.drawText(text, x, y, paint);
                    }
                }


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
        List<GoogleKeyboard.Key> keys = mKeyboard.getKeys();
        if (keys == null) return;
        if (keyIndex < 0 || keyIndex >= keys.size()) {
            return;
        }
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        final GoogleKeyboard.Key key = keys.get(keyIndex);
        mInvalidatedKey = key;
        mDirtyRect.union(key.x + paddingLeft, key.y + paddingTop,
                key.x + key.width + paddingLeft, key.y + key.height + paddingTop);
        onBufferDraw();
        invalidate(key.x + paddingLeft, key.y + paddingTop,
                key.x + key.width + paddingLeft, key.y + key.height + paddingTop);
    }

    public void closing() {
        mBuffer = null;
        mCanvas = null;
        mFocusKeyIndex = NOT_A_KEY;
        mCurrentKeyIndex = NOT_A_KEY;
    }

    private int getKeyIndices(int x, int y, int[] allKeys) {
        List<GoogleKeyboard.Key> keys = mKeyboard.getKeys();
        int primaryIndex = NOT_A_KEY;
        int[] nearestKeyIndices = mKeyboard.getNearestKeys(x, y);
        final int keyCount = nearestKeyIndices.length;
        for (int i = 0; i < keyCount; i++) {
            final GoogleKeyboard.Key key = keys.get(nearestKeyIndices[i]);
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
        LogUtil.log("GoogleKeyboardView -> showPreview -> mCurrentKeyIndex = " + mCurrentKeyIndex + ", keyIndex = " + keyIndex);

        int oldKeyIndex = mCurrentKeyIndex;

        mCurrentKeyIndex = keyIndex;
        // Release the old key and press the new key
        final List<GoogleKeyboard.Key> keys = mKeyboard.getKeys();
        if (oldKeyIndex != mCurrentKeyIndex) {
            if (oldKeyIndex != NOT_A_KEY && keys.size() > oldKeyIndex) {
                GoogleKeyboard.Key oldKey = keys.get(oldKeyIndex);
                oldKey.onReleased(mCurrentKeyIndex == NOT_A_KEY);
                invalidateKey(oldKeyIndex);
            }
            if (mCurrentKeyIndex != NOT_A_KEY && keys.size() > mCurrentKeyIndex) {
                GoogleKeyboard.Key newKey = keys.get(mCurrentKeyIndex);
                newKey.onFocused();
                invalidateKey(mCurrentKeyIndex);
            }

            // 点击音效
            playSoundEffect(SoundEffectConstants.CLICK);
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
        List<GoogleKeyboard.Key> keys = mKeyboard.getKeys();
        if (index != NOT_A_KEY && index < keys.size()) {
            final GoogleKeyboard.Key key = keys.get(index);
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
//            }
            // 点击音效
            playSoundEffect(SoundEffectConstants.CLICK);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        // Phone
        if (mKeyTouch) {
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
        } else {
            return false;
        }
    }

    private boolean onModifiedTouchEvent(MotionEvent me, boolean possiblePoly) {
        int touchX = (int) me.getX() - getPaddingLeft();
        int touchY = (int) me.getY() - getPaddingTop();
        if (touchY >= -mKeyVerticalCorrection)
            touchY += mKeyVerticalCorrection;
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

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        LogUtil.log("GoogleKeyboardView -> onFinishInflate ->");
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        LogUtil.log("GoogleKeyboardView -> onSizeChanged ->");

        if (mKeyboard != null) {
            mKeyboard.resize(w, h);
        }
        mBuffer = null;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        LogUtil.log("GoogleKeyboardView -> onAttachedToWindow ->");
        requestFocus();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        LogUtil.log("GoogleKeyboardView -> onDetachedFromWindow ->");
        clearFocus();
        closing();
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, @Nullable Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        LogUtil.log("GoogleKeyboardView -> onFocusChanged -> gainFocus = " + gainFocus);
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
//        super.setPadding(left, top, right, bottom);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        LogUtil.log("GoogleKeyboardView -> onKeyDown -> keyCode = " + keyCode + ", mFocusKeyIndex = " + mFocusKeyIndex);
        // TV
        if (mKeyFocus) {
            // action_down -> keycode_dpad_down
            if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                if (mFocusKeyIndex <= 29) {
                    mFocusKeyIndex += 11;
                    showPreview(mFocusKeyIndex);
                } else if (mFocusKeyIndex == 30 || mFocusKeyIndex == 31) {
                    mFocusKeyIndex = 41;
                    showPreview(mFocusKeyIndex);
                } else if (mFocusKeyIndex == 32) {
                    mFocusKeyIndex = 42;
                    showPreview(mFocusKeyIndex);
                }
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                if (mFocusKeyIndex == 42) {
                    mFocusKeyIndex = 32;
                    showPreview(mFocusKeyIndex);
                } else if (mFocusKeyIndex == 41) {
                    mFocusKeyIndex = 30;
                    showPreview(mFocusKeyIndex);
                } else if (mFocusKeyIndex >= 11 && mFocusKeyIndex <= 40) {
                    mFocusKeyIndex -= 11;
                    showPreview(mFocusKeyIndex);
                }
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                List<GoogleKeyboard.Key> keys = mKeyboard.getKeys();
                if (mFocusKeyIndex + 1 < keys.size()) {
                    mFocusKeyIndex += 1;
                    showPreview(mFocusKeyIndex);
                }
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                if (mFocusKeyIndex > 0) {
                    mFocusKeyIndex -= 1;
                    showPreview(mFocusKeyIndex);
                }
            } else if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                //
                List<GoogleKeyboard.Key> keys = mKeyboard.getKeys();
                GoogleKeyboard.Key mKey = keys.get(mFocusKeyIndex);
                detectAndSendKey(mFocusKeyIndex, mKey.x, mKey.y, 0);
            }
            return true;
        } else {
            return false;
        }
    }

    /***********/

    public interface OnKeyboardActionListener {

        void onKey(int primaryCode, GoogleKeyboard.Key key);
    }

    public void setOnKeyboardActionListener(OnKeyboardActionListener listener) {
        mKeyboardActionListener = listener;
    }
}