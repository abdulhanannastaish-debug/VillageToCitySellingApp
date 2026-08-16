package com.example.villagetocityreseilingapp.ui.main.buyer;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.example.villagetocityreseilingapp.R;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BuyerOrderFragment extends Fragment {

    // =========================================================
    // FIREBASE
    // =========================================================

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    // =========================================================
    // ORDERS CONTAINER
    // IMPORTANT: XML ID = ordersContainer
    // =========================================================

    private LinearLayout ordersContainer;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public BuyerOrderFragment() {
        // Required empty constructor
    }

    // =========================================================
    // CREATE VIEW
    // =========================================================

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_buyer_order,
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

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // =====================================================
        // FIND ORDERS CONTAINER
        // =====================================================

        ordersContainer =
                view.findViewById(
                        R.id.ordersContainer
                );

        // =====================================================
        // LOAD ORDERS
        // =====================================================

        loadBuyerOrders();
    }

    // =========================================================
    // LOAD BUYER ORDERS
    // =========================================================

    private void loadBuyerOrders() {

        if (!isAdded()) {
            return;
        }

        FirebaseUser currentUser =
                auth.getCurrentUser();

        if (currentUser == null) {

            showNoOrders(
                    "Please login first."
            );

            return;
        }

        if (ordersContainer == null) {
            return;
        }

        String buyerId =
                currentUser.getUid();

        ordersContainer.removeAllViews();

        // =====================================================
        // FIRST QUERY
        // =====================================================

        db.collection("orders")
                .whereEqualTo(
                        "buyerId",
                        buyerId
                )
                .orderBy(
                        "createdAt",
                        Query.Direction.DESCENDING
                )
                .get()
                .addOnSuccessListener(
                        snapshots -> {

                            if (!isAdded()) {
                                return;
                            }

                            ordersContainer.removeAllViews();

                            if (
                                    snapshots == null
                                            || snapshots.isEmpty()
                            ) {

                                showNoOrders(
                                        "No orders found."
                                );

                                return;
                            }

                            for (
                                    DocumentSnapshot document :
                                    snapshots.getDocuments()
                            ) {

                                addOrderCard(
                                        document
                                );
                            }
                        }
                )
                .addOnFailureListener(
                        e -> {

                            // =================================================
                            // FALLBACK WITHOUT ORDER BY
                            // =================================================

                            loadBuyerOrdersFallback();
                        }
                );
    }

    // =========================================================
    // FALLBACK ORDER LOADING
    // =========================================================

    private void loadBuyerOrdersFallback() {

        if (!isAdded()) {
            return;
        }

        FirebaseUser currentUser =
                auth.getCurrentUser();

        if (currentUser == null) {
            return;
        }

        String buyerId =
                currentUser.getUid();

        db.collection("orders")
                .whereEqualTo(
                        "buyerId",
                        buyerId
                )
                .get()
                .addOnSuccessListener(
                        snapshots -> {

                            if (!isAdded()) {
                                return;
                            }

                            ordersContainer.removeAllViews();

                            if (
                                    snapshots == null
                                            || snapshots.isEmpty()
                            ) {

                                showNoOrders(
                                        "No orders found."
                                );

                                return;
                            }

                            List<DocumentSnapshot> orderList =
                                    new ArrayList<>(
                                            snapshots.getDocuments()
                                    );

                            // =================================================
                            // NEWEST FIRST
                            // =================================================

                            orderList.sort(
                                    new Comparator<DocumentSnapshot>() {

                                        @Override
                                        public int compare(
                                                DocumentSnapshot first,
                                                DocumentSnapshot second) {

                                            long firstTime =
                                                    getCreatedTime(
                                                            first.getData()
                                                    );

                                            long secondTime =
                                                    getCreatedTime(
                                                            second.getData()
                                                    );

                                            return Long.compare(
                                                    secondTime,
                                                    firstTime
                                            );
                                        }
                                    }
                            );

                            for (
                                    DocumentSnapshot document :
                                    orderList
                            ) {

                                addOrderCard(
                                        document
                                );
                            }
                        }
                )
                .addOnFailureListener(
                        e -> {

                            if (!isAdded()) {
                                return;
                            }

                            showNoOrders(
                                    "Failed to load orders."
                            );

                            Toast.makeText(
                                    requireContext(),
                                    "Failed to load orders: "
                                            + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }

    // =========================================================
    // ADD ORDER CARD
    // =========================================================

    private void addOrderCard(
            DocumentSnapshot document) {

        if (!isAdded()) {
            return;
        }

        if (ordersContainer == null) {
            return;
        }

        // =====================================================
        // INFLATE ORDER ITEM
        // =====================================================

        View orderView =
                LayoutInflater.from(
                        requireContext()
                ).inflate(
                        R.layout.item_buyer_order,
                        ordersContainer,
                        false
                );

        // =====================================================
        // FIND VIEWS
        // =====================================================

        TextView tvOrderProduct =
                orderView.findViewById(
                        R.id.tvOrderProduct
                );

        TextView tvOrderId =
                orderView.findViewById(
                        R.id.tvOrderId
                );

        TextView tvOrderDate =
                orderView.findViewById(
                        R.id.tvOrderDate
                );

        TextView tvOrderPrice =
                orderView.findViewById(
                        R.id.tvOrderPrice
                );

        TextView tvOrderQuantity =
                orderView.findViewById(
                        R.id.tvOrderQuantity
                );

        TextView tvOrderStatus =
                orderView.findViewById(
                        R.id.tvOrderStatus
                );

        AppCompatButton btnCancelOrder =
                orderView.findViewById(
                        R.id.btnCancelOrder
                );

        AppCompatButton btnRateProduct =
                orderView.findViewById(
                        R.id.btnRateProduct
                );

        AppCompatButton btnComplaint =
                orderView.findViewById(
                        R.id.btnComplaint
                );

        TextView tvComplaintStatus =
                orderView.findViewById(
                        R.id.tvComplaintStatus
                );

        // =====================================================
        // DATA
        // =====================================================

        Map<String, Object> data =
                document.getData();

        if (data == null) {
            return;
        }

        // =====================================================
        // PRODUCT NAME
        // =====================================================

        String productName =
                getStringValue(
                        data,
                        "productName",
                        "Product"
                );

        // =====================================================
        // ORDER ID
        // =====================================================

        String orderId =
                getStringValue(
                        data,
                        "orderId",
                        document.getId()
                );

        // =====================================================
        // PRODUCT ID
        // =====================================================

        String productId =
                getStringValue(
                        data,
                        "productId",
                        ""
                );

        // =====================================================
        // SELLER ID
        // =====================================================

        String sellerId =
                getStringValue(
                        data,
                        "sellerId",
                        ""
                );

        // =====================================================
        // SELLER NAME
        // =====================================================

        String sellerName =
                getStringValue(
                        data,
                        "sellerName",
                        "Seller"
                );

        // =====================================================
        // QUANTITY
        // =====================================================

        String quantity =
                getStringValue(
                        data,
                        "quantity",
                        "1"
                );

        // =====================================================
        // TOTAL AMOUNT
        // =====================================================

        String totalAmount =
                getStringValue(
                        data,
                        "totalAmount",
                        "0"
                );

        // =====================================================
        // ORDER DATE
        // =====================================================

        String orderDate =
                getOrderDate(
                        data
                );

        // =====================================================
        // STATUS
        // =====================================================

        String orderStatus =
                getStringValue(
                        data,
                        "status",
                        "pending"
                );

        String cleanStatus =
                orderStatus
                        .trim()
                        .toLowerCase(
                                Locale.getDefault()
                        );

        // =====================================================
        // SET BASIC DATA
        // =====================================================

        if (tvOrderProduct != null) {

            tvOrderProduct.setText(
                    productName
            );
        }

        if (tvOrderId != null) {

            tvOrderId.setText(
                    "Order ID: " + orderId
            );
        }

        if (tvOrderDate != null) {

            tvOrderDate.setText(
                    orderDate
            );
        }

        if (tvOrderPrice != null) {

            tvOrderPrice.setText(
                    "Rs " + totalAmount
            );
        }

        if (tvOrderQuantity != null) {

            tvOrderQuantity.setText(
                    "Qty: " + quantity
            );
        }

        // =====================================================
        // DEFAULT BUTTONS
        // =====================================================

        if (btnCancelOrder != null) {

            btnCancelOrder.setVisibility(
                    View.GONE
            );

            btnCancelOrder.setEnabled(
                    false
            );
        }

        if (btnRateProduct != null) {

            btnRateProduct.setVisibility(
                    View.GONE
            );

            btnRateProduct.setEnabled(
                    false
            );

            // FIX:
            // setTextAllCaps() DOES NOT EXIST HERE.
            // Use setAllCaps(false).
            btnRateProduct.setAllCaps(
                    false
            );
        }

        if (btnComplaint != null) {

            btnComplaint.setVisibility(
                    View.GONE
            );

            btnComplaint.setEnabled(
                    false
            );

            btnComplaint.setText(
                    "Submit Complaint"
            );

            // FIX:
            // setTextAllCaps() replaced with setAllCaps().
            btnComplaint.setAllCaps(
                    false
            );

            btnComplaint.setTextColor(
                    Color.WHITE
            );

            btnComplaint.setBackgroundColor(
                    Color.rgb(
                            255,
                            152,
                            0
                    )
            );
        }

        if (tvComplaintStatus != null) {

            tvComplaintStatus.setVisibility(
                    View.GONE
            );

            tvComplaintStatus.setText(
                    ""
            );
        }

        // =====================================================
        // ORDER STATUS
        // =====================================================

        if (tvOrderStatus != null) {

            setOrderStatus(
                    tvOrderStatus,
                    cleanStatus
            );
        }

        // =====================================================
        // PENDING / NEW
        // =====================================================

        if (isPending(cleanStatus)) {

            if (btnCancelOrder != null) {

                btnCancelOrder.setVisibility(
                        View.VISIBLE
                );

                btnCancelOrder.setEnabled(
                        true
                );

                btnCancelOrder.setText(
                        "Cancel Order"
                );

                btnCancelOrder.setAllCaps(
                        false
                );

                btnCancelOrder.setOnClickListener(
                        v -> showCancelConfirmation(
                                document.getId(),
                                btnCancelOrder,
                                tvOrderStatus
                        )
                );
            }
        }

        // =====================================================
        // ACCEPTED
        // =====================================================

        else if (
                cleanStatus.equals(
                        "accepted"
                )
        ) {

            if (tvOrderStatus != null) {

                setAcceptedStyle(
                        tvOrderStatus
                );
            }

            // Complaint can be submitted
            // after seller accepts order.
            setupComplaint(
                    btnComplaint,
                    tvComplaintStatus,
                    productId,
                    productName,
                    sellerId,
                    sellerName,
                    orderId
            );
        }

        // =====================================================
        // SHIPPED
        // =====================================================

        else if (
                cleanStatus.equals(
                        "shipped"
                )
                        || cleanStatus.equals(
                        "shipment"
                )
        ) {

            if (tvOrderStatus != null) {

                setShipmentStyle(
                        tvOrderStatus,
                        "SHIPMENT"
                );
            }

            setupComplaint(
                    btnComplaint,
                    tvComplaintStatus,
                    productId,
                    productName,
                    sellerId,
                    sellerName,
                    orderId
            );
        }

        // =====================================================
        // DELIVERED
        // =====================================================

        else if (
                cleanStatus.equals(
                        "delivered"
                )
                        || cleanStatus.equals(
                        "completed"
                )
        ) {

            if (tvOrderStatus != null) {

                setDeliveredStyle(
                        tvOrderStatus
                );
            }

            // =================================================
            // RATE & REVIEW
            // =================================================

            checkAlreadyReviewed(
                    btnRateProduct,
                    productId,
                    productName,
                    sellerId,
                    orderId
            );

            // =================================================
            // COMPLAINT
            // =================================================

            setupComplaint(
                    btnComplaint,
                    tvComplaintStatus,
                    productId,
                    productName,
                    sellerId,
                    sellerName,
                    orderId
            );
        }

        // =====================================================
        // CANCELLED
        // =====================================================

        else if (
                cleanStatus.equals(
                        "cancelled"
                )
                        || cleanStatus.equals(
                        "canceled"
                )
                        || cleanStatus.equals(
                        "rejected"
                )
        ) {

            if (tvOrderStatus != null) {

                setCancelledStyle(
                        tvOrderStatus
                );
            }
        }

        // =====================================================
        // ADD CARD
        // =====================================================

        ordersContainer.addView(
                orderView
        );
    }

    // =========================================================
    // COMPLAINT SETUP
    // =========================================================

    private void setupComplaint(
            AppCompatButton complaintButton,
            TextView complaintStatusView,
            String productId,
            String productName,
            String sellerId,
            String sellerName,
            String orderId) {

        if (complaintButton == null) {
            return;
        }

        complaintButton.setVisibility(
                View.VISIBLE
        );

        complaintButton.setEnabled(
                false
        );

        complaintButton.setAllCaps(
                false
        );

        complaintButton.setText(
                "Checking Complaint..."
        );

        complaintButton.setTextColor(
                Color.WHITE
        );

        complaintButton.setBackgroundColor(
                Color.rgb(
                        255,
                        152,
                        0
                )
        );

        if (complaintStatusView != null) {

            complaintStatusView.setVisibility(
                    View.GONE
            );

            complaintStatusView.setText(
                    ""
            );
        }

        FirebaseUser currentUser =
                auth.getCurrentUser();

        if (currentUser == null) {

            complaintButton.setVisibility(
                    View.GONE
            );

            return;
        }

        String buyerId =
                currentUser.getUid();

        // =====================================================
        // IMPORTANT
        // COMPLAINT DOCUMENT ID = ORDER ID
        // =====================================================

        db.collection("complaints")
                .document(orderId)
                .addSnapshotListener(
                        (complaintDocument, error) -> {

                            if (!isAdded()) {
                                return;
                            }

                            // =================================================
                            // FIRESTORE ERROR
                            // =================================================

                            if (error != null) {

                                complaintButton.setVisibility(
                                        View.VISIBLE
                                );

                                complaintButton.setEnabled(
                                        true
                                );

                                complaintButton.setAllCaps(
                                        false
                                );

                                complaintButton.setText(
                                        "Submit Complaint"
                                );

                                complaintButton.setTextColor(
                                        Color.WHITE
                                );

                                complaintButton.setBackgroundColor(
                                        Color.rgb(
                                                255,
                                                152,
                                                0
                                        )
                                );

                                complaintButton.setOnClickListener(
                                        v ->
                                                showComplaintDialog(
                                                        productId,
                                                        productName,
                                                        sellerId,
                                                        sellerName,
                                                        orderId,
                                                        complaintButton,
                                                        complaintStatusView
                                                )
                                );

                                return;
                            }

                            // =================================================
                            // NO COMPLAINT
                            // =================================================

                            if (
                                    complaintDocument == null
                                            || !complaintDocument.exists()
                            ) {

                                complaintButton.setVisibility(
                                        View.VISIBLE
                                );

                                complaintButton.setEnabled(
                                        true
                                );

                                complaintButton.setAllCaps(
                                        false
                                );

                                complaintButton.setText(
                                        "Submit Complaint"
                                );

                                complaintButton.setTextColor(
                                        Color.WHITE
                                );

                                complaintButton.setBackgroundColor(
                                        Color.rgb(
                                                255,
                                                152,
                                                0
                                        )
                                );

                                complaintButton.setOnClickListener(
                                        v ->
                                                showComplaintDialog(
                                                        productId,
                                                        productName,
                                                        sellerId,
                                                        sellerName,
                                                        orderId,
                                                        complaintButton,
                                                        complaintStatusView
                                                )
                                );

                                if (complaintStatusView != null) {

                                    complaintStatusView.setVisibility(
                                            View.GONE
                                    );
                                }

                                return;
                            }

                            // =================================================
                            // COMPLAINT DATA
                            // =================================================

                            Map<String, Object> complaintData =
                                    complaintDocument.getData();

                            if (complaintData == null) {
                                return;
                            }

                            // =================================================
                            // BUYER SECURITY CHECK
                            // =================================================

                            String complaintBuyerId =
                                    getStringValue(
                                            complaintData,
                                            "buyerId",
                                            ""
                                    );

                            if (
                                    !complaintBuyerId.equals(
                                            buyerId
                                    )
                            ) {

                                complaintButton.setVisibility(
                                        View.GONE
                                );

                                if (
                                        complaintStatusView != null
                                ) {

                                    complaintStatusView.setVisibility(
                                            View.GONE
                                    );
                                }

                                return;
                            }

                            // =================================================
                            // COMPLAINT STATUS
                            // =================================================

                            String complaintStatus =
                                    getStringValue(
                                            complaintData,
                                            "status",
                                            "submitted"
                                    )
                                            .trim()
                                            .toLowerCase(
                                                    Locale.getDefault()
                                            );

                            // =================================================
                            // ACCEPTED / REVIEWED
                            // BUYER SIDE GREEN
                            // =================================================

                            if (
                                    complaintStatus.equals(
                                            "accepted"
                                    )
                                            || complaintStatus.equals(
                                            "reviewed"
                                    )
                                            || complaintStatus.equals(
                                            "resolved"
                                    )
                                            || complaintStatus.equals(
                                            "closed"
                                    )
                            ) {

                                complaintButton.setVisibility(
                                        View.VISIBLE
                                );

                                complaintButton.setEnabled(
                                        false
                                );

                                complaintButton.setAllCaps(
                                        false
                                );

                                complaintButton.setText(
                                        "Complaint Reviewed"
                                );

                                complaintButton.setTextColor(
                                        Color.WHITE
                                );

                                complaintButton.setBackgroundColor(
                                        Color.rgb(
                                                46,
                                                125,
                                                50
                                        )
                                );

                                if (
                                        complaintStatusView != null
                                ) {

                                    complaintStatusView.setVisibility(
                                            View.VISIBLE
                                    );

                                    complaintStatusView.setText(
                                            "COMPLAINT REVIEWED"
                                    );

                                    complaintStatusView.setTextColor(
                                            Color.WHITE
                                    );

                                    complaintStatusView.setGravity(
                                            Gravity.CENTER
                                    );

                                    complaintStatusView.setPadding(
                                            dpToPx(10),
                                            dpToPx(10),
                                            dpToPx(10),
                                            dpToPx(10)
                                    );

                                    complaintStatusView.setBackgroundColor(
                                            Color.rgb(
                                                    46,
                                                    125,
                                                    50
                                            )
                                    );
                                }

                                return;
                            }

                            // =================================================
                            // REJECTED
                            // =================================================

                            if (
                                    complaintStatus.equals(
                                            "rejected"
                                    )
                            ) {

                                complaintButton.setVisibility(
                                        View.VISIBLE
                                );

                                complaintButton.setEnabled(
                                        false
                                );

                                complaintButton.setAllCaps(
                                        false
                                );

                                complaintButton.setText(
                                        "Complaint Rejected"
                                );

                                complaintButton.setTextColor(
                                        Color.WHITE
                                );

                                complaintButton.setBackgroundColor(
                                        Color.rgb(
                                                198,
                                                40,
                                                40
                                        )
                                );

                                if (
                                        complaintStatusView != null
                                ) {

                                    showComplaintStatusText(
                                            complaintStatusView,
                                            complaintStatus
                                    );
                                }

                                return;
                            }

                            // =================================================
                            // SUBMITTED / UNDER REVIEW
                            // =================================================

                            complaintButton.setVisibility(
                                    View.VISIBLE
                            );

                            complaintButton.setEnabled(
                                    false
                            );

                            complaintButton.setAllCaps(
                                    false
                            );

                            complaintButton.setText(
                                    "Complaint Submitted"
                            );

                            complaintButton.setTextColor(
                                    Color.WHITE
                            );

                            complaintButton.setBackgroundColor(
                                    Color.rgb(
                                            255,
                                            152,
                                            0
                                    )
                            );

                            if (
                                    complaintStatusView != null
                            ) {

                                showComplaintStatusText(
                                        complaintStatusView,
                                        complaintStatus
                                );
                            }
                        }
                );
    }

    // =========================================================
    // COMPLAINT DIALOG
    // =========================================================

    private void showComplaintDialog(
            String productId,
            String productName,
            String sellerId,
            String sellerName,
            String orderId,
            AppCompatButton complaintButton,
            TextView complaintStatusView) {

        if (!isAdded()) {
            return;
        }

        // =====================================================
        // MAIN LAYOUT
        // =====================================================

        LinearLayout layout =
                new LinearLayout(
                        requireContext()
                );

        layout.setOrientation(
                LinearLayout.VERTICAL
        );

        layout.setPadding(
                dpToPx(20),
                dpToPx(5),
                dpToPx(20),
                dpToPx(5)
        );

        // =====================================================
        // PRODUCT
        // =====================================================

        TextView productText =
                new TextView(
                        requireContext()
                );

        productText.setText(
                "Product: " + productName
        );

        productText.setTextSize(
                15
        );

        productText.setTextColor(
                Color.rgb(
                        60,
                        60,
                        60
                )
        );

        productText.setPadding(
                0,
                dpToPx(5),
                0,
                dpToPx(10)
        );

        layout.addView(
                productText
        );

        // =====================================================
        // ORDER ID
        // =====================================================

        TextView orderText =
                new TextView(
                        requireContext()
                );

        orderText.setText(
                "Order ID: " + orderId
        );

        orderText.setTextSize(
                14
        );

        orderText.setTextColor(
                Color.rgb(
                        90,
                        90,
                        90
                )
        );

        orderText.setPadding(
                0,
                0,
                0,
                dpToPx(10)
        );

        layout.addView(
                orderText
        );

        // =====================================================
        // COMPLAINT INPUT
        // =====================================================

        EditText complaintInput =
                new EditText(
                        requireContext()
                );

        complaintInput.setHint(
                "Describe your complaint..."
        );

        complaintInput.setTextSize(
                14
        );

        complaintInput.setGravity(
                Gravity.TOP
        );

        complaintInput.setMinLines(
                5
        );

        complaintInput.setMaxLines(
                8
        );

        complaintInput.setInputType(
                InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                        | InputType.TYPE_TEXT_FLAG_MULTI_LINE
        );

        complaintInput.setPadding(
                dpToPx(12),
                dpToPx(12),
                dpToPx(12),
                dpToPx(12)
        );

        layout.addView(
                complaintInput,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dpToPx(130)
                )
        );

        // =====================================================
        // DIALOG
        // =====================================================

        AlertDialog dialog =
                new AlertDialog.Builder(
                        requireContext()
                )
                        .setTitle(
                                "Submit Complaint"
                        )
                        .setView(
                                layout
                        )
                        .setNegativeButton(
                                "Cancel",
                                null
                        )
                        .setPositiveButton(
                                "Submit",
                                null
                        )
                        .create();

        dialog.setOnShowListener(
                dialogInterface -> {

                    android.widget.Button submitButton =
                            dialog.getButton(
                                    AlertDialog.BUTTON_POSITIVE
                            );

                    submitButton.setOnClickListener(
                            v -> {

                                String complaintText =
                                        complaintInput
                                                .getText()
                                                .toString()
                                                .trim();

                                if (
                                        complaintText.isEmpty()
                                ) {

                                    complaintInput.setError(
                                            "Please enter your complaint."
                                    );

                                    complaintInput.requestFocus();

                                    return;
                                }

                                if (
                                        complaintText.length()
                                                > 500
                                ) {

                                    complaintInput.setError(
                                            "Complaint must be 500 characters or less."
                                    );

                                    return;
                                }

                                submitComplaint(
                                        productId,
                                        productName,
                                        sellerId,
                                        sellerName,
                                        orderId,
                                        complaintText,
                                        complaintButton,
                                        complaintStatusView,
                                        dialog
                                );
                            }
                    );
                }
        );

        dialog.show();
    }

    // =========================================================
    // SUBMIT COMPLAINT
    // =========================================================

    private void submitComplaint(
            String productId,
            String productName,
            String sellerId,
            String sellerName,
            String orderId,
            String complaintText,
            AppCompatButton complaintButton,
            TextView complaintStatusView,
            AlertDialog dialog) {

        if (!isAdded()) {
            return;
        }

        FirebaseUser currentUser =
                auth.getCurrentUser();

        if (currentUser == null) {

            Toast.makeText(
                    requireContext(),
                    "Please login first.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String buyerId =
                currentUser.getUid();

        // =====================================================
        // DISABLE BUTTON
        // =====================================================

        complaintButton.setEnabled(
                false
        );

        complaintButton.setText(
                "Submitting..."
        );

        // =====================================================
        // COMPLAINT DATA
        // =====================================================

        Map<String, Object> complaintData =
                new HashMap<>();

        complaintData.put(
                "complaintId",
                orderId
        );

        complaintData.put(
                "orderId",
                orderId
        );

        complaintData.put(
                "productId",
                productId
        );

        complaintData.put(
                "productName",
                productName
        );

        complaintData.put(
                "sellerId",
                sellerId
        );

        complaintData.put(
                "sellerName",
                sellerName
        );

        complaintData.put(
                "buyerId",
                buyerId
        );

        complaintData.put(
                "buyerName",
                getSafeBuyerName(
                        currentUser
                )
        );

        complaintData.put(
                "buyerEmail",
                getSafeValue(
                        currentUser.getEmail(),
                        ""
                )
        );

        // =====================================================
        // SAVE BOTH FIELD NAMES
        // =====================================================

        complaintData.put(
                "complaint",
                complaintText
        );

        complaintData.put(
                "complaintText",
                complaintText
        );

        // =====================================================
        // STATUS
        // =====================================================

        complaintData.put(
                "status",
                "submitted"
        );

        complaintData.put(
                "createdAt",
                Timestamp.now()
        );

        complaintData.put(
                "updatedAt",
                Timestamp.now()
        );

        // =====================================================
        // SAVE COMPLAINT
        // DOCUMENT ID = ORDER ID
        // =====================================================

        db.collection("complaints")
                .document(orderId)
                .set(
                        complaintData
                )
                .addOnSuccessListener(
                        unused -> {

                            if (!isAdded()) {
                                return;
                            }

                            if (
                                    dialog != null
                                            && dialog.isShowing()
                            ) {

                                dialog.dismiss();
                            }

                            // =================================================
                            // ORANGE BUTTON
                            // =================================================

                            complaintButton.setVisibility(
                                    View.VISIBLE
                            );

                            complaintButton.setEnabled(
                                    false
                            );

                            complaintButton.setAllCaps(
                                    false
                            );

                            complaintButton.setText(
                                    "Complaint Submitted"
                            );

                            complaintButton.setTextColor(
                                    Color.WHITE
                            );

                            complaintButton.setBackgroundColor(
                                    Color.rgb(
                                            255,
                                            152,
                                            0
                                    )
                            );

                            // =================================================
                            // STATUS
                            // =================================================

                            if (
                                    complaintStatusView != null
                            ) {

                                showComplaintStatusText(
                                        complaintStatusView,
                                        "submitted"
                                );
                            }

                            Toast.makeText(
                                    requireContext(),
                                    "Complaint submitted successfully.",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                )
                .addOnFailureListener(
                        e -> {

                            if (!isAdded()) {
                                return;
                            }

                            complaintButton.setEnabled(
                                    true
                            );

                            complaintButton.setAllCaps(
                                    false
                            );

                            complaintButton.setText(
                                    "Submit Complaint"
                            );

                            complaintButton.setBackgroundColor(
                                    Color.rgb(
                                            255,
                                            152,
                                            0
                                    )
                            );

                            Toast.makeText(
                                    requireContext(),
                                    "Failed to submit complaint: "
                                            + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }

    // =========================================================
    // COMPLAINT STATUS UI
    // =========================================================

    private void showComplaintStatusText(
            TextView statusView,
            String status) {

        if (statusView == null) {
            return;
        }

        if (
                status == null
                        || status.trim().isEmpty()
        ) {

            status = "submitted";
        }

        String cleanStatus =
                status.trim()
                        .toLowerCase(
                                Locale.getDefault()
                        );

        statusView.setVisibility(
                View.VISIBLE
        );

        statusView.setGravity(
                Gravity.CENTER
        );

        statusView.setPadding(
                dpToPx(10),
                dpToPx(10),
                dpToPx(10),
                dpToPx(10)
        );

        // =====================================================
        // SUBMITTED
        // =====================================================

        if (
                cleanStatus.equals(
                        "submitted"
                )
                        || cleanStatus.equals(
                        "pending"
                )
        ) {

            statusView.setText(
                    "COMPLAINT STATUS: SUBMITTED"
            );

            statusView.setTextColor(
                    Color.rgb(
                            102,
                            77,
                            3
                    )
            );

            statusView.setBackgroundColor(
                    Color.rgb(
                            255,
                            243,
                            205
                    )
            );

            return;
        }

        // =====================================================
        // UNDER REVIEW
        // =====================================================

        if (
                cleanStatus.equals(
                        "under_review"
                )
                        || cleanStatus.equals(
                        "under review"
                )
                        || cleanStatus.equals(
                        "review"
                )
        ) {

            statusView.setText(
                    "COMPLAINT STATUS: UNDER REVIEW"
            );

            statusView.setTextColor(
                    Color.WHITE
            );

            statusView.setBackgroundColor(
                    Color.rgb(
                            239,
                            108,
                            0
                    )
            );

            return;
        }

        // =====================================================
        // ACCEPTED / REVIEWED / RESOLVED
        // =====================================================

        if (
                cleanStatus.equals(
                        "accepted"
                )
                        || cleanStatus.equals(
                        "reviewed"
                )
                        || cleanStatus.equals(
                        "resolved"
                )
                        || cleanStatus.equals(
                        "closed"
                )
        ) {

            statusView.setText(
                    "COMPLAINT REVIEWED"
            );

            statusView.setTextColor(
                    Color.WHITE
            );

            statusView.setBackgroundColor(
                    Color.rgb(
                            46,
                            125,
                            50
                    )
            );

            return;
        }

        // =====================================================
        // REJECTED
        // =====================================================

        if (
                cleanStatus.equals(
                        "rejected"
                )
        ) {

            statusView.setText(
                    "COMPLAINT STATUS: REJECTED"
            );

            statusView.setTextColor(
                    Color.WHITE
            );

            statusView.setBackgroundColor(
                    Color.rgb(
                            198,
                            40,
                            40
                    )
            );

            return;
        }

        // =====================================================
        // OTHER
        // =====================================================

        statusView.setText(
                "COMPLAINT STATUS: "
                        + cleanStatus.toUpperCase(
                        Locale.getDefault()
                )
        );

        statusView.setTextColor(
                Color.WHITE
        );

        statusView.setBackgroundColor(
                Color.rgb(
                        97,
                        97,
                        97
                )
        );
    }

    // =========================================================
    // CANCEL CONFIRMATION
    // =========================================================

    private void showCancelConfirmation(
            String orderId,
            AppCompatButton cancelButton,
            TextView statusView) {

        if (!isAdded()) {
            return;
        }

        new AlertDialog.Builder(
                requireContext()
        )
                .setTitle(
                        "Cancel Order"
                )
                .setMessage(
                        "Are you sure you want to cancel this order?"
                )
                .setNegativeButton(
                        "No",
                        null
                )
                .setPositiveButton(
                        "Yes, Cancel",
                        (dialog, which) -> {

                            cancelButton.setEnabled(
                                    false
                            );

                            cancelButton.setText(
                                    "Cancelling..."
                            );

                            cancelBuyerOrder(
                                    orderId,
                                    cancelButton,
                                    statusView
                            );
                        }
                )
                .show();
    }

    // =========================================================
    // CANCEL BUYER ORDER
    // =========================================================

    private void cancelBuyerOrder(
            String orderId,
            AppCompatButton cancelButton,
            TextView statusView) {

        if (!isAdded()) {
            return;
        }

        FirebaseUser currentUser =
                auth.getCurrentUser();

        if (currentUser == null) {

            cancelButton.setEnabled(
                    true
            );

            cancelButton.setText(
                    "Cancel Order"
            );

            Toast.makeText(
                    requireContext(),
                    "Please login first.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        db.collection("orders")
                .document(orderId)
                .get()
                .addOnSuccessListener(
                        document -> {

                            if (!isAdded()) {
                                return;
                            }

                            if (!document.exists()) {

                                cancelButton.setEnabled(
                                        true
                                );

                                cancelButton.setText(
                                        "Cancel Order"
                                );

                                Toast.makeText(
                                        requireContext(),
                                        "Order not found.",
                                        Toast.LENGTH_SHORT
                                ).show();

                                return;
                            }

                            String buyerId =
                                    getSafeValue(
                                            document.getString(
                                                    "buyerId"
                                            ),
                                            ""
                                    );

                            String currentStatus =
                                    getSafeValue(
                                            document.getString(
                                                    "status"
                                            ),
                                            "pending"
                                    )
                                            .trim()
                                            .toLowerCase(
                                                    Locale.getDefault()
                                            );

                            // =================================================
                            // SECURITY
                            // =================================================

                            if (
                                    !buyerId.equals(
                                            currentUser.getUid()
                                    )
                            ) {

                                cancelButton.setEnabled(
                                        true
                                );

                                cancelButton.setText(
                                        "Cancel Order"
                                );

                                Toast.makeText(
                                        requireContext(),
                                        "You cannot cancel this order.",
                                        Toast.LENGTH_SHORT
                                ).show();

                                return;
                            }

                            // =================================================
                            // ONLY PENDING / NEW
                            // =================================================

                            if (!isPending(
                                    currentStatus
                            )) {

                                cancelButton.setEnabled(
                                        true
                                );

                                cancelButton.setText(
                                        "Cancel Order"
                                );

                                Toast.makeText(
                                        requireContext(),
                                        "This order can no longer be cancelled.",
                                        Toast.LENGTH_LONG
                                ).show();

                                return;
                            }

                            // =================================================
                            // UPDATE
                            // =================================================

                            Map<String, Object> updates =
                                    new HashMap<>();

                            updates.put(
                                    "status",
                                    "cancelled"
                            );

                            updates.put(
                                    "updatedAt",
                                    Timestamp.now()
                            );

                            updates.put(
                                    "cancelledBy",
                                    "buyer"
                            );

                            db.collection("orders")
                                    .document(orderId)
                                    .update(
                                            updates
                                    )
                                    .addOnSuccessListener(
                                            unused -> {

                                                if (!isAdded()) {
                                                    return;
                                                }

                                                if (
                                                        statusView != null
                                                ) {

                                                    setCancelledStyle(
                                                            statusView
                                                    );
                                                }

                                                cancelButton.setVisibility(
                                                        View.GONE
                                                );

                                                cancelButton.setEnabled(
                                                        false
                                                );

                                                Toast.makeText(
                                                        requireContext(),
                                                        "Order cancelled successfully.",
                                                        Toast.LENGTH_SHORT
                                                ).show();
                                            }
                                    )
                                    .addOnFailureListener(
                                            e -> {

                                                if (!isAdded()) {
                                                    return;
                                                }

                                                cancelButton.setEnabled(
                                                        true
                                                );

                                                cancelButton.setText(
                                                        "Cancel Order"
                                                );

                                                Toast.makeText(
                                                        requireContext(),
                                                        "Failed to cancel order: "
                                                                + e.getMessage(),
                                                        Toast.LENGTH_LONG
                                                ).show();
                                            }
                                    );
                        }
                )
                .addOnFailureListener(
                        e -> {

                            if (!isAdded()) {
                                return;
                            }

                            cancelButton.setEnabled(
                                    true
                            );

                            cancelButton.setText(
                                    "Cancel Order"
                            );

                            Toast.makeText(
                                    requireContext(),
                                    "Failed to check order: "
                                            + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }

    // =========================================================
    // ORDER STATUS
    // =========================================================

    private void setOrderStatus(
            TextView statusView,
            String status) {

        if (statusView == null) {
            return;
        }

        if (status == null) {
            status = "pending";
        }

        status =
                status.trim()
                        .toLowerCase(
                                Locale.getDefault()
                        );

        // =====================================================
        // PENDING
        // =====================================================

        if (isPending(status)) {

            statusView.setText(
                    "PENDING"
            );

            statusView.setTextColor(
                    Color.rgb(
                            102,
                            77,
                            3
                    )
            );

            statusView.setBackgroundColor(
                    Color.rgb(
                            255,
                            243,
                            205
                    )
            );

            return;
        }

        // =====================================================
        // ACCEPTED
        // =====================================================

        if (
                status.equals(
                        "accepted"
                )
        ) {

            setAcceptedStyle(
                    statusView
            );

            return;
        }

        // =====================================================
        // SHIPMENT
        // =====================================================

        if (
                status.equals(
                        "shipped"
                )
                        || status.equals(
                        "shipment"
                )
        ) {

            setShipmentStyle(
                    statusView,
                    "SHIPMENT"
            );

            return;
        }

        // =====================================================
        // DELIVERED
        // =====================================================

        if (
                status.equals(
                        "delivered"
                )
                        || status.equals(
                        "completed"
                )
        ) {

            setDeliveredStyle(
                    statusView
            );

            return;
        }

        // =====================================================
        // CANCELLED
        // =====================================================

        if (
                status.equals(
                        "cancelled"
                )
                        || status.equals(
                        "canceled"
                )
                        || status.equals(
                        "rejected"
                )
        ) {

            setCancelledStyle(
                    statusView
            );

            return;
        }

        // =====================================================
        // OTHER
        // =====================================================

        statusView.setText(
                status.toUpperCase(
                        Locale.getDefault()
                )
        );

        statusView.setTextColor(
                Color.WHITE
        );

        statusView.setBackgroundColor(
                Color.rgb(
                        97,
                        97,
                        97
                )
        );
    }

    // =========================================================
    // ACCEPTED STYLE
    // =========================================================

    private void setAcceptedStyle(
            TextView statusView) {

        statusView.setText(
                "ACCEPTED"
        );

        statusView.setTextColor(
                Color.WHITE
        );

        statusView.setBackgroundColor(
                Color.rgb(
                        46,
                        125,
                        50
                )
        );
    }

    // =========================================================
    // SHIPMENT STYLE
    // =========================================================

    private void setShipmentStyle(
            TextView statusView,
            String text) {

        statusView.setText(
                text
        );

        statusView.setTextColor(
                Color.WHITE
        );

        statusView.setBackgroundColor(
                Color.rgb(
                        239,
                        108,
                        0
                )
        );
    }

    // =========================================================
    // DELIVERED STYLE
    // =========================================================

    private void setDeliveredStyle(
            TextView statusView) {

        statusView.setText(
                "DELIVERED"
        );

        statusView.setTextColor(
                Color.WHITE
        );

        statusView.setBackgroundColor(
                Color.rgb(
                        46,
                        125,
                        50
                )
        );
    }

    // =========================================================
    // CANCELLED STYLE
    // =========================================================

    private void setCancelledStyle(
            TextView statusView) {

        statusView.setText(
                "CANCELLED"
        );

        statusView.setTextColor(
                Color.WHITE
        );

        statusView.setBackgroundColor(
                Color.rgb(
                        198,
                        40,
                        40
                )
        );
    }

    // =========================================================
    // CHECK ALREADY REVIEWED
    // =========================================================

    private void checkAlreadyReviewed(
            AppCompatButton btnRate,
            String productId,
            String productName,
            String sellerId,
            String orderId) {

        if (btnRate == null) {
            return;
        }

        FirebaseUser currentUser =
                auth.getCurrentUser();

        if (currentUser == null) {

            btnRate.setVisibility(
                    View.GONE
            );

            return;
        }

        String buyerId =
                currentUser.getUid();

        db.collection("reviews")
                .whereEqualTo(
                        "orderId",
                        orderId
                )
                .whereEqualTo(
                        "buyerId",
                        buyerId
                )
                .limit(1)
                .get()
                .addOnSuccessListener(
                        snapshots -> {

                            if (!isAdded()) {
                                return;
                            }

                            if (
                                    !snapshots.isEmpty()
                            ) {

                                setAlreadyRated(
                                        btnRate
                                );

                                btnRate.setVisibility(
                                        View.VISIBLE
                                );

                                return;
                            }

                            btnRate.setText(
                                    "Rate & Review Product"
                            );

                            btnRate.setAllCaps(
                                    false
                            );

                            btnRate.setEnabled(
                                    true
                            );

                            btnRate.setVisibility(
                                    View.VISIBLE
                            );

                            btnRate.setTextColor(
                                    Color.WHITE
                            );

                            btnRate.setBackgroundColor(
                                    Color.rgb(
                                            76,
                                            175,
                                            80
                                    )
                            );

                            btnRate.setOnClickListener(
                                    v ->
                                            showRatingDialog(
                                                    productId,
                                                    productName,
                                                    sellerId,
                                                    orderId,
                                                    btnRate
                                            )
                            );
                        }
                )
                .addOnFailureListener(
                        e -> {

                            if (!isAdded()) {
                                return;
                            }

                            btnRate.setVisibility(
                                    View.GONE
                            );
                        }
                );
    }

    // =========================================================
    // ALREADY RATED
    // =========================================================

    private void setAlreadyRated(
            AppCompatButton btnRate) {

        btnRate.setText(
                "Already Rated"
        );

        btnRate.setAllCaps(
                false
        );

        btnRate.setEnabled(
                false
        );

        btnRate.setTextColor(
                Color.rgb(
                        80,
                        80,
                        80
                )
        );

        btnRate.setBackgroundColor(
                Color.rgb(
                        224,
                        224,
                        224
                )
        );
    }

    // =========================================================
    // RATING DIALOG
    // =========================================================

    private void showRatingDialog(
            String productId,
            String productName,
            String sellerId,
            String orderId,
            AppCompatButton rateButton) {

        if (!isAdded()) {
            return;
        }

        LinearLayout layout =
                new LinearLayout(
                        requireContext()
                );

        layout.setOrientation(
                LinearLayout.VERTICAL
        );

        layout.setPadding(
                dpToPx(20),
                dpToPx(10),
                dpToPx(20),
                dpToPx(5)
        );

        // =====================================================
        // PRODUCT NAME
        // =====================================================

        TextView title =
                new TextView(
                        requireContext()
                );

        title.setText(
                productName
        );

        title.setTextSize(
                18
        );

        title.setTextColor(
                Color.rgb(
                        34,
                        34,
                        34
                )
        );

        title.setGravity(
                Gravity.CENTER
        );

        title.setPadding(
                0,
                dpToPx(5),
                0,
                dpToPx(10)
        );

        layout.addView(
                title
        );

        // =====================================================
        // RATING BAR
        // =====================================================

        RatingBar ratingBar =
                new RatingBar(
                        requireContext()
                );

        ratingBar.setNumStars(
                5
        );

        ratingBar.setStepSize(
                1.0f
        );

        ratingBar.setRating(
                5.0f
        );

        ratingBar.setIsIndicator(
                false
        );

        LinearLayout.LayoutParams ratingParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        ratingParams.gravity =
                Gravity.CENTER;

        ratingParams.topMargin =
                dpToPx(5);

        ratingParams.bottomMargin =
                dpToPx(15);

        layout.addView(
                ratingBar,
                ratingParams
        );

        // =====================================================
        // REVIEW INPUT
        // =====================================================

        EditText reviewInput =
                new EditText(
                        requireContext()
                );

        reviewInput.setHint(
                "Write a short review..."
        );

        reviewInput.setTextSize(
                14
        );

        reviewInput.setGravity(
                Gravity.TOP
        );

        reviewInput.setMinLines(
                3
        );

        reviewInput.setMaxLines(
                5
        );

        reviewInput.setInputType(
                InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                        | InputType.TYPE_TEXT_FLAG_MULTI_LINE
        );

        reviewInput.setPadding(
                dpToPx(12),
                dpToPx(12),
                dpToPx(12),
                dpToPx(12)
        );

        layout.addView(
                reviewInput,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dpToPx(100)
                )
        );

        // =====================================================
        // DIALOG
        // =====================================================

        AlertDialog dialog =
                new AlertDialog.Builder(
                        requireContext()
                )
                        .setTitle(
                                "Rate Product"
                        )
                        .setView(
                                layout
                        )
                        .setNegativeButton(
                                "Cancel",
                                null
                        )
                        .setPositiveButton(
                                "Submit Review",
                                null
                        )
                        .create();

        dialog.setOnShowListener(
                dialogInterface -> {

                    android.widget.Button submitButton =
                            dialog.getButton(
                                    AlertDialog.BUTTON_POSITIVE
                            );

                    submitButton.setOnClickListener(
                            v -> {

                                float rating =
                                        ratingBar.getRating();

                                String reviewText =
                                        reviewInput
                                                .getText()
                                                .toString()
                                                .trim();

                                if (rating <= 0) {

                                    Toast.makeText(
                                            requireContext(),
                                            "Please select a rating.",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    return;
                                }

                                if (
                                        reviewText.length()
                                                > 300
                                ) {

                                    reviewInput.setError(
                                            "Review must be 300 characters or less."
                                    );

                                    return;
                                }

                                submitReview(
                                        productId,
                                        productName,
                                        sellerId,
                                        orderId,
                                        rating,
                                        reviewText,
                                        rateButton,
                                        dialog
                                );
                            }
                    );
                }
        );

        dialog.show();
    }

    // =========================================================
    // SUBMIT REVIEW
    // =========================================================

    private void submitReview(
            String productId,
            String productName,
            String sellerId,
            String orderId,
            float rating,
            String reviewText,
            AppCompatButton rateButton,
            AlertDialog dialog) {

        if (!isAdded()) {
            return;
        }

        FirebaseUser currentUser =
                auth.getCurrentUser();

        if (currentUser == null) {

            Toast.makeText(
                    requireContext(),
                    "Please login first.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String buyerId =
                currentUser.getUid();

        rateButton.setEnabled(
                false
        );

        rateButton.setText(
                "Submitting..."
        );

        Map<String, Object> reviewData =
                new HashMap<>();

        reviewData.put(
                "orderId",
                orderId
        );

        reviewData.put(
                "productId",
                productId
        );

        reviewData.put(
                "productName",
                productName
        );

        reviewData.put(
                "sellerId",
                sellerId
        );

        reviewData.put(
                "buyerId",
                buyerId
        );

        reviewData.put(
                "buyerName",
                getSafeBuyerName(
                        currentUser
                )
        );

        reviewData.put(
                "buyerEmail",
                getSafeValue(
                        currentUser.getEmail(),
                        ""
                )
        );

        reviewData.put(
                "rating",
                rating
        );

        reviewData.put(
                "review",
                reviewText
        );

        reviewData.put(
                "createdAt",
                Timestamp.now()
        );

        reviewData.put(
                "updatedAt",
                Timestamp.now()
        );

        reviewData.put(
                "status",
                "published"
        );

        db.collection("reviews")
                .add(
                        reviewData
                )
                .addOnSuccessListener(
                        documentReference -> {

                            if (!isAdded()) {
                                return;
                            }

                            dialog.dismiss();

                            setAlreadyRated(
                                    rateButton
                            );

                            Toast.makeText(
                                    requireContext(),
                                    "Review submitted successfully.",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                )
                .addOnFailureListener(
                        e -> {

                            if (!isAdded()) {
                                return;
                            }

                            rateButton.setEnabled(
                                    true
                            );

                            rateButton.setAllCaps(
                                    false
                            );

                            rateButton.setText(
                                    "Rate & Review Product"
                            );

                            rateButton.setTextColor(
                                    Color.WHITE
                            );

                            rateButton.setBackgroundColor(
                                    Color.rgb(
                                            76,
                                            175,
                                            80
                                    )
                            );

                            Toast.makeText(
                                    requireContext(),
                                    "Failed to submit review: "
                                            + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }

    // =========================================================
    // CREATED TIME
    // =========================================================

    private long getCreatedTime(
            Map<String, Object> data) {

        if (data == null) {
            return 0L;
        }

        Object createdAt =
                data.get(
                        "createdAt"
                );

        if (
                createdAt instanceof Timestamp
        ) {

            return ((Timestamp) createdAt)
                    .toDate()
                    .getTime();
        }

        if (
                createdAt instanceof Date
        ) {

            return ((Date) createdAt)
                    .getTime();
        }

        if (
                createdAt instanceof Number
        ) {

            return ((Number) createdAt)
                    .longValue();
        }

        if (createdAt != null) {

            try {

                return Long.parseLong(
                        String.valueOf(
                                createdAt
                        )
                );

            } catch (Exception ignored) {
            }
        }

        Object orderDate =
                data.get(
                        "orderDate"
                );

        if (
                orderDate instanceof Timestamp
        ) {

            return ((Timestamp) orderDate)
                    .toDate()
                    .getTime();
        }

        if (
                orderDate instanceof Date
        ) {

            return ((Date) orderDate)
                    .getTime();
        }

        if (
                orderDate instanceof Number
        ) {

            return ((Number) orderDate)
                    .longValue();
        }

        return 0L;
    }

    // =========================================================
    // ORDER DATE
    // =========================================================

    private String getOrderDate(
            Map<String, Object> data) {

        long time =
                getCreatedTime(
                        data
                );

        if (time == 0L) {

            return "Date not available";
        }

        Date date =
                new Date(
                        time
                );

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
    // STRING VALUE
    // =========================================================

    private String getStringValue(
            Map<String, Object> data,
            String key,
            String defaultValue) {

        if (data == null) {
            return defaultValue;
        }

        Object value =
                data.get(
                        key
                );

        if (value == null) {
            return defaultValue;
        }

        String result =
                String.valueOf(
                        value
                ).trim();

        if (
                TextUtils.isEmpty(
                        result
                )
        ) {

            return defaultValue;
        }

        return result;
    }

    // =========================================================
    // SAFE VALUE
    // =========================================================

    private String getSafeValue(
            String value,
            String defaultValue) {

        if (
                value == null
                        || value.trim().isEmpty()
        ) {

            return defaultValue;
        }

        return value.trim();
    }

    // =========================================================
    // BUYER NAME
    // =========================================================

    private String getSafeBuyerName(
            FirebaseUser user) {

        if (user == null) {
            return "Buyer";
        }

        if (
                user.getDisplayName() != null
                        && !user.getDisplayName()
                        .trim()
                        .isEmpty()
        ) {

            return user.getDisplayName()
                    .trim();
        }

        return "Buyer";
    }

    // =========================================================
    // PENDING CHECK
    // =========================================================

    private boolean isPending(
            String status) {

        if (status == null) {
            return true;
        }

        return status.equals(
                "pending"
        )
                || status.equals(
                "new"
        )
                || status.isEmpty();
    }

    // =========================================================
    // DP TO PX
    // =========================================================

    private int dpToPx(
            int dp) {

        float density =
                requireContext()
                        .getResources()
                        .getDisplayMetrics()
                        .density;

        return Math.round(
                dp * density
        );
    }

    // =========================================================
    // NO ORDERS
    // =========================================================

    private void showNoOrders(
            String message) {

        if (!isAdded()) {
            return;
        }

        if (ordersContainer == null) {
            return;
        }

        ordersContainer.removeAllViews();

        TextView textView =
                new TextView(
                        requireContext()
                );

        textView.setLayoutParams(
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        textView.setText(
                message
        );

        textView.setTextSize(
                16
        );

        textView.setTextColor(
                Color.GRAY
        );

        textView.setGravity(
                Gravity.CENTER
        );

        textView.setPadding(
                dpToPx(20),
                dpToPx(50),
                dpToPx(20),
                dpToPx(50)
        );

        ordersContainer.addView(
                textView
        );
    }

    // =========================================================
    // RESUME
    // =========================================================

    @Override
    public void onResume() {

        super.onResume();

        if (
                isAdded()
                        && db != null
                        && auth != null
        ) {

            loadBuyerOrders();
        }
    }
}