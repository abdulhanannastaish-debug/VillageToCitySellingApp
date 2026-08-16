package com.example.villagetocityreseilingapp.ui.main.seller;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.villagetocityreseilingapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class SellerComplaintFragment extends Fragment {

    // =====================================================
    // VARIABLES
    // =====================================================

    private LinearLayout complaintContainer;
    private ImageButton btnBack;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public SellerComplaintFragment() {
        // Required empty constructor
    }

    // =====================================================
    // ON CREATE VIEW
    // =====================================================

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_seller_complaint,
                container,
                false
        );

        // =====================================================
        // FIND VIEWS
        // =====================================================

        complaintContainer =
                view.findViewById(R.id.complaintContainer);

        btnBack =
                view.findViewById(R.id.btnBack);

        // =====================================================
        // FIREBASE
        // =====================================================

        db = FirebaseFirestore.getInstance();

        auth = FirebaseAuth.getInstance();

        // =====================================================
        // CHECK CONTAINER
        // =====================================================

        if (complaintContainer == null) {

            Toast.makeText(
                    requireContext(),
                    "Complaint container not found",
                    Toast.LENGTH_LONG
            ).show();

            return view;
        }

        // =====================================================
        // BACK BUTTON
        // =====================================================

        if (btnBack != null) {

            btnBack.setOnClickListener(v -> {

                requireActivity()
                        .getSupportFragmentManager()
                        .popBackStack();

            });
        }

        // =====================================================
        // LOAD SELLER COMPLAINTS
        // =====================================================

        loadComplaints();

        return view;
    }

    // =========================================================
    // LOAD SELLER COMPLAINTS
    // =========================================================

    private void loadComplaints() {

        complaintContainer.removeAllViews();

        // =====================================================
        // CHECK LOGIN
        // =====================================================

        FirebaseUser currentUser =
                auth.getCurrentUser();

        if (currentUser == null) {

            showNoComplaints();

            Toast.makeText(
                    requireContext(),
                    "Seller is not logged in",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        // =====================================================
        // CURRENT SELLER FIREBASE UID
        // =====================================================

        String currentSellerId =
                currentUser.getUid();

        // =====================================================
        // GET ONLY CURRENT SELLER COMPLAINTS
        // =====================================================

        db.collection("complaints")
                .whereEqualTo(
                        "sellerId",
                        currentSellerId
                )
                .get()
                .addOnSuccessListener(
                        queryDocumentSnapshots -> {

                            // =========================================
                            // NO COMPLAINTS
                            // =========================================

                            if (queryDocumentSnapshots.isEmpty()) {

                                showNoComplaints();

                                return;
                            }

                            // =========================================
                            // LOOP COMPLAINTS
                            // =========================================

                            for (
                                    QueryDocumentSnapshot document :
                                    queryDocumentSnapshots
                            ) {

                                // =====================================
                                // COMPLAINT DOCUMENT ID
                                // =====================================

                                String complaintId =
                                        document.getId();

                                // =====================================
                                // SELLER ID
                                // =====================================

                                String sellerId =
                                        document.getString(
                                                "sellerId"
                                        );

                                // =====================================
                                // SELLER NAME
                                // =====================================

                                String sellerName =
                                        document.getString(
                                                "sellerName"
                                        );

                                // =====================================
                                // COMPLAINT DETAIL
                                // =====================================

                                String complaintDetail =
                                        document.getString(
                                                "complaintDetail"
                                        );

                                // =====================================
                                // IF complaintDetail DOES NOT EXIST
                                // TRY complaint
                                // =====================================

                                if (
                                        complaintDetail == null
                                                ||
                                                complaintDetail
                                                        .trim()
                                                        .isEmpty()
                                ) {

                                    complaintDetail =
                                            document.getString(
                                                    "complaint"
                                            );
                                }

                                // =====================================
                                // IF complaint DOES NOT EXIST
                                // TRY complaintText
                                // =====================================

                                if (
                                        complaintDetail == null
                                                ||
                                                complaintDetail
                                                        .trim()
                                                        .isEmpty()
                                ) {

                                    complaintDetail =
                                            document.getString(
                                                    "complaintText"
                                            );
                                }

                                // =====================================
                                // NULL SAFETY
                                // =====================================

                                if (
                                        sellerId == null
                                                ||
                                                sellerId.trim().isEmpty()
                                ) {

                                    sellerId = "N/A";
                                }

                                if (
                                        sellerName == null
                                                ||
                                                sellerName.trim().isEmpty()
                                ) {

                                    sellerName = "N/A";
                                }

                                if (
                                        complaintDetail == null
                                                ||
                                                complaintDetail
                                                        .trim()
                                                        .isEmpty()
                                ) {

                                    complaintDetail =
                                            "No complaint detail available";
                                }

                                // =====================================
                                // ADD COMPLAINT CARD
                                // =====================================

                                addComplaintCard(
                                        complaintId,
                                        sellerId,
                                        sellerName,
                                        complaintDetail
                                );
                            }
                        }
                )
                .addOnFailureListener(e -> {

                    // =============================================
                    // SHOW ERROR
                    // =============================================

                    showNoComplaints();

                    Toast.makeText(
                            requireContext(),
                            "Failed to load complaints:\n"
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    // =========================================================
    // ADD COMPLAINT CARD
    // =========================================================

    private void addComplaintCard(
            String complaintId,
            String sellerId,
            String sellerName,
            String complaintDetail) {

        // =====================================================
        // CARD
        // =====================================================

        CardView cardView =
                new CardView(requireContext());

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        cardParams.setMargins(
                0,
                0,
                0,
                16
        );

        cardView.setLayoutParams(
                cardParams
        );

        cardView.setRadius(16);

        cardView.setCardElevation(4);

        cardView.setUseCompatPadding(true);

        cardView.setClickable(true);

        cardView.setFocusable(true);

        // =====================================================
        // CARD LAYOUT
        // =====================================================

        LinearLayout cardLayout =
                new LinearLayout(requireContext());

        cardLayout.setOrientation(
                LinearLayout.VERTICAL
        );

        cardLayout.setPadding(
                20,
                20,
                20,
                20
        );

        // =====================================================
        // SELLER NAME
        // =====================================================

        TextView sellerNameLabel =
                createLabel(
                        "Seller Name"
                );

        TextView sellerNameText =
                createValue(
                        sellerName
                );

        // =====================================================
        // SELLER ID
        // =====================================================

        TextView sellerIdLabel =
                createLabel(
                        "Seller ID"
                );

        TextView sellerIdText =
                createValue(
                        sellerId
                );

        // =====================================================
        // COMPLAINT
        // =====================================================

        TextView complaintLabel =
                createLabel(
                        "Complaint"
                );

        TextView complaintText =
                createValue(
                        complaintDetail
                );

        complaintText.setMaxLines(3);

        // =====================================================
        // VIEW DETAILS
        // =====================================================

        TextView viewDetails =
                new TextView(requireContext());

        viewDetails.setText(
                "View Complaint Details"
        );

        viewDetails.setTextSize(14);

        viewDetails.setTextColor(
                Color.rgb(
                        46,
                        125,
                        50
                )
        );

        viewDetails.setTypeface(
                null,
                Typeface.BOLD
        );

        viewDetails.setPadding(
                0,
                16,
                0,
                4
        );

        // =====================================================
        // ADD TO CARD
        // =====================================================

        cardLayout.addView(
                sellerNameLabel
        );

        cardLayout.addView(
                sellerNameText
        );

        cardLayout.addView(
                sellerIdLabel
        );

        cardLayout.addView(
                sellerIdText
        );

        cardLayout.addView(
                complaintLabel
        );

        cardLayout.addView(
                complaintText
        );

        cardLayout.addView(
                viewDetails
        );

        cardView.addView(
                cardLayout
        );

        complaintContainer.addView(
                cardView
        );

        // =====================================================
        // CARD CLICK
        // =====================================================

        cardView.setOnClickListener(v -> {

            Bundle bundle =
                    new Bundle();

            // =============================================
            // COMPLAINT ID
            // =============================================

            bundle.putString(
                    "complaintId",
                    complaintId
            );

            // =============================================
            // SELLER ID
            // =============================================

            bundle.putString(
                    "sellerId",
                    sellerId
            );

            // =============================================
            // SELLER NAME
            // =============================================

            bundle.putString(
                    "sellerName",
                    sellerName
            );

            // =============================================
            // COMPLAINT DETAIL
            // =============================================

            bundle.putString(
                    "complaintDetail",
                    complaintDetail
            );

            // =============================================
            // OPEN DETAIL FRAGMENT
            // =============================================

            SellerComplaintDetailFragment
                    detailFragment =
                    new SellerComplaintDetailFragment();

            detailFragment.setArguments(
                    bundle
            );

            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(
                            R.id.fragment_container,
                            detailFragment
                    )
                    .addToBackStack(null)
                    .commit();
        });
    }

    // =========================================================
    // CREATE LABEL
    // =========================================================

    private TextView createLabel(
            String text) {

        TextView textView =
                new TextView(
                        requireContext()
                );

        textView.setText(text);

        textView.setTextSize(12);

        textView.setTextColor(
                Color.GRAY
        );

        textView.setTypeface(
                null,
                Typeface.NORMAL
        );

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        params.setMargins(
                0,
                4,
                0,
                2
        );

        textView.setLayoutParams(
                params
        );

        return textView;
    }

    // =========================================================
    // CREATE VALUE
    // =========================================================

    private TextView createValue(
            String text) {

        TextView textView =
                new TextView(
                        requireContext()
                );

        textView.setText(text);

        textView.setTextSize(15);

        textView.setTextColor(
                Color.rgb(
                        40,
                        40,
                        40
                )
        );

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        params.setMargins(
                0,
                0,
                0,
                10
        );

        textView.setLayoutParams(
                params
        );

        return textView;
    }

    // =========================================================
    // NO COMPLAINTS
    // =========================================================

    private void showNoComplaints() {

        if (complaintContainer == null) {
            return;
        }

        complaintContainer.removeAllViews();

        TextView textView =
                new TextView(
                        requireContext()
                );

        textView.setText(
                "No complaints found"
        );

        textView.setTextSize(16);

        textView.setTextColor(
                Color.GRAY
        );

        textView.setGravity(
                Gravity.CENTER
        );

        textView.setPadding(
                20,
                40,
                20,
                40
        );

        textView.setContentDescription(
                "No complaints found"
        );

        complaintContainer.addView(
                textView
        );
    }
}