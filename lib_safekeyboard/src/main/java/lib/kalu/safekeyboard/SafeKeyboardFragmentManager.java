package lib.kalu.safekeyboard;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

/**
 * description: FragmentManager
 * created by kalu on 2021-01-15
 */
final class SafeKeyboardFragmentManager {

    private static WeakReference<SafeKeyboardDialog> weakReference = null;

    static void setFragmentManager(@NonNull SafeKeyboardDialog dialog) {

        try {
            forceDismiss();
            weakReference = new WeakReference<>(dialog);
        } catch (Exception e) {
        }
    }

    static final void forceDismiss() {
        if (null != weakReference) {
            SafeKeyboardDialog dialog = weakReference.get();
            if (null != dialog) {
                dialog.dismiss();
                dialog = null;
            }
        }
    }

    static final SafeKeyboardDialog get() {
        if (null != weakReference) {
            SafeKeyboardDialog dialog = weakReference.get();
            if (null != dialog) {
                return dialog;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
}
