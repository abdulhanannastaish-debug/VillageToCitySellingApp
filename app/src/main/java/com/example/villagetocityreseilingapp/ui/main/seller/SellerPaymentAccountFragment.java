package com.example.villagetocityreseilingapp.ui.main.seller;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
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

public class SellerPaymentAccountFragment extends Fragment {

    // =========================================================
    // VIEWS
    // =========================================================

    private TextView txtPaymentAccountTitle;
    private RadioGroup paymentMethodGroup;

    private RadioButton radioBank;
    private RadioButton radioEasypaisa;
    private RadioButton radioJazzCash;

    private EditText edtAccountName;
    private EditText edtAccountNumber;
    private EditText edtBankName;

    private TextView txtBankNameLabel;

    private AppCompatButton btnSavePaymentAccount;

    // =========================================================
    // FIREBASE
    // =========================================================

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public SellerPaymentAccountFragment() {
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
                R.layout.fragment_seller_payment_account,
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

        // =====================================================
        // FIREBASE
        // =====================================================

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // =====================================================
        // FIND VIEWS
        // =====================================================

        txtPaymentAccountTitle =
                view.findViewById(R.id.txtPaymentAccountTitle);

        paymentMethodGroup =
                view.findViewById(R.id.paymentMethodGroup);

        radioBank =
                view.findViewById(R.id.radioBank);

        radioEasypaisa =
                view.findViewById(R.id.radioEasypaisa);

        radioJazzCash =
                view.findViewById(R.id.radioJazzCash);

        edtAccountName =
                view.findViewById(R.id.edtAccountName);

        edtAccountNumber =
                view.findViewById(R.id.edtAccountNumber);

        edtBankName =
                view.findViewById(R.id.edtBankName);

        txtBankNameLabel =
                view.findViewById(R.id.txtBankNameLabel);

        btnSavePaymentAccount =
                view.findViewById(R.id.btnSavePaymentAccount);

        // =====================================================
        // PAYMENT METHOD CHANGE
        // =====================================================

        paymentMethodGroup.setOnCheckedChangeListener(
                (group, checkedId) -> {

                    if (checkedId == R.id.radioBank) {

                        txtBankNameLabel.setVisibility(View.VISIBLE);
                        edtBankName.setVisibility(View.VISIBLE);

                    } else {

                        txtBankNameLabel.setVisibility(View.GONE);
                        edtBankName.setVisibility(View.GONE);
                        edtBankName.setText("");
                    }
                }
        );

        // =====================================================
        // SAVE BUTTON
        // =====================================================

        btnSavePaymentAccount.setOnClickListener(v -> {

            savePaymentAccount();

        });

        // =====================================================
        // LOAD SAVED ACCOUNT
        // =====================================================

        loadPaymentAccount();
    }

    // =========================================================
    // LOAD PAYMENT ACCOUNT
    // =========================================================

