package com.example.villagetocityreseilingapp.adapter;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.villagetocityreseilingapp.R;
import com.example.villagetocityreseilingapp.ui.main.seller.SellerEditProductFragment;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProductAdapter
        extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    // =========================================================
    // PRODUCT LIST
    // =========================================================

    private final List<Map<String, Object>> productList;

    // =========================================================
    // ACTIVITY
    // =========================================================

    private final FragmentActivity activity;

    // =========================================================
    // FIREBASE
    // =========================================================

    private final FirebaseAuth auth;
    private final FirebaseFirestore db;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public ProductAdapter(
            FragmentActivity activity,
            List<Map<String, Object>> productList) {

        this.activity = activity;
        this.productList = productList;

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        sortProducts();
    }

    // =========================================================
    // CREATE VIEW HOLDER
    // =========================================================

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view =
                LayoutInflater.from(parent.getContext())
                        .inflate(
                                R.layout.seller_item_product,
                                parent,
                                false
                        );

        return new ProductViewHolder(view);
    }

    // =========================================================
    // BIND DATA
    // =========================================================

    @Override
    public void onBindViewHolder(
            @NonNull ProductViewHolder holder,
            int position) {

        Map<String, Object> product =
                productList.get(position);

        // =====================================================
        // PRODUCT ID
        // =====================================================

        String productId =
                getStringValue(
                        product,
                        "productId",
                        ""
                );

        // =====================================================
        // PRODUCT NAME
        // =====================================================

        String name =
                getStringValue(
                        product,
                        "name",
                        "Product"
                );

        // =====================================================
        // PRICE
        // =====================================================

        double priceNumber =
                getDouble(
                        product.get("price")
                );

        String price;

        if (priceNumber == Math.floor(priceNumber)) {

            price =
                    String.valueOf(
                            (long) priceNumber
                    );

        } else {

            price =
                    String.format(
                            Locale.getDefault(),
                            "%.2f",
                            priceNumber
                    );
        }

        // =====================================================
        // STOCK
        // =====================================================

        Object stockValue =
                product.get("availableStock");

        if (stockValue == null) {

            stockValue =
                    product.get("totalStock");
        }

        if (stockValue == null) {

            stockValue =
                    product.get("quantity");
        }

        double stockNumber =
                getDouble(
                        stockValue
                );

        String availableStock =
                formatNumber(
                        stockValue
                );

        boolean isOutOfStock =
                stockNumber <= 0;

        // =====================================================
        // DESCRIPTION
        // =====================================================

        String description =
                getStringValue(
                        product,
                        "description",
                        ""
                );

        // =====================================================
        // SET NAME
        // =====================================================

        holder.txtProductName.setText(
                name
        );

        // =====================================================
        // SET PRICE
        // =====================================================

        holder.txtProductPrice.setText(
                "Price: Rs. " + price
        );

        // =====================================================
        // SET STOCK
        // =====================================================

        if (isOutOfStock) {

            holder.txtProductQuantity.setText(
                    "Available Stock: 0  •  OUT OF STOCK"
            );

            holder.txtProductQuantity.setTextColor(
                    Color.RED
            );

            holder.txtProductQuantity.setTypeface(
                    null,
                    Typeface.BOLD
            );

        } else {

            holder.txtProductQuantity.setText(
                    "Available Stock: "
                            + availableStock
            );

            holder.txtProductQuantity.setTextColor(
                    Color.parseColor("#555555")
            );

            holder.txtProductQuantity.setTypeface(
                    null,
                    Typeface.NORMAL
            );
        }

        // =====================================================
        // DESCRIPTION
        // =====================================================

        holder.txtProductDescription.setText(
                description
        );

        // =====================================================
        // STATUS
        // =====================================================

        if (isOutOfStock) {

            holder.txtProductStatus.setText(
                    "OUT OF STOCK"
            );

            holder.txtProductStatus.setTextColor(
                    Color.RED
            );

        } else {

            holder.txtProductStatus.setText(
                    "ACTIVE"
            );

            holder.txtProductStatus.setTextColor(
                    Color.rgb(
                            46,
                            125,
                            50
                    )
            );
        }

        holder.txtProductStatus.setTypeface(
                null,
                Typeface.BOLD
        );

        // =====================================================
        // PRODUCT DATE
        // =====================================================

        setProductAge(
                holder,
                product
        );

        // =====================================================
        // EDIT PRODUCT BUTTON
        // =====================================================

        holder.btnEditProduct.setVisibility(
                View.VISIBLE
        );

        holder.btnEditProduct.setEnabled(
                true
        );

        holder.btnEditProduct.setOnClickListener(
                v -> {

                    if (productId.isEmpty()) {

                        Toast.makeText(
                                activity,
                                "Product ID not found.",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }

                    SellerEditProductFragment fragment =
                            SellerEditProductFragment.newInstance(
                                    productId
                            );

                    activity.getSupportFragmentManager()
                            .beginTransaction()
                            .replace(
                                    R.id.fragment_container,
                                    fragment
                            )
                            .addToBackStack(null)
                            .commit();
                }
        );

        // =====================================================
        // DELETE PRODUCT BUTTON
        // =====================================================

        holder.btnDeleteProduct.setVisibility(
                View.VISIBLE
        );

        holder.btnDeleteProduct.setEnabled(
                true
        );

        holder.btnDeleteProduct.setOnClickListener(
                v -> {

                    if (productId.isEmpty()) {

                        Toast.makeText(
                                activity,
                                "Product ID not found.",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }

                    showDeleteConfirmation(
                            productId,
                            position
                    );
                }
        );
    }

    // =========================================================
    // DELETE CONFIRMATION
    // =========================================================

    private void showDeleteConfirmation(
            String productId,
            int position) {

        new AlertDialog.Builder(activity)
                .setTitle(
                        "Delete Product"
                )
                .setMessage(
                        "Are you sure you want to permanently delete this product?"
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .setPositiveButton(
                        "Delete",
                        (dialog, which) ->
                                permanentlyDeleteProduct(
                                        productId,
                                        position
                                )
                )
                .show();
    }

    // =========================================================
    // PERMANENT DELETE
    // =========================================================

    private void permanentlyDeleteProduct(
            String productId,
            int position) {

        FirebaseUser currentUser =
                auth.getCurrentUser();

        if (currentUser == null) {

            Toast.makeText(
                    activity,
                    "Seller is not logged in.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // =====================================================
        // DISABLE BUTTON
        // =====================================================

        if (position >= 0 &&
                position < productList.size()) {

            // Product remains visible until Firestore confirms
            // successful deletion.
        }

        // =====================================================
        // PERMANENT FIRESTORE DELETE
        // =====================================================

        db.collection("products")
                .document(productId)
                .delete()
                .addOnSuccessListener(unused -> {

                    // =========================================
                    // REMOVE FROM LOCAL LIST
                    // =========================================

                    int currentPosition =
                            findProductPosition(
                                    productId
                            );

                    if (currentPosition != -1) {

                        productList.remove(
                                currentPosition
                        );

                        notifyItemRemoved(
                                currentPosition
                        );
                    }

                    Toast.makeText(
                            activity,
                            "Product permanently deleted.",
                            Toast.LENGTH_SHORT
                    ).show();
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            activity,
                            "Failed to delete product: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    // =========================================================
    // FIND PRODUCT POSITION
    // =========================================================

    private int findProductPosition(
            String productId) {

        for (
                int i = 0;
                i < productList.size();
                i++
        ) {

            String id =
                    getStringValue(
                            productList.get(i),
                            "productId",
                            ""
                    );

            if (id.equals(productId)) {

                return i;
            }
        }

        return -1;
    }

    // =========================================================
    // SORT PRODUCTS
    // =========================================================
    //
    // NEWEST PRODUCTS FIRST
    //
    // =========================================================

    public void sortProducts() {

        Collections.sort(
                productList,
                new Comparator<Map<String, Object>>() {

                    @Override
                    public int compare(
                            Map<String, Object> product1,
                            Map<String, Object> product2) {

                        Date date1 =
                                getCreatedDate(
                                        product1
                                );

                        Date date2 =
                                getCreatedDate(
                                        product2
                                );

                        if (
                                date1 == null
                                        &&
                                        date2 == null
                        ) {

                            return 0;
                        }

                        if (date1 == null) {

                            return 1;
                        }

                        if (date2 == null) {

                            return -1;
                        }

                        return date2.compareTo(
                                date1
                        );
                    }
                }
        );
    }

    // =========================================================
    // GET CREATED DATE
    // =========================================================

    private Date getCreatedDate(
            Map<String, Object> product) {

        if (product == null) {
            return null;
        }

        Object createdAt =
                product.get("createdAt");

        // =====================================================
        // FIREBASE TIMESTAMP
        // =====================================================

        if (createdAt instanceof Timestamp) {

            return ((Timestamp) createdAt)
                    .toDate();
        }

        // =====================================================
        // JAVA DATE
        // =====================================================

        if (createdAt instanceof Date) {

            return (Date) createdAt;
        }

        // =====================================================
        // NUMBER
        // =====================================================

        if (createdAt instanceof Number) {

            return new Date(
                    ((Number) createdAt)
                            .longValue()
            );
        }

        // =====================================================
        // STRING TIMESTAMP
        // =====================================================

        if (createdAt != null) {

            try {

                long time =
                        Long.parseLong(
                                String.valueOf(
                                        createdAt
                                )
                        );

                if (time > 0) {

                    return new Date(
                            time
                    );
                }

            } catch (Exception ignored) {
            }
        }

        return null;
    }

    // =========================================================
    // PRODUCT AGE
    // =========================================================

    private void setProductAge(
            ProductViewHolder holder,
            Map<String, Object> product) {

        Date createdDate =
                getCreatedDate(
                        product
                );

        if (createdDate == null) {

            holder.txtProductAge.setText(
                    "Date unavailable"
            );

            return;
        }

        long difference =
                System.currentTimeMillis()
                        - createdDate.getTime();

        if (difference < 0) {
            difference = 0;
        }

        long oneDay =
                24L
                        * 60L
                        * 60L
                        * 1000L;

        long daysPassed =
                difference / oneDay;

        if (daysPassed == 0) {

            holder.txtProductAge.setText(
                    "Added today"
            );

            return;
        }

        if (daysPassed == 1) {

            holder.txtProductAge.setText(
                    "Added 1 day ago"
            );

            return;
        }

        if (daysPassed <= 3) {

            holder.txtProductAge.setText(
                    "Added "
                            + daysPassed
                            + " days ago"
            );

            return;
        }

        SimpleDateFormat dateFormat =
                new SimpleDateFormat(
                        "dd MMM yyyy",
                        Locale.getDefault()
                );

        holder.txtProductAge.setText(
                "Added on "
                        + dateFormat.format(
                        createdDate
                )
        );
    }

    // =========================================================
    // GET STRING
    // =========================================================

    private String getStringValue(
            Map<String, Object> data,
            String key,
            String defaultValue) {

        if (data == null) {
            return defaultValue;
        }

        Object value =
                data.get(key);

        if (value == null) {
            return defaultValue;
        }

        String result =
                String.valueOf(
                        value
                ).trim();

        if (result.isEmpty()) {

            return defaultValue;
        }

        return result;
    }

    // =========================================================
    // GET DOUBLE
    // =========================================================

    private double getDouble(
            Object value) {

        if (value instanceof Number) {

            return ((Number) value)
                    .doubleValue();
        }

        if (value != null) {

            try {

                return Double.parseDouble(
                        String.valueOf(
                                value
                        )
                );

            } catch (Exception ignored) {
            }
        }

        return 0.0;
    }

    // =========================================================
    // FORMAT NUMBER
    // =========================================================

    private String formatNumber(
            Object value) {

        if (value == null) {

            return "0";
        }

        if (value instanceof Number) {

            double number =
                    ((Number) value)
                            .doubleValue();

            if (
                    number
                            == Math.floor(
                            number
                    )
            ) {

                return String.valueOf(
                        (long) number
                );
            }

            return String.format(
                    Locale.getDefault(),
                    "%.2f",
                    number
            );
        }

        String text =
                String.valueOf(
                        value
                ).trim();

        try {

            double number =
                    Double.parseDouble(
                            text
                    );

            if (
                    number
                            == Math.floor(
                            number
                    )
            ) {

                return String.valueOf(
                        (long) number
                );
            }

            return String.format(
                    Locale.getDefault(),
                    "%.2f",
                    number
            );

        } catch (Exception ignored) {

            return text;
        }
    }

    // =========================================================
    // ITEM COUNT
    // =========================================================

    @Override
    public int getItemCount() {

        return productList.size();
    }

    // =========================================================
    // VIEW HOLDER
    // =========================================================

    public static class ProductViewHolder
            extends RecyclerView.ViewHolder {

        TextView txtProductName;
        TextView txtProductPrice;
        TextView txtProductQuantity;
        TextView txtProductDescription;
        TextView txtProductStatus;
        TextView txtProductAge;

        AppCompatButton btnEditProduct;
        AppCompatButton btnDeleteProduct;

        public ProductViewHolder(
                @NonNull View itemView) {

            super(itemView);

            txtProductName =
                    itemView.findViewById(
                            R.id.txtProductName
                    );

            txtProductPrice =
                    itemView.findViewById(
                            R.id.txtProductPrice
                    );

            txtProductQuantity =
                    itemView.findViewById(
                            R.id.txtProductQuantity
                    );

            txtProductDescription =
                    itemView.findViewById(
                            R.id.txtProductDescription
                    );

            txtProductStatus =
                    itemView.findViewById(
                            R.id.txtProductStatus
                    );

            txtProductAge =
                    itemView.findViewById(
                            R.id.txtProductAge
                    );

            btnEditProduct =
                    itemView.findViewById(
                            R.id.btnEditProduct
                    );

            btnDeleteProduct =
                    itemView.findViewById(
                            R.id.btnDeleteProduct
                    );
        }
    }
}