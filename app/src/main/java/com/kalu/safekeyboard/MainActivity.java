package com.kalu.safekeyboard;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;

import lib.kalu.safekeyboard.SafeKeyboardDialog;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.safe).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SafeKeyboardDialog dialog = new SafeKeyboardDialog();
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
            EditText editText = findViewById(R.id.edit);
            if (SafeKeyboardDialog.KEYBOARD_SURE.equalsIgnoreCase(type)) {
                Toast.makeText(getApplicationContext(), value, Toast.LENGTH_SHORT).show();
            } else {
                editText.setText(value);
            }
        }
    }
}