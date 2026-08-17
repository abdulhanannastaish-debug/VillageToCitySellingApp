package com.example.villagetocityreseilingapp.ui.main.seller;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.villagetocityreseilingapp.R;

public class SellerProductFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_seller_product, container, false);

        // Add Product Button Click
        Button btnAdd = view.findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(v -> {
            loadFragment(new SellerEditProductFragment());
        });

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