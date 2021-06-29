package com.kalu.safekeyboard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.security.MessageDigest;

public final class BlurUtil {

    public static final void blurScr(@NonNull Context context, @NonNull Window window, @NonNull String fileName) {

        if (null == fileName || fileName.length() <= 0)
            return;

        View decorView = window.getDecorView();
        if (null == decorView || !(decorView instanceof ViewGroup))
            return;

        String blurName;
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(fileName.getBytes("utf-8"));
            byte[] encryption = md5.digest();
            StringBuffer stringBuffer = new StringBuffer();
            for (int i = 0; i < encryption.length; i++) {
                if (Integer.toHexString(0xff & encryption[i]).length() == 1) {
                    stringBuffer.append("0").append(Integer.toHexString(0xff & encryption[i]));
                } else {
                    stringBuffer.append(Integer.toHexString(0xff & encryption[i]));
                }
            }
            blurName = stringBuffer.toString();
        } catch (Exception e) {
            blurName = null;
        }

        if (null == blurName || blurName.length() <= 0)
            return;

        // 模糊是否存在
        boolean isExist = false;
        String prefix = context.getFilesDir().getAbsolutePath() + File.separator + "blur";
        Log.e("wer", prefix);
        String blurPath = prefix + File.separator + blurName;
        Log.e("wer", blurPath);
        File file = new File(blurPath);
        if (file.exists()) {
            isExist = true;
        }

        if (!isExist) {
            Toast.makeText(context, "新生成", Toast.LENGTH_SHORT).show();

            // step1:截图
            decorView.setDrawingCacheEnabled(true);
            decorView.buildDrawingCache();
            Rect rect = new Rect();
            decorView.getWindowVisibleDisplayFrame(rect);
            WindowManager windowManager = window.getWindowManager();
            DisplayMetrics outMetrics = new DisplayMetrics();
            windowManager.getDefaultDisplay().getMetrics(outMetrics);
            int width = outMetrics.widthPixels;
            int height = outMetrics.heightPixels;
            Bitmap bitmap = Bitmap.createBitmap(decorView.getDrawingCache(), 0, rect.top, width, height - rect.top);
            decorView.destroyDrawingCache();
            decorView.setDrawingCacheEnabled(false);

            // step2:模糊
            RenderScript renderScript = RenderScript.create(context);
            Allocation input = Allocation.createFromBitmap(renderScript, bitmap);
            Allocation output = Allocation.createTyped(renderScript, input.getType());
            ScriptIntrinsicBlur scriptIntrinsicBlur = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));
            scriptIntrinsicBlur.setInput(input);
            scriptIntrinsicBlur.setRadius(20);
            scriptIntrinsicBlur.forEach(output);
            output.copyTo(bitmap);
            renderScript.destroy();

            // step3:保存
            try {

                File parentFile = file.getParentFile();
                if (null == parentFile || !parentFile.exists()) {
                    parentFile.mkdirs();
                }
                file.createNewFile();

                FileOutputStream fos = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 40, fos);
                fos.flush();
                fos.close();
                isExist = true;
            } catch (Exception e) {
                Log.e("wer", e.getMessage());
                isExist = false;
            }

            if (null != bitmap) {
                bitmap.recycle();
            }
        } else {
            Toast.makeText(context, "已存在", Toast.LENGTH_SHORT).show();
        }

        if (!isExist)
            return;

        // step3:添加
        ImageView imageView = new ImageView(context);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        imageView.setLayoutParams(params);
        Uri parse = Uri.parse(blurPath);
        imageView.setImageURI(parse);
        int childCount = ((ViewGroup) decorView).getChildCount();
        ((ViewGroup) decorView).addView(imageView, childCount);
    }
}
