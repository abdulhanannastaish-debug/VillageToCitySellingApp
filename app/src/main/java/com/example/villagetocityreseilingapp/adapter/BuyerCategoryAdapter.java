package com.example.villagetocityreseilingapp.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.villagetocityreseilingapp.R;

import java.util.List;
import java.util.Map;

public class BuyerCategoryAdapter
        extends RecyclerView.Adapter<BuyerCategoryAdapter.CategoryViewHolder> {

    // =========================================================
    // CATEGORY LIST
    // =========================================================

    private final List<Map<String, Object>> categoryList;

    // =========================================================
    // CLICK LISTENER
    // =========================================================

    private final OnCategoryClickListener listener;

    // =========================================================
    // CATEGORY CLICK LISTENER
    // =========================================================

    public interface OnCategoryClickListener {

        void onCategoryClick(
                String categoryId,
                String categoryName
        );
    }

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public BuyerCategoryAdapter(
            List<Map<String, Object>> categoryList,
            OnCategoryClickListener listener) {

        this.categoryList = categoryList;
        this.listener = listener;
    }

    // =========================================================
    // CREATE VIEW HOLDER
    // =========================================================

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(
                        R.layout.item_buyer_category,
                        parent,
                        false
                );

        return new CategoryViewHolder(view);
    }

    // =========================================================
    // BIND CATEGORY
    // =========================================================

    @Override
    public void onBindViewHolder(
            @NonNull CategoryViewHolder holder,
            int position) {

        Map<String, Object> category =
                categoryList.get(position);

        // =====================================================
        // CATEGORY NAME
        // =====================================================

        String categoryName = "";

        Object nameObject =
                category.get("name");

        if (nameObject != null) {

            categoryName =
                    String.valueOf(nameObject)
                            .trim();
        }

        if (categoryName.isEmpty()) {

            categoryName = "Category";
        }

        holder.txtCategoryName.setText(
                categoryName
        );

        // =====================================================
        // CATEGORY ICON (NAYA)
        // =====================================================

        int iconResource =
                getCategoryIcon(categoryName);

        holder.imgCategoryIcon.setImageResource(
                iconResource
        );

        // =====================================================
        // 🔥 CATEGORY ID
        // =====================================================
        //
        // BuyerHomeFragment categories load karte waqt:
        //
        // categoryId = document.getId()
        //
        // save kar raha hai.
        //
        // Isliye sab se pehle categoryId use hoga.
        //

        String categoryId = "";

        Object categoryIdObject =
                category.get("categoryId");

        if (categoryIdObject != null) {

            categoryId =
                    String.valueOf(
                            categoryIdObject
                    ).trim();
        }

        // =====================================================
        // FIRESTORE DOCUMENT ID FALLBACK
        // =====================================================

        if (categoryId.isEmpty()) {

            Object firestoreIdObject =
                    category.get(
                            "firestoreDocumentId"
                    );

            if (firestoreIdObject != null) {

                categoryId =
                        String.valueOf(
                                firestoreIdObject
                        ).trim();
            }
        }

        // =====================================================
        // ID FIELD FALLBACK
        // =====================================================

        if (categoryId.isEmpty()) {

            Object idObject =
                    category.get("id");

            if (idObject != null) {

                categoryId =
                        String.valueOf(idObject)
                                .trim();
            }
        }

        // =====================================================
        // FINAL VALUES
        // =====================================================

        final String finalCategoryId =
                categoryId;

        final String finalCategoryName =
                categoryName;

        // =====================================================
        // DEBUG
        // =====================================================

        Log.d(
                "BUYER_CATEGORY",
                "Category = "
                        + finalCategoryName
                        + " | ID = "
                        + finalCategoryId
        );

        // =====================================================
        // 🔥 CATEGORY CLICK
        // =====================================================
        //
        // Poora Card clickable hai.
        //
        // User category ke naam ya card ke kisi bhi
        // area par click karega to listener call hoga.
        //

        holder.itemView.setOnClickListener(v -> {

            if (listener == null) {
                return;
            }

            // =================================================
            // INVALID ID
            // =================================================

            if (finalCategoryId.isEmpty()) {

                Toast.makeText(
                        v.getContext(),
                        "Category ID not found",
                        Toast.LENGTH_SHORT
                ).show();

                Log.e(
                        "BUYER_CATEGORY",
                        "Empty Category ID for "
                                + finalCategoryName
                );

                return;
            }

            // =================================================
            // SEND CATEGORY TO BUYER HOME
            // =================================================

            Log.d(
                    "BUYER_CATEGORY",
                    "CLICKED: "
                            + finalCategoryName
                            + " | ID = "
                            + finalCategoryId
            );

            listener.onCategoryClick(
                    finalCategoryId,
                    finalCategoryName
            );
        });
    }

    // =========================================================
    // CATEGORY ICON MAPPING (NAYA)
    // =========================================================
    //
    // Category ke naam ke hisaab se sahi icon return karta hai.
    // Agar koi match na ho to default icon dikhta hai.
    //
    // =========================================================

    private int getCategoryIcon(String categoryName) {

        switch (categoryName.trim().toLowerCase()) {

            case "clothes":
                return R.drawable.ic_category_clothes;

            case "dairy product":
                return R.drawable.ic_category_dairy;

            case "vegetable":
                return R.drawable.ic_category_vegetable;

            case "herbal":
                return R.drawable.ic_category_herbal;

            case "handcraft":
                return R.drawable.ic_category_handcraft;

            case "furniture":
                return R.drawable.ic_category_furniture;

            case "dry fruit":
                return R.drawable.ic_category_dryfruit;

            default:
                return R.drawable.ic_category_default;
        }
    }

    // =========================================================
    // ITEM COUNT
    // =========================================================

    @Override
    public int getItemCount() {

        if (categoryList == null) {
            return 0;
        }

        return categoryList.size();
    }

    // =========================================================
    // VIEW HOLDER
    // =========================================================

    public static class CategoryViewHolder
            extends RecyclerView.ViewHolder {

        TextView txtCategoryName;
        ImageView imgCategoryIcon;

        public CategoryViewHolder(
                @NonNull View itemView) {

            super(itemView);

            txtCategoryName =
                    itemView.findViewById(
                            R.id.txtCategoryName
                    );

            imgCategoryIcon =
                    itemView.findViewById(
                            R.id.imgCategoryIcon
                    );
        }
    }
}