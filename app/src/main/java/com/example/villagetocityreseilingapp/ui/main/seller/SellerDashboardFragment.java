package com.example.villagetocityreseilingapp.ui.main.seller;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.villagetocityreseilingapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SellerDashboardFragment extends Fragment {

    // ================= DASHBOARD QUICK ACCESS =================

    private LinearLayout layoutRestaurantProfile;
    private LinearLayout layoutManageMenu;
    private LinearLayout layoutSellerWallet;

    // ================= DASHBOARD COUNTS =================

    private TextView txtMenuCount;

    // ================= FIREBASE =================

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    public SellerDashboardFragment() {
        // Required empty public constructor
    }

    // =========================================================
    // CREATE VIEW
    // =========================================================

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_seller_dashboard,
                container,
                false
        );
    }

    // =========================================================
    // VIEW CREATED
    // =========================================================

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        // ================= FIREBASE =================

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // ================= FIND QUICK ACCESS =================

        layoutRestaurantProfile =
                view.findViewById(R.id.layoutRestaurantProfile);

        layoutManageMenu =
                view.findViewById(R.id.layoutManageMenu);

        layoutSellerWallet =
                view.findViewById(R.id.layoutSellerWallet);

        // ================= FIND MY PRODUCTS COUNT =================

        txtMenuCount =
                view.findViewById(R.id.txtMenuCount);

        // ================= INITIAL VALUE =================

        txtMenuCount.setText("0");

        // ================= SELLER PROFILE =================

        layoutRestaurantProfile.setOnClickListener(v -> {

            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(
                            R.id.fragment_container,
                            new SellerProfileFragment()
                    )
                    .addToBackStack(null)
                    .commit();

        });

        // ================= MANAGE PRODUCT =================

        layoutManageMenu.setOnClickListener(v -> {

            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(
                            R.id.fragment_container,
                            new SellerProductFragment()
                    )
                    .addToBackStack(null)
                    .commit();

        });

        // ================= SELLER WALLET =================

        layoutSellerWallet.setOnClickListener(v -> {

            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(
                            R.id.fragment_container,
                            new SellerWalletFragment()
                    )
                    .addToBackStack(null)
                    .commit();

        });

        // ================= LOAD DASHBOARD DATA =================

        loadProductCount();
    }

    // =========================================================
    // LOAD CURRENT SELLER PRODUCT COUNT
    // =========================================================

    private void loadProductCount() {

        FirebaseUser currentUser =
                auth.getCurrentUser();

        // ================= USER NOT LOGGED IN =================

        if (currentUser == null) {

            txtMenuCount.setText("0");

            return;
        }

        String sellerId =
                currentUser.getUid();

        // ================= GET SELLER PRODUCTS =================

        db.collection("products")
                .whereEqualTo(
                        "sellerId",
                        sellerId
                )
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    if (!isAdded()) {
                        return;
                    }

                    int productCount =
                            queryDocumentSnapshots.size();

                    txtMenuCount.setText(
                            String.valueOf(productCount)
                    );

                })
                .addOnFailureListener(e -> {

                    if (!isAdded()) {
                        return;
                    }

                    txtMenuCount.setText("0");

                });
    }

    // =========================================================
    // REFRESH PRODUCT COUNT
    // =========================================================

    @Override
    public void onResume() {

        super.onResume();

        /*
         * Agar seller Product screen se wapas dashboard par aaye
         * to product count dobara Firestore se load hoga.
         */

        if (auth != null) {

            loadProductCount();
        }
    }
}