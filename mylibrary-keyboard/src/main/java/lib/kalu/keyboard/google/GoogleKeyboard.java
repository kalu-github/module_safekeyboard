package lib.kalu.keyboard.google;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.util.Xml;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import lib.kalu.keyboard.LogUtil;
import lib.kalu.keyboard.R;

public class GoogleKeyboard {

    // Keyboard XML Tags
    private static final String TAG_KEYBOARD = "Keyboard";
    private static final String TAG_ROW = "Row";
    private static final String TAG_KEY = "Key";


    public String mDefaultLanguageCode;

    /**
     * Horizontal gap default for all rows
     */
    private int mDefaultHorizontalGap;

    /**
     * Default key width
     */
    private int mDefaultWidth;

    /**
     * Default key height
     */
    private int mDefaultHeight;

    /**
     * Default gap between rows
     */
    private int mDefaultVerticalGap;


    /**
     * 符号键盘
     */
    private boolean mSymbol;

    /**
     * Is the keyboard in the shifted state
     */
    private boolean mShifted;

    /**
     * Key instance for the shift key, if present
     */
    private Key[] mShiftKeys = {null, null};

    /**
     * Key index for the shift key, if present
     */
    private int[] mShiftKeyIndices = {-1, -1};

    /**
     * Current key width, while loading the keyboard
     */
    private int mKeyWidth;

    /**
     * Current key height, while loading the keyboard
     */
    private int mKeyHeight;

    /**
     * Total height of the keyboard, including the padding and keys
     */
    private int mTotalHeight;

    /**
     * Total width of the keyboard, including left side gaps and keys, but not any gaps on the
     * right side.
     */
    private int mTotalWidth;

    /**
     * List of keys in this keyboard
     */
    private List<Key> mKeys;

    /**
     * Width of the screen available to fit the keyboard
     */
    private int mDisplayWidth;

    /**
     * Height of the screen
     */
    private int mDisplayHeight;

    private static final int GRID_WIDTH = 10;
    private static final int GRID_HEIGHT = 5;
    private static final int GRID_SIZE = GRID_WIDTH * GRID_HEIGHT;
    private int mCellWidth;
    private int mCellHeight;
    private int[][] mGridNeighbors;
    private int mProximityThreshold;
    /**
     * Number of key widths from current touch point to search for nearest keys.
     */
    private static float SEARCH_DISTANCE = 1.8f;

    private ArrayList<Row> rows = new ArrayList<Row>();


    public ArrayList<Row> getRows() {
        return rows;
    }

    /**
     * Container for keys in the keyboard. All keys in a row are at the same Y-coordinate.
     * Some of the key size defaults can be overridden per row from what the {@link GoogleKeyboard}
     * defines.
     *
     * @attr ref android.R.styleable#Keyboard_keyWidth
     * @attr ref android.R.styleable#Keyboard_keyHeight
     * @attr ref android.R.styleable#Keyboard_horizontalGap
     * @attr ref android.R.styleable#Keyboard_verticalGap
     * @attr ref android.R.styleable#Keyboard_Row_rowEdgeFlags
     * @attr ref android.R.styleable#Keyboard_Row_keyboardMode
     */
    public static class Row {
        /**
         * Default width of a key in this row.
         */
        public int defaultWidth;
        /**
         * Default height of a key in this row.
         */
        public int defaultHeight;
        /**
         * Default horizontal gap between keys in this row.
         */
        public int defaultHorizontalGap;
        /**
         * Vertical gap following this row.
         */
        public int verticalGap;

        public ArrayList<Key> mRowKeys = new ArrayList<>();

        private GoogleKeyboard parent;

        public Row(GoogleKeyboard parent) {
            this.parent = parent;
        }

