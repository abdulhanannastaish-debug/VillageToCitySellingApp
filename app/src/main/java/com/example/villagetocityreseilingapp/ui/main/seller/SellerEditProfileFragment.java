package com.example.villagetocityreseilingapp.ui.main.seller;

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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

public class SellerEditProfileFragment extends Fragment {

    // =====================================================
    // EDIT TEXTS
    // =====================================================

    private EditText etFullName;
    private EditText etPhone;
    private EditText etEmail;
    private EditText etAddress;
    private EditText etCnic;

    // =====================================================
    // PROFILE IMAGE
    // =====================================================

    private ImageView imgProfile;
    private AppCompatButton btnChangeImage;

    private Uri selectedImageUri;

    private String uploadedProfileImageUrl = "";

    private static final int PICK_IMAGE_REQUEST = 3001;

    // =====================================================
    // BUTTONS
    // =====================================================

    private AppCompatButton btnSaveProfile;
    private AppCompatButton btnCancel;

    // =====================================================
    // FIREBASE
    // =====================================================

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    // =====================================================
    // CLOUDINARY
    // =====================================================

    private static final String CLOUD_NAME = "cvhzteif";

    private static final String UPLOAD_PRESET =
            "rural_reach_upload";


    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public SellerEditProfileFragment() {
        // Required empty public constructor
    }


