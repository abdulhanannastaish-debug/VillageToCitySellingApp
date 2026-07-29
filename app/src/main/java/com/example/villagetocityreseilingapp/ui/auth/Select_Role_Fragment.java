package com.example.villagetocityreseilingapp.ui.auth;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.villagetocityreseilingapp.R;

public class Select_Role_Fragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_select_role_, container, false);

        // Buyer card click
        CardView cardBuyer = view.findViewById(R.id.cardBuyer);
        cardBuyer.setOnClickListener(v -> {
            SharedPreferences.Editor editor = requireActivity()
                    .getSharedPreferences("role", 0).edit();
            editor.putString("user_role", "buyer");
            editor.apply();
            Navigation.findNavController(v).navigate(R.id.action_role_to_login);
        });

        // Seller card click
        CardView cardSeller = view.findViewById(R.id.cardSeller);
        cardSeller.setOnClickListener(v -> {
            SharedPreferences.Editor editor = requireActivity()
                    .getSharedPreferences("role", 0).edit();
            editor.putString("user_role", "seller");
            editor.apply();
            Navigation.findNavController(v).navigate(R.id.action_role_to_login);
        });

        return view;
    }
}