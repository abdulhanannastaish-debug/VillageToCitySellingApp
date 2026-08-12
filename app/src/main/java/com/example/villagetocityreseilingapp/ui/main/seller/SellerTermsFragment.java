package com.example.villagetocityreseilingapp.ui.main.seller;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.villagetocityreseilingapp.R;

public class SellerTermsFragment extends Fragment {

    public SellerTermsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_seller_terms,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        // ================= BACK BUTTON =================

        View btnTermsBack =
                view.findViewById(R.id.btnTermsBack);

        btnTermsBack.setOnClickListener(v -> {

            requireActivity()
                    .getSupportFragmentManager()
                    .popBackStack();

        });
    }
}