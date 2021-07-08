package com.kalu.safekeyboard;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import lib.kalu.safekeyboard.SafeKeyboardDialog;
import lib.kalu.safekeyboard.SafeKeyboardEditText;

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
                bundle.putInt(SafeKeyboardDialog.BUNDLE_EDITTEXT_ID, R.id.edit2);

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
                bundle.putInt(SafeKeyboardDialog.BUNDLE_EDITTEXT_ID, R.id.edit3);
//                bundle.putString(SafeKeyboardDialog.BUNDLE_CALLBACK_EXTRA, "我是额外的Data");

                SafeKeyboardDialog dialog = new SafeKeyboardDialog();
                dialog.setArguments(bundle);
                dialog.show(getSupportFragmentManager(), SafeKeyboardDialog.TAG);
            }
        });

        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SafeKeyboardEditText editText = findViewById(R.id.edit2);
                String text = editText.getText().toString();
                String real = editText.getReal();
                Toast.makeText(getApplicationContext(), text + " - " + real, Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.button3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SafeKeyboardEditText editText = findViewById(R.id.edit3);
                String text = editText.getText().toString();
                String real = editText.getReal();
                Toast.makeText(getApplicationContext(), text + " - " + real, Toast.LENGTH_SHORT).show();
            }
        });
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