package lib.kalu.keyboard;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.LocaleList;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.Locale;

import lib.kalu.keyboard.google.GoogleKeyboard;
import lib.kalu.keyboard.google.GoogleKeyboardView;

final class KeyboardView extends GoogleKeyboardView implements GoogleKeyboardView.OnKeyboardActionListener {

    public KeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public KeyboardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public KeyboardView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setOnKeyboardActionListener(this);
        setKeyboard(new GoogleKeyboard(getContext(), R.xml.res_keyboard_keys), 0);
    }

    @Override
    public void onKey(int primaryCode, GoogleKeyboard.Key key) {
        LogUtil.log("CusKeyboardView -> onKey -> primaryCode = " + primaryCode + ", key.text = " + key.text + ", key.mult = " + key.mult + ", key.symbel = " + key.symbel + ", isSymbol = " + isSymbol());

        // Mulit Key
        if (null != key.mult && key.mult.length() > 0) {
            boolean symbol = isSymbol();
            if (symbol) {
                input(key);
            } else {
                showPopupMult(key);
            }
        }
        // 多语言
        else if (primaryCode == 410) {
            showPopupLanguage();
        }
        // 删除
        else if (primaryCode == 111) {
            if (null != mOnKeyChangeListener) {
                mOnKeyChangeListener.onDelete();
            }
        }
        // 大小写切换
        else if (primaryCode == 211) {
            boolean symbol = isSymbol();
            if (!symbol) {
                boolean shifted = isShifted();
                setShifted(!shifted);
            }
        }
        // 空格
        else if (primaryCode == 310) {
            input(key);
        }
        // 邮箱后缀
        else if (primaryCode == 311) {
            input(key);
        }
        // 特殊处理 西班牙语ñ
        else if (primaryCode == 408) {
            input(key);
        }
        // 切换键盘
        else if (primaryCode == 409) {
            boolean symbol = isSymbol();
            setSymbol(!symbol);
        }
        // 默认
        else {
            input(key);
        }
    }

    /*******************************************************************************************/

    private void input(GoogleKeyboard.Key key) {
//        CusUtil.log("CusKeyboardView -> input -> text = " + text + ", mOnKeyChangeListener = " + mOnKeyChangeListener);
        if (null != mOnKeyChangeListener) {
            boolean symbol = isSymbol();
            if (symbol && key.supportSymbel) {
                mOnKeyChangeListener.onInput(key.symbel);
            } else {
                boolean shifted = isShifted();
                if (shifted && key.supportUpper) {
                    mOnKeyChangeListener.onInput(key.text.toString().toUpperCase());
                } else {
                    mOnKeyChangeListener.onInput(key.text);
                }
            }
        }

        // 大写仅锁定一次
        boolean shifted = isShifted();
        if (shifted) {
            setShifted(false);
        }
    }

    private void language(String languageCode) {
        try {

            if (null == languageCode)
                throw new Exception("error: languageCode null");
            if (languageCode.isEmpty())
                throw new Exception("error: languageCode isEmpty");

            String[] split = languageCode.split("-");
            int length = split.length;
            if (length > 2)
                throw new Exception("error: split.length = " + length);

            Locale targetLocale;
            if (length == 2) {
                targetLocale = new Locale(split[0], split[1]);
            } else {
                targetLocale = new Locale(split[0]);
            }

            Context baseContext = getContext();
            Resources resources = baseContext.getResources();
            Configuration config = new Configuration(resources.getConfiguration());

            // 设置 Locale
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                config.setLocales(new LocaleList(targetLocale));
            } else {
                config.locale = targetLocale;
            }

            // 更新配置
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                config.densityDpi = resources.getDisplayMetrics().densityDpi;
                baseContext = baseContext.createConfigurationContext(config);
            } else {
                resources.updateConfiguration(config, resources.getDisplayMetrics());
            }

            GoogleKeyboard keyboard = new GoogleKeyboard(baseContext, R.xml.res_keyboard_keys);
            setKeyboard(keyboard, keyboard.getKeys().size() - 1);
        } catch (Exception e) {
            LogUtil.log("KeyboardView -> language -> Exception " + e.getMessage());
        }
    }

    private void showPopupMult(GoogleKeyboard.Key key) {

        try {

            CharSequence mult;
            boolean shifted = isShifted();
            if (shifted) {
                mult = key.multUpper;
            } else {
                mult = key.mult;
            }

            if (null == mult)
                throw new Exception("error: mult null");
            if (mult.length() == 0)
                throw new Exception("error: mult.length() == 0");
            String[] split = mult.toString().split(",");
            if (split.length == 0)
                throw new Exception("error: split.length == 0");

            int width = key.width;
            int height = key.height;
            int horizontalGap = key.hzGap;
            int verticalGap = key.vtGap;

            int popuWidth = key.width * 3 + horizontalGap * 2;
            int popuHeight = key.height * 3 + verticalGap * 2;
            // LogUtil.log("CusKeyboardView -> showPopupMult -> popuWidth = " + popuWidth + ", popuHeight = " + popuHeight);
            View inflate = LayoutInflater.from(getContext()).inflate(R.layout.res_keyboard_layout_popu_letter, null);
            PopupWindow popupWindow = new PopupWindow(
                    inflate, // 内容视图
                    popuWidth, // 宽度
                    popuHeight // 高度
            );
            // 禁用动画
            popupWindow.setAnimationStyle(0);
            // 设置背景，使点击外部可关闭弹窗
            popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            popupWindow.setFocusable(true);
            popupWindow.setOutsideTouchable(false);

            int xoff = key.x - key.width - horizontalGap + getPaddingLeft();
            int yoff = key.y + key.height * 2 + verticalGap + getPaddingTop();
            //  LogUtil.log("CusKeyboardView -> showPopupMult -> xoff = " + xoff + ", yoff = " + yoff);
            popupWindow.showAsDropDown(this, xoff, yoff);


            for (int i = 0; i < 5; i++) {

                int id;
                // center
                if (i == 0) {
                    id = R.id.popu_center;
                }
                // top
                else if (i == 1) {
                    id = R.id.popu_top;
                }
                // left
                else if (i == 2) {
                    id = R.id.popu_left;
                }
                // right
                else if (i == 3) {
                    id = R.id.popu_right;
                }
                // bottom
                else {
                    id = R.id.popu_bottom;
                }

                TextView textView = inflate.findViewById(id);
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) textView.getLayoutParams();
                layoutParams.width = width;
                layoutParams.height = height;

                // center
                if (i == 0) {
                    textView.requestFocus();
                }
                // top
                else if (i == 1) {
                    layoutParams.setMargins(0, 0, 0, verticalGap);
                }
                // left
                else if (i == 2) {
                    layoutParams.setMargins(0, 0, horizontalGap, 0);
                }
                // right
                else if (i == 3) {
                    layoutParams.setMargins(horizontalGap, 0, 0, 0);
                }
                // bottom
                else {
                    layoutParams.setMargins(0, verticalGap, 0, 0);
                }
                textView.setLayoutParams(layoutParams);

                if (i + 1 > split.length) {
                    textView.setVisibility(View.INVISIBLE);
                } else {
                    textView.setText(split[i]);
                    //
                    textView.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //
                            popupWindow.dismiss();
                            //
                            if (null != mOnKeyChangeListener) {
                                CharSequence text = ((TextView) view).getText();
                                mOnKeyChangeListener.onInput(text);
                            }
                            // 大写仅锁定一次
                            boolean shifted = isShifted();
                            if (shifted) {
                                setShifted(false);
                            }
                        }
                    });
                }
            }
        } catch (Exception e) {
            LogUtil.log("KeyboardView -> showPopupMult -> Exception " + e.getMessage());
        }
    }

    private void showPopupLanguage() {
        try {

            Object tag = getTag();
            if (null == tag)
                throw new Exception("error: tag null");
            ArrayList<String> list = (ArrayList<String>) tag;
            if (null == list)
                throw new Exception("error: list null");
            if (list.isEmpty())
                throw new Exception("error: list isEmpty");
            int size = list.size();
            if (size % 2 != 0)
                throw new Exception("error: size % 2 != 0");

            int popuWidth = getWidth();
            int popuHeight = getHeight();
            LogUtil.log("KeyboardView -> showPopupLanguage -> popuWidth = " + popuWidth + ", popuHeight = " + popuHeight);

            View inflate = LayoutInflater.from(getContext()).inflate(R.layout.res_keyboard_layout_popu_language, null);
            PopupWindow popupWindow = new PopupWindow(
                    inflate, // 内容视图
                    popuWidth, // 宽度
                    popuHeight // 高度
            );
            // 禁用动画
            popupWindow.setAnimationStyle(0);
            // 必须设置背景，否则无法处理触摸事件
            popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            popupWindow.setFocusable(true);
            popupWindow.setOutsideTouchable(false);
            popupWindow.showAtLocation(this, Gravity.BOTTOM, 0, 0);

            // 拦截返回键事件
            inflate.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                        // 消费返回键事件，不关闭PopupWindow
                        // 若需要在特定条件下关闭，可在此处添加逻辑
                        return true;
                    }
                    return false;
                }
            });


            String languageCode = getKeyboard().mDefaultLanguageCode;
            LogUtil.log("KeyboardView -> showPopupLanguage -> languageCode = " + languageCode);

            //
            ViewGroup viewGroup = inflate.findViewById(R.id.res_keyboard_popu_language_content);

            for (int i = 0; i < size; i += 2) {

                String name = list.get(i);
                String code = list.get(i + 1);
                LogUtil.log("KeyboardView -> showPopupLanguage -> i = " + i + ", name = " + name + ", code = " + code);

                LayoutInflater.from(getContext()).inflate(R.layout.res_keyboard_layout_popu_language_item, viewGroup, true);
                int index = i / 2;
                RadioButton radioButton = (RadioButton) viewGroup.getChildAt(index);
                if (code.equalsIgnoreCase(languageCode) || languageCode.startsWith(code)) {
                    radioButton.setChecked(true);
                    radioButton.requestFocus();
                } else {
                    radioButton.setChecked(false);
                }
                radioButton.setText(name);
                radioButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //
                        popupWindow.dismiss();
                        //
                        language(code);
                    }
                });
            }

        } catch (Exception e) {
            LogUtil.log("KeyboardView -> showPopupLanguage -> Exception " + e.getMessage());
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
