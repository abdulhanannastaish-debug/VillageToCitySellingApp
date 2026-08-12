package com.example.villagetocityreseilingapp.ui.main.seller;

import android.content.Intent;
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
import com.example.villagetocityreseilingapp.activity.seller_MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.HashMap;
import java.util.Map;

public class SellerVerificationFragment extends Fragment {

    private EditText etFullName;
    private EditText etPhone;
    private EditText etEmail;
    private EditText etAddress;
    private EditText etCnic;

    private AppCompatButton btnUploadCnicFront;
    private AppCompatButton btnUploadCnicBack;
    private AppCompatButton btnSubmit;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private ListenerRegistration sellerStatusListener;

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_seller_verification,
                container,
                false
        );

        // ================= FIREBASE =================

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();


        // ================= XML IDs =================

        etFullName = view.findViewById(R.id.etFullName);
        etPhone = view.findViewById(R.id.etPhone);
        etEmail = view.findViewById(R.id.etEmail);
        etAddress = view.findViewById(R.id.etAddress);
        etCnic = view.findViewById(R.id.etCnic);

        btnUploadCnicFront =
                view.findViewById(R.id.btnUploadCnicFront);

        btnUploadCnicBack =
                view.findViewById(R.id.btnUploadCnicBack);

        btnSubmit =
                view.findViewById(R.id.btnSubmit);


        // ================= CNIC FRONT =================

        btnUploadCnicFront.setOnClickListener(v -> {

            Toast.makeText(
                    getContext(),
                    "CNIC Front image baad mein add karenge",
                    Toast.LENGTH_SHORT
            ).show();

        });


        // ================= CNIC BACK =================

        btnUploadCnicBack.setOnClickListener(v -> {

            Toast.makeText(
                    getContext(),
                    "CNIC Back image baad mein add karenge",
                    Toast.LENGTH_SHORT
            ).show();

        });


        // ================= SUBMIT =================

        btnSubmit.setOnClickListener(v ->
                submitSellerVerification()
        );


        // ================= CHECK ADMIN STATUS =================

        listenForSellerVerification();


        return view;
    }


    // =========================================================
    // SUBMIT SELLER VERIFICATION
    // =========================================================

    private void submitSellerVerification() {

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

            etCnic.setError("CNIC must contain 13 digits");
            etCnic.requestFocus();
            return;
        }


        // ================= CURRENT USER =================

        FirebaseUser currentUser =
                auth.getCurrentUser();

        if (currentUser == null) {

            Toast.makeText(
                    getContext(),
                    "User is not logged in. Please login again.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }


        String uid = currentUser.getUid();


        // ================= SELLER DATA =================

        Map<String, Object> sellerData =
                new HashMap<>();

        sellerData.put("uid", uid);
        sellerData.put("name", name);
        sellerData.put("phone", phone);
        sellerData.put("email", email);
        sellerData.put("address", address);
        sellerData.put("cnic", cnic);
        sellerData.put("status", "pending");

        sellerData.put(
                "createdAt",
                com.google.firebase.firestore.FieldValue.serverTimestamp()
        );


        // ================= SAVE SELLER =================

        db.collection("sellers")
                .document(uid)
                .set(sellerData)
                .addOnSuccessListener(unused -> {

                    // ================= UPDATE USERS =================

                    Map<String, Object> userUpdate =
                            new HashMap<>();

                    userUpdate.put("name", name);
                    userUpdate.put("email", email);
                    userUpdate.put("phone", phone);
                    userUpdate.put("address", address);
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


    // =========================================================
    // LISTEN FOR ADMIN VERIFICATION
    // =========================================================

    private void listenForSellerVerification() {

        FirebaseUser currentUser =
                auth.getCurrentUser();

        if (currentUser == null) {
            return;
        }


        String uid = currentUser.getUid();


        sellerStatusListener =
                db.collection("sellers")
                        .document(uid)
                        .addSnapshotListener((snapshot, error) -> {

                            if (error != null) {

                                Toast.makeText(
                                        getContext(),
                                        "Unable to check verification status",
                                        Toast.LENGTH_SHORT
                                ).show();

                                return;
                            }


                            if (snapshot == null ||
                                    !snapshot.exists()) {

                                return;
                            }


                            String status =
                                    snapshot.getString("status");


                            if (status == null) {
                                return;
                            }


                            // ================= VERIFIED =================

                            if (status.equals("verified")) {

                                Toast.makeText(
                                        getContext(),
                                        "Your account has been verified!",
                                        Toast.LENGTH_LONG
                                ).show();


                                openSellerMainActivity();
                            }


                            // ================= REJECTED =================

                            else if (status.equals("rejected")) {

                                Toast.makeText(
                                        getContext(),
                                        "Your seller verification was rejected.",
                                        Toast.LENGTH_LONG
                                ).show();

                            }

                        });
    }


    // =========================================================
    // OPEN SELLER MAIN ACTIVITY
    // =========================================================

    private void openSellerMainActivity() {

        if (!isAdded()) {
            return;
        }


        Intent intent = new Intent(
                requireActivity(),
                seller_MainActivity.class
        );


        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );


        startActivity(intent);


        requireActivity().finish();
    }


    // =========================================================
    // REMOVE FIRESTORE LISTENER
    // =========================================================

    @Override
    public void onDestroyView() {

        if (sellerStatusListener != null) {

            sellerStatusListener.remove();
            sellerStatusListener = null;
        }

        super.onDestroyView();
    }
}