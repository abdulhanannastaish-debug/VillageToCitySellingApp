package com.example.villagetocityreseilingapp.ui.main.seller;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.example.villagetocityreseilingapp.R;
import com.example.villagetocityreseilingapp.ui.main.buyer.BuyerNotificationHelper;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

// =========================================================
// SELLER ORDER DETAIL FRAGMENT
// =========================================================

public class SellerOrderDetailFragment extends Fragment {

    // =========================================================
    // FIREBASE
    // =========================================================

    private FirebaseFirestore db;

    // =========================================================
    // VIEWS
    // =========================================================

    private ImageButton btnBack;

    private TextView tvOrderStatus;
    private TextView tvOrderId;

    private TextView tvProductName;
    private TextView tvProductQuantity;
    private TextView tvProductPrice;

    private TextView tvBuyerName;
    private TextView tvBuyerPhone;
    private TextView tvBuyerAddress;

    private TextView tvPaymentMethod;
    private TextView tvProductTotal;
    private TextView tvDeliveryCharges;
    private TextView tvTotalAmount;

    private AppCompatButton btnOrderAction;
    private AppCompatButton btnCancelOrder;

    // =========================================================
    // ORDER DOCUMENT ID
    // =========================================================

    private String orderDocumentId;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public SellerOrderDetailFragment() {
        // Required empty public constructor
    }

    // =========================================================
    // NEW INSTANCE
    // =========================================================

    public static SellerOrderDetailFragment newInstance(
            String orderDocumentId) {

        SellerOrderDetailFragment fragment =
                new SellerOrderDetailFragment();

        Bundle args = new Bundle();

        args.putString(
                "orderDocumentId",
                orderDocumentId
        );

        fragment.setArguments(args);

        return fragment;
    }

    // =========================================================
    // ON CREATE
    // =========================================================

    @Override
    public void onCreate(
            Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (getArguments() != null) {

            orderDocumentId =
                    getArguments()
                            .getString("orderDocumentId");

            // Compatibility with old navigation
            if (orderDocumentId == null ||
                    orderDocumentId.trim().isEmpty()) {

                orderDocumentId =
                        getArguments()
                                .getString("orderId");
            }
        }
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
                R.layout.fragment_seller_order_detail,
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

        db =
                FirebaseFirestore.getInstance();

        // =====================================================
        // FIND VIEWS
        // =====================================================

        btnBack =
                view.findViewById(
                        R.id.btn_back
                );

        tvOrderStatus =
                view.findViewById(
                        R.id.tvOrderStatus
                );

        tvOrderId =
                view.findViewById(
                        R.id.tvOrderId
                );

        tvProductName =
                view.findViewById(
                        R.id.tvProductName
                );

        tvProductQuantity =
                view.findViewById(
                        R.id.tvProductQuantity
                );

        tvProductPrice =
                view.findViewById(
                        R.id.tvProductPrice
                );

        tvBuyerName =
                view.findViewById(
                        R.id.tvBuyerName
                );

        tvBuyerPhone =
                view.findViewById(
                        R.id.tvBuyerPhone
                );

        tvBuyerAddress =
                view.findViewById(
                        R.id.tvBuyerAddress
                );

        tvPaymentMethod =
                view.findViewById(
                        R.id.tvPaymentMethod
                );

        tvProductTotal =
                view.findViewById(
                        R.id.tvProductTotal
                );

        tvDeliveryCharges =
                view.findViewById(
                        R.id.tvDeliveryCharges
                );

        tvTotalAmount =
                view.findViewById(
                        R.id.tvTotalAmount
                );

        btnOrderAction =
                view.findViewById(
                        R.id.btnOrderAction
                );

        btnCancelOrder =
                view.findViewById(
                        R.id.btnCancelOrder
                );

        // =====================================================
        // BACK
        // =====================================================

        if (btnBack != null) {

            btnBack.setOnClickListener(
                    v -> {

                        requireActivity()
                                .getSupportFragmentManager()
                                .popBackStack();
                    }
            );
        }

        // =====================================================
        // ORDER ACTION
        // =====================================================

        if (btnOrderAction != null) {

            btnOrderAction.setOnClickListener(
                    v -> handleOrderAction()
            );
        }

        // =====================================================
        // CANCEL ORDER
        // =====================================================

        if (btnCancelOrder != null) {

            btnCancelOrder.setOnClickListener(
                    v -> updateOrderStatus(
                            "cancelled"
                    )
            );
        }

        // =====================================================
        // LOAD ORDER
        // =====================================================

        loadOrder();
    }

