package com.example.villagetocityreseilingapp.ui.main.seller;

import android.os.Bundle;

import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.villagetocityreseilingapp.R;

public class SellerOrderFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_seller_orders, container, false);

        AppCompatButton btnOrderDetails = view.findViewById(R.id.btnOrderDetails);
        if (btnOrderDetails != null) {
            btnOrderDetails.setOnClickListener(v -> {
                loadFragment(new SellerOrderDetailFragment());
            });
        }

        AppCompatButton btnOrderStatus = view.findViewById(R.id.btnOrderStatus);
        if (btnOrderStatus != null) {
            btnOrderStatus.setOnClickListener(v -> {
                loadFragment(new SellerOrderStatusFragment());
            });
        }

        return view;
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = requireActivity()
                .getSupportFragmentManager()
                .beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}