package com.example.villagetocityreseilingapp.ui.auth;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.villagetocityreseilingapp.R;

public class SelectRoleFragment extends Fragment {

    // =========================================================
    // CONSTANTS
    // =========================================================

    private static final String PREF_NAME = "role";
    private static final String KEY_USER_ROLE = "user_role";

    private static final String ROLE_BUYER = "buyer";
    private static final String ROLE_SELLER = "seller";

    // =========================================================
    // CLICK CONTROL
    // =========================================================

    private boolean clickLocked = false;

    private final Handler clickHandler =
            new Handler(Looper.getMainLooper());

    // =========================================================
    // CREATE VIEW
    // =========================================================

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_select_role,
                container,
                false
        );

        // =====================================================
        // BUYER CARD
        // =====================================================

        CardView cardBuyer =
                view.findViewById(R.id.cardBuyer);

        if (cardBuyer != null) {

            cardBuyer.setClickable(true);
            cardBuyer.setFocusable(false);

            cardBuyer.setOnClickListener(v -> {

                if (clickLocked) {
                    return;
                }

                clickLocked = true;

                blinkOnce(
                        cardBuyer,
                        () -> {

                            if (!isAdded()) {
                                return;
                            }

                            // ---------------------------------
                            // SAVE BUYER ROLE
                            // ---------------------------------

                            saveSelectedRole(ROLE_BUYER);

                            // ---------------------------------
                            // OPEN LOGIN
                            // ---------------------------------

                            Navigation
                                    .findNavController(v)
                                    .navigate(
                                            R.id.action_role_to_login
                                    );
                        }
                );
            });
        }

        // =====================================================
        // SELLER CARD
        // =====================================================

        CardView cardSeller =
                view.findViewById(R.id.cardSeller);

        if (cardSeller != null) {

            cardSeller.setClickable(true);
            cardSeller.setFocusable(false);

            cardSeller.setOnClickListener(v -> {

                if (clickLocked) {
                    return;
                }

                clickLocked = true;

                blinkOnce(
                        cardSeller,
                        () -> {

                            if (!isAdded()) {
                                return;
                            }

                            // ---------------------------------
                            // SAVE SELLER ROLE
                            // ---------------------------------

                            saveSelectedRole(ROLE_SELLER);

                            // ---------------------------------
                            // OPEN LOGIN
                            // ---------------------------------

                            Navigation
                                    .findNavController(v)
                                    .navigate(
                                            R.id.action_role_to_login
                                    );
                        }
                );
            });
        }

        return view;
    }

    // =========================================================
    // SAVE SELECTED ROLE
    // =========================================================

    private void saveSelectedRole(String role) {

        if (!isAdded()) {
            return;
        }

        if (!ROLE_BUYER.equals(role)
                && !ROLE_SELLER.equals(role)) {
            return;
        }

        SharedPreferences preferences =
                requireActivity()
                        .getSharedPreferences(
                                PREF_NAME,
                                0
                        );

        preferences.edit()
                .putString(
                        KEY_USER_ROLE,
                        role
                )
                .apply();
    }

    // =========================================================
    // ONE LIGHT-GREEN BLINK
    // =========================================================

    private void blinkOnce(
            View view,
            Runnable afterBlink) {

        if (view == null) {

            if (afterBlink != null) {
                afterBlink.run();
            }

            return;
        }

        // =====================================================
        // CREATE GREEN OVERLAY
        // =====================================================

        final GradientDrawable greenOverlay =
                new GradientDrawable();

        greenOverlay.setColor(
                Color.rgb(
                        232,
                        245,
                        233
                )
        );

        greenOverlay.setCornerRadius(
                dpToPx(14)
        );

        // =====================================================
        // ADD OVERLAY AFTER VIEW IS READY
        // =====================================================

        view.post(() -> {

            if (!isAdded()) {
                clickLocked = false;
                return;
            }

            greenOverlay.setBounds(
                    0,
                    0,
                    view.getWidth(),
                    view.getHeight()
            );

            view.getOverlay().add(
                    greenOverlay
            );

            // =================================================
            // SINGLE BLINK ANIMATION
            // =================================================

            ValueAnimator animator =
                    ValueAnimator.ofInt(
                            0,
                            210,
                            0
                    );

            animator.setDuration(220);

            animator.addUpdateListener(
                    animation -> {

                        int alpha =
                                (Integer)
                                        animation
                                                .getAnimatedValue();

                        greenOverlay.setAlpha(alpha);
                    }
            );

            animator.addListener(
                    new AnimatorListenerAdapter() {

                        @Override
                        public void onAnimationEnd(
                                Animator animation) {

                            removeOverlayAndContinue(
                                    view,
                                    greenOverlay,
                                    afterBlink
                            );
                        }

                        @Override
                        public void onAnimationCancel(
                                Animator animation) {

                            removeOverlayAndContinue(
                                    view,
                                    greenOverlay,
                                    afterBlink
                            );
                        }
                    }
            );

            animator.start();
        });
    }

    // =========================================================
    // REMOVE OVERLAY + CONTINUE
    // =========================================================

    private void removeOverlayAndContinue(
            View view,
            GradientDrawable overlay,
            Runnable afterBlink) {

        if (view != null) {

            try {
                view.getOverlay().remove(overlay);
            } catch (Exception ignored) {
                // Prevent animation cleanup crash
            }
        }

        if (!isAdded()) {
            clickLocked = false;
            return;
        }

        if (afterBlink != null) {
            afterBlink.run();
        }
    }

    // =========================================================
    // DP TO PX
    // =========================================================

    private float dpToPx(float dp) {

        if (getContext() == null) {
            return dp;
        }

        return dp *
                getResources()
                        .getDisplayMetrics()
                        .density;
    }

    // =========================================================
    // DESTROY VIEW
    // =========================================================

    @Override
    public void onDestroyView() {

        clickHandler.removeCallbacksAndMessages(null);

        clickLocked = false;

        super.onDestroyView();
    }
}