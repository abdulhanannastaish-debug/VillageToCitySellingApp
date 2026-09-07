package com.example.villagetocityreseilingapp.ui.main.buyer;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.villagetocityreseilingapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class BuyerEditProfileFragment extends Fragment {

    // =====================================================
    // FIREBASE
    // =====================================================

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // =====================================================
    // EDIT TEXTS
    // =====================================================

    private EditText etEditName;
    private EditText etEditPhone;
    private EditText etEditEmail;
    private EditText etEditAddress;

    // =====================================================
    // PROFILE IMAGE
    // =====================================================

    private ImageView imgEditProfile;

    private Uri selectedImageUri;

    private String uploadedProfileImageUrl = "";

    private static final int PICK_IMAGE_REQUEST = 2001;

    // =====================================================
    // CLOUDINARY
    // =====================================================

    private static final String CLOUD_NAME = "cvhzteif";

    private static final String UPLOAD_PRESET =
            "rural_reach_upload";

    // =====================================================
    // SAVE BUTTON
    // =====================================================

    private AppCompatButton btnSaveProfile;


    // =====================================================
    // ON CREATE VIEW
    // =====================================================

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_buyer_edit_profile,
                container,
                false
        );

        // =================================================
        // FIREBASE
        // =================================================

        mAuth = FirebaseAuth.getInstance();

        db = FirebaseFirestore.getInstance();

        // =================================================
        // CLOUDINARY
        // =================================================

        initializeCloudinary();

        // =================================================
        // FIND VIEWS
        // =================================================

        etEditName =
                view.findViewById(R.id.etEditName);

        etEditPhone =
                view.findViewById(R.id.etEditPhone);

        etEditEmail =
                view.findViewById(R.id.etEditEmail);

        etEditAddress =
                view.findViewById(R.id.etEditAddress);

        imgEditProfile =
                view.findViewById(R.id.imgEditProfile);

        btnSaveProfile =
                view.findViewById(R.id.btnSaveProfile);

        // =================================================
        // LOAD PROFILE
        // =================================================

        loadProfileData();

        // =================================================
        // PROFILE IMAGE CLICK
        // =================================================

        imgEditProfile.setOnClickListener(v -> {
            openImagePicker();
        });

        // =================================================
        // BACK BUTTON
        // =================================================

        View btnEditProfileBack =
                view.findViewById(R.id.btnEditProfileBack);

        btnEditProfileBack.setOnClickListener(v -> {

            requireActivity()
                    .getSupportFragmentManager()
                    .popBackStack();

        });

        // =================================================
        // SAVE BUTTON
        // =================================================

        btnSaveProfile.setOnClickListener(v -> {
            updateProfile();
        });

        return view;
    }


    // =====================================================
    // INITIALIZE CLOUDINARY
    // =====================================================

    private void initializeCloudinary() {

        try {

            MediaManager.get();

        } catch (IllegalStateException e) {

            Map<String, Object> config =
                    new HashMap<>();

            config.put(
                    "cloud_name",
                    CLOUD_NAME
            );

            MediaManager.init(
                    requireContext(),
                    config
            );
        }
    }


    // =====================================================
    // OPEN GALLERY
    // =====================================================

    private void openImagePicker() {

        Intent intent = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        );

        intent.setType("image/*");

        startActivityForResult(
                intent,
                PICK_IMAGE_REQUEST
        );
    }


    // =====================================================
    // IMAGE PICKER RESULT
    // =====================================================

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

        if (requestCode == PICK_IMAGE_REQUEST
                && resultCode == Activity.RESULT_OK
                && data != null
                && data.getData() != null) {

            selectedImageUri =
                    data.getData();

            imgEditProfile.setImageURI(
                    selectedImageUri
            );

            Toast.makeText(
                    requireContext(),
                    "Profile image selected",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }


    // =====================================================
    // LOAD PROFILE DATA
    // =====================================================

    private void loadProfileData() {

        FirebaseUser currentUser =
                mAuth.getCurrentUser();

        if (currentUser == null) {

            Toast.makeText(
                    requireContext(),
                    "User is not logged in",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String uid =
                currentUser.getUid();

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (!isAdded()) {
                        return;
                    }

                    if (!documentSnapshot.exists()) {
                        return;
                    }

                    // =====================================
                    // NAME
                    // =====================================

                    String name =
                            documentSnapshot.getString("name");

                    if (name != null) {
                        etEditName.setText(name);
                    }

                    // =====================================
                    // PHONE
                    // =====================================

                    String phone =
                            documentSnapshot.getString("phone");

                    if (phone != null) {
                        etEditPhone.setText(phone);
                    }

                    // =====================================
                    // EMAIL
                    // =====================================

                    String email =
                            documentSnapshot.getString("email");

                    if (email != null) {

                        etEditEmail.setText(email);

                    } else if (
                            currentUser.getEmail() != null) {

                        etEditEmail.setText(
                                currentUser.getEmail()
                        );
                    }

                    // =====================================
                    // ADDRESS
                    // =====================================

                    String address =
                            documentSnapshot.getString("address");

                    if (address != null) {
                        etEditAddress.setText(address);
                    }

                    // =====================================
                    // PROFILE IMAGE
                    // =====================================

                    String profileImageUrl =
                            documentSnapshot.getString(
                                    "profileImageUrl"
                            );

                    if (profileImageUrl != null
                            && !profileImageUrl.isEmpty()) {

                        uploadedProfileImageUrl =
                                profileImageUrl;

                        Glide.with(requireContext())
                                .load(profileImageUrl)
                                .placeholder(R.drawable.hanan)
                                .error(R.drawable.hanan)
                                .into(imgEditProfile);
                    }

                })
                .addOnFailureListener(e -> {

                    if (!isAdded()) {
                        return;
                    }

                    Toast.makeText(
                            requireContext(),
                            "Failed to load profile",
                            Toast.LENGTH_LONG
                    ).show();
                });
    }


    // =====================================================
    // UPDATE PROFILE
    // =====================================================

    private void updateProfile() {

        FirebaseUser currentUser =
                mAuth.getCurrentUser();

        if (currentUser == null) {

            Toast.makeText(
                    requireContext(),
                    "User is not logged in",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // =================================================
        // GET VALUES
        // =================================================

        String name =
                etEditName.getText()
                        .toString()
                        .trim();

        String phone =
                etEditPhone.getText()
                        .toString()
                        .trim();

        String email =
                etEditEmail.getText()
                        .toString()
                        .trim();

        String address =
                etEditAddress.getText()
                        .toString()
                        .trim();

        // =================================================
        // VALIDATION
        // =================================================

        if (TextUtils.isEmpty(name)) {

            etEditName.setError(
                    "Please enter your name"
            );

            etEditName.requestFocus();

            return;
        }

        if (TextUtils.isEmpty(phone)) {

            etEditPhone.setError(
                    "Please enter your phone number"
            );

            etEditPhone.requestFocus();

            return;
        }

        if (TextUtils.isEmpty(email)) {

            etEditEmail.setError(
                    "Please enter your email"
            );

            etEditEmail.requestFocus();

            return;
        }

        if (TextUtils.isEmpty(address)) {

            etEditAddress.setError(
                    "Please enter your address"
            );

            etEditAddress.requestFocus();

            return;
        }

        String uid =
                currentUser.getUid();

        // =================================================
        // DISABLE SAVE BUTTON
        // =================================================

        btnSaveProfile.setEnabled(false);

        btnSaveProfile.setText(
                "Saving..."
        );

        // =================================================
        // NEW IMAGE SELECTED
        // =================================================

        if (selectedImageUri != null) {

            uploadProfileImageToCloudinary(
                    selectedImageUri,
                    uid,
                    name,
                    phone,
                    email,
                    address
            );

        } else {

            // =============================================
            // NO NEW IMAGE
            // =============================================

            saveProfileToFirestore(
                    uid,
                    name,
                    phone,
                    email,
                    address,
                    uploadedProfileImageUrl
            );
        }
    }


    // =====================================================
    // UPLOAD PROFILE IMAGE
    // =====================================================

    private void uploadProfileImageToCloudinary(
            Uri imageUri,
            String uid,
            String name,
            String phone,
            String email,
            String address) {

        Toast.makeText(
                requireContext(),
                "Uploading profile image...",
                Toast.LENGTH_SHORT
        ).show();

        MediaManager.get()
                .upload(imageUri)
                .unsigned(UPLOAD_PRESET)
                .option(
                        "folder",
                        "rural_reach/profile"
                )
                .callback(
                        new UploadCallback() {

                            @Override
                            public void onStart(
                                    String requestId) {

                                Log.d(
                                        "CLOUDINARY",
                                        "Profile upload started"
                                );
                            }


                            @Override
                            public void onProgress(
                                    String requestId,
                                    long bytes,
                                    long totalBytes) {

                                Log.d(
                                        "CLOUDINARY",
                                        "Upload progress: "
                                                + bytes
                                                + "/"
                                                + totalBytes
                                );
                            }


                            @Override
                            public void onSuccess(
                                    String requestId,
                                    Map resultData) {

                                if (!isAdded()) {
                                    return;
                                }

                                Object secureUrl =
                                        resultData.get(
                                                "secure_url"
                                        );

                                if (secureUrl != null) {

                                    uploadedProfileImageUrl =
                                            secureUrl.toString();

                                    Log.d(
                                            "CLOUDINARY",
                                            "Profile URL = "
                                                    + uploadedProfileImageUrl
                                    );

                                    // =============================
                                    // SAVE TO FIRESTORE
                                    // =============================

                                    saveProfileToFirestore(
                                            uid,
                                            name,
                                            phone,
                                            email,
                                            address,
                                            uploadedProfileImageUrl
                                    );

                                } else {

                                    enableSaveButton();

                                    Toast.makeText(
                                            requireContext(),
                                            "Image uploaded but URL not received",
                                            Toast.LENGTH_LONG
                                    ).show();
                                }
                            }


                            @Override
                            public void onError(
                                    String requestId,
                                    ErrorInfo error) {

                                if (!isAdded()) {
                                    return;
                                }

                                enableSaveButton();

                                Log.e(
                                        "CLOUDINARY",
                                        "Upload Error: "
                                                + error.getDescription()
                                );

                                Toast.makeText(
                                        requireContext(),
                                        "Image upload failed",
                                        Toast.LENGTH_LONG
                                ).show();
                            }


                            @Override
                            public void onReschedule(
                                    String requestId,
                                    ErrorInfo error) {

                                Log.d(
                                        "CLOUDINARY",
                                        "Upload rescheduled"
                                );
                            }
                        }
                )
                .dispatch();
    }


    // =====================================================
    // SAVE PROFILE TO FIRESTORE
    // =====================================================

    private void saveProfileToFirestore(
            String uid,
            String name,
            String phone,
            String email,
            String address,
            String profileImageUrl) {

        Map<String, Object> updates =
                new HashMap<>();

        updates.put(
                "name",
                name
        );

        updates.put(
                "phone",
                phone
        );

        updates.put(
                "email",
                email
        );

        updates.put(
                "address",
                address
        );

        // ================================================
        // PROFILE IMAGE URL
        // ================================================

        if (profileImageUrl != null
                && !profileImageUrl.isEmpty()) {

            updates.put(
                    "profileImageUrl",
                    profileImageUrl
            );
        }

        // =================================================
        // FIRESTORE UPDATE
        // =================================================

        db.collection("users")
                .document(uid)
                .update(updates)
                .addOnSuccessListener(unused -> {

                    if (!isAdded()) {
                        return;
                    }

                    Toast.makeText(
                            requireContext(),
                            "Profile updated successfully",
                            Toast.LENGTH_SHORT
                    ).show();

                    enableSaveButton();

                    requireActivity()
                            .getSupportFragmentManager()
                            .popBackStack();

                })
                .addOnFailureListener(e -> {

                    if (!isAdded()) {
                        return;
                    }

                    enableSaveButton();

                    Toast.makeText(
                            requireContext(),
                            "Failed to update profile: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }


    // =====================================================
    // ENABLE SAVE BUTTON
    // =====================================================

    private void enableSaveButton() {

        btnSaveProfile.setEnabled(true);

        btnSaveProfile.setText(
                "Save Changes"
        );
    }
}