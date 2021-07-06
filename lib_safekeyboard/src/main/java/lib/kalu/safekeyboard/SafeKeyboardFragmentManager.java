package lib.kalu.safekeyboard;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.lang.ref.WeakReference;

/**
 * description: FragmentManager
 * created by kalu on 2021-01-15
 */
final class SafeKeyboardFragmentManager {

    private static WeakReference<Fragment[]> weakReference = new WeakReference<>(new Fragment[1]);

    static void setFragmentManager(@NonNull SafeKeyboardDialog dialog) {

        try {
            Fragment[] fragments = weakReference.get();
            if (null == fragments || fragments.length == 0)
                return;

            fragments[0] = dialog;
        } catch (Exception e) {
        }
    }

    private static SafeKeyboardDialog getFragmentManager() {

        if (null == weakReference)
            return null;

        Fragment[] fragments = weakReference.get();
        if (null == fragments)
            return null;

        return (SafeKeyboardDialog) fragments[0];
    }

    static final void forceDismiss() {

        try {
            SafeKeyboardDialog dialog = getFragmentManager();
            if (null == dialog)
                return;

            dialog.dismiss();
        } catch (Exception e) {
        }
    }
}
