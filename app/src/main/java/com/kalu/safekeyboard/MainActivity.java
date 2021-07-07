package com.kalu.safekeyboard;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import lib.kalu.safekeyboard.SafeKeyboardDialog;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_main);

        findViewById(R.id.edit2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Bundle bundle = new Bundle();
                bundle.putBoolean(SafeKeyboardDialog.BUNDLE_RANDOM_NUMBER, false);
                bundle.putBoolean(SafeKeyboardDialog.BUNDLE_RANDOM_LETTER, false);
                bundle.putBoolean(SafeKeyboardDialog.BUNDLE_OUTSIDE_CANCLE, true);
                bundle.putInt(SafeKeyboardDialog.BUNDLE_DELAY_TIME, 60);
                bundle.putInt(SafeKeyboardDialog.BUNDLE_CALLBACK_ID, R.id.edit2);
                bundle.putString(SafeKeyboardDialog.BUNDLE_CALLBACK_EXTRA, "我是额外的Data");

                SafeKeyboardDialog dialog = new SafeKeyboardDialog();
                dialog.setArguments(bundle);
                dialog.show(getSupportFragmentManager(), SafeKeyboardDialog.TAG);
            }
        });

        findViewById(R.id.edit3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Bundle bundle = new Bundle();
                bundle.putBoolean(SafeKeyboardDialog.BUNDLE_RANDOM_NUMBER, false);
                bundle.putBoolean(SafeKeyboardDialog.BUNDLE_RANDOM_LETTER, false);
                bundle.putBoolean(SafeKeyboardDialog.BUNDLE_OUTSIDE_CANCLE, true);
                bundle.putInt(SafeKeyboardDialog.BUNDLE_DELAY_TIME, 60);
                bundle.putInt(SafeKeyboardDialog.BUNDLE_CALLBACK_ID, R.id.edit3);
                bundle.putString(SafeKeyboardDialog.BUNDLE_CALLBACK_EXTRA, "我是额外的Data");

                SafeKeyboardDialog dialog = new SafeKeyboardDialog();
                dialog.setArguments(bundle);
                dialog.show(getSupportFragmentManager(), SafeKeyboardDialog.TAG);
            }
        });
    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        super.onActivityReenter(resultCode, data);

        if (resultCode != SafeKeyboardDialog.BUNDLE_CALLBACK_CODE)
            return;

        int ids = data.getIntExtra(SafeKeyboardDialog.BUNDLE_CALLBACK_ID, -1);
        if (-1 == ids)
            return;

        EditText editText = findViewById(ids);
        if (null == editText)
            return;

        String type = data.getStringExtra(SafeKeyboardDialog.BUNDLE_CALLBACK_TYPE);
        String value = data.getStringExtra(SafeKeyboardDialog.BUNDLE_CALLBACK_VALUE);
        Log.e("main", "onActivityReenter => type = " + type + ", value = " + value);

        if (SafeKeyboardDialog.KEYBOARD_DONE.equalsIgnoreCase(type)) {
            String extra = data.getStringExtra(SafeKeyboardDialog.BUNDLE_CALLBACK_EXTRA);
            Toast.makeText(getApplicationContext(), ids + " - " + value + " - " + extra, Toast.LENGTH_SHORT).show();
        }else if (SafeKeyboardDialog.KEYBOARD_DELETE.equalsIgnoreCase(type)) {
            String str = editText.getText().toString();
            if (null != str && str.length() > 0) {
                String news = "";
                if (str.length() > 1) {
                    news = str.substring(0, str.length() - 1);
                }
                editText.setText(news);
                editText.setSelection(news.length());
            }
        } else if (SafeKeyboardDialog.KEYBOARD_INPUT.equalsIgnoreCase(type)) {
            String str = editText.getText().toString() + value;
            editText.setText(str);
            editText.setSelection(str.length());
        }
    }

    @Override
    protected void onPause() {
//        String s = MainActivity.class.getName().toLowerCase();
//        BlurUtil.blurScr(getApplicationContext(), getWindow(), s);
        super.onPause();
    }

//    @Override
//    protected void onRestart() {
//        super.onRestart();
//        Toast.makeText(getApplicationContext(), "onRestart", Toast.LENGTH_SHORT).show();
//
//        /*获取windows中最顶层的view*/
//        View view = getWindow().getDecorView();
//        if (!(view instanceof ViewGroup))
//            return;
//
//        int childCount = ((ViewGroup) view).getChildCount();
//        ((ViewGroup) view).removeViewAt(childCount - 1);
//    }
}