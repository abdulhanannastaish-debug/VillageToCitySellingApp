package com.villagetocity.marketplace.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.villagetocity.marketplace.R;

public class Splash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aactivity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            FirebaseUser currentUser =
                    FirebaseAuth.getInstance().getCurrentUser();

            if (currentUser != null) {

                String role = getSharedPreferences("role", MODE_PRIVATE)
                        .getString("user_role", "");

                if ("buyer".equalsIgnoreCase(role)) {

                    Intent intent =
                            new Intent(Splash.this, buyer_MainActivity.class);

                    intent.addFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK
                                    | Intent.FLAG_ACTIVITY_CLEAR_TASK
                    );

                    startActivity(intent);

                } else if ("seller".equalsIgnoreCase(role)) {

                    Intent intent =
                            new Intent(Splash.this, seller_MainActivity.class);

                    intent.addFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK
                                    | Intent.FLAG_ACTIVITY_CLEAR_TASK
                    );

                    startActivity(intent);

                } else {

                    startActivity(
                            new Intent(Splash.this, AuthActivity.class)
                    );
                }

            } else {

                startActivity(
                        new Intent(Splash.this, AuthActivity.class)
                );
            }

            finish();

        }, 2000);
    }
}