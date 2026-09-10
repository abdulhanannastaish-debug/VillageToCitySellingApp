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

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProductAdapter
        extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private final List<Map<String, Object>> productList;
    private final FragmentActivity activity;

    private final FirebaseAuth auth;
    private final FirebaseFirestore db;

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

        View view = LayoutInflater.from(parent.getContext())
                .inflate(
                        R.layout.seller_item_product,
                        parent,
                        false
                );

        return new ProductViewHolder(view);
    }

    // =========================================================
    // BIND VIEW
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
        // NAME
        // =====================================================

        String name =
                getStringValue(
                        product,
                        "name",
                        "Product"
                );

        holder.txtProductName.setText(name);

        // =====================================================
        // PRICE
        // =====================================================

        double priceNumber =
                getDouble(product.get("price"));

        String price;

        if (priceNumber == Math.floor(priceNumber)) {

            price = String.valueOf(
                    (long) priceNumber
            );

        } else {

            price = String.format(
                    Locale.getDefault(),
                    "%.2f",
                    priceNumber
            );
        }

        holder.txtProductPrice.setText(
                activity.getString(
                        R.string.product_price_label,
                        price
                )
        );

        // =====================================================
        // STOCK
        // =====================================================

        Object stockValue =
                product.get("availableStock");

        if (stockValue == null) {
            stockValue = product.get("totalStock");
        }

        if (stockValue == null) {
            stockValue = product.get("quantity");
        }

        double stockNumber =
                getDouble(stockValue);

        String availableStock =
                formatNumber(stockValue);

        boolean isOutOfStock =
                stockNumber <= 0;

        if (isOutOfStock) {

            holder.txtProductQuantity.setText(
                    activity.getString(
                            R.string.available_stock_label,
                            "0"
                    )
                            + " • "
                            + activity.getString(
                            R.string.out_of_stock
                    )
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
                    activity.getString(
                            R.string.available_stock_label,
                            availableStock
                    )
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

        String description =
                getStringValue(
                        product,
                        "description",
                        ""
                );

        holder.txtProductDescription.setText(
                description
        );

        // =====================================================
        // STATUS
        // =====================================================

        if (isOutOfStock) {

            holder.txtProductStatus.setText(
                    R.string.out_of_stock
            );

            holder.txtProductStatus.setTextColor(
                    Color.RED
            );

        } else {

            holder.txtProductStatus.setText(
                    R.string.active
            );

            holder.txtProductStatus.setTextColor(
                    Color.rgb(46, 125, 50)
            );
        }

        holder.txtProductStatus.setTypeface(
                null,
                Typeface.BOLD
        );

        // =====================================================
        // DATE
        // =====================================================

        setProductAge(
                holder,
                product
        );

        // =====================================================
        // EDIT BUTTON
        // =====================================================

        holder.btnEditProduct.setVisibility(
                View.VISIBLE
        );

        holder.btnEditProduct.setEnabled(
                true
        );

        holder.btnEditProduct.setClickable(
                true
        );

        // Urdu Edit text
        holder.btnEditProduct.setText(
                R.string.edit_product
        );

        holder.btnEditProduct.setOnClickListener(
                view -> {

                    // -----------------------------------------
                    // SAFELY GET CURRENT POSITION
                    // -----------------------------------------

                    int adapterPosition =
                            holder.getBindingAdapterPosition();

                    if (adapterPosition ==
                            RecyclerView.NO_POSITION) {
                        return;
                    }

                    // -----------------------------------------
                    // GET CURRENT PRODUCT
                    // -----------------------------------------

                    Map<String, Object> selectedProduct =
                            productList.get(
                                    adapterPosition
                            );

                    // -----------------------------------------
                    // GET PRODUCT ID AGAIN
                    // -----------------------------------------

                    String selectedProductId =
                            getStringValue(
                                    selectedProduct,
                                    "productId",
                                    ""
                            );

                    // -----------------------------------------
                    // CHECK PRODUCT ID
                    // -----------------------------------------

                    if (selectedProductId.isEmpty()) {

                        Toast.makeText(
                                activity,
                                activity.getString(
                                        R.string.product_id_not_found
                                ),
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }

                    // -----------------------------------------
                    // OPEN EDIT FRAGMENT
                    // -----------------------------------------

                    SellerEditProductFragment editFragment =
                            SellerEditProductFragment
                                    .newInstance(
                                            selectedProductId
                                    );

                    activity
                            .getSupportFragmentManager()
                            .beginTransaction()
                            .replace(
                                    R.id.fragment_container,
                                    editFragment
                            )
                            .addToBackStack(null)
                            .commit();
                }
        );

        // =====================================================
        // DELETE BUTTON
        // =====================================================

        holder.btnDeleteProduct.setVisibility(
                View.VISIBLE
        );

        holder.btnDeleteProduct.setEnabled(
                true
        );

        holder.btnDeleteProduct.setClickable(
                true
        );

        // Urdu Delete text
        holder.btnDeleteProduct.setText(
                R.string.delete_product
        );

        holder.btnDeleteProduct.setOnClickListener(
                view -> {

                    int adapterPosition =
                            holder.getBindingAdapterPosition();

                    if (adapterPosition ==
                            RecyclerView.NO_POSITION) {
                        return;
                    }

                    Map<String, Object> selectedProduct =
                            productList.get(
                                    adapterPosition
                            );

                    String selectedProductId =
                            getStringValue(
                                    selectedProduct,
                                    "productId",
                                    ""
                            );

                    if (selectedProductId.isEmpty()) {

                        Toast.makeText(
                                activity,
                                activity.getString(
                                        R.string.product_id_not_found
                                ),
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }

                    showDeleteConfirmation(
                            selectedProductId,
                            adapterPosition
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
                        R.string.delete_product
                )
                .setMessage(
                        R.string.delete_product_message
                )
                .setNegativeButton(
                        R.string.cancel,
                        null
                )
                .setPositiveButton(
                        R.string.delete,
                        (dialog, which) ->
                                permanentlyDeleteProduct(
                                        productId,
                                        position
                                )
                )
                .show();
    }

    // =========================================================
    // DELETE PRODUCT
    // =========================================================

    private void permanentlyDeleteProduct(
            String productId,
            int position) {

        FirebaseUser currentUser =
                auth.getCurrentUser();

        if (currentUser == null) {

            Toast.makeText(
                    activity,
                    activity.getString(
                            R.string.seller_not_logged_in
                    ),
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        db.collection("products")
                .document(productId)
                .delete()
                .addOnSuccessListener(
                        unused -> {

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
                                    activity.getString(
                                            R.string.product_deleted
                                    ),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                )
                .addOnFailureListener(
                        e -> {

                            Toast.makeText(
                                    activity,
                                    activity.getString(
                                            R.string.delete_failed,
                                            e.getMessage()
                                    ),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
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

    public void sortProducts() {

        Collections.sort(
                productList,
                new Comparator<Map<String, Object>>() {

                    @Override
                    public int compare(
                            Map<String, Object> product1,
                            Map<String, Object> product2) {

                        Date date1 =
                                getCreatedDate(product1);

                        Date date2 =
                                getCreatedDate(product2);

                        if (date1 == null &&
                                date2 == null) {

                            return 0;
                        }

                        if (date1 == null) {
                            return 1;
                        }

                        if (date2 == null) {
                            return -1;
                        }

                        return date2.compareTo(date1);
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

        if (createdAt instanceof Timestamp) {

            return ((Timestamp) createdAt).toDate();
        }

        if (createdAt instanceof Date) {

            return (Date) createdAt;
        }

        if (createdAt instanceof Number) {

            return new Date(
                    ((Number) createdAt).longValue()
            );
        }

        if (createdAt != null) {

            try {

                long time =
                        Long.parseLong(
                                String.valueOf(
                                        createdAt
                                )
                        );

                if (time > 0) {

                    return new Date(time);
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
                getCreatedDate(product);

        if (createdDate == null) {

            holder.txtProductAge.setText(
                    R.string.date_unavailable
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
                24L * 60L * 60L * 1000L;

        long daysPassed =
                difference / oneDay;

        if (daysPassed == 0) {

            holder.txtProductAge.setText(
                    R.string.added_today
            );

            return;
        }

        if (daysPassed == 1) {

            holder.txtProductAge.setText(
                    R.string.added_one_day
            );

            return;
        }

        if (daysPassed <= 3) {

            holder.txtProductAge.setText(
                    activity.getString(
                            R.string.added_days,
                            daysPassed
                    )
            );

            return;
        }

        java.text.SimpleDateFormat dateFormat =
                new java.text.SimpleDateFormat(
                        "dd MMM yyyy",
                        Locale.getDefault()
                );

        holder.txtProductAge.setText(
                activity.getString(
                        R.string.added_on,
                        dateFormat.format(createdDate)
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
                String.valueOf(value).trim();

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

            return ((Number) value).doubleValue();
        }

        if (value != null) {

            try {

                return Double.parseDouble(
                        String.valueOf(value)
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
                    ((Number) value).doubleValue();

            if (number == Math.floor(number)) {

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
                String.valueOf(value).trim();

        try {

            double number =
                    Double.parseDouble(text);

            if (number == Math.floor(number)) {

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