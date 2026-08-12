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
import androidx.appcompat.app.AlertDialog;
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

    private static final String ARG_PRODUCT_ID = "productId";

    private String productId;


    // =========================================================
    // VIEWS
    // =========================================================

    private EditText etProductName;
    private EditText etProductPrice;
    private EditText etProductQuantity;
    private EditText etProductDescription;

    private Button btnUpdateProduct;
    private Button btnDeleteProduct;

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
                    getArguments().getString(
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

        etProductQuantity =
                view.findViewById(
                        R.id.etProductQuantity
                );

        etProductDescription =
                view.findViewById(
                        R.id.etProductDescription
                );

        btnUpdateProduct =
                view.findViewById(
                        R.id.btnUpdateProduct
                );

        btnDeleteProduct =
                view.findViewById(
                        R.id.btnDeleteProduct
                );


        // =====================================================
        // BACK BUTTON
        // =====================================================

        btnBack.setOnClickListener(v ->
                goBackToProducts()
        );


        // =====================================================
        // CHECK PRODUCT ID
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
        // UPDATE PRODUCT
        // =====================================================

        btnUpdateProduct.setOnClickListener(v ->
                updateProduct()
        );


        // =====================================================
        // DELETE PRODUCT
        // =====================================================

        btnDeleteProduct.setOnClickListener(v ->
                showDeleteConfirmation()
        );
    }


    // =========================================================
    // LOAD PRODUCT FROM FIRESTORE
    // =========================================================

    private void loadProduct() {

        db.collection("products")
                .document(productId)
                .get()
                .addOnSuccessListener(
                        documentSnapshot -> {

                            if (!documentSnapshot.exists()) {

                                Toast.makeText(
                                        requireContext(),
                                        "Product not found.",
                                        Toast.LENGTH_SHORT
                                ).show();

                                goBackToProducts();

                                return;
                            }


                            // =================================================
                            // CHECK CURRENT SELLER
                            // =================================================

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
                                    documentSnapshot.getString(
                                            "sellerId"
                                    );


                            // =================================================
                            // SECURITY CHECK
                            // =================================================

                            if (sellerId == null ||
                                    !sellerId.equals(
                                            currentUser.getUid()
                                    )) {

                                Toast.makeText(
                                        requireContext(),
                                        "You cannot edit this product.",
                                        Toast.LENGTH_LONG
                                ).show();

                                goBackToProducts();

                                return;
                            }


                            // =================================================
                            // GET DATA
                            // =================================================

                            String name =
                                    documentSnapshot.getString(
                                            "name"
                                    );

                            String description =
                                    documentSnapshot.getString(
                                            "description"
                                    );


                            // =================================================
                            // PRICE
                            // =================================================

                            Object priceObject =
                                    documentSnapshot.get(
                                            "price"
                                    );


                            // =================================================
                            // QUANTITY
                            // =================================================

                            Object quantityObject =
                                    documentSnapshot.get(
                                            "quantity"
                                    );


                            // =================================================
                            // SET NAME
                            // =================================================

                            if (name != null) {

                                etProductName.setText(
                                        name
                                );
                            }


                            // =================================================
                            // SET PRICE
                            // =================================================

                            if (priceObject != null) {

                                etProductPrice.setText(
                                        String.valueOf(
                                                priceObject
                                        )
                                );
                            }


                            // =================================================
                            // SET QUANTITY
                            // =================================================

                            if (quantityObject != null) {

                                etProductQuantity.setText(
                                        String.valueOf(
                                                quantityObject
                                        )
                                );
                            }


                            // =================================================
                            // SET DESCRIPTION
                            // =================================================

                            if (description != null) {

                                etProductDescription.setText(
                                        description
                                );
                            }

                        }
                )
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            requireContext(),
                            "Failed to load product: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();

                });
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

        String quantityText =
                etProductQuantity
                        .getText()
                        .toString()
                        .trim();

        String description =
                etProductDescription
                        .getText()
                        .toString()
                        .trim();


        // =====================================================
        // VALIDATION
        // =====================================================

        if (TextUtils.isEmpty(name)) {

            etProductName.setError(
                    "Enter product name"
            );

            etProductName.requestFocus();

            return;
        }


        if (TextUtils.isEmpty(priceText)) {

            etProductPrice.setError(
                    "Enter product price"
            );

            etProductPrice.requestFocus();

            return;
        }


        if (TextUtils.isEmpty(quantityText)) {

            etProductQuantity.setError(
                    "Enter product quantity"
            );

            etProductQuantity.requestFocus();

            return;
        }


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


        // =====================================================
        // QUANTITY
        // =====================================================

        int quantity;

        try {

            quantity =
                    Integer.parseInt(
                            quantityText
                    );

        } catch (NumberFormatException e) {

            etProductQuantity.setError(
                    "Enter a valid quantity"
            );

            etProductQuantity.requestFocus();

            return;
        }


        // =====================================================
        // DISABLE BUTTON
        // =====================================================

        btnUpdateProduct.setEnabled(false);


        // =====================================================
        // UPDATED DATA
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
                "quantity",
                quantity
        );

        updateData.put(
                "description",
                description
        );


        // =====================================================
        // UPDATE FIRESTORE
        // =====================================================

        db.collection("products")
                .document(productId)
                .update(updateData)
                .addOnSuccessListener(unused -> {

                    btnUpdateProduct.setEnabled(
                            true
                    );

                    Toast.makeText(
                            requireContext(),
                            "Product updated successfully!",
                            Toast.LENGTH_SHORT
                    ).show();

                    goBackToProducts();

                })
                .addOnFailureListener(e -> {

                    btnUpdateProduct.setEnabled(
                            true
                    );

                    Toast.makeText(
                            requireContext(),
                            "Failed to update product: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }


    // =========================================================
    // DELETE CONFIRMATION
    // =========================================================

    private void showDeleteConfirmation() {

        new AlertDialog.Builder(
                requireContext()
        )
                .setTitle("Delete Product")
                .setMessage(
                        "Are you sure you want to delete this product?"
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .setPositiveButton(
                        "Delete",
                        (dialog, which) ->
                                deleteProduct()
                )
                .show();
    }


    // =========================================================
    // DELETE PRODUCT
    // =========================================================

    private void deleteProduct() {

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


        btnDeleteProduct.setEnabled(
                false
        );


        // =====================================================
        // DELETE FROM FIRESTORE
        // =====================================================

        db.collection("products")
                .document(productId)
                .delete()
                .addOnSuccessListener(unused -> {

                    btnDeleteProduct.setEnabled(
                            true
                    );

                    Toast.makeText(
                            requireContext(),
                            "Product deleted successfully!",
                            Toast.LENGTH_SHORT
                    ).show();

                    goBackToProducts();

                })
                .addOnFailureListener(e -> {

                    btnDeleteProduct.setEnabled(
                            true
                    );

                    Toast.makeText(
                            requireContext(),
                            "Failed to delete product: "
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