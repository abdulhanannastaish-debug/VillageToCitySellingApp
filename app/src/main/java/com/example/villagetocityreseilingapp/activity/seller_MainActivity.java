package com.example.villagetocityreseilingapp.activity;

import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.villagetocityreseilingapp.R;
import com.example.villagetocityreseilingapp.ui.main.seller.seller_fragment_home;
import com.example.villagetocityreseilingapp.ui.main.seller.seller_fragment_menu;
import com.example.villagetocityreseilingapp.ui.main.seller.seller_fragment_orders;
import com.example.villagetocityreseilingapp.ui.main.seller.seller_fragment_profile;

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

        loadFragment(new seller_fragment_home());

        homeBtn.setOnClickListener(v -> loadFragment(new seller_fragment_home()));
        menuBtn.setOnClickListener(v -> loadFragment(new seller_fragment_menu()));
        ordersBtn.setOnClickListener(v -> loadFragment(new seller_fragment_orders()));
        profileBtn.setOnClickListener(v -> loadFragment(new seller_fragment_profile()));
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }
}