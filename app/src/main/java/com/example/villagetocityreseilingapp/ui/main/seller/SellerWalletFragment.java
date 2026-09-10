package com.example.villagetocityreseilingapp.ui.main.seller;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.villagetocityreseilingapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SellerWalletFragment extends Fragment {

    private TextView txtAvailableBalance;
    private TextView txtPendingBalance;
    private TextView txtTotalEarned;
    private TextView txtTotalPaid;
    private TextView txtPaymentMethod;
    private TextView txtPaymentAccount;
    private TextView txtPaymentAccountStatus;
    private CardView paymentAccountCard;
    private TextView txtTransactionAmount1;
    private TextView txtTransactionAmount2;
    private TextView txtTransactionAmount3;
    private TextView txtOrderId1;
    private TextView txtOrderId2;
    private TextView txtOrderId3;
    private TextView txtOrderDate1;
    private TextView txtOrderDate2;
    private TextView txtOrderDate3;
    private TextView txtTransactionStatus1;
    private TextView txtTransactionStatus2;
    private TextView txtTransactionStatus3;
    private TextView txtViewAllTransactions;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    public SellerWalletFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_seller_wallet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        txtAvailableBalance = view.findViewById(R.id.txtAvailableBalance);
        txtPendingBalance = view.findViewById(R.id.txtPendingBalance);
        txtTotalEarned = view.findViewById(R.id.txtTotalEarned);
        txtTotalPaid = view.findViewById(R.id.txtTotalPaid);
        txtPaymentMethod = view.findViewById(R.id.txtPaymentMethod);
        txtPaymentAccount = view.findViewById(R.id.txtPaymentAccount);
        txtPaymentAccountStatus = view.findViewById(R.id.txtPaymentAccountStatus);
        paymentAccountCard = view.findViewById(R.id.paymentAccountCard);
        txtTransactionAmount1 = view.findViewById(R.id.txtTransactionAmount1);
        txtTransactionAmount2 = view.findViewById(R.id.txtTransactionAmount2);
        txtTransactionAmount3 = view.findViewById(R.id.txtTransactionAmount3);
        txtOrderId1 = view.findViewById(R.id.txtOrderId1);
        txtOrderId2 = view.findViewById(R.id.txtOrderId2);
        txtOrderId3 = view.findViewById(R.id.txtOrderId3);
        txtOrderDate1 = view.findViewById(R.id.txtOrderDate1);
        txtOrderDate2 = view.findViewById(R.id.txtOrderDate2);
        txtOrderDate3 = view.findViewById(R.id.txtOrderDate3);
        txtTransactionStatus1 = view.findViewById(R.id.txtTransactionStatus1);
        txtTransactionStatus2 = view.findViewById(R.id.txtTransactionStatus2);
        txtTransactionStatus3 = view.findViewById(R.id.txtTransactionStatus3);
        txtViewAllTransactions = view.findViewById(R.id.txtViewAllTransactions);

        setInitialValues();

        if (paymentAccountCard != null) {
            paymentAccountCard.setOnClickListener(v -> {
                if (!isAdded()) return;
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .setReorderingAllowed(true)
                        .replace(R.id.fragment_container, new SellerPaymentAccountFragment())
                        .addToBackStack(null)
                        .commit();
            });
        }

        if (txtViewAllTransactions != null) {
            txtViewAllTransactions.setOnClickListener(v -> {
                if (!isAdded()) return;
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .setReorderingAllowed(true)
                        .replace(R.id.fragment_container, new SellerTransactionHistoryFragment())
                        .addToBackStack(null)
                        .commit();
            });
        }

        loadWalletData();
        loadPaymentAccount();
        loadRecentTransactions();
    }

    // =========================================================
    // INITIAL VALUES — NULL SAFE
    // =========================================================

    private void setInitialValues() {
        if (txtAvailableBalance != null) txtAvailableBalance.setText("Rs. 0");
        if (txtPendingBalance != null) txtPendingBalance.setText("Pending: Rs. 0");
        if (txtTotalEarned != null) txtTotalEarned.setText("Rs. 0");
        if (txtTotalPaid != null) txtTotalPaid.setText("Rs. 0");
        if (txtPaymentMethod != null) txtPaymentMethod.setText("Not Added");
        if (txtPaymentAccount != null) txtPaymentAccount.setText("No payment account");
        if (txtPaymentAccountStatus != null) txtPaymentAccountStatus.setText("Tap to add payment account");
        if (txtTransactionAmount1 != null) txtTransactionAmount1.setText("-");
        if (txtOrderId1 != null) txtOrderId1.setText("No transaction");
        if (txtOrderDate1 != null) txtOrderDate1.setText("-");
        if (txtTransactionStatus1 != null) txtTransactionStatus1.setText("-");
        if (txtTransactionAmount2 != null) txtTransactionAmount2.setText("-");
        if (txtOrderId2 != null) txtOrderId2.setText("No transaction");
        if (txtOrderDate2 != null) txtOrderDate2.setText("-");
        if (txtTransactionStatus2 != null) txtTransactionStatus2.setText("-");
        if (txtTransactionAmount3 != null) txtTransactionAmount3.setText("-");
        if (txtOrderId3 != null) txtOrderId3.setText("No transaction");
        if (txtOrderDate3 != null) txtOrderDate3.setText("-");
        if (txtTransactionStatus3 != null) txtTransactionStatus3.setText("-");
    }

    // =========================================================
    // LOAD WALLET DATA
    // =========================================================

    private void loadWalletData() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) return;

        String uid = currentUser.getUid();

        db.collection("sellerWallets").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!isAdded()) return;

                    if (!documentSnapshot.exists()) {
                        if (txtAvailableBalance != null) txtAvailableBalance.setText("Rs. 0");
                        if (txtPendingBalance != null) txtPendingBalance.setText("Pending: Rs. 0");
                        if (txtTotalEarned != null) txtTotalEarned.setText("Rs. 0");
                        if (txtTotalPaid != null) txtTotalPaid.setText("Rs. 0");
                        return;
                    }

                    Number available = documentSnapshot.getLong("availableBalance");
                    Number pending = documentSnapshot.getLong("pendingBalance");
                    Number totalEarned = documentSnapshot.getLong("totalEarned");
                    Number totalPaid = documentSnapshot.getLong("totalPaid");

                    if (txtAvailableBalance != null) txtAvailableBalance.setText("Rs. " + numberValue(available));
                    if (txtPendingBalance != null) txtPendingBalance.setText("Pending: Rs. " + numberValue(pending));
                    if (txtTotalEarned != null) txtTotalEarned.setText("Rs. " + numberValue(totalEarned));
                    if (txtTotalPaid != null) txtTotalPaid.setText("Rs. " + numberValue(totalPaid));
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    Toast.makeText(requireContext(), "Failed to load wallet: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    // =========================================================
    // LOAD PAYMENT ACCOUNT
    // =========================================================

    private void loadPaymentAccount() {
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            if (txtPaymentMethod != null) txtPaymentMethod.setText("Not Added");
            if (txtPaymentAccount != null) txtPaymentAccount.setText("No payment account");
            if (txtPaymentAccountStatus != null) txtPaymentAccountStatus.setText("Please add a payment account");
            return;
        }

        String uid = currentUser.getUid();

        db.collection("sellers").document(uid)
                .collection("paymentAccount").document("account").get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!isAdded()) return;

                    if (!documentSnapshot.exists()) {
                        if (txtPaymentMethod != null) txtPaymentMethod.setText("Not Added");
                        if (txtPaymentAccount != null) txtPaymentAccount.setText("No payment account");
                        if (txtPaymentAccountStatus != null) txtPaymentAccountStatus.setText("Tap to add payment account");
                        return;
                    }

                    String paymentMethod = documentSnapshot.getString("paymentMethod");
                    String accountNumber = documentSnapshot.getString("accountNumber");
                    String status = documentSnapshot.getString("status");

                    if (txtPaymentMethod != null)
                        txtPaymentMethod.setText(paymentMethod != null && !paymentMethod.trim().isEmpty() ? paymentMethod : "Payment Account");

                    if (txtPaymentAccount != null)
                        txtPaymentAccount.setText(accountNumber != null && !accountNumber.trim().isEmpty() ? accountNumber : "Account number not available");

                    if (txtPaymentAccountStatus != null)
                        txtPaymentAccountStatus.setText("active".equalsIgnoreCase(status) ? "Registered for automatic payment" : "Payment account saved");
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    Toast.makeText(requireContext(), "Failed to load payment account: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    // =========================================================
    // LOAD RECENT TRANSACTIONS
    // =========================================================

    private void loadRecentTransactions() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) return;

        String sellerId = currentUser.getUid();

        db.collection("transactions")
                .whereEqualTo("sellerId", sellerId)
                .limit(3)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!isAdded()) return;

                    setTransactionEmpty(1);
                    setTransactionEmpty(2);
                    setTransactionEmpty(3);

                    int position = 1;

                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        if (position > 3) break;

                        String orderId = document.getString("orderId");
                        String status = document.getString("status");
                        Number amount = document.getLong("amount");
                        String date = getTransactionDate(document);

                        if (orderId == null || orderId.trim().isEmpty()) orderId = document.getId();
                        if (status == null || status.trim().isEmpty()) status = "Pending";

                        String amountText = "+ Rs. " + numberValue(amount);

                        if (position == 1) {
                            if (txtOrderId1 != null) txtOrderId1.setText("Order #" + orderId);
                            if (txtOrderDate1 != null) txtOrderDate1.setText(date);
                            if (txtTransactionAmount1 != null) txtTransactionAmount1.setText(amountText);
                            if (txtTransactionStatus1 != null) txtTransactionStatus1.setText(status);
                        } else if (position == 2) {
                            if (txtOrderId2 != null) txtOrderId2.setText("Order #" + orderId);
                            if (txtOrderDate2 != null) txtOrderDate2.setText(date);
                            if (txtTransactionAmount2 != null) txtTransactionAmount2.setText(amountText);
                            if (txtTransactionStatus2 != null) txtTransactionStatus2.setText(status);
                        } else if (position == 3) {
                            if (txtOrderId3 != null) txtOrderId3.setText("Order #" + orderId);
                            if (txtOrderDate3 != null) txtOrderDate3.setText(date);
                            if (txtTransactionAmount3 != null) txtTransactionAmount3.setText(amountText);
                            if (txtTransactionStatus3 != null) txtTransactionStatus3.setText(status);
                        }
                        position++;
                    }
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    Toast.makeText(requireContext(), "Failed to load transactions: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    // =========================================================
    // EMPTY TRANSACTION — NULL SAFE
    // =========================================================

    private void setTransactionEmpty(int position) {
        if (position == 1) {
            if (txtTransactionAmount1 != null) txtTransactionAmount1.setText("-");
            if (txtOrderId1 != null) txtOrderId1.setText("No transaction");
            if (txtOrderDate1 != null) txtOrderDate1.setText("-");
            if (txtTransactionStatus1 != null) txtTransactionStatus1.setText("-");
        } else if (position == 2) {
            if (txtTransactionAmount2 != null) txtTransactionAmount2.setText("-");
            if (txtOrderId2 != null) txtOrderId2.setText("No transaction");
            if (txtOrderDate2 != null) txtOrderDate2.setText("-");
            if (txtTransactionStatus2 != null) txtTransactionStatus2.setText("-");
        } else if (position == 3) {
            if (txtTransactionAmount3 != null) txtTransactionAmount3.setText("-");
            if (txtOrderId3 != null) txtOrderId3.setText("No transaction");
            if (txtOrderDate3 != null) txtOrderDate3.setText("-");
            if (txtTransactionStatus3 != null) txtTransactionStatus3.setText("-");
        }
    }

    private String getTransactionDate(DocumentSnapshot document) {
        Timestamp timestamp = document.getTimestamp("createdAt");
        if (timestamp == null) return "Date not available";
        Date date = timestamp.toDate();
        SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        return formatter.format(date);
    }

    private long numberValue(Number number) {
        if (number == null) return 0;
        return number.longValue();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (auth != null) {
            loadWalletData();
            loadPaymentAccount();
            loadRecentTransactions();
        }
    }
}