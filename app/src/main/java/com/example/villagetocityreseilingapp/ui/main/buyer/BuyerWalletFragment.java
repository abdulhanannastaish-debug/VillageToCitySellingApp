package com.example.villagetocityreseilingapp.ui.main.buyer;

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
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BuyerWalletFragment extends Fragment {

    // =========================================================
    // WALLET VIEWS
    // =========================================================

    private TextView txtAvailableBalance;
    private TextView txtPendingBalance;
    private TextView txtTotalRefunded;

    // =========================================================
    // REFUND STATUS
    // =========================================================

    private TextView txtRefundStatus;

    // =========================================================
    // PAYMENT ACCOUNT
    // =========================================================

    private TextView txtPaymentMethod;
    private TextView txtPaymentAccount;
    private TextView txtPaymentAccountStatus;

    private CardView paymentAccountCard;

    // =========================================================
    // RECENT REFUNDS
    // =========================================================

    private TextView txtTransactionAmount1;
    private TextView txtTransactionAmount2;
    private TextView txtTransactionAmount3;

    private TextView txtOrderId1;
    private TextView txtOrderId2;
    private TextView txtOrderId3;

    private TextView txtOrderDate1;
    private TextView txtOrderDate2;
    private TextView txtOrderDate3;

    // =========================================================
    // FIREBASE
    // =========================================================

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public BuyerWalletFragment() {
        // Required empty public constructor
    }

    // =========================================================
    // CREATE VIEW
    // =========================================================

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_buyer_wallet,
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

        super.onViewCreated(
                view,
                savedInstanceState
        );

        // =====================================================
        // FIREBASE
        // =====================================================

        auth = FirebaseAuth.getInstance();

        db = FirebaseFirestore.getInstance();

        // =====================================================
        // WALLET
        // =====================================================

        txtAvailableBalance =
                view.findViewById(
                        R.id.txtAvailableBalance
                );

        txtPendingBalance =
                view.findViewById(
                        R.id.txtPendingBalance
                );

        txtTotalRefunded =
                view.findViewById(
                        R.id.txtTotalRefunded
                );

        // =====================================================
        // REFUND STATUS
        // =====================================================

        txtRefundStatus =
                view.findViewById(
                        R.id.txtRefundStatus
                );

        // =====================================================
        // PAYMENT ACCOUNT
        // =====================================================

        txtPaymentMethod =
                view.findViewById(
                        R.id.txtPaymentMethod
                );

        txtPaymentAccount =
                view.findViewById(
                        R.id.txtPaymentAccount
                );

        txtPaymentAccountStatus =
                view.findViewById(
                        R.id.txtPaymentAccountStatus
                );

        paymentAccountCard =
                view.findViewById(
                        R.id.paymentAccountCard
                );

        // =====================================================
        // TRANSACTIONS
        // =====================================================

        txtTransactionAmount1 =
                view.findViewById(
                        R.id.txtTransactionAmount1
                );

        txtTransactionAmount2 =
                view.findViewById(
                        R.id.txtTransactionAmount2
                );

        txtTransactionAmount3 =
                view.findViewById(
                        R.id.txtTransactionAmount3
                );

        txtOrderId1 =
                view.findViewById(
                        R.id.txtOrderId1
                );

        txtOrderId2 =
                view.findViewById(
                        R.id.txtOrderId2
                );

        txtOrderId3 =
                view.findViewById(
                        R.id.txtOrderId3
                );

        txtOrderDate1 =
                view.findViewById(
                        R.id.txtOrderDate1
                );

        txtOrderDate2 =
                view.findViewById(
                        R.id.txtOrderDate2
                );

        txtOrderDate3 =
                view.findViewById(
                        R.id.txtOrderDate3
                );

        // =====================================================
        // INITIAL VALUES
        // =====================================================

        setInitialValues();

        // =====================================================
        // PAYMENT ACCOUNT CLICK
        // =====================================================

        if (paymentAccountCard != null) {

            paymentAccountCard.setOnClickListener(
                    v -> {

                        if (!isAdded()) {
                            return;
                        }

                        Toast.makeText(
                                requireContext(),
                                "Payment account setup will be available here.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
            );
        }

        // =====================================================
        // LOAD DATA
        // =====================================================

        loadWalletData();

        loadPaymentAccount();

        loadRecentRefunds();
    }

    // =========================================================
    // INITIAL VALUES
    // =========================================================

    private void setInitialValues() {

        txtAvailableBalance.setText(
                "Rs. 0"
        );

        txtPendingBalance.setText(
                "Pending Refund: Rs. 0"
        );

        txtTotalRefunded.setText(
                "Rs. 0"
        );

        txtRefundStatus.setText(
                "Automatic refund is enabled"
        );

        // =====================================================
        // PAYMENT ACCOUNT
        // =====================================================

        txtPaymentMethod.setText(
                "Not Added"
        );

        txtPaymentAccount.setText(
                "No payment account"
        );

        txtPaymentAccountStatus.setText(
                "Tap to add payment account"
        );

        // =====================================================
        // REFUNDS
        // =====================================================

        setRefundEmpty(1);
        setRefundEmpty(2);
        setRefundEmpty(3);
    }

    // =========================================================
    // LOAD BUYER WALLET
    // =========================================================

    private void loadWalletData() {

        FirebaseUser currentUser =
                auth.getCurrentUser();

        if (currentUser == null) {
            return;
        }

        String buyerId =
                currentUser.getUid();

        /*
         * Firestore path:
         *
         * buyerWallets
         *      └── buyer UID
         *
         * Fields:
         *
         * availableBalance
         * pendingBalance
         * totalRefunded
         */

        db.collection("buyerWallets")
                .document(buyerId)
                .get()
                .addOnSuccessListener(
                        documentSnapshot -> {

                            if (!isAdded()) {
                                return;
                            }

                            if (!documentSnapshot.exists()) {

                                txtAvailableBalance.setText(
                                        "Rs. 0"
                                );

                                txtPendingBalance.setText(
                                        "Pending Refund: Rs. 0"
                                );

                                txtTotalRefunded.setText(
                                        "Rs. 0"
                                );

                                return;
                            }

                            // =============================================
                            // AVAILABLE BALANCE
                            // =============================================

                            Number available =
                                    documentSnapshot.getLong(
                                            "availableBalance"
                                    );

                            // =============================================
                            // PENDING BALANCE
                            // =============================================

                            Number pending =
                                    documentSnapshot.getLong(
                                            "pendingBalance"
                                    );

                            // =============================================
                            // TOTAL REFUNDED
                            // =============================================

                            Number totalRefunded =
                                    documentSnapshot.getLong(
                                            "totalRefunded"
                                    );

                            // =============================================
                            // SHOW BALANCES
                            // =============================================

                            txtAvailableBalance.setText(
                                    "Rs. "
                                            + numberValue(
                                            available
                                    )
                            );

                            txtPendingBalance.setText(
                                    "Pending Refund: Rs. "
                                            + numberValue(
                                            pending
                                    )
                            );

                            txtTotalRefunded.setText(
                                    "Rs. "
                                            + numberValue(
                                            totalRefunded
                                    )
                            );
                        }
                )
                .addOnFailureListener(
                        e -> {

                            if (!isAdded()) {
                                return;
                            }

                            Toast.makeText(
                                    requireContext(),
                                    "Failed to load wallet: "
                                            + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }

    // =========================================================
    // LOAD PAYMENT ACCOUNT
    // =========================================================

    private void loadPaymentAccount() {

        FirebaseUser currentUser =
                auth.getCurrentUser();

        if (currentUser == null) {

            setPaymentAccountEmpty();

            return;
        }

        String buyerId =
                currentUser.getUid();

        /*
         * Firestore path:
         *
         * buyers
         *   └── buyer UID
         *        └── paymentAccount
         *             └── account
         */

        db.collection("buyers")
                .document(buyerId)
                .collection("paymentAccount")
                .document("account")
                .get()
                .addOnSuccessListener(
                        documentSnapshot -> {

                            if (!isAdded()) {
                                return;
                            }

                            if (!documentSnapshot.exists()) {

                                setPaymentAccountEmpty();

                                return;
                            }

                            String paymentMethod =
                                    documentSnapshot.getString(
                                            "paymentMethod"
                                    );

                            String accountNumber =
                                    documentSnapshot.getString(
                                            "accountNumber"
                                    );

                            String status =
                                    documentSnapshot.getString(
                                            "status"
                                    );

                            // =============================================
                            // PAYMENT METHOD
                            // =============================================

                            if (
                                    paymentMethod != null
                                            &&
                                            !paymentMethod
                                                    .trim()
                                                    .isEmpty()
                            ) {

                                txtPaymentMethod.setText(
                                        paymentMethod
                                );

                            } else {

                                txtPaymentMethod.setText(
                                        "Payment Account"
                                );
                            }

                            // =============================================
                            // ACCOUNT NUMBER
                            // =============================================

                            if (
                                    accountNumber != null
                                            &&
                                            !accountNumber
                                                    .trim()
                                                    .isEmpty()
                            ) {

                                txtPaymentAccount.setText(
                                        accountNumber
                                );

                            } else {

                                txtPaymentAccount.setText(
                                        "Account number not available"
                                );
                            }

                            // =============================================
                            // STATUS
                            // =============================================

                            if (
                                    "active"
                                            .equalsIgnoreCase(
                                                    status
                                            )
                            ) {

                                txtPaymentAccountStatus.setText(
                                        "Registered for automatic refund"
                                );

                            } else {

                                txtPaymentAccountStatus.setText(
                                        "Payment account saved"
                                );
                            }
                        }
                )
                .addOnFailureListener(
                        e -> {

                            if (!isAdded()) {
                                return;
                            }

                            Toast.makeText(
                                    requireContext(),
                                    "Failed to load payment account: "
                                            + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }

    // =========================================================
    // EMPTY PAYMENT ACCOUNT
    // =========================================================

    private void setPaymentAccountEmpty() {

        txtPaymentMethod.setText(
                "Not Added"
        );

        txtPaymentAccount.setText(
                "No payment account"
        );

        txtPaymentAccountStatus.setText(
                "Tap to add payment account"
        );
    }

    // =========================================================
    // LOAD RECENT REFUNDS
    // =========================================================

    private void loadRecentRefunds() {

        FirebaseUser currentUser =
                auth.getCurrentUser();

        if (currentUser == null) {
            return;
        }

        String buyerId =
                currentUser.getUid();

        /*
         * IMPORTANT:
         *
         * We intentionally DO NOT use:
         *
         * .orderBy("createdAt")
         *
         * because that can require a Firestore
         * composite index together with buyerId.
         *
         * Instead:
         *
         * 1. Get buyer's refunds using buyerId.
         * 2. Sort them locally in Java.
         * 3. Display latest 3.
         */

        db.collection("refundTransactions")
                .whereEqualTo(
                        "buyerId",
                        buyerId
                )
                .get()
                .addOnSuccessListener(
                        querySnapshot -> {

                            if (!isAdded()) {
                                return;
                            }

                            // =============================================
                            // RESET OLD DATA
                            // =============================================

                            setRefundEmpty(1);
                            setRefundEmpty(2);
                            setRefundEmpty(3);

                            // =============================================
                            // COPY DOCUMENTS
                            // =============================================

                            List<DocumentSnapshot> refunds =
                                    new ArrayList<>(
                                            querySnapshot.getDocuments()
                                    );

                            // =============================================
                            // SORT BY CREATED DATE
                            // LATEST FIRST
                            // =============================================

                            Collections.sort(
                                    refunds,
                                    new Comparator<DocumentSnapshot>() {

                                        @Override
                                        public int compare(
                                                DocumentSnapshot first,
                                                DocumentSnapshot second) {

                                            Timestamp firstTimestamp =
                                                    first.getTimestamp(
                                                            "createdAt"
                                                    );

                                            Timestamp secondTimestamp =
                                                    second.getTimestamp(
                                                            "createdAt"
                                                    );

                                            // Documents without date
                                            // go to the end.
                                            if (
                                                    firstTimestamp == null
                                                            &&
                                                            secondTimestamp == null
                                            ) {
                                                return 0;
                                            }

                                            if (firstTimestamp == null) {
                                                return 1;
                                            }

                                            if (secondTimestamp == null) {
                                                return -1;
                                            }

                                            // Latest first.
                                            return secondTimestamp
                                                    .compareTo(
                                                            firstTimestamp
                                                    );
                                        }
                                    }
                            );

                            // =============================================
                            // SHOW LATEST 3
                            // =============================================

                            int position = 1;

                            for (
                                    DocumentSnapshot document
                                    : refunds
                            ) {

                                if (position > 3) {
                                    break;
                                }

                                String orderId =
                                        document.getString(
                                                "orderId"
                                        );

                                Number amount =
                                        document.getLong(
                                                "amount"
                                        );

                                String date =
                                        getRefundDate(
                                                document
                                        );

                                // =========================================
                                // ORDER ID FALLBACK
                                // =========================================

                                if (
                                        orderId == null
                                                ||
                                                orderId
                                                        .trim()
                                                        .isEmpty()
                                ) {

                                    orderId =
                                            document.getId();
                                }

                                // =========================================
                                // AMOUNT
                                // =========================================

                                String amountText =
                                        "+ Rs. "
                                                + numberValue(
                                                amount
                                        );

                                // =========================================
                                // POSITION 1
                                // =========================================

                                if (position == 1) {

                                    txtOrderId1.setText(
                                            "Order #"
                                                    + orderId
                                    );

                                    txtOrderDate1.setText(
                                            date
                                    );

                                    txtTransactionAmount1.setText(
                                            amountText
                                    );
                                }

                                // =========================================
                                // POSITION 2
                                // =========================================

                                else if (position == 2) {

                                    txtOrderId2.setText(
                                            "Order #"
                                                    + orderId
                                    );

                                    txtOrderDate2.setText(
                                            date
                                    );

                                    txtTransactionAmount2.setText(
                                            amountText
                                    );
                                }

                                // =========================================
                                // POSITION 3
                                // =========================================

                                else if (position == 3) {

                                    txtOrderId3.setText(
                                            "Order #"
                                                    + orderId
                                    );

                                    txtOrderDate3.setText(
                                            date
                                    );

                                    txtTransactionAmount3.setText(
                                            amountText
                                    );
                                }

                                position++;
                            }
                        }
                )
                .addOnFailureListener(
                        e -> {

                            if (!isAdded()) {
                                return;
                            }

                            Toast.makeText(
                                    requireContext(),
                                    "Failed to load refunds: "
                                            + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }

    // =========================================================
    // EMPTY REFUND
    // =========================================================

    private void setRefundEmpty(
            int position) {

        if (position == 1) {

            txtTransactionAmount1.setText(
                    "-"
            );

            txtOrderId1.setText(
                    "No refund"
            );

            txtOrderDate1.setText(
                    "-"
            );
        }

        else if (position == 2) {

            txtTransactionAmount2.setText(
                    "-"
            );

            txtOrderId2.setText(
                    "No refund"
            );

            txtOrderDate2.setText(
                    "-"
            );
        }

        else if (position == 3) {

            txtTransactionAmount3.setText(
                    "-"
            );

            txtOrderId3.setText(
                    "No refund"
            );

            txtOrderDate3.setText(
                    "-"
            );
        }
    }

    // =========================================================
    // REFUND DATE
    // =========================================================

    private String getRefundDate(
            DocumentSnapshot document) {

        Timestamp timestamp =
                document.getTimestamp(
                        "createdAt"
                );

        if (timestamp == null) {

            return "Date not available";
        }

        Date date =
                timestamp.toDate();

        SimpleDateFormat formatter =
                new SimpleDateFormat(
                        "dd MMM yyyy",
                        Locale.getDefault()
                );

        return formatter.format(
                date
        );
    }

    // =========================================================
    // NUMBER VALUE
    // =========================================================

    private long numberValue(
            Number number) {

        if (number == null) {
            return 0;
        }

        return number.longValue();
    }

    // =========================================================
    // RELOAD
    // =========================================================

    @Override
    public void onResume() {

        super.onResume();

        if (auth != null) {

            loadWalletData();

            loadPaymentAccount();

            loadRecentRefunds();
        }
    }
}