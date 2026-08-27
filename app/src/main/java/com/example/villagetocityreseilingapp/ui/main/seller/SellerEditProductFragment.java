package com.example.villagetocityreseilingapp.ui.main.seller;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.example.villagetocityreseilingapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.Timestamp;

import java.util.HashMap;
import java.util.Map;

public class SellerEditProductFragment extends Fragment {

    // =========================================================
    // PRODUCT ID
    // =========================================================

    private static final String ARG_PRODUCT_ID = "productId";

    private String productId;

    // =========================================================
    // VIEWS
    // =========================================================

    private EditText etProductName;
    private EditText etProductPrice;
    private EditText etProductStock;
    private EditText etProductUnit;
    private EditText etMinimumOrder;
    private EditText etProductDescription;

    private AppCompatButton btnUpdateProduct;
    private ImageButton btnBack;

    // =========================================================
    // FIREBASE
    // =========================================================

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public SellerEditProductFragment() {
    }

    // =========================================================
    // NEW INSTANCE
    // =========================================================

    public static SellerEditProductFragment newInstance(
            String productId) {

        SellerEditProductFragment fragment =
                new SellerEditProductFragment();

        Bundle args = new Bundle();

        args.putString(
                ARG_PRODUCT_ID,
                productId
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

            productId =
                    getArguments()
                            .getString(ARG_PRODUCT_ID);
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
                R.layout.fragment_seller_edit_product,
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

        auth =
                FirebaseAuth.getInstance();

        db =
                FirebaseFirestore.getInstance();

        // =====================================================
        // FIND VIEWS
        // =====================================================

        btnBack =
                view.findViewById(
                        R.id.btn_back
                );

        etProductName =
                view.findViewById(
                        R.id.etProductName
                );

        etProductPrice =
                view.findViewById(
                        R.id.etProductPrice
                );

        etProductStock =
                view.findViewById(
                        R.id.etProductStock
                );

        etProductUnit =
                view.findViewById(
                        R.id.etProductUnit
                );

        etMinimumOrder =
                view.findViewById(
                        R.id.etMinimumOrder
                );

        etProductDescription =
                view.findViewById(
                        R.id.etProductDescription
                );

        btnUpdateProduct =
                view.findViewById(
                        R.id.btnUpdateProduct
                );

        // =====================================================
        // BACK BUTTON
        // =====================================================

        btnBack.setOnClickListener(
                v -> goBackToProducts()
        );

        // =====================================================
        // PRODUCT ID CHECK
        // =====================================================

        if (TextUtils.isEmpty(productId)) {

            Toast.makeText(
                    requireContext(),
                    "Product ID not found.",
                    Toast.LENGTH_SHORT
            ).show();

            goBackToProducts();

            return;
        }

        // =====================================================
        // LOAD PRODUCT
        // =====================================================

        loadProduct();

        // =====================================================
        // UPDATE BUTTON
        // =====================================================

        btnUpdateProduct.setOnClickListener(
                v -> updateProduct()
        );
    }

    // =========================================================
    // LOAD PRODUCT
    // =========================================================

    private void loadProduct() {

        db.collection("products")
                .document(productId)
                .get()
                .addOnSuccessListener(
                        documentSnapshot -> {

                            if (!isAdded()) {
                                return;
                            }

                            if (!documentSnapshot.exists()) {

                                Toast.makeText(
                                        requireContext(),
                                        "Product not found.",
                                        Toast.LENGTH_SHORT
                                ).show();

                                goBackToProducts();

                                return;
                            }

                            FirebaseUser currentUser =
                                    auth.getCurrentUser();

                            if (currentUser == null) {

                                Toast.makeText(
                                        requireContext(),
                                        "Seller is not logged in.",
                                        Toast.LENGTH_SHORT
                                ).show();

                                return;
                            }

                            // =================================
                            // SELLER SECURITY CHECK
                            // =================================

                            String sellerId =
                                    documentSnapshot.getString(
                                            "sellerId"
                                    );

                            if (
                                    sellerId == null
                                            ||
                                            !sellerId.equals(
                                                    currentUser.getUid()
                                            )
                            ) {

                                Toast.makeText(
                                        requireContext(),
                                        "You cannot edit this product.",
                                        Toast.LENGTH_LONG
                                ).show();

                                goBackToProducts();

                                return;
                            }

                            // =================================
                            // PRODUCT NAME
                            // =================================

                            String name =
                                    documentSnapshot.getString(
                                            "name"
                                    );

                            if (!TextUtils.isEmpty(name)) {

                                etProductName.setText(name);
                            }

                            // =================================
                            // DESCRIPTION
                            // =================================

                            String description =
                                    documentSnapshot.getString(
                                            "description"
                                    );

                            if (!TextUtils.isEmpty(description)) {

                                etProductDescription.setText(
                                        description
                                );
                            }

                            // =================================
                            // PRICE
                            // =================================

                            Object price =
                                    documentSnapshot.get("price");

                            if (price != null) {

                                etProductPrice.setText(
                                        formatNumber(price)
                                );
                            }

                            // =================================
                            // STOCK
                            // =================================

                            Object stock =
                                    documentSnapshot.get(
                                            "availableStock"
                                    );

                            if (stock == null) {

                                stock =
                                        documentSnapshot.get(
                                                "totalStock"
                                        );
                            }

                            if (stock == null) {

                                stock =
                                        documentSnapshot.get(
                                                "quantity"
                                        );
                            }

                            if (stock != null) {

                                etProductStock.setText(
                                        formatNumber(stock)
                                );
                            }

                            // =================================
                            // UNIT
                            // =================================

                            String unit =
                                    documentSnapshot.getString(
                                            "unitType"
                                    );

                            if (TextUtils.isEmpty(unit)) {

                                unit =
                                        documentSnapshot.getString(
                                                "unit"
                                        );
                            }

                            if (!TextUtils.isEmpty(unit)) {

                                etProductUnit.setText(unit);
                            }

                            // =================================
                            // MINIMUM ORDER
                            // =================================

                            Object minimumOrder =
                                    documentSnapshot.get(
                                            "minimumOrder"
                                    );

                            if (minimumOrder == null) {

                                minimumOrder =
                                        documentSnapshot.get(
                                                "minOrderQuantity"
                                        );
                            }

                            if (minimumOrder != null) {

                                etMinimumOrder.setText(
                                        formatNumber(
                                                minimumOrder
                                        )
                                );
                            }
                        }
                )
                .addOnFailureListener(
                        e -> {

                            if (!isAdded()) {
                                return;
                            }

                            Toast.makeText(
                                    requireContext(),
                                    "Failed to load product: "
                                            + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }

    // =========================================================
    // UPDATE PRODUCT
    // =========================================================

    private void updateProduct() {

        // =====================================================
        // GET VALUES
        // =====================================================

        String name =
                etProductName
                        .getText()
                        .toString()
                        .trim();

        String priceText =
                etProductPrice
                        .getText()
                        .toString()
                        .trim();

        String stockText =
                etProductStock
                        .getText()
                        .toString()
                        .trim();

        String unit =
                etProductUnit
                        .getText()
                        .toString()
                        .trim();

        String minimumOrderText =
                etMinimumOrder
                        .getText()
                        .toString()
                        .trim();

        String description =
                etProductDescription
                        .getText()
                        .toString()
                        .trim();

        // =====================================================
        // VALIDATE NAME
        // =====================================================

        if (TextUtils.isEmpty(name)) {

            etProductName.setError(
                    "Enter product name"
            );

            etProductName.requestFocus();

            return;
        }

        // =====================================================
        // VALIDATE PRICE
        // =====================================================

        if (TextUtils.isEmpty(priceText)) {

            etProductPrice.setError(
                    "Enter product price"
            );

            etProductPrice.requestFocus();

            return;
        }

        // =====================================================
        // VALIDATE STOCK
        // =====================================================

        if (TextUtils.isEmpty(stockText)) {

            etProductStock.setError(
                    "Enter available stock"
            );

            etProductStock.requestFocus();

            return;
        }

        // =====================================================
        // VALIDATE UNIT
        // =====================================================

        if (TextUtils.isEmpty(unit)) {

            etProductUnit.setError(
                    "Enter unit"
            );

            etProductUnit.requestFocus();

            return;
        }

        // =====================================================
        // VALIDATE MINIMUM ORDER
        // =====================================================

        if (TextUtils.isEmpty(minimumOrderText)) {

            etMinimumOrder.setError(
                    "Enter minimum order"
            );

            etMinimumOrder.requestFocus();

            return;
        }

        // =====================================================
        // VALIDATE DESCRIPTION
        // =====================================================

        if (TextUtils.isEmpty(description)) {

            etProductDescription.setError(
                    "Enter product description"
            );

            etProductDescription.requestFocus();

            return;
        }

        // =====================================================
        // CURRENT USER
        // =====================================================

        FirebaseUser currentUser =
                auth.getCurrentUser();

        if (currentUser == null) {

            Toast.makeText(
                    requireContext(),
                    "Seller is not logged in.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // =====================================================
        // PRICE
        // =====================================================

        final double price;

        try {

            price =
                    Double.parseDouble(
                            priceText
                    );

        } catch (NumberFormatException e) {

            etProductPrice.setError(
                    "Enter a valid price"
            );

            etProductPrice.requestFocus();

            return;
        }

        if (price <= 0) {

            etProductPrice.setError(
                    "Price must be greater than 0"
            );

            etProductPrice.requestFocus();

            return;
        }

        // =====================================================
        // STOCK
        // =====================================================

        final double stock;

        try {

            stock =
                    Double.parseDouble(
                            stockText
                    );

        } catch (NumberFormatException e) {

            etProductStock.setError(
                    "Enter a valid stock"
            );

            etProductStock.requestFocus();

            return;
        }

        if (stock < 0) {

            etProductStock.setError(
                    "Stock cannot be negative"
            );

            etProductStock.requestFocus();

            return;
        }

        // =====================================================
        // MINIMUM ORDER
        // =====================================================

        final double minimumOrder;

        try {

            minimumOrder =
                    Double.parseDouble(
                            minimumOrderText
                    );

        } catch (NumberFormatException e) {

            etMinimumOrder.setError(
                    "Enter a valid minimum order"
            );

            etMinimumOrder.requestFocus();

            return;
        }

        if (minimumOrder <= 0) {

            etMinimumOrder.setError(
                    "Minimum order must be greater than 0"
            );

            etMinimumOrder.requestFocus();

            return;
        }

        if (minimumOrder > stock) {

            etMinimumOrder.setError(
                    "Minimum order cannot exceed stock"
            );

            etMinimumOrder.requestFocus();

            return;
        }

        // =====================================================
        // DISABLE BUTTON
        // =====================================================

        btnUpdateProduct.setEnabled(false);

        // =====================================================
        // UPDATE DATA
        // =====================================================

        Map<String, Object> updateData =
                new HashMap<>();

        updateData.put(
                "name",
                name
        );

        updateData.put(
                "price",
                price
        );

        updateData.put(
                "pricePerUnit",
                price
        );

        updateData.put(
                "availableStock",
                stock
        );

        updateData.put(
                "totalStock",
                stock
        );

        updateData.put(
                "quantity",
                stock
        );

        updateData.put(
                "unitType",
                unit
        );

        updateData.put(
                "unit",
                unit
        );

        updateData.put(
                "minimumOrder",
                minimumOrder
        );

        updateData.put(
                "minOrderQuantity",
                minimumOrder
        );

        updateData.put(
                "description",
                description
        );

        updateData.put(
                "updatedAt",
                Timestamp.now()
        );

        // =====================================================
        // STATUS
        // =====================================================

        if (stock <= 0) {

            updateData.put(
                    "status",
                    "outOfStock"
            );

        } else {

            updateData.put(
                    "status",
                    "available"
            );
        }

        // =====================================================
        // FIRESTORE UPDATE
        // =====================================================

        db.collection("products")
                .document(productId)
                .update(updateData)
                .addOnSuccessListener(
                        unused -> {

                            if (!isAdded()) {
                                return;
                            }

                            btnUpdateProduct.setEnabled(
                                    true
                            );

                            Toast.makeText(
                                    requireContext(),
                                    "Product updated successfully!",
                                    Toast.LENGTH_SHORT
                            ).show();

                            goBackToProducts();
                        }
                )
                .addOnFailureListener(
                        e -> {

                            if (!isAdded()) {
                                return;
                            }

                            btnUpdateProduct.setEnabled(
                                    true
                            );

                            Toast.makeText(
                                    requireContext(),
                                    "Failed to update product: "
                                            + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }

    // =========================================================
    // FORMAT NUMBER
    // =========================================================

    private String formatNumber(
            Object value) {

        if (value == null) {
            return "";
        }

        if (value instanceof Number) {

            double number =
                    ((Number) value)
                            .doubleValue();

            if (number == Math.floor(number)) {

                return String.valueOf(
                        (long) number
                );
            }

            return String.valueOf(number);
        }

        String text =
                String.valueOf(value)
                        .trim();

        if (text.isEmpty()) {
            return "";
        }

        try {

            double number =
                    Double.parseDouble(text);

            if (number == Math.floor(number)) {

                return String.valueOf(
                        (long) number
                );
            }

            return String.valueOf(number);

        } catch (Exception e) {

            return text;
        }
    }

    // =========================================================
    // BACK TO PRODUCTS
    // =========================================================

    private void goBackToProducts() {

        if (!isAdded()) {
            return;
        }

        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.fragment_container,
                        new SellerProductFragment()
                )
                .commit();
    }
}