    private void loadPaymentAccount() {

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
                .collection("paymentAccount")
                .document("account")
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (!isAdded()) {
                        return;
                    }

                    // =================================================
                    // NO SAVED ACCOUNT
                    // =================================================

                    if (!documentSnapshot.exists()) {

                        radioEasypaisa.setChecked(true);

                        txtBankNameLabel.setVisibility(
                                View.GONE
                        );

                        edtBankName.setVisibility(
                                View.GONE
                        );

                        return;
                    }

                    // =================================================
                    // GET DATA
                    // =================================================

                    String method =
                            documentSnapshot.getString("paymentMethod");

                    String accountName =
                            documentSnapshot.getString("accountName");

                    String accountNumber =
                            documentSnapshot.getString("accountNumber");

                    String bankName =
                            documentSnapshot.getString("bankName");

                    // =================================================
                    // ACCOUNT NAME
                    // =================================================

                    if (accountName != null) {

                        edtAccountName.setText(
                                accountName
                        );
                    }

                    // =================================================
                    // ACCOUNT NUMBER
                    // =================================================

                    if (accountNumber != null) {

                        edtAccountNumber.setText(
                                accountNumber
                        );
                    }

                    // =================================================
                    // PAYMENT METHOD
                    // =================================================

                    if ("Bank Account".equalsIgnoreCase(method)) {

                        radioBank.setChecked(true);

                        txtBankNameLabel.setVisibility(
                                View.VISIBLE
                        );

                        edtBankName.setVisibility(
                                View.VISIBLE
                        );

                        if (bankName != null) {

                            edtBankName.setText(
                                    bankName
                            );
                        }

                    } else if ("JazzCash".equalsIgnoreCase(method)) {

                        radioJazzCash.setChecked(true);

                        txtBankNameLabel.setVisibility(
                                View.GONE
                        );

                        edtBankName.setVisibility(
                                View.GONE
                        );

                    } else {

                        radioEasypaisa.setChecked(true);

                        txtBankNameLabel.setVisibility(
                                View.GONE
                        );

                        edtBankName.setVisibility(
                                View.GONE
                        );
                    }

                })
                .addOnFailureListener(e -> {

                    if (!isAdded()) {
                        return;
                    }

                    Toast.makeText(
                            requireContext(),
                            "Failed to load payment account: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    // =========================================================
    // SAVE PAYMENT ACCOUNT
    // =========================================================

    private void savePaymentAccount() {

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

        // =====================================================
        // GET VALUES
        // =====================================================

        String accountName =
                edtAccountName.getText()
                        .toString()
                        .trim();

        String accountNumber =
                edtAccountNumber.getText()
                        .toString()
                        .trim();

        String bankName =
                edtBankName.getText()
                        .toString()
                        .trim();

        // =====================================================
        // PAYMENT METHOD
        // =====================================================

        String paymentMethod;

        int selectedId =
                paymentMethodGroup.getCheckedRadioButtonId();

        if (selectedId == -1) {

            Toast.makeText(
                    requireContext(),
                    "Please select a payment method.",
                    Toast.LENGTH_SHORT
            ).show();

            return;

        } else if (selectedId == R.id.radioBank) {

            paymentMethod = "Bank Account";

        } else if (selectedId == R.id.radioJazzCash) {

            paymentMethod = "JazzCash";

        } else {

            paymentMethod = "Easypaisa";
        }

        // =====================================================
        // VALIDATION
        // =====================================================

        if (TextUtils.isEmpty(accountName)) {

            edtAccountName.setError(
                    "Enter account holder name"
            );

            edtAccountName.requestFocus();

            return;
        }

        if (TextUtils.isEmpty(accountNumber)) {

            edtAccountNumber.setError(
                    "Enter account or mobile number"
            );

            edtAccountNumber.requestFocus();

            return;
        }

        if (paymentMethod.equals("Bank Account") &&
                TextUtils.isEmpty(bankName)) {

            edtBankName.setError(
                    "Enter bank name"
            );

            edtBankName.requestFocus();

            return;
        }

        // =====================================================
        // DISABLE BUTTON
        // =====================================================

        btnSavePaymentAccount.setEnabled(false);

        btnSavePaymentAccount.setText(
                "Saving..."
        );

        // =====================================================
        // CREATE DATA
        // =====================================================

        Map<String, Object> paymentData =
                new HashMap<>();

        paymentData.put(
                "paymentMethod",
                paymentMethod
        );

        paymentData.put(
                "accountName",
                accountName
        );

        paymentData.put(
                "accountNumber",
                accountNumber
        );

        paymentData.put(
                "bankName",
                bankName
        );

        paymentData.put(
                "status",
                "active"
        );

        paymentData.put(
                "updatedAt",
                com.google.firebase.firestore.FieldValue.serverTimestamp()
        );

        // =====================================================
        // SAVE TO FIRESTORE
        // =====================================================

        String uid =
                currentUser.getUid();

        db.collection("sellers")
                .document(uid)
                .collection("paymentAccount")
                .document("account")
                .set(paymentData)
                .addOnSuccessListener(unused -> {

                    if (!isAdded()) {
                        return;
                    }

                    Toast.makeText(
                            requireContext(),
                            "Payment account saved successfully.",
                            Toast.LENGTH_SHORT
                    ).show();

                    btnSavePaymentAccount.setEnabled(
                            true
                    );

                    btnSavePaymentAccount.setText(
                            "Update Payment Account"
                    );

                    // =================================================
                    // GO BACK TO WALLET
                    // =================================================

                    requireActivity()
                            .getSupportFragmentManager()
                            .popBackStack();

                })
                .addOnFailureListener(e -> {

                    if (!isAdded()) {
                        return;
                    }

                    btnSavePaymentAccount.setEnabled(
                            true
                    );

                    btnSavePaymentAccount.setText(
                            "Save Payment Account"
                    );

                    Toast.makeText(
                            requireContext(),
                            "Failed to save account: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }
}