package com.example.villagetocityreseilingapp.ui.auth;

import android.content.Intent;
import android.content.SharedPreferences;
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
import com.example.villagetocityreseilingapp.activity.buyer_MainActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

public class Login_Fragment extends Fragment {

    private FirebaseAuth auth;

    // Google Sign-In
    private GoogleSignInClient googleSignInClient;
    private static final int RC_SIGN_IN = 9001;

    // User Role
    private String role;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_login, container, false);

        // ================= FIREBASE AUTHENTICATION =================

        auth = FirebaseAuth.getInstance();


        // ================= GOOGLE SIGN-IN =================

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(
                requireActivity(),
                gso
        );


        // ================= ROLE CHECK =================

        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("role", 0);

        role = prefs.getString("user_role", "buyer");

        TextView txtRoleTitle = view.findViewById(R.id.txtRoleTitle);

        if (role.equals("seller")) {
            txtRoleTitle.setText("Seller Login");
        } else {
            txtRoleTitle.setText("Buyer Login");
        }


        // ================= EMAIL & PASSWORD =================

        EditText etEmail = view.findViewById(R.id.etEmail);
        EditText etPassword = view.findViewById(R.id.etPassword);


        // ================= PASSWORD VISIBILITY =================

        etPassword.setOnTouchListener((v, event) -> {

            if (event.getAction() == android.view.MotionEvent.ACTION_UP) {

                if (etPassword.getCompoundDrawables()[2] != null &&
                        event.getX() >=
                                (etPassword.getWidth()
                                        - etPassword.getPaddingEnd()
                                        - etPassword.getCompoundDrawables()[2]
                                        .getBounds().width())) {

                    if (etPassword.getTransformationMethod() == null) {

                        // Hide password
                        etPassword.setTransformationMethod(
                                android.text.method.PasswordTransformationMethod
                                        .getInstance()
                        );

                    } else {

                        // Show password
                        etPassword.setTransformationMethod(
                                android.text.method.HideReturnsTransformationMethod
                                        .getInstance()
                        );
                    }

                    etPassword.setSelection(
                            etPassword.getText().length()
                    );

                    v.performClick();

                    return true;
                }
            }

            return false;
        });


        // ================= LOGIN BUTTON =================

        AppCompatButton btnLogin = view.findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> {

            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            // Empty email
            if (TextUtils.isEmpty(email)) {
                etEmail.setError("Enter your email");
                etEmail.requestFocus();
                return;
            }

            // Empty password
            if (TextUtils.isEmpty(password)) {
                etPassword.setError("Enter your password");
                etPassword.requestFocus();
                return;
            }

            // Firebase Authentication Login
            btnLogin.setEnabled(false);

            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(requireActivity(), task -> {

                        btnLogin.setEnabled(true);

                        if (task.isSuccessful()) {

                            Toast.makeText(
                                    getContext(),
                                    "Login successful",
                                    Toast.LENGTH_SHORT
                            ).show();

                            if (role.equals("seller")) {

                                Navigation.findNavController(v)
                                        .navigate(
                                                R.id.action_login_to_verification
                                        );

                            } else {

                                Intent intent = new Intent(
                                        getActivity(),
                                        buyer_MainActivity.class
                                );

                                startActivity(intent);

                                requireActivity().finish();
                            }

                        } else {

                            String errorMessage;

                            if (task.getException() != null) {
                                errorMessage =
                                        task.getException().getMessage();
                            } else {
                                errorMessage =
                                        "Invalid email or password";
                            }

                            Toast.makeText(
                                    getContext(),
                                    "Login failed: " + errorMessage,
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    });
        });


        // ================= GOOGLE LOGIN BUTTON =================

        AppCompatButton btnGoogleLogin =
                view.findViewById(R.id.btnGoogleLogin);

        btnGoogleLogin.setOnClickListener(v -> {

            btnGoogleLogin.setEnabled(false);

            Intent signInIntent =
                    googleSignInClient.getSignInIntent();

            startActivityForResult(
                    signInIntent,
                    RC_SIGN_IN
            );
        });


        // ================= SIGNUP =================

        TextView txtSignup =
                view.findViewById(R.id.txtRegister);

        txtSignup.setOnClickListener(v -> {

            Navigation.findNavController(v)
                    .navigate(
                            R.id.action_login_to_signup
                    );
        });


        // ================= FORGOT PASSWORD =================

        TextView txtForgot =
                view.findViewById(R.id.txtForgot);

        txtForgot.setOnClickListener(v -> {

            Navigation.findNavController(v)
                    .navigate(
                            R.id.action_login_to_forgot_password
                    );
        });


        return view;
    }


    // ================= GOOGLE LOGIN RESULT =================

    @Override
    public void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data) {

        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

        if (requestCode != RC_SIGN_IN) {
            return;
        }

        try {

            GoogleSignInAccount account =
                    GoogleSignIn
                            .getSignedInAccountFromIntent(data)
                            .getResult(ApiException.class);

            if (account == null) {

                Toast.makeText(
                        getContext(),
                        "Google account not selected",
                        Toast.LENGTH_LONG
                ).show();

                return;
            }


            // Google ID Token
            String idToken = account.getIdToken();

            if (idToken == null) {

                Toast.makeText(
                        getContext(),
                        "Google ID Token is missing",
                        Toast.LENGTH_LONG
                ).show();

                return;
            }


            // Firebase Credential
            AuthCredential credential =
                    GoogleAuthProvider.getCredential(
                            idToken,
                            null
                    );


            // Firebase Login
            auth.signInWithCredential(credential)
                    .addOnCompleteListener(
                            requireActivity(),
                            task -> {

                                if (task.isSuccessful()) {

                                    Toast.makeText(
                                            getContext(),
                                            "Google Login successful",
                                            Toast.LENGTH_SHORT
                                    ).show();


                                    // ================= SELLER =================

                                    if (role.equals("seller")) {

                                        Navigation.findNavController(
                                                requireView()
                                        ).navigate(
                                                R.id.action_login_to_verification
                                        );


                                        // ================= BUYER =================

                                    } else {

                                        Intent intent =
                                                new Intent(
                                                        getActivity(),
                                                        buyer_MainActivity.class
                                                );

                                        startActivity(intent);

                                        requireActivity().finish();
                                    }


                                } else {

                                    String errorMessage;

                                    if (task.getException() != null) {

                                        errorMessage =
                                                task.getException()
                                                        .getMessage();

                                    } else {

                                        errorMessage =
                                                "Google Login failed";
                                    }

                                    Toast.makeText(
                                            getContext(),
                                            "Google Login failed: "
                                                    + errorMessage,
                                            Toast.LENGTH_LONG
                                    ).show();
                                }
                            }
                    );


        } catch (ApiException e) {

            Toast.makeText(
                    getContext(),
                    "Google Sign-In failed: "
                            + e.getStatusCode(),
                    Toast.LENGTH_LONG
            ).show();
        }
    }
}