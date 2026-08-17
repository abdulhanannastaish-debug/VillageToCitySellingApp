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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.Locale;

public class SellerDashboardFragment extends Fragment {

    // =========================================================
    // DASHBOARD QUICK ACCESS
    // =========================================================

    private LinearLayout layoutRestaurantProfile;
    private LinearLayout layoutManageMenu;
    private LinearLayout layoutSellerWallet;

    // =========================================================
    // DASHBOARD CARDS
    // =========================================================

    private View cardComplaint;
    private View cardRating;

    // =========================================================
    // DASHBOARD COUNTS
    // =========================================================

    private TextView txtComplaintCount;
    private TextView txtMenuCount;
    private TextView txtRating;

    // =========================================================
    // FIREBASE
    // =========================================================

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    // =========================================================
    // LISTENERS
    // =========================================================

    private ListenerRegistration productsListener;
    private ListenerRegistration reviewsListener;
    private ListenerRegistration complaintsListener;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

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

        super.onViewCreated(
                view,
                savedInstanceState
        );

        // =====================================================
        // FIREBASE
        // =====================================================

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // =====================================================
        // FIND QUICK ACCESS
        // =====================================================

        layoutRestaurantProfile =
                view.findViewById(
                        R.id.layoutRestaurantProfile
                );

        layoutManageMenu =
                view.findViewById(
                        R.id.layoutManageMenu
                );

        layoutSellerWallet =
                view.findViewById(
                        R.id.layoutSellerWallet
                );

        // =====================================================
        // FIND DASHBOARD CARDS
        // =====================================================

        cardComplaint =
                view.findViewById(
                        R.id.cardComplaint
                );

        cardRating =
                view.findViewById(
                        R.id.cardRating
                );

        // =====================================================
        // FIND DASHBOARD TEXT
        // =====================================================

        txtComplaintCount =
                view.findViewById(
                        R.id.txtComplaintCount
                );

        txtMenuCount =
                view.findViewById(
                        R.id.txtMenuCount
                );

        txtRating =
                view.findViewById(
                        R.id.txtRating
                );

        // =====================================================
        // INITIAL VALUES
        // =====================================================

        if (txtComplaintCount != null) {
            txtComplaintCount.setText("0");
        }

        if (txtMenuCount != null) {
            txtMenuCount.setText("0");
        }

        if (txtRating != null) {
            txtRating.setText("0.0 ★");
        }

        // =====================================================
        // COMPLAINT BOX
        // =====================================================

        if (cardComplaint != null) {

            cardComplaint.setClickable(true);
            cardComplaint.setFocusable(true);

            cardComplaint.setOnClickListener(v -> {

                if (!isAdded()) {
                    return;
                }

                requireActivity()
                        .getSupportFragmentManager()
                        .beginTransaction()
                        .setReorderingAllowed(true)
                        .replace(
                                R.id.fragment_container,
                                new SellerComplaintFragment()
                        )
                        .addToBackStack(null)
                        .commit();
            });
        }

        // =====================================================
        // SELLER PROFILE
        // =====================================================

