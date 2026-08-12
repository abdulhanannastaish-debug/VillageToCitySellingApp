package com.example.villagetocityreseilingapp.ui.main.seller;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.example.villagetocityreseilingapp.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class SellerOrderFragment extends Fragment {

    // =========================================================
    // FIREBASE
    // =========================================================

    private FirebaseFirestore db;

    // =========================================================
    // VIEWS
    // =========================================================

    private ViewGroup orderContentContainer;

    private TextView tabAll;
    private TextView tabActive;
    private TextView tabDelivered;
    private TextView tabCancelled;

    // =========================================================
    // CURRENT FILTER
    // =========================================================

    private String currentFilter = "all";

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public SellerOrderFragment() {
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
                R.layout.fragment_seller_orders,
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

        db = FirebaseFirestore.getInstance();

        // =====================================================
        // FIND VIEWS
        // =====================================================

        orderContentContainer =
                view.findViewById(R.id.orderContentContainer);

        tabAll =
                view.findViewById(R.id.tabAll);

        tabActive =
                view.findViewById(R.id.tabActive);

        tabDelivered =
                view.findViewById(R.id.tabDelivered);

        tabCancelled =
                view.findViewById(R.id.tabCancelled);

        // =====================================================
        // ALL
        // =====================================================

        tabAll.setOnClickListener(v -> {

            currentFilter = "all";

            updateTabColors();

            loadOrders();
        });

        // =====================================================
        // ACTIVE
        // =====================================================

        tabActive.setOnClickListener(v -> {

            currentFilter = "active";

            updateTabColors();

            loadOrders();
        });

        // =====================================================
        // DELIVERED
        // =====================================================

        tabDelivered.setOnClickListener(v -> {

            currentFilter = "delivered";

            updateTabColors();

            loadOrders();
        });

        // =====================================================
        // CANCELLED
        // =====================================================

        tabCancelled.setOnClickListener(v -> {

            currentFilter = "cancelled";

            updateTabColors();

            loadOrders();
        });

        // =====================================================
        // INITIAL
        // =====================================================

        updateTabColors();

        loadOrders();
    }

    // =========================================================
    // TAB COLORS
    // =========================================================

    private void updateTabColors() {

        tabAll.setTextColor(
                getResources().getColor(
                        android.R.color.darker_gray
                )
        );

        tabActive.setTextColor(
                getResources().getColor(
                        android.R.color.darker_gray
                )
        );

        tabDelivered.setTextColor(
                getResources().getColor(
                        android.R.color.darker_gray
                )
        );

        tabCancelled.setTextColor(
                getResources().getColor(
                        android.R.color.darker_gray
                )
        );

        if (currentFilter.equals("all")) {

            tabAll.setTextColor(
                    getResources().getColor(
                            R.color.green
                    )
            );

        } else if (currentFilter.equals("active")) {

            tabActive.setTextColor(
                    getResources().getColor(
                            R.color.green
                    )
            );

        } else if (currentFilter.equals("delivered")) {

            tabDelivered.setTextColor(
                    getResources().getColor(
                            R.color.green
                    )
            );

        } else if (currentFilter.equals("cancelled")) {

            tabCancelled.setTextColor(
                    getResources().getColor(
                            R.color.green
                    )
            );
        }
    }

    // =========================================================
    // LOAD ORDERS
    // =========================================================

    private void loadOrders() {

        if (!isAdded()) {
            return;
        }

        orderContentContainer.removeAllViews();

        db.collection("orders")
                .orderBy(
                        "orderDate",
                        Query.Direction.DESCENDING
                )
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    if (!isAdded()) {
                        return;
                    }

                    if (queryDocumentSnapshots.isEmpty()) {

                        showNoOrdersMessage();

                        return;
                    }

                    boolean foundOrder = false;

                    for (DocumentSnapshot document :
                            queryDocumentSnapshots.getDocuments()) {

                        String status =
                                document.getString("status");

                        if (status == null ||
                                status.trim().isEmpty()) {

                            status = "new";
                        }

                        status = status
                                .toLowerCase()
                                .trim();

                        if (!matchesFilter(status)) {
                            continue;
                        }

                        foundOrder = true;

                        addOrderCard(document);
                    }

                    if (!foundOrder) {

                        showNoOrdersMessage();
                    }

                })
                .addOnFailureListener(e -> {

                    if (!isAdded()) {
                        return;
                    }

                    Toast.makeText(
                            requireContext(),
                            "Failed to load orders: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();

                    showNoOrdersMessage();
                });
    }

    // =========================================================
    // FILTER
    // =========================================================

    private boolean matchesFilter(
            String status) {

        if (status == null) {
            status = "new";
        }

        status = status
                .toLowerCase()
                .trim();

        // =====================================================
        // ALL
        // =====================================================

        if (currentFilter.equals("all")) {

            return true;
        }

        // =====================================================
        // ACTIVE
        //
        // New
        // Pending
        // Processing
        // Shipped
        // =====================================================

        if (currentFilter.equals("active")) {

            return status.equals("new")
                    || status.equals("pending")
                    || status.equals("processing")
                    || status.equals("shipped");
        }

        // =====================================================
        // DELIVERED
        // =====================================================

        if (currentFilter.equals("delivered")) {

            return status.equals("delivered")
                    || status.equals("completed");
        }

        // =====================================================
        // CANCELLED
        // =====================================================

        if (currentFilter.equals("cancelled")) {

            return status.equals("cancelled")
                    || status.equals("canceled");
        }

        return false;
    }

    // =========================================================
    // ADD ORDER CARD
    // =========================================================

    private void addOrderCard(
            DocumentSnapshot document) {

        if (!isAdded()) {
            return;
        }

        // =====================================================
        // INFLATE
        // =====================================================

        View orderView =
                LayoutInflater.from(requireContext())
                        .inflate(
                                R.layout.fragment_seller_order_card,
                                orderContentContainer,
                                false
                        );

        // =====================================================
        // FIND VIEWS
        // =====================================================

        TextView txtOrderId =
                orderView.findViewById(
                        R.id.txtOrderId
                );

        TextView txtOrderDate =
                orderView.findViewById(
                        R.id.txtOrderDate
                );

        TextView txtOrderStatus =
                orderView.findViewById(
                        R.id.txtOrderStatus
                );

        TextView txtProductName =
                orderView.findViewById(
                        R.id.txtProductName
                );

        TextView txtQuantity =
                orderView.findViewById(
                        R.id.txtQuantity
                );

        TextView txtPrice =
                orderView.findViewById(
                        R.id.txtPrice
                );

        View btnOrderDetails =
                orderView.findViewById(
                        R.id.btnOrderDetails
                );

        View btnOrderStatus =
                orderView.findViewById(
                        R.id.btnOrderStatus
                );

        // =====================================================
        // FIRESTORE DATA
        // =====================================================

        String firestoreOrderId =
                document.getString("id");

        String orderDate =
                document.getString("orderDate");

        String productName =
                document.getString("productName");

        String quantity =
                document.getString("quantity");

        String amount =
                document.getString("amount");

        String firestoreStatus =
                document.getString("status");

        // =====================================================
        // ORDER ID
        // =====================================================

        final String finalOrderId;

        if (firestoreOrderId == null ||
                firestoreOrderId.trim().isEmpty()) {

            finalOrderId = document.getId();

        } else {

            finalOrderId = firestoreOrderId;
        }

        // =====================================================
        // STATUS
        // =====================================================

        final String finalStatus;

        if (firestoreStatus == null ||
                firestoreStatus.trim().isEmpty()) {

            finalStatus = "new";

        } else {

            finalStatus = firestoreStatus
                    .toLowerCase()
                    .trim();
        }

        // =====================================================
        // BASIC DATA
        // =====================================================

        txtOrderId.setText(
                "Order #" + finalOrderId
        );

        if (orderDate != null &&
                !orderDate.trim().isEmpty()) {

            txtOrderDate.setText(
                    orderDate
            );

        } else {

            txtOrderDate.setText(
                    "Date not available"
            );
        }

        if (productName != null &&
                !productName.trim().isEmpty()) {

            txtProductName.setText(
                    productName
            );

        } else {

            txtProductName.setText(
                    "Product"
            );
        }

        if (quantity != null &&
                !quantity.trim().isEmpty()) {

            txtQuantity.setText(
                    "Quantity: " + quantity
            );

        } else {

            txtQuantity.setText(
                    "Quantity: 1"
            );
        }

        if (amount != null &&
                !amount.trim().isEmpty()) {

            txtPrice.setText(
                    "Rs. " + amount
            );

        } else {

            txtPrice.setText(
                    "Rs. 0"
            );
        }

        // =====================================================
        // STATUS DISPLAY
        // =====================================================

        txtOrderStatus.setText(
                formatStatus(finalStatus)
        );

        // =====================================================
        // DETAILS BUTTON
        // =====================================================

        btnOrderDetails.setVisibility(
                View.VISIBLE
        );

        btnOrderDetails.setOnClickListener(v -> {

            openOrderDetails(
                    document.getId()
            );
        });

        // =====================================================
        // NEW
        // =====================================================

        if (finalStatus.equals("new")) {

            setStatusButton(
                    btnOrderStatus,
                    "Accept Order"
            );

            btnOrderStatus.setOnClickListener(v -> {

                updateOrderStatus(
                        document.getId(),
                        "pending"
                );
            });

            addCancelButton(
                    orderView,
                    document.getId()
            );

        }

        // =====================================================
        // PENDING
        // =====================================================

        else if (finalStatus.equals("pending")) {

            setStatusButton(
                    btnOrderStatus,
                    "Start Processing"
            );

            btnOrderStatus.setOnClickListener(v -> {

                updateOrderStatus(
                        document.getId(),
                        "processing"
                );
            });
        }

        // =====================================================
        // PROCESSING
        // =====================================================

        else if (finalStatus.equals("processing")) {

            setStatusButton(
                    btnOrderStatus,
                    "Mark as Shipped"
            );

            btnOrderStatus.setOnClickListener(v -> {

                updateOrderStatus(
                        document.getId(),
                        "shipped"
                );
            });
        }

        // =====================================================
        // SHIPPED
        // =====================================================

        else if (finalStatus.equals("shipped")) {

            setStatusButton(
                    btnOrderStatus,
                    "Mark as Delivered"
            );

            btnOrderStatus.setOnClickListener(v -> {

                updateOrderStatus(
                        document.getId(),
                        "delivered"
                );
            });
        }

        // =====================================================
        // DELIVERED
        // =====================================================

        else if (finalStatus.equals("delivered") ||
                finalStatus.equals("completed")) {

            setStatusButton(
                    btnOrderStatus,
                    "Delivered"
            );

            btnOrderStatus.setOnClickListener(v -> {

                Toast.makeText(
                        requireContext(),
                        "Order Delivered",
                        Toast.LENGTH_SHORT
                ).show();
            });
        }

        // =====================================================
        // CANCELLED
        // =====================================================

        else if (finalStatus.equals("cancelled") ||
                finalStatus.equals("canceled")) {

            setStatusButton(
                    btnOrderStatus,
                    "Cancelled"
            );

            btnOrderStatus.setOnClickListener(v -> {

                Toast.makeText(
                        requireContext(),
                        "Order Cancelled",
                        Toast.LENGTH_SHORT
                ).show();
            });
        }

        // =====================================================
        // OTHER STATUS
        // =====================================================

        else {

            setStatusButton(
                    btnOrderStatus,
                    formatStatus(finalStatus)
            );
        }

        // =====================================================
        // ADD CARD
        // =====================================================

        orderContentContainer.addView(
                orderView
        );
    }

    // =========================================================
    // SET STATUS BUTTON
    // =========================================================

    private void setStatusButton(
            View button,
            String text) {

        button.setVisibility(
                View.VISIBLE
        );

        if (button instanceof AppCompatButton) {

            ((AppCompatButton) button)
                    .setText(text);
        }
    }

    // =========================================================
    // ADD CANCEL BUTTON
    // =========================================================

    private void addCancelButton(
            View orderView,
            String orderId) {

        if (!(orderView instanceof ViewGroup)) {
            return;
        }

        ViewGroup root =
                (ViewGroup) orderView;

        TextView cancelButton =
                new TextView(requireContext());

        cancelButton.setText(
                "Cancel Order"
        );

        cancelButton.setTextSize(
                14
        );

        cancelButton.setGravity(
                Gravity.CENTER
        );

        cancelButton.setTextColor(
                getResources().getColor(
                        android.R.color.white
                )
        );

        cancelButton.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );

        cancelButton.setBackgroundColor(
                android.graphics.Color.rgb(
                        229,
                        57,
                        53
                )
        );

        cancelButton.setPadding(
                10,
                10,
                10,
                10
        );

        android.widget.LinearLayout.LayoutParams params =
                new android.widget.LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        52
                );

        params.setMargins(
                16,
                8,
                16,
                16
        );

        cancelButton.setLayoutParams(
                params
        );

        cancelButton.setOnClickListener(v -> {

            cancelOrder(
                    orderId
            );
        });

        root.addView(
                cancelButton
        );
    }

    // =========================================================
    // UPDATE ORDER STATUS
    // =========================================================

    private void updateOrderStatus(
            String orderId,
            String newStatus) {

        if (!isAdded()) {
            return;
        }

        db.collection("orders")
                .document(orderId)
                .update(
                        "status",
                        newStatus
                )
                .addOnSuccessListener(unused -> {

                    if (!isAdded()) {
                        return;
                    }

                    Toast.makeText(
                            requireContext(),
                            "Order status updated to "
                                    + formatStatus(newStatus),
                            Toast.LENGTH_SHORT
                    ).show();

                    loadOrders();
                })
                .addOnFailureListener(e -> {

                    if (!isAdded()) {
                        return;
                    }

                    Toast.makeText(
                            requireContext(),
                            "Failed to update order: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    // =========================================================
    // CANCEL ORDER
    // =========================================================

    private void cancelOrder(
            String orderId) {

        if (!isAdded()) {
            return;
        }

        db.collection("orders")
                .document(orderId)
                .update(
                        "status",
                        "cancelled"
                )
                .addOnSuccessListener(unused -> {

                    if (!isAdded()) {
                        return;
                    }

                    Toast.makeText(
                            requireContext(),
                            "Order cancelled successfully.",
                            Toast.LENGTH_SHORT
                    ).show();

                    loadOrders();
                })
                .addOnFailureListener(e -> {

                    if (!isAdded()) {
                        return;
                    }

                    Toast.makeText(
                            requireContext(),
                            "Failed to cancel order: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    // =========================================================
    // OPEN ORDER DETAILS
    // =========================================================

    private void openOrderDetails(
            String orderId) {

        if (!isAdded()) {
            return;
        }

        SellerOrderDetailFragment fragment =
                SellerOrderDetailFragment.newInstance(
                        orderId
                );

        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .replace(
                        R.id.fragment_container,
                        fragment
                )
                .addToBackStack(null)
                .commit();
    }

    // =========================================================
    // FORMAT STATUS
    // =========================================================

    private String formatStatus(
            String status) {

        if (status == null) {
            return "New";
        }

        status = status.trim();

        if (status.equalsIgnoreCase("new")) {
            return "New";
        }

        if (status.equalsIgnoreCase("pending")) {
            return "Pending";
        }

        if (status.equalsIgnoreCase("processing")) {
            return "Processing";
        }

        if (status.equalsIgnoreCase("shipped")) {
            return "Shipped";
        }

        if (status.equalsIgnoreCase("completed") ||
                status.equalsIgnoreCase("delivered")) {

            return "Delivered";
        }

        if (status.equalsIgnoreCase("cancelled") ||
                status.equalsIgnoreCase("canceled")) {

            return "Cancelled";
        }

        if (status.equalsIgnoreCase("accepted")) {
            return "Accepted";
        }

        if (status.equalsIgnoreCase("confirmed")) {
            return "Confirmed";
        }

        return status;
    }

    // =========================================================
    // NO ORDERS
    // =========================================================

    private void showNoOrdersMessage() {

        if (!isAdded()) {
            return;
        }

        TextView noOrders =
                new TextView(requireContext());

        noOrders.setText(
                "No orders found"
        );

        noOrders.setTextSize(
                16
        );

        noOrders.setTextColor(
                getResources().getColor(
                        android.R.color.darker_gray
                )
        );

        noOrders.setGravity(
                Gravity.CENTER
        );

        noOrders.setPadding(
                20,
                60,
                20,
                60
        );

        orderContentContainer.addView(
                noOrders
        );
    }

    // =========================================================
    // RELOAD
    // =========================================================

    @Override
    public void onResume() {

        super.onResume();

        if (db != null &&
                orderContentContainer != null) {

            loadOrders();
        }
    }
}