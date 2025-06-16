package lib.kalu.keyboard;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;

import java.util.Locale;

public class LanguageUtil {
    /**
     * 创建指定语言的 Context
     */
    public static Context createLanguageContext(Context baseContext, String languageCode) {
        Resources resources = baseContext.getResources();
        Configuration config = new Configuration(resources.getConfiguration());

        // 解析语言代码
        Locale targetLocale = parseLocale(languageCode);

        // 设置 Locale
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocales(new LocaleList(targetLocale));
        } else {
            config.locale = targetLocale;
        }

        // 更新配置
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return baseContext.createConfigurationContext(config);
        } else {
            resources.updateConfiguration(config, resources.getDisplayMetrics());
            return baseContext;
        }
    }

    private static Locale parseLocale(String languageCode) {
        if (languageCode.contains("-")) {
            String[] parts = languageCode.split("-");
            if (parts.length >= 2) {
                return new Locale(parts[0], parts[1]);
            }
        }
        return new Locale(languageCode);
    }
}