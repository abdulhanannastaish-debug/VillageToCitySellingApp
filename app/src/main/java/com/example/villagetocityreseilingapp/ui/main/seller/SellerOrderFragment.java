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
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SellerOrderFragment extends Fragment {

    // =========================================================
    // FIREBASE
    // =========================================================

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    // =========================================================
    // REALTIME LISTENER
    // =========================================================

    private ListenerRegistration ordersListener;

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
        // FIND VIEWS
        // =====================================================

        orderContentContainer =
                view.findViewById(
                        R.id.orderContentContainer
                );

        tabAll =
                view.findViewById(
                        R.id.tabAll
                );

        tabActive =
                view.findViewById(
                        R.id.tabActive
                );

        tabDelivered =
                view.findViewById(
                        R.id.tabDelivered
                );

        tabCancelled =
                view.findViewById(
                        R.id.tabCancelled
                );

        // =====================================================
        // ALL
        // =====================================================

        if (tabAll != null) {

            tabAll.setOnClickListener(v -> {

                currentFilter = "all";

                updateTabColors();

                startOrdersListener();
            });
        }

        // =====================================================
        // ACTIVE
        // =====================================================

        if (tabActive != null) {

            tabActive.setOnClickListener(v -> {

                currentFilter = "active";

                updateTabColors();

                startOrdersListener();
            });
        }

        // =====================================================
        // DELIVERED
        // =====================================================

        if (tabDelivered != null) {

            tabDelivered.setOnClickListener(v -> {

                currentFilter = "delivered";

                updateTabColors();

                startOrdersListener();
            });
        }

        // =====================================================
        // CANCELLED
        // =====================================================

        if (tabCancelled != null) {

            tabCancelled.setOnClickListener(v -> {

                currentFilter = "cancelled";

                updateTabColors();

                startOrdersListener();
            });
        }

        // =====================================================
        // INITIAL
        // =====================================================

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

        int gray = Color.rgb(
                100,
                100,
                100
        );

        int green = Color.rgb(
                46,
                125,
                50
        );

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
    // START REALTIME ORDERS LISTENER
    //
    // Buyer agar order cancel kare:
    //
    // status = cancelled
    //
    // Yeh listener Firestore change ko automatically receive karega.
    // =========================================================

    private void startOrdersListener() {

        if (!isAdded() ||
                db == null ||
                auth == null ||
                orderContentContainer == null) {

            return;
        }

        // =====================================================
        // REMOVE OLD LISTENER
        // =====================================================

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

        // =====================================================
        // CLEAR OLD CARDS
        // =====================================================

        orderContentContainer.removeAllViews();

        // =====================================================
        // REALTIME QUERY
        //
        // sellerId ke orders realtime monitor honge.
        // =====================================================

        ordersListener =
                db.collection("orders")
                        .whereEqualTo(
                                "sellerId",
                                sellerId
                        )
                        .addSnapshotListener(
                                (snapshots, error) -> {

                                    if (!isAdded()) {
                                        return;
                                    }

                                    if (error != null) {

                                        orderContentContainer
                                                .removeAllViews();

                                        Toast.makeText(
                                                requireContext(),
                                                "Failed to load orders: "
                                                        + error.getMessage(),
                                                Toast.LENGTH_LONG
                                        ).show();

                                        showNoOrdersMessage();

                                        return;
                                    }

                                    if (snapshots == null) {

                                        showNoOrdersMessage();

                                        return;
                                    }

                                    // =================================
                                    // GET ALL DOCUMENTS
                                    // =================================

                                    List<DocumentSnapshot> documents =
                                            new ArrayList<>(
                                                    snapshots.getDocuments()
                                            );

                                    // =================================
                                    // SORT
                                    // =================================

                                    documents.sort(
                                            (a, b) ->
                                                    Long.compare(
                                                            getOrderDateValue(b),
                                                            getOrderDateValue(a)
                                                    )
                                    );

                                    // =================================
                                    // DISPLAY
                                    // =================================

                                    displayOrders(
                                            documents
                                    );
                                }
                        );
    }

    // =========================================================
    // REMOVE REALTIME LISTENER
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

        // =====================================================
        // LOOP ALL ORDERS
        // =====================================================

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
                            .toLowerCase(
                                    Locale.getDefault()
                            )
                            .trim();

            // =================================================
            // FILTER
            // =================================================

            if (!matchesFilter(status)) {
                continue;
            }

            foundOrder = true;

            addOrderCard(
                    document
            );
        }

        // =====================================================
        // NO MATCHING ORDERS
        // =====================================================

        if (!foundOrder) {

            showNoOrdersMessage();
        }
    }

    // =========================================================
    // GET ORDER DATE VALUE
    // =========================================================

    private long getOrderDateValue(
            DocumentSnapshot document) {

        if (document == null) {
            return 0L;
        }

        Object value =
                document.get("orderDate");

        if (value instanceof Number) {

            return ((Number) value)
                    .longValue();
        }

        if (value instanceof Timestamp) {

            return ((Timestamp) value)
                    .toDate()
                    .getTime();
        }

        if (value != null) {

            try {

                return Long.parseLong(
                        String.valueOf(value)
                                .trim()
                );

            } catch (Exception ignored) {
            }
        }

        // =====================================================
        // FALLBACK CREATED AT
        // =====================================================

        Object createdAt =
                document.get("createdAt");

        if (createdAt instanceof Timestamp) {

            return ((Timestamp) createdAt)
                    .toDate()
                    .getTime();
        }

        if (createdAt instanceof Number) {

            return ((Number) createdAt)
                    .longValue();
        }

        return 0L;
    }

    // =========================================================
    // FILTER
    // =========================================================

    private boolean matchesFilter(
            String status) {

        if (status == null ||
                status.trim().isEmpty()) {

            status = "pending";
        }

        status =
                status
                        .toLowerCase(
                                Locale.getDefault()
                        )
                        .trim();

        // =====================================================
        // ALL
        //
        // Pending/new orders
        // =====================================================

        if (currentFilter.equals("all")) {

            return status.equals("new")
                    || status.equals("pending");
        }

        // =====================================================
        // ACTIVE
        // =====================================================

        if (currentFilter.equals("active")) {

            return status.equals("accepted")
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
        //
        // Buyer cancel kare ya seller cancel kare:
        // status = cancelled
        //
        // Is wajah se yahan automatically show hoga.
        // =====================================================

        if (currentFilter.equals("cancelled")) {

            return status.equals("cancelled")
                    || status.equals("canceled")
                    || status.equals("rejected");
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

        View orderView =
                LayoutInflater.from(
                        requireContext()
                ).inflate(
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
        // ORDER ID
        // =====================================================

        String firestoreOrderId =
                getSafeString(
                        document,
                        "orderId",
                        ""
                );

        if (firestoreOrderId
                .trim()
                .isEmpty()) {

            firestoreOrderId =
                    getSafeString(
                            document,
                            "id",
                            document.getId()
                    );
        }

        final String finalOrderId =
                firestoreOrderId
                        .trim()
                        .isEmpty()
                        ? document.getId()
                        : firestoreOrderId;

        // =====================================================
        // DATE
        // =====================================================

        if (txtOrderDate != null) {

            txtOrderDate.setText(
                    getReadableOrderDate(
                            document
                    )
            );
        }

        // =====================================================
        // PRODUCT NAME
        // =====================================================

        String productName =
                getSafeString(
                        document,
                        "productName",
                        "Product"
                );

        if (txtProductName != null) {

            txtProductName.setText(
                    productName
            );
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
                    "Quantity: " + quantity
            );
        }

        // =====================================================
        // AMOUNT
        // =====================================================

        String amount =
                getNumberOrString(
                        document,
                        "totalAmount",
                        ""
                );

        if (amount
                .trim()
                .isEmpty()) {

            amount =
                    getNumberOrString(
                            document,
                            "amount",
                            "0"
                    );
        }

        if (txtPrice != null) {

            txtPrice.setText(
                    "Rs. " + amount
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
                        .toLowerCase(
                                Locale.getDefault()
                        )
                        .trim();

        if (txtOrderId != null) {

            txtOrderId.setText(
                    "Order #" + finalOrderId
            );
        }

        if (txtOrderStatus != null) {

            txtOrderStatus.setText(
                    formatStatus(
                            finalStatus
                    )
            );

            setStatusTextColor(
                    txtOrderStatus,
                    finalStatus
            );
        }

        // =====================================================
        // ORDER DETAILS
        // =====================================================

        if (btnOrderDetails != null) {

            btnOrderDetails.setVisibility(
                    View.VISIBLE
            );

            btnOrderDetails.setOnClickListener(
                    v -> openOrderDetails(
                            document.getId()
                    )
            );
        }

        // =====================================================
        // PENDING / NEW
        //
        // Buyer ne abhi cancel nahi kiya.
        // Seller accept kar sakta hai.
        // =====================================================

        if (finalStatus.equals("new")
                || finalStatus.equals("pending")) {

            if (btnOrderStatus != null) {

                setStatusButton(
                        btnOrderStatus,
                        "Accept Order"
                );

                btnOrderStatus.setOnClickListener(
                        v -> acceptOrderAndNotifyBuyer(
                                document.getId()
                        )
                );
            }

            // =================================================
            // SELLER CANCEL BUTTON
            // =================================================

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
                    "Mark as Shipped"
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
                    "Mark as Shipped"
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
                    "Mark as Delivered"
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
                    "Delivered"
            );

            if (btnOrderStatus != null) {

                btnOrderStatus.setOnClickListener(
                        v -> {

                            if (isAdded()) {

                                Toast.makeText(
                                        requireContext(),
                                        "Order Delivered",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }
                );
            }
        }

        // =====================================================
        // CANCELLED
        //
        // IMPORTANT:
        //
        // Buyer cancel karega to yahan ye block chalega.
        // =====================================================

        else if (
                finalStatus.equals("cancelled")
                        || finalStatus.equals("canceled")
                        || finalStatus.equals("rejected")
        ) {

            setStatusButton(
                    btnOrderStatus,
                    "Cancelled"
            );

            if (btnOrderStatus != null) {

                btnOrderStatus.setOnClickListener(
                        v -> {

                            if (isAdded()) {

                                Toast.makeText(
                                        requireContext(),
                                        "Order Cancelled",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }
                );
            }
        }

        // =====================================================
        // OTHER
        // =====================================================

        else {

            setStatusButton(
                    btnOrderStatus,
                    formatStatus(
                            finalStatus
                    )
            );
        }

        // =====================================================
        // CARD SPACING
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

            orderView.setLayoutParams(
                    params
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
    // ACCEPT ORDER + NOTIFY BUYER
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

                                Toast.makeText(
                                        requireContext(),
                                        "Order not found.",
                                        Toast.LENGTH_SHORT
                                ).show();

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

                            // =================================================
                            // IMPORTANT:
                            //
                            // Agar buyer ne seller ke accept karne se pehle
                            // order cancel kar diya hai to seller accept
                            // NAHI kar sakta.
                            // =================================================

                            if (!currentStatus.equals("pending")
                                    && !currentStatus.equals("new")) {

                                Toast.makeText(
                                        requireContext(),
                                        "This order is already "
                                                + formatStatus(currentStatus)
                                                + ".",
                                        Toast.LENGTH_LONG
                                ).show();

                                return;
                            }

                            String buyerId =
                                    getSafeString(
                                            documentSnapshot,
                                            "buyerId",
                                            ""
                                    );

                            // =================================================
                            // UPDATE STATUS
                            // =================================================

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

                                                // =================================
                                                // BUYER NOTIFICATION
                                                // =================================

                                                if (!buyerId
                                                        .trim()
                                                        .isEmpty()) {

                                                    BuyerNotificationHelper
                                                            .createOrderAcceptedNotification(
                                                                    buyerId,
                                                                    orderId
                                                            );
                                                }

                                                Toast.makeText(
                                                        requireContext(),
                                                        "Order accepted successfully.",
                                                        Toast.LENGTH_SHORT
                                                ).show();

                                                // =================================
                                                // REALTIME LISTENER
                                                // khud screen update karega.
                                                // =================================
                                            }
                                    )
                                    .addOnFailureListener(
                                            e -> {

                                                if (!isAdded()) {
                                                    return;
                                                }

                                                Toast.makeText(
                                                        requireContext(),
                                                        "Failed to accept order: "
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

                            Toast.makeText(
                                    requireContext(),
                                    "Failed to read order: "
                                            + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
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
                        newStatus,
                        "updatedAt",
                        Timestamp.now()
                )
                .addOnSuccessListener(
                        unused -> {

                            if (!isAdded()) {
                                return;
                            }

                            Toast.makeText(
                                    requireContext(),
                                    "Order status updated to "
                                            + formatStatus(
                                            newStatus
                                    ),
                                    Toast.LENGTH_SHORT
                            ).show();

                            // =================================================
                            // REALTIME LISTENER AUTOMATICALLY REFRESH KAREGA
                            // =================================================
                        }
                )
                .addOnFailureListener(
                        e -> {

                            if (!isAdded()) {
                                return;
                            }

                            Toast.makeText(
                                    requireContext(),
                                    "Failed to update order: "
                                            + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
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

                            // =================================================
                            // CANCEL SIRF PENDING ORDER KO
                            // =================================================

                            if (!currentStatus.equals("pending")
                                    && !currentStatus.equals("new")) {

                                Toast.makeText(
                                        requireContext(),
                                        "Order cannot be cancelled now.",
                                        Toast.LENGTH_SHORT
                                ).show();

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

                                                Toast.makeText(
                                                        requireContext(),
                                                        "Order cancelled successfully.",
                                                        Toast.LENGTH_SHORT
                                                ).show();

                                                // =================================================
                                                // REALTIME LISTENER:
                                                //
                                                // pending card automatically disappear
                                                // aur Cancelled tab mein show ho jayega.
                                                // =================================================
                                            }
                                    )
                                    .addOnFailureListener(
                                            e -> {

                                                if (!isAdded()) {
                                                    return;
                                                }

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
    // ADD CANCEL BUTTON
    // =========================================================

    private void addCancelButton(
            View orderView,
            String orderId) {

        if (!isAdded() ||
                orderView == null) {

            return;
        }

        LinearLayout targetLayout =
                findFirstLinearLayout(
                        orderView
                );

        if (targetLayout == null) {
            return;
        }

        // =====================================================
        // CANCEL BUTTON
        // =====================================================

        TextView cancelButton =
                new TextView(
                        requireContext()
                );

        cancelButton.setText(
                "Cancel Order"
        );

        cancelButton.setTextSize(
                13
        );

        cancelButton.setGravity(
                Gravity.CENTER
        );

        cancelButton.setTextColor(
                Color.rgb(
                        198,
                        40,
                        40
                )
        );

        cancelButton.setTypeface(
                null,
                Typeface.BOLD
        );

        // =====================================================
        // BACKGROUND
        // =====================================================

        GradientDrawable background =
                new GradientDrawable();

        background.setColor(
                Color.rgb(
                        255,
                        235,
                        235
                )
        );

        background.setCornerRadius(
                12
        );

        background.setStroke(
                1,
                Color.rgb(
                        229,
                        57,
                        53
                )
        );

        cancelButton.setBackground(
                background
        );

        cancelButton.setPadding(
                10,
                8,
                10,
                8
        );

        // =====================================================
        // SIZE
        // =====================================================

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

        cancelButton.setLayoutParams(
                params
        );

        // =====================================================
        // CLICK
        // =====================================================

        cancelButton.setOnClickListener(
                v -> cancelOrder(
                        orderId
                )
        );

        // =====================================================
        // ADD
        // =====================================================

        targetLayout.addView(
                cancelButton
        );
    }

    // =========================================================
    // FIND FIRST LINEAR LAYOUT
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
                        findFirstLinearLayout(
                                child
                        );

                if (result != null) {

                    return result;
                }
            }
        }

        return null;
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
    // STATUS TEXT COLOR
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
                        .toLowerCase(
                                Locale.getDefault()
                        )
                        .trim();

        if (status.equals("accepted")
                || status.equals("processing")) {

            statusView.setTextColor(
                    Color.rgb(
                            46,
                            125,
                            50
                    )
            );

        } else if (
                status.equals("shipped")
        ) {

            statusView.setTextColor(
                    Color.rgb(
                            21,
                            101,
                            192
                    )
            );

        } else if (
                status.equals("delivered")
                        || status.equals("completed")
        ) {

            statusView.setTextColor(
                    Color.rgb(
                            46,
                            125,
                            50
                    )
            );

        } else if (
                status.equals("cancelled")
                        || status.equals("canceled")
                        || status.equals("rejected")
        ) {

            statusView.setTextColor(
                    Color.rgb(
                            198,
                            40,
                            40
                    )
            );

        } else {

            statusView.setTextColor(
                    Color.rgb(
                            239,
                            108,
                            0
                    )
            );
        }
    }

    // =========================================================
    // SET STATUS BUTTON
    // =========================================================

    private void setStatusButton(
            View button,
            String text) {

        if (button == null) {
            return;
        }

        button.setVisibility(
                View.VISIBLE
        );

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

    private String formatStatus(
            String status) {

        if (status == null ||
                status.trim().isEmpty()) {

            return "Pending";
        }

        status =
                status.trim();

        if (status.equalsIgnoreCase("new")) {
            return "Pending";
        }

        if (status.equalsIgnoreCase("pending")) {
            return "Pending";
        }

        if (status.equalsIgnoreCase("accepted")) {
            return "Accepted";
        }

        if (status.equalsIgnoreCase("processing")) {
            return "Processing";
        }

        if (status.equalsIgnoreCase("shipped")) {
            return "Shipped";
        }

        if (status.equalsIgnoreCase("completed")
                || status.equalsIgnoreCase("delivered")) {

            return "Delivered";
        }

        if (status.equalsIgnoreCase("cancelled")
                || status.equalsIgnoreCase("canceled")
                || status.equalsIgnoreCase("rejected")) {

            return "Cancelled";
        }

        return status;
    }

    // =========================================================
    // READABLE ORDER DATE
    // =========================================================

    private String getReadableOrderDate(
            DocumentSnapshot document) {

        if (document == null) {
            return "Date not available";
        }

        Object orderDate =
                document.get("orderDate");

        long time = -1;

        // =====================================================
        // NUMBER
        // =====================================================

        if (orderDate instanceof Number) {

            time =
                    ((Number) orderDate)
                            .longValue();
        }

        // =====================================================
        // TIMESTAMP
        // =====================================================

        else if (orderDate instanceof Timestamp) {

            time =
                    ((Timestamp) orderDate)
                            .toDate()
                            .getTime();
        }

        // =====================================================
        // STRING
        // =====================================================

        else if (orderDate != null) {

            try {

                time =
                        Long.parseLong(
                                String.valueOf(
                                        orderDate
                                ).trim()
                        );

            } catch (Exception ignored) {
            }
        }

        // =====================================================
        // FALLBACK CREATED AT
        // =====================================================

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

            return "Date not available";
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
                String.valueOf(value)
                        .trim();

        if (result.isEmpty()) {
            return defaultValue;
        }

        return result;
    }

    // =========================================================
    // NUMBER OR STRING
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

        // =====================================================
        // NUMBER
        // =====================================================

        if (value instanceof Number) {

            Number number =
                    (Number) value;

            double doubleValue =
                    number.doubleValue();

            long longValue =
                    number.longValue();

            if (doubleValue == longValue) {

                return String.valueOf(
                        longValue
                );
            }

            return String.valueOf(
                    doubleValue
            );
        }

        // =====================================================
        // STRING
        // =====================================================

        String result =
                String.valueOf(value)
                        .trim();

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
                new TextView(
                        requireContext()
                );

        if (currentFilter.equals("all")) {

            noOrders.setText(
                    "No pending orders"
            );

        } else if (currentFilter.equals("active")) {

            noOrders.setText(
                    "No active orders"
            );

        } else if (currentFilter.equals("delivered")) {

            noOrders.setText(
                    "No delivered orders"
            );

        } else if (currentFilter.equals("cancelled")) {

            noOrders.setText(
                    "No cancelled orders"
            );

        } else {

            noOrders.setText(
                    "No orders found"
            );
        }

        noOrders.setTextSize(
                16
        );

        noOrders.setTextColor(
                Color.rgb(
                        90,
                        90,
                        90
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

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        noOrders.setLayoutParams(
                params
        );

        orderContentContainer.addView(
                noOrders
        );
    }

    // =========================================================
    // PAUSE
    // =========================================================

    @Override
    public void onPause() {

        super.onPause();

        // Listener remove karna zaroori hai
        // taake unnecessary Firebase reads na hon.
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
    // DESTROY VIEW
    // =========================================================

    @Override
    public void onDestroyView() {

        removeOrdersListener();

        super.onDestroyView();
    }
}