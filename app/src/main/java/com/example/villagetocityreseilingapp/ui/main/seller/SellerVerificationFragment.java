package com.example.villagetocityreseilingapp.ui.main.seller;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.villagetocityreseilingapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.HashMap;
import java.util.Map;

public class SellerVerificationFragment extends Fragment {

    // =====================================================
    // FORM FIELDS
    // =====================================================

    private EditText etFullName;
    private EditText etPhone;
    private EditText etEmail;
    private EditText etAddress;
    private EditText etCnic;

    // =====================================================
    // BUTTONS
    // =====================================================

    private AppCompatButton btnUploadCnicFront;
    private AppCompatButton btnUploadCnicBack;
    private AppCompatButton btnSubmit;

    // =====================================================
    // STATUS UI
    // =====================================================

    private TextView txtVerificationStatus;
    private TextView txtVerificationMessage;

    // =====================================================
    // FIREBASE
    // =====================================================

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    // =====================================================
    // LISTENER
    // =====================================================

    private ListenerRegistration sellerStatusListener;

    // =====================================================
    // CURRENT STATUS
    // =====================================================

    private String currentStatus = "pending";

    // =====================================================
    // CREATE VIEW
    // =====================================================

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

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etFullName = view.findViewById(R.id.etFullName);
        etPhone = view.findViewById(R.id.etPhone);
        etEmail = view.findViewById(R.id.etEmail);
        etAddress = view.findViewById(R.id.etAddress);
        etCnic = view.findViewById(R.id.etCnic);

        btnUploadCnicFront = view.findViewById(R.id.btnUploadCnicFront);
        btnUploadCnicBack = view.findViewById(R.id.btnUploadCnicBack);
        btnSubmit = view.findViewById(R.id.btnSubmit);

        txtVerificationStatus = view.findViewById(R.id.txtVerificationStatus);
        txtVerificationMessage = view.findViewById(R.id.txtVerificationMessage);

        showLoadingStatus();

        btnUploadCnicFront.setOnClickListener(v ->
                Toast.makeText(getContext(),
                        "CNIC Front image baad mein add karenge",
                        Toast.LENGTH_SHORT).show()
        );

        btnUploadCnicBack.setOnClickListener(v ->
                Toast.makeText(getContext(),
                        "CNIC Back image baad mein add karenge",
                        Toast.LENGTH_SHORT).show()
        );

        btnSubmit.setOnClickListener(v ->
                submitSellerVerification()
        );

        loadSellerVerification();

