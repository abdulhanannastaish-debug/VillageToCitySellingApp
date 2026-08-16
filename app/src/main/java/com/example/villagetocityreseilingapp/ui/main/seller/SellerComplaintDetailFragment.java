package com.example.villagetocityreseilingapp.ui.main.seller;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.example.villagetocityreseilingapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SellerComplaintDetailFragment extends Fragment {

    private TextView tvSellerId;
    private TextView tvSellerName;
    private TextView tvComplaintDetail;

    private AppCompatButton btnAcceptComplaint;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private String complaintId;

    public SellerComplaintDetailFragment() {
        // Required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_seller_complaint_detail,
                container,
                false
        );

        // =====================================================
        // FIND VIEWS
        // =====================================================

        tvSellerId =
                view.findViewById(R.id.tvSellerId);

        tvSellerName =
                view.findViewById(R.id.tvSellerName);

        tvComplaintDetail =
                view.findViewById(R.id.tvComplaintDetail);

        btnAcceptComplaint =
                view.findViewById(R.id.btnAcceptComplaint);

        // =====================================================
        // FIREBASE
        // =====================================================

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // =====================================================
        // HIDE BUTTON WHILE STATUS IS LOADING
        // =====================================================
        // This prevents:
        // ORANGE -> GREEN blink
        // when an already accepted complaint is opened.
        // =====================================================

        btnAcceptComplaint.setVisibility(View.INVISIBLE);

        // =====================================================
        // GET ARGUMENTS
        // =====================================================

        Bundle bundle = getArguments();

        if (bundle != null) {

            complaintId =
                    bundle.getString("complaintId");

            String sellerId =
                    bundle.getString(
                            "sellerId",
                            "N/A"
                    );

            String sellerName =
                    bundle.getString(
                            "sellerName",
                            "N/A"
                    );

            String complaintDetail =
                    bundle.getString(
                            "complaintDetail",
                            "No complaint detail available"
                    );

            // =================================================
            // SHOW COMPLAINT DATA
            // =================================================

            tvSellerId.setText(sellerId);

            tvSellerName.setText(sellerName);

            tvComplaintDetail.setText(
                    complaintDetail
            );
        }

        // =====================================================
        // BUTTON CLICK
        // =====================================================

        btnAcceptComplaint.setOnClickListener(v -> {

            acceptComplaint();

        });

        // =====================================================
        // LOAD FIRESTORE STATUS
        // =====================================================

        if (complaintId != null
                && !complaintId.trim().isEmpty()) {

            loadComplaintStatus();

        } else {

            // =================================================
            // NO COMPLAINT ID
            // =================================================

            setPendingButton();
        }

        return view;
    }

    // =========================================================
    // LOAD CURRENT COMPLAINT STATUS
    // =========================================================

    private void loadComplaintStatus() {

        db.collection("complaints")
                .document(complaintId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (!documentSnapshot.exists()) {

                        setPendingButton();

                        return;
                    }

                    String status =
                            documentSnapshot.getString(
                                    "status"
                            );

                    // =========================================
                    // ALREADY ACCEPTED
                    // =========================================

                    if ("accepted".equalsIgnoreCase(status)) {

                        setAcceptedButton();

                    }

                    // =========================================
                    // NOT ACCEPTED YET
                    // =========================================

                    else {

                        setPendingButton();
                    }
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            requireContext(),
                            "Failed to load complaint status",
                            Toast.LENGTH_SHORT
                    ).show();

                    setPendingButton();
                });
    }

    // =========================================================
    // ACCEPT COMPLAINT
    // =========================================================

    private void acceptComplaint() {

        if (complaintId == null
                || complaintId.trim().isEmpty()) {

            Toast.makeText(
                    requireContext(),
                    "Complaint ID not found",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (auth.getCurrentUser() == null) {

            Toast.makeText(
                    requireContext(),
                    "Seller is not logged in",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // =====================================================
        // UPDATE STATUS
        // =====================================================

        Map<String, Object> updateData =
                new HashMap<>();

        updateData.put(
                "status",
                "accepted"
        );

        updateData.put(
                "updatedAt",
                FieldValue.serverTimestamp()
        );

        // Disable while updating
        btnAcceptComplaint.setEnabled(false);

        db.collection("complaints")
                .document(complaintId)
                .update(updateData)
                .addOnSuccessListener(unused -> {

                    // =========================================
                    // SUCCESS = GREEN
                    // =========================================

                    setAcceptedButton();

                    Toast.makeText(
                            requireContext(),
                            "Complaint Accepted",
                            Toast.LENGTH_SHORT
                    ).show();
                })
                .addOnFailureListener(e -> {

                    // =========================================
                    // FAILED = ORANGE AGAIN
                    // =========================================

                    setPendingButton();

                    Toast.makeText(
                            requireContext(),
                            "Failed to accept complaint: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    // =========================================================
    // ORANGE BUTTON
    // =========================================================

    private void setPendingButton() {

        btnAcceptComplaint.setText(
                "Accept Complaint"
        );

        btnAcceptComplaint.setTextColor(
                Color.WHITE
        );

        btnAcceptComplaint.setBackgroundTintList(
                ColorStateList.valueOf(
                        Color.rgb(
                                245,
                                124,
                                0
                        )
                )
        );

        btnAcceptComplaint.setEnabled(true);

        // Show only AFTER status has been determined
        btnAcceptComplaint.setVisibility(
                View.VISIBLE
        );
    }

    // =========================================================
    // GREEN BUTTON
    // =========================================================

    private void setAcceptedButton() {

        btnAcceptComplaint.setText(
                "Complaint Accepted"
        );

        btnAcceptComplaint.setTextColor(
                Color.WHITE
        );

        btnAcceptComplaint.setBackgroundTintList(
                ColorStateList.valueOf(
                        Color.rgb(
                                46,
                                125,
                                50
                        )
                )
        );

        btnAcceptComplaint.setEnabled(false);

        // Show only AFTER accepted status is confirmed
        btnAcceptComplaint.setVisibility(
                View.VISIBLE
        );
    }
}