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

    private final List<Map<String, Object>> productList =
            new ArrayList<>();

    private ProductAdapter productAdapter;

    public SellerProductFragment() {
        // Required empty public constructor
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

        // ================= FIREBASE =================

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // ================= FIND VIEWS =================

        btnAdd = view.findViewById(R.id.btnAdd);

        txtEmpty = view.findViewById(R.id.txtEmpty);

        recyclerProducts =
                view.findViewById(R.id.recyclerProducts);

        progressProducts =
                view.findViewById(R.id.progressProducts);

        // ================= INITIAL STATE =================

        progressProducts.setVisibility(View.VISIBLE);
        txtEmpty.setVisibility(View.GONE);
        recyclerProducts.setVisibility(View.GONE);

        // ================= RECYCLER VIEW =================

        recyclerProducts.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );

        productAdapter =
                new ProductAdapter(
                        requireActivity(),
                        productList
                );

        recyclerProducts.setAdapter(
                productAdapter
        );

        // ================= ADD PRODUCT =================

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

        // ================= LOAD PRODUCTS =================

        loadSellerProducts();
    }

    // =========================================================
    // LOAD SELLER PRODUCTS FROM FIRESTORE
    // =========================================================

    private void loadSellerProducts() {

        FirebaseUser currentUser =
                auth.getCurrentUser();

        if (currentUser == null) {

            progressProducts.setVisibility(View.GONE);

            txtEmpty.setVisibility(View.VISIBLE);

            recyclerProducts.setVisibility(View.GONE);

            Toast.makeText(
                    requireContext(),
                    "Seller is not logged in.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String sellerId =
                currentUser.getUid();

        // ================= LOADING STATE =================

        progressProducts.setVisibility(View.VISIBLE);

        txtEmpty.setVisibility(View.GONE);

        recyclerProducts.setVisibility(View.GONE);

        // ================= FIRESTORE =================

        db.collection("products")
                .whereEqualTo(
                        "sellerId",
                        sellerId
                )
                .get()
                .addOnSuccessListener(
                        queryDocumentSnapshots -> {

                            if (!isAdded()) {
                                return;
                            }

                            productList.clear();

                            // ================= LOADING END =================

                            progressProducts.setVisibility(
                                    View.GONE
                            );

                            // ================= NO PRODUCTS =================

                            if (queryDocumentSnapshots.isEmpty()) {

                                txtEmpty.setVisibility(
                                        View.VISIBLE
                                );

                                recyclerProducts.setVisibility(
                                        View.GONE
                                );

                            }

                            // ================= PRODUCTS FOUND =================

                            else {

                                txtEmpty.setVisibility(
                                        View.GONE
                                );

                                recyclerProducts.setVisibility(
                                        View.VISIBLE
                                );

                                for (
                                        com.google.firebase.firestore.QueryDocumentSnapshot document
                                        : queryDocumentSnapshots
                                ) {

                                    Map<String, Object> product =
                                            document.getData();

                                    // Make sure productId exists
                                    if (!product.containsKey("productId")) {

                                        product.put(
                                                "productId",
                                                document.getId()
                                        );
                                    }

                                    productList.add(product);
                                }

                                productAdapter.notifyDataSetChanged();
                            }

                        })
                .addOnFailureListener(e -> {

                    if (!isAdded()) {
                        return;
                    }

                    // ================= LOADING END =================

                    progressProducts.setVisibility(
                            View.GONE
                    );

                    recyclerProducts.setVisibility(
                            View.GONE
                    );

                    txtEmpty.setVisibility(
                            View.VISIBLE
                    );

                    Toast.makeText(
                            requireContext(),
                            "Failed to load products: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();

                });
    }

    // =========================================================
    // RELOAD PRODUCTS
    // =========================================================

    @Override
    public void onResume() {

        super.onResume();

        /*
         * Product add/update/delete ke baad
         * list dobara load hogi.
         */

        if (auth != null) {

            loadSellerProducts();
        }
    }
}