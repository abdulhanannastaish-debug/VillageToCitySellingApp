package com.example.villagetocityreseilingapp.ui.main.buyer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.villagetocityreseilingapp.R;
import com.example.villagetocityreseilingapp.adapter.BuyerCategoryAdapter;
import com.example.villagetocityreseilingapp.adapter.BuyerProductAdapter;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BuyerHomeFragment extends Fragment
        implements BuyerCategoryAdapter.OnCategoryClickListener {

    // =========================================================
    // FIREBASE
    // =========================================================

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    // =========================================================
    // NOTIFICATION
    // =========================================================

    private View notificationCard;
    private TextView notificationBadge;

    // =========================================================
    // SEARCH
    // =========================================================

    private EditText etSearch;

    private String currentSearchText = "";

    // =========================================================
    // RECYCLERS
    // =========================================================

    private RecyclerView recyclerCategories;
    private RecyclerView recyclerFeaturedProducts;

    // =========================================================
    // DATA
    // =========================================================

    private final List<Map<String, Object>> categoryList =
            new ArrayList<>();

    private final List<Map<String, Object>> productList =
            new ArrayList<>();

    // =========================================================
    // ADAPTERS
    // =========================================================

    private BuyerCategoryAdapter categoryAdapter;

    private BuyerProductAdapter productAdapter;

    // =========================================================
    // CATEGORY
    // =========================================================

    private String selectedCategoryId = "";

    private String selectedCategoryName = "";

    // =========================================================
    // PRODUCT CLICK CONTROL
    // =========================================================

    private boolean productClickLocked = false;

    // =========================================================
    // CREATE VIEW
    // =========================================================

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_buyer_home,
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

        auth = FirebaseAuth.getInstance();

        // =====================================================
        // SEARCH
        // =====================================================

        setupSearchBar(view);

        // =====================================================
        // NOTIFICATION
        // =====================================================

        setupNotification(view);

        // =====================================================
        // CATEGORIES
        // =====================================================

        recyclerCategories =
                view.findViewById(
                        R.id.recyclerCategories
                );

        if (recyclerCategories != null) {

            recyclerCategories.setLayoutManager(
                    new GridLayoutManager(
                            requireContext(),
                            2
                    )
            );

            recyclerCategories.setNestedScrollingEnabled(
                    false
            );

            categoryAdapter =
                    new BuyerCategoryAdapter(
                            categoryList,
                            this
                    );

            recyclerCategories.setAdapter(
                    categoryAdapter
            );
        }

        // =====================================================
        // FEATURED PRODUCTS
        // =====================================================

        recyclerFeaturedProducts =
                view.findViewById(
                        R.id.recyclerFeaturedProducts
                );

        if (recyclerFeaturedProducts != null) {

            recyclerFeaturedProducts.setLayoutManager(
                    new GridLayoutManager(
                            requireContext(),
                            2
                    )
            );

            recyclerFeaturedProducts.setNestedScrollingEnabled(
                    false
            );

            productAdapter =
                    new BuyerProductAdapter(
                            productList
                    );

            // =================================================
            // PRODUCT CLICK
            // =================================================

            productAdapter.setOnProductClickListener(
                    product -> {

                        if (productClickLocked) {
                            return;
                        }

                        productClickLocked = true;

                        blinkProductAndOpen(
                                product
                        );
                    }
            );

            recyclerFeaturedProducts.setAdapter(
                    productAdapter
            );
        }

        // =====================================================
        // LOAD DATA
        // =====================================================

        loadCategories();

        loadAllProducts();

        // =====================================================
        // LOAD NOTIFICATION COUNT
        // =====================================================

        loadUnreadNotificationCount();
    }

    // =========================================================
    // PRODUCT LIGHT GREEN BLINK
    // =========================================================

    private void blinkProductAndOpen(
            Map<String, Object> product) {

        if (!isAdded()) {

            productClickLocked = false;

            return;
        }

        if (product == null) {

            productClickLocked = false;

            Toast.makeText(
                    requireContext(),
                    "Product not found",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        Object productIdObject =
                product.get("productId");

        if (productIdObject == null) {

            productClickLocked = false;

            Toast.makeText(
                    requireContext(),
                    "Product ID not found",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String productId =
                String.valueOf(
                        productIdObject
                ).trim();

        if (productId.isEmpty()) {

            productClickLocked = false;

            Toast.makeText(
                    requireContext(),
                    "Product ID not found",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        int productPosition =
                productList.indexOf(product);

        if (productPosition < 0 ||
                recyclerFeaturedProducts == null) {

            openProductDetail(product);

            return;
        }

        View clickedView = null;

        for (
                int i = 0;
                i < recyclerFeaturedProducts.getChildCount();
                i++
        ) {

            View child =
                    recyclerFeaturedProducts.getChildAt(i);

            RecyclerView.ViewHolder holder =
                    recyclerFeaturedProducts
                            .getChildViewHolder(child);

            int adapterPosition =
                    holder.getBindingAdapterPosition();

            if (adapterPosition ==
                    productPosition) {

                clickedView = child;

                break;
            }
        }

        if (clickedView == null) {

            openProductDetail(product);

            return;
        }

        final View finalClickedView =
                clickedView;

        finalClickedView.post(
                () -> {

                    if (!isAdded()) {

                        productClickLocked = false;

                        return;
                    }

                    final GradientDrawable greenOverlay =
                            new GradientDrawable();

                    greenOverlay.setColor(
                            Color.rgb(
                                    220,
                                    245,
                                    220
                            )
                    );

                    greenOverlay.setCornerRadius(
                            dpToPx(14)
                    );

                    greenOverlay.setBounds(
                            0,
                            0,
                            finalClickedView.getWidth(),
                            finalClickedView.getHeight()
                    );

                    finalClickedView
                            .getOverlay()
                            .add(greenOverlay);

                    ValueAnimator blinkAnimator =
                            ValueAnimator.ofInt(
                                    0,
                                    255,
                                    0
                            );

                    blinkAnimator.setDuration(
                            260
                    );

                    blinkAnimator.addUpdateListener(
                            animation -> {

                                int alpha =
                                        (Integer)
                                                animation
                                                        .getAnimatedValue();

                                greenOverlay.setAlpha(
                                        alpha
                                );
                            }
                    );

                    blinkAnimator.addListener(
                            new AnimatorListenerAdapter() {

                                @Override
                                public void onAnimationEnd(
                                        Animator animation) {

                                    finalClickedView
                                            .getOverlay()
                                            .remove(
                                                    greenOverlay
                                            );

                                    if (isAdded()) {

                                        openProductDetail(
                                                product
                                        );

                                    } else {

                                        productClickLocked =
                                                false;
                                    }
                                }

                                @Override
                                public void onAnimationCancel(
                                        Animator animation) {

                                    finalClickedView
                                            .getOverlay()
                                            .remove(
                                                    greenOverlay
                                            );

                                    productClickLocked =
                                            false;
                                }
                            }
                    );

                    blinkAnimator.start();
                }
        );
    }

    // =========================================================
    // DP TO PX
    // =========================================================

    private float dpToPx(
            float dp) {

        if (getContext() == null) {
            return dp;
        }

        return dp *
                getResources()
                        .getDisplayMetrics()
                        .density;
    }

    // =========================================================
    // NOTIFICATION SETUP
    // =========================================================

    private void setupNotification(
            View view) {

        notificationCard =
                view.findViewById(
                        R.id.notificationCard
                );

        notificationBadge =
                view.findViewById(
                        R.id.notificationBadge
                );

        if (notificationCard == null) {
            return;
        }

        notificationCard.setOnClickListener(
                v -> openNotificationFragment()
        );
    }

    // =========================================================
    // OPEN NOTIFICATION
    // =========================================================

    private void openNotificationFragment() {

        if (!isAdded()) {
            return;
        }

        BuyerNotificationFragment notificationFragment =
                new BuyerNotificationFragment();

        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .replace(
                        R.id.fragment_container,
                        notificationFragment
                )
                .addToBackStack(null)
                .commit();
    }

    // =========================================================
    // LOAD UNREAD NOTIFICATION COUNT
    // =========================================================

    private void loadUnreadNotificationCount() {

        if (!isAdded()) {
            return;
        }

        if (auth == null ||
                auth.getCurrentUser() == null) {

            hideNotificationBadge();

            return;
        }

        String buyerId =
                auth.getCurrentUser().getUid();

        if (buyerId == null ||
                buyerId.trim().isEmpty()) {

            hideNotificationBadge();

            return;
        }

        db.collection("notifications")
                .whereEqualTo(
                        "buyerId",
                        buyerId
                )
                .whereEqualTo(
                        "read",
                        false
                )
                .get()
                .addOnSuccessListener(
                        queryDocumentSnapshots -> {

                            if (!isAdded()) {
                                return;
                            }

                            showNotificationBadge(
                                    queryDocumentSnapshots.size()
                            );
                        }
                )
                .addOnFailureListener(
                        e -> {

                            if (!isAdded()) {
                                return;
                            }

                            hideNotificationBadge();
                        }
                );
    }

    // =========================================================
    // SHOW BADGE
    // =========================================================

    private void showNotificationBadge(
            int count) {

        if (notificationBadge == null) {
            return;
        }

        if (count <= 0) {

            hideNotificationBadge();

            return;
        }

        notificationBadge.setVisibility(
                View.VISIBLE
        );

        notificationBadge.setText(
                count > 99
                        ? "99+"
                        : String.valueOf(count)
        );
    }

    // =========================================================
    // HIDE BADGE
    // =========================================================

    private void hideNotificationBadge() {

        if (notificationBadge != null) {

            notificationBadge.setVisibility(
                    View.GONE
            );
        }
    }

    // =========================================================
    // OPEN PRODUCT DETAIL
    // =========================================================

    private void openProductDetail(
            Map<String, Object> product) {

        if (!isAdded()) {

            productClickLocked = false;

            return;
        }

        if (product == null) {

            productClickLocked = false;

            Toast.makeText(
                    requireContext(),
                    "Product not found",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        Object productIdObject =
                product.get("productId");

        if (productIdObject == null) {

            productClickLocked = false;

            Toast.makeText(
                    requireContext(),
                    "Product ID not found",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String productId =
                String.valueOf(
                        productIdObject
                ).trim();

        if (productId.isEmpty()) {

            productClickLocked = false;

            Toast.makeText(
                    requireContext(),
                    "Product ID not found",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        BuyerProductDetailFragment detailFragment =
                new BuyerProductDetailFragment();

        detailFragment.setProduct(
                product
        );

        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .replace(
                        R.id.fragment_container,
                        detailFragment
                )
                .addToBackStack(null)
                .commit();
    }

    // =========================================================
    // SEARCH BAR
    // =========================================================

    private void setupSearchBar(
            View view) {

        etSearch =
                view.findViewById(
                        R.id.etSearch
                );

        if (etSearch == null) {
            return;
        }

        etSearch.addTextChangedListener(
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

                        currentSearchText =
                                s.toString()
                                        .trim()
                                        .toLowerCase(
                                                Locale.ROOT
                                        );

                        performSearch();
                    }

                    @Override
                    public void afterTextChanged(
                            Editable s) {
                    }
                }
        );
    }

    // =========================================================
    // LOAD CATEGORIES
    // =========================================================

    private void loadCategories() {

        db.collection("categories")
                .get()
                .addOnSuccessListener(
                        queryDocumentSnapshots -> {

                            if (!isAdded()) {
                                return;
                            }

                            categoryList.clear();

                            for (
                                    QueryDocumentSnapshot document
                                    : queryDocumentSnapshots
                            ) {

                                Map<String, Object> category =
                                        document.getData();

                                String categoryId =
                                        document.getId();

                                Object name =
                                        category.get("name");

                                if (name == null) {
                                    continue;
                                }

                                String categoryName =
                                        String.valueOf(name)
                                                .trim();

                                if (categoryName.isEmpty()) {
                                    continue;
                                }

                                category.put(
                                        "categoryId",
                                        categoryId
                                );

                                category.put(
                                        "firestoreDocumentId",
                                        categoryId
                                );

                                categoryList.add(
                                        category
                                );
                            }

                            if (categoryAdapter != null) {

                                categoryAdapter
                                        .notifyDataSetChanged();
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
                                    "Failed to load categories: "
                                            + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }

    // =========================================================
    // LOAD ALL PRODUCTS
    // =========================================================
    //
    // IMPORTANT:
    //
    // Yahan stock ko filter NAHI kiya ja raha.
    //
    // Stock 0 ho to product Buyer Home par rahega.
    // Buyer detail screen par OUT OF STOCK show karega.
    //
    // =========================================================

    private void loadAllProducts() {

        db.collection("products")
                .get()
                .addOnSuccessListener(
                        queryDocumentSnapshots -> {

                            if (!isAdded()) {
                                return;
                            }

                            productList.clear();

                            for (
                                    QueryDocumentSnapshot document
                                    : queryDocumentSnapshots
                            ) {

                                Map<String, Object> product =
                                        document.getData();

                                if (!isProductVisible(
                                        product
                                )) {
                                    continue;
                                }

                                product.put(
                                        "productId",
                                        document.getId()
                                );

                                normalizeStockFields(
                                        product
                                );

                                productList.add(
                                        product
                                );
                            }

                            sortProductsNewestFirst();

                            notifyProductAdapter();
                        }
                )
                .addOnFailureListener(
                        e -> {

                            if (!isAdded()) {
                                return;
                            }

                            Toast.makeText(
                                    requireContext(),
                                    "Failed to load products: "
                                            + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }

    // =========================================================
    // CATEGORY CLICK
    // =========================================================

    @Override
    public void onCategoryClick(
            String categoryId,
            String categoryName) {

        if (!isAdded()) {
            return;
        }

        selectedCategoryId =
                categoryId == null
                        ? ""
                        : categoryId.trim();

        selectedCategoryName =
                categoryName == null
                        ? ""
                        : categoryName.trim();

        if (selectedCategoryId.isEmpty()) {
            return;
        }

        filterProductsByCategory(
                selectedCategoryId,
                selectedCategoryName
        );
    }

    // =========================================================
    // FILTER CATEGORY
    // =========================================================

    private void filterProductsByCategory(
            String categoryId,
            String categoryName) {

        if (categoryId == null ||
                categoryId.trim().isEmpty()) {
            return;
        }

        db.collection("products")
                .whereEqualTo(
                        "categoryId",
                        categoryId
                )
                .get()
                .addOnSuccessListener(
                        queryDocumentSnapshots -> {

                            if (!isAdded()) {
                                return;
                            }

                            productList.clear();

                            for (
                                    QueryDocumentSnapshot document
                                    : queryDocumentSnapshots
                            ) {

                                Map<String, Object> product =
                                        document.getData();

                                if (!isProductVisible(
                                        product
                                )) {
                                    continue;
                                }

                                product.put(
                                        "productId",
                                        document.getId()
                                );

                                normalizeStockFields(
                                        product
                                );

                                productList.add(
                                        product
                                );
                            }

                            sortProductsNewestFirst();

                            notifyProductAdapter();

                            if (productList.isEmpty()) {

                                Toast.makeText(
                                        requireContext(),
                                        "No products found in "
                                                + categoryName,
                                        Toast.LENGTH_SHORT
                                ).show();
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
                                    "Failed to load products: "
                                            + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }

    // =========================================================
    // SEARCH
    // =========================================================

    private void performSearch() {

        if (currentSearchText.isEmpty()) {

            if (!selectedCategoryId.isEmpty()) {

                filterProductsByCategory(
                        selectedCategoryId,
                        selectedCategoryName
                );

            } else {

                loadAllProducts();
            }

            return;
        }

        if (!selectedCategoryId.isEmpty()) {

            searchInsideCategory();

        } else {

            searchAllProducts();
        }
    }

    // =========================================================
    // SEARCH INSIDE CATEGORY
    // =========================================================

    private void searchInsideCategory() {

        db.collection("products")
                .whereEqualTo(
                        "categoryId",
                        selectedCategoryId
                )
                .get()
                .addOnSuccessListener(
                        queryDocumentSnapshots -> {

                            if (!isAdded()) {
                                return;
                            }

                            productList.clear();

                            for (
                                    QueryDocumentSnapshot document
                                    : queryDocumentSnapshots
                            ) {

                                Map<String, Object> product =
                                        document.getData();

                                if (!isProductVisible(
                                        product
                                )) {
                                    continue;
                                }

                                if (doesProductMatchSearch(
                                        product,
                                        currentSearchText
                                )) {

                                    product.put(
                                            "productId",
                                            document.getId()
                                    );

                                    normalizeStockFields(
                                            product
                                    );

                                    productList.add(
                                            product
                                    );
                                }
                            }

                            sortProductsNewestFirst();

                            notifyProductAdapter();
                        }
                )
                .addOnFailureListener(
                        this::showSearchError
                );
    }

    // =========================================================
    // SEARCH ALL PRODUCTS
    // =========================================================

    private void searchAllProducts() {

        db.collection("products")
                .get()
                .addOnSuccessListener(
                        queryDocumentSnapshots -> {

                            if (!isAdded()) {
                                return;
                            }

                            productList.clear();

                            for (
                                    QueryDocumentSnapshot document
                                    : queryDocumentSnapshots
                            ) {

                                Map<String, Object> product =
                                        document.getData();

                                if (!isProductVisible(
                                        product
                                )) {
                                    continue;
                                }

                                if (doesProductMatchSearch(
                                        product,
                                        currentSearchText
                                )) {

                                    product.put(
                                            "productId",
                                            document.getId()
                                    );

                                    normalizeStockFields(
                                            product
                                    );

                                    productList.add(
                                            product
                                    );
                                }
                            }

                            sortProductsNewestFirst();

                            notifyProductAdapter();
                        }
                )
                .addOnFailureListener(
                        this::showSearchError
                );
    }

    // =========================================================
    // SEARCH ERROR
    // =========================================================

    private void showSearchError(
            Exception e) {

        if (!isAdded()) {
            return;
        }

        Toast.makeText(
                requireContext(),
                "Search failed: "
                        + e.getMessage(),
                Toast.LENGTH_SHORT
        ).show();
    }

    // =========================================================
    // PRODUCT SEARCH MATCH
    // =========================================================

    private boolean doesProductMatchSearch(
            Map<String, Object> product,
            String search) {

        String productName =
                getStringValue(
                        product,
                        "name"
                );

        String categoryName =
                getStringValue(
                        product,
                        "categoryName"
                );

        String description =
                getStringValue(
                        product,
                        "description"
                );

        String productNameLower =
                productName.toLowerCase(
                        Locale.ROOT
                );

        String categoryNameLower =
                categoryName.toLowerCase(
                        Locale.ROOT
                );

        String descriptionLower =
                description.toLowerCase(
                        Locale.ROOT
                );

        return productNameLower.contains(search)
                || categoryNameLower.contains(search)
                || descriptionLower.contains(search);
    }

    // =========================================================
    // PRODUCT VISIBILITY
    // =========================================================
    //
    // IMPORTANT:
    //
    // Product ko sirf status ki wajah se hide nahi karna
    // agar status "out of stock" hai.
    //
    // Out of stock product Buyer Home par visible rahega.
    //
    // Sirf explicitly inactive/disabled/deleted product hide
    // hoga.
    //
    // =========================================================

    private boolean isProductVisible(
            Map<String, Object> product) {

        if (product == null) {
            return false;
        }

        Object statusObject =
                product.get("status");

        if (statusObject == null) {
            return true;
        }

        String status =
                String.valueOf(statusObject)
                        .trim()
                        .toLowerCase(
                                Locale.ROOT
                        );

        if (status.isEmpty()) {
            return true;
        }

        // =====================================================
        // OUT OF STOCK PRODUCT KO HIDE NAHI KARNA
        // =====================================================

        if (status.equals("out of stock")
                || status.equals("out_of_stock")
                || status.equals("outofstock")) {

            return true;
        }

        // =====================================================
        // AVAILABLE PRODUCT
        // =====================================================

        if (status.equals("available")
                || status.equals("active")) {

            return true;
        }

        // =====================================================
        // EXPLICITLY DISABLED / DELETED PRODUCTS
        // =====================================================

        if (status.equals("inactive")
                || status.equals("disabled")
                || status.equals("deleted")
                || status.equals("removed")) {

            return false;
        }

        // =====================================================
        // UNKNOWN STATUS
        // Product ko unnecessarily hide nahi karna
        // =====================================================

        return true;
    }

    // =========================================================
    // NORMALIZE STOCK FIELDS
    // =========================================================
    //
    // totalStock primary hai.
    //
    // Agar old product mein sirf availableStock hai to
    // Buyer side temporarily totalStock mein copy kar dete hain.
    //
    // =========================================================

    private void normalizeStockFields(
            Map<String, Object> product) {

        if (product == null) {
            return;
        }

        Object totalStock =
                product.get("totalStock");

        Object availableStock =
                product.get("availableStock");

        // =====================================================
        // TOTAL STOCK AVAILABLE
        // =====================================================

        if (totalStock != null) {

            product.put(
                    "availableStock",
                    totalStock
            );

            return;
        }

        // =====================================================
        // OLD PRODUCT DATA
        // =====================================================

        if (availableStock != null) {

            product.put(
                    "totalStock",
                    availableStock
            );
        }
    }

    // =========================================================
    // SORT PRODUCTS
    // =========================================================

    private void sortProductsNewestFirst() {

        Collections.sort(
                productList,
                new Comparator<Map<String, Object>>() {

                    @Override
                    public int compare(
                            Map<String, Object> p1,
                            Map<String, Object> p2) {

                        return Long.compare(
                                getCreatedTime(p2),
                                getCreatedTime(p1)
                        );
                    }
                }
        );
    }

    // =========================================================
    // CREATED TIME
    // =========================================================

    private long getCreatedTime(
            Map<String, Object> product) {

        if (product == null) {
            return 0L;
        }

        Object createdAt =
                product.get("createdAt");

        if (createdAt instanceof Timestamp) {

            return ((Timestamp) createdAt)
                    .toDate()
                    .getTime();
        }

        if (createdAt instanceof Number) {

            return ((Number) createdAt)
                    .longValue();
        }

        return 0L;
    }

    // =========================================================
    // GET STRING
    // =========================================================

    private String getStringValue(
            Map<String, Object> data,
            String key) {

        if (data == null) {
            return "";
        }

        Object value =
                data.get(key);

        return value == null
                ? ""
                : String.valueOf(value);
    }

    // =========================================================
    // NOTIFY ADAPTER
    // =========================================================

    private void notifyProductAdapter() {

        if (productAdapter != null) {

            productAdapter.notifyDataSetChanged();
        }
    }

    // =========================================================
    // RESUME
    // =========================================================

    @Override
    public void onResume() {

        super.onResume();

        productClickLocked = false;

        if (db == null) {
            return;
        }

        loadUnreadNotificationCount();

        loadCategories();

        if (selectedCategoryId.isEmpty()) {

            loadAllProducts();

        } else {

            filterProductsByCategory(
                    selectedCategoryId,
                    selectedCategoryName
            );
        }
    }

    // =========================================================
    // DESTROY VIEW
    // =========================================================

    @Override
    public void onDestroyView() {

        productClickLocked = false;

        if (productAdapter != null) {

            productAdapter.stopTimeUpdates();
        }

        notificationCard = null;

        notificationBadge = null;

        recyclerCategories = null;

        recyclerFeaturedProducts = null;

        etSearch = null;

        categoryAdapter = null;

        productAdapter = null;

        super.onDestroyView();
    }
}