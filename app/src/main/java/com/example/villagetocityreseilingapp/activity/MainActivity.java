package com.example.villagetocityreseilingapp.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.villagetocityreseilingapp.R;
import com.example.villagetocityreseilingapp.ui.main.seller.SellerDashboardFragment;
import com.example.villagetocityreseilingapp.ui.main.seller.SellerProductFragment;
import com.example.villagetocityreseilingapp.ui.main.seller.SellerOrderFragment;
import com.example.villagetocityreseilingapp.ui.main.seller.SellerProfileFragment;

public class MainActivity extends AppCompatActivity {

    LinearLayout homeBtn, menuBtn, ordersBtn, dashboardBtn;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.seller_fragment_loader);

        // Find views
        homeBtn = findViewById(R.id.home_layout);
        menuBtn = findViewById(R.id.menu_layout);
        ordersBtn = findViewById(R.id.orders_layout);
        dashboardBtn = findViewById(R.id.dashboard_layout);

        // Set default fragment
        loadFragment(new SellerDashboardFragment());

        // Set click listeners
        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFragment(new SellerDashboardFragment());
            }
        });

        menuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFragment(new SellerProductFragment());
            }
        });

        ordersBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFragment(new SellerOrderFragment());
            }
        });

        dashboardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFragment(new SellerProfileFragment());
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
