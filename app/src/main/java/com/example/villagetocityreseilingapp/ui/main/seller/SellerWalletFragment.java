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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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

        loadPaymentAccount();
        loadWalletAndTransactionsFromOrders();
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
        setTransactionEmpty(1);
        setTransactionEmpty(2);
        setTransactionEmpty(3);
    }

    // =========================================================
    // LOAD WALLET SUMMARY + RECENT TRANSACTIONS
    //
    // Instead of reading from separate "sellerWallets" /
    // "transactions" collections (which nothing writes to),
    // this reads directly from the "orders" collection, where
    // commissionAmount / sellerPayoutAmount / paymentStatus /
    // sellerPaymentStatus are already saved at checkout time.
    //
    // Balance logic:
    //   - totalEarned   = sum of sellerPayoutAmount for ALL orders
    //                     that have commission data
    //   - totalPaid     = sum of sellerPayoutAmount where
    //                     sellerPaymentStatus == "paid"
    //   - availableBalance = sum of sellerPayoutAmount where
    //                     paymentStatus == "received" AND
    //                     sellerPaymentStatus != "paid"
    //                     (admin has the cash, payout not sent yet)
    //   - pendingBalance = sum of sellerPayoutAmount where
    //                     paymentStatus == "pending"
    //                     (still waiting for COD to be collected)
    // =========================================================

    private void loadWalletAndTransactionsFromOrders() {

        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            return;
        }

        String sellerId = currentUser.getUid();

        db.collection("orders")
                .whereEqualTo("sellerId", sellerId)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    if (!isAdded()) {
                        return;
                    }

                    double totalEarned = 0;
                    double totalPaid = 0;
                    double availableBalance = 0;
                    double pendingBalance = 0;

                    List<QueryDocumentSnapshot> allOrders =
                            new ArrayList<>();

                    for (QueryDocumentSnapshot document : querySnapshot) {

                        allOrders.add(document);

                        Object payoutObj =
                                document.get("sellerPayoutAmount");

                        if (payoutObj == null) {
                            // Older order placed before commission
                            // tracking was added — skip it.
                            continue;
                        }

                        double payout =
                                toDouble(payoutObj);

                        String paymentStatus =
                                safeLowerCase(
                                        document.getString("paymentStatus")
                                );

                        String sellerPaymentStatus =
                                safeLowerCase(
                                        document.getString("sellerPaymentStatus")
                                );

                        totalEarned += payout;

                        if (sellerPaymentStatus.equals("paid")) {

                            totalPaid += payout;

                        } else if (paymentStatus.equals("received")) {

                            availableBalance += payout;

                        } else {

                            pendingBalance += payout;
                        }
                    }

                    if (txtAvailableBalance != null)
                        txtAvailableBalance.setText("Rs. " + formatAmount(availableBalance));

                    if (txtPendingBalance != null)
                        txtPendingBalance.setText("Pending: Rs. " + formatAmount(pendingBalance));

                    if (txtTotalEarned != null)
                        txtTotalEarned.setText("Rs. " + formatAmount(totalEarned));

                    if (txtTotalPaid != null)
                        txtTotalPaid.setText("Rs. " + formatAmount(totalPaid));

                    displayRecentTransactions(allOrders);
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    Toast.makeText(requireContext(), "Failed to load wallet: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    // =========================================================
    // DISPLAY RECENT TRANSACTIONS (TOP 3, NEWEST FIRST)
    // =========================================================

    private void displayRecentTransactions(
            List<QueryDocumentSnapshot> allOrders) {

        if (!isAdded()) {
            return;
        }

        setTransactionEmpty(1);
        setTransactionEmpty(2);
        setTransactionEmpty(3);

        if (allOrders == null || allOrders.isEmpty()) {
            return;
        }

        allOrders.sort((a, b) ->
                Long.compare(
                        getOrderTimeValue(b),
                        getOrderTimeValue(a)
                )
        );

        int position = 1;

        for (DocumentSnapshot document : allOrders) {

            if (position > 3) break;

            Object payoutObj =
                    document.get("sellerPayoutAmount");

            if (payoutObj == null) {
                // Skip orders without commission data
                continue;
            }

            String orderId =
                    document.getString("orderId");

            if (orderId == null || orderId.trim().isEmpty()) {
                orderId = document.getId();
            }

            String status =
                    buildTransactionStatusLabel(
                            document.getString("paymentStatus"),
                            document.getString("sellerPaymentStatus")
                    );

            String date =
                    getReadableDate(document);

            String amountText =
                    "+ Rs. " + formatAmount(toDouble(payoutObj));

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
    }

    // =========================================================
    // TRANSACTION STATUS LABEL
    // =========================================================

    private String buildTransactionStatusLabel(
            String paymentStatus,
            String sellerPaymentStatus) {

        String payment = safeLowerCase(paymentStatus);
        String sellerPayment = safeLowerCase(sellerPaymentStatus);

        if (sellerPayment.equals("paid")) {
            return "Paid";
        }

        if (payment.equals("received")) {
            return "Ready to Pay";
        }

        return "Pending";
    }

    // =========================================================
    // LOAD PAYMENT ACCOUNT (UNCHANGED)
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

    // =========================================================
    // HELPERS
    // =========================================================

    private long getOrderTimeValue(DocumentSnapshot document) {

        Object createdAt = document.get("createdAt");

        if (createdAt instanceof Timestamp) {
            return ((Timestamp) createdAt).toDate().getTime();
        }

        Object orderDate = document.get("orderDate");

        if (orderDate != null) {
            try {
                return Long.parseLong(String.valueOf(orderDate).trim());
            } catch (Exception ignored) {
            }
        }

        return 0L;
    }

    private String getReadableDate(DocumentSnapshot document) {

        Timestamp timestamp = document.getTimestamp("createdAt");

        if (timestamp == null) {
            return "Date not available";
        }

        Date date = timestamp.toDate();

        SimpleDateFormat formatter =
                new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

        return formatter.format(date);
    }

    private String safeLowerCase(String value) {

        if (value == null) {
            return "pending";
        }

        return value.toLowerCase(Locale.getDefault()).trim();
    }

    private double toDouble(Object value) {

        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }

        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (Exception e) {
            return 0;
        }
    }

    private String formatAmount(double amount) {

        if (amount == (long) amount) {
            return String.valueOf((long) amount);
        }

        return String.format(Locale.US, "%.2f", amount);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (auth != null) {
            loadPaymentAccount();
            loadWalletAndTransactionsFromOrders();
        }
    }
}