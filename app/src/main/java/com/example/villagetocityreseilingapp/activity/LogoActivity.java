package com.example.villagetocityreseilingapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.example.villagetocityreseilingapp.R;

public class LogoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logoactivity);

        new Handler().postDelayed(() -> {
            Intent i = new Intent(LogoActivity.this, Splash.class);
            startActivity(i);
            finish();
        }, 2000);
    }
}