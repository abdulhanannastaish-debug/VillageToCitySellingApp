package com.example.villagetocityreseilingapp.ui.main.seller;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.villagetocityreseilingapp.R;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SellerEditProductFragment extends Fragment {

    private static final String ARG_PRODUCT_ID = "productId";
    private static final int PICK_IMAGE_REQUEST = 1001;

    private static final String CLOUD_NAME = "cvhzteif";
    private static final String UPLOAD_PRESET = "rural_reach_upload";

    private String productId;
    private String existingImageUrl = "";
    private String uploadedImageUrl = "";
    private Uri selectedImageUri;

    private EditText etProductName;
    private EditText etProductPrice;
    private EditText etProductStock;
    private EditText etProductUnit;
    private EditText etMinimumOrder;
    private EditText etProductDescription;

    private AppCompatButton btnUpdateProduct;

    private ImageButton btnBack;

    private FrameLayout productImageBox;
    private ImageView imgProduct;
    private TextView txtChangeImage;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    public SellerEditProductFragment() {
    }

    // =========================================================
    // NEW INSTANCE
    // =========================================================

    public static SellerEditProductFragment newInstance(String productId) {

        SellerEditProductFragment fragment =
                new SellerEditProductFragment();

        Bundle args = new Bundle();
        args.putString(ARG_PRODUCT_ID, productId);

        fragment.setArguments(args);

        return fragment;
    }

    // =========================================================
    // ON CREATE
    // =========================================================

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (getArguments() != null) {

            productId =
                    getArguments().getString(ARG_PRODUCT_ID);
        }
    }

    // =========================================================
    // ON CREATE VIEW
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
    // ON VIEW CREATED
    // =========================================================

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(
                view,
                savedInstanceState
        );

        auth =
                FirebaseAuth.getInstance();

        db =
                FirebaseFirestore.getInstance();

        // CLOUDINARY
        initializeCloudinary();

        // =====================================================
        // FIND VIEWS
        // =====================================================

        btnBack =
                view.findViewById(R.id.btn_back);

        etProductName =
                view.findViewById(R.id.etProductName);

        etProductPrice =
                view.findViewById(R.id.etProductPrice);

        etProductStock =
                view.findViewById(R.id.etProductStock);

        etProductUnit =
                view.findViewById(R.id.etProductUnit);

        etMinimumOrder =
                view.findViewById(R.id.etMinimumOrder);

        etProductDescription =
                view.findViewById(R.id.etProductDescription);

        btnUpdateProduct =
                view.findViewById(R.id.btnUpdateProduct);

        productImageBox =
                view.findViewById(R.id.productImageBox);

        imgProduct =
                view.findViewById(R.id.imgProduct);

        txtChangeImage =
                view.findViewById(R.id.txtChangeImage);

        // =====================================================
        // IMAGE CLICK
        // =====================================================

        if (productImageBox != null) {

            productImageBox.setOnClickListener(
                    v -> openImagePicker()
            );
        }

        if (imgProduct != null) {

            imgProduct.setOnClickListener(
                    v -> openImagePicker()
            );
        }

        // =====================================================
        // BACK BUTTON
        // =====================================================

        if (btnBack != null) {

            btnBack.setOnClickListener(
                    v -> goBackToProducts()
            );
        }

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
        // UPDATE BUTTON
        // =====================================================

        if (btnUpdateProduct != null) {

            btnUpdateProduct.setOnClickListener(
                    v -> updateProduct()
            );
        }
    }

    // =========================================================
    // CLOUDINARY INITIALIZE
    // =========================================================

    private void initializeCloudinary() {

        try {

            MediaManager.get();

        } catch (IllegalStateException e) {

            Map<String, Object> config =
                    new HashMap<>();

            config.put(
                    "cloud_name",
                    CLOUD_NAME
            );

            MediaManager.init(
                    requireContext(),
                    config
            );
        }
    }

    // =========================================================
    // OPEN IMAGE PICKER
    // =========================================================

    private void openImagePicker() {

        Intent intent =
                new Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                );

        intent.setType("image/*");

        startActivityForResult(
                intent,
                PICK_IMAGE_REQUEST
        );
    }

    // =========================================================
    // IMAGE PICK RESULT
    // =========================================================

    @Override
    public void onActivityResult(
            int requestCode,
            int resultCode,
            @Nullable Intent data) {

        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

        if (requestCode == PICK_IMAGE_REQUEST
                && resultCode == android.app.Activity.RESULT_OK
                && data != null
                && data.getData() != null) {

            selectedImageUri =
                    data.getData();

            // New selected image preview
            if (imgProduct != null) {

                imgProduct.setImageURI(
                        selectedImageUri
                );
            }

            // Hide Change Image text
            if (txtChangeImage != null) {

                txtChangeImage.setVisibility(
                        View.GONE
                );
            }

            Toast.makeText(
                    requireContext(),
                    "Image selected",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    // =========================================================
    // LOAD PRODUCT
    // =========================================================

    private void loadProduct() {

        db.collection("products")
                .document(productId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

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

                    // =================================================
                    // CHECK CURRENT USER
                    // =================================================

                    FirebaseUser currentUser =
                            auth.getCurrentUser();

                    if (currentUser == null) {

                        Toast.makeText(
                                requireContext(),
                                "Seller is not logged in.",
                                Toast.LENGTH_SHORT
                        ).show();

                        goBackToProducts();

                        return;
                    }

                    // =================================================
                    // CHECK SELLER
                    // =================================================

                    String sellerId =
                            documentSnapshot.getString(
                                    "sellerId"
                            );

                    if (sellerId == null
                            || !sellerId.equals(
                            currentUser.getUid())) {

                        Toast.makeText(
                                requireContext(),
                                "You cannot edit this product.",
                                Toast.LENGTH_LONG
                        ).show();

                        goBackToProducts();

                        return;
                    }

                    // =================================================
                    // LOAD EXISTING IMAGE
                    // =================================================

                    existingImageUrl =
                            documentSnapshot.getString(
                                    "imageUrl"
                            );

                    // If imageUrl is empty/null,
                    // try productImage
                    if (TextUtils.isEmpty(
                            existingImageUrl)) {

                        existingImageUrl =
                                documentSnapshot.getString(
                                        "productImage"
                                );
                    }

                    // Make sure it is not null
                    if (existingImageUrl == null) {

                        existingImageUrl = "";
                    }

                    // =================================================
                    // SHOW EXISTING IMAGE
                    // =================================================

                    if (!TextUtils.isEmpty(
                            existingImageUrl)) {

                        if (imgProduct != null) {

                            Glide.with(requireContext())
                                    .load(existingImageUrl)
                                    .placeholder(
                                            android.R.drawable.ic_menu_gallery
                                    )
                                    .error(
                                            android.R.drawable.ic_menu_gallery
                                    )
                                    .into(imgProduct);
                        }

                        // Hide Upload/Change text
                        if (txtChangeImage != null) {

                            txtChangeImage.setVisibility(
                                    View.GONE
                            );
                        }

                    } else {

                        // =================================================
                        // NO IMAGE AVAILABLE
                        // =================================================

                        if (imgProduct != null) {

                            imgProduct.setImageResource(
                                    android.R.drawable.ic_menu_gallery
                            );
                        }

                        if (txtChangeImage != null) {

                            txtChangeImage.setVisibility(
                                    View.VISIBLE
                            );
                        }
                    }

                    // =================================================
                    // LOAD PRODUCT NAME
                    // =================================================

                    String name =
                            documentSnapshot.getString(
                                    "name"
                            );

                    if (!TextUtils.isEmpty(name)) {

                        etProductName.setText(name);
                    }

                    // =================================================
                    // LOAD DESCRIPTION
                    // =================================================

                    String description =
                            documentSnapshot.getString(
                                    "description"
                            );

                    if (!TextUtils.isEmpty(description)) {

                        etProductDescription.setText(
                                description
                        );
                    }

                    // =================================================
                    // LOAD PRICE
                    // =================================================

                    Object price =
                            documentSnapshot.get("price");

                    if (price != null) {

                        etProductPrice.setText(
                                formatNumber(price)
                        );
                    }

                    // =================================================
                    // LOAD STOCK
                    // =================================================

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

                    // =================================================
                    // LOAD UNIT
                    // =================================================

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

                    // =================================================
                    // LOAD MINIMUM ORDER
                    // =================================================

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

                })
                .addOnFailureListener(e -> {

                    if (!isAdded()) {
                        return;
                    }

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

        if (TextUtils.isEmpty(stockText)) {

            etProductStock.setError(
                    "Enter available stock"
            );

            etProductStock.requestFocus();

            return;
        }

        if (TextUtils.isEmpty(unit)) {

            etProductUnit.setError(
                    "Enter unit"
            );

            etProductUnit.requestFocus();

            return;
        }

        if (TextUtils.isEmpty(minimumOrderText)) {

            etMinimumOrder.setError(
                    "Enter minimum order"
            );

            etMinimumOrder.requestFocus();

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
        // CHECK USER
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

            return;
        }

        if (price <= 0) {

            etProductPrice.setError(
                    "Price must be greater than 0"
            );

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

            return;
        }

        if (stock < 0) {

            etProductStock.setError(
                    "Stock cannot be negative"
            );

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

            return;
        }

        if (minimumOrder <= 0) {

            etMinimumOrder.setError(
                    "Minimum order must be greater than 0"
            );

            return;
        }

        if (minimumOrder > stock) {

            etMinimumOrder.setError(
                    "Minimum order cannot exceed stock"
            );

            return;
        }

        // =====================================================
        // DISABLE UPDATE BUTTON
        // =====================================================

        btnUpdateProduct.setEnabled(false);

        // =====================================================
        // NEW IMAGE SELECTED?
        // =====================================================

        if (selectedImageUri != null) {

            uploadImageAndUpdate(
                    name,
                    price,
                    stock,
                    unit,
                    minimumOrder,
                    description
            );

        } else {

            // Keep old image
            saveUpdatedProduct(
                    name,
                    price,
                    stock,
                    unit,
                    minimumOrder,
                    description,
                    existingImageUrl
            );
        }
    }

    // =========================================================
    // UPLOAD NEW IMAGE AND UPDATE
    // =========================================================

    private void uploadImageAndUpdate(
            String name,
            double price,
            double stock,
            String unit,
            double minimumOrder,
            String description) {

        Toast.makeText(
                requireContext(),
                "Uploading image...",
                Toast.LENGTH_SHORT
        ).show();

        MediaManager.get()
                .upload(selectedImageUri)
                .unsigned(UPLOAD_PRESET)
                .option(
                        "folder",
                        "rural_reach/products"
                )
                .callback(
                        new UploadCallback() {

                            @Override
                            public void onStart(
                                    String requestId) {
                            }

                            @Override
                            public void onProgress(
                                    String requestId,
                                    long bytes,
                                    long totalBytes) {
                            }

                            @Override
                            public void onSuccess(
                                    String requestId,
                                    Map resultData) {

                                if (!isAdded()) {
                                    return;
                                }

                                Object secureUrl =
                                        resultData.get(
                                                "secure_url"
                                        );

                                if (secureUrl != null) {

                                    uploadedImageUrl =
                                            secureUrl.toString();

                                    saveUpdatedProduct(
                                            name,
                                            price,
                                            stock,
                                            unit,
                                            minimumOrder,
                                            description,
                                            uploadedImageUrl
                                    );

                                } else {

                                    btnUpdateProduct
                                            .setEnabled(true);

                                    Toast.makeText(
                                            requireContext(),
                                            "Image upload failed.",
                                            Toast.LENGTH_LONG
                                    ).show();
                                }
                            }

                            @Override
                            public void onError(
                                    String requestId,
                                    ErrorInfo error) {

                                if (!isAdded()) {
                                    return;
                                }

                                btnUpdateProduct
                                        .setEnabled(true);

                                Toast.makeText(
                                        requireContext(),
                                        "Image upload failed: "
                                                + error.getDescription(),
                                        Toast.LENGTH_LONG
                                ).show();
                            }

                            @Override
                            public void onReschedule(
                                    String requestId,
                                    ErrorInfo error) {
                            }
                        }
                )
                .dispatch();
    }

    // =========================================================
    // SAVE UPDATED PRODUCT
    // =========================================================

    private void saveUpdatedProduct(
            String name,
            double price,
            double stock,
            String unit,
            double minimumOrder,
            String description,
            String imageUrl) {

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

        // =====================================================
        // SAVE IMAGE URL
        // =====================================================

        updateData.put(
                "imageUrl",
                imageUrl
        );

        updateData.put(
                "productImage",
                imageUrl
        );

        updateData.put(
                "updatedAt",
                Timestamp.now()
        );

        updateData.put(
                "status",
                stock <= 0
                        ? "outOfStock"
                        : "available"
        );

        // =====================================================
        // FIRESTORE UPDATE
        // =====================================================

        db.collection("products")
                .document(productId)
                .update(updateData)
                .addOnSuccessListener(unused -> {

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
                })
                .addOnFailureListener(e -> {

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
                });
    }

    // =========================================================
    // FORMAT NUMBER
    // =========================================================

    private String formatNumber(Object value) {

        if (value == null) {
            return "";
        }

        if (value instanceof Number) {

            double number =
                    ((Number) value).doubleValue();

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
    // GO BACK TO PRODUCTS
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