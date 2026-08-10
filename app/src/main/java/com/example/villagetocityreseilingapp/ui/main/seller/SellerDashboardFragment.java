package com.example.villagetocityreseilingapp.ui.main.seller;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.villagetocityreseilingapp.R;

public class SellerDashboardFragment extends Fragment {

    // Dashboard Quick Access
    private LinearLayout layoutRestaurantProfile;
    private LinearLayout layoutManageMenu;
    private LinearLayout layoutSellerWallet;

    public SellerDashboardFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_seller_dashboard,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        // ================= FIND VIEWS =================

        layoutRestaurantProfile =
                view.findViewById(R.id.layoutRestaurantProfile);

        layoutManageMenu =
                view.findViewById(R.id.layoutManageMenu);

        layoutSellerWallet =
                view.findViewById(R.id.layoutSellerWallet);


        // ================= SELLER PROFILE =================

        layoutRestaurantProfile.setOnClickListener(v -> {

            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(
                            R.id.fragment_container,
                            new SellerProfileFragment()
                    )
                    .addToBackStack(null)
                    .commit();

        });


        // ================= MANAGE PRODUCT =================

        layoutManageMenu.setOnClickListener(v -> {

            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(
                            R.id.fragment_container,
                            new SellerProductFragment()
                    )
                    .addToBackStack(null)
                    .commit();

        });


        // ================= SELLER WALLET =================

        layoutSellerWallet.setOnClickListener(v -> {

            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(
                            R.id.fragment_container,
                            new SellerWalletFragment()
                    )
                    .addToBackStack(null)
                    .commit();

        });

    }
}