        public Row(Resources res, GoogleKeyboard parent, XmlResourceParser parser) {
            this.parent = parent;
            TypedArray a = res.obtainAttributes(Xml.asAttributeSet(parser),
                    R.styleable.Keyboard);
            defaultWidth = getDimensionOrFraction(a, R.styleable.Keyboard_keyWidth,
                    parent.mDisplayWidth, parent.mDefaultWidth);
            defaultHeight = getDimensionOrFraction(a, R.styleable.Keyboard_keyHeight,
                    parent.mDisplayHeight, parent.mDefaultHeight);
            defaultHorizontalGap = getDimensionOrFraction(a, R.styleable.Keyboard_horizontalGap,
                    parent.mDisplayWidth, parent.mDefaultHorizontalGap);
            verticalGap = getDimensionOrFraction(a, R.styleable.Keyboard_verticalGap,
                    parent.mDisplayHeight, parent.mDefaultVerticalGap);
            a.recycle();
        }
    }

    /**
     * Class for describing the position and characteristics of a single key in the keyboard.
     *
     * @attr ref android.R.styleable#Keyboard_keyWidth
     * @attr ref android.R.styleable#Keyboard_keyHeight
     * @attr ref android.R.styleable#Keyboard_horizontalGap
     * @attr ref android.R.styleable#Keyboard_Key_codes
     * @attr ref android.R.styleable#Keyboard_Key_keyIcon
     * @attr ref android.R.styleable#Keyboard_Key_keyLabel
     * @attr ref android.R.styleable#Keyboard_Key_iconPreview
     * @attr ref android.R.styleable#Keyboard_Key_isSticky
     * @attr ref android.R.styleable#Keyboard_Key_isRepeatable
     * @attr ref android.R.styleable#Keyboard_Key_isModifier
     * @attr ref android.R.styleable#Keyboard_Key_popupKeyboard
     * @attr ref android.R.styleable#Keyboard_Key_popupCharacters
     * @attr ref android.R.styleable#Keyboard_Key_keyOutputText
     * @attr ref android.R.styleable#Keyboard_Key_keyEdgeFlags
     */
    public static class Key {

        private int paddingLeft;
        private int paddingRight;
        public int marginLeft;
        public int marginRight;

        public Drawable background;

        public CharSequence symbel;
        // 符号
        public boolean supportSymbel;
        // 大写
        public boolean supportUpper;

        /**
         * All the key codes (unicode or custom code) that this key could generate, zero'th
         * being the most important.
         */
        public int code;

        /**
         * Label to display
         */
        public int textSize;
        public CharSequence text;
        public CharSequence textUpper;
        public CharSequence mult;
        public CharSequence multUpper;

        /**
         * Icon to display instead of a label. Icon takes precedence over a label
         */
        public Drawable icon;
        public Drawable iconUpper;
        private int iconWidth;
        private int iconHeight;

        /**
         * Width of the key, not including the gap
         */
        public int width;
        /**
         * Height of the key, not including the gap
         */
        public int height;
        /**
         * The horizontal gap before this key
         */
        public int vtGap;
        public int hzGap;
        /**
         * X coordinate of the key in the keyboard layout
         */
        public int x;
        /**
         * Y coordinate of the key in the keyboard layout
         */
        public int y;
        /**
         * The current focused state of this key
         */
        public boolean focused;

        public boolean shifted;
        /**
         * The keyboard that this key belongs to
         */
        private GoogleKeyboard keyboard;

        @Override
        public String toString() {
            return "{" +
                    "paddingLeft=" + paddingLeft +
                    ", paddingRight=" + paddingRight +
                    ", marginLeft=" + marginLeft +
                    ", marginRight=" + marginRight +
                    ", background=" + background +
                    ", symbel=" + symbel +
                    ", supportSymbel=" + supportSymbel +
                    ", supportUpper=" + supportUpper +
                    ", code=" + code +
                    ", textSize=" + textSize +
                    ", text=" + text +
                    ", textUpper=" + textUpper +
                    ", mult=" + mult +
                    ", multUpper=" + multUpper +
                    ", icon=" + icon +
                    ", iconUpper=" + iconUpper +
                    ", iconWidth=" + iconWidth +
                    ", iconHeight=" + iconHeight +
                    ", width=" + width +
                    ", height=" + height +
                    ", vtGap=" + vtGap +
                    ", hzGap=" + hzGap +
                    ", x=" + x +
                    ", y=" + y +
                    ", focused=" + focused +
                    ", keyboard=" + keyboard +
                    '}';
        }

