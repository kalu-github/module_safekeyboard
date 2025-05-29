package com.kalu.safekeyboard;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        findViewById(R.id.button01).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //
                EditText editText = findViewById(R.id.edit01);
                String string = editText.getText().toString();
                Toast.makeText(getApplicationContext(), "-> "+string, Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.button02).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //
                EditText editText = findViewById(R.id.edit02);
                String string = editText.getText().toString();
                Toast.makeText(getApplicationContext(), "-> "+string, Toast.LENGTH_SHORT).show();
            }
        });
    }
}