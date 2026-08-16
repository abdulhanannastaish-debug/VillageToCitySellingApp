package com.example.villagetocityreseilingapp.ui.auth;

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
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.villagetocityreseilingapp.R;

public class SelectRoleFragment extends Fragment {

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
                view.findViewById(
                        R.id.cardBuyer
                );

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

                            SharedPreferences.Editor editor =
                                    requireActivity()
                                            .getSharedPreferences(
                                                    "role",
                                                    0
                                            )
                                            .edit();

                            editor.putString(
                                    "user_role",
                                    "buyer"
                            );

                            editor.apply();

                            // ---------------------------------
                            // OPEN LOGIN
                            // ---------------------------------

                            Navigation
                                    .findNavController(v)
                                    .navigate(
                                            R.id.action_role_to_login
                                    );

                            unlockClick();
                        }
                );
            });
        }

        // =====================================================
        // SELLER CARD
        // =====================================================

        CardView cardSeller =
                view.findViewById(
                        R.id.cardSeller
                );

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

                            SharedPreferences.Editor editor =
                                    requireActivity()
                                            .getSharedPreferences(
                                                    "role",
                                                    0
                                            )
                                            .edit();

                            editor.putString(
                                    "user_role",
                                    "seller"
                            );

                            editor.apply();

                            // ---------------------------------
                            // OPEN LOGIN
                            // ---------------------------------

                            Navigation
                                    .findNavController(v)
                                    .navigate(
                                            R.id.action_role_to_login
                                    );

                            unlockClick();
                        }
                );
            });
        }

        return view;
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

        // Original background is preserved.
        final GradientDrawable greenOverlay =
                new GradientDrawable();

        greenOverlay.setColor(
                Color.rgb(
                        232,
                        245,
                        233
                )
        );

        // Card corner radius
        greenOverlay.setCornerRadius(
                dpToPx(14)
        );

        // =====================================================
        // ADD ONLY ONE OVERLAY
        // =====================================================

        view.post(() -> {

            if (!isAdded()) {
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
            // ONE BLINK
            // =================================================

            ValueAnimator animator =
                    ValueAnimator.ofInt(
                            0,
                            210,
                            0
                    );

            // Short single blink
            animator.setDuration(220);

            animator.addUpdateListener(
                    animation -> {

                        int alpha =
                                (Integer)
                                        animation
                                                .getAnimatedValue();

                        greenOverlay.setAlpha(
                                alpha
                        );
                    }
            );

            animator.addListener(
                    new android.animation.AnimatorListenerAdapter() {

                        @Override
                        public void onAnimationEnd(
                                android.animation.Animator animation) {

                            // ---------------------------------
                            // REMOVE OVERLAY
                            // ---------------------------------

                            view.getOverlay().remove(
                                    greenOverlay
                            );

                            // ---------------------------------
                            // OPEN NEXT SCREEN
                            // ---------------------------------

                            if (afterBlink != null) {
                                afterBlink.run();
                            }
                        }

                        @Override
                        public void onAnimationCancel(
                                android.animation.Animator animation) {

                            view.getOverlay().remove(
                                    greenOverlay
                            );

                            if (afterBlink != null) {
                                afterBlink.run();
                            }
                        }
                    }
            );

            animator.start();
        });
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
    // UNLOCK CLICK
    // =========================================================

    private void unlockClick() {

        clickHandler.postDelayed(
                () -> clickLocked = false,
                500
        );
    }

    // =========================================================
    // DESTROY VIEW
    // =========================================================

    @Override
    public void onDestroyView() {

        clickHandler.removeCallbacksAndMessages(
                null
        );

        clickLocked = false;

        super.onDestroyView();
    }
}