package lib.kalu.input;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lib.kalu.input.core.MiniKeyboard;
import lib.kalu.input.core.MiniKeyboardView;

final class CusKeyboardView extends MiniKeyboardView implements MiniKeyboardView.OnKeyboardActionListener {


    public CusKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnKeyboardActionListener(this);
        show();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public CusKeyboardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOnKeyboardActionListener(this);
        show();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public CusKeyboardView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setOnKeyboardActionListener(this);
        show();
    }

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        CusUtil.log("CusKeyboardView -> onKey -> primaryCode = " + primaryCode + ", keyCodes.length = " + keyCodes.length);


        // Test
        if (primaryCode == 101 || primaryCode == 201 || primaryCode == 301 || primaryCode == 401) {
            showPopu(primaryCode);
        }
        // Test
        else if (primaryCode == 104 || primaryCode == 204 || primaryCode == 304 || primaryCode == 404) {
            showPopu(primaryCode);
        }
        // 删除
        else if (primaryCode == 111) {
            delete();
        }
        // 默认
        else {
            input(primaryCode);
        }
    }

    @Override
    public void onPress(int primaryCode) {

//        // 震动
//        try {
//            Vibrator vib = (Vibrator) getContext().getSystemService(Service.VIBRATOR_SERVICE);
//            vib.vibrate(12);
//        } catch (Exception e) {
//        }

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
        //   SafeKeyboardLogUtil.log("onText => text = " + text);
    }

    @Override
    public void onRelease(int primaryCode) {
    }

    /*******************************************************************************************/

    /**
     * 切换大小写
     */
    private void shiftKeyboard() {

        // 字母键盘
        if (null == getKeyboard())
            return;

        List<MiniKeyboard.Key> keys = getKeyboard().getKeys();
        if (null == keys || keys.size() == 0)
            return;

        // 大写状态
        boolean shifted = getKeyboard().isShifted();

        if (shifted) {
            // 大写切换小写
            getKeyboard().setShifted(false);
            String temp = "abcdefghijklmnopqrstuvwxyz";
            for (MiniKeyboard.Key key : keys) {
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
            for (MiniKeyboard.Key key : keys) {
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

    private void show() {
        setKeyboard(new MiniKeyboard(getContext(), R.xml.moudle_safe_keyboard_letter, false, false));
    }


    private void showPopu(int code) {
        try {

            MiniKeyboard miniKeyboard = getKeyboard();
            ArrayList<MiniKeyboard.Row> rows = miniKeyboard.getRows();
            for (MiniKeyboard.Row row : rows) {

                int verticalGap = row.verticalGap;
                CusUtil.log("CusKeyboardView -> showPopu -> verticalGap = " + verticalGap);

                for (MiniKeyboard.Key key : row.mKeys) {

                    int code1 = key.codes[0];
                    CusUtil.log("CusKeyboardView -> showPopu -> code1 = " + code1 + ", key.codes = " + Arrays.toString(key.codes));
                    if (code1 != code)
                        continue;

                    int width = key.width;
                    int height = key.height;
                    int horizontalGap = key.gap;
                    CusUtil.log("CusKeyboardView -> showPopu -> width = " + width + ", height = " + height + ", horizontalGap = " + horizontalGap);

//                    LinearLayout linearLayout = new LinearLayout(getContext());
//                    linearLayout.setFocusable(false);
//                    linearLayout.setOrientation(LinearLayout.VERTICAL);
//                    linearLayout.setGravity(Gravity.CENTER);
//                    linearLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//
//                    for (int i = 0; i <= 2; i++) {
//
//                        if (i == 1) {
//
//                            LinearLayout subLinearLayout = new LinearLayout(getContext());
//                            subLinearLayout.setFocusable(false);
//                            subLinearLayout.setClipChildren(false);
//                            subLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
//                            subLinearLayout.setGravity(Gravity.CENTER);
//                            subLinearLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//
//                            for (int j = 0; j <= 2; j++) {
//
//
//                                TextView textView = new TextView(getContext());
//                                textView.setFocusable(true);
//                                textView.setGravity(Gravity.CENTER);
//                                textView.setBackgroundResource(R.drawable.moudle_safe_keyboard_background_popu);
//                                if (j == 0) {
//                                    textView.setText("left");
//                                } else if (j == 1) {
//                                    textView.setText("center");
//                                } else {
//                                    textView.setText("right");
//                                }
//                                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, height);
//                                if (j == 1) {
//                                    layoutParams.setMargins(horizontalGap, verticalGap, horizontalGap, verticalGap);
//                                }
//                                textView.setLayoutParams(layoutParams);
//                                subLinearLayout.addView(textView);
//                            }
//
//                            linearLayout.addView(subLinearLayout);
//
//                        } else {
//                            TextView textView = new TextView(getContext());
//                            textView.setFocusable(true);
//                            textView.setGravity(Gravity.CENTER);
//                            textView.setBackgroundResource(R.drawable.moudle_safe_keyboard_background_popu);
//
//                            if (i == 0) {
//                                textView.setText("top");
//                            } else {
//                                textView.setText("bottom");
//                            }
//
//                            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, height);
//                            textView.setLayoutParams(layoutParams);
//                            linearLayout.addView(textView);
//                        }

//                        /**
//                         * // 3. 将按钮放置在另一个视图（R.id.another_view）的右侧
//                         * layoutParams.addRule(RelativeLayout.RIGHT_OF, R.id.another_view);
//                         *
//                         * // 4. 让按钮的顶部与另一个视图的底部对齐
//                         * layoutParams.addRule(RelativeLayout.BELOW, R.id.another_view);
//                         */
//
//
//                        if (i == 0) {
//                            textView.setId(R.id.keyboard_id_popu_center);
//                            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
//                        } else if (i == 1) {
////                            layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
////                            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
//                            layoutParams.addRule(RelativeLayout.ABOVE, R.id.keyboard_id_popu_center);
//                        } else if (i == 2) {
////                            layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
////                            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
//                            layoutParams.addRule(RelativeLayout.BELOW, R.id.keyboard_id_popu_center);
//                        }
//
//                        textView.setLayoutParams(layoutParams);
//                        relativeLayout.addView(textView);
//                    }

                    View inflate = LayoutInflater.from(getContext()).inflate(R.layout.popu_layout, null);
                    List<Integer> list = Arrays.asList(R.id.popu_center, R.id.popu_left, R.id.popu_top, R.id.popu_right, R.id.popu_bottom);
                    for (Integer integer : list) {
                        View viewById = inflate.findViewById(integer);
                        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) viewById.getLayoutParams();
                        layoutParams.width = width;
                        layoutParams.height = height;
                        if (integer == R.id.popu_center) {
                            layoutParams.setMargins(horizontalGap, 0, horizontalGap, 0);
                        } else if (integer == R.id.popu_top) {
                            layoutParams.setMargins(0, 0, 0, verticalGap);
                        } else if (integer == R.id.popu_bottom) {
                            layoutParams.setMargins(0, verticalGap, 0, 0);
                        }
                        viewById.setLayoutParams(layoutParams);
                    }

                    PopupWindow popupWindow = new PopupWindow(
                            inflate, // 内容视图
                            ViewGroup.LayoutParams.WRAP_CONTENT, // 宽度
                            ViewGroup.LayoutParams.WRAP_CONTENT // 高度
                    );
                    // 设置背景，使点击外部可关闭弹窗
                    popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    popupWindow.setFocusable(true);
                    popupWindow.setOutsideTouchable(false);
                    // 设置动画
//            popupWindow.setAnimationStyle(R.style.PopupAnimation);
//                    // 1. 相对于某个锚点视图显示
//                    popupWindow.showAsDropDown(anchorView); // 在锚点视图下方显示
//                    // 2. 相对于某个锚点视图显示，并设置偏移量
                    /**
                     * anchor：锚定视图。
                     * xoff：X 轴偏移量（像素），正值向右偏移，负值向左偏移。
                     * yoff：Y 轴偏移量（像素），正值向下偏移，负值向上偏移。
                     * gravity：对齐方式，例如 Gravity.START | Gravity.BOTTOM。
                     */
                    int xoff = key.x - key.width + getLeft() / 3 * 2;
                    int yoff = key.y - getTop();
                    View viewById = ((ViewGroup) getParent()).findViewById(R.id.moudle_safe_id_baseline);

                    CusUtil.log("CusKeyboardView -> showPopu -> xoff = " + xoff + ", yoff = " + yoff + ", height = " + height);
                    popupWindow.showAsDropDown(viewById, xoff, yoff, Gravity.NO_GRAVITY);
                    return;
                }
            }

//            List<Keyboard.Key> keys = keyboard.getKeys();
//            SafeKeyboardLogUtil.log("CusKeyboardView -> showPopu -> keys = " + keys);
//            for (Keyboard.Key key : keys) {
//
//            }
        } catch (Exception e) {
        }
    }

    /**
     * 输入加密
     *
     * @param code
     */
    private void input(int code) {
        CusUtil.log("CusKeyboardView -> input -> code = " + code + ", mOnKeyChangeListener = " + mOnKeyChangeListener);
        if (null != mOnKeyChangeListener) {

            List<MiniKeyboard.Key> keys = getKeyboard().getKeys();
            CusUtil.log("CusKeyboardView -> input -> keys = " + keys);
            for (MiniKeyboard.Key key : keys) {
                int code1 = key.codes[0];
                CusUtil.log("CusKeyboardView -> input -> code1 = " + code1 + ", key.codes = " + key.codes.toString());
                if (code1 == code) {
                    mOnKeyChangeListener.onInput(key.label);
                    break;
                }

            }
        }
    }

    /**
     * 删除字符
     */
    private void delete() {
        CusUtil.log("delete =>");
        if (null != mOnKeyChangeListener) {
            mOnKeyChangeListener.onDelete();
        }
    }

    /********************************/

    private OnKeyChangeListener mOnKeyChangeListener;

    public void setOnKeyChangeListener(@NonNull OnKeyChangeListener listener) {
        this.mOnKeyChangeListener = listener;
    }

    public interface OnKeyChangeListener {

        void onInput(CharSequence text);

        void onDelete();
    }
}
