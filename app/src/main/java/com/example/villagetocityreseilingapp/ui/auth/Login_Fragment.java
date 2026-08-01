package com.example.villagetocityreseilingapp.ui.auth;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.villagetocityreseilingapp.R;
import com.example.villagetocityreseilingapp.activity.buyer_MainActivity;

public class Login_Fragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_buyer_login, container, false);

        // Role check karke title change karo
        SharedPreferences prefs = requireActivity().getSharedPreferences("role", 0);
        String role = prefs.getString("user_role", "buyer");

        TextView txtRoleTitle = view.findViewById(R.id.txtRoleTitle);
        if (role.equals("seller")) {
            txtRoleTitle.setText("Seller Login");
        } else {
            txtRoleTitle.setText("Buyer Login");
        }

        // Login button
        AppCompatButton btnLogin = view.findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(v -> {
            if (role.equals("seller")) {
                Navigation.findNavController(v)
                        .navigate(R.id.action_login_to_verification);
            } else {
                Intent intent = new Intent(getActivity(), buyer_MainActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        });

        // Signup text
        TextView txtSignup = view.findViewById(R.id.txtSignup);
        txtSignup.setOnClickListener(v -> {
            Navigation.findNavController(v)
                    .navigate(R.id.action_login_to_signup);
        });

        // Forgot Password
        TextView txtForgot = view.findViewById(R.id.txtForgot);
        txtForgot.setOnClickListener(v -> {
            new AlertDialog.Builder(getContext())
                    .setTitle("Forgot Password")
                    .setMessage("Password reset email bheja jayega!")
                    .setPositiveButton("OK", null)
                    .show();
        });

        return view;
    }
}