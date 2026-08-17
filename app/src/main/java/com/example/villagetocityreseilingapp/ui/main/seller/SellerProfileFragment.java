package com.example.villagetocityreseilingapp.ui.main.seller;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.villagetocityreseilingapp.R;

public class SellerProfileFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_seller_profile, container, false);

        // Edit Profile Click
        TextView txtEditProfile = view.findViewById(R.id.txtEditProfile);
        txtEditProfile.setOnClickListener(v -> {
            loadFragment(new SellerEditProfileFragment());
        });

        // Logout Click
        TextView txtLogout = view.findViewById(R.id.txtLogout);
        txtLogout.setOnClickListener(v -> {
            SharedPreferences prefs = requireActivity().getSharedPreferences("role", 0);
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();

            Intent intent = new Intent(getActivity(), com.example.villagetocityreseilingapp.activity.AuthActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
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