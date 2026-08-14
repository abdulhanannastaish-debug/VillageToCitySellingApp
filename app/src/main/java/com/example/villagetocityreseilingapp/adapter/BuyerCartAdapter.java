package com.example.villagetocityreseilingapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.example.villagetocityreseilingapp.R;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BuyerCartAdapter
        extends RecyclerView.Adapter<BuyerCartAdapter.CartViewHolder> {

    public interface OnCartActionListener {

        void onIncrease(Map<String, Object> item);

        void onDecrease(Map<String, Object> item);

        void onRemove(Map<String, Object> item);

        void onCheckout(Map<String, Object> item);
    }

    private final List<Map<String, Object>> cartList;

    private final OnCartActionListener listener;

    private static final double DELIVERY_CHARGES = 200;

    public BuyerCartAdapter(
            List<Map<String, Object>> cartList,
            OnCartActionListener listener) {

        this.cartList = cartList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view =
                LayoutInflater.from(parent.getContext())
                        .inflate(
                                R.layout.item_buyer_cart,
                                parent,
                                false
                        );

        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull CartViewHolder holder,
            int position) {

        Map<String, Object> item =
                cartList.get(position);

        String name =
                getStringValue(
                        item,
                        "name",
                        "Product"
                );

        String sellerName =
                getStringValue(
                        item,
                        "sellerName",
                        ""
                );

        if (sellerName.isEmpty()) {

            sellerName =
                    getStringValue(
                            item,
                            "sellerId",
                            "Seller"
                    );
        }

        double price =
                getDouble(
                        item.get("price")
                );

        int quantity =
                getInt(
                        item.get("quantity"),
                        1
                );

        holder.tvProductName.setText(name);

        holder.tvSeller.setText(
                "Seller: " + sellerName
        );

        holder.tvPrice.setText(
                formatPrice(price)
        );

        holder.tvQuantity.setText(
                String.valueOf(quantity)
        );

        double subtotal =
                price * quantity;

        double total =
                subtotal + DELIVERY_CHARGES;

        holder.tvSubtotal.setText(
                formatPrice(subtotal)
        );

        holder.tvDelivery.setText(
                formatPrice(DELIVERY_CHARGES)
        );

        holder.tvTotal.setText(
                formatPrice(total)
        );

        holder.btnIncrease.setOnClickListener(
                v -> listener.onIncrease(item)
        );

        holder.btnDecrease.setOnClickListener(
                v -> listener.onDecrease(item)
        );

        holder.btnRemove.setOnClickListener(
                v -> listener.onRemove(item)
        );

        holder.btnCheckout.setOnClickListener(
                v -> listener.onCheckout(item)
        );
    }

    @Override
    public int getItemCount() {
        return cartList.size();
    }

    // =========================================================
    // VIEW HOLDER
    // =========================================================

    static class CartViewHolder
            extends RecyclerView.ViewHolder {

        ImageView imgProduct;

        TextView tvProductName;
        TextView tvSeller;
        TextView tvPrice;
        TextView tvQuantity;

        TextView tvSubtotal;
        TextView tvDelivery;
        TextView tvTotal;

        TextView btnIncrease;
        TextView btnDecrease;

        AppCompatButton btnRemove;
        AppCompatButton btnCheckout;

        CartViewHolder(
                @NonNull View itemView) {

            super(itemView);

            imgProduct =
                    itemView.findViewById(
                            R.id.imgCartProduct
                    );

            tvProductName =
                    itemView.findViewById(
                            R.id.tvCartProductName
                    );

            tvSeller =
                    itemView.findViewById(
                            R.id.tvCartSeller
                    );

            tvPrice =
                    itemView.findViewById(
                            R.id.tvCartPrice
                    );

            tvQuantity =
                    itemView.findViewById(
                            R.id.tvCartQuantity
                    );

            tvSubtotal =
                    itemView.findViewById(
                            R.id.tvSubtotal
                    );

            tvDelivery =
                    itemView.findViewById(
                            R.id.tvDeliveryCharges
                    );

            tvTotal =
                    itemView.findViewById(
                            R.id.tvTotal
                    );

            btnIncrease =
                    itemView.findViewById(
                            R.id.btnIncrease
                    );

            btnDecrease =
                    itemView.findViewById(
                            R.id.btnDecrease
                    );

            btnRemove =
                    itemView.findViewById(
                            R.id.btnRemoveItem
                    );

            btnCheckout =
                    itemView.findViewById(
                            R.id.btnProceedCheckout
                    );
        }
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private static String getStringValue(
            Map<String, Object> data,
            String key,
            String defaultValue) {

        Object value =
                data.get(key);

        if (value == null) {
            return defaultValue;
        }

        String result =
                String.valueOf(value).trim();

        return result.isEmpty()
                ? defaultValue
                : result;
    }

    private static double getDouble(
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

    private static int getInt(
            Object value,
            int defaultValue) {

        if (value instanceof Number) {

            int result =
                    ((Number) value).intValue();

            return result > 0
                    ? result
                    : defaultValue;
        }

        if (value != null) {

            try {

                int result =
                        Integer.parseInt(
                                String.valueOf(value)
                        );

                return result > 0
                        ? result
                        : defaultValue;

            } catch (Exception ignored) {
            }
        }

        return defaultValue;
    }

    private static String formatPrice(
            double price) {

        if (price == Math.floor(price)) {

            return "Rs " + (long) price;
        }

        return "Rs " +
                String.format(
                        Locale.getDefault(),
                        "%.2f",
                        price
                );
    }
}