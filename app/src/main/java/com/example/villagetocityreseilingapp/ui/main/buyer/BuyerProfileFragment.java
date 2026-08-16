package com.example.villagetocityreseilingapp.ui.main.buyer;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.villagetocityreseilingapp.R;
import com.example.villagetocityreseilingapp.activity.AuthActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class BuyerProfileFragment extends Fragment {

    // =====================================================
    // FIREBASE
    // =====================================================

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // =====================================================
    // PROFILE VIEWS
    // =====================================================

    private TextView tvProfileName;
    private TextView tvProfileRole;
    private TextView tvProfileEmail;
    private TextView tvProfileAddress;
    private TextView tvProfilePhone;

    private TextView btnLogout;

    // =====================================================
    // OTHER VIEWS
    // =====================================================

    private View layoutEditProfile;
    private View layoutTerms;
    private View layoutWallet;

    // =====================================================
    // LIGHT GREEN CLICK COLOR
    // =====================================================

    private static final int NORMAL_COLOR =
            Color.WHITE;

    private static final int CLICK_COLOR =
            Color.rgb(232, 245, 233);

    // =====================================================
    // ON CREATE VIEW
    // =====================================================

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_buyer_profile,
                container,
                false
        );

        // =================================================
        // FIREBASE
        // =================================================

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // =================================================
        // FIND PROFILE VIEWS
        // =================================================

        tvProfileName =
                view.findViewById(R.id.tvProfileName);

        tvProfileRole =
                view.findViewById(R.id.tvProfileRole);

        tvProfileEmail =
                view.findViewById(R.id.tvProfileEmail);

        tvProfileAddress =
                view.findViewById(R.id.tvProfileAddress);

        tvProfilePhone =
                view.findViewById(R.id.tvProfilePhone);

        btnLogout =
                view.findViewById(R.id.btnLogout);

        // =================================================
        // EDIT PROFILE
        // =================================================

        layoutEditProfile =
                view.findViewById(R.id.layoutEditProfile);

        if (layoutEditProfile != null) {

            layoutEditProfile.setOnClickListener(v -> {

                // Light green blink
                blinkGreen(layoutEditProfile);

                // Open Edit Profile after blink
                layoutEditProfile.postDelayed(() -> {

                    if (!isAdded()) {
                        return;
                    }

                    requireActivity()
                            .getSupportFragmentManager()
                            .beginTransaction()
                            .replace(
                                    R.id.fragment_container,
                                    new BuyerEditProfileFragment()
                            )
                            .addToBackStack(null)
                            .commit();

                }, 180);
            });
        }

        // =================================================
        // TERMS & CONDITIONS
        // =================================================

        layoutTerms =
                view.findViewById(R.id.layoutTerms);

        if (layoutTerms != null) {

            layoutTerms.setOnClickListener(v -> {

                // Light green blink
                blinkGreen(layoutTerms);

                // Open Terms after blink
                layoutTerms.postDelayed(() -> {

                    if (!isAdded()) {
                        return;
                    }

                    requireActivity()
                            .getSupportFragmentManager()
                            .beginTransaction()
                            .replace(
                                    R.id.fragment_container,
                                    new BuyerTermsFragment()
                            )
                            .addToBackStack(null)
                            .commit();

                }, 180);
            });
        }

        // =================================================
        // WALLET
        // =================================================

        layoutWallet =
                view.findViewById(R.id.layoutWallet);

        if (layoutWallet != null) {

            layoutWallet.setOnClickListener(v -> {

                // Light green blink
                blinkGreen(layoutWallet);

                // Open Wallet after blink
                layoutWallet.postDelayed(() -> {

                    if (!isAdded()) {
                        return;
                    }

                    requireActivity()
                            .getSupportFragmentManager()
                            .beginTransaction()
                            .replace(
                                    R.id.fragment_container,
                                    new BuyerWalletFragment()
                            )
                            .addToBackStack(null)
                            .commit();

                }, 180);
            });
        }

        // =================================================
        // LOGOUT
        // =================================================

        if (btnLogout != null) {

            btnLogout.setOnClickListener(v -> {

                mAuth.signOut();

                Toast.makeText(
                        requireContext(),
                        "Logged out successfully",
                        Toast.LENGTH_SHORT
                ).show();

                Intent intent = new Intent(
                        requireActivity(),
                        AuthActivity.class
                );

                intent.addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                                |
                                Intent.FLAG_ACTIVITY_CLEAR_TASK
                );

                startActivity(intent);
            });
        }

        // =================================================
        // LOAD PROFILE
        // =================================================

        loadBuyerProfile();

        return view;
    }

    // =====================================================
    // LIGHT GREEN BLINK
    // =====================================================

    private void blinkGreen(View view) {

        if (view == null) {
            return;
        }

        // Light green
        view.setBackgroundColor(
                CLICK_COLOR
        );

        // Return to original white
        view.postDelayed(() -> {

            if (view != null) {

                view.setBackgroundColor(
                        NORMAL_COLOR
                );
            }

        }, 160);
    }

    // =====================================================
    // LOAD BUYER PROFILE
    // =====================================================

    private void loadBuyerProfile() {

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

                    if (documentSnapshot.exists()) {

                        // =================================
                        // NAME
                        // =================================

                        String name =
                                documentSnapshot.getString("name");

                        if (name != null
                                && !name.isEmpty()) {

                            tvProfileName.setText(name);

                        } else {

                            tvProfileName.setText(
                                    "Buyer"
                            );
                        }

                        // =================================
                        // EMAIL
                        // =================================

                        String email =
                                documentSnapshot.getString("email");

                        if (email != null
                                && !email.isEmpty()) {

                            tvProfileEmail.setText(email);

                        } else {

                            tvProfileEmail.setText(
                                    currentUser.getEmail() != null
                                            ? currentUser.getEmail()
                                            : "Email not available"
                            );
                        }

                        // =================================
                        // PHONE
                        // =================================

                        String phone =
                                documentSnapshot.getString("phone");

                        if (phone != null
                                && !phone.isEmpty()) {

                            tvProfilePhone.setText(phone);

                        } else {

                            tvProfilePhone.setText(
                                    "Phone not added"
                            );
                        }

                        // =================================
                        // ROLE
                        // =================================

                        String role =
                                documentSnapshot.getString("role");

                        if (role != null
                                && !role.isEmpty()) {

                            String formattedRole =
                                    role.substring(0, 1)
                                            .toUpperCase()
                                            + role.substring(1);

                            tvProfileRole.setText(
                                    formattedRole
                            );

                        } else {

                            tvProfileRole.setText(
                                    "Buyer"
                            );
                        }

                        // =================================
                        // ADDRESS
                        // =================================

                        String address =
                                documentSnapshot.getString("address");

                        if (address != null
                                && !address.isEmpty()) {

                            tvProfileAddress.setText(
                                    address
                            );

                        } else {

                            tvProfileAddress.setText(
                                    "Address not added"
                            );
                        }

                    } else {

                        Toast.makeText(
                                requireContext(),
                                "Profile data not found",
                                Toast.LENGTH_SHORT
                        ).show();
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
}