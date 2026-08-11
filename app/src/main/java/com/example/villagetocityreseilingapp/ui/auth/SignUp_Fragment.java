package com.example.villagetocityreseilingapp.ui.auth;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUp_Fragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private EditText etName;
    private EditText etEmail;
    private EditText etPhone;
    private EditText etPassword;
    private EditText etConfirmPassword;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_signup, container, false);

        // Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // ================= FIELDS =================

        etName = view.findViewById(R.id.etName);
        etEmail = view.findViewById(R.id.etEmail);
        etPhone = view.findViewById(R.id.etPhone);
        etPassword = view.findViewById(R.id.etPassword);
        etConfirmPassword = view.findViewById(R.id.etConfirmPassword);

        // ================= PASSWORD EYE =================

        setupPasswordVisibility(etPassword);
        setupPasswordVisibility(etConfirmPassword);

        // ================= REGISTER BUTTON =================

        AppCompatButton btnRegister = view.findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> {

            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            // ================= GET ROLE =================

            SharedPreferences prefs = requireActivity()
                    .getSharedPreferences("role", 0);

            String role = prefs.getString("user_role", "");

            // ================= VALIDATION =================

            if (TextUtils.isEmpty(name)) {
                etName.setError("Please enter your name");
                etName.requestFocus();
                return;
            }

            if (TextUtils.isEmpty(email)) {
                etEmail.setError("Please enter your email");
                etEmail.requestFocus();
                return;
            }

            if (TextUtils.isEmpty(phone)) {
                etPhone.setError("Please enter your phone number");
                etPhone.requestFocus();
                return;
            }

            if (TextUtils.isEmpty(password)) {
                etPassword.setError("Please enter a password");
                etPassword.requestFocus();
                return;
            }

            if (password.length() < 6) {
                etPassword.setError("Password must be at least 6 characters");
                etPassword.requestFocus();
                return;
            }

            if (TextUtils.isEmpty(confirmPassword)) {
                etConfirmPassword.setError("Please confirm your password");
                etConfirmPassword.requestFocus();
                return;
            }

            if (!password.equals(confirmPassword)) {
                etConfirmPassword.setError("Passwords do not match");
                etConfirmPassword.requestFocus();
                return;
            }

            if (TextUtils.isEmpty(role)) {
                Toast.makeText(
                        getContext(),
                        "Please select Buyer or Seller first",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            // ================= CREATE FIREBASE ACCOUNT =================

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(requireActivity(), task -> {

                        if (task.isSuccessful()) {

                            FirebaseUser firebaseUser = mAuth.getCurrentUser();

                            if (firebaseUser == null) {
                                Toast.makeText(
                                        getContext(),
                                        "Account creation failed",
                                        Toast.LENGTH_SHORT
                                ).show();
                                return;
                            }

                            String uid = firebaseUser.getUid();

                            // ================= USER DATA =================

                            Map<String, Object> user = new HashMap<>();

                            user.put("name", name);
                            user.put("email", email);
                            user.put("phone", phone);
                            user.put("role", role);
                            user.put("status", "active");
                            user.put("createdAt",
                                    FieldValue.serverTimestamp());

                            // ================= SAVE TO FIRESTORE =================

                            db.collection("users")
                                    .document(uid)
                                    .set(user)
                                    .addOnSuccessListener(unused -> {

                                        Toast.makeText(
                                                getContext(),
                                                "Account created successfully!",
                                                Toast.LENGTH_SHORT
                                        ).show();

                                        Navigation.findNavController(v)
                                                .navigate(
                                                        R.id.action_signup_to_login
                                                );
                                    })
                                    .addOnFailureListener(e -> {

                                        Toast.makeText(
                                                getContext(),
                                                "Account created, but data could not be saved: "
                                                        + e.getMessage(),
                                                Toast.LENGTH_LONG
                                        ).show();
                                    });

                        } else {

                            String errorMessage;

                            if (task.getException() != null) {
                                errorMessage =
                                        task.getException().getMessage();
                            } else {
                                errorMessage = "Registration failed";
                            }

                            Toast.makeText(
                                    getContext(),
                                    errorMessage,
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    });
        });

        // ================= GOOGLE BUTTON =================

        AppCompatButton btnGoogle = view.findViewById(R.id.btnGoogle);

        btnGoogle.setOnClickListener(v -> {

            Toast.makeText(
                    getContext(),
                    "Google Sign Up coming soon!",
                    Toast.LENGTH_SHORT
            ).show();
        });

        // ================= LOGIN TEXT =================

        TextView btnSignup = view.findViewById(R.id.btnSignup);

        btnSignup.setOnClickListener(v -> {

            Navigation.findNavController(v)
                    .navigate(R.id.action_signup_to_login);
        });

        return view;
    }


    // ================= PASSWORD VISIBILITY =================

    private void setupPasswordVisibility(EditText editText) {

        editText.setOnTouchListener((v, event) -> {

            if (event.getAction() == MotionEvent.ACTION_UP) {

                if (editText.getCompoundDrawables()[2] != null &&
                        event.getX() >=
                                (editText.getWidth()
                                        - editText.getPaddingEnd()
                                        - editText.getCompoundDrawables()[2]
                                        .getBounds().width())) {

                    if (editText.getTransformationMethod() == null) {

                        // Hide password
                        editText.setTransformationMethod(
                                PasswordTransformationMethod.getInstance()
                        );

                    } else {

                        // Show password
                        editText.setTransformationMethod(
                                HideReturnsTransformationMethod.getInstance()
                        );
                    }

                    editText.setSelection(
                            editText.getText().length()
                    );

                    // Accessibility warning fix
                    v.performClick();

                    return true;
                }
            }

            return false;
        });
    }
}