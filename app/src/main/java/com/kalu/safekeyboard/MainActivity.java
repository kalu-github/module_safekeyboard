package com.kalu.safekeyboard;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;

import lib.kalu.safekeyboard.SafeKeyboardDialog;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.edit2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText editText = findViewById(R.id.edit2);
                editText.getText().clear();

                Bundle bundle = new Bundle();
                bundle.putBoolean(SafeKeyboardDialog.BUNDLE_RANDOM_NUMBER, true);
                bundle.putBoolean(SafeKeyboardDialog.BUNDLE_RANDOM_LETTER, true);
                bundle.putBoolean(SafeKeyboardDialog.BUNDLE_OUTSIDE_CANCLE, false);

                SafeKeyboardDialog dialog = new SafeKeyboardDialog();
                dialog.setArguments(bundle);
                dialog.show(getSupportFragmentManager(), SafeKeyboardDialog.TAG);
            }
        });
    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        super.onActivityReenter(resultCode, data);
        if (resultCode == SafeKeyboardDialog.INTENT_CALLBACK_CODE) {
            String type = data.getStringExtra(SafeKeyboardDialog.INTENT_CALLBACK_TYPE);
            String value = data.getStringExtra(SafeKeyboardDialog.INTENT_CALLBACK_VALUE);
            Log.e("main", "onActivityReenter => type = " + type + ", value = " + value);

            if (SafeKeyboardDialog.KEYBOARD_SURE.equalsIgnoreCase(type)) {
                Toast.makeText(getApplicationContext(), value, Toast.LENGTH_SHORT).show();
            } else if (SafeKeyboardDialog.KEYBOARD_DISMISS.equalsIgnoreCase(type)) {
                EditText editText = findViewById(R.id.edit1);
                editText.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
            } else if (SafeKeyboardDialog.KEYBOARD_DELETE.equalsIgnoreCase(type)) {
                EditText editText = findViewById(R.id.edit2);
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
                EditText editText = findViewById(R.id.edit2);
                String str = editText.getText().toString() + value;
                editText.setText(str);
                editText.setSelection(str.length());
            }
        }
    }
}