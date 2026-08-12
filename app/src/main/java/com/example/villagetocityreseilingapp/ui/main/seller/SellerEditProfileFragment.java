package com.example.villagetocityreseilingapp.ui.main.seller;

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

public class SellerEditProfileFragment extends Fragment {

    private EditText etFullName;
    private EditText etPhone;
    private EditText etEmail;
    private EditText etAddress;
    private EditText etCnic;

    private AppCompatButton btnSaveProfile;
    private AppCompatButton btnCancel;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    public SellerEditProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_seller_edit_profile,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        // ================= FIREBASE =================

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // ================= FIND VIEWS =================

        etFullName = view.findViewById(R.id.etFullName);
        etPhone = view.findViewById(R.id.etPhone);
        etEmail = view.findViewById(R.id.etEmail);
        etAddress = view.findViewById(R.id.etAddress);
        etCnic = view.findViewById(R.id.etCnic);

        btnSaveProfile = view.findViewById(R.id.btnSaveProfile);
        btnCancel = view.findViewById(R.id.btnCancel);

        // ================= LOAD CURRENT DATA =================

        loadSellerData();

        // ================= SAVE =================

        btnSaveProfile.setOnClickListener(v ->
                saveSellerProfile()
        );

        // ================= CANCEL =================

        btnCancel.setOnClickListener(v ->
                goBackToProfile()
        );

        // ================= BACK ARROW =================

        View btnTermsBack =
                view.findViewById(R.id.btnTermsBack);

        if (btnTermsBack != null) {

            btnTermsBack.setOnClickListener(v ->
                    goBackToProfile()
            );
        }
    }

    // =========================================================
    // LOAD SELLER DATA FROM FIRESTORE
    // =========================================================

    private void loadSellerData() {

        FirebaseUser currentUser =
                auth.getCurrentUser();

        if (currentUser == null) {

            Toast.makeText(
                    requireContext(),
                    "Seller is not logged in.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String uid =
                currentUser.getUid();

        db.collection("sellers")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (!documentSnapshot.exists()) {

                        Toast.makeText(
                                requireContext(),
                                "Seller profile not found.",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }

                    // ================= NAME =================

                    String name =
                            documentSnapshot.getString("name");

                    if (name != null) {
                        etFullName.setText(name);
                    }

                    // ================= PHONE =================

                    String phone =
                            documentSnapshot.getString("phone");

                    if (phone != null) {
                        etPhone.setText(phone);
                    }

                    // ================= EMAIL =================

                    String email =
                            documentSnapshot.getString("email");

                    if (email != null) {
                        etEmail.setText(email);
                    }

                    // ================= ADDRESS =================

                    String address =
                            documentSnapshot.getString("address");

                    if (address != null) {
                        etAddress.setText(address);
                    }

                    // ================= CNIC =================

                    String cnic =
                            documentSnapshot.getString("cnic");

                    if (cnic != null) {
                        etCnic.setText(cnic);
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

    // =========================================================
    // SAVE SELLER PROFILE
    // =========================================================

    private void saveSellerProfile() {

        String name =
                etFullName.getText().toString().trim();

        String phone =
                etPhone.getText().toString().trim();

        String email =
                etEmail.getText().toString().trim();

        String address =
                etAddress.getText().toString().trim();

        String cnic =
                etCnic.getText().toString().trim();

        // ================= VALIDATION =================

        if (TextUtils.isEmpty(name)) {

            etFullName.setError("Enter your name");
            etFullName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(phone)) {

            etPhone.setError("Enter your phone number");
            etPhone.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {

            etEmail.setError("Enter your email");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(address)) {

            etAddress.setError("Enter your address");
            etAddress.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(cnic)) {

            etCnic.setError("Enter your CNIC");
            etCnic.requestFocus();
            return;
        }

        if (cnic.length() != 13) {

            etCnic.setError(
                    "CNIC must contain 13 digits"
            );

            etCnic.requestFocus();
            return;
        }

        // ================= CURRENT USER =================

        FirebaseUser currentUser =
                auth.getCurrentUser();

        if (currentUser == null) {

            Toast.makeText(
                    requireContext(),
                    "Seller is not logged in.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String uid =
                currentUser.getUid();

        // =====================================================
        // UPDATE FIRESTORE ONLY
        // =====================================================

        Map<String, Object> sellerUpdate =
                new HashMap<>();

        sellerUpdate.put("name", name);
        sellerUpdate.put("phone", phone);
        sellerUpdate.put("email", email);
        sellerUpdate.put("address", address);
        sellerUpdate.put("cnic", cnic);

        // =====================================================
        // UPDATE SELLERS COLLECTION
        // =====================================================

        db.collection("sellers")
                .document(uid)
                .update(sellerUpdate)
                .addOnSuccessListener(unused -> {

                    // =================================================
                    // UPDATE USERS COLLECTION
                    // =================================================

                    Map<String, Object> userUpdate =
                            new HashMap<>();

                    userUpdate.put("name", name);
                    userUpdate.put("phone", phone);
                    userUpdate.put("email", email);
                    userUpdate.put("address", address);

                    db.collection("users")
                            .document(uid)
                            .update(userUpdate)
                            .addOnSuccessListener(unused2 -> {

                                Toast.makeText(
                                        requireContext(),
                                        "Profile updated successfully!",
                                        Toast.LENGTH_SHORT
                                ).show();

                                goBackToProfile();

                            })
                            .addOnFailureListener(e -> {

                                Toast.makeText(
                                        requireContext(),
                                        "Seller profile updated, but user data update failed: "
                                                + e.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show();

                                goBackToProfile();
                            });

                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            requireContext(),
                            "Failed to update profile: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    // =========================================================
    // GO BACK TO SELLER PROFILE
    // =========================================================

    private void goBackToProfile() {

        if (!isAdded()) {
            return;
        }

        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.fragment_container,
                        new SellerProfileFragment()
                )
                .commit();
    }
}