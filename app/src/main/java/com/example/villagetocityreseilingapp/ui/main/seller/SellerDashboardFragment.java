package com.example.villagetocityreseilingapp.ui.main.seller;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.villagetocityreseilingapp.R;

public class SellerDashboardFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public SellerDashboardFragment() {
        // Required empty public constructor
    }

    public static SellerDashboardFragment newInstance(String param1, String param2) {
        SellerDashboardFragment fragment = new SellerDashboardFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_seller_dashboard, container, false);

        // Seller Profile Click
        LinearLayout layoutProfile = view.findViewById(R.id.layoutRestaurantProfile);
        layoutProfile.setOnClickListener(v -> {
            loadFragment(new SellerProfileFragment());
        });

        // Manage Product Click
        LinearLayout layoutManageMenu = view.findViewById(R.id.layoutManageMenu);
        layoutManageMenu.setOnClickListener(v -> {
            loadFragment(new SellerProductFragment());
        });

        // Seller Wallet Click
        LinearLayout layoutWallet = view.findViewById(R.id.layoutSellerWallet);
        layoutWallet.setOnClickListener(v -> {
            loadFragment(new SellerWalletFragment());
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