        private final static int[] KEY_STATE_FOCUSED = {
                android.R.attr.state_focused,
        };

        private final static int[] KEY_STATE_NORMAL = {
        };

        /**
         * Create an empty key with no attributes.
         */
        public Key(Row parent) {
            keyboard = parent.parent;
            height = parent.defaultHeight;
            width = parent.defaultWidth;
            LogUtil.log("GoogleKeyboard33 -> width = " + width);
            vtGap = parent.defaultHorizontalGap;
            hzGap = parent.parent.mDefaultVerticalGap;
        }

        /**
         * Create a key with the given top-left coordinate and extract its attributes from
         * the XML parser.
         *
         * @param res    resources associated with the caller's context
         * @param parent the row that this key belongs to. The row must already be attached to
         *               a {@link GoogleKeyboard}.
         * @param x      the x coordinate of the top-left
         * @param y      the y coordinate of the top-left
         * @param parser the XML parser containing the attributes for this key
         */
        public Key(Resources res, Row parent, int x, int y, XmlResourceParser parser) {
            this(parent);

            this.x = x;
            this.y = y;

            TypedArray a = res.obtainAttributes(Xml.asAttributeSet(parser), R.styleable.Keyboard);

            width = getDimensionOrFraction(a, R.styleable.Keyboard_keyWidth,
                    keyboard.mDisplayWidth, parent.defaultWidth);
            height = getDimensionOrFraction(a, R.styleable.Keyboard_keyHeight,
                    keyboard.mDisplayHeight, parent.defaultHeight);
            hzGap = getDimensionOrFraction(a, R.styleable.Keyboard_horizontalGap,
                    keyboard.mDisplayWidth, parent.defaultHorizontalGap);
            a.recycle();
            a = res.obtainAttributes(Xml.asAttributeSet(parser), R.styleable.Keyboard_Key);

//            if (x > 0) {
//                this.x += hzGap;
//            }
//            TypedValue codesValue = new TypedValue();
//            a.getValue(R.styleable.Keyboard_Key_code, codesValue);
//            if (codesValue.type == TypedValue.TYPE_INT_DEC
//                    || codesValue.type == TypedValue.TYPE_INT_HEX) {
//                codes = new int[]{codesValue.data};
//            } else if (codesValue.type == TypedValue.TYPE_STRING) {
//                codes = parseCSV(codesValue.string.toString());
//            }

            iconWidth = a.getDimensionPixelOffset(R.styleable.Keyboard_Key_iconWidth, 0);
            iconHeight = a.getDimensionPixelOffset(R.styleable.Keyboard_Key_iconHeight, 0);

            // icon
            icon = a.getDrawable(R.styleable.Keyboard_Key_icon);
            if (icon != null) {
                if (iconWidth > 0 && iconHeight > 0) {
                    icon.setBounds(0, 0, iconWidth, iconHeight);
                } else {
                    icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
                }
            }

            // iconUpper
            iconUpper = a.getDrawable(R.styleable.Keyboard_Key_iconUpper);
            if (iconUpper != null) {
                if (iconWidth > 0 && iconHeight > 0) {
                    iconUpper.setBounds(0, 0, iconWidth, iconHeight);
                } else {
                    iconUpper.setBounds(0, 0, iconUpper.getIntrinsicWidth(), iconUpper.getIntrinsicHeight());
                }
            }

//            return values;

            //
            textUpper = a.getText(R.styleable.Keyboard_Key_textUpper);

            //
            symbel = a.getText(R.styleable.Keyboard_Key_symbel);
            supportSymbel = a.getBoolean(R.styleable.Keyboard_Key_supportSymbel, false);
            supportUpper = a.getBoolean(R.styleable.Keyboard_Key_supportUpper, false);
            code = a.getInt(R.styleable.Keyboard_Key_code, -1);

            mult = a.getText(R.styleable.Keyboard_Key_mult);
            multUpper = a.getText(R.styleable.Keyboard_Key_multUpper);

            text = a.getText(R.styleable.Keyboard_Key_text);
            LogUtil.log("GoogleKeyboard22 -> Key -> text = " + text);
            textSize = a.getDimensionPixelSize(R.styleable.Keyboard_Key_textSize, 0);
            LogUtil.log("GoogleKeyboard22 -> Key -> textSize = " + textSize);


            background = a.getDrawable(R.styleable.Keyboard_Key_background);

            // padding
            paddingLeft = getDimensionOrFraction(a, R.styleable.Keyboard_Key_paddingLeft, keyboard.mDisplayWidth, 0);
            width += paddingLeft;
            LogUtil.log("GoogleKeyboard33 -> width = " + width);
            paddingRight = getDimensionOrFraction(a, R.styleable.Keyboard_Key_paddingRight, keyboard.mDisplayWidth, 0);
            width += paddingRight;
            LogUtil.log("GoogleKeyboard33 -> width = " + width);

            //margin
            marginLeft = getDimensionOrFraction(a, R.styleable.Keyboard_Key_marginLeft, keyboard.mDisplayWidth, 0);
            marginRight = getDimensionOrFraction(a, R.styleable.Keyboard_Key_marginRight, keyboard.mDisplayWidth, 0);

            a.recycle();
        }

