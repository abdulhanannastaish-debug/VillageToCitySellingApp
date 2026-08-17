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
import com.example.villagetocityreseilingapp.ui.main.seller.SellerDashboardFragment;
import com.example.villagetocityreseilingapp.ui.main.seller.SellerProductFragment;
import com.example.villagetocityreseilingapp.ui.main.seller.SellerOrderFragment;
import com.example.villagetocityreseilingapp.ui.main.seller.SellerProfileFragment;

public class seller_MainActivity extends AppCompatActivity {

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

    // Unselected icon + text
    private static final int BLACK =
            Color.BLACK;

    // Selected icon + text
    private static final int GREEN =
            Color.parseColor("#4CAF50");

    // =========================================================
    // ON CREATE
    // =========================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.seller_activity_main
        );

        // =====================================================
        // FIND BOTTOM NAVIGATION
        // =====================================================

        homeBtn =
                findViewById(
                        R.id.home_layout
                );

        menuBtn =
                findViewById(
                        R.id.menu_layout
                );

        ordersBtn =
                findViewById(
                        R.id.orders_layout
                );

        profileBtn =
                findViewById(
                        R.id.profile_layout
                );

        // =====================================================
        // DEFAULT SCREEN = DASHBOARD
        // =====================================================

        if (savedInstanceState == null) {

            loadFragment(
                    new SellerDashboardFragment()
            );

            setSelectedTab(0);
        }

        // =====================================================
        // HOME / DASHBOARD
        // =====================================================

        homeBtn.setOnClickListener(
                v -> {

                    loadFragment(
                            new SellerDashboardFragment()
                    );

                    setSelectedTab(0);
                }
        );

        // =====================================================
        // PRODUCTS
        // =====================================================

        menuBtn.setOnClickListener(
                v -> {

                    loadFragment(
                            new SellerProductFragment()
                    );

                    setSelectedTab(1);
                }
        );

        // =====================================================
        // ORDERS
        // =====================================================

        ordersBtn.setOnClickListener(
                v -> {

                    loadFragment(
                            new SellerOrderFragment()
                    );

                    setSelectedTab(2);
                }
        );

        // =====================================================
        // PROFILE
        // =====================================================

        profileBtn.setOnClickListener(
                v -> {

                    loadFragment(
                            new SellerProfileFragment()
                    );

                    setSelectedTab(3);
                }
        );
    }

    // =========================================================
    // LOAD FRAGMENT
    // =========================================================

    private void loadFragment(
            Fragment fragment) {

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

    private void setSelectedTab(
            int position) {

        // =====================================================
        // RESET ALL NAVIGATION ITEMS
        // ICON + TEXT = BLACK
        // =====================================================

        setNavigationColor(
                homeBtn,
                BLACK
        );

        setNavigationColor(
                menuBtn,
                BLACK
        );

        setNavigationColor(
                ordersBtn,
                BLACK
        );

        setNavigationColor(
                profileBtn,
                BLACK
        );

        // =====================================================
        // SELECTED ITEM
        // ICON + TEXT = GREEN
        // =====================================================

        switch (position) {

            // =================================================
            // HOME / DASHBOARD
            // =================================================

            case 0:

                setNavigationColor(
                        homeBtn,
                        GREEN
                );

                break;

            // =================================================
            // PRODUCTS
            // =================================================

            case 1:

                setNavigationColor(
                        menuBtn,
                        GREEN
                );

                break;

            // =================================================
            // ORDERS
            // =================================================

            case 2:

                setNavigationColor(
                        ordersBtn,
                        GREEN
                );

                break;

            // =================================================
            // PROFILE
            // =================================================

            case 3:

                setNavigationColor(
                        profileBtn,
                        GREEN
                );

                break;
        }
    }

    // =========================================================
    // CHANGE ICON + TEXT COLOR
    // =========================================================

    private void setNavigationColor(
            View parent,
            int color) {

        if (parent == null) {
            return;
        }

        // =====================================================
        // DIRECT IMAGEVIEW
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
        // DIRECT TEXTVIEW
        // =====================================================

        if (parent instanceof TextView) {

            TextView textView =
                    (TextView) parent;

            textView.setTextColor(
                    color
            );

            return;
        }

        // =====================================================
        // VIEWGROUP KE ANDAR ICON + TEXT FIND KARNA
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
                // IMAGE / ICON
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
                // TEXT / NAME
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