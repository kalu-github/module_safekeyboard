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

    private static WeakReference<Object[]> weakReference = new WeakReference<>(new Object[1]);

    static void setFragmentManager(@NonNull FragmentManager manager) {

        try {
            if (null == weakReference) {
                weakReference = new WeakReference<>(new Object[1]);
            }

            Object[] objects = weakReference.get();
            if (null == objects) {
                weakReference = new WeakReference<>(new Object[1]);
                objects = weakReference.get();
            }
            objects[0] = manager;
        } catch (Exception e) {
        }
    }

    private static FragmentManager getFragmentManager() {

        if (null == weakReference)
            return null;

        Object[] objects = weakReference.get();
        if (null == objects || objects.length != 1 || null == objects[0])
            return null;

        return (FragmentManager) objects[0];
    }

    static final void forceDismiss() {

        try {
            FragmentManager fragmentManager = getFragmentManager();
            if (null == fragmentManager)
                return;

            Fragment fragment = fragmentManager.findFragmentByTag(SafeKeyboardDialog.TAG);
            if (null == fragment || !(fragment instanceof SafeKeyboardDialog))
                return;

            SafeKeyboardDialog dialog = (SafeKeyboardDialog) fragment;
            dialog.dismiss();
        } catch (Exception e) {
        }
    }
}
