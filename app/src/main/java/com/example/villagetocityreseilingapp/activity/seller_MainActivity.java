package com.example.villagetocityreseilingapp.activity;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.villagetocityreseilingapp.LocaleHelper;
import com.example.villagetocityreseilingapp.R;
import com.example.villagetocityreseilingapp.ui.main.seller.SellerDashboardFragment;
import com.example.villagetocityreseilingapp.ui.main.seller.SellerProductFragment;
import com.example.villagetocityreseilingapp.ui.main.seller.SellerOrderFragment;
import com.example.villagetocityreseilingapp.ui.main.seller.SellerProfileFragment;

public class seller_MainActivity extends AppCompatActivity {

    // =========================================================
    // LANGUAGE (LOCALE)
    // =========================================================

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase));
    }

    // =========================================================
    // BOTTOM NAVIGATION
    // =========================================================

    private View homeBtn;
    private View menuBtn;
    private View ordersBtn;
    private View profileBtn;

    // =========================================================
    // COLORS
    // =========================================================

    private static final int BLACK = Color.BLACK;
    private static final int GREEN = Color.parseColor("#4CAF50");

    // =========================================================
    // ON CREATE
    // =========================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // =====================================================
        // RTL / LTR LAYOUT DIRECTION
        // =====================================================

        String lang = LocaleHelper.getSavedLanguage(this);

        if ("ur".equals(lang)) {
            getWindow().getDecorView().setLayoutDirection(
                    View.LAYOUT_DIRECTION_RTL
            );
        } else {
            getWindow().getDecorView().setLayoutDirection(
                    View.LAYOUT_DIRECTION_LTR
            );
        }

        setContentView(R.layout.seller_activity_main);

        // =====================================================
        // FIND BOTTOM NAVIGATION
        // =====================================================

        homeBtn = findViewById(R.id.home_layout);
        menuBtn = findViewById(R.id.menu_layout);
        ordersBtn = findViewById(R.id.orders_layout);
        profileBtn = findViewById(R.id.profile_layout);

        // =====================================================
        // DEFAULT SCREEN = DASHBOARD
        // =====================================================

        if (savedInstanceState == null) {
            loadFragment(new SellerDashboardFragment());
            setSelectedTab(0);
        }

        // =====================================================
        // HOME / DASHBOARD
        // =====================================================

        homeBtn.setOnClickListener(v -> {
            loadFragment(new SellerDashboardFragment());
            setSelectedTab(0);
        });

        // =====================================================
        // PRODUCTS
        // =====================================================

        menuBtn.setOnClickListener(v -> {
            loadFragment(new SellerProductFragment());
            setSelectedTab(1);
        });

        // =====================================================
        // ORDERS
        // =====================================================

        ordersBtn.setOnClickListener(v -> {
            loadFragment(new SellerOrderFragment());
            setSelectedTab(2);
        });

        // =====================================================
        // PROFILE
        // =====================================================

        profileBtn.setOnClickListener(v -> {
            loadFragment(new SellerProfileFragment());
            setSelectedTab(3);
        });
    }

    // =========================================================
    // LOAD FRAGMENT
    // =========================================================

    private void loadFragment(Fragment fragment) {

        getSupportFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    // =========================================================
    // SELECTED TAB
    // =========================================================

    private void setSelectedTab(int position) {

        setNavigationColor(homeBtn, BLACK);
        setNavigationColor(menuBtn, BLACK);
        setNavigationColor(ordersBtn, BLACK);
        setNavigationColor(profileBtn, BLACK);

        switch (position) {

            case 0:
                setNavigationColor(homeBtn, GREEN);
                break;

            case 1:
                setNavigationColor(menuBtn, GREEN);
                break;

            case 2:
                setNavigationColor(ordersBtn, GREEN);
                break;

            case 3:
                setNavigationColor(profileBtn, GREEN);
                break;
        }
    }

    // =========================================================
    // CHANGE ICON + TEXT COLOR
    // =========================================================

    private void setNavigationColor(View parent, int color) {

        if (parent == null) return;

        if (parent instanceof ImageView) {
            ((ImageView) parent).setImageTintList(
                    ColorStateList.valueOf(color)
            );
            return;
        }

        if (parent instanceof TextView) {
            ((TextView) parent).setTextColor(color);
            return;
        }

        if (parent instanceof ViewGroup) {

            ViewGroup group = (ViewGroup) parent;

            for (int i = 0; i < group.getChildCount(); i++) {

                View child = group.getChildAt(i);

                if (child instanceof ImageView) {
                    ((ImageView) child).setImageTintList(
                            ColorStateList.valueOf(color)
                    );
                } else if (child instanceof TextView) {
                    ((TextView) child).setTextColor(color);
                } else if (child instanceof ViewGroup) {
                    setNavigationColor(child, color);
                }
            }
        }
    }
}