        /**
         * Informs the key that it has been focused, in case it needs to change its appearance or
         * state.
         *
         * @see #onReleased(boolean)
         */
        public void onFocused() {
            focused = !focused;
        }

        /**
         * Changes the focused state of the key.
         *
         * <p>Toggled state of the key will be flipped when all the following conditions are
         * fulfilled:</p>
         *
         * <ul>
         *     <li>This is a sticky key, that is {@code true}.
         *     <li>The parameter {@code inside} is {@code true}.
         *     <li>{@link android.os.Build.VERSION#SDK_INT} is greater than
         *         {@link android.os.Build.VERSION_CODES#LOLLIPOP_MR1}.
         * </ul>
         *
         * @param inside whether the finger was released inside the key. Works only on Android M and
         *               later. See the method document for details.
         * @see #onFocused()
         */
        public void onReleased(boolean inside) {
            focused = !focused;
            if (inside) {
                shifted = !shifted;
            }
        }

        /**
         * Detects if a point falls inside this key.
         *
         * @param x the x-coordinate of the point
         * @param y the y-coordinate of the point
         * @return whether or not the point falls inside the key. If the key is attached to an edge,
         * it will assume that all points between the key and the edge are considered to be inside
         * the key.
         */
        public boolean isInside(int x, int y) {
//            boolean leftEdge = (edgeFlags & EDGE_LEFT) > 0;
//            boolean rightEdge = (edgeFlags & EDGE_RIGHT) > 0;
//            boolean topEdge = (edgeFlags & EDGE_TOP) > 0;
//            boolean bottomEdge = (edgeFlags & EDGE_BOTTOM) > 0;
            if ((x >= this.x || (x <= this.x + this.width))
                    && (x < this.x + this.width || (x >= this.x))
                    && (y >= this.y || (y <= this.y + this.height))
                    && (y < this.y + this.height || (y >= this.y))) {
                return true;
            } else {
                return false;
            }
        }

        /**
         * Returns the square of the distance between the center of the key and the given point.
         *
         * @param x the x-coordinate of the point
         * @param y the y-coordinate of the point
         * @return the square of the distance of the point from the center of the key
         */
        public int squaredDistanceFrom(int x, int y) {
            int xDist = this.x + width / 2 - x;
            int yDist = this.y + height / 2 - y;
            return xDist * xDist + yDist * yDist;
        }

