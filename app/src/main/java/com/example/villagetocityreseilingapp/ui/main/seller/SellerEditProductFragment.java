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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SellerEditProductFragment extends Fragment {

    // =========================================================
    // PRODUCT ID
    // =========================================================

    private static final String ARG_PRODUCT_ID =
            "productId";

    private String productId;

    // =========================================================
    // VIEWS
    // =========================================================

    private EditText etProductName;
    private EditText etProductPrice;
    private EditText etProductStock;
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
        // Required empty public constructor
    }

    // =========================================================
    // NEW INSTANCE
    // =========================================================

    public static SellerEditProductFragment newInstance(
            String productId) {

        SellerEditProductFragment fragment =
                new SellerEditProductFragment();

        Bundle args =
                new Bundle();

        args.putString(
                ARG_PRODUCT_ID,
                productId
        );

        fragment.setArguments(
                args
        );

        return fragment;
    }

    // =========================================================
    // ON CREATE
    // =========================================================

    @Override
    public void onCreate(
            Bundle savedInstanceState) {

        super.onCreate(
                savedInstanceState
        );

        if (getArguments() != null) {

            productId =
                    getArguments()
                            .getString(
                                    ARG_PRODUCT_ID
                            );
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

        etProductDescription =
                view.findViewById(
                        R.id.etProductDescription
                );

        btnUpdateProduct =
                view.findViewById(
                        R.id.btnUpdateProduct
                );

        // =====================================================
        // BACK
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
        // UPDATE
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
                            // NAME
                            // =================================

                            String name =
                                    documentSnapshot.getString(
                                            "name"
                                    );

                            if (!TextUtils.isEmpty(name)) {

                                etProductName.setText(
                                        name
                                );
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

                            Object priceObject =
                                    documentSnapshot.get(
                                            "price"
                                    );

                            if (priceObject != null) {

                                etProductPrice.setText(
                                        formatNumber(
                                                priceObject
                                        )
                                );
                            }

                            // =================================
                            // STOCK
                            // =================================

                            Object stockObject =
                                    documentSnapshot.get(
                                            "availableStock"
                                    );

                            if (stockObject == null) {

                                stockObject =
                                        documentSnapshot.get(
                                                "totalStock"
                                        );
                            }

                            if (stockObject == null) {

                                stockObject =
                                        documentSnapshot.get(
                                                "quantity"
                                        );
                            }

                            if (stockObject != null) {

                                etProductStock.setText(
                                        formatNumber(
                                                stockObject
                                        )
                                );

                            } else {

                                etProductStock.setText(
                                        "0"
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

        String description =
                etProductDescription
                        .getText()
                        .toString()
                        .trim();

        // =====================================================
        // NAME
        // =====================================================

        if (TextUtils.isEmpty(name)) {

            etProductName.setError(
                    "Enter product name"
            );

            etProductName.requestFocus();

            return;
        }

        // =====================================================
        // PRICE
        // =====================================================

        if (TextUtils.isEmpty(priceText)) {

            etProductPrice.setError(
                    "Enter product price"
            );

            etProductPrice.requestFocus();

            return;
        }

        // =====================================================
        // STOCK
        // =====================================================

        if (TextUtils.isEmpty(stockText)) {

            etProductStock.setError(
                    "Enter available stock"
            );

            etProductStock.requestFocus();

            return;
        }

        // =====================================================
        // DESCRIPTION
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

        double price;

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

        if (price < 0) {

            etProductPrice.setError(
                    "Price cannot be negative"
            );

            etProductPrice.requestFocus();

            return;
        }

        // =====================================================
        // STOCK
        // =====================================================

        int availableStock;

        try {

            availableStock =
                    Integer.parseInt(
                            stockText
                    );

        } catch (NumberFormatException e) {

            etProductStock.setError(
                    "Enter a valid stock"
            );

            etProductStock.requestFocus();

            return;
        }

        if (availableStock < 0) {

            etProductStock.setError(
                    "Stock cannot be negative"
            );

            etProductStock.requestFocus();

            return;
        }

        // =====================================================
        // DISABLE BUTTON
        // =====================================================

        btnUpdateProduct.setEnabled(
                false
        );

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
                "availableStock",
                availableStock
        );

        updateData.put(
                "totalStock",
                availableStock
        );

        updateData.put(
                "quantity",
                availableStock
        );

        updateData.put(
                "description",
                description
        );

        // =====================================================
        // STATUS
        // =====================================================

        if (availableStock <= 0) {

            updateData.put(
                    "status",
                    "outOfStock"
            );

        } else {

            updateData.put(
                    "status",
                    "active"
            );
        }

        // =====================================================
        // UPDATE FIRESTORE
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

            double doubleValue =
                    ((Number) value)
                            .doubleValue();

            if (
                    doubleValue
                            == Math.floor(
                            doubleValue
                    )
            ) {

                return String.valueOf(
                        (long) doubleValue
                );
            }

            return String.valueOf(
                    doubleValue
            );
        }

        String stringValue =
                String.valueOf(
                        value
                ).trim();

        if (TextUtils.isEmpty(stringValue)) {
            return "";
        }

        try {

            double doubleValue =
                    Double.parseDouble(
                            stringValue
                    );

            if (
                    doubleValue
                            == Math.floor(
                            doubleValue
                    )
            ) {

                return String.valueOf(
                        (long) doubleValue
                );
            }

            return String.valueOf(
                    doubleValue
            );

        } catch (Exception e) {

            return stringValue;
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