        if (layoutRestaurantProfile != null) {

            layoutRestaurantProfile.setOnClickListener(v -> {

                if (!isAdded()) {
                    return;
                }

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
        }

        // =====================================================
        // MANAGE PRODUCT
        // =====================================================

        if (layoutManageMenu != null) {

            layoutManageMenu.setOnClickListener(v -> {

                if (!isAdded()) {
                    return;
                }

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
        }

        // =====================================================
        // SELLER WALLET
        // =====================================================

        if (layoutSellerWallet != null) {

            layoutSellerWallet.setOnClickListener(v -> {

                if (!isAdded()) {
                    return;
                }

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
        }

        // =====================================================
        // RATING CARD
        // =====================================================

        if (cardRating != null) {

            cardRating.setClickable(true);
            cardRating.setFocusable(true);

            cardRating.setOnClickListener(v -> {

                if (!isAdded()) {
                    return;
                }

                requireActivity()
                        .getSupportFragmentManager()
                        .beginTransaction()
                        .setReorderingAllowed(true)
                        .replace(
                                R.id.fragment_container,
                                new SellerReviewsFragment()
                        )
                        .addToBackStack(null)
                        .commit();
            });
        }

        // =====================================================
        // LOAD DASHBOARD DATA
        // =====================================================

        loadComplaintCount();
        loadProductCount();
        loadSellerRating();
    }

    // =========================================================
    // LOAD COMPLAINT COUNT
    //
    // Sirf un complaints ko count karega jo accepted nahi hain.
    //
    // Example:
    // 5 complaints
    // 1 accepted
    // Dashboard = 4
    //
    // Firestore realtime listener ki wajah se count automatically
    // update hoga.
    // =========================================================

    private void loadComplaintCount() {

        if (!isAdded()) {
            return;
        }

        FirebaseUser currentUser =
                auth.getCurrentUser();

        // =====================================================
        // CHECK LOGIN
        // =====================================================

        if (currentUser == null) {

            if (txtComplaintCount != null) {
                txtComplaintCount.setText("0");
            }

            return;
        }

        String sellerId =
                currentUser.getUid();

        // =====================================================
        // REMOVE OLD LISTENER
        // =====================================================

        if (complaintsListener != null) {

            complaintsListener.remove();

            complaintsListener = null;
        }

        // =====================================================
        // REALTIME COMPLAINT LISTENER
        // =====================================================

        complaintsListener =
                db.collection("complaints")
                        .whereEqualTo(
                                "sellerId",
                                sellerId
                        )
                        .addSnapshotListener(
                                (snapshots, error) -> {

                                    if (!isAdded()) {
                                        return;
                                    }

                                    // =================================
                                    // ERROR
                                    // =================================

                                    if (error != null ||
                                            snapshots == null) {

                                        if (txtComplaintCount != null) {
                                            txtComplaintCount.setText("0");
                                        }

                                        return;
                                    }

                                    // =================================
                                    // COUNT ONLY NOT ACCEPTED
                                    // =================================

                                    int complaintCount = 0;

                                    for (
                                            DocumentSnapshot document
                                            : snapshots.getDocuments()
                                    ) {

                                        String status =
                                                document.getString(
                                                        "status"
                                                );

                                        // =================================
                                        // ACCEPTED COMPLAINT
                                        // =================================
                                        // Accepted complaint dashboard
                                        // count mein nahi aayegi.
                                        // =================================

                                        if (
                                                status != null
                                                        &&
                                                        status.trim()
                                                                .equalsIgnoreCase(
                                                                        "accepted"
                                                                )
                                        ) {

                                            continue;
                                        }

                                        // =================================
                                        // PENDING / OTHER STATUS
                                        // =================================

                                        complaintCount++;
                                    }

                                    // =================================
                                    // SHOW CURRENT COUNT
                                    // =================================

                                    if (txtComplaintCount != null) {

                                        txtComplaintCount.setText(
                                                String.valueOf(
                                                        complaintCount
                                                )
                                        );
                                    }
                                }
                        );
    }

    // =========================================================
    // LOAD CURRENT SELLER PRODUCT COUNT
    // =========================================================

    private void loadProductCount() {

        if (!isAdded()) {
            return;
        }

        FirebaseUser currentUser =
                auth.getCurrentUser();

        // =====================================================
        // CHECK LOGIN
        // =====================================================

        if (currentUser == null) {

            if (txtMenuCount != null) {
                txtMenuCount.setText("0");
            }

            return;
        }

        String sellerId =
                currentUser.getUid();

        // =====================================================
        // REMOVE OLD LISTENER
        // =====================================================

        if (productsListener != null) {

            productsListener.remove();

            productsListener = null;
        }

        // =====================================================
        // REALTIME PRODUCTS LISTENER
        // =====================================================

        productsListener =
                db.collection("products")
                        .addSnapshotListener(
                                (queryDocumentSnapshots, error) -> {

                                    if (!isAdded()) {
                                        return;
                                    }

                                    if (error != null ||
                                            queryDocumentSnapshots == null) {

                                        if (txtMenuCount != null) {
                                            txtMenuCount.setText("0");
                                        }

                                        return;
                                    }

                                    int productCount = 0;

                                    // =================================
                                    // CHECK EVERY PRODUCT
                                    // =================================

                                    for (
                                            DocumentSnapshot document
                                            : queryDocumentSnapshots
                                    ) {

                                        Object sellerIdObject =
                                                document.get(
                                                        "sellerId"
                                                );

                                        if (sellerIdObject == null) {
                                            continue;
                                        }

                                        String productSellerId =
                                                String.valueOf(
                                                                sellerIdObject
                                                        )
                                                        .trim();

                                        // =================================
                                        // CURRENT SELLER ONLY
                                        // =================================

                                        if (!sellerId.equals(
                                                productSellerId
                                        )) {
                                            continue;
                                        }

                                        // =================================
                                        // PRODUCT STATUS
                                        // =================================

                                        Object statusObject =
                                                document.get(
                                                        "status"
                                                );

                                        if (statusObject != null) {

                                            String status =
                                                    String.valueOf(
                                                                    statusObject
                                                            )
                                                            .trim()
                                                            .toLowerCase(
                                                                    Locale.getDefault()
                                                            );

                                            // =================================
                                            // DO NOT COUNT DELETED
                                            // =================================

                                            if (status.equals("deleted")
                                                    ||
                                                    status.equals("delete")) {

                                                continue;
                                            }
                                        }

                                        productCount++;
                                    }

                                    // =================================
                                    // SHOW COUNT
                                    // =================================

                                    if (txtMenuCount != null) {

                                        txtMenuCount.setText(
                                                String.valueOf(
                                                        productCount
                                                )
                                        );
                                    }
                                }
                        );
    }

    // =========================================================
    // LOAD SELLER RATING
    // =========================================================

    private void loadSellerRating() {

        if (!isAdded()) {
            return;
        }

        FirebaseUser currentUser =
                auth.getCurrentUser();

        // =====================================================
        // CHECK LOGIN
        // =====================================================

        if (currentUser == null) {

            if (txtRating != null) {
                txtRating.setText("0.0 ★");
            }

            return;
        }

        String sellerId =
                currentUser.getUid();

        // =====================================================
        // REMOVE OLD REVIEW LISTENER
        // =====================================================

        if (reviewsListener != null) {

            reviewsListener.remove();

            reviewsListener = null;
        }

        // =====================================================
        // REALTIME REVIEWS
        // =====================================================

        reviewsListener =
                db.collection("reviews")
                        .whereEqualTo(
                                "sellerId",
                                sellerId
                        )
                        .addSnapshotListener(
                                (queryDocumentSnapshots, error) -> {

                                    if (!isAdded()) {
                                        return;
                                    }

                                    // =================================
                                    // ERROR
                                    // =================================

                                    if (error != null) {

                                        if (txtRating != null) {
                                            txtRating.setText(
                                                    "0.0 ★"
                                            );
                                        }

                                        return;
                                    }

                                    // =================================
                                    // EMPTY
                                    // =================================

                                    if (
                                            queryDocumentSnapshots == null
                                                    ||
                                                    queryDocumentSnapshots.isEmpty()
                                    ) {

                                        if (txtRating != null) {
                                            txtRating.setText(
                                                    "0.0 ★"
                                            );
                                        }

                                        return;
                                    }

                                    // =================================
                                    // CALCULATE RATING
                                    // =================================

                                    double totalRating =
                                            0.0;

                                    int ratingCount =
                                            0;

                                    for (
                                            DocumentSnapshot document
                                            : queryDocumentSnapshots
                                            .getDocuments()
                                    ) {

                                        Object ratingObject =
                                                document.get(
                                                        "rating"
                                                );

                                        if (
                                                ratingObject
                                                        instanceof Number
                                        ) {

                                            double rating =
                                                    (
                                                            (
                                                                    Number
                                                                    ) ratingObject
                                                    )
                                                            .doubleValue();

                                            if (
                                                    rating >= 0
                                                            &&
                                                            rating <= 5
                                            ) {

                                                totalRating +=
                                                        rating;

                                                ratingCount++;
                                            }
                                        }
                                    }

                                    // =================================
                                    // NO VALID RATINGS
                                    // =================================

                                    if (ratingCount == 0) {

                                        if (txtRating != null) {
                                            txtRating.setText(
                                                    "0.0 ★"
                                            );
                                        }

                                        return;
                                    }

                                    // =================================
                                    // AVERAGE
                                    // =================================

                                    double averageRating =
                                            totalRating
                                                    / ratingCount;

                                    String formattedRating =
                                            String.format(
                                                    Locale.getDefault(),
                                                    "%.1f ★",
                                                    averageRating
                                            );

                                    // =================================
                                    // SHOW RATING
                                    // =================================

                                    if (txtRating != null) {

                                        txtRating.setText(
                                                formattedRating
                                        );
                                    }
                                }
                        );
    }

    // =========================================================
    // REFRESH
    // =========================================================

    @Override
    public void onResume() {

        super.onResume();

        if (
                auth != null
                        &&
                        db != null
        ) {

            loadComplaintCount();
            loadProductCount();
            loadSellerRating();
        }
    }

    // =========================================================
    // DESTROY VIEW
    // =========================================================

    @Override
    public void onDestroyView() {

        // =====================================================
        // REMOVE COMPLAINT LISTENER
        // =====================================================

        if (complaintsListener != null) {

            complaintsListener.remove();

            complaintsListener = null;
        }

        // =====================================================
        // REMOVE PRODUCTS LISTENER
        // =====================================================

        if (productsListener != null) {

            productsListener.remove();

            productsListener = null;
        }

        // =====================================================
        // REMOVE REVIEWS LISTENER
        // =====================================================

        if (reviewsListener != null) {

            reviewsListener.remove();

            reviewsListener = null;
        }

        super.onDestroyView();
    }
}