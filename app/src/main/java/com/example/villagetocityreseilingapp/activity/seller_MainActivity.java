package com.example.villagetocityreseilingapp.activity;

import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.villagetocityreseilingapp.R;
import com.example.villagetocityreseilingapp.ui.main.seller.SellerHomeFragment;
import com.example.villagetocityreseilingapp.ui.main.seller.SellerMenuFragment;
import com.example.villagetocityreseilingapp.ui.main.seller.SellerOrderFragment;
import com.example.villagetocityreseilingapp.ui.main.seller.SellerProfileFragment;

public class seller_MainActivity extends AppCompatActivity {

    LinearLayout homeBtn, menuBtn, ordersBtn, profileBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.seller_activity_main);

        homeBtn = findViewById(R.id.home_layout);
        menuBtn = findViewById(R.id.menu_layout);
        ordersBtn = findViewById(R.id.orders_layout);
        profileBtn = findViewById(R.id.profile_layout);

        loadFragment(new SellerHomeFragment());

        homeBtn.setOnClickListener(v -> loadFragment(new SellerHomeFragment()));
        menuBtn.setOnClickListener(v -> loadFragment(new SellerMenuFragment()));
        ordersBtn.setOnClickListener(v -> loadFragment(new SellerOrderFragment()));
        profileBtn.setOnClickListener(v -> loadFragment(new SellerProfileFragment()));
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }
}