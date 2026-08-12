package com.example.villagetocityreseilingapp.ui.main.seller;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.villagetocityreseilingapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SellerAddProductFragment extends Fragment {

    private EditText etItemName;
    private EditText etItemPrice;
    private EditText etItemQuantity;
    private EditText etItemDescription;

    private Button btnAddItem;
    private ImageButton btnBack;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    public SellerAddProductFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_seller_add_product,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        // ================= FIREBASE =================

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // ================= FIND VIEWS =================

        etItemName =
                view.findViewById(R.id.et_item_name);

        etItemPrice =
                view.findViewById(R.id.et_item_price);

        etItemQuantity =
                view.findViewById(R.id.et_item_quantity);

        etItemDescription =
                view.findViewById(R.id.et_item_description);

        btnAddItem =
                view.findViewById(R.id.btn_add_item);

        btnBack =
                view.findViewById(R.id.btn_back);

        // ================= BACK ARROW =================

        btnBack.setOnClickListener(v ->
                goBackToProducts()
        );

        // ================= ADD PRODUCT =================

        btnAddItem.setOnClickListener(v ->
                saveProduct()
        );
    }

    // =========================================================
    // SAVE PRODUCT
    // =========================================================

    private void saveProduct() {

        String productName =
                etItemName.getText().toString().trim();

        String priceText =
                etItemPrice.getText().toString().trim();

        String quantityText =
                etItemQuantity.getText().toString().trim();

        String description =
                etItemDescription.getText().toString().trim();

        // ================= VALIDATION =================

        if (TextUtils.isEmpty(productName)) {

            etItemName.setError("Enter product name");
            etItemName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(priceText)) {

            etItemPrice.setError("Enter product price");
            etItemPrice.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(quantityText)) {

            etItemQuantity.setError("Enter product quantity");
            etItemQuantity.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(description)) {

            etItemDescription.setError(
                    "Enter product description"
            );

            etItemDescription.requestFocus();
            return;
        }

        // ================= CURRENT SELLER =================

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

        String sellerId =
                currentUser.getUid();

        // ================= PRICE =================

        double price;

        try {

            price =
                    Double.parseDouble(priceText);

        } catch (NumberFormatException e) {

            etItemPrice.setError(
                    "Enter a valid price"
            );

            etItemPrice.requestFocus();
            return;
        }

        // ================= QUANTITY =================

        int quantity;

        try {

            quantity =
                    Integer.parseInt(quantityText);

        } catch (NumberFormatException e) {

            etItemQuantity.setError(
                    "Enter a valid quantity"
            );

            etItemQuantity.requestFocus();
            return;
        }

        // ================= DISABLE BUTTON =================

        btnAddItem.setEnabled(false);

        // ================= PRODUCT ID =================

        String productId =
                db.collection("products")
                        .document()
                        .getId();

        // ================= PRODUCT DATA =================

        Map<String, Object> productData =
                new HashMap<>();

        productData.put(
                "productId",
                productId
        );

        productData.put(
                "sellerId",
                sellerId
        );

        productData.put(
                "name",
                productName
        );

        productData.put(
                "price",
                price
        );

        productData.put(
                "quantity",
                quantity
        );

        productData.put(
                "description",
                description
        );

        productData.put(
                "status",
                "available"
        );

        productData.put(
                "createdAt",
                System.currentTimeMillis()
        );

        // =====================================================
        // SAVE TO FIRESTORE
        // =====================================================

        db.collection("products")
                .document(productId)
                .set(productData)
                .addOnSuccessListener(unused -> {

                    btnAddItem.setEnabled(true);

                    Toast.makeText(
                            requireContext(),
                            "Product added successfully!",
                            Toast.LENGTH_SHORT
                    ).show();

                    goBackToProducts();

                })
                .addOnFailureListener(e -> {

                    btnAddItem.setEnabled(true);

                    Toast.makeText(
                            requireContext(),
                            "Failed to add product: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
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