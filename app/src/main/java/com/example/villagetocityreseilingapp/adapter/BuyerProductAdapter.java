package com.example.villagetocityreseilingapp.adapter;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.villagetocityreseilingapp.R;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class BuyerProductAdapter
        extends RecyclerView.Adapter<BuyerProductAdapter.ProductViewHolder> {

    private final List<Map<String, Object>> productList;

    // =========================================================
    // PRODUCT CLICK LISTENER
    // =========================================================

    public interface OnProductClickListener {

        void onProductClick(
                Map<String, Object> product
        );
    }

    private OnProductClickListener listener;

    // =========================================================
    // LIVE TIME
    // =========================================================

    private final Handler timeHandler =
            new Handler(Looper.getMainLooper());

    private final Runnable timeUpdater =
            new Runnable() {

                @Override
                public void run() {

                    if (!productList.isEmpty()) {
                        notifyDataSetChanged();
                    }

                    timeHandler.postDelayed(
                            this,
                            30 * 1000
                    );
                }
            };

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public BuyerProductAdapter(
            List<Map<String, Object>> productList) {

        this.productList = productList;

        timeHandler.post(timeUpdater);
    }

    // =========================================================
    // SET CLICK LISTENER
    // =========================================================

    public void setOnProductClickListener(
            OnProductClickListener listener) {

        this.listener = listener;
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
                                R.layout.item_buyer_product,
                                parent,
                                false
                        );

        return new ProductViewHolder(view);
    }

    // =========================================================
    // BIND PRODUCT
    // =========================================================

    @Override
    public void onBindViewHolder(
            @NonNull ProductViewHolder holder,
            int position) {

        Map<String, Object> product =
                productList.get(position);

        // =====================================================
        // PRODUCT NAME
        // =====================================================

        Object nameObject =
                product.get("name");

        String productName =
                nameObject != null
                        ? String.valueOf(nameObject).trim()
                        : "Product";

        if (productName.isEmpty()) {
            productName = "Product";
        }

        holder.txtProductName.setText(
                productName
        );

        // =====================================================
        // CATEGORY
        // =====================================================

        Object categoryObject =
                product.get("categoryName");

        String categoryName =
                categoryObject != null
                        ? String.valueOf(categoryObject).trim()
                        : "Category";

        if (categoryName.isEmpty()) {
            categoryName = "Category";
        }

        holder.txtProductCategory.setText(
                categoryName
        );

        // =====================================================
        // PRICE
        // =====================================================

        double price =
                getDouble(
                        product.get("price")
                );

        holder.txtProductPrice.setText(
                formatPrice(price)
        );

        // =====================================================
        // PRODUCT IMAGE
        // =====================================================

        String imageUrl = "";

        // First try imageUrl
        Object imageUrlObject =
                product.get("imageUrl");

        if (imageUrlObject != null) {

            imageUrl =
                    String.valueOf(
                            imageUrlObject
                    ).trim();
        }

        // =====================================================
        // FALLBACK TO productImage
        // =====================================================

        if (imageUrl.isEmpty()) {

            Object productImageObject =
                    product.get("productImage");

            if (productImageObject != null) {

                imageUrl =
                        String.valueOf(
                                productImageObject
                        ).trim();
            }
        }

        // =====================================================
        // LOAD CLOUDINARY IMAGE
        // =====================================================

        if (!imageUrl.isEmpty()) {

            Glide.with(
                            holder.itemView.getContext()
                    )
                    .load(imageUrl)
                    .placeholder(
                            R.drawable.baseline_menu_24
                    )
                    .error(
                            R.drawable.baseline_menu_24
                    )
                    .centerCrop()
                    .into(
                            holder.productImage
                    );

        } else {

            holder.productImage.setImageResource(
                    R.drawable.baseline_menu_24
            );
        }

        // =====================================================
        // UPLOAD TIME
        // =====================================================

        holder.txtProductTime.setText(
                getProductUploadTime(
                        product.get("createdAt")
                )
        );

        // =====================================================
        // PRODUCT CLICK
        // =====================================================

        final Map<String, Object> clickedProduct =
                product;

        holder.itemView.setClickable(true);

        holder.itemView.setFocusable(false);

        holder.itemView.setOnClickListener(
                v -> {

                    if (listener == null) {
                        return;
                    }

                    // Blink is handled ONLY
                    // in BuyerHomeFragment

                    listener.onProductClick(
                            clickedProduct
                    );
                }
        );
    }

    // =========================================================
    // DOUBLE
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
                        String.valueOf(value)
                );

            } catch (Exception ignored) {
            }
        }

        return 0;
    }

    // =========================================================
    // FORMAT PRICE
    // =========================================================

    private String formatPrice(
            double price) {

        if (price == Math.floor(price)) {

            return "Rs " + (long) price;
        }

        return "Rs "
                + String.format(
                Locale.getDefault(),
                "%.2f",
                price
        );
    }

    // =========================================================
    // PRODUCT TIME
    // =========================================================

    private String getProductUploadTime(
            Object createdAtObject) {

        if (createdAtObject == null) {

            return "Recently added";
        }

        long createdAtMillis = 0;

        // =====================================================
        // FIREBASE TIMESTAMP
        // =====================================================

        if (createdAtObject instanceof Timestamp) {

            Timestamp timestamp =
                    (Timestamp) createdAtObject;

            createdAtMillis =
                    timestamp.toDate().getTime();

        }

        // =====================================================
        // NUMBER
        // =====================================================

        else if (createdAtObject instanceof Number) {

            createdAtMillis =
                    ((Number) createdAtObject)
                            .longValue();

        }

        // =====================================================
        // STRING
        // =====================================================

        else if (createdAtObject instanceof String) {

            try {

                createdAtMillis =
                        Long.parseLong(
                                (String) createdAtObject
                        );

            } catch (Exception e) {

                return "Recently added";
            }
        }

        // =====================================================
        // INVALID DATE
        // =====================================================

        if (createdAtMillis <= 0) {

            return "Recently added";
        }

        long difference =
                System.currentTimeMillis()
                        - createdAtMillis;

        if (difference < 0) {

            difference = 0;
        }

        // =====================================================
        // SECONDS
        // =====================================================

        long seconds =
                TimeUnit.MILLISECONDS.toSeconds(
                        difference
                );

        if (seconds < 60) {

            if (seconds <= 0) {

                return "Just now";
            }

            return seconds + " sec ago";
        }

        // =====================================================
        // MINUTES
        // =====================================================

        long minutes =
                TimeUnit.MILLISECONDS.toMinutes(
                        difference
                );

        if (minutes < 60) {

            return minutes + " min ago";
        }

        // =====================================================
        // HOURS
        // =====================================================

        long hours =
                TimeUnit.MILLISECONDS.toHours(
                        difference
                );

        if (hours < 24) {

            return hours + " hr ago";
        }

        // =====================================================
        // DAYS
        // =====================================================

        long days =
                TimeUnit.MILLISECONDS.toDays(
                        difference
                );

        if (days <= 3) {

            return days
                    + " day"
                    + (days == 1 ? "" : "s")
                    + " ago";
        }

        // =====================================================
        // DATE
        // =====================================================

        SimpleDateFormat dateFormat =
                new SimpleDateFormat(
                        "dd MMM yyyy",
                        Locale.getDefault()
                );

        return dateFormat.format(
                new Date(createdAtMillis)
        );
    }

    // =========================================================
    // ITEM COUNT
    // =========================================================

    @Override
    public int getItemCount() {

        if (productList == null) {

            return 0;
        }

        return productList.size();
    }

    // =========================================================
    // STOP TIMER
    // =========================================================

    public void stopTimeUpdates() {

        timeHandler.removeCallbacks(
                timeUpdater
        );
    }

    // =========================================================
    // VIEW HOLDER
    // =========================================================

    public static class ProductViewHolder
            extends RecyclerView.ViewHolder {

        ImageView productImage;

        TextView txtProductName;
        TextView txtProductCategory;
        TextView txtProductPrice;
        TextView txtProductTime;

        public ProductViewHolder(
                @NonNull View itemView) {

            super(itemView);

            // =================================================
            // PRODUCT IMAGE
            // =================================================

            productImage =
                    itemView.findViewById(
                            R.id.productImage
                    );

            // =================================================
            // TEXT FIELDS
            // =================================================

            txtProductName =
                    itemView.findViewById(
                            R.id.txtProductName
                    );

            txtProductCategory =
                    itemView.findViewById(
                            R.id.txtProductCategory
                    );

            txtProductPrice =
                    itemView.findViewById(
                            R.id.txtProductPrice
                    );

            txtProductTime =
                    itemView.findViewById(
                            R.id.txtProductTime
                    );
        }
    }
}