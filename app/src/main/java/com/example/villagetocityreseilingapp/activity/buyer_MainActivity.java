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

import com.example.villagetocityreseilingapp.R;
import com.example.villagetocityreseilingapp.ui.main.buyer.BuyerCartFragment;
import com.example.villagetocityreseilingapp.ui.main.buyer.BuyerHomeFragment;
import com.example.villagetocityreseilingapp.ui.main.buyer.BuyerOrderFragment;
import com.example.villagetocityreseilingapp.ui.main.buyer.BuyerProfileFragment;

public class buyer_MainActivity extends AppCompatActivity {

    // =========================================================
    // BOTTOM NAVIGATION LAYOUTS
    // =========================================================

    private View homeLayout;
    private View menuLayout;
    private View ordersLayout;
    private View dashboardLayout;

    // =========================================================
    // COLORS
    // =========================================================

    // Unselected icon + text
    private static final int BLACK = Color.BLACK;

    // Selected icon + text
    private static final int GREEN =
            Color.parseColor("#4CAF50");

    // =========================================================
    // ON CREATE
    // =========================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // =====================================================
        // BUYER LOADER
        // =====================================================

        setContentView(
                R.layout.fragment_buyer_loader
        );

        // =====================================================
        // FIND NAVIGATION LAYOUTS
        // =====================================================

        homeLayout =
                findViewById(
                        R.id.home_layout
                );

        menuLayout =
                findViewById(
                        R.id.menu_layout
                );

        ordersLayout =
                findViewById(
                        R.id.orders_layout
                );

        dashboardLayout =
                findViewById(
                        R.id.dashboard_layout
                );

        // =====================================================
        // HOME CLICK
        // =====================================================

        homeLayout.setOnClickListener(
                v -> {

                    loadFragment(
                            new BuyerHomeFragment()
                    );

                    setSelectedTab(0);
                }
        );

        // =====================================================
        // CART CLICK
        // =====================================================

        menuLayout.setOnClickListener(
                v -> {

                    loadFragment(
                            new BuyerCartFragment()
                    );

                    setSelectedTab(1);
                }
        );

        // =====================================================
        // ORDERS CLICK
        // =====================================================

        ordersLayout.setOnClickListener(
                v -> {

                    loadFragment(
                            new BuyerOrderFragment()
                    );

                    setSelectedTab(2);
                }
        );

        // =====================================================
        // PROFILE CLICK
        // =====================================================

        dashboardLayout.setOnClickListener(
                v -> {

                    loadFragment(
                            new BuyerProfileFragment()
                    );

                    setSelectedTab(3);
                }
        );

        // =====================================================
        // FIRST SCREEN = HOME
        // =====================================================

        if (savedInstanceState == null) {

            loadFragment(
                    new BuyerHomeFragment()
            );

            setSelectedTab(0);
        }
    }

    // =========================================================
    // LOAD FRAGMENT
    // =========================================================

    public void loadFragment(Fragment fragment) {

        getSupportFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .replace(
                        R.id.fragment_container,
                        fragment
                )
                .commit();
    }

    // =========================================================
    // SELECTED TAB
    // =========================================================

    private void setSelectedTab(int position) {

        // =====================================================
        // HOME
        // =====================================================

        setNavigationColor(
                homeLayout,
                BLACK
        );

        // =====================================================
        // CART
        // =====================================================

        setNavigationColor(
                menuLayout,
                BLACK
        );

        // =====================================================
        // ORDERS
        // =====================================================

        setNavigationColor(
                ordersLayout,
                BLACK
        );

        // =====================================================
        // PROFILE
        // =====================================================

        setNavigationColor(
                dashboardLayout,
                BLACK
        );

        // =====================================================
        // SELECTED NAVIGATION
        // ICON + TEXT = GREEN
        // =====================================================

        switch (position) {

            // =================================================
            // HOME
            // =================================================

            case 0:

                setNavigationColor(
                        homeLayout,
                        GREEN
                );

                break;

            // =================================================
            // CART
            // =================================================

            case 1:

                setNavigationColor(
                        menuLayout,
                        GREEN
                );

                break;

            // =================================================
            // ORDERS
            // =================================================

            case 2:

                setNavigationColor(
                        ordersLayout,
                        GREEN
                );

                break;

            // =================================================
            // PROFILE
            // =================================================

            case 3:

                setNavigationColor(
                        dashboardLayout,
                        GREEN
                );

                break;
        }
    }

    // =========================================================
    // CHANGE BOTH ICON + TEXT COLOR
    // =========================================================

    private void setNavigationColor(
            View parent,
            int color) {

        if (parent == null) {
            return;
        }

        // =====================================================
        // AGAR DIRECT IMAGEVIEW HAI
        // =====================================================

        if (parent instanceof ImageView) {

            ImageView imageView =
                    (ImageView) parent;

            imageView.setImageTintList(
                    ColorStateList.valueOf(
                            color
                    )
            );

            return;
        }

        // =====================================================
        // AGAR DIRECT TEXTVIEW HAI
        // =====================================================

        if (parent instanceof TextView) {

            TextView textView =
                    (TextView) parent;

            textView.setTextColor(color);

            return;
        }

        // =====================================================
        // AGAR VIEWGROUP HAI
        // TO USKE ANDAR KE ICON + TEXT FIND KAREN
        // =====================================================

        if (parent instanceof ViewGroup) {

            ViewGroup group =
                    (ViewGroup) parent;

            for (int i = 0;
                 i < group.getChildCount();
                 i++) {

                View child =
                        group.getChildAt(i);

                // =================================================
                // IMAGEVIEW
                // =================================================

                if (child instanceof ImageView) {

                    ImageView imageView =
                            (ImageView) child;

                    imageView.setImageTintList(
                            ColorStateList.valueOf(
                                    color
                            )
                    );
                }

                // =================================================
                // TEXTVIEW
                // =================================================

                else if (child instanceof TextView) {

                    TextView textView =
                            (TextView) child;

                    textView.setTextColor(
                            color
                    );
                }

                // =================================================
                // NESTED LAYOUT
                // =================================================

                else if (child instanceof ViewGroup) {

                    setNavigationColor(
                            child,
                            color
                    );
                }
            }
        }
    }
}