    // =====================================================
    // ON CREATE VIEW
    // =====================================================

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_seller_edit_profile,
                container,
                false
        );
    }


    // =====================================================
    // ON VIEW CREATED
    // =====================================================

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(
                view,
                savedInstanceState
        );

        // =====================================================
        // FIREBASE
        // =====================================================

        auth = FirebaseAuth.getInstance();

        db = FirebaseFirestore.getInstance();

        // =====================================================
        // CLOUDINARY
        // =====================================================

        initializeCloudinary();

        // =====================================================
        // FIND VIEWS
        // =====================================================

        etFullName =
                view.findViewById(R.id.etFullName);

        etPhone =
                view.findViewById(R.id.etPhone);

        etEmail =
                view.findViewById(R.id.etEmail);

        etAddress =
                view.findViewById(R.id.etAddress);

        etCnic =
                view.findViewById(R.id.etCnic);

        imgProfile =
                view.findViewById(R.id.imgProfile);

        btnChangeImage =
                view.findViewById(R.id.btnChangeImage);

        btnSaveProfile =
                view.findViewById(R.id.btnSaveProfile);

        btnCancel =
                view.findViewById(R.id.btnCancel);

        // =====================================================
        // LOAD CURRENT SELLER DATA
        // =====================================================

        loadSellerData();

        // =====================================================
        // CHANGE IMAGE BUTTON
        // =====================================================

        btnChangeImage.setOnClickListener(v -> {

            openImagePicker();

        });

        // =====================================================
        // PROFILE IMAGE CLICK
        // =====================================================

        imgProfile.setOnClickListener(v -> {

            openImagePicker();

        });

        // =====================================================
        // SAVE
        // =====================================================

        btnSaveProfile.setOnClickListener(v -> {

            saveSellerProfile();

        });

        // =====================================================
        // CANCEL
        // =====================================================

        btnCancel.setOnClickListener(v -> {

            goBackToProfile();

        });

        // =====================================================
        // BACK ARROW
        // =====================================================

        View btnTermsBack =
                view.findViewById(R.id.btnTermsBack);

        if (btnTermsBack != null) {

            btnTermsBack.setOnClickListener(v -> {

                goBackToProfile();

            });
        }
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

        Intent intent =
                new Intent(
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

            // =============================================
            // SHOW SELECTED IMAGE
            // =============================================

            imgProfile.setImageURI(
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
    // LOAD SELLER DATA
    // =====================================================

    private void loadSellerData() {

        FirebaseUser currentUser =
                auth.getCurrentUser();

        if (currentUser == null) {

            Toast.makeText(
                    requireContext(),
                    "Seller is not logged in.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String uid =
                currentUser.getUid();

        db.collection("sellers")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (!isAdded()) {
                        return;
                    }

                    if (!documentSnapshot.exists()) {

                        Toast.makeText(
                                requireContext(),
                                "Seller profile not found.",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }

                    // =================================================
                    // NAME
                    // =================================================

                    String name =
                            documentSnapshot.getString("name");

                    if (name != null) {

                        etFullName.setText(name);
                    }

                    // =================================================
                    // PHONE
                    // =================================================

                    String phone =
                            documentSnapshot.getString("phone");

                    if (phone != null) {

                        etPhone.setText(phone);
                    }

                    // =================================================
                    // EMAIL
                    // =================================================

                    String email =
                            documentSnapshot.getString("email");

                    if (email != null) {

                        etEmail.setText(email);
                    }

                    // =================================================
                    // ADDRESS
                    // =================================================

                    String address =
                            documentSnapshot.getString("address");

                    if (address != null) {

                        etAddress.setText(address);
                    }

                    // =================================================
                    // CNIC
                    // =================================================

                    String cnic =
                            documentSnapshot.getString("cnic");

                    if (cnic != null) {

                        etCnic.setText(cnic);
                    }

                    // =================================================
                    // PROFILE IMAGE
                    // =================================================

                    String profileImageUrl =
                            documentSnapshot.getString(
                                    "profileImageUrl"
                            );

                    if (profileImageUrl != null
                            && !profileImageUrl.isEmpty()) {

                        uploadedProfileImageUrl =
                                profileImageUrl;

                        Glide.with(
                                        requireContext()
                                )
                                .load(profileImageUrl)
                                .placeholder(
                                        R.drawable.profile
                                )
                                .error(
                                        R.drawable.profile
                                )
                                .into(imgProfile);
                    }

                })
                .addOnFailureListener(e -> {

                    if (!isAdded()) {
                        return;
                    }

                    Toast.makeText(
                            requireContext(),
                            "Failed to load profile: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }


    // =====================================================
    // SAVE SELLER PROFILE
    // =====================================================

    private void saveSellerProfile() {

        // =================================================
        // GET VALUES
        // =================================================

        String name =
                etFullName.getText()
                        .toString()
                        .trim();

        String phone =
                etPhone.getText()
                        .toString()
                        .trim();

        String email =
                etEmail.getText()
                        .toString()
                        .trim();

        String address =
                etAddress.getText()
                        .toString()
                        .trim();

        String cnic =
                etCnic.getText()
                        .toString()
                        .trim();

        // =================================================
        // VALIDATION
        // =================================================

        if (TextUtils.isEmpty(name)) {

            etFullName.setError(
                    "Enter your name"
            );

            etFullName.requestFocus();

            return;
        }

        if (TextUtils.isEmpty(phone)) {

            etPhone.setError(
                    "Enter your phone number"
            );

            etPhone.requestFocus();

            return;
        }

        if (TextUtils.isEmpty(email)) {

            etEmail.setError(
                    "Enter your email"
            );

            etEmail.requestFocus();

            return;
        }

        if (TextUtils.isEmpty(address)) {

            etAddress.setError(
                    "Enter your address"
            );

            etAddress.requestFocus();

            return;
        }

        if (TextUtils.isEmpty(cnic)) {

            etCnic.setError(
                    "Enter your CNIC"
            );

            etCnic.requestFocus();

            return;
        }

        if (cnic.length() != 13) {

            etCnic.setError(
                    "CNIC must contain 13 digits"
            );

            etCnic.requestFocus();

            return;
        }

        // =================================================
        // CURRENT USER
        // =================================================

        FirebaseUser currentUser =
                auth.getCurrentUser();

        if (currentUser == null) {

            Toast.makeText(
                    requireContext(),
                    "Seller is not logged in.",
                    Toast.LENGTH_SHORT
            ).show();

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
        // NEW IMAGE SELECTED?
        // =================================================

        if (selectedImageUri != null) {

            uploadProfileImageToCloudinary(
                    selectedImageUri,
                    uid,
                    name,
                    phone,
                    email,
                    address,
                    cnic
            );

        } else {

            // =============================================
            // NO NEW IMAGE
            // =============================================

            saveSellerDataToFirestore(
                    uid,
                    name,
                    phone,
                    email,
                    address,
                    cnic,
                    uploadedProfileImageUrl
            );
        }
    }


    // =====================================================
    // UPLOAD PROFILE IMAGE TO CLOUDINARY
    // =====================================================

    private void uploadProfileImageToCloudinary(
            Uri imageUri,
            String uid,
            String name,
            String phone,
            String email,
            String address,
            String cnic) {

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
                                        "Seller profile upload started"
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
                                            "Seller profile URL = "
                                                    + uploadedProfileImageUrl
                                    );

                                    // =================================
                                    // SAVE TO FIRESTORE
                                    // =================================

                                    saveSellerDataToFirestore(
                                            uid,
                                            name,
                                            phone,
                                            email,
                                            address,
                                            cnic,
                                            uploadedProfileImageUrl
                                    );

                                } else {

                                    enableSaveButton();

                                    Toast.makeText(
                                            requireContext(),
                                            "Image uploaded but URL was not received.",
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
                                        "Profile upload error: "
                                                + error.getDescription()
                                );

                                Toast.makeText(
                                        requireContext(),
                                        "Image upload failed: "
                                                + error.getDescription(),
                                        Toast.LENGTH_LONG
                                ).show();
                            }


                            @Override
                            public void onReschedule(
                                    String requestId,
                                    ErrorInfo error) {

                                Log.d(
                                        "CLOUDINARY",
                                        "Profile upload rescheduled"
                                );
                            }
                        }
                )
                .dispatch();
    }


    // =====================================================
    // SAVE SELLER DATA TO FIRESTORE
    // =====================================================

    private void saveSellerDataToFirestore(
            String uid,
            String name,
            String phone,
            String email,
            String address,
            String cnic,
            String profileImageUrl) {

        // =================================================
        // SELLERS COLLECTION
        // =================================================

        Map<String, Object> sellerUpdate =
                new HashMap<>();

        sellerUpdate.put(
                "name",
                name
        );

        sellerUpdate.put(
                "phone",
                phone
        );

        sellerUpdate.put(
                "email",
                email
        );

        sellerUpdate.put(
                "address",
                address
        );

        sellerUpdate.put(
                "cnic",
                cnic
        );

        // =================================================
        // PROFILE IMAGE URL
        // =================================================

        if (profileImageUrl != null
                && !profileImageUrl.isEmpty()) {

            sellerUpdate.put(
                    "profileImageUrl",
                    profileImageUrl
            );
        }

        // =================================================
        // UPDATE SELLERS
        // =================================================

        db.collection("sellers")
                .document(uid)
                .update(sellerUpdate)
                .addOnSuccessListener(unused -> {

                    if (!isAdded()) {
                        return;
                    }

                    // =================================================
                    // USERS COLLECTION
                    // =================================================

                    Map<String, Object> userUpdate =
                            new HashMap<>();

                    userUpdate.put(
                            "name",
                            name
                    );

                    userUpdate.put(
                            "phone",
                            phone
                    );

                    userUpdate.put(
                            "email",
                            email
                    );

                    userUpdate.put(
                            "address",
                            address
                    );

                    // =============================================
                    // PROFILE IMAGE URL
                    // =============================================

                    if (profileImageUrl != null
                            && !profileImageUrl.isEmpty()) {

                        userUpdate.put(
                                "profileImageUrl",
                                profileImageUrl
                        );
                    }

                    // =================================================
                    // UPDATE USERS
                    // =================================================

                    db.collection("users")
                            .document(uid)
                            .update(userUpdate)
                            .addOnSuccessListener(unused2 -> {

                                if (!isAdded()) {
                                    return;
                                }

                                Toast.makeText(
                                        requireContext(),
                                        "Profile updated successfully!",
                                        Toast.LENGTH_SHORT
                                ).show();

                                enableSaveButton();

                                goBackToProfile();

                            })
                            .addOnFailureListener(e -> {

                                if (!isAdded()) {
                                    return;
                                }

                                enableSaveButton();

                                Toast.makeText(
                                        requireContext(),
                                        "Seller profile updated, but user data update failed: "
                                                + e.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show();

                                goBackToProfile();
                            });

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


    // =====================================================
    // GO BACK TO SELLER PROFILE
    // =====================================================

    private void goBackToProfile() {

        if (!isAdded()) {
            return;
        }

        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.fragment_container,
                        new SellerProfileFragment()
                )
                .commit();
    }
}