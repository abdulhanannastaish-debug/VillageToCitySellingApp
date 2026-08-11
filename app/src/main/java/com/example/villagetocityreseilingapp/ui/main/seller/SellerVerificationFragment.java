package com.example.villagetocityreseilingapp.ui.main.seller;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.example.villagetocityreseilingapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SellerVerificationFragment extends Fragment {

    private EditText etFullName;
    private EditText etPhone;
    private EditText etEmail;
    private EditText etCnic;

    private AppCompatButton btnUploadCnicFront;
    private AppCompatButton btnUploadCnicBack;
    private AppCompatButton btnSubmit;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_seller_verification,
                container,
                false
        );

        // Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // XML IDs
        etFullName = view.findViewById(R.id.etFullName);
        etPhone = view.findViewById(R.id.etPhone);
        etEmail = view.findViewById(R.id.etEmail);
        etCnic = view.findViewById(R.id.etCnic);

        btnUploadCnicFront = view.findViewById(R.id.btnUploadCnicFront);
        btnUploadCnicBack = view.findViewById(R.id.btnUploadCnicBack);
        btnSubmit = view.findViewById(R.id.btnSubmit);

        // CNIC images abhi implement nahi kar rahe
        btnUploadCnicFront.setOnClickListener(v -> {
            Toast.makeText(
                    getContext(),
                    "CNIC Front image baad mein add karenge",
                    Toast.LENGTH_SHORT
            ).show();
        });

        btnUploadCnicBack.setOnClickListener(v -> {
            Toast.makeText(
                    getContext(),
                    "CNIC Back image baad mein add karenge",
                    Toast.LENGTH_SHORT
            ).show();
        });

        // Submit Seller Verification
        btnSubmit.setOnClickListener(v -> submitSellerVerification());

        return view;
    }

    private void submitSellerVerification() {

        String name = etFullName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String cnic = etCnic.getText().toString().trim();

        // Validation
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

        if (TextUtils.isEmpty(cnic)) {
            etCnic.setError("Enter your CNIC");
            etCnic.requestFocus();
            return;
        }

        if (cnic.length() != 13) {
            etCnic.setError("CNIC must contain 13 digits");
            etCnic.requestFocus();
            return;
        }

        // Check logged-in Firebase user
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(
                    getContext(),
                    "User is not logged in. Please login again.",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        String uid = currentUser.getUid();

        // Seller data
        Map<String, Object> sellerData = new HashMap<>();

        sellerData.put("uid", uid);
        sellerData.put("name", name);
        sellerData.put("phone", phone);
        sellerData.put("email", email);
        sellerData.put("cnic", cnic);
        sellerData.put("status", "pending");
        sellerData.put("createdAt",
                com.google.firebase.firestore.FieldValue.serverTimestamp());

        // Save in sellers collection
        db.collection("sellers")
                .document(uid)
                .set(sellerData)
                .addOnSuccessListener(unused -> {

                    // Update user status
                    Map<String, Object> userUpdate = new HashMap<>();
                    userUpdate.put("name", name);
                    userUpdate.put("email", email);
                    userUpdate.put("phone", phone);
                    userUpdate.put("role", "seller");
                    userUpdate.put("status", "pending");

                    db.collection("users")
                            .document(uid)
                            .update(userUpdate)
                            .addOnSuccessListener(unused2 -> {

                                Toast.makeText(
                                        getContext(),
                                        "Seller verification request submitted successfully!",
                                        Toast.LENGTH_LONG
                                ).show();

                            })
                            .addOnFailureListener(e -> {

                                Toast.makeText(
                                        getContext(),
                                        "Seller saved, but user status update failed: "
                                                + e.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show();

                            });

                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            getContext(),
                            "Failed to submit verification: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();

                });
    }
}