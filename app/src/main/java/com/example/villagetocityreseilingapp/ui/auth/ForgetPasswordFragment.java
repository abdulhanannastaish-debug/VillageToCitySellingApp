package com.example.villagetocityreseilingapp.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.villagetocityreseilingapp.R;
import com.google.firebase.auth.FirebaseAuth;

public class ForgetPasswordFragment extends Fragment {

    private EditText etEmail;
    private AppCompatButton btnSendLink;
    private TextView txtBackLogin;

    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_forget_password,
                container,
                false
        );

        // ================= FIREBASE =================

        mAuth = FirebaseAuth.getInstance();

        // ================= XML IDs =================

        etEmail = view.findViewById(R.id.etEmail);
        btnSendLink = view.findViewById(R.id.btnSendLink);
        txtBackLogin = view.findViewById(R.id.txtBackLogin);

        // ================= SEND RESET LINK =================

        btnSendLink.setOnClickListener(v -> {

            String email = etEmail.getText().toString().trim();

            // Email validation
            if (TextUtils.isEmpty(email)) {
                etEmail.setError("Enter your email");
                etEmail.requestFocus();
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS
                    .matcher(email)
                    .matches()) {

                etEmail.setError("Enter a valid email address");
                etEmail.requestFocus();
                return;
            }

            // Disable button while sending
            btnSendLink.setEnabled(false);

            mAuth.sendPasswordResetEmail(email)
                    .addOnSuccessListener(unused -> {

                        Toast.makeText(
                                getContext(),
                                "Password reset link has been sent to your email.",
                                Toast.LENGTH_LONG
                        ).show();

                        btnSendLink.setEnabled(true);
                    })
                    .addOnFailureListener(e -> {

                        Toast.makeText(
                                getContext(),
                                "Failed: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();

                        btnSendLink.setEnabled(true);
                    });
        });

        // ================= BACK TO LOGIN =================

        txtBackLogin.setOnClickListener(v -> {

            Navigation.findNavController(v)
                    .navigate(R.id.action_forget_password_to_login);
        });

        return view;
    }
}