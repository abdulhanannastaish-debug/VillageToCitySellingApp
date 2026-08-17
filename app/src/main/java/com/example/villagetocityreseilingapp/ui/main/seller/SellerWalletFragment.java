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
import com.google.firebase.firestore.Query;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SellerWalletFragment extends Fragment {

    // =========================================================
    // WALLET VIEWS
    // =========================================================

    private TextView txtAvailableBalance;
    private TextView txtPendingBalance;
    private TextView txtTotalEarned;
    private TextView txtTotalPaid;

    // =========================================================
    // PAYMENT ACCOUNT VIEWS
    // =========================================================

    private TextView txtPaymentMethod;
    private TextView txtPaymentAccount;
    private TextView txtPaymentAccountStatus;

    private CardView paymentAccountCard;

    // =========================================================
    // TRANSACTION VIEWS
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

    private TextView txtTransactionStatus1;
    private TextView txtTransactionStatus2;
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
            Bundle savedInstanceState) {

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
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        // =====================================================
        // FIREBASE
        // =====================================================

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // =====================================================
        // WALLET VIEWS
        // =====================================================

        txtAvailableBalance =
                view.findViewById(R.id.txtAvailableBalance);

        txtPendingBalance =
                view.findViewById(R.id.txtPendingBalance);

        txtTotalEarned =
                view.findViewById(R.id.txtTotalEarned);

        txtTotalPaid =
                view.findViewById(R.id.txtTotalPaid);

        // =====================================================
        // PAYMENT ACCOUNT
        // =====================================================

        txtPaymentMethod =
                view.findViewById(R.id.txtPaymentMethod);

        txtPaymentAccount =
                view.findViewById(R.id.txtPaymentAccount);

        txtPaymentAccountStatus =
                view.findViewById(R.id.txtPaymentAccountStatus);

        paymentAccountCard =
                view.findViewById(R.id.paymentAccountCard);

        // =====================================================
        // TRANSACTIONS
        // =====================================================

        txtTransactionAmount1 =
                view.findViewById(R.id.txtTransactionAmount1);

        txtTransactionAmount2 =
                view.findViewById(R.id.txtTransactionAmount2);

        txtTransactionAmount3 =
                view.findViewById(R.id.txtTransactionAmount3);

        txtOrderId1 =
                view.findViewById(R.id.txtOrderId1);

        txtOrderId2 =
                view.findViewById(R.id.txtOrderId2);

        txtOrderId3 =
                view.findViewById(R.id.txtOrderId3);

        txtOrderDate1 =
                view.findViewById(R.id.txtOrderDate1);

        txtOrderDate2 =
                view.findViewById(R.id.txtOrderDate2);

        txtOrderDate3 =
                view.findViewById(R.id.txtOrderDate3);

        txtTransactionStatus1 =
                view.findViewById(R.id.txtTransactionStatus1);

        txtTransactionStatus2 =
                view.findViewById(R.id.txtTransactionStatus2);

        txtTransactionStatus3 =
                view.findViewById(R.id.txtTransactionStatus3);

        txtViewAllTransactions =
                view.findViewById(R.id.txtViewAllTransactions);

        // =====================================================
        // INITIAL VALUES
        // =====================================================

        setInitialValues();

        // =====================================================
        // PAYMENT ACCOUNT CLICK
        // =====================================================

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

        // =====================================================
        // VIEW ALL TRANSACTIONS
        // =====================================================

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

        // =====================================================
        // LOAD DATA
        // =====================================================

        loadWalletData();
        loadPaymentAccount();
        loadRecentTransactions();
    }

    // =========================================================
    // INITIAL VALUES
    // =========================================================

    private void setInitialValues() {

        txtAvailableBalance.setText("Rs. 0");
        txtPendingBalance.setText("Pending: Rs. 0");
        txtTotalEarned.setText("Rs. 0");
        txtTotalPaid.setText("Rs. 0");

        txtPaymentMethod.setText("Not Added");
        txtPaymentAccount.setText("No payment account");
        txtPaymentAccountStatus.setText(
                "Tap to add payment account"
        );

        // Transaction 1
        txtTransactionAmount1.setText("-");
        txtOrderId1.setText("No transaction");
        txtOrderDate1.setText("-");
        txtTransactionStatus1.setText("-");

        // Transaction 2
        txtTransactionAmount2.setText("-");
        txtOrderId2.setText("No transaction");
        txtOrderDate2.setText("-");
        txtTransactionStatus2.setText("-");

        // Transaction 3
        txtTransactionAmount3.setText("-");
        txtOrderId3.setText("No transaction");
        txtOrderDate3.setText("-");
        txtTransactionStatus3.setText("-");
    }

    // =========================================================
    // LOAD WALLET DATA
    // =========================================================

    private void loadWalletData() {

        FirebaseUser currentUser =
                auth.getCurrentUser();

        if (currentUser == null) {
            return;
        }

        String uid =
                currentUser.getUid();

        db.collection("sellerWallets")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (!isAdded()) {
                        return;
                    }

                    if (!documentSnapshot.exists()) {

                        txtAvailableBalance.setText("Rs. 0");
                        txtPendingBalance.setText(
                                "Pending: Rs. 0"
                        );
                        txtTotalEarned.setText("Rs. 0");
                        txtTotalPaid.setText("Rs. 0");

                        return;
                    }

                    // =================================================
                    // GET BALANCES
                    // =================================================

                    Number available =
                            documentSnapshot.getLong(
                                    "availableBalance"
                            );

                    Number pending =
                            documentSnapshot.getLong(
                                    "pendingBalance"
                            );

                    Number totalEarned =
                            documentSnapshot.getLong(
                                    "totalEarned"
                            );

                    Number totalPaid =
                            documentSnapshot.getLong(
                                    "totalPaid"
                            );

                    // =================================================
                    // SHOW BALANCES
                    // =================================================

                    txtAvailableBalance.setText(
                            "Rs. " + numberValue(available)
                    );

                    txtPendingBalance.setText(
                            "Pending: Rs. "
                                    + numberValue(pending)
                    );

                    txtTotalEarned.setText(
                            "Rs. "
                                    + numberValue(totalEarned)
                    );

                    txtTotalPaid.setText(
                            "Rs. "
                                    + numberValue(totalPaid)
                    );

                })
                .addOnFailureListener(e -> {

                    if (!isAdded()) {
                        return;
                    }

                    Toast.makeText(
                            requireContext(),
                            "Failed to load wallet: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    // =========================================================
    // LOAD PAYMENT ACCOUNT
    // =========================================================

    private void loadPaymentAccount() {

        FirebaseUser currentUser =
                auth.getCurrentUser();

        if (currentUser == null) {

            txtPaymentMethod.setText("Not Added");
            txtPaymentAccount.setText(
                    "No payment account"
            );
            txtPaymentAccountStatus.setText(
                    "Please add a payment account"
            );

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

                    if (!documentSnapshot.exists()) {

                        txtPaymentMethod.setText(
                                "Not Added"
                        );

                        txtPaymentAccount.setText(
                                "No payment account"
                        );

                        txtPaymentAccountStatus.setText(
                                "Tap to add payment account"
                        );

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

                    if (paymentMethod != null &&
                            !paymentMethod.trim().isEmpty()) {

                        txtPaymentMethod.setText(
                                paymentMethod
                        );

                    } else {

                        txtPaymentMethod.setText(
                                "Payment Account"
                        );
                    }

                    if (accountNumber != null &&
                            !accountNumber.trim().isEmpty()) {

                        txtPaymentAccount.setText(
                                accountNumber
                        );

                    } else {

                        txtPaymentAccount.setText(
                                "Account number not available"
                        );
                    }

                    if ("active".equalsIgnoreCase(status)) {

                        txtPaymentAccountStatus.setText(
                                "Registered for automatic payment"
                        );

                    } else {

                        txtPaymentAccountStatus.setText(
                                "Payment account saved"
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
    // LOAD RECENT TRANSACTIONS
    // =========================================================

    private void loadRecentTransactions() {

        FirebaseUser currentUser =
                auth.getCurrentUser();

        if (currentUser == null) {
            return;
        }

        String sellerId =
                currentUser.getUid();

        db.collection("transactions")
                .whereEqualTo(
                        "sellerId",
                        sellerId
                )
                .limit(3)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    if (!isAdded()) {
                        return;
                    }

                    // Reset first
                    setTransactionEmpty(
                            1
                    );

                    setTransactionEmpty(
                            2
                    );

                    setTransactionEmpty(
                            3
                    );

                    int position = 1;

                    for (
                            DocumentSnapshot document
                            : querySnapshot.getDocuments()
                    ) {

                        if (position > 3) {
                            break;
                        }

                        String orderId =
                                document.getString(
                                        "orderId"
                                );

                        String status =
                                document.getString(
                                        "status"
                                );

                        Number amount =
                                document.getLong(
                                        "amount"
                                );

                        String date =
                                getTransactionDate(
                                        document
                                );

                        if (orderId == null ||
                                orderId.trim().isEmpty()) {

                            orderId =
                                    document.getId();
                        }

                        if (status == null ||
                                status.trim().isEmpty()) {

                            status = "Pending";
                        }

                        String amountText =
                                "+ Rs. "
                                        + numberValue(amount);

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

                            txtTransactionStatus1.setText(
                                    status
                            );

                        } else if (position == 2) {

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

                            txtTransactionStatus2.setText(
                                    status
                            );

                        } else if (position == 3) {

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

                            txtTransactionStatus3.setText(
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

    private void setTransactionEmpty(
            int position) {

        if (position == 1) {

            txtTransactionAmount1.setText("-");
            txtOrderId1.setText(
                    "No transaction"
            );
            txtOrderDate1.setText("-");
            txtTransactionStatus1.setText("-");

        } else if (position == 2) {

            txtTransactionAmount2.setText("-");
            txtOrderId2.setText(
                    "No transaction"
            );
            txtOrderDate2.setText("-");
            txtTransactionStatus2.setText("-");

        } else if (position == 3) {

            txtTransactionAmount3.setText("-");
            txtOrderId3.setText(
                    "No transaction"
            );
            txtOrderDate3.setText("-");
            txtTransactionStatus3.setText("-");
        }
    }

    // =========================================================
    // TRANSACTION DATE
    // =========================================================

    private String getTransactionDate(
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
    // RELOAD WALLET
    // =========================================================

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