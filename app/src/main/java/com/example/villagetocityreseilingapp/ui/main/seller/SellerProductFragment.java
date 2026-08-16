package com.example.villagetocityreseilingapp.ui.main.seller;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.villagetocityreseilingapp.R;
import com.example.villagetocityreseilingapp.adapter.ProductAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SellerProductFragment extends Fragment {

    // =========================================================
    // VIEWS
    // =========================================================

    private Button btnAdd;
    private TextView txtEmpty;
    private RecyclerView recyclerProducts;
    private ProgressBar progressProducts;

    // =========================================================
    // FIREBASE
    // =========================================================

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    // =========================================================
    // FIRESTORE LISTENER
    // =========================================================

    private ListenerRegistration productsListener;

    // =========================================================
    // PRODUCT LIST
    // =========================================================

    private final List<Map<String, Object>> productList =
            new ArrayList<>();

    private ProductAdapter productAdapter;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public SellerProductFragment() {
        // Required empty public constructor
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
                R.layout.fragment_seller_product,
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

        btnAdd = view.findViewById(R.id.btnAdd);

        txtEmpty = view.findViewById(R.id.txtEmpty);

        recyclerProducts =
                view.findViewById(R.id.recyclerProducts);

        progressProducts =
                view.findViewById(R.id.progressProducts);

        // =====================================================
        // INITIAL STATE
        // =====================================================

        progressProducts.setVisibility(View.VISIBLE);

        txtEmpty.setVisibility(View.GONE);

        recyclerProducts.setVisibility(View.GONE);

        // =====================================================
        // RECYCLER VIEW
        // =====================================================

        recyclerProducts.setLayoutManager(
                new LinearLayoutManager(
                        requireContext()
                )
        );

        recyclerProducts.setHasFixedSize(false);

        // =====================================================
        // PRODUCT ADAPTER
        // =====================================================

        productAdapter =
                new ProductAdapter(
                        requireActivity(),
                        productList
                );

        recyclerProducts.setAdapter(
                productAdapter
        );

        // =====================================================
        // ADD PRODUCT
        // =====================================================

        btnAdd.setOnClickListener(v -> {

            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(
                            R.id.fragment_container,
                            new SellerAddProductFragment()
                    )
                    .addToBackStack(null)
                    .commit();

        });

        // =====================================================
        // LOAD PRODUCTS
        // =====================================================

        loadSellerProducts();
    }

    // =========================================================
    // LOAD SELLER PRODUCTS
    // =========================================================

    private void loadSellerProducts() {

        if (!isAdded()) {
            return;
        }

        // =====================================================
        // CURRENT USER
        // =====================================================

        FirebaseUser currentUser =
                auth.getCurrentUser();

        // =====================================================
        // CHECK LOGIN
        // =====================================================

        if (currentUser == null) {

            progressProducts.setVisibility(
                    View.GONE
            );

            recyclerProducts.setVisibility(
                    View.GONE
            );

            txtEmpty.setVisibility(
                    View.VISIBLE
            );

            txtEmpty.setText(
                    "Seller is not logged in."
            );

            return;
        }

        String sellerId =
                currentUser.getUid();

        // =====================================================
        // REMOVE OLD LISTENER
        // =====================================================

        if (productsListener != null) {

            productsListener.remove();

            productsListener = null;
        }

        // =====================================================
        // LOADING STATE
        // =====================================================

        progressProducts.setVisibility(
                View.VISIBLE
        );

        txtEmpty.setVisibility(
                View.GONE
        );

        recyclerProducts.setVisibility(
                View.GONE
        );

        // =====================================================
        // FIRESTORE REAL-TIME LISTENER
        // =====================================================

        productsListener =
                db.collection("products")
                        .addSnapshotListener(
                                (queryDocumentSnapshots, error) -> {

                                    if (!isAdded()) {
                                        return;
                                    }

                                    // =================================
                                    // ERROR
                                    // =================================

                                    if (error != null) {

                                        progressProducts.setVisibility(
                                                View.GONE
                                        );

                                        recyclerProducts.setVisibility(
                                                View.GONE
                                        );

                                        txtEmpty.setVisibility(
                                                View.VISIBLE
                                        );

                                        txtEmpty.setText(
                                                "Unable to load your products."
                                        );

                                        Toast.makeText(
                                                requireContext(),
                                                "Failed to load products: "
                                                        + error.getMessage(),
                                                Toast.LENGTH_LONG
                                        ).show();

                                        return;
                                    }

                                    // =================================
                                    // NULL SNAPSHOT
                                    // =================================

                                    if (queryDocumentSnapshots == null) {

                                        showEmptyProducts();

                                        return;
                                    }

                                    // =================================
                                    // CLEAR OLD LIST
                                    // =================================

                                    productList.clear();

                                    // =================================
                                    // GET SELLER PRODUCTS
                                    // =================================

                                    for (
                                            QueryDocumentSnapshot document
                                            : queryDocumentSnapshots
                                    ) {

                                        Map<String, Object> product =
                                                document.getData();

                                        // =============================
                                        // SELLER ID
                                        // =============================

                                        Object sellerIdObject =
                                                product.get("sellerId");

                                        if (sellerIdObject == null) {
                                            continue;
                                        }

                                        String productSellerId =
                                                String.valueOf(
                                                        sellerIdObject
                                                ).trim();

                                        // =============================
                                        // CURRENT SELLER ONLY
                                        // =============================

                                        if (!sellerId.equals(
                                                productSellerId
                                        )) {
                                            continue;
                                        }

                                        // =============================
                                        // CHECK PRODUCT STATUS
                                        // =============================
                                        //
                                        // IMPORTANT:
                                        //
                                        // Agar kisi purane product ka
                                        // status "deleted" hai to usko
                                        // list mein add nahi karna.
                                        //
                                        // =============================

                                        Object statusObject =
                                                product.get("status");

                                        if (statusObject != null) {

                                            String status =
                                                    String.valueOf(
                                                                    statusObject
                                                            )
                                                            .trim()
                                                            .toLowerCase();

                                            if (
                                                    status.equals("deleted")
                                                            ||
                                                            status.equals("delete")
                                            ) {
                                                continue;
                                            }
                                        }

                                        // =============================
                                        // PRODUCT ID
                                        // =============================

                                        if (
                                                !product.containsKey(
                                                        "productId"
                                                )
                                                        ||
                                                        product.get(
                                                                "productId"
                                                        ) == null
                                                        ||
                                                        String.valueOf(
                                                                        product.get(
                                                                                "productId"
                                                                        )
                                                                )
                                                                .trim()
                                                                .isEmpty()
                                        ) {

                                            product.put(
                                                    "productId",
                                                    document.getId()
                                            );
                                        }

                                        // =============================
                                        // ADD PRODUCT
                                        // =============================

                                        productList.add(
                                                product
                                        );
                                    }

                                    // =================================
                                    // LOADING COMPLETE
                                    // =================================

                                    progressProducts.setVisibility(
                                            View.GONE
                                    );

                                    // =================================
                                    // NO PRODUCTS
                                    // =================================

                                    if (productList.isEmpty()) {

                                        showEmptyProducts();

                                        return;
                                    }

                                    // =================================
                                    // SORT
                                    // =================================

                                    productAdapter.sortProducts();

                                    // =================================
                                    // SHOW PRODUCTS
                                    // =================================

                                    txtEmpty.setVisibility(
                                            View.GONE
                                    );

                                    recyclerProducts.setVisibility(
                                            View.VISIBLE
                                    );

                                    // =================================
                                    // REFRESH ADAPTER
                                    // =================================

                                    productAdapter.notifyDataSetChanged();
                                }
                        );
    }

    // =========================================================
    // SHOW EMPTY PRODUCTS
    // =========================================================

    private void showEmptyProducts() {

        if (!isAdded()) {
            return;
        }

        progressProducts.setVisibility(
                View.GONE
        );

        recyclerProducts.setVisibility(
                View.GONE
        );

        txtEmpty.setVisibility(
                View.VISIBLE
        );

        txtEmpty.setText(
                "No product items yet. Click '+ Add Product' to create your product!"
        );
    }

    // =========================================================
    // STOP FIRESTORE LISTENER
    // =========================================================

    @Override
    public void onDestroyView() {

        super.onDestroyView();

        if (productsListener != null) {

            productsListener.remove();

            productsListener = null;
        }
    }

    // =========================================================
    // RELOAD WHEN SCREEN RESUMES
    // =========================================================

    @Override
    public void onResume() {

        super.onResume();

        if (
                auth != null
                        && db != null
                        && productsListener == null
        ) {

            loadSellerProducts();
        }
    }
}