        /**
         * Returns the drawable state for the key, based on the current state and type of the key.
         *
         * @return the drawable state of the key.
         * @see android.graphics.drawable.StateListDrawable#setState(int[])
         */
        public int[] getCurrentDrawableState() {
            if (focused) {
                return KEY_STATE_FOCUSED;
            } else {
                return KEY_STATE_NORMAL;
            }
        }
    }

    /**
     * Creates a keyboard from the given xml key layout file. Weeds out rows
     * that have a keyboard mode defined but don't match the specified mode.
     *
     * @param context        the application or service context
     * @param xmlLayoutResId the resource file that contains the keyboard layout and keys.
     */
    public GoogleKeyboard(Context context, int xmlLayoutResId) {

        // Android 7.0 (API 24) 及以下
        Locale locale = context.getResources().getConfiguration().locale;
        // Android 8.0 (API 26) 及以上
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            locale = context.getResources().getConfiguration().getLocales().get(0);
        }
        String language = locale.getLanguage();
        String country = locale.getCountry();
        StringBuilder builder = new StringBuilder();
        if (null != language && language.length() > 0) {
            builder.append(language);
        }
        if (null != country && country.length() > 0) {
            builder.append("_");
            builder.append(country);
        }
        mDefaultLanguageCode = builder.toString();

        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        mDisplayWidth = dm.widthPixels;
        mDisplayHeight = dm.heightPixels;
        LogUtil.log("GoogleKeyboard33 -> mDisplayWidth = " + mDisplayWidth + ", mDisplayHeight = " + mDisplayHeight);
        //Log.v(TAG, "keyboard's display metrics:" + dm);

