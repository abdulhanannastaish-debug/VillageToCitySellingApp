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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.villagetocityreseilingapp.R;
import com.example.villagetocityreseilingapp.activity.buyer_MainActivity;
import com.example.villagetocityreseilingapp.activity.seller_MainActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

public class Login_Fragment extends Fragment {

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private GoogleSignInClient googleSignInClient;
    private static final int RC_SIGN_IN = 9001;
    private String role;

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_login,
                container,
                false
        );

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        GoogleSignInOptions gso =
                new GoogleSignInOptions.Builder(
                        GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);

        SharedPreferences prefs =
                requireActivity().getSharedPreferences("role", 0);

        role = prefs.getString("user_role", "buyer");

        TextView txtRoleTitle = view.findViewById(R.id.txtRoleTitle);

        if ("seller".equals(role)) {
            txtRoleTitle.setText("Seller Login");
        } else {
            txtRoleTitle.setText("Buyer Login");
        }

        EditText etEmail = view.findViewById(R.id.etEmail);
        EditText etPassword = view.findViewById(R.id.etPassword);

        // =====================================================
        // PASSWORD VISIBILITY
        // =====================================================

        etPassword.setOnTouchListener((v, event) -> {

            if (event.getAction() == android.view.MotionEvent.ACTION_UP) {

                if (etPassword.getCompoundDrawables()[2] != null
                        && event.getX() >= (etPassword.getWidth()
                        - etPassword.getPaddingEnd()
                        - etPassword.getCompoundDrawables()[2].getBounds().width())) {

                    if (etPassword.getTransformationMethod() == null) {
                        etPassword.setTransformationMethod(
                                android.text.method.PasswordTransformationMethod.getInstance());
                    } else {
                        etPassword.setTransformationMethod(
                                android.text.method.HideReturnsTransformationMethod.getInstance());
                    }

                    etPassword.setSelection(etPassword.getText().length());
                    v.performClick();
                    return true;
                }
            }
            return false;
        });

        // =====================================================
        // LOGIN BUTTON
        // =====================================================

        AppCompatButton btnLogin = view.findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> {

            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                etEmail.setError("Enter your email");
                etEmail.requestFocus();
                return;
            }

            if (TextUtils.isEmpty(password)) {
                etPassword.setError("Enter your password");
                etPassword.requestFocus();
                return;
            }

            btnLogin.setEnabled(false);
            btnLogin.setText("Logging in...");

            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(requireActivity(), task -> {

                        if (!task.isSuccessful()) {

                            btnLogin.setEnabled(true);
                            btnLogin.setText("Login");

                            String errorMessage = task.getException() != null
                                    ? task.getException().getMessage()
                                    : "Invalid email or password";

                            Toast.makeText(getContext(),
                                    "Login failed: " + errorMessage,
                                    Toast.LENGTH_LONG).show();

                            return;
                        }

                        if ("seller".equals(role)) {
                            checkSellerStatusAndNavigate(v, btnLogin);
                        } else {
                            openBuyerMainActivity();
                        }
                    });
        });

        // =====================================================
        // GOOGLE LOGIN
        // =====================================================

        AppCompatButton btnGoogleLogin = view.findViewById(R.id.btnGoogleLogin);

        btnGoogleLogin.setOnClickListener(v -> {
            btnGoogleLogin.setEnabled(false);
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });

        // =====================================================
        // SIGN UP
        // =====================================================

        TextView txtSignup = view.findViewById(R.id.txtRegister);

        txtSignup.setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_login_to_signup));

        // =====================================================
        // FORGOT PASSWORD
        // =====================================================

        TextView txtForgot = view.findViewById(R.id.txtForgot);

        txtForgot.setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_login_to_forgot_password));

        return view;
    }

    // =====================================================
    // CHECK SELLER STATUS
    // =====================================================

    private void checkSellerStatusAndNavigate(
            View clickedView,
            AppCompatButton btnLogin) {

        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            btnLogin.setEnabled(true);
            btnLogin.setText("Login");
            Toast.makeText(getContext(),
                    "User session not found. Please login again.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        String uid = currentUser.getUid();

        db.collection("sellers")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (!isAdded()) return;

                    btnLogin.setEnabled(true);
                    btnLogin.setText("Login");

                    if (!documentSnapshot.exists()) {
                        navigateToVerification(clickedView);
                        return;
                    }

                    String status = documentSnapshot.getString("status");
                    if (status == null) status = "pending";
                    status = status.trim().toLowerCase();

                    // =========================================
                    // VERIFIED = CHOOSE LANGUAGE
                    // =========================================

                    if ("verified".equals(status)) {
                        navigateToChooseLanguage(clickedView);

                    } else {
                        navigateToVerification(clickedView);
                    }
                })
                .addOnFailureListener(e -> {

                    if (!isAdded()) return;

                    btnLogin.setEnabled(true);
                    btnLogin.setText("Login");

                    Toast.makeText(getContext(),
                            "Unable to check seller verification: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    // =====================================================
    // NAVIGATE TO VERIFICATION
    // =====================================================

    private void navigateToVerification(View view) {

        if (!isAdded()) return;

        Navigation.findNavController(view)
                .navigate(R.id.action_login_to_verification);
    }

    // =====================================================
    // NAVIGATE TO CHOOSE LANGUAGE
    // =====================================================

    private void navigateToChooseLanguage(View view) {

        if (!isAdded()) return;

        Navigation.findNavController(view)
                .navigate(R.id.action_login_to_choose_language);
    }

    // =====================================================
    // GOOGLE LOGIN RESULT
    // =====================================================

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != RC_SIGN_IN) return;

        try {

            GoogleSignInAccount account =
                    GoogleSignIn.getSignedInAccountFromIntent(data)
                            .getResult(ApiException.class);

            if (account == null) {
                Toast.makeText(getContext(),
                        "Google account not selected",
                        Toast.LENGTH_LONG).show();
                return;
            }

            String idToken = account.getIdToken();

            if (idToken == null) {
                Toast.makeText(getContext(),
                        "Google ID Token is missing",
                        Toast.LENGTH_LONG).show();
                return;
            }

            AuthCredential credential =
                    GoogleAuthProvider.getCredential(idToken, null);

            auth.signInWithCredential(credential)
                    .addOnCompleteListener(requireActivity(), task -> {

                        if (!task.isSuccessful()) {

                            String errorMessage = task.getException() != null
                                    ? task.getException().getMessage()
                                    : "Google Login failed";

                            Toast.makeText(getContext(),
                                    "Google Login failed: " + errorMessage,
                                    Toast.LENGTH_LONG).show();

                            return;
                        }

                        if ("seller".equals(role)) {
                            checkSellerStatusAfterGoogleLogin();
                        } else {
                            openBuyerMainActivity();
                        }
                    });

        } catch (ApiException e) {
            Toast.makeText(getContext(),
                    "Google Sign-In failed: " + e.getStatusCode(),
                    Toast.LENGTH_LONG).show();
        }
    }

    // =====================================================
    // CHECK SELLER AFTER GOOGLE LOGIN
    // =====================================================

    private void checkSellerStatusAfterGoogleLogin() {

        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(getContext(),
                    "User session not found.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        String uid = currentUser.getUid();

        db.collection("sellers")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (!isAdded()) return;

                    if (!documentSnapshot.exists()) {
                        navigateToVerificationFromCurrentFragment();
                        return;
                    }

                    String status = documentSnapshot.getString("status");
                    if (status == null) status = "pending";
                    status = status.trim().toLowerCase();

                    if ("verified".equals(status)) {
                        navigateToChooseLanguageFromCurrentFragment();
                    } else {
                        navigateToVerificationFromCurrentFragment();
                    }
                })
                .addOnFailureListener(e -> {

                    if (!isAdded()) return;

                    Toast.makeText(getContext(),
                            "Unable to check seller verification: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    // =====================================================
    // NAVIGATE FROM CURRENT FRAGMENT
    // =====================================================

    private void navigateToVerificationFromCurrentFragment() {

        if (!isAdded()) return;

        Navigation.findNavController(requireView())
                .navigate(R.id.action_login_to_verification);
    }

    // =====================================================
    // NAVIGATE TO CHOOSE LANGUAGE FROM CURRENT FRAGMENT
    // =====================================================

    private void navigateToChooseLanguageFromCurrentFragment() {

        if (!isAdded()) return;

        Navigation.findNavController(requireView())
                .navigate(R.id.action_login_to_choose_language);
    }

    // =====================================================
    // OPEN BUYER
    // =====================================================

    private void openBuyerMainActivity() {

        if (!isAdded()) return;

        Intent intent = new Intent(requireActivity(), buyer_MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}