package com.example.villagetocityreseilingapp.ui.main.seller;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.example.villagetocityreseilingapp.R;
import com.example.villagetocityreseilingapp.ui.main.buyer.BuyerNotificationHelper;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SellerOrderFragment extends Fragment {

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private ListenerRegistration ordersListener;

    private ViewGroup orderContentContainer;

    private TextView tabAll;
    private TextView tabActive;
    private TextView tabDelivered;
    private TextView tabCancelled;

    private String currentFilter = "all";

    public SellerOrderFragment() {
        // Required empty public constructor
    }

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

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

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

        if (tabAll != null) {
            tabAll.setOnClickListener(v -> {
                currentFilter = "all";
                updateTabColors();
                startOrdersListener();
            });
        }

        if (tabActive != null) {
            tabActive.setOnClickListener(v -> {
                currentFilter = "active";
                updateTabColors();
                startOrdersListener();
            });
        }

        if (tabDelivered != null) {
            tabDelivered.setOnClickListener(v -> {
                currentFilter = "delivered";
                updateTabColors();
                startOrdersListener();
            });
        }

        if (tabCancelled != null) {
            tabCancelled.setOnClickListener(v -> {
                currentFilter = "cancelled";
                updateTabColors();
                startOrdersListener();
            });
        }

        updateTabColors();
        startOrdersListener();
    }

    // =========================================================
    // TAB COLORS
    // =========================================================

    private void updateTabColors() {

        if (tabAll == null ||
                tabActive == null ||
                tabDelivered == null ||
                tabCancelled == null) {
            return;
        }

        int gray = Color.rgb(100, 100, 100);
        int green = Color.rgb(46, 125, 50);

        tabAll.setTextColor(gray);
        tabActive.setTextColor(gray);
        tabDelivered.setTextColor(gray);
        tabCancelled.setTextColor(gray);

        if (currentFilter.equals("all")) {
            tabAll.setTextColor(green);

        } else if (currentFilter.equals("active")) {
            tabActive.setTextColor(green);

        } else if (currentFilter.equals("delivered")) {
            tabDelivered.setTextColor(green);

        } else if (currentFilter.equals("cancelled")) {
            tabCancelled.setTextColor(green);
        }
    }

    // =========================================================
    // START LISTENER
    // =========================================================

    private void startOrdersListener() {

        if (!isAdded() ||
                db == null ||
                auth == null ||
                orderContentContainer == null) {
            return;
        }

        removeOrdersListener();

        FirebaseUser currentUser =
                auth.getCurrentUser();

        if (currentUser == null) {

            orderContentContainer.removeAllViews();

            showNoOrdersMessage();

            return;
        }

        String sellerId =
                currentUser.getUid();

        orderContentContainer.removeAllViews();

        ordersListener =
                db.collection("orders")
                        .whereEqualTo("sellerId", sellerId)
                        .addSnapshotListener(
                                (snapshots, error) -> {

                                    if (!isAdded()) {
                                        return;
                                    }

                                    if (error != null) {

                                        orderContentContainer
                                                .removeAllViews();

                                        showToast(
                                                getString(
                                                        R.string.failed_load_orders
                                                )
                                                        + " "
                                                        + error.getMessage()
                                        );

                                        showNoOrdersMessage();

                                        return;
                                    }

                                    if (snapshots == null) {

                                        showNoOrdersMessage();

                                        return;
                                    }

                                    List<DocumentSnapshot> documents =
                                            new ArrayList<>(
                                                    snapshots.getDocuments()
                                            );

                                    documents.sort(
                                            (a, b) ->
                                                    Long.compare(
                                                            getOrderDateValue(b),
                                                            getOrderDateValue(a)
                                                    )
                                    );

                                    displayOrders(documents);
                                }
                        );
    }

    // =========================================================
    // REMOVE LISTENER
    // =========================================================

    private void removeOrdersListener() {

        if (ordersListener != null) {

            ordersListener.remove();

            ordersListener = null;
        }
    }

    // =========================================================
    // DISPLAY ORDERS
    // =========================================================

    private void displayOrders(
            List<DocumentSnapshot> documents) {

        if (!isAdded() ||
                orderContentContainer == null) {
            return;
        }

        orderContentContainer.removeAllViews();

        if (documents == null ||
                documents.isEmpty()) {

            showNoOrdersMessage();

            return;
        }

        boolean foundOrder = false;

        for (DocumentSnapshot document : documents) {

            if (document == null ||
                    !document.exists()) {
                continue;
            }

            String status =
                    getSafeString(
                            document,
                            "status",
                            "pending"
                    );

            status =
                    status
                            .toLowerCase(Locale.getDefault())
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
    }

    // =========================================================
    // DATE VALUE
    // =========================================================

    private long getOrderDateValue(
            DocumentSnapshot document) {

        if (document == null) {
            return 0L;
        }

        Object value =
                document.get("orderDate");

        if (value instanceof Number) {

            return ((Number) value).longValue();
        }

        if (value instanceof Timestamp) {

            return ((Timestamp) value)
                    .toDate()
                    .getTime();
        }

        if (value != null) {

            try {

                return Long.parseLong(
                        String.valueOf(value).trim()
                );

            } catch (Exception ignored) {
            }
        }

        Object createdAt =
                document.get("createdAt");

        if (createdAt instanceof Timestamp) {

            return ((Timestamp) createdAt)
                    .toDate()
                    .getTime();
        }

        if (createdAt instanceof Number) {

            return ((Number) createdAt).longValue();
        }

        return 0L;
    }

    // =========================================================
    // FILTER
    // =========================================================

    private boolean matchesFilter(String status) {

        if (status == null ||
                status.trim().isEmpty()) {

            status = "pending";
        }

        status =
                status
                        .toLowerCase(Locale.getDefault())
                        .trim();

        if (currentFilter.equals("all")) {

            return status.equals("new")
                    || status.equals("pending");
        }

        if (currentFilter.equals("active")) {

            return status.equals("accepted")
                    || status.equals("processing")
                    || status.equals("shipped");
        }

        if (currentFilter.equals("delivered")) {

            return status.equals("delivered")
                    || status.equals("completed");
        }

        if (currentFilter.equals("cancelled")) {

            return status.equals("cancelled")
                    || status.equals("canceled")
                    || status.equals("rejected");
        }

        return false;
    }

    // =========================================================
    // ORDER CARD
    // =========================================================

    private void addOrderCard(
            DocumentSnapshot document) {

        if (!isAdded()) {
            return;
        }

        View orderView =
                LayoutInflater.from(requireContext())
                        .inflate(
                                R.layout.fragment_seller_order_card,
                                orderContentContainer,
                                false
                        );

        TextView txtOrderId =
                orderView.findViewById(R.id.txtOrderId);

        TextView txtOrderDate =
                orderView.findViewById(R.id.txtOrderDate);

        TextView txtOrderStatus =
                orderView.findViewById(R.id.txtOrderStatus);

        TextView txtProductName =
                orderView.findViewById(R.id.txtProductName);

        TextView txtQuantity =
                orderView.findViewById(R.id.txtQuantity);

        TextView txtPrice =
                orderView.findViewById(R.id.txtPrice);

        View btnOrderDetails =
                orderView.findViewById(R.id.btnOrderDetails);

        View btnOrderStatus =
                orderView.findViewById(R.id.btnOrderStatus);

        // =====================================================
        // ORDER ID
        // =====================================================

        String orderId =
                getSafeString(
                        document,
                        "orderId",
                        ""
                );

        if (orderId.trim().isEmpty()) {

            orderId =
                    getSafeString(
                            document,
                            "id",
                            document.getId()
                    );
        }

        final String finalOrderId =
                orderId.trim().isEmpty()
                        ? document.getId()
                        : orderId;

        if (txtOrderId != null) {

            txtOrderId.setText(
                    getString(
                            R.string.order_label,
                            finalOrderId
                    )
            );
        }

        // =====================================================
        // DATE
        // =====================================================

        if (txtOrderDate != null) {

            txtOrderDate.setText(
                    getReadableOrderDate(document)
            );
        }

        // =====================================================
        // PRODUCT
        // =====================================================

        String productName =
                getSafeString(
                        document,
                        "productName",
                        getString(R.string.product)
                );

        if (txtProductName != null) {

            txtProductName.setText(productName);
        }

        // =====================================================
        // QUANTITY
        // =====================================================

        String quantity =
                getNumberOrString(
                        document,
                        "quantity",
                        "1"
                );

        if (txtQuantity != null) {

            txtQuantity.setText(
                    getString(
                            R.string.quantity_label,
                            quantity
                    )
            );
        }

        // =====================================================
        // PRICE
        // =====================================================

        String amount =
                getNumberOrString(
                        document,
                        "totalAmount",
                        ""
                );

        if (amount.trim().isEmpty()) {

            amount =
                    getNumberOrString(
                            document,
                            "amount",
                            "0"
                    );
        }

        if (txtPrice != null) {

            txtPrice.setText(
                    getString(
                            R.string.price_label,
                            amount
                    )
            );
        }

        // =====================================================
        // STATUS
        // =====================================================

        String firestoreStatus =
                getSafeString(
                        document,
                        "status",
                        "pending"
                );

        final String finalStatus =
                firestoreStatus
                        .toLowerCase(Locale.getDefault())
                        .trim();

        if (txtOrderStatus != null) {

            txtOrderStatus.setText(
                    formatStatus(finalStatus)
            );

            setStatusTextColor(
                    txtOrderStatus,
                    finalStatus
            );
        }

        // =====================================================
        // DETAILS
        // =====================================================

        if (btnOrderDetails != null) {

            btnOrderDetails.setVisibility(View.VISIBLE);

            btnOrderDetails.setOnClickListener(
                    v -> openOrderDetails(
                            document.getId()
                    )
            );
        }

        // =====================================================
        // PENDING
        // =====================================================

        if (finalStatus.equals("new")
                || finalStatus.equals("pending")) {

            if (btnOrderStatus != null) {

                setStatusButton(
                        btnOrderStatus,
                        getString(R.string.accept_order)
                );

                btnOrderStatus.setOnClickListener(
                        v -> acceptOrderAndNotifyBuyer(
                                document.getId()
                        )
                );
            }

            addCancelButton(
                    orderView,
                    document.getId()
            );
        }

        // =====================================================
        // ACCEPTED
        // =====================================================

        else if (finalStatus.equals("accepted")) {

            setStatusButton(
                    btnOrderStatus,
                    getString(R.string.mark_as_shipped)
            );

            if (btnOrderStatus != null) {

                btnOrderStatus.setOnClickListener(
                        v -> updateOrderStatus(
                                document.getId(),
                                "shipped"
                        )
                );
            }
        }

        // =====================================================
        // PROCESSING
        // =====================================================

        else if (finalStatus.equals("processing")) {

            setStatusButton(
                    btnOrderStatus,
                    getString(R.string.mark_as_shipped)
            );

            if (btnOrderStatus != null) {

                btnOrderStatus.setOnClickListener(
                        v -> updateOrderStatus(
                                document.getId(),
                                "shipped"
                        )
                );
            }
        }

        // =====================================================
        // SHIPPED
        // =====================================================

        else if (finalStatus.equals("shipped")) {

            setStatusButton(
                    btnOrderStatus,
                    getString(R.string.mark_as_delivered)
            );

            if (btnOrderStatus != null) {

                btnOrderStatus.setOnClickListener(
                        v -> updateOrderStatus(
                                document.getId(),
                                "delivered"
                        )
                );
            }
        }

        // =====================================================
        // DELIVERED
        // =====================================================

        else if (
                finalStatus.equals("delivered")
                        || finalStatus.equals("completed")
        ) {

            setStatusButton(
                    btnOrderStatus,
                    getString(R.string.delivered)
            );

            if (btnOrderStatus != null) {

                btnOrderStatus.setOnClickListener(
                        v -> showToast(
                                getString(R.string.order_delivered)
                        )
                );
            }
        }

        // =====================================================
        // CANCELLED
        // =====================================================

        else if (
                finalStatus.equals("cancelled")
                        || finalStatus.equals("canceled")
                        || finalStatus.equals("rejected")
        ) {

            setStatusButton(
                    btnOrderStatus,
                    getString(R.string.cancelled)
            );

            if (btnOrderStatus != null) {

                btnOrderStatus.setOnClickListener(
                        v -> showToast(
                                getString(R.string.order_cancelled)
                        )
                );
            }
        }

        else {

            setStatusButton(
                    btnOrderStatus,
                    formatStatus(finalStatus)
            );
        }

        // =====================================================
        // SPACING
        // =====================================================

        ViewGroup.LayoutParams existingParams =
                orderView.getLayoutParams();

        if (existingParams instanceof
                LinearLayout.LayoutParams) {

            LinearLayout.LayoutParams params =
                    (LinearLayout.LayoutParams)
                            existingParams;

            params.width =
                    ViewGroup.LayoutParams.MATCH_PARENT;

            params.height =
                    ViewGroup.LayoutParams.WRAP_CONTENT;

            params.setMargins(
                    0,
                    0,
                    0,
                    16
            );

            orderView.setLayoutParams(params);
        }

        orderContentContainer.addView(orderView);
    }

    // =========================================================
    // ACCEPT ORDER
    // =========================================================

    private void acceptOrderAndNotifyBuyer(
            String orderId) {

        if (!isAdded()) {
            return;
        }

        db.collection("orders")
                .document(orderId)
                .get()
                .addOnSuccessListener(
                        documentSnapshot -> {

                            if (!isAdded()) {
                                return;
                            }

                            if (!documentSnapshot.exists()) {

                                showToast(
                                        getString(
                                                R.string.order_not_found
                                        )
                                );

                                return;
                            }

                            String currentStatus =
                                    getSafeString(
                                            documentSnapshot,
                                            "status",
                                            "pending"
                                    )
                                            .toLowerCase(
                                                    Locale.getDefault()
                                            )
                                            .trim();

                            if (!currentStatus.equals("pending")
                                    && !currentStatus.equals("new")) {

                                showToast(
                                        getString(
                                                R.string.order_already_status,
                                                formatStatus(currentStatus)
                                        )
                                );

                                return;
                            }

                            String buyerId =
                                    getSafeString(
                                            documentSnapshot,
                                            "buyerId",
                                            ""
                                    );

                            db.collection("orders")
                                    .document(orderId)
                                    .update(
                                            "status",
                                            "accepted",
                                            "updatedAt",
                                            Timestamp.now()
                                    )
                                    .addOnSuccessListener(
                                            unused -> {

                                                if (!isAdded()) {
                                                    return;
                                                }

                                                if (!buyerId
                                                        .trim()
                                                        .isEmpty()) {

                                                    BuyerNotificationHelper
                                                            .createOrderAcceptedNotification(
                                                                    buyerId,
                                                                    orderId
                                                            );
                                                }

                                                showToast(
                                                        getString(
                                                                R.string.order_accepted_successfully
                                                        )
                                                );
                                            }
                                    )
                                    .addOnFailureListener(
                                            e -> {

                                                if (!isAdded()) {
                                                    return;
                                                }

                                                showToast(
                                                        getString(
                                                                R.string.failed_accept_order
                                                        )
                                                                + " "
                                                                + e.getMessage()
                                                );
                                            }
                                    );
                        }
                )
                .addOnFailureListener(
                        e -> {

                            if (!isAdded()) {
                                return;
                            }

                            showToast(
                                    getString(
                                            R.string.failed_read_order
                                    )
                                            + " "
                                            + e.getMessage()
                            );
                        }
                );
    }

    // =========================================================
    // UPDATE STATUS
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
                        newStatus,
                        "updatedAt",
                        Timestamp.now()
                )
                .addOnSuccessListener(
                        unused -> {

                            if (!isAdded()) {
                                return;
                            }

                            showToast(
                                    getString(
                                            R.string.status_updated,
                                            formatStatus(newStatus)
                                    )
                            );
                        }
                )
                .addOnFailureListener(
                        e -> {

                            if (!isAdded()) {
                                return;
                            }

                            showToast(
                                    getString(
                                            R.string.failed_update_order
                                    )
                                            + " "
                                            + e.getMessage()
                            );
                        }
                );
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
                .get()
                .addOnSuccessListener(
                        documentSnapshot -> {

                            if (!isAdded()) {
                                return;
                            }

                            if (!documentSnapshot.exists()) {
                                return;
                            }

                            String currentStatus =
                                    getSafeString(
                                            documentSnapshot,
                                            "status",
                                            "pending"
                                    )
                                            .toLowerCase(
                                                    Locale.getDefault()
                                            )
                                            .trim();

                            if (!currentStatus.equals("pending")
                                    && !currentStatus.equals("new")) {

                                showToast(
                                        getString(
                                                R.string.order_cannot_cancel
                                        )
                                );

                                return;
                            }

                            db.collection("orders")
                                    .document(orderId)
                                    .update(
                                            "status",
                                            "cancelled",
                                            "updatedAt",
                                            Timestamp.now()
                                    )
                                    .addOnSuccessListener(
                                            unused -> {

                                                if (!isAdded()) {
                                                    return;
                                                }

                                                showToast(
                                                        getString(
                                                                R.string.order_cancelled_successfully
                                                        )
                                                );
                                            }
                                    )
                                    .addOnFailureListener(
                                            e -> {

                                                if (!isAdded()) {
                                                    return;
                                                }

                                                showToast(
                                                        getString(
                                                                R.string.failed_cancel_order
                                                        )
                                                                + " "
                                                                + e.getMessage()
                                                );
                                            }
                                    );
                        }
                )
                .addOnFailureListener(
                        e -> {

                            if (!isAdded()) {
                                return;
                            }

                            showToast(
                                    getString(
                                            R.string.failed_check_order
                                    )
                                            + " "
                                            + e.getMessage()
                            );
                        }
                );
    }

    // =========================================================
    // CANCEL BUTTON
    // =========================================================

    private void addCancelButton(
            View orderView,
            String orderId) {

        if (!isAdded() ||
                orderView == null) {
            return;
        }

        LinearLayout targetLayout =
                findFirstLinearLayout(orderView);

        if (targetLayout == null) {
            return;
        }

        TextView cancelButton =
                new TextView(requireContext());

        cancelButton.setText(
                getString(R.string.cancel_order)
        );

        cancelButton.setTextSize(13);

        cancelButton.setGravity(Gravity.CENTER);

        cancelButton.setTextColor(
                Color.rgb(198, 40, 40)
        );

        cancelButton.setTypeface(
                null,
                Typeface.BOLD
        );

        GradientDrawable background =
                new GradientDrawable();

        background.setColor(
                Color.rgb(255, 235, 235)
        );

        background.setCornerRadius(12);

        background.setStroke(
                1,
                Color.rgb(229, 57, 53)
        );

        cancelButton.setBackground(background);

        cancelButton.setPadding(
                10,
                8,
                10,
                8
        );

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        44
                );

        params.setMargins(
                16,
                8,
                16,
                12
        );

        cancelButton.setLayoutParams(params);

        cancelButton.setOnClickListener(
                v -> cancelOrder(orderId)
        );

        targetLayout.addView(cancelButton);
    }

    // =========================================================
    // FIND LINEAR LAYOUT
    // =========================================================

    private LinearLayout findFirstLinearLayout(
            View view) {

        if (view instanceof LinearLayout) {
            return (LinearLayout) view;
        }

        if (view instanceof ViewGroup) {

            ViewGroup group =
                    (ViewGroup) view;

            for (int i = 0;
                 i < group.getChildCount();
                 i++) {

                View child =
                        group.getChildAt(i);

                LinearLayout result =
                        findFirstLinearLayout(child);

                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }

    // =========================================================
    // ORDER DETAILS
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
    // STATUS COLOR
    // =========================================================

    private void setStatusTextColor(
            TextView statusView,
            String status) {

        if (statusView == null) {
            return;
        }

        if (status == null) {
            status = "pending";
        }

        status =
                status
                        .toLowerCase(Locale.getDefault())
                        .trim();

        if (status.equals("accepted")
                || status.equals("processing")) {

            statusView.setTextColor(
                    Color.rgb(46, 125, 50)
            );

        } else if (status.equals("shipped")) {

            statusView.setTextColor(
                    Color.rgb(21, 101, 192)
            );

        } else if (
                status.equals("delivered")
                        || status.equals("completed")
        ) {

            statusView.setTextColor(
                    Color.rgb(46, 125, 50)
            );

        } else if (
                status.equals("cancelled")
                        || status.equals("canceled")
                        || status.equals("rejected")
        ) {

            statusView.setTextColor(
                    Color.rgb(198, 40, 40)
            );

        } else {

            statusView.setTextColor(
                    Color.rgb(239, 108, 0)
            );
        }
    }

    // =========================================================
    // STATUS BUTTON
    // =========================================================

    private void setStatusButton(
            View button,
            String text) {

        if (button == null) {
            return;
        }

        button.setVisibility(View.VISIBLE);

        if (button instanceof AppCompatButton) {

            ((AppCompatButton) button)
                    .setText(text);

        } else if (button instanceof TextView) {

            ((TextView) button)
                    .setText(text);
        }
    }

    // =========================================================
    // FORMAT STATUS
    // =========================================================

    private String formatStatus(String status) {

        if (status == null ||
                status.trim().isEmpty()) {

            return getString(R.string.pending);
        }

        status = status.trim();

        if (status.equalsIgnoreCase("new")
                || status.equalsIgnoreCase("pending")) {

            return getString(R.string.pending);
        }

        if (status.equalsIgnoreCase("accepted")) {

            return getString(R.string.accepted);
        }

        if (status.equalsIgnoreCase("processing")) {

            return getString(R.string.processing);
        }

        if (status.equalsIgnoreCase("shipped")) {

            return getString(R.string.shipped);
        }

        if (status.equalsIgnoreCase("completed")
                || status.equalsIgnoreCase("delivered")) {

            return getString(R.string.delivered);
        }

        if (status.equalsIgnoreCase("cancelled")
                || status.equalsIgnoreCase("canceled")
                || status.equalsIgnoreCase("rejected")) {

            return getString(R.string.cancelled);
        }

        return status;
    }

    // =========================================================
    // DATE
    // =========================================================

    private String getReadableOrderDate(
            DocumentSnapshot document) {

        if (document == null) {
            return getString(R.string.date_not_available);
        }

        Object orderDate =
                document.get("orderDate");

        long time = -1;

        if (orderDate instanceof Number) {

            time =
                    ((Number) orderDate).longValue();

        } else if (orderDate instanceof Timestamp) {

            time =
                    ((Timestamp) orderDate)
                            .toDate()
                            .getTime();

        } else if (orderDate != null) {

            try {

                time =
                        Long.parseLong(
                                String.valueOf(orderDate)
                                        .trim()
                        );

            } catch (Exception ignored) {
            }
        }

        if (time <= 0) {

            Object createdAt =
                    document.get("createdAt");

            if (createdAt instanceof Timestamp) {

                time =
                        ((Timestamp) createdAt)
                                .toDate()
                                .getTime();
            }

            if (createdAt instanceof Number) {

                time =
                        ((Number) createdAt)
                                .longValue();
            }
        }

        if (time <= 0) {

            return getString(
                    R.string.date_not_available
            );
        }

        try {

            SimpleDateFormat formatter =
                    new SimpleDateFormat(
                            "dd MMM yyyy, hh:mm a",
                            Locale.getDefault()
                    );

            return formatter.format(
                    new Date(time)
            );

        } catch (Exception e) {

            return String.valueOf(time);
        }
    }

    // =========================================================
    // SAFE STRING
    // =========================================================

    private String getSafeString(
            DocumentSnapshot document,
            String field,
            String defaultValue) {

        if (document == null) {
            return defaultValue;
        }

        Object value =
                document.get(field);

        if (value == null) {
            return defaultValue;
        }

        String result =
                String.valueOf(value).trim();

        if (result.isEmpty()) {
            return defaultValue;
        }

        return result;
    }

    // =========================================================
    // NUMBER
    // =========================================================

    private String getNumberOrString(
            DocumentSnapshot document,
            String field,
            String defaultValue) {

        if (document == null) {
            return defaultValue;
        }

        Object value =
                document.get(field);

        if (value == null) {
            return defaultValue;
        }

        if (value instanceof Number) {

            Number number =
                    (Number) value;

            double doubleValue =
                    number.doubleValue();

            long longValue =
                    number.longValue();

            if (doubleValue == longValue) {

                return String.valueOf(longValue);
            }

            return String.valueOf(doubleValue);
        }

        String result =
                String.valueOf(value).trim();

        if (result.isEmpty()) {
            return defaultValue;
        }

        return result;
    }

    // =========================================================
    // NO ORDERS
    // =========================================================

    private void showNoOrdersMessage() {

        if (!isAdded() ||
                orderContentContainer == null) {
            return;
        }

        TextView noOrders =
                new TextView(requireContext());

        if (currentFilter.equals("all")) {

            noOrders.setText(
                    R.string.no_pending_orders
            );

        } else if (currentFilter.equals("active")) {

            noOrders.setText(
                    R.string.no_active_orders
            );

        } else if (currentFilter.equals("delivered")) {

            noOrders.setText(
                    R.string.no_delivered_orders
            );

        } else if (currentFilter.equals("cancelled")) {

            noOrders.setText(
                    R.string.no_cancelled_orders
            );

        } else {

            noOrders.setText(
                    R.string.no_orders_found
            );
        }

        noOrders.setTextSize(16);

        noOrders.setTextColor(
                Color.rgb(90, 90, 90)
        );

        noOrders.setGravity(Gravity.CENTER);

        noOrders.setPadding(
                20,
                60,
                20,
                60
        );

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        noOrders.setLayoutParams(params);

        orderContentContainer.addView(noOrders);
    }

    // =========================================================
    // TOAST
    // =========================================================

    private void showToast(String message) {

        if (isAdded()) {

            Toast.makeText(
                    requireContext(),
                    message,
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    // =========================================================
    // PAUSE
    // =========================================================

    @Override
    public void onPause() {

        super.onPause();

        removeOrdersListener();
    }

    // =========================================================
    // RESUME
    // =========================================================

    @Override
    public void onResume() {

        super.onResume();

        if (db != null &&
                orderContentContainer != null) {

            startOrdersListener();
        }
    }

    // =========================================================
    // DESTROY
    // =========================================================

    @Override
    public void onDestroyView() {

        removeOrdersListener();

        super.onDestroyView();
    }
}