package com.sundar.devtech;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import com.sundar.devtech.Services.ActivityMoving;

public class SuccessActivity extends AppCompatActivity {

    private AppCompatButton EXIT,CONTINUE;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success);

        EXIT =findViewById(R.id.exitBtn);
        CONTINUE = findViewById(R.id.continueBtn);

        EXIT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityMoving.ActivityMoving(SuccessActivity.this,ScannerActivity.class);
            }
        });

        CONTINUE.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityMoving.ActivityMoving(SuccessActivity.this,SlotDetailActivity.class);
            }
        });
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ActivityMoving.ActivityMoving(SuccessActivity.this,ScannerActivity.class);
    }
}