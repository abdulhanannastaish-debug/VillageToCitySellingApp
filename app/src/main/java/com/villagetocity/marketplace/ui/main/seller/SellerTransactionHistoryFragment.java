package com.villagetocity.marketplace.ui.main.seller;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.villagetocity.marketplace.R;
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

/**
 * Shows the seller's full transaction / order history,
 * loaded live from the "orders" collection in Firestore.
 */
public class SellerTransactionHistoryFragment extends Fragment {

    // =========================================================
    // VIEWS
    // =========================================================

    private TextView txtCurrentBalance;

    private TextView txtOrderId1;
    private TextView txtOrderDate1;
    private TextView txtTransactionAmount1;
    private TextView txtTransactionStatus1;

    private TextView txtOrderId2;
    private TextView txtOrderDate2;
    private TextView txtTransactionAmount2;
    private TextView txtTransactionStatus2;

    private TextView txtOrderId3;
    private TextView txtOrderDate3;
    private TextView txtTransactionAmount3;
    private TextView txtTransactionStatus3;

    private TextView txtNoTransactions;

    // =========================================================
    // FIREBASE
    // =========================================================

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    public SellerTransactionHistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_seller_transaction_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        txtCurrentBalance = view.findViewById(R.id.txtCurrentBalance);

        txtOrderId1 = view.findViewById(R.id.txtOrderId1);
        txtOrderDate1 = view.findViewById(R.id.txtOrderDate1);
        txtTransactionAmount1 = view.findViewById(R.id.txtTransactionAmount1);
        txtTransactionStatus1 = view.findViewById(R.id.txtTransactionStatus1);

        txtOrderId2 = view.findViewById(R.id.txtOrderId2);
        txtOrderDate2 = view.findViewById(R.id.txtOrderDate2);
        txtTransactionAmount2 = view.findViewById(R.id.txtTransactionAmount2);
        txtTransactionStatus2 = view.findViewById(R.id.txtTransactionStatus2);

        txtOrderId3 = view.findViewById(R.id.txtOrderId3);
        txtOrderDate3 = view.findViewById(R.id.txtOrderDate3);
        txtTransactionAmount3 = view.findViewById(R.id.txtTransactionAmount3);
        txtTransactionStatus3 = view.findViewById(R.id.txtTransactionStatus3);

        txtNoTransactions = view.findViewById(R.id.txtNoTransactions);

        loadTransactions();
    }

    // =========================================================
    // LOAD TRANSACTIONS FROM "orders" COLLECTION
    // =========================================================

    private void loadTransactions() {

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

                    double currentBalance = 0;

                    List<QueryDocumentSnapshot> allOrders = new ArrayList<>();

                    for (QueryDocumentSnapshot document : querySnapshot) {

                        Object payoutObj = document.get("sellerPayoutAmount");

                        if (payoutObj == null) {
                            continue;
                        }

                        allOrders.add(document);

                        double payout = toDouble(payoutObj);

                        String paymentStatus =
                                safeLowerCase(document.getString("paymentStatus"));
                        String sellerPaymentStatus =
                                safeLowerCase(document.getString("sellerPaymentStatus"));

                        if (!sellerPaymentStatus.equals("paid")
                                && paymentStatus.equals("received")) {
                            currentBalance += payout;
                        }
                    }

                    if (txtCurrentBalance != null) {
                        txtCurrentBalance.setText("Rs. " + formatAmount(currentBalance));
                    }

                    allOrders.sort((a, b) ->
                            Long.compare(getOrderTimeValue(b), getOrderTimeValue(a)));

                    displayTransactions(allOrders);
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    Toast.makeText(requireContext(),
                            "Failed to load transactions: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    // =========================================================
    // DISPLAY TRANSACTIONS (TOP 3)
    // =========================================================

    private void displayTransactions(List<QueryDocumentSnapshot> allOrders) {

        if (!isAdded()) {
            return;
        }

        if (allOrders.isEmpty()) {
            if (txtNoTransactions != null) {
                txtNoTransactions.setVisibility(View.VISIBLE);
            }
            setRowEmpty(1);
            setRowEmpty(2);
            setRowEmpty(3);
            return;
        }

        if (txtNoTransactions != null) {
            txtNoTransactions.setVisibility(View.GONE);
        }

        int position = 1;

        for (DocumentSnapshot document : allOrders) {

            if (position > 3) break;

            Object payoutObj = document.get("sellerPayoutAmount");

            if (payoutObj == null) {
                continue;
            }

            String orderId = document.getString("orderId");
            if (orderId == null || orderId.trim().isEmpty()) {
                orderId = document.getId();
            }

            String status = buildStatusLabel(
                    document.getString("paymentStatus"),
                    document.getString("sellerPaymentStatus")
            );

            String date = getReadableDate(document);

            String amountText = "+ Rs. " + formatAmount(toDouble(payoutObj));

            if (position == 1) {
                setRow(txtOrderId1, txtOrderDate1, txtTransactionAmount1,
                        txtTransactionStatus1, orderId, date, amountText, status);
            } else if (position == 2) {
                setRow(txtOrderId2, txtOrderDate2, txtTransactionAmount2,
                        txtTransactionStatus2, orderId, date, amountText, status);
            } else {
                setRow(txtOrderId3, txtOrderDate3, txtTransactionAmount3,
                        txtTransactionStatus3, orderId, date, amountText, status);
            }

            position++;
        }

        // Clear out any remaining unused rows
        while (position <= 3) {
            setRowEmpty(position);
            position++;
        }
    }

    private void setRow(TextView orderIdView, TextView dateView, TextView amountView,
                        TextView statusView, String orderId, String date,
                        String amount, String status) {

        if (orderIdView != null) orderIdView.setText("Order #" + orderId);
        if (dateView != null) dateView.setText(date);
        if (amountView != null) amountView.setText(amount);
        if (statusView != null) statusView.setText(status);
    }

    private void setRowEmpty(int position) {
        if (position == 1) {
            if (txtOrderId1 != null) txtOrderId1.setText("No transaction");
            if (txtOrderDate1 != null) txtOrderDate1.setText("-");
            if (txtTransactionAmount1 != null) txtTransactionAmount1.setText("-");
            if (txtTransactionStatus1 != null) txtTransactionStatus1.setText("-");
        } else if (position == 2) {
            if (txtOrderId2 != null) txtOrderId2.setText("No transaction");
            if (txtOrderDate2 != null) txtOrderDate2.setText("-");
            if (txtTransactionAmount2 != null) txtTransactionAmount2.setText("-");
            if (txtTransactionStatus2 != null) txtTransactionStatus2.setText("-");
        } else if (position == 3) {
            if (txtOrderId3 != null) txtOrderId3.setText("No transaction");
            if (txtOrderDate3 != null) txtOrderDate3.setText("-");
            if (txtTransactionAmount3 != null) txtTransactionAmount3.setText("-");
            if (txtTransactionStatus3 != null) txtTransactionStatus3.setText("-");
        }
    }

    // =========================================================
    // STATUS LABEL
    // =========================================================

    private String buildStatusLabel(String paymentStatus, String sellerPaymentStatus) {

        String payment = safeLowerCase(paymentStatus);
        String sellerPayment = safeLowerCase(sellerPaymentStatus);

        if (sellerPayment.equals("paid")) {
            return "Paid Automatically";
        }

        if (payment.equals("received")) {
            return "Available";
        }

        return "Pending";
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private long getOrderTimeValue(DocumentSnapshot document) {

        Object createdAt = document.get("createdAt");

        if (createdAt instanceof Timestamp) {
            return ((Timestamp) createdAt).toDate().getTime();
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
}