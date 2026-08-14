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
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
    // =========================================================

    private LinearLayout ordersContainer;

    // =========================================================
    // CREATE VIEW
    // =========================================================

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

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

        db = FirebaseFirestore.getInstance();

        auth = FirebaseAuth.getInstance();

        ordersContainer =
                view.findViewById(
                        R.id.ordersContainer
                );

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

        String buyerId =
                currentUser.getUid();

        ordersContainer.removeAllViews();

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

                            if (snapshots.isEmpty()) {

                                showNoOrders(
                                        "No orders found."
                                );

                                return;
                            }

                            for (
                                    DocumentSnapshot document
                                    : snapshots.getDocuments()
                            ) {

                                addOrderCard(
                                        document
                                );
                            }
                        }
                )
                .addOnFailureListener(
                        e -> loadBuyerOrdersFallback()
                );
    }

    // =========================================================
    // FALLBACK
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

        db.collection("orders")
                .whereEqualTo(
                        "buyerId",
                        currentUser.getUid()
                )
                .get()
                .addOnSuccessListener(
                        snapshots -> {

                            if (!isAdded()) {
                                return;
                            }

                            ordersContainer.removeAllViews();

                            if (snapshots.isEmpty()) {

                                showNoOrders(
                                        "No orders found."
                                );

                                return;
                            }

                            List<DocumentSnapshot> orderList =
                                    new ArrayList<>(
                                            snapshots.getDocuments()
                                    );

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
                                    DocumentSnapshot document
                                    : orderList
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

        Button btnRateProduct =
                orderView.findViewById(
                        R.id.btnRateProduct
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
        // QUANTITY
        // =====================================================

        String quantity =
                getStringValue(
                        data,
                        "quantity",
                        "1"
                );

        // =====================================================
        // TOTAL
        // =====================================================

        String totalAmount =
                getStringValue(
                        data,
                        "totalAmount",
                        "0"
                );

        // =====================================================
        // DATE
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

        // =====================================================
        // SET BASIC DATA
        // =====================================================

        tvOrderProduct.setText(
                productName
        );

        tvOrderId.setText(
                "Order ID: " + orderId
        );

        tvOrderDate.setText(
                orderDate
        );

        tvOrderPrice.setText(
                "Rs " + totalAmount
        );

        tvOrderQuantity.setText(
                "Qty: " + quantity
        );

        // =====================================================
        // SET STATUS
        // =====================================================

        setOrderStatus(
                tvOrderStatus,
                orderStatus
        );

        // =====================================================
        // HIDE RATING BUTTON BY DEFAULT
        // =====================================================

        btnRateProduct.setVisibility(
                View.INVISIBLE
        );

        btnRateProduct.setEnabled(
                false
        );

        // =====================================================
        // ACCEPTED
        // CHECK REVIEW BEFORE SHOWING BUTTON
        // =====================================================

        if (isAccepted(orderStatus)) {

            checkAlreadyReviewed(
                    btnRateProduct,
                    productId,
                    productName,
                    sellerId,
                    orderId
            );
        }

        // =====================================================
        // CARD CLICK
        // =====================================================

        orderView.setOnClickListener(
                v -> {

                    Toast.makeText(
                            requireContext(),
                            "Order ID: " + orderId,
                            Toast.LENGTH_SHORT
                    ).show();
                }
        );

        // =====================================================
        // ADD CARD
        // =====================================================

        ordersContainer.addView(
                orderView
        );
    }

    // =========================================================
    // ORDER STATUS UI
    // =========================================================

    private void setOrderStatus(
            TextView statusView,
            String status) {

        String cleanStatus =
                status == null
                        ? "pending"
                        : status.trim()
                        .toLowerCase(
                                Locale.getDefault()
                        );

        // =====================================================
        // PENDING - YELLOW
        // =====================================================

        if (cleanStatus.equals("pending")
                || cleanStatus.equals("new")
                || cleanStatus.isEmpty()) {

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
        // ACCEPTED - GREEN
        // =====================================================

        if (cleanStatus.equals("accepted")) {

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

            return;
        }

        // =====================================================
        // DELIVERED - BLUE
        // =====================================================

        if (cleanStatus.equals("delivered")) {

            statusView.setText(
                    "DELIVERED"
            );

            statusView.setTextColor(
                    Color.WHITE
            );

            statusView.setBackgroundColor(
                    Color.rgb(
                            25,
                            118,
                            210
                    )
            );

            return;
        }

        // =====================================================
        // REJECTED - RED
        // =====================================================

        if (cleanStatus.equals("rejected")) {

            statusView.setText(
                    "REJECTED"
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
        // CANCELLED - RED
        // =====================================================

        if (cleanStatus.equals("cancelled")) {

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

            return;
        }

        // =====================================================
        // UNKNOWN STATUS
        // =====================================================

        statusView.setText(
                cleanStatus.toUpperCase(
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
    // ACCEPTED CHECK
    // =========================================================

    private boolean isAccepted(
            String status) {

        if (status == null) {
            return false;
        }

        return status.trim()
                .equalsIgnoreCase(
                        "accepted"
                );
    }

    // =========================================================
    // CHECK ALREADY REVIEWED
    // =========================================================

    private void checkAlreadyReviewed(
            Button btnRate,
            String productId,
            String productName,
            String sellerId,
            String orderId) {

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

                            // =================================================
                            // ALREADY REVIEWED
                            // =================================================

                            if (!snapshots.isEmpty()) {

                                setAlreadyRated(
                                        btnRate
                                );

                                btnRate.setVisibility(
                                        View.VISIBLE
                                );

                                return;
                            }

                            // =================================================
                            // NOT REVIEWED YET
                            // =================================================

                            btnRate.setText(
                                    "Rate & Review Product"
                            );

                            btnRate.setEnabled(
                                    true
                            );

                            btnRate.setVisibility(
                                    View.VISIBLE
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

                            // =================================================
                            // IF CHECK FAILS
                            // KEEP BUTTON HIDDEN
                            // =================================================

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
            Button btnRate) {

        btnRate.setText(
                "Already Rated"
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
            Button rateButton) {

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
        // TITLE
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

                    Button submitButton =
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
                                        reviewText.length() > 300
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
            Button rateButton,
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
                .add(reviewData)
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

                            rateButton.setText(
                                    "Rate & Review Product"
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
    // GET CREATED TIME
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

        if (createdAt instanceof Timestamp) {

            return ((Timestamp) createdAt)
                    .toDate()
                    .getTime();
        }

        if (createdAt instanceof Date) {

            return ((Date) createdAt)
                    .getTime();
        }

        if (createdAt instanceof Number) {

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

        if (orderDate instanceof Timestamp) {

            return ((Timestamp) orderDate)
                    .toDate()
                    .getTime();
        }

        if (orderDate instanceof Date) {

            return ((Date) orderDate)
                    .getTime();
        }

        if (orderDate instanceof Number) {

            return ((Number) orderDate)
                    .longValue();
        }

        if (orderDate != null) {

            try {

                return Long.parseLong(
                        String.valueOf(
                                orderDate
                        )
                );

            } catch (Exception ignored) {
            }
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

        if (TextUtils.isEmpty(result)) {

            return defaultValue;
        }

        return result;
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

        // Email is intentionally NOT used as buyer name.
        return "Buyer";
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
                db != null
                        && ordersContainer != null
        ) {

            loadBuyerOrders();
        }
    }
}