package com.villagetocity.marketplace.ui.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.villagetocity.marketplace.R;
import com.villagetocity.marketplace.activity.buyer_MainActivity;
import com.villagetocity.marketplace.activity.seller_MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class Login_Fragment extends Fragment {

    // =========================================================
    // CONSTANTS
    // =========================================================

    private static final String PREF_NAME = "role";
    private static final String KEY_USER_ROLE = "user_role";

    private static final String ROLE_BUYER = "buyer";
    private static final String ROLE_SELLER = "seller";

    // =========================================================
    // FIREBASE
    // =========================================================

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    // =========================================================
    // SELECTED ROLE
    // =========================================================

    private String selectedRole;

    // =========================================================
    // CREATE VIEW
    // =========================================================

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_login,
                container,
                false
        );

        // =====================================================
        // FIREBASE
        // =====================================================

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // =====================================================
        // GET SELECTED ROLE
        // =====================================================

        loadSelectedRole();

        // =====================================================
        // ROLE TITLE
        // =====================================================

        TextView txtRoleTitle =
                view.findViewById(R.id.txtRoleTitle);

        if (ROLE_SELLER.equals(selectedRole)) {

            txtRoleTitle.setText("Seller Login");

        } else {

            txtRoleTitle.setText("Buyer Login");
        }

        // =====================================================
        // INPUT FIELDS
        // =====================================================

        EditText etEmail =
                view.findViewById(R.id.etEmail);

        EditText etPassword =
                view.findViewById(R.id.etPassword);

        // =====================================================
        // PASSWORD VISIBILITY
        // =====================================================

        setupPasswordVisibility(etPassword);

        // =====================================================
        // LOGIN BUTTON
        // =====================================================

        AppCompatButton btnLogin =
                view.findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> {

            String email =
                    etEmail.getText()
                            .toString()
                            .trim();

            String password =
                    etPassword.getText()
                            .toString()
                            .trim();

            // -------------------------------------------------
            // ROLE CHECK
            // -------------------------------------------------

            if (TextUtils.isEmpty(selectedRole)) {

                Toast.makeText(
                        getContext(),
                        "Please select Buyer or Seller first.",
                        Toast.LENGTH_LONG
                ).show();

                return;
            }

            // -------------------------------------------------
            // EMAIL
            // -------------------------------------------------

            if (TextUtils.isEmpty(email)) {

                etEmail.setError(
                        "Enter your email"
                );

                etEmail.requestFocus();

                return;
            }

            // -------------------------------------------------
            // PASSWORD
            // -------------------------------------------------

            if (TextUtils.isEmpty(password)) {

                etPassword.setError(
                        "Enter your password"
                );

                etPassword.requestFocus();

                return;
            }

            // -------------------------------------------------
            // DISABLE LOGIN
            // -------------------------------------------------

            btnLogin.setEnabled(false);
            btnLogin.setText("Logging in...");

            // =================================================
            // FIREBASE EMAIL/PASSWORD LOGIN
            // =================================================

            auth.signInWithEmailAndPassword(
                            email,
                            password
                    )
                    .addOnCompleteListener(
                            requireActivity(),
                            task -> {

                                if (!task.isSuccessful()) {

                                    resetLoginButton(
                                            btnLogin
                                    );

                                    String errorMessage =
                                            task.getException() != null
                                                    ? task.getException()
                                                    .getMessage()
                                                    : "Invalid email or password";

                                    Toast.makeText(
                                            getContext(),
                                            "Login failed: "
                                                    + errorMessage,
                                            Toast.LENGTH_LONG
                                    ).show();

                                    return;
                                }

                                // -----------------------------------------
                                // AUTH SUCCESS
                                // -----------------------------------------

                                checkFirestoreRoleAndNavigate(
                                        btnLogin
                                );
                            }
                    );
        });

        // =====================================================
        // SIGN UP
        // =====================================================

        TextView txtSignup =
                view.findViewById(
                        R.id.txtRegister
                );

        txtSignup.setOnClickListener(v ->
                Navigation
                        .findNavController(v)
                        .navigate(
                                R.id.action_login_to_signup
                        )
        );

        // =====================================================
        // FORGOT PASSWORD
        // =====================================================

        TextView txtForgot =
                view.findViewById(
                        R.id.txtForgot
                );

        txtForgot.setOnClickListener(v ->
                Navigation
                        .findNavController(v)
                        .navigate(
                                R.id.action_login_to_forgot_password
                        )
        );

        return view;
    }

    // =========================================================
    // LOAD SELECTED ROLE
    // =========================================================

    private void loadSelectedRole() {

        if (!isAdded()) {
            return;
        }

        SharedPreferences prefs =
                requireActivity()
                        .getSharedPreferences(
                                PREF_NAME,
                                0
                        );

        selectedRole =
                prefs.getString(
                        KEY_USER_ROLE,
                        null
                );

        if (selectedRole != null) {

            selectedRole =
                    selectedRole
                            .trim()
                            .toLowerCase();
        }

        // -----------------------------------------------------
        // ONLY ACCEPT VALID ROLES
        // -----------------------------------------------------

        if (!ROLE_BUYER.equals(selectedRole)
                && !ROLE_SELLER.equals(selectedRole)) {

            selectedRole = null;
        }
    }

    // =========================================================
    // CHECK FIRESTORE ROLE
    // =========================================================

    private void checkFirestoreRoleAndNavigate(
            AppCompatButton btnLogin) {

        if (!isAdded()) {
            return;
        }

        FirebaseUser currentUser =
                auth.getCurrentUser();

        if (currentUser == null) {

            resetLoginButton(btnLogin);

            Toast.makeText(
                    getContext(),
                    "User session not found. Please login again.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        String uid =
                currentUser.getUid();

        // =====================================================
        // READ USERS/{UID}
        // =====================================================

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(
                        documentSnapshot -> {

                            if (!isAdded()) {
                                return;
                            }

                            // =================================================
                            // USER DOCUMENT MISSING
                            // =================================================

                            if (!documentSnapshot.exists()) {

                                resetLoginButton(
                                        btnLogin
                                );

                                Toast.makeText(
                                        getContext(),
                                        "Account profile not found. Please sign up first.",
                                        Toast.LENGTH_LONG
                                ).show();

                                auth.signOut();

                                return;
                            }

                            // =================================================
                            // FIRESTORE ROLE
                            // =================================================

                            String firestoreRole =
                                    documentSnapshot.getString(
                                            "role"
                                    );

                            if (firestoreRole != null) {

                                firestoreRole =
                                        firestoreRole
                                                .trim()
                                                .toLowerCase();
                            }

                            // =================================================
                            // ROLE MISSING
                            // =================================================

                            if (TextUtils.isEmpty(
                                    firestoreRole
                            )) {

                                resetLoginButton(
                                        btnLogin
                                );

                                Toast.makeText(
                                        getContext(),
                                        "Account role is missing. Please contact admin.",
                                        Toast.LENGTH_LONG
                                ).show();

                                auth.signOut();

                                return;
                            }

                            // =================================================
                            // INVALID ROLE
                            // =================================================

                            if (!ROLE_BUYER.equals(
                                    firestoreRole
                            )
                                    && !ROLE_SELLER.equals(
                                    firestoreRole
                            )) {

                                resetLoginButton(
                                        btnLogin
                                );

                                Toast.makeText(
                                        getContext(),
                                        "Invalid account role. Please contact admin.",
                                        Toast.LENGTH_LONG
                                ).show();

                                auth.signOut();

                                return;
                            }

                            // =================================================
                            // SELECTED ROLE VS FIRESTORE ROLE
                            // =================================================

                            if (!firestoreRole.equals(
                                    selectedRole
                            )) {

                                resetLoginButton(
                                        btnLogin
                                );

                                Toast.makeText(
                                        getContext(),
                                        "This account is registered as "
                                                + capitalizeRole(
                                                firestoreRole
                                        )
                                                + ". Please select "
                                                + capitalizeRole(
                                                firestoreRole
                                        )
                                                + " role and login again.",
                                        Toast.LENGTH_LONG
                                ).show();

                                auth.signOut();

                                return;
                            }

                            // =================================================
                            // ROLE MATCHED
                            // =================================================

                            saveRoleLocally(
                                    firestoreRole
                            );

                            resetLoginButton(
                                    btnLogin
                            );

                            // =================================================
                            // BUYER
                            // =================================================

                            if (ROLE_BUYER.equals(
                                    firestoreRole
                            )) {

                                openBuyerMainActivity();

                                return;
                            }

                            // =================================================
                            // SELLER
                            // =================================================

                            if (ROLE_SELLER.equals(
                                    firestoreRole
                            )) {

                                // Seller Verification SKIPPED
                                // Directly open Seller Main Activity

                                openSellerMainActivity();
                            }
                        }
                )
                .addOnFailureListener(
                        e -> {

                            if (!isAdded()) {
                                return;
                            }

                            resetLoginButton(
                                    btnLogin
                            );

                            Toast.makeText(
                                    getContext(),
                                    "Unable to check account role: "
                                            + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }

    // =========================================================
    // OPEN BUYER MAIN ACTIVITY
    // =========================================================

    private void openBuyerMainActivity() {

        if (!isAdded()) {
            return;
        }

        Intent intent =
                new Intent(
                        requireActivity(),
                        buyer_MainActivity.class
                );

        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);

        requireActivity().finish();
    }

    // =========================================================
    // OPEN SELLER MAIN ACTIVITY
    // =========================================================

    private void openSellerMainActivity() {

        if (!isAdded()) {
            return;
        }

        Intent intent =
                new Intent(
                        requireActivity(),
                        seller_MainActivity.class
                );

        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);

        requireActivity().finish();
    }

    // =========================================================
    // SAVE ROLE LOCALLY
    // =========================================================

    private void saveRoleLocally(
            String role) {

        if (!isAdded()) {
            return;
        }

        if (!ROLE_BUYER.equals(role)
                && !ROLE_SELLER.equals(role)) {

            return;
        }

        SharedPreferences prefs =
                requireActivity()
                        .getSharedPreferences(
                                PREF_NAME,
                                0
                        );

        prefs.edit()
                .putString(
                        KEY_USER_ROLE,
                        role
                )
                .apply();

        selectedRole = role;
    }

    // =========================================================
    // RESET LOGIN BUTTON
    // =========================================================

    private void resetLoginButton(
            AppCompatButton btnLogin) {

        if (btnLogin == null) {
            return;
        }

        btnLogin.setEnabled(true);
        btnLogin.setText("Login");
    }

    // =========================================================
    // PASSWORD VISIBILITY
    // =========================================================

    private void setupPasswordVisibility(
            EditText editText) {

        editText.setOnTouchListener(
                (v, event) -> {

                    if (event.getAction()
                            == MotionEvent.ACTION_UP) {

                        if (editText
                                .getCompoundDrawables()[2] != null
                                && event.getX() >= (
                                editText.getWidth()
                                        - editText.getPaddingEnd()
                                        - editText
                                        .getCompoundDrawables()[2]
                                        .getBounds()
                                        .width()
                        )) {

                            if (editText
                                    .getTransformationMethod()
                                    == null) {

                                editText.setTransformationMethod(
                                        android.text.method
                                                .PasswordTransformationMethod
                                                .getInstance()
                                );

                            } else {

                                editText.setTransformationMethod(
                                        android.text.method
                                                .HideReturnsTransformationMethod
                                                .getInstance()
                                );
                            }

                            editText.setSelection(
                                    editText
                                            .getText()
                                            .length()
                            );

                            v.performClick();

                            return true;
                        }
                    }

                    return false;
                }
        );
    }

    // =========================================================
    // CAPITALIZE ROLE
    // =========================================================

    private String capitalizeRole(
            String role) {

        if (TextUtils.isEmpty(role)) {
            return "";
        }

        if (role.length() == 1) {
            return role.toUpperCase();
        }

        return role.substring(0, 1).toUpperCase()
                + role.substring(1).toLowerCase();
    }

    // =========================================================
    // DESTROY VIEW
    // =========================================================

    @Override
    public void onDestroyView() {

        super.onDestroyView();
    }
}