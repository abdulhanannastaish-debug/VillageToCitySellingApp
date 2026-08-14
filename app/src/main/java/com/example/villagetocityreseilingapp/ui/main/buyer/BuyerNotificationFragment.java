package com.example.villagetocityreseilingapp.ui.main.buyer;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.villagetocityreseilingapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BuyerNotificationFragment extends Fragment {

    // =========================================================
    // FIREBASE
    // =========================================================

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    // =========================================================
    // VIEWS
    // =========================================================

    private View btnBack;
    private LinearLayout notificationContainer;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public BuyerNotificationFragment() {
        // Required empty public constructor
    }

    // =========================================================
    // CREATE VIEW
    // =========================================================

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_buyer_notification,
                container,
                false
        );
    }

    // =========================================================
    // VIEW CREATED
    // =========================================================

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
        // FIND VIEWS
        // =====================================================

        btnBack =
                view.findViewById(
                        R.id.btnNotificationBack
                );

        notificationContainer =
                view.findViewById(
                        R.id.notificationContainer
                );

        // =====================================================
        // BACK
        // =====================================================

        btnBack.setOnClickListener(v -> {

            requireActivity()
                    .getSupportFragmentManager()
                    .popBackStack();
        });

        // =====================================================
        // LOAD NOTIFICATIONS
        // =====================================================

        loadNotifications();
    }

    // =========================================================
    // LOAD NOTIFICATIONS
    // =========================================================

    private void loadNotifications() {

        if (!isAdded()) {
            return;
        }

        FirebaseUser currentUser =
                auth.getCurrentUser();

        if (currentUser == null) {

            showEmptyMessage(
                    "Please login first."
            );

            return;
        }

        notificationContainer.removeAllViews();

        db.collection("notifications")
                .whereEqualTo(
                        "buyerId",
                        currentUser.getUid()
                )
                .get()
                .addOnSuccessListener(
                        queryDocumentSnapshots -> {

                            if (!isAdded()) {
                                return;
                            }

                            if (queryDocumentSnapshots.isEmpty()) {

                                showEmptyMessage(
                                        "No notifications yet."
                                );

                                return;
                            }

                            List<DocumentSnapshot> notifications =
                                    new ArrayList<>(
                                            queryDocumentSnapshots
                                                    .getDocuments()
                                    );

                            // =============================================
                            // NEWEST FIRST
                            // =============================================

                            Collections.sort(
                                    notifications,
                                    new Comparator<DocumentSnapshot>() {

                                        @Override
                                        public int compare(
                                                DocumentSnapshot first,
                                                DocumentSnapshot second) {

                                            Long firstTime =
                                                    getCreatedTime(first);

                                            Long secondTime =
                                                    getCreatedTime(second);

                                            return Long.compare(
                                                    secondTime,
                                                    firstTime
                                            );
                                        }
                                    }
                            );

                            for (
                                    DocumentSnapshot document
                                    : notifications
                            ) {

                                addNotificationCard(
                                        document
                                );
                            }
                        }
                )
                .addOnFailureListener(
                        e -> {

                            if (!isAdded()) {
                                return;
                            }

                            Toast.makeText(
                                    requireContext(),
                                    "Failed to load notifications: "
                                            + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }

    // =========================================================
    // ADD NOTIFICATION CARD
    // =========================================================

    private void addNotificationCard(
            DocumentSnapshot document) {

        if (!isAdded()) {
            return;
        }

        String title =
                document.getString("title");

        String message =
                document.getString("message");

        String orderId =
                document.getString("orderId");

        Boolean read =
                document.getBoolean("read");

        if (title == null ||
                title.trim().isEmpty()) {

            title = "Notification";
        }

        if (message == null ||
                message.trim().isEmpty()) {

            message = "You have a new notification.";
        }

        // =====================================================
        // CARD
        // =====================================================

        LinearLayout card =
                new LinearLayout(
                        requireContext()
                );

        card.setOrientation(
                LinearLayout.VERTICAL
        );

        card.setPadding(
                20,
                18,
                20,
                18
        );

        card.setBackgroundResource(
                R.drawable.bg_notification_card
        );

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        cardParams.setMargins(
                16,
                8,
                16,
                8
        );

        card.setLayoutParams(
                cardParams
        );

        // =====================================================
        // TITLE
        // =====================================================

        TextView titleView =
                new TextView(
                        requireContext()
                );

        titleView.setText(
                title
        );

        titleView.setTextSize(
                17
        );

        titleView.setTypeface(
                null,
                Typeface.BOLD
        );

        titleView.setTextColor(
                getResources().getColor(
                        R.color.green
                )
        );

        // =====================================================
        // MESSAGE
        // =====================================================

        TextView messageView =
                new TextView(
                        requireContext()
                );

        messageView.setText(
                message
        );

        messageView.setTextSize(
                14
        );

        messageView.setTextColor(
                getResources().getColor(
                        android.R.color.darker_gray
                )
        );

        messageView.setPadding(
                0,
                8,
                0,
                0
        );

        // =====================================================
        // ORDER ID
        // =====================================================

        if (orderId != null &&
                !orderId.trim().isEmpty()) {

            TextView orderView =
                    new TextView(
                            requireContext()
                    );

            orderView.setText(
                    "Order #" + orderId
            );

            orderView.setTextSize(
                    13
            );

            orderView.setTextColor(
                    getResources().getColor(
                            android.R.color.darker_gray
                    )
            );

            orderView.setPadding(
                    0,
                    8,
                    0,
                    0
            );

            card.addView(
                    orderView
            );
        }

        // =====================================================
        // ADD VIEWS
        // =====================================================

        card.addView(
                titleView,
                0
        );

        card.addView(
                messageView
        );

        // =====================================================
        // READ / UNREAD UI
        // =====================================================

        if (read == null ||
                !read) {

            // New notification
            card.setAlpha(
                    1.0f
            );

        } else {

            // Already read notification
            card.setAlpha(
                    0.70f
            );
        }

        // =====================================================
        // CARD CLICK
        // =====================================================

        card.setOnClickListener(v -> {

            /*
             * Notification screen open ho chuki hai.
             *
             * Saari unread notifications ko read kar do.
             * Is se notification badge ka count 0 ho jayega.
             */

            markAllNotificationsAsRead();
        });

        notificationContainer.addView(
                card
        );
    }

    // =========================================================
    // MARK ALL NOTIFICATIONS AS READ
    // =========================================================

    private void markAllNotificationsAsRead() {

        if (!isAdded()) {
            return;
        }

        FirebaseUser currentUser =
                auth.getCurrentUser();

        if (currentUser == null) {
            return;
        }

        db.collection("notifications")
                .whereEqualTo(
                        "buyerId",
                        currentUser.getUid()
                )
                .whereEqualTo(
                        "read",
                        false
                )
                .get()
                .addOnSuccessListener(
                        snapshots -> {

                            if (!isAdded()) {
                                return;
                            }

                            // =========================================
                            // NO UNREAD NOTIFICATIONS
                            // =========================================

                            if (snapshots.isEmpty()) {
                                return;
                            }

                            // =========================================
                            // BATCH UPDATE
                            // =========================================

                            WriteBatch batch =
                                    db.batch();

                            for (
                                    DocumentSnapshot document
                                    : snapshots.getDocuments()
                            ) {

                                batch.update(
                                        document.getReference(),
                                        "read",
                                        true
                                );
                            }

                            batch.commit()
                                    .addOnSuccessListener(
                                            unused -> {

                                                if (!isAdded()) {
                                                    return;
                                                }

                                                /*
                                                 * UI refresh.
                                                 *
                                                 * Ab saari notifications
                                                 * read=true hain.
                                                 */

                                                loadNotifications();
                                            }
                                    )
                                    .addOnFailureListener(
                                            e -> {

                                                if (!isAdded()) {
                                                    return;
                                                }

                                                Toast.makeText(
                                                        requireContext(),
                                                        "Failed to update notifications.",
                                                        Toast.LENGTH_SHORT
                                                ).show();
                                            }
                                    );
                        }
                )
                .addOnFailureListener(
                        e -> {

                            if (!isAdded()) {
                                return;
                            }

                            Toast.makeText(
                                    requireContext(),
                                    "Failed to update notifications.",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                );
    }

    // =========================================================
    // CREATED TIME
    // =========================================================

    private Long getCreatedTime(
            DocumentSnapshot document) {

        Object createdAt =
                document.get("createdAt");

        if (createdAt instanceof com.google.firebase.Timestamp) {

            return ((com.google.firebase.Timestamp) createdAt)
                    .toDate()
                    .getTime();
        }

        if (createdAt instanceof Number) {

            return ((Number) createdAt)
                    .longValue();
        }

        return 0L;
    }

    // =========================================================
    // EMPTY
    // =========================================================

    private void showEmptyMessage(
            String message) {

        if (!isAdded()) {
            return;
        }

        notificationContainer.removeAllViews();

        TextView empty =
                new TextView(
                        requireContext()
                );

        empty.setText(
                message
        );

        empty.setTextSize(
                16
        );

        empty.setTextColor(
                getResources().getColor(
                        android.R.color.darker_gray
                )
        );

        empty.setGravity(
                Gravity.CENTER
        );

        empty.setPadding(
                20,
                80,
                20,
                80
        );

        notificationContainer.addView(
                empty
        );
    }

    // =========================================================
    // RESUME
    // =========================================================

    @Override
    public void onResume() {

        super.onResume();

        if (db != null) {
            loadNotifications();
        }
    }
}