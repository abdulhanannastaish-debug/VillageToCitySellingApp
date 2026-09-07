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

    private Button btnAdd;
    private TextView txtEmpty;
    private RecyclerView recyclerProducts;
    private ProgressBar progressProducts;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private ListenerRegistration productsListener;

    private final List<Map<String, Object>> productList =
            new ArrayList<>();

    private ProductAdapter productAdapter;

    public SellerProductFragment() {
    }

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

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        // Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Find Views
        btnAdd = view.findViewById(R.id.btnAdd);
        txtEmpty = view.findViewById(R.id.txtEmpty);
        recyclerProducts = view.findViewById(R.id.recyclerProducts);
        progressProducts = view.findViewById(R.id.progressProducts);

        // Safety check
        if (btnAdd == null) {
            Toast.makeText(
                    requireContext(),
                    "btnAdd NOT FOUND",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        // RecyclerView
        recyclerProducts.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );

        productAdapter = new ProductAdapter(
                requireActivity(),
                productList
        );

        recyclerProducts.setAdapter(productAdapter);

        // Initial UI
        progressProducts.setVisibility(View.VISIBLE);
        txtEmpty.setVisibility(View.GONE);
        recyclerProducts.setVisibility(View.GONE);

        // =====================================================
        // ADD PRODUCT BUTTON
        // =====================================================

        btnAdd.setEnabled(true);
        btnAdd.setClickable(true);
        btnAdd.setFocusable(true);

        btnAdd.setOnClickListener(v -> {

            Toast.makeText(
                    requireContext(),
                    "BUTTON CLICKED",
                    Toast.LENGTH_SHORT
            ).show();

            getParentFragmentManager()
                    .beginTransaction()
                    .replace(
                            R.id.fragment_container,
                            new SellerAddProductFragment()
                    )
                    .addToBackStack(null)
                    .commit();
        });

        // Load products
        loadSellerProducts();
    }

    // =========================================================
    // LOAD SELLER PRODUCTS
    // =========================================================

    private void loadSellerProducts() {

        if (!isAdded()) {
            return;
        }

        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {

            progressProducts.setVisibility(View.GONE);
            recyclerProducts.setVisibility(View.GONE);
            txtEmpty.setVisibility(View.VISIBLE);

            txtEmpty.setText("Seller is not logged in.");

            return;
        }

        String sellerId = user.getUid();

        if (productsListener != null) {
            productsListener.remove();
            productsListener = null;
        }

        progressProducts.setVisibility(View.VISIBLE);
        txtEmpty.setVisibility(View.GONE);
        recyclerProducts.setVisibility(View.GONE);

        productsListener = db.collection("products")
                .addSnapshotListener((snapshot, error) -> {

                    if (!isAdded()) {
                        return;
                    }

                    if (error != null) {

                        progressProducts.setVisibility(View.GONE);
                        recyclerProducts.setVisibility(View.GONE);
                        txtEmpty.setVisibility(View.VISIBLE);

                        txtEmpty.setText(
                                "Unable to load products."
                        );

                        Toast.makeText(
                                requireContext(),
                                "Failed: " + error.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();

                        return;
                    }

                    if (snapshot == null) {
                        showEmptyProducts();
                        return;
                    }

                    productList.clear();

                    for (
                            QueryDocumentSnapshot document :
                            snapshot
                    ) {

                        Map<String, Object> product =
                                document.getData();

                        Object sellerObject =
                                product.get("sellerId");

                        if (sellerObject == null) {
                            continue;
                        }

                        String productSellerId =
                                String.valueOf(sellerObject).trim();

                        if (!sellerId.equals(productSellerId)) {
                            continue;
                        }

                        Object statusObject =
                                product.get("status");

                        if (statusObject != null) {

                            String status =
                                    String.valueOf(statusObject)
                                            .trim()
                                            .toLowerCase();

                            if (status.equals("deleted")
                                    || status.equals("delete")) {

                                continue;
                            }
                        }

                        if (!product.containsKey("productId")
                                || product.get("productId") == null
                                || String.valueOf(
                                product.get("productId")
                        ).trim().isEmpty()) {

                            product.put(
                                    "productId",
                                    document.getId()
                            );
                        }

                        productList.add(product);
                    }

                    progressProducts.setVisibility(View.GONE);

                    if (productList.isEmpty()) {
                        showEmptyProducts();
                        return;
                    }

                    productAdapter.sortProducts();

                    txtEmpty.setVisibility(View.GONE);
                    recyclerProducts.setVisibility(View.VISIBLE);

                    productAdapter.notifyDataSetChanged();
                });
    }

    // =========================================================
    // EMPTY
    // =========================================================

    private void showEmptyProducts() {

        if (!isAdded()) {
            return;
        }

        progressProducts.setVisibility(View.GONE);
        recyclerProducts.setVisibility(View.GONE);
        txtEmpty.setVisibility(View.VISIBLE);

        txtEmpty.setText(
                "No products yet. Click Add Product."
        );
    }

    // =========================================================
    // DESTROY
    // =========================================================

    @Override
    public void onDestroyView() {

        if (productsListener != null) {
            productsListener.remove();
            productsListener = null;
        }

        super.onDestroyView();
    }

    // =========================================================
    // RESUME
    // =========================================================

    @Override
    public void onResume() {

        super.onResume();

        if (auth != null
                && db != null
                && productsListener == null) {

            loadSellerProducts();
        }
    }
}