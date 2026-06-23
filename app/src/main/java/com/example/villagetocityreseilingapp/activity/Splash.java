package com.example.villagetocityreseilingapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.example.villagetocityreseilingapp.R;

public class Splash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aactivity_splash);

        new Handler().postDelayed(() -> {
            Intent intent = new Intent(Splash.this, AuthActivity.class);
            startActivity(intent);
            finish();
        }, 2000);
    }
}