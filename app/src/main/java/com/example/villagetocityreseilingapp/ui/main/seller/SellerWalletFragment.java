package com.example.villagetocityreseilingapp.ui.main.seller;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.villagetocityreseilingapp.R;

public class SellerWalletFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_seller_wallet, container, false);

        // Payment Account Card Click
        CardView paymentAccountCard = view.findViewById(R.id.paymentAccountCard);
        paymentAccountCard.setOnClickListener(v -> {
            loadFragment(new SellerPaymentAccountFragment());
        });

        // View All Transactions Click
        TextView txtViewAllTransactions = view.findViewById(R.id.txtViewAllTransactions);
        txtViewAllTransactions.setOnClickListener(v -> {
            loadFragment(new SellerTransactionsFragment());
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