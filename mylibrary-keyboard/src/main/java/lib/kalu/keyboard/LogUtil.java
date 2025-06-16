package lib.kalu.keyboard;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.util.Log;

import androidx.annotation.NonNull;

public final class LogUtil {

    private static final String TAG = "lib_keyboard";

    public static void log(@NonNull String content) {

//        if (!BuildConfig.DEBUG)
//            return;
//
//        if (null == content || content.length() == 0)
//            return;
//
        Log.d(TAG, content);
    }

    public static Activity getCurActivity(Context context) {
        try {
            if (context instanceof Activity) {
                return (Activity) context;
            } else if (context instanceof ContextWrapper) {
                return getCurActivity(((ContextWrapper) context).getBaseContext());
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }
}
