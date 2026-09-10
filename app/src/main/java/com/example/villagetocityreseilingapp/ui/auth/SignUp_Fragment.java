package com.example.villagetocityreseilingapp.ui.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.villagetocityreseilingapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
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

    private ImageView txtSignup;
    private Uri selectedImageUri;

    // ================= CLOUDINARY =================

    private static final String CLOUD_NAME = "cvhzteif";
    private static final String UPLOAD_PRESET = "rural_reach_upload";
    private static final String CLOUDINARY_FOLDER = "rural_reach/profiles";

    // ================= IMAGE PICKER =================

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {

                        if (uri != null) {

                            selectedImageUri = uri;

                            // Show selected image
                            txtSignup.setImageURI(uri);
                        }
                    }
            );

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_signup,
                container,
                false
        );

        // ================= FIREBASE =================

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // ================= FIELDS =================

        etName = view.findViewById(R.id.etName);
        etEmail = view.findViewById(R.id.etEmail);
        etPhone = view.findViewById(R.id.etPhone);
        etPassword = view.findViewById(R.id.etPassword);
        etConfirmPassword = view.findViewById(R.id.etConfirmPassword);

        // ================= PROFILE IMAGE =================

        txtSignup = view.findViewById(R.id.txtSignup);

        AppCompatButton btnChooseImage =
                view.findViewById(R.id.btnChooseImage);

        btnChooseImage.setOnClickListener(v -> {

            imagePickerLauncher.launch("image/*");

        });

        // ================= PASSWORD EYE =================

        setupPasswordVisibility(etPassword);
        setupPasswordVisibility(etConfirmPassword);

        // ================= REGISTER BUTTON =================

        AppCompatButton btnRegister =
                view.findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> {

            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword =
                    etConfirmPassword.getText().toString().trim();

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

                etPassword.setError(
                        "Password must be at least 6 characters"
                );

                etPassword.requestFocus();
                return;
            }

            if (TextUtils.isEmpty(confirmPassword)) {

                etConfirmPassword.setError(
                        "Please confirm your password"
                );

                etConfirmPassword.requestFocus();
                return;
            }

            if (!password.equals(confirmPassword)) {

                etConfirmPassword.setError(
                        "Passwords do not match"
                );

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

            btnRegister.setEnabled(false);

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(requireActivity(), task -> {

                        if (task.isSuccessful()) {

                            FirebaseUser firebaseUser =
                                    mAuth.getCurrentUser();

                            if (firebaseUser == null) {

                                btnRegister.setEnabled(true);

                                Toast.makeText(
                                        getContext(),
                                        "Account creation failed",
                                        Toast.LENGTH_SHORT
                                ).show();

                                return;
                            }

                            String uid = firebaseUser.getUid();

                            // ================= NO IMAGE =================

                            if (selectedImageUri == null) {

                                saveUserData(
                                        uid,
                                        name,
                                        email,
                                        phone,
                                        role,
                                        ""
                                );

                            } else {

                                // ================= UPLOAD IMAGE =================

                                Toast.makeText(
                                        getContext(),
                                        "Uploading profile picture...",
                                        Toast.LENGTH_SHORT
                                ).show();

                                uploadImageToCloudinary(
                                        selectedImageUri,
                                        uid,
                                        name,
                                        email,
                                        phone,
                                        role,
                                        btnRegister
                                );
                            }

                        } else {

                            btnRegister.setEnabled(true);

                            String errorMessage;

                            if (task.getException() != null) {

                                errorMessage =
                                        task.getException().getMessage();

                            } else {

                                errorMessage =
                                        "Registration failed";
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

        AppCompatButton btnGoogle =
                view.findViewById(R.id.btnGoogle);

        btnGoogle.setOnClickListener(v -> {

            Toast.makeText(
                    getContext(),
                    "Google Sign Up coming soon!",
                    Toast.LENGTH_SHORT
            ).show();
        });

        // ================= LOGIN TEXT =================

        TextView btnSignup =
                view.findViewById(R.id.btnSignup);

        btnSignup.setOnClickListener(v -> {

            Navigation.findNavController(v)
                    .navigate(R.id.action_signup_to_login);
        });

        return view;
    }


    // ============================================================
    // CLOUDINARY IMAGE UPLOAD
    // ============================================================

    private void uploadImageToCloudinary(
            Uri imageUri,
            String uid,
            String name,
            String email,
            String phone,
            String role,
            AppCompatButton btnRegister) {

        new Thread(() -> {

            HttpURLConnection connection = null;

            try {

                URL url = new URL(
                        "https://api.cloudinary.com/v1_1/"
                                + CLOUD_NAME
                                + "/image/upload"
                );

                connection =
                        (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setDoInput(true);

                connection.setRequestProperty(
                        "Content-Type",
                        "application/x-www-form-urlencoded"
                );

                String imageData =
                        convertImageToBase64(imageUri);

                String postData =
                        "file=data:image/jpeg;base64,"
                                + URLEncoder.encode(
                                imageData,
                                "UTF-8"
                        )
                                + "&upload_preset="
                                + URLEncoder.encode(
                                UPLOAD_PRESET,
                                "UTF-8"
                        )
                                + "&folder="
                                + URLEncoder.encode(
                                CLOUDINARY_FOLDER,
                                "UTF-8"
                        );

                OutputStream outputStream =
                        connection.getOutputStream();

                outputStream.write(
                        postData.getBytes("UTF-8")
                );

                outputStream.flush();
                outputStream.close();

                int responseCode =
                        connection.getResponseCode();

                InputStream inputStream;

                if (responseCode >= 200 &&
                        responseCode < 300) {

                    inputStream =
                            connection.getInputStream();

                } else {

                    inputStream =
                            connection.getErrorStream();
                }

                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        inputStream
                                )
                        );

                StringBuilder response =
                        new StringBuilder();

                String line;

                while ((line = reader.readLine()) != null) {

                    response.append(line);
                }

                reader.close();

                if (responseCode >= 200 &&
                        responseCode < 300) {

                    JSONObject json =
                            new JSONObject(
                                    response.toString()
                            );

                    String imageUrl =
                            json.getString("secure_url");

                    requireActivity().runOnUiThread(() -> {

                        saveUserData(
                                uid,
                                name,
                                email,
                                phone,
                                role,
                                imageUrl
                        );

                    });

                } else {

                    requireActivity().runOnUiThread(() -> {

                        btnRegister.setEnabled(true);

                        Toast.makeText(
                                getContext(),
                                "Profile picture upload failed",
                                Toast.LENGTH_LONG
                        ).show();
                    });
                }

            } catch (Exception e) {

                requireActivity().runOnUiThread(() -> {

                    btnRegister.setEnabled(true);

                    Toast.makeText(
                            getContext(),
                            "Image upload error: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });

            } finally {

                if (connection != null) {
                    connection.disconnect();
                }
            }

        }).start();
    }


    // ============================================================
    // CONVERT IMAGE TO BASE64
    // ============================================================

    private String convertImageToBase64(Uri imageUri)
            throws Exception {

        InputStream inputStream =
                requireContext()
                        .getContentResolver()
                        .openInputStream(imageUri);

        java.io.ByteArrayOutputStream byteArrayOutputStream =
                new java.io.ByteArrayOutputStream();

        byte[] buffer = new byte[4096];

        int bytesRead;

        while ((bytesRead =
                inputStream.read(buffer)) != -1) {

            byteArrayOutputStream.write(
                    buffer,
                    0,
                    bytesRead
            );
        }

        inputStream.close();

        byte[] imageBytes =
                byteArrayOutputStream.toByteArray();

        return android.util.Base64.encodeToString(
                imageBytes,
                android.util.Base64.NO_WRAP
        );
    }


    // ============================================================
    // SAVE USER DATA TO FIRESTORE
    // ============================================================

    private void saveUserData(
            String uid,
            String name,
            String email,
            String phone,
            String role,
            String profileImageUrl) {

        Map<String, Object> user =
                new HashMap<>();

        user.put("name", name);
        user.put("email", email);
        user.put("phone", phone);
        user.put("role", role);
        user.put("status", "active");
        user.put("profileImage", profileImageUrl);
        user.put("createdAt",
                FieldValue.serverTimestamp());

        db.collection("users")
                .document(uid)
                .set(user)
                .addOnSuccessListener(unused -> {

                    Toast.makeText(
                            getContext(),
                            "Account created successfully!",
                            Toast.LENGTH_SHORT
                    ).show();

                    Navigation.findNavController(
                                    requireView()
                            )
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
    }


    // ============================================================
    // PASSWORD VISIBILITY
    // ============================================================

    private void setupPasswordVisibility(
            EditText editText) {

        editText.setOnTouchListener(
                (v, event) -> {

                    if (event.getAction() ==
                            MotionEvent.ACTION_UP) {

                        if (editText
                                .getCompoundDrawables()[2] != null &&
                                event.getX() >=
                                        (editText.getWidth()
                                                - editText
                                                .getPaddingEnd()
                                                - editText
                                                .getCompoundDrawables()[2]
                                                .getBounds()
                                                .width())) {

                            if (editText
                                    .getTransformationMethod()
                                    == null) {

                                editText.setTransformationMethod(
                                        PasswordTransformationMethod
                                                .getInstance()
                                );

                            } else {

                                editText.setTransformationMethod(
                                        HideReturnsTransformationMethod
                                                .getInstance()
                                );
                            }

                            editText.setSelection(
                                    editText.getText().length()
                            );

                            v.performClick();

                            return true;
                        }
                    }

                    return false;
                }
        );
    }
}