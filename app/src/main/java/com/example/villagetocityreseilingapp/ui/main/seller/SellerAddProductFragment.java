package com.example.villagetocityreseilingapp.ui.main.seller;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SellerAddProductFragment extends Fragment {

    // =========================================================
    // VIEWS
    // =========================================================

    private EditText etItemName;
    private EditText etItemPrice;
    private EditText etItemQuantity;
    private EditText etMinOrderQuantity;
    private EditText etItemDescription;

    private Spinner spinnerCategory;
    private Spinner spinnerUnit;

    private TextView txtStockPreview;

    private Button btnAddItem;
    private ImageButton btnBack;

    // =========================================================
    // FIREBASE
    // =========================================================

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    // =========================================================
    // CATEGORIES
    // =========================================================

    private final List<String> categoryNames =
            new ArrayList<>();

    private final List<String> categoryIds =
            new ArrayList<>();

    private ArrayAdapter<String> categoryAdapter;

    private boolean categoriesLoaded = false;

    // =========================================================
    // UNIT LIST
    // =========================================================

    private final String[] units = {
            "KG",
            "GRAM",
            "LITER",
            "ML",
            "PIECE",
            "DOZEN"
    };

    private ArrayAdapter<String> unitAdapter;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public SellerAddProductFragment() {
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
                R.layout.fragment_seller_add_product,
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

        etItemName =
                view.findViewById(
                        R.id.et_item_name
                );

        etItemPrice =
                view.findViewById(
                        R.id.et_item_price
                );

        etItemQuantity =
                view.findViewById(
                        R.id.et_item_quantity
                );

        etMinOrderQuantity =
                view.findViewById(
                        R.id.et_min_order_quantity
                );

        etItemDescription =
                view.findViewById(
                        R.id.et_item_description
                );

        spinnerCategory =
                view.findViewById(
                        R.id.spinner_category
                );

        spinnerUnit =
                view.findViewById(
                        R.id.spinner_unit
                );

        txtStockPreview =
                view.findViewById(
                        R.id.txtStockPreview
                );

        btnAddItem =
                view.findViewById(
                        R.id.btn_add_item
                );

        btnBack =
                view.findViewById(
                        R.id.btn_back
                );

        // =====================================================
        // SETUP
        // =====================================================

        setupCategorySpinner();

        setupUnitSpinner();

        setupProductNameListener();

        setupStockPreviewListeners();

        loadCategories();

        // =====================================================
        // BACK
        // =====================================================

        btnBack.setOnClickListener(
                v -> goBackToProducts()
        );

        // =====================================================
        // ADD PRODUCT
        // =====================================================

        btnAddItem.setOnClickListener(
                v -> saveProduct()
        );

        // Initial preview
        updateStockPreview();
    }

    // =========================================================
    // CATEGORY SPINNER
    // =========================================================

    private void setupCategorySpinner() {

        categoryNames.clear();

        categoryIds.clear();

        categoryNames.add(
                "Select Category"
        );

        categoryIds.add(
                ""
        );

        categoryAdapter =
                new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        categoryNames
                );

        categoryAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerCategory.setAdapter(
                categoryAdapter
        );
    }

    // =========================================================
    // UNIT SPINNER
    // =========================================================

    private void setupUnitSpinner() {

        unitAdapter =
                new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        units
                );

        unitAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerUnit.setAdapter(
                unitAdapter
        );
    }

    // =========================================================
    // STOCK PREVIEW LISTENERS
    // =========================================================

    private void setupStockPreviewListeners() {

        TextWatcher stockWatcher =
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count) {

                        updateStockPreview();
                    }

                    @Override
                    public void afterTextChanged(
                            Editable s) {
                    }
                };

        etItemPrice.addTextChangedListener(
                stockWatcher
        );

        etItemQuantity.addTextChangedListener(
                stockWatcher
        );

        etMinOrderQuantity.addTextChangedListener(
                stockWatcher
        );

        spinnerUnit.setOnItemSelectedListener(
                new android.widget.AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            android.widget.AdapterView<?> parent,
                            View view,
                            int position,
                            long id) {

                        updateStockPreview();
                    }

                    @Override
                    public void onNothingSelected(
                            android.widget.AdapterView<?> parent) {
                    }
                }
        );
    }

    // =========================================================
    // UPDATE STOCK PREVIEW
    // =========================================================

    private void updateStockPreview() {

        String priceText =
                etItemPrice.getText()
                        .toString()
                        .trim();

        String stockText =
                etItemQuantity.getText()
                        .toString()
                        .trim();

        String minOrderText =
                etMinOrderQuantity.getText()
                        .toString()
                        .trim();

        String unit =
                spinnerUnit != null
                        && spinnerUnit.getSelectedItem() != null
                        ? spinnerUnit
                        .getSelectedItem()
                        .toString()
                        : "KG";

        String priceDisplay =
                "0";

        String stockDisplay =
                "0";

        String minOrderDisplay =
                "0";

        if (!priceText.isEmpty()) {

            try {

                double price =
                        Double.parseDouble(
                                priceText
                        );

                priceDisplay =
                        formatNumber(price);

            } catch (NumberFormatException ignored) {
            }
        }

        if (!stockText.isEmpty()) {

            try {

                double stock =
                        Double.parseDouble(
                                stockText
                        );

                stockDisplay =
                        formatNumber(stock);

            } catch (NumberFormatException ignored) {
            }
        }

        if (!minOrderText.isEmpty()) {

            try {

                double minimum =
                        Double.parseDouble(
                                minOrderText
                        );

                minOrderDisplay =
                        formatNumber(minimum);

            } catch (NumberFormatException ignored) {
            }
        }

        txtStockPreview.setText(
                "Rs. "
                        + priceDisplay
                        + " / "
                        + unit
                        + " • Stock: "
                        + stockDisplay
                        + " "
                        + unit
                        + " • Min Order: "
                        + minOrderDisplay
                        + " "
                        + unit
        );
    }

    // =========================================================
    // FORMAT NUMBER
    // =========================================================

    private String formatNumber(
            double number) {

        if (
                number == Math.floor(number)
        ) {

            return String.valueOf(
                    (long) number
            );
        }

        return String.format(
                Locale.ROOT,
                "%.2f",
                number
        );
    }

    // =========================================================
    // LOAD ADMIN CATEGORIES
    // =========================================================

    private void loadCategories() {

        categoriesLoaded = false;

        db.collection("categories")
                .get()
                .addOnSuccessListener(
                        snapshot -> {

                            if (!isAdded()) {
                                return;
                            }

                            categoryNames.clear();

                            categoryIds.clear();

                            categoryNames.add(
                                    "Select Category"
                            );

                            categoryIds.add(
                                    ""
                            );

                            for (
                                    DocumentSnapshot document
                                    : snapshot.getDocuments()
                            ) {

                                String categoryId =
                                        document.getId();

                                String categoryName =
                                        document.getString(
                                                "name"
                                        );

                                if (
                                        TextUtils.isEmpty(
                                                categoryId
                                        )
                                                ||
                                                TextUtils.isEmpty(
                                                        categoryName
                                                )
                                ) {
                                    continue;
                                }

                                categoryId =
                                        categoryId.trim();

                                categoryName =
                                        categoryName.trim();

                                if (
                                        categoryId.isEmpty()
                                                ||
                                                categoryName.isEmpty()
                                ) {
                                    continue;
                                }

                                categoryNames.add(
                                        categoryName
                                );

                                categoryIds.add(
                                        categoryId
                                );

                                Log.d(
                                        "SELLER_CATEGORY",
                                        "Loaded Category: "
                                                + categoryName
                                                + " | REAL ID: "
                                                + categoryId
                                );
                            }

                            categoryAdapter
                                    .notifyDataSetChanged();

                            categoriesLoaded = true;

                            if (
                                    categoryNames.size()
                                            <= 1
                            ) {

                                Toast.makeText(
                                        requireContext(),
                                        "No categories found. Admin needs to add categories first.",
                                        Toast.LENGTH_LONG
                                ).show();

                            } else {

                                autoSelectCategory();
                            }
                        }
                )
                .addOnFailureListener(
                        e -> {

                            if (!isAdded()) {
                                return;
                            }

                            categoriesLoaded = false;

                            Toast.makeText(
                                    requireContext(),
                                    "Failed to load categories: "
                                            + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }

    // =========================================================
    // PRODUCT NAME LISTENER
    // =========================================================

    private void setupProductNameListener() {

        etItemName.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count) {

                        if (categoriesLoaded) {

                            autoSelectCategory();
                        }
                    }

                    @Override
                    public void afterTextChanged(
                            Editable s) {
                    }
                }
        );
    }

    // =========================================================
    // AUTO SELECT CATEGORY
    // =========================================================

    private void autoSelectCategory() {

        if (
                !categoriesLoaded
                        || categoryNames.size() <= 1
        ) {
            return;
        }

        String productName =
                etItemName.getText()
                        .toString()
                        .trim()
                        .toLowerCase(
                                Locale.ROOT
                        );

        if (productName.isEmpty()) {
            return;
        }

        // =====================================================
        // EXACT / CONTAINS MATCH
        // =====================================================

        for (
                int i = 1;
                i < categoryNames.size();
                i++
        ) {

            String categoryName =
                    categoryNames.get(i)
                            .toLowerCase(
                                    Locale.ROOT
                            );

            if (
                    productName.equals(
                            categoryName
                    )
                            ||
                            productName.contains(
                                    categoryName
                            )
                            ||
                            categoryName.contains(
                                    productName
                            )
            ) {

                spinnerCategory.setSelection(
                        i
                );

                return;
            }
        }

        // =====================================================
        // KEYWORD MATCH
        // =====================================================

        String[] productWords =
                productName.split(
                        "\\s+"
                );

        int bestPosition = -1;

        int bestScore = 0;

        for (
                int i = 1;
                i < categoryNames.size();
                i++
        ) {

            String categoryName =
                    categoryNames.get(i)
                            .toLowerCase(
                                    Locale.ROOT
                            );

            String[] categoryWords =
                    categoryName.split(
                            "\\s+"
                    );

            int score = 0;

            for (
                    String productWord
                    : productWords
            ) {

                if (
                        productWord.length()
                                < 3
                ) {
                    continue;
                }

                for (
                        String categoryWord
                        : categoryWords
                ) {

                    if (
                            categoryWord.length()
                                    < 3
                    ) {
                        continue;
                    }

                    if (
                            productWord.contains(
                                    categoryWord
                            )
                                    ||
                                    categoryWord.contains(
                                            productWord
                                    )
                    ) {

                        score++;
                    }
                }
            }

            if (score > bestScore) {

                bestScore = score;

                bestPosition = i;
            }
        }

        if (bestPosition != -1) {

            spinnerCategory.setSelection(
                    bestPosition
            );
        }
    }

    // =========================================================
    // SAVE PRODUCT
    // =========================================================

    private void saveProduct() {

        // =====================================================
        // PRODUCT INPUTS
        // =====================================================

        String productName =
                etItemName.getText()
                        .toString()
                        .trim();

        String priceText =
                etItemPrice.getText()
                        .toString()
                        .trim();

        String quantityText =
                etItemQuantity.getText()
                        .toString()
                        .trim();

        String minOrderText =
                etMinOrderQuantity.getText()
                        .toString()
                        .trim();

        String description =
                etItemDescription.getText()
                        .toString()
                        .trim();

        // =====================================================
        // VALIDATION
        // =====================================================

        if (
                TextUtils.isEmpty(
                        productName
                )
        ) {

            etItemName.setError(
                    "Enter product name"
            );

            etItemName.requestFocus();

            return;
        }

        if (
                TextUtils.isEmpty(
                        priceText
                )
        ) {

            etItemPrice.setError(
                    "Enter product price"
            );

            etItemPrice.requestFocus();

            return;
        }

        if (
                TextUtils.isEmpty(
                        quantityText
                )
        ) {

            etItemQuantity.setError(
                    "Enter total stock"
            );

            etItemQuantity.requestFocus();

            return;
        }

        if (
                TextUtils.isEmpty(
                        minOrderText
                )
        ) {

            etMinOrderQuantity.setError(
                    "Enter minimum order quantity"
            );

            etMinOrderQuantity.requestFocus();

            return;
        }

        if (
                TextUtils.isEmpty(
                        description
                )
        ) {

            etItemDescription.setError(
                    "Enter product description"
            );

            etItemDescription.requestFocus();

            return;
        }

        // =====================================================
        // CATEGORY
        // =====================================================

        int selectedPosition =
                spinnerCategory
                        .getSelectedItemPosition();

        if (
                selectedPosition <= 0
                        ||
                        selectedPosition
                                >= categoryIds.size()
        ) {

            Toast.makeText(
                    requireContext(),
                    "Please select a category",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        final String selectedCategoryId =
                categoryIds
                        .get(selectedPosition)
                        .trim();

        final String selectedCategoryName =
                categoryNames
                        .get(selectedPosition)
                        .trim();

        if (
                selectedCategoryId.isEmpty()
        ) {

            Toast.makeText(
                    requireContext(),
                    "Invalid category ID",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // =====================================================
        // UNIT
        // =====================================================

        final String selectedUnit =
                spinnerUnit
                        .getSelectedItem()
                        .toString()
                        .trim();

        if (selectedUnit.isEmpty()) {

            Toast.makeText(
                    requireContext(),
                    "Please select a unit",
                    Toast.LENGTH_SHORT
            ).show();

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

        final String sellerId =
                currentUser.getUid();

        // =====================================================
        // PRICE
        // =====================================================

        final double price;

        try {

            price =
                    Double.parseDouble(
                            priceText
                    );

        } catch (
                NumberFormatException e
        ) {

            etItemPrice.setError(
                    "Enter a valid price"
            );

            etItemPrice.requestFocus();

            return;
        }

        if (price <= 0) {

            etItemPrice.setError(
                    "Price must be greater than 0"
            );

            etItemPrice.requestFocus();

            return;
        }

        // =====================================================
        // TOTAL STOCK
        // =====================================================

        final double totalStock;

        try {

            totalStock =
                    Double.parseDouble(
                            quantityText
                    );

        } catch (
                NumberFormatException e
        ) {

            etItemQuantity.setError(
                    "Enter a valid stock quantity"
            );

            etItemQuantity.requestFocus();

            return;
        }

        if (totalStock <= 0) {

            etItemQuantity.setError(
                    "Stock must be greater than 0"
            );

            etItemQuantity.requestFocus();

            return;
        }

        // =====================================================
        // MINIMUM ORDER
        // =====================================================

        final double minimumOrder;

        try {

            minimumOrder =
                    Double.parseDouble(
                            minOrderText
                    );

        } catch (
                NumberFormatException e
        ) {

            etMinOrderQuantity.setError(
                    "Enter a valid minimum order"
            );

            etMinOrderQuantity.requestFocus();

            return;
        }

        if (minimumOrder <= 0) {

            etMinOrderQuantity.setError(
                    "Minimum order must be greater than 0"
            );

            etMinOrderQuantity.requestFocus();

            return;
        }

        if (minimumOrder > totalStock) {

            etMinOrderQuantity.setError(
                    "Minimum order cannot exceed total stock"
            );

            etMinOrderQuantity.requestFocus();

            return;
        }

        // =====================================================
        // PRODUCT ID
        // =====================================================

        final String productId =
                db.collection("products")
                        .document()
                        .getId();

        // =====================================================
        // DISABLE BUTTON
        // =====================================================

        btnAddItem.setEnabled(false);

        // =====================================================
        // LOAD SELLER
        // =====================================================

        loadSellerAndSaveProduct(
                sellerId,
                productId,
                productName,
                price,
                totalStock,
                minimumOrder,
                selectedUnit,
                description,
                selectedCategoryId,
                selectedCategoryName
        );
    }

    // =========================================================
    // LOAD SELLER AND SAVE PRODUCT
    // =========================================================

    private void loadSellerAndSaveProduct(
            String sellerId,
            String productId,
            String productName,
            double price,
            double totalStock,
            double minimumOrder,
            String selectedUnit,
            String description,
            String selectedCategoryId,
            String selectedCategoryName) {

        db.collection("sellers")
                .document(sellerId)
                .get()
                .addOnSuccessListener(
                        sellerDocument -> {

                            if (!isAdded()) {
                                return;
                            }

                            if (
                                    sellerDocument.exists()
                            ) {

                                String sellerName =
                                        getSellerName(
                                                sellerDocument
                                        );

                                String sellerPhone =
                                        getSellerPhone(
                                                sellerDocument
                                        );

                                saveProductToFirestore(
                                        productId,
                                        sellerId,
                                        sellerName,
                                        sellerPhone,
                                        productName,
                                        price,
                                        totalStock,
                                        minimumOrder,
                                        selectedUnit,
                                        description,
                                        selectedCategoryId,
                                        selectedCategoryName
                                );

                            } else {

                                loadSellerFromUsers(
                                        sellerId,
                                        productId,
                                        productName,
                                        price,
                                        totalStock,
                                        minimumOrder,
                                        selectedUnit,
                                        description,
                                        selectedCategoryId,
                                        selectedCategoryName
                                );
                            }
                        }
                )
                .addOnFailureListener(
                        e -> {

                            if (!isAdded()) {
                                return;
                            }

                            loadSellerFromUsers(
                                    sellerId,
                                    productId,
                                    productName,
                                    price,
                                    totalStock,
                                    minimumOrder,
                                    selectedUnit,
                                    description,
                                    selectedCategoryId,
                                    selectedCategoryName
                            );
                        }
                );
    }

    // =========================================================
    // LOAD SELLER FROM USERS
    // =========================================================

    private void loadSellerFromUsers(
            String sellerId,
            String productId,
            String productName,
            double price,
            double totalStock,
            double minimumOrder,
            String selectedUnit,
            String description,
            String selectedCategoryId,
            String selectedCategoryName) {

        db.collection("users")
                .document(sellerId)
                .get()
                .addOnSuccessListener(
                        userDocument -> {

                            if (!isAdded()) {
                                return;
                            }

                            String sellerName = "";

                            String sellerPhone = "";

                            if (
                                    userDocument.exists()
                            ) {

                                sellerName =
                                        getSellerName(
                                                userDocument
                                        );

                                sellerPhone =
                                        getSellerPhone(
                                                userDocument
                                        );
                            }

                            saveProductToFirestore(
                                    productId,
                                    sellerId,
                                    sellerName,
                                    sellerPhone,
                                    productName,
                                    price,
                                    totalStock,
                                    minimumOrder,
                                    selectedUnit,
                                    description,
                                    selectedCategoryId,
                                    selectedCategoryName
                            );
                        }
                )
                .addOnFailureListener(
                        e -> {

                            if (!isAdded()) {
                                return;
                            }

                            saveProductToFirestore(
                                    productId,
                                    sellerId,
                                    "",
                                    "",
                                    productName,
                                    price,
                                    totalStock,
                                    minimumOrder,
                                    selectedUnit,
                                    description,
                                    selectedCategoryId,
                                    selectedCategoryName
                            );
                        }
                );
    }

    // =========================================================
    // GET SELLER NAME
    // =========================================================

    private String getSellerName(
            DocumentSnapshot document) {

        String name =
                getDocumentString(
                        document,
                        "name"
                );

        if (!name.isEmpty()) {
            return name;
        }

        name =
                getDocumentString(
                        document,
                        "sellerName"
                );

        if (!name.isEmpty()) {
            return name;
        }

        return getDocumentString(
                document,
                "fullName"
        );
    }

    // =========================================================
    // GET SELLER PHONE
    // =========================================================

    private String getSellerPhone(
            DocumentSnapshot document) {

        String phone =
                getDocumentString(
                        document,
                        "phone"
                );

        if (!phone.isEmpty()) {
            return phone;
        }

        phone =
                getDocumentString(
                        document,
                        "phoneNumber"
                );

        if (!phone.isEmpty()) {
            return phone;
        }

        return getDocumentString(
                document,
                "sellerPhone"
        );
    }

    // =========================================================
    // GET DOCUMENT STRING
    // =========================================================

    private String getDocumentString(
            DocumentSnapshot document,
            String key) {

        Object value =
                document.get(key);

        if (value == null) {
            return "";
        }

        String result =
                String.valueOf(
                        value
                ).trim();

        if (
                TextUtils.isEmpty(
                        result
                )
        ) {
            return "";
        }

        return result;
    }

    // =========================================================
    // SAVE PRODUCT TO FIRESTORE
    // =========================================================

    private void saveProductToFirestore(
            String productId,
            String sellerId,
            String sellerName,
            String sellerPhone,
            String productName,
            double price,
            double totalStock,
            double minimumOrder,
            String selectedUnit,
            String description,
            String selectedCategoryId,
            String selectedCategoryName) {

        if (!isAdded()) {
            return;
        }

        // =====================================================
        // PRODUCT DATA
        // =====================================================

        Map<String, Object> productData =
                new HashMap<>();

        // =====================================================
        // PRODUCT ID
        // =====================================================

        productData.put(
                "productId",
                productId
        );

        // =====================================================
        // SELLER
        // =====================================================

        productData.put(
                "sellerId",
                sellerId
        );

        productData.put(
                "sellerName",
                sellerName
        );

        productData.put(
                "sellerPhone",
                sellerPhone
        );

        // =====================================================
        // PRODUCT BASIC DATA
        // =====================================================

        productData.put(
                "name",
                productName
        );

        productData.put(
                "description",
                description
        );

        // =====================================================
        // PRICE
        // =====================================================

        productData.put(
                "price",
                price
        );

        productData.put(
                "pricePerUnit",
                price
        );

        // =====================================================
        // UNIT
        // =====================================================

        productData.put(
                "unitType",
                selectedUnit
        );

        // =====================================================
        // STOCK
        // =====================================================

        productData.put(
                "totalStock",
                totalStock
        );

        productData.put(
                "availableStock",
                totalStock
        );

        // =====================================================
        // OLD QUANTITY FIELD
        // =====================================================
        //
        // Existing buyer-side code agar quantity read
        // karta hai to break nahi hoga.
        //
        // =====================================================

        productData.put(
                "quantity",
                totalStock
        );

        // =====================================================
        // MINIMUM ORDER
        // =====================================================

        productData.put(
                "minimumOrder",
                minimumOrder
        );

        productData.put(
                "minOrderQuantity",
                minimumOrder
        );

        // =====================================================
        // CATEGORY
        // =====================================================

        productData.put(
                "categoryId",
                selectedCategoryId
        );

        productData.put(
                "categoryName",
                selectedCategoryName
        );

        // =====================================================
        // STATUS
        // =====================================================

        productData.put(
                "status",
                "available"
        );

        // =====================================================
        // RATING
        // =====================================================

        productData.put(
                "rating",
                0.0
        );

        productData.put(
                "reviewCount",
                0
        );

        // =====================================================
        // CREATED AT
        // =====================================================

        productData.put(
                "createdAt",
                Timestamp.now()
        );

        // =====================================================
        // UPDATED AT
        // =====================================================

        productData.put(
                "updatedAt",
                Timestamp.now()
        );

        // =====================================================
        // SAVE
        // =====================================================

        db.collection("products")
                .document(productId)
                .set(productData)
                .addOnSuccessListener(
                        unused -> {

                            if (!isAdded()) {
                                return;
                            }

                            btnAddItem.setEnabled(
                                    true
                            );

                            Log.d(
                                    "SELLER_PRODUCT",
                                    "Product Saved"
                                            + " | Product ID = "
                                            + productId
                                            + " | Seller ID = "
                                            + sellerId
                                            + " | Seller Name = "
                                            + sellerName
                                            + " | Seller Phone = "
                                            + sellerPhone
                                            + " | Category ID = "
                                            + selectedCategoryId
                                            + " | Category Name = "
                                            + selectedCategoryName
                                            + " | Price = "
                                            + price
                                            + " | Unit = "
                                            + selectedUnit
                                            + " | Total Stock = "
                                            + totalStock
                                            + " | Available Stock = "
                                            + totalStock
                                            + " | Minimum Order = "
                                            + minimumOrder
                            );

                            Toast.makeText(
                                    requireContext(),
                                    "Product added successfully!",
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

                            btnAddItem.setEnabled(
                                    true
                            );

                            Toast.makeText(
                                    requireContext(),
                                    "Failed to add product: "
                                            + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }

    // =========================================================
    // BACK
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