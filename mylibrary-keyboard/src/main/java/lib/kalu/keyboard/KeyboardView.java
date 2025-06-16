package lib.kalu.keyboard;

import android.content.Context;
import android.content.res.AssetManager;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.util.Arrays;
import java.util.List;
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
        setKeyboard(new GoogleKeyboard(getContext(), R.xml.res_keyboard_keys));
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
    public void onKey(int primaryCode, GoogleKeyboard.Key key) {
        LogUtil.log("CusKeyboardView -> onKey -> primaryCode = " + primaryCode + ", key.text = " + key.text + ", key.mult = " + key.mult + ", key.symbel = " + key.symbel);

        // Mulit Key
        if (null != key.mult && key.mult.length() > 0) {
            showPopuMult(key);
        }
        // 多语言
        else if (primaryCode == 410) {
            showPopuLanguage();
        }
        // 删除
        else if (primaryCode == 111) {
            delete();
        }
        // 大小写切换
        else if (primaryCode == 211) {
            Toast.makeText(getContext(), "大小写切换", Toast.LENGTH_SHORT).show();
            boolean shifted = getKeyboard().isShifted();
            setShifted(!shifted);
        }
        // 空格
        else if (primaryCode == 310) {
            Toast.makeText(getContext(), "空格", Toast.LENGTH_SHORT).show();
            input(key);
        }
        // 邮箱后缀
        else if (primaryCode == 311) {
            Toast.makeText(getContext(), "邮箱后缀", Toast.LENGTH_SHORT).show();
            input(key);
        }
        // 收起键盘
        else if (primaryCode == 408) {
            Toast.makeText(getContext(), "收起键盘", Toast.LENGTH_SHORT).show();
            if (null != mOnKeyChangeListener) {
                mOnKeyChangeListener.onDismiss();
            }
        }
        // 切换键盘
        else if (primaryCode == 409) {
            Toast.makeText(getContext(), "切换键盘", Toast.LENGTH_SHORT).show();
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
            if (symbol && key.isSymbel) {
                mOnKeyChangeListener.onInput(key.symbel);
            } else {
                boolean shifted = isShifted();
                if (shifted && key.isUpper) {
                    mOnKeyChangeListener.onInput(key.text.toString().toUpperCase());
                } else {
                    mOnKeyChangeListener.onInput(key.text);
                }
            }
        }
    }

    /**
     * 删除字符
     */
    private void delete() {
        LogUtil.log("delete =>");
        if (null != mOnKeyChangeListener) {
            mOnKeyChangeListener.onDelete();
        }
    }

    private void showPopuMult(GoogleKeyboard.Key key) {

        try {

            CharSequence mult = key.mult;
            if (null == mult)
                throw new Exception("error: mult null");
            if (mult.length() == 0)
                throw new Exception("error: mult.length() == 0");
            String[] split = mult.toString().split(",");
            if (split.length == 0)
                throw new Exception("error: split.length == 0");

            View inflate = LayoutInflater.from(getContext()).inflate(R.layout.res_keyboard_layout_popu_letter, null);
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
//            LogUtil.log("CusKeyboardView -> showPopu -> xoff = " + xoff + ", yoff = " + yoff + ", height = " + height);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                popupWindow.showAsDropDown(viewById, xoff, yoff, Gravity.NO_GRAVITY);
//            }


            popupWindow.showAtLocation((View) getParent(), Gravity.LEFT, xoff, yoff);


            int width = key.width;
            int height = key.height;
            int horizontalGap = key.hzGap;
            int verticalGap = key.vtGap;
//            CusUtil.log("CusKeyboardView -> showPopu -> width = " + width + ", height = " + height + ", horizontalGap = " + horizontalGap + ", verticalGap = " + verticalGap);
            for (int i = 0; i < 5; i++) {

                TextView textView;
                // center
                if (i == 0) {
                    textView = inflate.findViewById(R.id.popu_center);
                }
                // top
                else if (i == 1) {
                    textView = inflate.findViewById(R.id.popu_top);
                }
                // left
                else if (i == 2) {
                    textView = inflate.findViewById(R.id.popu_left);
//                    LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) textView.getLayoutParams();
//                    layoutParams.width = width;
//                    layoutParams.height = height;
//                    layoutParams.setMargins(horizontalGap, 0, horizontalGap, 0);
//                    textView.setLayoutParams(layoutParams);
                }
                // right
                else if (i == 3) {
                    textView = inflate.findViewById(R.id.popu_right);
                }
                // bottom
                else {
                    textView = inflate.findViewById(R.id.popu_bottom);
//                    LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) textView.getLayoutParams();
//                    layoutParams.width = width;
//                    layoutParams.height = height;
//                    layoutParams.setMargins(0, verticalGap, 0, 0);
//                    textView.setLayoutParams(layoutParams);
                }

                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) textView.getLayoutParams();
                layoutParams.width = width;
                layoutParams.height = height;

                // center
                if (i == 0) {
                }
                // top
                else if (i == 1) {
                    layoutParams.setMargins(0, 0, 0, verticalGap);
                }
                // left
                else if (i == 2) {
                }
                // right
                else if (i == 3) {
                }
                // bottom
                else {
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
                        }
                    });
                }
            }

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
            LogUtil.log("KeyboardView -> showPopuMult -> Exception " + e.getMessage());
        }
    }

    private void showPopuLanguage() {
        LogUtil.log("KeyboardView -> showPopuLanguage");

        try {

            Context context = getContext();
            Resources resources = context.getResources();
            Configuration configuration = resources.getConfiguration();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                String curLanguage = configuration.getLocales().get(0).getLanguage();
                LogUtil.log("KeyboardView -> showPopuLanguage -> curLanguage = " + curLanguage);
            } else {
                String curLanguage = configuration.locale.getLanguage();
                LogUtil.log("KeyboardView -> showPopuLanguage -> curLanguage = " + curLanguage);
            }

            Locale aDefault = Locale.getDefault();
            LogUtil.log("KeyboardView -> showPopuLanguage -> aDefault.language = " + aDefault.getLanguage() + ", aDefault.country = " + aDefault);

            Locale[] locales = Locale.getAvailableLocales();
            for (Locale locale : locales) {
                if (null == locale)
                    continue;
                String language = locale.getLanguage();
                String country = locale.getCountry();
                if (country.isEmpty())
                    continue;
                if (language.isEmpty())
                    continue;

                LogUtil.log("KeyboardView -> showPopuLanguage -> language = " + language + ", country = " + country);

                String symble = language + "-r" + country;
                String result = getStringByLanguage(getContext(), R.string.res_keyboard_popu_language_title, symble);
                LogUtil.log("KeyboardView -> showPopuLanguage -> result = " + result);

//                // 尝试设置为当前语言
//
//                 testResources;
//                try {
//                    testResources = new Resources(assetManager, resources.getDisplayMetrics(), config);
//                } catch (Exception e) {
//                    continue;
//                }
//
//                // 比较资源是否存在差异
//                String testString = testResources.getString(R.string.app_name);
//                String defaultString = resources.getString(R.string.app_name);
//
//                // 如果字符串不同，说明该语言有独立资源
//                if (!testString.equals(defaultString)) {
//                    supportedLanguages.add(locale.getLanguage());
//                }
            }

            int width = getWidth();
            int height = (int) (getHeight() * 1.5);
            LogUtil.log("KeyboardView -> showPopuLanguage -> width = " + width + ", height = " + height);

            View inflate = LayoutInflater.from(getContext()).inflate(R.layout.res_keyboard_layout_popu_language, null);
            PopupWindow popupWindow = new PopupWindow(
                    inflate, // 内容视图
                    width, // 宽度
                    height // 高度
            );

            // 必须设置背景，否则无法处理触摸事件
            popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            popupWindow.setFocusable(true);
            popupWindow.setOutsideTouchable(false);
            popupWindow.showAtLocation((View) getParent(), Gravity.TOP, 0, 0);
//            popupWindow.showAsDropDown((View) getParent());
            // 设置动画
//            popupWindow.setAnimationStyle(R.style.PopupAnimation);
//                    // 1. 相对于某个锚点视图显示
//                    popupWindow.showAsDropDown(anchorView); // 在锚点视图下方显示
//                    // 2. 相对于某个锚点视图显示，并设置偏移量

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

            //
            ViewGroup viewGroup = inflate.findViewById(R.id.res_keyboard_popu_language_content);
            List<String> list = Arrays.asList("英语", "Português (Brasil) 巴葡", "Español 西班牙语");
            for (String s : list) {
                LayoutInflater.from(getContext()).inflate(R.layout.res_keyboard_layout_popu_language_item, viewGroup, true);
                int indexOf = list.indexOf(s);
                RadioButton radioButton = (RadioButton) viewGroup.getChildAt(indexOf);
                radioButton.setText(s);
                radioButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //
                        popupWindow.dismiss();
//                        //

                        //
                        Context context;
                        if ("英语".equals(s)) {
                            context = LanguageUtil.createLanguageContext(getContext(), "en");
                        } else {
                            context = LanguageUtil.createLanguageContext(getContext(), "es");
                        }
                        // closing();
                        updateKeyboard(new GoogleKeyboard(context, R.xml.res_keyboard_keys));
//                        update(context, R.xml.res_keyboard_keys);
                    }
                });
            }

            // 6. 强制PopupWindow的ContentView获取焦点
            inflate.setFocusableInTouchMode(true);
            inflate.requestFocus();

        } catch (Exception e) {
            LogUtil.log("KeyboardView -> showPopuLanguage -> Exception " + e.getMessage());
        }
    }

    /**
     * 获取指定语言的字符串资源
     *
     * @param context  上下文
     * @param resId    资源ID（如 R.string.hello_world）
     * @param language 语言代码（如 "en"、"zh"、"zh-rCN"）
     * @return 指定语言的字符串，若不存在则返回默认语言
     */
    private String getStringByLanguage(Context context, int resId, String language) {
        Resources defaultResources = context.getResources();
        AssetManager assetManager = defaultResources.getAssets();
        Configuration config = new Configuration(defaultResources.getConfiguration());

        // 解析语言代码（支持 "zh" 或 "zh-rCN" 格式）
        Locale targetLocale;
        if (language.contains("-")) {
            String[] parts = language.split("-");
            targetLocale = new Locale(parts[0], parts[1]);
        } else {
            targetLocale = new Locale(language);
        }

        // 设置语言环境
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocales(new LocaleList(targetLocale));
        } else {
            config.locale = targetLocale;
        }

        Resources targetResources = new Resources(assetManager,
                defaultResources.getDisplayMetrics(),
                config);

        try {
            // 尝试获取指定语言的字符串
            return targetResources.getString(resId);
        } catch (Exception e) {
            // 若不存在该语言资源，返回默认语言
            return "null";
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

        void onDismiss();
    }
}
