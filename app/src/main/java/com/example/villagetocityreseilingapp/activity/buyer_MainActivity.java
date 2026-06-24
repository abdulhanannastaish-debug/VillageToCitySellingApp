package com.example.villagetocityreseilingapp.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.villagetocityreseilingapp.R;
import com.example.villagetocityreseilingapp.ui.main.buyer.buyer_fragment_cart;
import com.example.villagetocityreseilingapp.ui.main.buyer.buyer_fragment_home;
import com.example.villagetocityreseilingapp.ui.main.buyer.buyer_fragment_orders;
import com.example.villagetocityreseilingapp.ui.main.buyer.buyer_fragment_profile;

public class buyer_MainActivity extends AppCompatActivity {

    LinearLayout homeBtn, menuBtn, ordersBtn, dashboardBtn;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.buyer_fragment_loader);

        // Find views
        homeBtn = findViewById(R.id.home_layout);
        menuBtn = findViewById(R.id.menu_layout);
        ordersBtn = findViewById(R.id.orders_layout);
        dashboardBtn = findViewById(R.id.dashboard_layout);

        // Set default fragment
        loadFragment(new buyer_fragment_home());

        // Set click listeners
        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFragment(new buyer_fragment_home());
            }
        });

        menuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFragment(new buyer_fragment_cart());
            }
        });

        ordersBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFragment(new buyer_fragment_orders());
            }
        });

        dashboardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFragment(new buyer_fragment_profile());
            }
        });
    }

    // This is the only method you need for loading fragments
    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }
}