        mDefaultHorizontalGap = 0;
        mDefaultWidth = mDisplayWidth / 10;
        mDefaultVerticalGap = 0;
        mDefaultHeight = mDefaultWidth;
        LogUtil.log("GoogleKeyboard33 -> mDefaultHorizontalGap = " + mDefaultHorizontalGap + ", mDefaultWidth = " + mDefaultWidth + ", mDefaultVerticalGap = " + mDefaultVerticalGap + ", mDefaultHeight = " + mDefaultHeight);
        mKeys = new ArrayList<>();
        loadKeyboard(context, context.getResources().getXml(xmlLayoutResId));
    }

    final void resize(int newWidth, int newHeight) {
        int numRows = rows.size();
        for (int rowIndex = 0; rowIndex < numRows; ++rowIndex) {
            Row row = rows.get(rowIndex);
            int numKeys = row.mRowKeys.size();
            int totalGap = 0;
            int totalWidth = 0;
            for (int keyIndex = 0; keyIndex < numKeys; ++keyIndex) {
                Key key = row.mRowKeys.get(keyIndex);
                if (keyIndex > 0) {
                    totalGap += key.hzGap;
                }
                totalWidth += key.width;
            }
            if (totalGap + totalWidth > newWidth) {
                int x = 0;
                float scaleFactor = (float) (newWidth - totalGap) / totalWidth;
                LogUtil.log("GoogleKeyboard33 -> scaleFactor = " + scaleFactor);
                for (int keyIndex = 0; keyIndex < numKeys; ++keyIndex) {
                    Key key = row.mRowKeys.get(keyIndex);
                    key.width *= scaleFactor;
                    key.x = x;
                    x += key.width + key.hzGap;
                }
            }
        }
        mTotalWidth = newWidth;
//        // TODO: This does not adjust the vertical placement according to the new size.
//        // The main problem in the previous code was horizontal placement/size, but we should
//        // also recalculate the vertical sizes/positions when we get this resize call.
    }

    public List<Key> getKeys() {
        return mKeys;
    }

    protected int getHorizontalGap() {
        return mDefaultHorizontalGap;
    }

    protected void setHorizontalGap(int gap) {
        mDefaultHorizontalGap = gap;
    }

    protected int getVerticalGap() {
        return mDefaultVerticalGap;
    }

    protected void setVerticalGap(int gap) {
        mDefaultVerticalGap = gap;
    }

    protected int getKeyHeight() {
        return mDefaultHeight;
    }

    protected void setKeyHeight(int height) {
        mDefaultHeight = height;
    }

    protected int getKeyWidth() {
        return mDefaultWidth;
    }

    protected void setKeyWidth(int width) {
        mDefaultWidth = width;
    }

    /**
     * Returns the total height of the keyboard
     *
     * @return the total height of the keyboard
     */
    public int getTotalHeight() {
        return mTotalHeight;
    }

    public int getTotalWidth() {
        return mTotalWidth;
    }

    public boolean setShifted(boolean shiftState) {
        for (Key key : mShiftKeys) {
            if (key != null) {
                key.shifted = shiftState;
            }
        }
        if (mShifted != shiftState) {
            mShifted = shiftState;
            return true;
        }
        return false;
    }

    public boolean isShifted() {
        return mShifted;
    }

    public boolean setSymbol(boolean symbolState) {
        if (mSymbol != symbolState) {
            mSymbol = symbolState;
            return true;
        }
        return false;
    }

    public boolean isSymbol() {
        return mSymbol;
    }

    /**
     * @hide
     */
    public int[] getShiftKeyIndices() {
        return mShiftKeyIndices;
    }

    public int getShiftKeyIndex() {
        return mShiftKeyIndices[0];
    }

    private void computeNearestNeighbors() {
        // Round-up so we don't have any pixels outside the grid
        mCellWidth = (getTotalWidth() + GRID_WIDTH - 1) / GRID_WIDTH;
        mCellHeight = (getTotalHeight() + GRID_HEIGHT - 1) / GRID_HEIGHT;
        mGridNeighbors = new int[GRID_SIZE][];
        int[] indices = new int[mKeys.size()];
        final int gridWidth = GRID_WIDTH * mCellWidth;
        final int gridHeight = GRID_HEIGHT * mCellHeight;
        for (int x = 0; x < gridWidth; x += mCellWidth) {
            for (int y = 0; y < gridHeight; y += mCellHeight) {
                int count = 0;
                for (int i = 0; i < mKeys.size(); i++) {
                    final Key key = mKeys.get(i);
                    if (key.squaredDistanceFrom(x, y) < mProximityThreshold ||
                            key.squaredDistanceFrom(x + mCellWidth - 1, y) < mProximityThreshold ||
                            key.squaredDistanceFrom(x + mCellWidth - 1, y + mCellHeight - 1)
                                    < mProximityThreshold ||
                            key.squaredDistanceFrom(x, y + mCellHeight - 1) < mProximityThreshold) {
                        indices[count++] = i;
                    }
                }
                int[] cell = new int[count];
                System.arraycopy(indices, 0, cell, 0, count);
                mGridNeighbors[(y / mCellHeight) * GRID_WIDTH + (x / mCellWidth)] = cell;
            }
        }
    }

    /**
     * Returns the indices of the keys that are closest to the given point.
     *
     * @param x the x-coordinate of the point
     * @param y the y-coordinate of the point
     * @return the array of integer indices for the nearest keys to the given point. If the given
     * point is out of range, then an array of size zero is returned.
     */
    public int[] getNearestKeys(int x, int y) {
        if (mGridNeighbors == null) computeNearestNeighbors();
        if (x >= 0 && x < getTotalWidth() && y >= 0 && y < getTotalHeight()) {
            int index = (y / mCellHeight) * GRID_WIDTH + (x / mCellWidth);
            if (index < GRID_SIZE) {
                return mGridNeighbors[index];
            }
        }
        return new int[0];
    }

    protected Row createRowFromXml(Resources res, XmlResourceParser parser) {
        return new Row(res, this, parser);
    }

    protected Key createKeyFromXml(Resources res, Row parent, int x, int y,
                                   XmlResourceParser parser) {
        return new Key(res, parent, x, y, parser);
    }

    private void loadKeyboard(Context context, XmlResourceParser parser) {
        boolean inKey = false;
        boolean inRow = false;
        int row = 0;
        int x = 0;
        int y = 0;
        Key key = null;
        Row currentRow = null;
        Resources res = context.getResources();

        try {
            int event;
            int index = 0;
            while ((event = parser.next()) != XmlResourceParser.END_DOCUMENT) {
                if (event == XmlResourceParser.START_TAG) {
                    String tag = parser.getName();
                    if (TAG_ROW.equals(tag)) {
//                        LogUtil.log("GoogleKeyboard -> loadKeyboard -> XmlResourceParser.START_TAG.TAG_ROW -> index = " + index + ", row = " + row + ", x = " + x + ", key.width = " + key.width + ", key.height = " + key.height);
                        inRow = true;
                        x = 0;
                        currentRow = createRowFromXml(res, parser);
                        rows.add(currentRow);
                    } else if (TAG_KEY.equals(tag)) {
                        inKey = true;
                        key = createKeyFromXml(res, currentRow, x, y, parser);
                        LogUtil.log("GoogleKeyboard -> loadKeyboard -> TAG_KEY -> index = " + index + ", row = " + row + ", x = " + x + ", key.width = " + key.width + ", key.height = " + key.height + ", key.x = " + key.x + ", key.y = " + key.y);
                        mKeys.add(key);
                        currentRow.mRowKeys.add(key);
                    } else if (TAG_KEYBOARD.equals(tag)) {
                        parseKeyboardAttributes(res, parser);
                    }
                } else if (event == XmlResourceParser.END_TAG) {
                    if (inKey) {
                        LogUtil.log("GoogleKeyboard -> loadKeyboard -> XmlResourceParser.END_TAG -> index = " + index + ", row = " + row + ", x = " + x + ", key.width = " + key.width + ", key.height = " + key.height);
                        inKey = false;

                        x += key.hzGap;
                        x += key.width;

                        if (x > mTotalWidth) {
                            mTotalWidth = x;
                        }

                        ++index;
                    } else if (inRow) {
                        inRow = false;
                        y += currentRow.verticalGap;
                        y += currentRow.defaultHeight;
                        row++;
                    } else {
                        // TODO: error or extend?
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.log("GoogleKeyboard -> loadKeyboard -> Exception " + e.getMessage());
        }
        mTotalHeight = y - mDefaultVerticalGap;
        mTotalWidth -= mDefaultHorizontalGap;
    }

    private void parseKeyboardAttributes(Resources res, XmlResourceParser parser) {
        TypedArray a = res.obtainAttributes(Xml.asAttributeSet(parser), R.styleable.Keyboard);

        mDefaultWidth = getDimensionOrFraction(a, R.styleable.Keyboard_keyWidth,
                mDisplayWidth, mDisplayWidth / 10);
        mDefaultHeight = getDimensionOrFraction(a, R.styleable.Keyboard_keyHeight,
                mDisplayHeight, 50);
        mDefaultHorizontalGap = getDimensionOrFraction(a, R.styleable.Keyboard_horizontalGap,
                mDisplayWidth, 0);
        mDefaultVerticalGap = getDimensionOrFraction(a, R.styleable.Keyboard_verticalGap,
                mDisplayHeight, 0);
        mProximityThreshold = (int) (mDefaultWidth * SEARCH_DISTANCE);
        mProximityThreshold = mProximityThreshold * mProximityThreshold; // Square it for comparison
        a.recycle();
    }

    static int getDimensionOrFraction(TypedArray a, int index, int base, int defValue) {
        TypedValue value = a.peekValue(index);
        if (value == null) return defValue;
        if (value.type == TypedValue.TYPE_DIMENSION) {
            return a.getDimensionPixelOffset(index, defValue);
        } else if (value.type == TypedValue.TYPE_FRACTION) {
            // Round it to avoid values like 47.9999 from getting truncated
            return Math.round(a.getFraction(index, base, base, defValue));
        }
        return defValue;
    }
}
