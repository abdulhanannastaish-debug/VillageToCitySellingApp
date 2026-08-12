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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

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

        db = FirebaseFirestore.getInstance();

        // =====================================================
        // FIND VIEWS
        // =====================================================

        btnBack =
                view.findViewById(R.id.btn_back);

        tvOrderStatus =
                view.findViewById(R.id.tvOrderStatus);

        tvOrderId =
                view.findViewById(R.id.tvOrderId);

        tvProductName =
                view.findViewById(R.id.tvProductName);

        tvProductQuantity =
                view.findViewById(R.id.tvProductQuantity);

        tvProductPrice =
                view.findViewById(R.id.tvProductPrice);

        tvBuyerName =
                view.findViewById(R.id.tvBuyerName);

        tvBuyerPhone =
                view.findViewById(R.id.tvBuyerPhone);

        tvBuyerAddress =
                view.findViewById(R.id.tvBuyerAddress);

        tvPaymentMethod =
                view.findViewById(R.id.tvPaymentMethod);

        tvProductTotal =
                view.findViewById(R.id.tvProductTotal);

        tvDeliveryCharges =
                view.findViewById(R.id.tvDeliveryCharges);

        tvTotalAmount =
                view.findViewById(R.id.tvTotalAmount);

        btnOrderAction =
                view.findViewById(R.id.btnOrderAction);

        btnCancelOrder =
                view.findViewById(R.id.btnCancelOrder);

        // =====================================================
        // BACK
        // =====================================================

        btnBack.setOnClickListener(v -> {

            requireActivity()
                    .getSupportFragmentManager()
                    .popBackStack();

        });

        // =====================================================
        // ACCEPT ORDER
        // =====================================================

        btnOrderAction.setOnClickListener(v -> {

            /*
             * Seller accepts the order.
             *
             * NEW -> PROCESSING
             */

            updateOrderStatus(
                    "processing"
            );
        });

        // =====================================================
        // CANCEL ORDER
        // =====================================================

        btnCancelOrder.setOnClickListener(v -> {

            /*
             * Seller can cancel only a NEW order.
             *
             * NEW -> CANCELLED
             */

            updateOrderStatus(
                    "cancelled"
            );
        });

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
                .addOnSuccessListener(documentSnapshot -> {

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

                })
                .addOnFailureListener(e -> {

                    if (!isAdded()) {
                        return;
                    }

                    Toast.makeText(
                            requireContext(),
                            "Failed to load order: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    // =========================================================
    // DISPLAY ORDER
    // =========================================================

    private void displayOrderData(
            DocumentSnapshot document) {

        // =====================================================
        // ORDER ID
        // =====================================================

        String orderId =
                document.getString("id");

        if (orderId == null ||
                orderId.trim().isEmpty()) {

            orderId = document.getId();
        }

        tvOrderId.setText(
                "Order ID: #" + orderId
        );

        // =====================================================
        // STATUS
        // =====================================================

        String status =
                document.getString("status");

        if (status == null ||
                status.trim().isEmpty()) {

            status = "new";
        }

        status = status
                .toLowerCase()
                .trim();

        tvOrderStatus.setText(
                formatStatus(status)
        );

        // =====================================================
        // PRODUCT NAME
        // =====================================================

        String productName =
                document.getString("productName");

        if (productName == null ||
                productName.trim().isEmpty()) {

            productName = "Product";
        }

        tvProductName.setText(
                productName
        );

        // =====================================================
        // QUANTITY
        // =====================================================

        String quantity =
                document.getString("quantity");

        if (quantity == null ||
                quantity.trim().isEmpty()) {

            quantity = "1";
        }

        tvProductQuantity.setText(
                "Quantity: " + quantity
        );

        // =====================================================
        // PRICE
        // =====================================================

        String amount =
                document.getString("amount");

        if (amount == null ||
                amount.trim().isEmpty()) {

            amount = "0";
        }

        tvProductPrice.setText(
                "Price: Rs. " + amount
        );

        tvProductTotal.setText(
                "Rs. " + amount
        );

        // =====================================================
        // BUYER NAME
        // =====================================================

        String customerName =
                document.getString("customerName");

        if (customerName == null ||
                customerName.trim().isEmpty()) {

            customerName = "Not available";
        }

        tvBuyerName.setText(
                customerName
        );

        // =====================================================
        // BUYER PHONE
        // =====================================================

        String customerPhone =
                document.getString("customerPhone");

        if (customerPhone == null ||
                customerPhone.trim().isEmpty()) {

            customerPhone =
                    document.getString("buyerPhone");
        }

        if (customerPhone == null ||
                customerPhone.trim().isEmpty()) {

            customerPhone = "Not available";
        }

        tvBuyerPhone.setText(
                customerPhone
        );

        // =====================================================
        // BUYER ADDRESS
        // =====================================================

        String customerAddress =
                document.getString("customerAddress");

        if (customerAddress == null ||
                customerAddress.trim().isEmpty()) {

            customerAddress =
                    document.getString("buyerAddress");
        }

        if (customerAddress == null ||
                customerAddress.trim().isEmpty()) {

            customerAddress = "Not available";
        }

        tvBuyerAddress.setText(
                customerAddress
        );

        // =====================================================
        // PAYMENT METHOD
        // =====================================================

        String paymentMethod =
                document.getString("paymentMethod");

        if (paymentMethod == null ||
                paymentMethod.trim().isEmpty()) {

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
                document.getString("deliveryCharges");

        if (deliveryCharges == null ||
                deliveryCharges.trim().isEmpty()) {

            deliveryCharges = "0";
        }

        tvDeliveryCharges.setText(
                "Rs. " + deliveryCharges
        );

        // =====================================================
        // TOTAL
        // =====================================================

        String totalAmount =
                document.getString("totalAmount");

        if (totalAmount == null ||
                totalAmount.trim().isEmpty()) {

            totalAmount = amount;
        }

        tvTotalAmount.setText(
                "Rs. " + totalAmount
        );

        // =====================================================
        // BUTTONS
        // =====================================================

        updateActionButtons(
                status
        );
    }

    // =========================================================
    // UPDATE STATUS
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

        btnOrderAction.setEnabled(false);
        btnCancelOrder.setEnabled(false);

        db.collection("orders")
                .document(orderDocumentId)
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

                    tvOrderStatus.setText(
                            formatStatus(newStatus)
                    );

                    updateActionButtons(
                            newStatus
                    );

                })
                .addOnFailureListener(e -> {

                    if (!isAdded()) {
                        return;
                    }

                    btnOrderAction.setEnabled(true);
                    btnCancelOrder.setEnabled(true);

                    Toast.makeText(
                            requireContext(),
                            "Failed to update status: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    // =========================================================
    // ACTION BUTTON STATE
    // =========================================================

    private void updateActionButtons(
            String status) {

        if (status == null) {
            status = "new";
        }

        status = status
                .toLowerCase()
                .trim();

        // =====================================================
        // NEW
        //
        // Seller can Accept or Cancel
        // =====================================================

        if (status.equals("new")) {

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
        // Seller has accepted the order.
        // =====================================================

        if (status.equals("processing")) {

            btnOrderAction.setVisibility(
                    View.GONE
            );

            btnCancelOrder.setVisibility(
                    View.GONE
            );

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
                status.equals("canceled")) {

            btnOrderAction.setVisibility(
                    View.GONE
            );

            btnCancelOrder.setVisibility(
                    View.GONE
            );

            return;
        }

        // =====================================================
        // UNKNOWN STATUS
        // =====================================================

        btnOrderAction.setVisibility(
                View.GONE
        );

        btnCancelOrder.setVisibility(
                View.GONE
        );
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

        if (status.equalsIgnoreCase("processing")) {
            return "Processing";
        }

        if (status.equalsIgnoreCase("delivered") ||
                status.equalsIgnoreCase("completed")) {

            return "Delivered";
        }

        if (status.equalsIgnoreCase("cancelled") ||
                status.equalsIgnoreCase("canceled")) {

            return "Cancelled";
        }

        return status;
    }
}