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

    private LinearLayout homeBtn;
    private LinearLayout menuBtn;
    private LinearLayout ordersBtn;
    private LinearLayout profileBtn;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Seller main loader screen
        setContentView(R.layout.fragment_seller_loader);

        // Bottom navigation buttons
        homeBtn = findViewById(R.id.home_layout);
        menuBtn = findViewById(R.id.menu_layout);
        ordersBtn = findViewById(R.id.orders_layout);
        profileBtn = findViewById(R.id.profile_layout);

        // Default screen
        loadFragment(new SellerDashboardFragment());

        // ================= DASHBOARD =================

        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFragment(new SellerDashboardFragment());
            }
        });

        // ================= PRODUCTS =================

        menuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFragment(new SellerProductFragment());
            }
        });

        // ================= ORDERS =================

        ordersBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFragment(new SellerOrderFragment());
            }
        });

        // ================= PROFILE =================

        profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFragment(new SellerProfileFragment());
            }
        });
    }

    // ================= LOAD FRAGMENT =================

    private void loadFragment(Fragment fragment) {

        FragmentTransaction transaction =
                getSupportFragmentManager().beginTransaction();

        transaction.replace(R.id.fragment_container, fragment);

        transaction.commit();
    }
}