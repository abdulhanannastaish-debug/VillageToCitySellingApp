package com.example.villagetocityreseilingapp.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.villagetocityreseilingapp.R;

public class SignUp_Fragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup, container, false);

        // Signup button
        AppCompatButton btnSignup = view.findViewById(R.id.btnSignup);
        btnSignup.setOnClickListener(v -> {
            Navigation.findNavController(v)
                    .navigate(R.id.action_signup_to_login);
        });

        // Google button
        AppCompatButton btnGoogle = view.findViewById(R.id.btnGoogle);
        btnGoogle.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Google Sign Up coming soon!", Toast.LENGTH_SHORT).show();
        });

        // Already have account
        TextView txtLogin = view.findViewById(R.id.txtLogin);

        return view;
    }}