package com.example.villagetocityreseilingapp.ui.main.buyer;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.villagetocityreseilingapp.R;
import com.example.villagetocityreseilingapp.adapter.BuyerCartAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuyerCartFragment extends Fragment
        implements BuyerCartAdapter.OnCartActionListener {

    // =========================================================
    // FIREBASE
    // =========================================================

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    // =========================================================
    // VIEWS
    // =========================================================

    private TextView tvCartCount;
    private RecyclerView recyclerCartItems;

    // =========================================================
    // DATA
    // =========================================================

    private final List<Map<String, Object>> cartList =
            new ArrayList<>();

    private BuyerCartAdapter cartAdapter;

    // =========================================================
    // EMPTY TOAST CONTROL
    // =========================================================

    private boolean emptyCartToastShown = false;

    // =========================================================
    // CREATE VIEW
    // =========================================================

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_buyer_cart,
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
        // FIND VIEWS
        // =====================================================

        tvCartCount =
                view.findViewById(
                        R.id.tvCartCount
                );

        recyclerCartItems =
                view.findViewById(
                        R.id.recyclerCartItems
                );

        // =====================================================
        // RECYCLER VIEW
        // =====================================================

        recyclerCartItems.setLayoutManager(
                new LinearLayoutManager(
                        requireContext()
                )
        );

        recyclerCartItems.setNestedScrollingEnabled(
                false
        );

        // =====================================================
        // ADAPTER
        // =====================================================

        cartAdapter =
                new BuyerCartAdapter(
                        cartList,
                        this
                );

        recyclerCartItems.setAdapter(
                cartAdapter
        );

        // =====================================================
        // LOAD CART
        // =====================================================

        loadCart();
    }

    // =========================================================
    // LOAD CART
    // =========================================================

    private void loadCart() {

        FirebaseUser currentUser =
                auth.getCurrentUser();

        // =====================================================
        // USER NOT LOGGED IN
        // =====================================================

        if (currentUser == null) {

            cartList.clear();

            updateCartCount();

            if (cartAdapter != null) {
                cartAdapter.notifyDataSetChanged();
            }

            showEmptyCartToastOnce();

            return;
        }

        // =====================================================
        // FIRESTORE CART
        // =====================================================

        db.collection("cart")
                .document(
                        currentUser.getUid()
                )
                .collection("items")
                .get()
                .addOnSuccessListener(
                        snapshots -> {

                            if (!isAdded()) {
                                return;
                            }

                            // =================================
                            // CLEAR OLD DATA
                            // =================================

                            cartList.clear();

                            // =================================
                            // READ CART ITEMS
                            // =================================

                            for (
                                    DocumentSnapshot document
                                    : snapshots.getDocuments()
                            ) {

                                Map<String, Object> item =
                                        new HashMap<>(
                                                document.getData()
                                        );

                                // =================================
                                // PRODUCT ID
                                // =================================

                                item.put(
                                        "productId",
                                        document.getId()
                                );

                                // =================================
                                // QUANTITY
                                // =================================

                                int quantity =
                                        getInt(
                                                item.get(
                                                        "quantity"
                                                ),
                                                1
                                        );

                                item.put(
                                        "quantity",
                                        quantity
                                );

                                // =================================
                                // ADD TO LIST
                                // =================================

                                cartList.add(
                                        item
                                );
                            }

                            // =================================
                            // SORT NEWEST FIRST
                            // =================================

                            sortCartNewestFirst();

                            // =================================
                            // UPDATE COUNT
                            // =================================

                            updateCartCount();

                            // =================================
                            // REFRESH ADAPTER
                            // =================================

                            if (cartAdapter != null) {

                                cartAdapter.notifyDataSetChanged();
                            }

                            // =================================
                            // EMPTY CHECK
                            // =================================

                            if (cartList.isEmpty()) {

                                showEmptyCartToastOnce();

                            } else {

                                emptyCartToastShown = false;
                            }
                        }
                )
                .addOnFailureListener(
                        e -> showError(
                                "Failed to load cart",
                                e
                        )
                );
    }

    // =========================================================
    // EMPTY CART TOAST
    // =========================================================

    private void showEmptyCartToastOnce() {

        if (!isAdded()) {
            return;
        }

        if (emptyCartToastShown) {
            return;
        }

        emptyCartToastShown = true;

        Toast.makeText(
                requireContext(),
                "Your cart is empty",
                Toast.LENGTH_SHORT
        ).show();
    }

    // =========================================================
    // SORT CART
    // =========================================================

    private void sortCartNewestFirst() {

        Collections.sort(
                cartList,
                new Comparator<Map<String, Object>>() {

                    @Override
                    public int compare(
                            Map<String, Object> item1,
                            Map<String, Object> item2) {

                        long time1 =
                                getAddedAt(
                                        item1.get(
                                                "addedAt"
                                        )
                                );

                        long time2 =
                                getAddedAt(
                                        item2.get(
                                                "addedAt"
                                        )
                                );

                        return Long.compare(
                                time2,
                                time1
                        );
                    }
                }
        );
    }

    // =========================================================
    // GET ADDED AT
    // =========================================================

    private long getAddedAt(
            Object value) {

        if (value instanceof Number) {

            return ((Number) value)
                    .longValue();
        }

        if (value != null) {

            try {

                return Long.parseLong(
                        String.valueOf(
                                value
                        )
                );

            } catch (Exception ignored) {
            }
        }

        return 0L;
    }

    // =========================================================
    // INCREASE
    // =========================================================

    @Override
    public void onIncrease(
            Map<String, Object> item) {

        String productId =
                getStringValue(
                        item,
                        "productId",
                        ""
                );

        int quantity =
                getInt(
                        item.get(
                                "quantity"
                        ),
                        1
                );

        quantity++;

        updateFirebaseQuantity(
                productId,
                quantity
        );
    }

    // =========================================================
    // DECREASE
    // =========================================================

    @Override
    public void onDecrease(
            Map<String, Object> item) {

        String productId =
                getStringValue(
                        item,
                        "productId",
                        ""
                );

        int quantity =
                getInt(
                        item.get(
                                "quantity"
                        ),
                        1
                );

        if (quantity <= 1) {

            Toast.makeText(
                    requireContext(),
                    "Minimum quantity is 1",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        quantity--;

        updateFirebaseQuantity(
                productId,
                quantity
        );
    }

    // =========================================================
    // UPDATE QUANTITY
    // =========================================================

    private void updateFirebaseQuantity(
            String productId,
            int quantity) {

        if (TextUtils.isEmpty(productId)) {
            return;
        }

        FirebaseUser currentUser =
                auth.getCurrentUser();

        if (currentUser == null) {
            return;
        }

        db.collection("cart")
                .document(
                        currentUser.getUid()
                )
                .collection("items")
                .document(
                        productId
                )
                .update(
                        "quantity",
                        quantity
                )
                .addOnSuccessListener(
                        unused -> {

                            if (!isAdded()) {
                                return;
                            }

                            loadCart();
                        }
                )
                .addOnFailureListener(
                        e -> showError(
                                "Failed to update quantity",
                                e
                        )
                );
    }

    // =========================================================
    // REMOVE
    // =========================================================

    @Override
    public void onRemove(
            Map<String, Object> item) {

        String productId =
                getStringValue(
                        item,
                        "productId",
                        ""
                );

        if (TextUtils.isEmpty(productId)) {
            return;
        }

        FirebaseUser currentUser =
                auth.getCurrentUser();

        if (currentUser == null) {
            return;
        }

        db.collection("cart")
                .document(
                        currentUser.getUid()
                )
                .collection("items")
                .document(
                        productId
                )
                .delete()
                .addOnSuccessListener(
                        unused -> {

                            if (!isAdded()) {
                                return;
                            }

                            Toast.makeText(
                                    requireContext(),
                                    "Item removed from cart",
                                    Toast.LENGTH_SHORT
                            ).show();

                            loadCart();
                        }
                )
                .addOnFailureListener(
                        e -> showError(
                                "Failed to remove item",
                                e
                        )
                );
    }

    // =========================================================
    // CHECKOUT
    // =========================================================
    //
    // IMPORTANT:
    // Delivery service yahan se PASS NAHI ho rahi.
    //
    // Checkout screen par:
    // TCS / Leopards select hoga.
    //
    // Is liye newInstance() ko exactly 6 arguments mil rahe hain.
    // =========================================================

    @Override
    public void onCheckout(
            Map<String, Object> item) {

        if (!isAdded()) {
            return;
        }

        // =====================================================
        // PRODUCT ID
        // =====================================================

        String productId =
                getStringValue(
                        item,
                        "productId",
                        ""
                );

        // =====================================================
        // PRODUCT NAME
        // =====================================================

        String productName =
                getStringValue(
                        item,
                        "name",
                        "Product"
                );

        // =====================================================
        // SELLER ID
        // =====================================================

        String sellerId =
                getStringValue(
                        item,
                        "sellerId",
                        ""
                );

        // =====================================================
        // SELLER NAME
        // =====================================================

        String sellerName =
                getStringValue(
                        item,
                        "sellerName",
                        ""
                );

        // =====================================================
        // QUANTITY
        // =====================================================

        int quantity =
                getInt(
                        item.get(
                                "quantity"
                        ),
                        1
                );

        // =====================================================
        // PRODUCT PRICE
        // =====================================================

        double price =
                getDouble(
                        item.get(
                                "price"
                        )
                );

        // =====================================================
        // PRODUCT TOTAL
        // =====================================================

        double amount =
                price * quantity;

        // =====================================================
        // OPEN CHECKOUT
        // =====================================================

        BuyerCheckoutFragment checkoutFragment =
                BuyerCheckoutFragment.newInstance(
                        productId,
                        productName,
                        sellerId,
                        sellerName,
                        String.valueOf(
                                quantity
                        ),
                        formatNumber(
                                amount
                        )
                );

        // =====================================================
        // NAVIGATION
        // =====================================================

        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.fragment_container,
                        checkoutFragment
                )
                .addToBackStack(null)
                .commit();
    }

    // =========================================================
    // CART COUNT
    // =========================================================

    private void updateCartCount() {

        int count =
                cartList.size();

        if (tvCartCount == null) {
            return;
        }

        if (count == 0) {

            tvCartCount.setText(
                    "0 Items"
            );

        } else if (count == 1) {

            tvCartCount.setText(
                    "1 Item"
            );

        } else {

            tvCartCount.setText(
                    count + " Items"
            );
        }
    }

    // =========================================================
    // EMPTY CART
    // =========================================================

    private void showEmptyCart() {

        cartList.clear();

        if (cartAdapter != null) {

            cartAdapter.notifyDataSetChanged();
        }

        if (tvCartCount != null) {

            tvCartCount.setText(
                    "0 Items"
            );
        }
    }

    // =========================================================
    // STRING VALUE
    // =========================================================

    private String getStringValue(
            Map<String, Object> data,
            String key,
            String defaultValue) {

        Object value =
                data.get(key);

        if (value == null) {
            return defaultValue;
        }

        String result =
                String.valueOf(
                        value
                ).trim();

        return result.isEmpty()
                ? defaultValue
                : result;
    }

    // =========================================================
    // INT VALUE
    // =========================================================

    private int getInt(
            Object value,
            int defaultValue) {

        if (value instanceof Number) {

            int result =
                    ((Number) value)
                            .intValue();

            return result > 0
                    ? result
                    : defaultValue;
        }

        if (value != null) {

            try {

                int result =
                        Integer.parseInt(
                                String.valueOf(
                                        value
                                )
                        );

                return result > 0
                        ? result
                        : defaultValue;

            } catch (Exception ignored) {
            }
        }

        return defaultValue;
    }

    // =========================================================
    // DOUBLE VALUE
    // =========================================================

    private double getDouble(
            Object value) {

        if (value instanceof Number) {

            return ((Number) value)
                    .doubleValue();
        }

        if (value != null) {

            try {

                return Double.parseDouble(
                        String.valueOf(
                                value
                        )
                );

            } catch (Exception ignored) {
            }
        }

        return 0;
    }

    // =========================================================
    // NUMBER FORMAT
    // =========================================================

    private String formatNumber(
            double value) {

        if (value == Math.floor(value)) {

            return String.valueOf(
                    (long) value
            );
        }

        return String.valueOf(
                value
        );
    }

    // =========================================================
    // ERROR
    // =========================================================

    private void showError(
            String message,
            Exception e) {

        if (!isAdded()) {
            return;
        }

        Toast.makeText(
                requireContext(),
                message
                        + ": "
                        + e.getMessage(),
                Toast.LENGTH_LONG
        ).show();
    }

    // =========================================================
    // IMPORTANT
    // =========================================================
    //
    // loadCart() ko onResume() mein dobara call nahi karna.
    //
    // =========================================================
}