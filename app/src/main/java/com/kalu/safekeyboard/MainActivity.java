package com.kalu.safekeyboard;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import org.json.JSONArray;

import lib.kalu.safekeyboard.SafeKeyboardDialog;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        JSONArray json = new JSONArray();
        json.put("111");
        json.put("222");

        Log.e("main", json.toString());

        findViewById(R.id.safe).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SafeKeyboardDialog dialog = new SafeKeyboardDialog();
                dialog.show(getSupportFragmentManager(), SafeKeyboardDialog.TAG);

            }
        });
    }
}