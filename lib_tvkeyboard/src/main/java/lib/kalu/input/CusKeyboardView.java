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
import android.widget.TextView;
import android.widget.Toast;

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
    public void onRelease(int primaryCode) {
    }

    @Override
    public void onKey(int primaryCode, MiniKeyboard.Key key) {
        CusUtil.log("CusKeyboardView -> onKey -> primaryCode = " + primaryCode + ", codes = " + Arrays.toString(key.codeExtra) + ", primaryText = " + key.text + ", texts = " + Arrays.toString(key.textExtra));

        // Mulit Key
        if (null != key.codeExtra && null != key.textExtra && key.codeExtra.length == key.textExtra.length) {
            showPopu(key);
        }
        // Enter
        else if (primaryCode == 408) {
            Toast.makeText(getContext(), "Enter", Toast.LENGTH_SHORT).show();
        }
        // 大小写切换
        else if (primaryCode == 210) {
            Toast.makeText(getContext(), "大小写切换", Toast.LENGTH_SHORT).show();
        }
        // 多语言
        else if (primaryCode == 410) {
            Toast.makeText(getContext(), "多语言", Toast.LENGTH_SHORT).show();
        }
        // 删除
        else if (primaryCode == 111) {
            delete();
        }
        // 默认
        else {
            input(key.text);
        }
    }

    /*******************************************************************************************/

    private void show() {
        setKeyboard(new MiniKeyboard(getContext(), R.xml.moudle_safe_keyboard_letter, false, false));
    }


    private void showPopu(MiniKeyboard.Key key) {
        try {


            int width = key.width;
            int height = key.height;
            int horizontalGap = key.hzGap;
            int verticalGap = key.vtGap;
            CharSequence[] textExtra = key.textExtra;
            CusUtil.log("CusKeyboardView -> showPopu -> width = " + width + ", height = " + height + ", horizontalGap = " + horizontalGap + ", verticalGap = " + verticalGap);

            View inflate = LayoutInflater.from(getContext()).inflate(R.layout.popu_layout, null);
            List<Integer> list = Arrays.asList(R.id.popu_center, R.id.popu_left, R.id.popu_top, R.id.popu_right, R.id.popu_bottom);
            for (Integer integer : list) {
                TextView viewById = inflate.findViewById(integer);
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) viewById.getLayoutParams();
                layoutParams.width = width;
                layoutParams.height = height;
                if (integer == R.id.popu_center) {
                    viewById.setText(textExtra[0]);
                    layoutParams.setMargins(horizontalGap, 0, horizontalGap, 0);
                } else if (integer == R.id.popu_top) {
                    viewById.setText(textExtra[1]);
                    layoutParams.setMargins(0, 0, 0, verticalGap);
                } else if (integer == R.id.popu_right) {
                    viewById.setText(textExtra[2]);
                } else if (integer == R.id.popu_bottom) {
                    viewById.setText(textExtra[3]);
                    layoutParams.setMargins(0, verticalGap, 0, 0);
                } else if (integer == R.id.popu_left) {
                    viewById.setText(textExtra[4]);
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
//                    new Handler(Looper.getMainLooper()).post(new Runnable() {
//                        @Override
//                        public void run() {
//                            View viewById1 = inflate.findViewById(R.id.popu_center);
//                            viewById1.requestFocus();
//                            CusUtil.log("CusKeyboardView -> showPopu -> viewById1 = "+viewById1);
//                        }
//                    });
            return;

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
     * @param text
     */
    private void input(CharSequence text) {
        CusUtil.log("CusKeyboardView -> input -> text = " + text + ", mOnKeyChangeListener = " + mOnKeyChangeListener);
        if (null != mOnKeyChangeListener) {
            mOnKeyChangeListener.onInput(text);
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
