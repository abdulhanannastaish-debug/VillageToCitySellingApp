package com.example.villagetocityreseilingapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.villagetocityreseilingapp.R;
import com.example.villagetocityreseilingapp.ui.main.seller.SellerEditProductFragment;

import java.util.List;
import java.util.Map;

public class ProductAdapter
        extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private final List<Map<String, Object>> productList;
    private final FragmentActivity activity;

    public ProductAdapter(
            FragmentActivity activity,
            List<Map<String, Object>> productList) {

        this.activity = activity;
        this.productList = productList;
    }

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

    @Override
    public void onBindViewHolder(
            @NonNull ProductViewHolder holder,
            int position) {

        Map<String, Object> product =
                productList.get(position);

        String productId =
                String.valueOf(product.get("productId"));

        String name =
                String.valueOf(product.get("name"));

        Object priceValue = product.get("price");

        String price;

        if (priceValue instanceof Number) {
            double priceNumber = ((Number) priceValue).doubleValue();

            if (priceNumber == Math.floor(priceNumber)) {
                price = String.valueOf((long) priceNumber);
            } else {
                price = String.valueOf(priceNumber);
            }
        } else {
            price = String.valueOf(priceValue);
        }

        String quantity =
                String.valueOf(product.get("quantity"));

        String description =
                String.valueOf(product.get("description"));

        String status =
                String.valueOf(product.get("status"));


        holder.txtProductName.setText(name);

        holder.txtProductPrice.setText(
                "Price: Rs. " + price
        );

        holder.txtProductQuantity.setText(
                "Quantity: " + quantity
        );

        holder.txtProductDescription.setText(
                description
        );

        holder.txtProductStatus.setText(
                status
        );


        // ================= EDIT PRODUCT =================

        holder.btnEditProduct.setOnClickListener(v -> {

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
        });
    }


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

        Button btnEditProduct;


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

            btnEditProduct =
                    itemView.findViewById(
                            R.id.btnEditProduct
                    );
        }
    }
}