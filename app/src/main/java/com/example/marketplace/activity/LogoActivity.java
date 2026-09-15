package com.example.marketplace.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.villagetocity.marketplace.R;

public class LogoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logoactivity);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent i = new Intent(LogoActivity.this, Splash.class);
            startActivity(i);
            finish();
        }, 2000);
    }
}