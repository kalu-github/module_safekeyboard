package lib.kalu.keyboard;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.util.Log;

import androidx.annotation.NonNull;

public final class LogUtil {

    private static boolean mEnable = true;
    private static final String TAG = "lib_keyboard";

    public static boolean isLogger() {
        return mEnable;
    }

    public static void setLogger(boolean enable) {
        mEnable = enable;
    }

    public static void log(String content) {
        if (mEnable) {
            Log.d(TAG, content);
        }
    }
}
