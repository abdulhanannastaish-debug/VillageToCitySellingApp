package com.example.villagetocityreseilingapp.ui.main.seller;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.villagetocityreseilingapp.R;
import com.example.villagetocityreseilingapp.activity.AuthActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SellerProfileFragment extends Fragment {

    private TextView txtSellerName;
    private TextView txtSellerStatus;
    private TextView txtSellerPhone;
    private TextView txtSellerEmail;
    private TextView txtSellerAddress;
    private TextView txtSellerCnic;
    private TextView txtEditProfile;
    private TextView txtLogout;

    private View layoutTerms;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    public SellerProfileFragment() {
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

        View view = inflater.inflate(
                R.layout.fragment_seller_profile,
                container,
                false
        );

        return view;
    }

    // =========================================================
    // VIEW CREATED
    // =========================================================

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        // =====================================================
        // FIREBASE
        // =====================================================

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // =====================================================
        // FIND VIEWS
        // =====================================================

        txtSellerName =
                view.findViewById(R.id.txtSellerName);

        txtSellerStatus =
                view.findViewById(R.id.txtSellerStatus);

        txtSellerPhone =
                view.findViewById(R.id.txtSellerPhone);

        txtSellerEmail =
                view.findViewById(R.id.txtSellerEmail);

        txtSellerAddress =
                view.findViewById(R.id.txtSellerAddress);

        txtSellerCnic =
                view.findViewById(R.id.txtSellerCnic);

        txtEditProfile =
                view.findViewById(R.id.txtEditProfile);

        layoutTerms =
                view.findViewById(R.id.layoutTerms);

        txtLogout =
                view.findViewById(R.id.txtLogout);

        // =====================================================
        // CLEAR OLD / DEFAULT DATA IMMEDIATELY
        // =====================================================

        clearProfileData();

        // =====================================================
        // TERMS & CONDITIONS
        // =====================================================

        layoutTerms.setOnClickListener(v -> {

            if (!isAdded()) {
                return;
            }

            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(
                            R.id.fragment_container,
                            new SellerTermsFragment()
                    )
                    .addToBackStack(null)
                    .commit();
        });

        // =====================================================
        // EDIT PROFILE
        // =====================================================

        txtEditProfile.setOnClickListener(v -> {

            if (!isAdded()) {
                return;
            }

            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(
                            R.id.fragment_container,
                            new SellerEditProfileFragment()
                    )
                    .addToBackStack(null)
                    .commit();
        });

        // =====================================================
        // LOGOUT
        // =====================================================

        txtLogout.setOnClickListener(v -> {

            if (!isAdded()) {
                return;
            }

            showLogoutConfirmation();
        });

        // =====================================================
        // LOAD CURRENT SELLER
        // =====================================================

        loadSellerProfile();
    }

    // =========================================================
    // CLEAR PROFILE DATA
    // =========================================================

    private void clearProfileData() {

        if (txtSellerName != null) {
            txtSellerName.setText("Loading...");
        }

        if (txtSellerStatus != null) {
            txtSellerStatus.setText("Loading...");
        }

        if (txtSellerPhone != null) {
            txtSellerPhone.setText("Loading...");
        }

        if (txtSellerEmail != null) {
            txtSellerEmail.setText("Loading...");
        }

        if (txtSellerAddress != null) {
            txtSellerAddress.setText("Loading...");
        }

        if (txtSellerCnic != null) {
            txtSellerCnic.setText("Loading...");
        }
    }

    // =========================================================
    // LOGOUT CONFIRMATION
    // =========================================================

    private void showLogoutConfirmation() {

        new AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .setPositiveButton(
                        "Logout",
                        (dialog, which) -> logoutSeller()
                )
                .show();
    }

    // =========================================================
    // LOGOUT SELLER
    // =========================================================

    private void logoutSeller() {

        auth.signOut();

        Toast.makeText(
                requireContext(),
                "Logged out successfully.",
                Toast.LENGTH_SHORT
        ).show();

        Intent intent = new Intent(
                requireActivity(),
                AuthActivity.class
        );

        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);

        requireActivity().finish();
    }

    // =========================================================
    // LOAD CURRENT SELLER PROFILE
    // =========================================================

    private void loadSellerProfile() {

        FirebaseUser currentUser =
                auth.getCurrentUser();

        // =====================================================
        // USER CHECK
        // =====================================================

        if (currentUser == null) {

            if (!isAdded()) {
                return;
            }

            txtSellerName.setText("Seller");
            txtSellerStatus.setText("Not Logged In");
            txtSellerPhone.setText("N/A");
            txtSellerEmail.setText("N/A");
            txtSellerAddress.setText("N/A");
            txtSellerCnic.setText("N/A");

            Toast.makeText(
                    requireContext(),
                    "Seller is not logged in.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // =====================================================
        // CURRENT USER UID
        // =====================================================

        String uid =
                currentUser.getUid();

        // =====================================================
        // GET ONLY CURRENT SELLER DOCUMENT
        // =====================================================

        db.collection("sellers")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (!isAdded()) {
                        return;
                    }

                    // =================================================
                    // PROFILE NOT FOUND
                    // =================================================

                    if (!documentSnapshot.exists()) {

                        txtSellerName.setText("Seller");
                        txtSellerStatus.setText(
                                "Verification Pending"
                        );
                        txtSellerPhone.setText("N/A");

                        if (currentUser.getEmail() != null) {
                            txtSellerEmail.setText(
                                    currentUser.getEmail()
                            );
                        } else {
                            txtSellerEmail.setText("N/A");
                        }

                        txtSellerAddress.setText("N/A");
                        txtSellerCnic.setText("N/A");

                        return;
                    }

                    // =================================================
                    // GET CURRENT SELLER DATA
                    // =================================================

                    String name =
                            documentSnapshot.getString("name");

                    String phone =
                            documentSnapshot.getString("phone");

                    String email =
                            documentSnapshot.getString("email");

                    String address =
                            documentSnapshot.getString("address");

                    String cnic =
                            documentSnapshot.getString("cnic");

                    String status =
                            documentSnapshot.getString("status");

                    // =================================================
                    // NAME
                    // =================================================

                    if (name != null &&
                            !name.trim().isEmpty()) {

                        txtSellerName.setText(name);

                    } else {

                        txtSellerName.setText("Seller");
                    }

                    // =================================================
                    // STATUS
                    // =================================================

                    if (status != null &&
                            status.equalsIgnoreCase("verified")) {

                        txtSellerStatus.setText(
                                "Verified Seller"
                        );

                    } else if (status != null &&
                            status.equalsIgnoreCase("rejected")) {

                        txtSellerStatus.setText(
                                "Verification Rejected"
                        );

                    } else {

                        txtSellerStatus.setText(
                                "Verification Pending"
                        );
                    }

                    // =================================================
                    // PHONE
                    // =================================================

                    if (phone != null &&
                            !phone.trim().isEmpty()) {

                        txtSellerPhone.setText(phone);

                    } else {

                        txtSellerPhone.setText("N/A");
                    }

                    // =================================================
                    // EMAIL
                    // =================================================

                    if (email != null &&
                            !email.trim().isEmpty()) {

                        txtSellerEmail.setText(email);

                    } else if (currentUser.getEmail() != null) {

                        txtSellerEmail.setText(
                                currentUser.getEmail()
                        );

                    } else {

                        txtSellerEmail.setText("N/A");
                    }

                    // =================================================
                    // ADDRESS
                    // =================================================

                    if (address != null &&
                            !address.trim().isEmpty()) {

                        txtSellerAddress.setText(address);

                    } else {

                        txtSellerAddress.setText("N/A");
                    }

                    // =================================================
                    // CNIC
                    // =================================================

                    if (cnic != null &&
                            !cnic.trim().isEmpty()) {

                        txtSellerCnic.setText(cnic);

                    } else {

                        txtSellerCnic.setText("N/A");
                    }

                })
                .addOnFailureListener(e -> {

                    if (!isAdded()) {
                        return;
                    }

                    // =================================================
                    // FIRESTORE ERROR
                    // =================================================

                    txtSellerName.setText("Seller");
                    txtSellerStatus.setText(
                            "Unable to load profile"
                    );
                    txtSellerPhone.setText("N/A");
                    txtSellerEmail.setText("N/A");
                    txtSellerAddress.setText("N/A");
                    txtSellerCnic.setText("N/A");

                    Toast.makeText(
                            requireContext(),
                            "Failed to load seller profile: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }
}