    // =========================================================
    // LOAD ORDER
    // =========================================================

    private void loadOrder() {

        if (!isAdded()) {
            return;
        }

        if (orderDocumentId == null ||
                orderDocumentId.trim().isEmpty()) {

            Toast.makeText(
                    requireContext(),
                    "Order ID not found.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        db.collection("orders")
                .document(orderDocumentId)
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

                            displayOrderData(
                                    documentSnapshot
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
                                    "Failed to load order: "
                                            + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }

    // =========================================================
    // DISPLAY ORDER
    // =========================================================

    private void displayOrderData(
            DocumentSnapshot document) {

        if (document == null ||
                !document.exists()) {

            return;
        }

        // =====================================================
        // ORDER ID
        // =====================================================

        String orderId =
                getValueAsString(
                        document,
                        "orderId",
                        ""
                );

        if (orderId.trim().isEmpty()) {

            orderId =
                    getValueAsString(
                            document,
                            "id",
                            ""
                    );
        }

        if (orderId.trim().isEmpty()) {

            orderId =
                    document.getId();
        }

        tvOrderId.setText(
                "Order ID: #" + orderId
        );

        // =====================================================
        // STATUS
        // =====================================================

        String status =
                getValueAsString(
                        document,
                        "status",
                        "pending"
                );

        status =
                normalizeStatus(status);

        tvOrderStatus.setText(
                formatStatus(status)
        );

        // =====================================================
        // PRODUCT NAME
        // =====================================================

        String productName =
                getValueAsString(
                        document,
                        "productName",
                        "Product"
                );

        tvProductName.setText(
                productName
        );

        // =====================================================
        // QUANTITY
        // =====================================================

        String quantity =
                getNumberOrString(
                        document,
                        "quantity",
                        "1"
                );

        tvProductQuantity.setText(
                "Quantity: " + quantity
        );

        // =====================================================
        // PRODUCT PRICE
        // =====================================================

        String amount =
                getNumberOrString(
                        document,
                        "amount",
                        ""
                );

        if (amount.trim().isEmpty()) {

            amount =
                    getNumberOrString(
                            document,
                            "price",
                            ""
                    );
        }

        if (amount.trim().isEmpty()) {

            amount =
                    getNumberOrString(
                            document,
                            "totalAmount",
                            "0"
                    );
        }

        tvProductPrice.setText(
                "Price: Rs. " + amount
        );

        // =====================================================
        // PRODUCT TOTAL
        // =====================================================

        String productTotal =
                getNumberOrString(
                        document,
                        "productTotal",
                        ""
                );

        if (productTotal.trim().isEmpty()) {

            productTotal =
                    getNumberOrString(
                            document,
                            "totalProductAmount",
                            ""
                    );
        }

        if (productTotal.trim().isEmpty()) {

            productTotal =
                    amount;
        }

        tvProductTotal.setText(
                "Rs. " + productTotal
        );

        // =====================================================
        // BUYER NAME
        // =====================================================

        String customerName =
                getValueAsString(
                        document,
                        "customerName",
                        ""
                );

        if (customerName.trim().isEmpty()) {

            customerName =
                    getValueAsString(
                            document,
                            "buyerName",
                            ""
                    );
        }

        if (customerName.trim().isEmpty()) {

            customerName =
                    "Not available";
        }

        tvBuyerName.setText(
                customerName
        );

        // =====================================================
        // BUYER PHONE
        // =====================================================

        String customerPhone =
                getValueAsString(
                        document,
                        "customerPhone",
                        ""
                );

        if (customerPhone.trim().isEmpty()) {

            customerPhone =
                    getValueAsString(
                            document,
                            "buyerPhone",
                            ""
                    );
        }

        if (customerPhone.trim().isEmpty()) {

            customerPhone =
                    "Not available";
        }

        tvBuyerPhone.setText(
                customerPhone
        );

        // =====================================================
        // BUYER ADDRESS
        // =====================================================

        String customerAddress =
                getValueAsString(
                        document,
                        "customerAddress",
                        ""
                );

        if (customerAddress.trim().isEmpty()) {

            customerAddress =
                    getValueAsString(
                            document,
                            "buyerAddress",
                            ""
                    );
        }

        if (customerAddress.trim().isEmpty()) {

            customerAddress =
                    getValueAsString(
                            document,
                            "address",
                            ""
                    );
        }

        if (customerAddress.trim().isEmpty()) {

            customerAddress =
                    "Not available";
        }

        tvBuyerAddress.setText(
                customerAddress
        );

        // =====================================================
        // PAYMENT METHOD
        // =====================================================

        String paymentMethod =
                getValueAsString(
                        document,
                        "paymentMethod",
                        ""
                );

        if (paymentMethod.trim().isEmpty()) {

            paymentMethod =
                    "Cash on Delivery";
        }

        tvPaymentMethod.setText(
                paymentMethod
        );

        // =====================================================
        // DELIVERY CHARGES
        // =====================================================

        String deliveryCharges =
                getNumberOrString(
                        document,
                        "deliveryCharges",
                        "0"
                );

        tvDeliveryCharges.setText(
                "Rs. " + deliveryCharges
        );

        // =====================================================
        // TOTAL AMOUNT
        // =====================================================

        String totalAmount =
                getNumberOrString(
                        document,
                        "totalAmount",
                        ""
                );

        if (totalAmount.trim().isEmpty()) {

            totalAmount =
                    getNumberOrString(
                            document,
                            "grandTotal",
                            ""
                    );
        }

        if (totalAmount.trim().isEmpty()) {

            totalAmount =
                    productTotal;
        }

        tvTotalAmount.setText(
                "Rs. " + totalAmount
        );

        // =====================================================
        // ACTION BUTTONS
        // =====================================================

        updateActionButtons(
                status
        );
    }

    // =========================================================
    // HANDLE ORDER ACTION
    // =========================================================

    private void handleOrderAction() {

        if (!isAdded()) {
            return;
        }

        if (orderDocumentId == null ||
                orderDocumentId.trim().isEmpty()) {

            return;
        }

        db.collection("orders")
                .document(orderDocumentId)
                .get()
                .addOnSuccessListener(
                        document -> {

                            if (!isAdded()) {
                                return;
                            }

                            if (!document.exists()) {

                                Toast.makeText(
                                        requireContext(),
                                        "Order not found.",
                                        Toast.LENGTH_SHORT
                                ).show();

                                return;
                            }

                            String status =
                                    normalizeStatus(
                                            getValueAsString(
                                                    document,
                                                    "status",
                                                    "pending"
                                            )
                                    );

                            // =========================================
                            // NEW / PENDING
                            //
                            // ACCEPT ORDER
                            // =========================================

                            if (status.equals("pending") ||
                                    status.equals("new")) {

                                updateOrderStatus(
                                        "processing"
                                );

                                return;
                            }

                            // =========================================
                            // PROCESSING / ACCEPTED
                            //
                            // SHIPMENT READY
                            // =========================================

                            if (status.equals("accepted") ||
                                    status.equals("processing")) {

                                updateOrderStatus(
                                        "shipment_ready"
                                );

                                return;
                            }

                            // =========================================
                            // SHIPMENT READY
                            //
                            // WAIT FOR ADMIN
                            // =========================================

                            if (status.equals("shipment_ready")) {

                                Toast.makeText(
                                        requireContext(),
                                        "Shipment is ready. Waiting for Admin to create delivery.",
                                        Toast.LENGTH_LONG
                                ).show();

                                return;
                            }

                            // =========================================
                            // SHIPPED
                            //
                            // NO SELLER ACTION
                            // =========================================

                            if (status.equals("shipped")) {

                                Toast.makeText(
                                        requireContext(),
                                        "Shipment has been handed over to courier.",
                                        Toast.LENGTH_SHORT
                                ).show();

                                return;
                            }

                            // =========================================
                            // DELIVERED
                            // =========================================

                            if (status.equals("delivered") ||
                                    status.equals("completed")) {

                                Toast.makeText(
                                        requireContext(),
                                        "Order has already been delivered.",
                                        Toast.LENGTH_SHORT
                                ).show();

                                return;
                            }

                            Toast.makeText(
                                    requireContext(),
                                    "No action available.",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                )
                .addOnFailureListener(
                        e -> {

                            if (!isAdded()) {
                                return;
                            }

                            Toast.makeText(
                                    requireContext(),
                                    "Failed to check order status: "
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
            String newStatus) {

        if (!isAdded()) {
            return;
        }

        if (orderDocumentId == null ||
                orderDocumentId.trim().isEmpty()) {

            return;
        }

        if (btnOrderAction != null) {

            btnOrderAction.setEnabled(false);
        }

        if (btnCancelOrder != null) {

            btnCancelOrder.setEnabled(false);
        }

        // =====================================================
        // READ BUYER ID
        // =====================================================

        db.collection("orders")
                .document(orderDocumentId)
                .get()
                .addOnSuccessListener(
                        document -> {

                            if (!isAdded()) {
                                return;
                            }

                            if (!document.exists()) {

                                enableButtons();

                                Toast.makeText(
                                        requireContext(),
                                        "Order not found.",
                                        Toast.LENGTH_SHORT
                                ).show();

                                return;
                            }

                            String buyerId =
                                    getValueAsString(
                                            document,
                                            "buyerId",
                                            ""
                                    );

                            // =========================================
                            // UPDATE FIRESTORE
                            // =========================================

                            db.collection("orders")
                                    .document(orderDocumentId)
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

                                                // =================================
                                                // BUYER NOTIFICATION
                                                // =================================

                                                if (newStatus.equals(
                                                        "processing"
                                                )) {

                                                    if (!buyerId
                                                            .trim()
                                                            .isEmpty()) {

                                                        BuyerNotificationHelper
                                                                .createOrderAcceptedNotification(
                                                                        buyerId,
                                                                        orderDocumentId
                                                                );
                                                    }
                                                }

                                                // =================================
                                                // UI
                                                // =================================

                                                tvOrderStatus.setText(
                                                        formatStatus(
                                                                newStatus
                                                        )
                                                );

                                                updateActionButtons(
                                                        newStatus
                                                );

                                                // =================================
                                                // SUCCESS MESSAGE
                                                // =================================

                                                String message;

                                                if (newStatus.equals(
                                                        "processing"
                                                )) {

                                                    message =
                                                            "Order accepted successfully.";

                                                } else if (newStatus.equals(
                                                        "shipment_ready"
                                                )) {

                                                    message =
                                                            "Shipment marked as ready. Admin will create delivery.";

                                                } else if (newStatus.equals(
                                                        "cancelled"
                                                )) {

                                                    message =
                                                            "Order cancelled.";

                                                } else {

                                                    message =
                                                            "Order status updated to "
                                                                    + formatStatus(
                                                                    newStatus
                                                            );
                                                }

                                                Toast.makeText(
                                                        requireContext(),
                                                        message,
                                                        Toast.LENGTH_LONG
                                                ).show();
                                            }
                                    )
                                    .addOnFailureListener(
                                            e -> {

                                                if (!isAdded()) {
                                                    return;
                                                }

                                                enableButtons();

                                                Toast.makeText(
                                                        requireContext(),
                                                        "Failed to update status: "
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

                            enableButtons();

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
    // ENABLE BUTTONS
    // =========================================================

    private void enableButtons() {

        if (btnOrderAction != null) {

            btnOrderAction.setEnabled(true);
        }

        if (btnCancelOrder != null) {

            btnCancelOrder.setEnabled(true);
        }
    }

    // =========================================================
    // ACTION BUTTON STATE
    // =========================================================

    private void updateActionButtons(
            String status) {

        if (status == null ||
                status.trim().isEmpty()) {

            status = "pending";
        }

        status =
                normalizeStatus(status);

        // =====================================================
        // NEW / PENDING
        //
        // ACCEPT + CANCEL
        // =====================================================

        if (status.equals("pending")) {

            btnOrderAction.setVisibility(
                    View.VISIBLE
            );

            btnCancelOrder.setVisibility(
                    View.VISIBLE
            );

            btnOrderAction.setText(
                    "Accept Order"
            );

            btnCancelOrder.setText(
                    "Cancel Order"
            );

            btnOrderAction.setEnabled(true);
            btnCancelOrder.setEnabled(true);

            return;
        }

        // =====================================================
        // PROCESSING
        //
        // SHIPMENT READY
        // =====================================================

        if (status.equals("processing") ||
                status.equals("accepted")) {

            btnOrderAction.setVisibility(
                    View.VISIBLE
            );

            btnCancelOrder.setVisibility(
                    View.GONE
            );

            btnOrderAction.setText(
                    "Shipment Ready"
            );

            btnOrderAction.setEnabled(true);

            return;
        }

        // =====================================================
        // SHIPMENT READY
        //
        // WAIT FOR ADMIN
        // =====================================================

        if (status.equals("shipment_ready")) {

            btnOrderAction.setVisibility(
                    View.VISIBLE
            );

            btnCancelOrder.setVisibility(
                    View.GONE
            );

            btnOrderAction.setText(
                    "Waiting for Admin"
            );

            btnOrderAction.setEnabled(false);

            return;
        }

        // =====================================================
        // SHIPPED
        //
        // COURIER HAS THE PARCEL
        // =====================================================

        if (status.equals("shipped")) {

            btnOrderAction.setVisibility(
                    View.VISIBLE
            );

            btnCancelOrder.setVisibility(
                    View.GONE
            );

            btnOrderAction.setText(
                    "Shipped"
            );

            btnOrderAction.setEnabled(false);

            return;
        }

        // =====================================================
        // DELIVERED
        // =====================================================

        if (status.equals("delivered") ||
                status.equals("completed")) {

            btnOrderAction.setVisibility(
                    View.GONE
            );

            btnCancelOrder.setVisibility(
                    View.GONE
            );

            return;
        }

        // =====================================================
        // CANCELLED
        // =====================================================

        if (status.equals("cancelled") ||
                status.equals("canceled") ||
                status.equals("rejected")) {

            btnOrderAction.setVisibility(
                    View.GONE
            );

            btnCancelOrder.setVisibility(
                    View.GONE
            );

            return;
        }

        // =====================================================
        // DEFAULT
        // =====================================================

        btnOrderAction.setVisibility(
                View.GONE
        );

        btnCancelOrder.setVisibility(
                View.GONE
        );
    }

    // =========================================================
    // NORMALIZE STATUS
    // =========================================================

    private String normalizeStatus(
            String status) {

        if (status == null ||
                status.trim().isEmpty()) {

            return "pending";
        }

        status =
                status
                        .toLowerCase()
                        .trim();

        // -----------------------------------------------------
        // OLD NEW STATUS
        // -----------------------------------------------------

        if (status.equals("new")) {

            return "pending";
        }

        // -----------------------------------------------------
        // OLD ACCEPTED STATUS
        // -----------------------------------------------------

        if (status.equals("accepted")) {

            return "accepted";
        }

        return status;
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

        if (status.equalsIgnoreCase("new") ||
                status.equalsIgnoreCase("pending")) {

            return "Pending";
        }

        if (status.equalsIgnoreCase("accepted")) {

            return "Accepted";
        }

        if (status.equalsIgnoreCase("processing")) {

            return "Processing";
        }

        if (status.equalsIgnoreCase("shipment_ready")) {

            return "Shipment Ready";
        }

        if (status.equalsIgnoreCase("shipped")) {

            return "Shipped";
        }

        if (status.equalsIgnoreCase("delivered") ||
                status.equalsIgnoreCase("completed")) {

            return "Delivered";
        }

        if (status.equalsIgnoreCase("cancelled") ||
                status.equalsIgnoreCase("canceled") ||
                status.equalsIgnoreCase("rejected")) {

            return "Cancelled";
        }

        return status;
    }

    // =========================================================
    // SAFE VALUE
    // =========================================================

    private String getValueAsString(
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
    // RESUME
    // =========================================================

    @Override
    public void onResume() {

        super.onResume();

        // -----------------------------------------------------
        // Reload latest order status
        // -----------------------------------------------------

        if (db != null &&
                orderDocumentId != null &&
                !orderDocumentId.trim().isEmpty()) {

            loadOrder();
        }
    }
}