        return view;
    }

    // =====================================================
    // LOAD SELLER VERIFICATION
    // =====================================================

    private void loadSellerVerification() {

        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(getContext(),
                    "Please login again.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        String uid = currentUser.getUid();

        db.collection("sellers")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (!isAdded()) return;

                    if (!documentSnapshot.exists()) {
                        currentStatus = "new";
                        enableFormForNewSeller();
                        return;
                    }

                    loadExistingData(documentSnapshot);

                    String status = documentSnapshot.getString("status");
                    if (status == null) status = "pending";

                    currentStatus = status.trim().toLowerCase();
                    applyStatusUI(currentStatus);
                })
                .addOnFailureListener(e -> {

                    if (!isAdded()) return;

                    Toast.makeText(getContext(),
                            "Failed to load verification data: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();

                    enableFormForNewSeller();
                });
    }

    // =====================================================
    // LOAD EXISTING DATA
    // =====================================================

    private void loadExistingData(DocumentSnapshot snapshot) {

        String name = snapshot.getString("name");
        String phone = snapshot.getString("phone");
        String email = snapshot.getString("email");
        String address = snapshot.getString("address");
        String cnic = snapshot.getString("cnic");

        if (name != null) etFullName.setText(name);
        if (phone != null) etPhone.setText(phone);
        if (email != null) etEmail.setText(email);
        if (address != null) etAddress.setText(address);
        if (cnic != null) etCnic.setText(cnic);
    }

    // =====================================================
    // APPLY STATUS UI
    // =====================================================

    private void applyStatusUI(String status) {

        // =================================================
        // VERIFIED
        // =================================================

        if ("verified".equals(status)) {

            txtVerificationStatus.setVisibility(View.VISIBLE);
            txtVerificationStatus.setText("VERIFIED");
            txtVerificationStatus.setTextColor(Color.parseColor("#2E7D32"));

            txtVerificationMessage.setVisibility(View.VISIBLE);
            txtVerificationMessage.setText("Your seller account has been verified.");
            txtVerificationMessage.setTextColor(Color.parseColor("#2E7D32"));

            disableForm();

            btnSubmit.setText("Verified");
            btnSubmit.setEnabled(false);

            // =============================================
            // CHOOSE LANGUAGE SCREEN PAR JAYE
            // =============================================

            openChooseLanguage();

            return;
        }

        // =================================================
        // PENDING
        // =================================================

        if ("pending".equals(status)) {

            txtVerificationStatus.setVisibility(View.VISIBLE);
            txtVerificationStatus.setText("PENDING");
            txtVerificationStatus.setTextColor(Color.parseColor("#F9A825"));

            txtVerificationMessage.setVisibility(View.VISIBLE);
            txtVerificationMessage.setText("Your verification request is pending admin approval.");
            txtVerificationMessage.setTextColor(Color.parseColor("#F9A825"));

            disableForm();

            btnSubmit.setText("PENDING");
            btnSubmit.setTextColor(Color.WHITE);
            btnSubmit.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                            Color.parseColor("#F9A825")));
            btnSubmit.setEnabled(false);

            return;
        }

        // =================================================
        // REJECTED
        // =================================================

        if ("rejected".equals(status)) {

            txtVerificationStatus.setVisibility(View.VISIBLE);
            txtVerificationStatus.setText("REJECTED");
            txtVerificationStatus.setTextColor(Color.parseColor("#D32F2F"));

            txtVerificationMessage.setVisibility(View.VISIBLE);
            txtVerificationMessage.setText("Your verification request was rejected. Resubmit the request.");
            txtVerificationMessage.setTextColor(Color.parseColor("#D32F2F"));

            enableForm();

            btnSubmit.setText("Resubmit the Request");
            btnSubmit.setTextColor(Color.WHITE);
            btnSubmit.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                            Color.parseColor("#6AC32F")));
            btnSubmit.setEnabled(true);

            return;
        }

        // =================================================
        // NEW
        // =================================================

        enableFormForNewSeller();
    }

    // =====================================================
    // NEW SELLER
    // =====================================================

    private void enableFormForNewSeller() {

        txtVerificationStatus.setVisibility(View.GONE);
        txtVerificationMessage.setVisibility(View.GONE);

        enableForm();

        btnSubmit.setText("Register as Seller");
        btnSubmit.setTextColor(Color.WHITE);
        btnSubmit.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        Color.parseColor("#6AC32F")));
        btnSubmit.setEnabled(true);
    }

    // =====================================================
    // ENABLE FORM
    // =====================================================

    private void enableForm() {
        etFullName.setEnabled(true);
        etPhone.setEnabled(true);
        etEmail.setEnabled(true);
        etAddress.setEnabled(true);
        etCnic.setEnabled(true);
        btnUploadCnicFront.setEnabled(true);
        btnUploadCnicBack.setEnabled(true);
    }

    // =====================================================
    // DISABLE FORM
    // =====================================================

    private void disableForm() {
        etFullName.setEnabled(false);
        etPhone.setEnabled(false);
        etEmail.setEnabled(false);
        etAddress.setEnabled(false);
        etCnic.setEnabled(false);
        btnUploadCnicFront.setEnabled(false);
        btnUploadCnicBack.setEnabled(false);
    }

    // =====================================================
    // LOADING STATUS
    // =====================================================

    private void showLoadingStatus() {

        txtVerificationStatus.setVisibility(View.VISIBLE);
        txtVerificationStatus.setText("Checking verification status...");
        txtVerificationStatus.setTextColor(Color.DKGRAY);

        txtVerificationMessage.setVisibility(View.VISIBLE);
        txtVerificationMessage.setText("Please wait...");

        btnSubmit.setEnabled(false);
    }

    // =====================================================
    // SUBMIT VERIFICATION
    // =====================================================

    private void submitSellerVerification() {

        String name = etFullName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String cnic = etCnic.getText().toString().trim();

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

        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(getContext(),
                    "User is not logged in. Please login again.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        String uid = currentUser.getUid();

        btnSubmit.setEnabled(false);
        btnSubmit.setText("Submitting...");

        Map<String, Object> sellerData = new HashMap<>();
        sellerData.put("uid", uid);
        sellerData.put("name", name);
        sellerData.put("phone", phone);
        sellerData.put("email", email);
        sellerData.put("address", address);
        sellerData.put("cnic", cnic);
        sellerData.put("status", "pending");
        sellerData.put("submittedAt", FieldValue.serverTimestamp());

        db.collection("sellers")
                .document(uid)
                .set(sellerData,
                        com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener(unused -> {

                    Map<String, Object> userUpdate = new HashMap<>();
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

                                if (!isAdded()) return;

                                currentStatus = "pending";
                                applyStatusUI("pending");

                                Toast.makeText(getContext(),
                                        "Verification request submitted successfully!",
                                        Toast.LENGTH_LONG).show();
                            })
                            .addOnFailureListener(e -> {

                                if (!isAdded()) return;

                                currentStatus = "pending";
                                applyStatusUI("pending");

                                Toast.makeText(getContext(),
                                        "Verification submitted, but user status update failed: "
                                                + e.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(e -> {

                    if (!isAdded()) return;

                    btnSubmit.setEnabled(true);

                    if ("rejected".equals(currentStatus)) {
                        btnSubmit.setText("Resubmit the Request");
                    } else {
                        btnSubmit.setText("Register as Seller");
                    }

                    Toast.makeText(getContext(),
                            "Failed to submit verification: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    // =====================================================
    // LISTEN FOR ADMIN STATUS
    // =====================================================

    private void listenForSellerVerification() {

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) return;

        String uid = currentUser.getUid();

        sellerStatusListener = db.collection("sellers")
                .document(uid)
                .addSnapshotListener((snapshot, error) -> {

                    if (!isAdded()) return;

                    if (error != null) {
                        Toast.makeText(getContext(),
                                "Unable to check verification status",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (snapshot == null || !snapshot.exists()) return;

                    String status = snapshot.getString("status");
                    if (status == null) return;

                    status = status.trim().toLowerCase();

                    if ("verified".equals(status)) {
                        currentStatus = "verified";
                        openChooseLanguage();

                    } else if ("rejected".equals(status)) {
                        currentStatus = "rejected";
                        loadExistingData(snapshot);
                        applyStatusUI("rejected");

                    } else if ("pending".equals(status)) {
                        currentStatus = "pending";
                        loadExistingData(snapshot);
                        applyStatusUI("pending");
                    }
                });
    }

    // =====================================================
    // OPEN CHOOSE LANGUAGE
    // =====================================================

    private void openChooseLanguage() {

        if (!isAdded()) return;

        View view = getView();
        if (view == null) return;

        Navigation.findNavController(view)
                .navigate(
                        R.id.action_verification_to_choose_language
                );
    }

    // =====================================================
    // ON START
    // =====================================================

    @Override
    public void onStart() {
        super.onStart();
        if (sellerStatusListener == null) {
            listenForSellerVerification();
        }
    }

    // =====================================================
    // DESTROY VIEW
    // =====================================================

    @Override
    public void onDestroyView() {
        if (sellerStatusListener != null) {
            sellerStatusListener.remove();
            sellerStatusListener = null;
        }
        super.onDestroyView();
    }
}