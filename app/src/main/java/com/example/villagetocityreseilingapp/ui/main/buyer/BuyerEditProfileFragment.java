package com.example.villagetocityreseilingapp.ui.main.buyer;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.example.villagetocityreseilingapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class BuyerEditProfileFragment extends Fragment {

    // =====================================================
    // FIREBASE
    // =====================================================

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // =====================================================
    // EDIT TEXTS
    // =====================================================

    private EditText etEditName;
    private EditText etEditPhone;
    private EditText etEditEmail;
    private EditText etEditAddress;

    // =====================================================
    // SAVE BUTTON
    // =====================================================

    private AppCompatButton btnSaveProfile;

    // =====================================================
    // ON CREATE VIEW
    // =====================================================

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_buyer_edit_profile,
                container,
                false
        );

        // =================================================
        // FIREBASE
        // =================================================

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // =================================================
        // FIND VIEWS
        // =================================================

        etEditName =
                view.findViewById(R.id.etEditName);

        etEditPhone =
                view.findViewById(R.id.etEditPhone);

        etEditEmail =
                view.findViewById(R.id.etEditEmail);

        etEditAddress =
                view.findViewById(R.id.etEditAddress);

        btnSaveProfile =
                view.findViewById(R.id.btnSaveProfile);

        // =================================================
        // LOAD EXISTING DATA
        // =================================================

        loadProfileData();

        // =================================================
        // BACK BUTTON
        // =================================================

        View btnEditProfileBack =
                view.findViewById(R.id.btnEditProfileBack);

        btnEditProfileBack.setOnClickListener(v -> {

            requireActivity()
                    .getSupportFragmentManager()
                    .popBackStack();

        });

        // =================================================
        // SAVE PROFILE
        // =================================================

        btnSaveProfile.setOnClickListener(v -> {

            updateProfile();

        });

        return view;
    }

    // =====================================================
    // LOAD PROFILE DATA
    // =====================================================

    private void loadProfileData() {

        FirebaseUser currentUser =
                mAuth.getCurrentUser();

        if (currentUser == null) {

            Toast.makeText(
                    requireContext(),
                    "User is not logged in",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String uid = currentUser.getUid();

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (documentSnapshot.exists()) {

                        // ==============================
                        // NAME
                        // ==============================

                        String name =
                                documentSnapshot.getString("name");

                        if (name != null &&
                                !name.isEmpty()) {

                            etEditName.setText(name);
                        }

                        // ==============================
                        // PHONE
                        // ==============================

                        String phone =
                                documentSnapshot.getString("phone");

                        if (phone != null &&
                                !phone.isEmpty()) {

                            etEditPhone.setText(phone);
                        }

                        // ==============================
                        // EMAIL
                        // ==============================

                        String email =
                                documentSnapshot.getString("email");

                        if (email != null &&
                                !email.isEmpty()) {

                            etEditEmail.setText(email);

                        } else if (currentUser.getEmail() != null) {

                            etEditEmail.setText(
                                    currentUser.getEmail()
                            );
                        }

                        // ==============================
                        // ADDRESS
                        // ==============================

                        String address =
                                documentSnapshot.getString("address");

                        if (address != null &&
                                !address.isEmpty()) {

                            etEditAddress.setText(address);

                        } else {

                            etEditAddress.setText("");
                        }

                    } else {

                        Toast.makeText(
                                requireContext(),
                                "Profile data not found",
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            requireContext(),
                            "Failed to load profile: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    // =====================================================
    // UPDATE PROFILE
    // =====================================================

    private void updateProfile() {

        FirebaseUser currentUser =
                mAuth.getCurrentUser();

        if (currentUser == null) {

            Toast.makeText(
                    requireContext(),
                    "User is not logged in",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // =================================================
        // GET VALUES
        // =================================================

        String name =
                etEditName.getText()
                        .toString()
                        .trim();

        String phone =
                etEditPhone.getText()
                        .toString()
                        .trim();

        String email =
                etEditEmail.getText()
                        .toString()
                        .trim();

        String address =
                etEditAddress.getText()
                        .toString()
                        .trim();

        // =================================================
        // VALIDATION
        // =================================================

        if (TextUtils.isEmpty(name)) {

            etEditName.setError(
                    "Please enter your name"
            );

            etEditName.requestFocus();

            return;
        }

        if (TextUtils.isEmpty(phone)) {

            etEditPhone.setError(
                    "Please enter your phone number"
            );

            etEditPhone.requestFocus();

            return;
        }

        if (TextUtils.isEmpty(email)) {

            etEditEmail.setError(
                    "Please enter your email"
            );

            etEditEmail.requestFocus();

            return;
        }

        if (TextUtils.isEmpty(address)) {

            etEditAddress.setError(
                    "Please enter your address"
            );

            etEditAddress.requestFocus();

            return;
        }

        // =================================================
        // UID
        // =================================================

        String uid = currentUser.getUid();

        // =================================================
        // DATA TO UPDATE
        // =================================================

        Map<String, Object> updates =
                new HashMap<>();

        updates.put("name", name);
        updates.put("phone", phone);
        updates.put("email", email);
        updates.put("address", address);

        // =================================================
        // UPDATE FIRESTORE
        // =================================================

        btnSaveProfile.setEnabled(false);

        btnSaveProfile.setText(
                "Saving..."
        );

        db.collection("users")
                .document(uid)
                .update(updates)
                .addOnSuccessListener(unused -> {

                    Toast.makeText(
                            requireContext(),
                            "Profile updated successfully",
                            Toast.LENGTH_SHORT
                    ).show();

                    btnSaveProfile.setEnabled(true);

                    btnSaveProfile.setText(
                            "Save Changes"
                    );

                    // =====================================
                    // GO BACK TO PROFILE
                    // =====================================

                    requireActivity()
                            .getSupportFragmentManager()
                            .popBackStack();

                })
                .addOnFailureListener(e -> {

                    btnSaveProfile.setEnabled(true);

                    btnSaveProfile.setText(
                            "Save Changes"
                    );

                    Toast.makeText(
                            requireContext(),
                            "Failed to update profile: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }
}