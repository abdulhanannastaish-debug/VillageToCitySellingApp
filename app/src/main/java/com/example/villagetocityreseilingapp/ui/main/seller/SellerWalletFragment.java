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
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SellerWalletFragment extends Fragment {

    // =========================================================
    // WALLET TEXT VIEWS
    // =========================================================

    private TextView txtAvailableBalance;
    private TextView txtPendingBalance;
    private TextView txtTotalEarned;
    private TextView txtTotalPaid;

    // =========================================================
    // PAYMENT ACCOUNT
    // =========================================================

    private TextView txtPaymentMethod;
    private TextView txtPaymentAccount;
    private TextView txtPaymentAccountStatus;
    private CardView paymentAccountCard;

    // =========================================================
    // TRANSACTION 1
    // =========================================================

    private TextView txtTransactionAmount1;
    private TextView txtOrderId1;
    private TextView txtOrderDate1;
    private TextView txtTransactionStatus1;

    // =========================================================
    // TRANSACTION 2
    // =========================================================

    private TextView txtTransactionAmount2;
    private TextView txtOrderId2;
    private TextView txtOrderDate2;
    private TextView txtTransactionStatus2;

    // =========================================================
    // TRANSACTION 3
    // =========================================================

    private TextView txtTransactionAmount3;
    private TextView txtOrderId3;
    private TextView txtOrderDate3;
    private TextView txtTransactionStatus3;

    private TextView txtViewAllTransactions;

    // =========================================================
    // FIREBASE
    // =========================================================

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public SellerWalletFragment() {
        // Required empty public constructor
    }

    // =========================================================
    // CREATE VIEW
    // =========================================================

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        return inflater.inflate(
                R.layout.fragment_seller_wallet,
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
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        // Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // =====================================================
        // FIND VIEWS
        // =====================================================

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

        txtViewAllTransactions = view.findViewById(
                R.id.txtViewAllTransactions
        );

        // =====================================================
        // INITIAL VALUES
        // =====================================================

        setInitialValues();

        // =====================================================
        // PAYMENT ACCOUNT CLICK
        // =====================================================

        if (paymentAccountCard != null) {

            paymentAccountCard.setOnClickListener(v -> {

                if (!isAdded()) {
                    return;
                }

                requireActivity()
                        .getSupportFragmentManager()
                        .beginTransaction()
                        .setReorderingAllowed(true)
                        .replace(
                                R.id.fragment_container,
                                new SellerPaymentAccountFragment()
                        )
                        .addToBackStack(null)
                        .commit();
            });
        }

        // =====================================================
        // VIEW ALL TRANSACTIONS
        // =====================================================

        if (txtViewAllTransactions != null) {

            txtViewAllTransactions.setOnClickListener(v -> {

                if (!isAdded()) {
                    return;
                }

                requireActivity()
                        .getSupportFragmentManager()
                        .beginTransaction()
                        .setReorderingAllowed(true)
                        .replace(
                                R.id.fragment_container,
                                new SellerTransactionHistoryFragment()
                        )
                        .addToBackStack(null)
                        .commit();
            });
        }

        // =====================================================
        // LOAD FIREBASE DATA
        // =====================================================

        loadWalletData();
        loadPaymentAccount();
        loadRecentTransactions();
    }

    // =========================================================
    // INITIAL VALUES
    // =========================================================

    private void setInitialValues() {

        setTextSafe(
                txtAvailableBalance,
                "Rs. 0"
        );

        setTextSafe(
                txtPendingBalance,
                "Pending: Rs. 0"
        );

        setTextSafe(
                txtTotalEarned,
                "Rs. 0"
        );

        setTextSafe(
                txtTotalPaid,
                "Rs. 0"
        );

        setTextSafe(
                txtPaymentMethod,
                "Not Added"
        );

        setTextSafe(
                txtPaymentAccount,
                "No payment account"
        );

        setTextSafe(
                txtPaymentAccountStatus,
                "Tap to add payment account"
        );

        // Transaction 1
        setTransactionEmpty(1);

        // Transaction 2
        setTransactionEmpty(2);

        // Transaction 3
        setTransactionEmpty(3);
    }

    // =========================================================
    // NULL SAFE SET TEXT
    // =========================================================

    private void setTextSafe(
            TextView textView,
            String text
    ) {
        if (textView != null) {
            textView.setText(text);
        }
    }

    // =========================================================
    // LOAD WALLET DATA
    // =========================================================

    private void loadWalletData() {

        if (auth == null || db == null) {
            return;
        }

        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            return;
        }

        String uid = currentUser.getUid();

        db.collection("sellerWallets")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (!isAdded()) {
                        return;
                    }

                    if (!documentSnapshot.exists()) {

                        setTextSafe(
                                txtAvailableBalance,
                                "Rs. 0"
                        );

                        setTextSafe(
                                txtPendingBalance,
                                "Pending: Rs. 0"
                        );

                        setTextSafe(
                                txtTotalEarned,
                                "Rs. 0"
                        );

                        setTextSafe(
                                txtTotalPaid,
                                "Rs. 0"
                        );

                        return;
                    }

                    Number available =
                            documentSnapshot.getLong("availableBalance");

                    Number pending =
                            documentSnapshot.getLong("pendingBalance");

                    Number totalEarned =
                            documentSnapshot.getLong("totalEarned");

                    Number totalPaid =
                            documentSnapshot.getLong("totalPaid");

                    setTextSafe(
                            txtAvailableBalance,
                            "Rs. " + numberValue(available)
                    );

                    setTextSafe(
                            txtPendingBalance,
                            "Pending: Rs. " + numberValue(pending)
                    );

                    setTextSafe(
                            txtTotalEarned,
                            "Rs. " + numberValue(totalEarned)
                    );

                    setTextSafe(
                            txtTotalPaid,
                            "Rs. " + numberValue(totalPaid)
                    );
                })
                .addOnFailureListener(e -> {

                    if (!isAdded()) {
                        return;
                    }

                    Toast.makeText(
                            requireContext(),
                            "Failed to load wallet: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    // =========================================================
    // LOAD PAYMENT ACCOUNT
    // =========================================================

    private void loadPaymentAccount() {

        if (auth == null || db == null) {
            return;
        }

        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {

            setTextSafe(
                    txtPaymentMethod,
                    "Not Added"
            );

            setTextSafe(
                    txtPaymentAccount,
                    "No payment account"
            );

            setTextSafe(
                    txtPaymentAccountStatus,
                    "Please add a payment account"
            );

            return;
        }

        String uid = currentUser.getUid();

        db.collection("sellers")
                .document(uid)
                .collection("paymentAccount")
                .document("account")
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (!isAdded()) {
                        return;
                    }

                    if (!documentSnapshot.exists()) {

                        setTextSafe(
                                txtPaymentMethod,
                                "Not Added"
                        );

                        setTextSafe(
                                txtPaymentAccount,
                                "No payment account"
                        );

                        setTextSafe(
                                txtPaymentAccountStatus,
                                "Tap to add payment account"
                        );

                        return;
                    }

                    String paymentMethod =
                            documentSnapshot.getString("paymentMethod");

                    String accountNumber =
                            documentSnapshot.getString("accountNumber");

                    String status =
                            documentSnapshot.getString("status");

                    if (paymentMethod == null ||
                            paymentMethod.trim().isEmpty()) {

                        paymentMethod = "Payment Account";
                    }

                    if (accountNumber == null ||
                            accountNumber.trim().isEmpty()) {

                        accountNumber = "Account number not available";
                    }

                    String statusText;

                    if ("active".equalsIgnoreCase(status)) {

                        statusText =
                                "Registered for automatic payment";

                    } else {

                        statusText =
                                "Payment account saved";
                    }

                    setTextSafe(
                            txtPaymentMethod,
                            paymentMethod
                    );

                    setTextSafe(
                            txtPaymentAccount,
                            accountNumber
                    );

                    setTextSafe(
                            txtPaymentAccountStatus,
                            statusText
                    );
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
    // LOAD RECENT TRANSACTIONS
    // =========================================================

    private void loadRecentTransactions() {

        if (auth == null || db == null) {
            return;
        }

        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            return;
        }

        String sellerId = currentUser.getUid();

        db.collection("transactions")
                .whereEqualTo("sellerId", sellerId)
                .limit(3)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    if (!isAdded()) {
                        return;
                    }

                    // Reset all transaction cards
                    setTransactionEmpty(1);
                    setTransactionEmpty(2);
                    setTransactionEmpty(3);

                    int position = 1;

                    for (DocumentSnapshot document :
                            querySnapshot.getDocuments()) {

                        if (position > 3) {
                            break;
                        }

                        String orderId =
                                document.getString("orderId");

                        String status =
                                document.getString("status");

                        Number amount =
                                document.getLong("amount");

                        String date =
                                getTransactionDate(document);

                        if (orderId == null ||
                                orderId.trim().isEmpty()) {

                            orderId = document.getId();
                        }

                        if (status == null ||
                                status.trim().isEmpty()) {

                            status = "Pending";
                        }

                        String amountText =
                                "+ Rs. " + numberValue(amount);

                        // =================================================
                        // TRANSACTION 1
                        // =================================================

                        if (position == 1) {

                            setTextSafe(
                                    txtOrderId1,
                                    "Order #" + orderId
                            );

                            setTextSafe(
                                    txtOrderDate1,
                                    date
                            );

                            setTextSafe(
                                    txtTransactionAmount1,
                                    amountText
                            );

                            setTextSafe(
                                    txtTransactionStatus1,
                                    status
                            );
                        }

                        // =================================================
                        // TRANSACTION 2
                        // =================================================

                        else if (position == 2) {

                            setTextSafe(
                                    txtOrderId2,
                                    "Order #" + orderId
                            );

                            setTextSafe(
                                    txtOrderDate2,
                                    date
                            );

                            setTextSafe(
                                    txtTransactionAmount2,
                                    amountText
                            );

                            setTextSafe(
                                    txtTransactionStatus2,
                                    status
                            );
                        }

                        // =================================================
                        // TRANSACTION 3
                        // =================================================

                        else if (position == 3) {

                            setTextSafe(
                                    txtOrderId3,
                                    "Order #" + orderId
                            );

                            setTextSafe(
                                    txtOrderDate3,
                                    date
                            );

                            setTextSafe(
                                    txtTransactionAmount3,
                                    amountText
                            );

                            setTextSafe(
                                    txtTransactionStatus3,
                                    status
                            );
                        }

                        position++;
                    }
                })
                .addOnFailureListener(e -> {

                    if (!isAdded()) {
                        return;
                    }

                    Toast.makeText(
                            requireContext(),
                            "Failed to load transactions: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    // =========================================================
    // EMPTY TRANSACTION
    // =========================================================

    private void setTransactionEmpty(int position) {

        if (position == 1) {

            setTextSafe(
                    txtTransactionAmount1,
                    "-"
            );

            setTextSafe(
                    txtOrderId1,
                    "No transaction"
            );

            setTextSafe(
                    txtOrderDate1,
                    "-"
            );

            setTextSafe(
                    txtTransactionStatus1,
                    "-"
            );
        }

        else if (position == 2) {

            setTextSafe(
                    txtTransactionAmount2,
                    "-"
            );

            setTextSafe(
                    txtOrderId2,
                    "No transaction"
            );

            setTextSafe(
                    txtOrderDate2,
                    "-"
            );

            setTextSafe(
                    txtTransactionStatus2,
                    "-"
            );
        }

        else if (position == 3) {

            setTextSafe(
                    txtTransactionAmount3,
                    "-"
            );

            setTextSafe(
                    txtOrderId3,
                    "No transaction"
            );

            setTextSafe(
                    txtOrderDate3,
                    "-"
            );

            setTextSafe(
                    txtTransactionStatus3,
                    "-"
            );
        }
    }

    // =========================================================
    // TRANSACTION DATE
    // =========================================================

    private String getTransactionDate(
            DocumentSnapshot document
    ) {

        Timestamp timestamp =
                document.getTimestamp("createdAt");

        if (timestamp == null) {
            return "Date not available";
        }

        Date date = timestamp.toDate();

        SimpleDateFormat formatter =
                new SimpleDateFormat(
                        "dd MMM yyyy",
                        Locale.getDefault()
                );

        return formatter.format(date);
    }

    // =========================================================
    // NUMBER VALUE
    // =========================================================

    private long numberValue(Number number) {

        if (number == null) {
            return 0;
        }

        return number.longValue();
    }

    // =========================================================
    // ON RESUME
    // =========================================================

    @Override
    public void onResume() {

        super.onResume();

        if (auth == null) {
            return;
        }

        loadWalletData();
        loadPaymentAccount();
        loadRecentTransactions();
    }
}