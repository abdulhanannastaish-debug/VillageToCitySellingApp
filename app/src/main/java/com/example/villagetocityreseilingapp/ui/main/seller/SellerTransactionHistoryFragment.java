package com.example.villagetocityreseilingapp.ui.main.seller;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.villagetocityreseilingapp.R;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Shows the seller's current wallet balance plus up to 3 recent
 * order transactions, pulled directly from the "orders" collection
 * (commissionAmount / sellerPayoutAmount / paymentStatus /
 * sellerPaymentStatus fields saved at checkout time).
 */
public class SellerTransactionHistoryFragment extends Fragment {

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

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    public SellerTransactionHistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_seller_transaction_history,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

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
    // LOAD TRANSACTIONS FROM ORDERS COLLECTION
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

                    List<QueryDocumentSnapshot> ordersWithPayment =
                            new ArrayList<>();

                    double availableBalance = 0;

                    for (QueryDocumentSnapshot document : querySnapshot) {

                        Object payoutObj =
                                document.get("sellerPayoutAmount");

                        if (payoutObj == null) {
                            // Older order without commission data
                            continue;
                        }

                        ordersWithPayment.add(document);

                        String paymentStatus =
                                safeLowerCase(
                                        document.getString("paymentStatus")
                                );

                        String sellerPaymentStatus =
                                safeLowerCase(
                                        document.getString("sellerPaymentStatus")
                                );

                        // "Available" balance = admin has received the
                        // cash but hasn't paid the seller yet
                        if (paymentStatus.equals("received")
                                && !sellerPaymentStatus.equals("paid")) {

                            availableBalance +=
                                    toDouble(payoutObj);
                        }
                    }

                    if (txtCurrentBalance != null) {
                        txtCurrentBalance.setText(
                                "Rs. " + formatAmount(availableBalance)
                        );
                    }

                    ordersWithPayment.sort((a, b) ->
                            Long.compare(
                                    getOrderTimeValue(b),
                                    getOrderTimeValue(a)
                            )
                    );

                    displayTransactions(ordersWithPayment);
                })
                .addOnFailureListener(e -> {

                    if (!isAdded()) {
                        return;
                    }

                    Toast.makeText(
                            requireContext(),
                            "Failed to load transactions: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    // =========================================================
    // DISPLAY UP TO 3 TRANSACTIONS
    // =========================================================

    private void displayTransactions(
            List<QueryDocumentSnapshot> orders) {

        if (!isAdded()) {
            return;
        }

        if (orders == null || orders.isEmpty()) {

            setSlotEmpty(1);
            setSlotEmpty(2);
            setSlotEmpty(3);

            if (txtNoTransactions != null) {
                txtNoTransactions.setVisibility(View.VISIBLE);
            }

            return;
        }

        if (txtNoTransactions != null) {
            txtNoTransactions.setVisibility(View.GONE);
        }

        for (int i = 0; i < 3; i++) {

            int position = i + 1;

            if (i < orders.size()) {

                fillSlot(position, orders.get(i));

            } else {

                setSlotEmpty(position);
            }
        }
    }

    // =========================================================
    // FILL A SINGLE SLOT
    // =========================================================

    private void fillSlot(
            int position,
            DocumentSnapshot document) {

        String orderId =
                document.getString("orderId");

        if (orderId == null || orderId.trim().isEmpty()) {
            orderId = document.getId();
        }

        String date =
                getReadableDate(document);

        double payout =
                toDouble(document.get("sellerPayoutAmount"));

        String amountText =
                "+ Rs. " + formatAmount(payout);

        String status =
                buildStatusLabel(
                        document.getString("paymentStatus"),
                        document.getString("sellerPaymentStatus")
                );

        int statusColor =
                getStatusColor(
                        document.getString("paymentStatus"),
                        document.getString("sellerPaymentStatus")
                );

        if (position == 1) {

            if (txtOrderId1 != null) txtOrderId1.setText("Order #" + orderId);
            if (txtOrderDate1 != null) txtOrderDate1.setText(date);
            if (txtTransactionAmount1 != null) txtTransactionAmount1.setText(amountText);

            if (txtTransactionStatus1 != null) {
                txtTransactionStatus1.setText(status);
                txtTransactionStatus1.setTextColor(statusColor);
            }

        } else if (position == 2) {

            if (txtOrderId2 != null) txtOrderId2.setText("Order #" + orderId);
            if (txtOrderDate2 != null) txtOrderDate2.setText(date);
            if (txtTransactionAmount2 != null) txtTransactionAmount2.setText(amountText);

            if (txtTransactionStatus2 != null) {
                txtTransactionStatus2.setText(status);
                txtTransactionStatus2.setTextColor(statusColor);
            }

        } else if (position == 3) {

            if (txtOrderId3 != null) txtOrderId3.setText("Order #" + orderId);
            if (txtOrderDate3 != null) txtOrderDate3.setText(date);
            if (txtTransactionAmount3 != null) txtTransactionAmount3.setText(amountText);

            if (txtTransactionStatus3 != null) {
                txtTransactionStatus3.setText(status);
                txtTransactionStatus3.setTextColor(statusColor);
            }
        }
    }

    // =========================================================
    // EMPTY SLOT
    // =========================================================

    private void setSlotEmpty(int position) {

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
    // STATUS LABEL / COLOR
    // =========================================================

    private String buildStatusLabel(
            String paymentStatus,
            String sellerPaymentStatus) {

        String payment = safeLowerCase(paymentStatus);
        String sellerPayment = safeLowerCase(sellerPaymentStatus);

        if (sellerPayment.equals("paid")) {
            return "Paid";
        }

        if (payment.equals("received")) {
            return "Available";
        }

        return "Pending";
    }

    private int getStatusColor(
            String paymentStatus,
            String sellerPaymentStatus) {

        String payment = safeLowerCase(paymentStatus);
        String sellerPayment = safeLowerCase(sellerPaymentStatus);

        if (sellerPayment.equals("paid")) {
            return android.graphics.Color.rgb(46, 125, 50); // green
        }

        if (payment.equals("received")) {
            return android.graphics.Color.rgb(21, 101, 192); // blue
        }

        return android.graphics.Color.rgb(230, 81, 0); // orange
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
        if (auth != null && db != null) {
            loadTransactions();
        }
    }
}