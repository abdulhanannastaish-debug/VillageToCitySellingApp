package com.example.villagetocityreseilingapp.ui.main.seller;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.villagetocityreseilingapp.R;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SellerReviewsFragment extends Fragment {

    // =========================================================
    // FIREBASE
    // =========================================================

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    // =========================================================
    // VIEWS
    // =========================================================

    private LinearLayout reviewsContainer;

    private TextView txtAverageRating;
    private TextView txtReviewCount;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public SellerReviewsFragment() {
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
                R.layout.fragment_seller_reviews,
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

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // =====================================================
        // FIND VIEWS
        // =====================================================

        reviewsContainer =
                view.findViewById(
                        R.id.reviewsContainer
                );

        txtAverageRating =
                view.findViewById(
                        R.id.txtAverageRating
                );

        txtReviewCount =
                view.findViewById(
                        R.id.txtReviewCount
                );

        // =====================================================
        // BACK BUTTON
        // =====================================================

        ImageView btnBack =
                view.findViewById(
                        R.id.btnBack
                );

        if (btnBack != null) {

            btnBack.setOnClickListener(
                    v -> requireActivity()
                            .getSupportFragmentManager()
                            .popBackStack()
            );
        }

        // =====================================================
        // LOAD REVIEWS
        // =====================================================

        loadSellerReviews();
    }

    // =========================================================
    // LOAD SELLER REVIEWS
    // =========================================================

    private void loadSellerReviews() {

        if (!isAdded()) {
            return;
        }

        FirebaseUser currentUser =
                auth.getCurrentUser();

        if (currentUser == null) {

            txtAverageRating.setText(
                    "0.0 ★"
            );

            txtReviewCount.setText(
                    "No reviews yet"
            );

            showEmptyMessage(
                    "Please login first."
            );

            return;
        }

        String sellerId =
                currentUser.getUid();

        reviewsContainer.removeAllViews();

        db.collection("reviews")
                .whereEqualTo(
                        "sellerId",
                        sellerId
                )
                .get()
                .addOnSuccessListener(
                        snapshots -> {

                            if (!isAdded()) {
                                return;
                            }

                            reviewsContainer.removeAllViews();

                            if (snapshots.isEmpty()) {

                                txtAverageRating.setText(
                                        "0.0 ★"
                                );

                                txtReviewCount.setText(
                                        "No reviews yet"
                                );

                                showEmptyMessage(
                                        "No reviews available yet."
                                );

                                return;
                            }

                            // =================================================
                            // REVIEWS LIST
                            // =================================================

                            List<DocumentSnapshot> reviews =
                                    new ArrayList<>(
                                            snapshots.getDocuments()
                                    );

                            // =================================================
                            // NEWEST FIRST
                            // =================================================

                            reviews.sort(
                                    new Comparator<DocumentSnapshot>() {

                                        @Override
                                        public int compare(
                                                DocumentSnapshot first,
                                                DocumentSnapshot second) {

                                            long firstTime =
                                                    getCreatedTime(
                                                            first
                                                    );

                                            long secondTime =
                                                    getCreatedTime(
                                                            second
                                                    );

                                            return Long.compare(
                                                    secondTime,
                                                    firstTime
                                            );
                                        }
                                    }
                            );

                            // =================================================
                            // CALCULATE AVERAGE
                            // =================================================

                            double totalRating = 0.0;
                            int ratingCount = 0;

                            for (
                                    DocumentSnapshot review
                                    : reviews
                            ) {

                                double rating =
                                        getDoubleValue(
                                                review,
                                                "rating",
                                                0.0
                                        );

                                if (
                                        rating > 0.0
                                                && rating <= 5.0
                                ) {

                                    totalRating += rating;
                                    ratingCount++;
                                }
                            }

                            double averageRating = 0.0;

                            if (ratingCount > 0) {

                                averageRating =
                                        totalRating
                                                / ratingCount;
                            }

                            // =================================================
                            // SHOW AVERAGE
                            // =================================================

                            txtAverageRating.setText(
                                    String.format(
                                            Locale.getDefault(),
                                            "%.1f ★",
                                            averageRating
                                    )
                            );

                            // =================================================
                            // SHOW REVIEW COUNT
                            // =================================================

                            txtReviewCount.setText(
                                    ratingCount
                                            + (
                                            ratingCount == 1
                                                    ? " Review"
                                                    : " Reviews"
                                    )
                            );

                            // =================================================
                            // ADD REVIEW CARDS
                            // =================================================

                            for (
                                    DocumentSnapshot review
                                    : reviews
                            ) {

                                addReviewCard(
                                        review
                                );
                            }
                        }
                )
                .addOnFailureListener(
                        e -> {

                            if (!isAdded()) {
                                return;
                            }

                            txtAverageRating.setText(
                                    "0.0 ★"
                            );

                            txtReviewCount.setText(
                                    "Unable to load reviews"
                            );

                            showEmptyMessage(
                                    "Failed to load reviews."
                            );
                        }
                );
    }

    // =========================================================
    // ADD REVIEW CARD
    // =========================================================

    private void addReviewCard(
            DocumentSnapshot document) {

        if (!isAdded()) {
            return;
        }

        View reviewView =
                LayoutInflater.from(
                        requireContext()
                ).inflate(
                        R.layout.item_seller_review,
                        reviewsContainer,
                        false
                );

        // =====================================================
        // FIND VIEWS
        // =====================================================

        ImageView imgBuyerProfile =
                reviewView.findViewById(
                        R.id.imgBuyerProfile
                );

        TextView txtBuyerName =
                reviewView.findViewById(
                        R.id.txtBuyerName
                );

        TextView txtOrderId =
                reviewView.findViewById(
                        R.id.txtReviewOrderId
                );

        TextView txtRating =
                reviewView.findViewById(
                        R.id.txtReviewRating
                );

        TextView txtReview =
                reviewView.findViewById(
                        R.id.txtReviewText
                );

        TextView txtDate =
                reviewView.findViewById(
                        R.id.txtReviewDate
                );

        // =====================================================
        // DEFAULT PROFILE IMAGE
        // =====================================================

        if (imgBuyerProfile != null) {

            imgBuyerProfile.setImageResource(
                    R.drawable.ic_profile
            );
        }

        // =====================================================
        // DEFAULT NAME
        // =====================================================

        txtBuyerName.setText(
                "Buyer"
        );

        // =====================================================
        // FETCH BUYER NAME FROM FIRESTORE
        // =====================================================

        fetchBuyerName(
                document,
                txtBuyerName
        );

        // =====================================================
        // ORDER ID
        // =====================================================

        String orderId =
                getStringValue(
                        document,
                        "orderId",
                        "N/A"
                );

        txtOrderId.setText(
                "Order ID: " + orderId
        );

        // =====================================================
        // RATING
        // =====================================================

        double rating =
                getDoubleValue(
                        document,
                        "rating",
                        0.0
                );

        txtRating.setText(
                String.format(
                        Locale.getDefault(),
                        "⭐ %.1f",
                        rating
                )
        );

        // =====================================================
        // REVIEW
        // =====================================================

        String review =
                getStringValue(
                        document,
                        "review",
                        "No review text."
                );

        if (TextUtils.isEmpty(review)) {
            review = "No review text.";
        }

        txtReview.setText(
                "\"" + review + "\""
        );

        // =====================================================
        // DATE
        // =====================================================

        txtDate.setText(
                getReviewDate(
                        document
                )
        );

        // =====================================================
        // ADD CARD
        // =====================================================

        reviewsContainer.addView(
                reviewView
        );
    }

    // =========================================================
    // FETCH BUYER NAME
    // =========================================================

    private void fetchBuyerName(
            DocumentSnapshot reviewDocument,
            TextView txtBuyerName) {

        if (!isAdded()) {
            return;
        }

        // =====================================================
        // FIRST TRY: buyerId
        // =====================================================

        String buyerId =
                getStringValue(
                        reviewDocument,
                        "buyerId",
                        ""
                );

        // =====================================================
        // SECOND TRY: userId
        // =====================================================

        if (TextUtils.isEmpty(buyerId)) {

            buyerId =
                    getStringValue(
                            reviewDocument,
                            "userId",
                            ""
                    );
        }

        // =====================================================
        // THIRD TRY: reviewerId
        // =====================================================

        if (TextUtils.isEmpty(buyerId)) {

            buyerId =
                    getStringValue(
                            reviewDocument,
                            "reviewerId",
                            ""
                    );
        }

        // =====================================================
        // IF BUYER UID EXISTS
        // GET users/{buyerId}
        // =====================================================

        if (!TextUtils.isEmpty(buyerId)) {

            final String finalBuyerId =
                    buyerId;

            db.collection("users")
                    .document(finalBuyerId)
                    .get()
                    .addOnSuccessListener(
                            userDocument -> {

                                if (!isAdded()) {
                                    return;
                                }

                                String name =
                                        getUserName(
                                                userDocument
                                        );

                                if (!TextUtils.isEmpty(name)) {

                                    txtBuyerName.setText(
                                            name
                                    );
                                }
                            }
                    );

            return;
        }

        // =====================================================
        // IF NO UID:
        // TRY EMAIL
        // =====================================================

        String buyerEmail =
                getStringValue(
                        reviewDocument,
                        "buyerEmail",
                        ""
                );

        // =====================================================
        // ALSO TRY email FIELD
        // =====================================================

        if (TextUtils.isEmpty(buyerEmail)) {

            buyerEmail =
                    getStringValue(
                            reviewDocument,
                            "email",
                            ""
                    );
        }

        // =====================================================
        // IF EMAIL EXISTS
        // SEARCH USERS COLLECTION
        // =====================================================

        if (!TextUtils.isEmpty(buyerEmail)) {

            db.collection("users")
                    .whereEqualTo(
                            "email",
                            buyerEmail
                    )
                    .limit(1)
                    .get()
                    .addOnSuccessListener(
                            querySnapshot -> {

                                if (!isAdded()) {
                                    return;
                                }

                                if (
                                        !querySnapshot.isEmpty()
                                ) {

                                    DocumentSnapshot userDocument =
                                            querySnapshot
                                                    .getDocuments()
                                                    .get(0);

                                    String name =
                                            getUserName(
                                                    userDocument
                                            );

                                    if (
                                            !TextUtils.isEmpty(
                                                    name
                                            )
                                    ) {

                                        txtBuyerName.setText(
                                                name
                                        );
                                    }
                                }
                            }
                    );
        }
    }

    // =========================================================
    // GET USER NAME
    // =========================================================

    private String getUserName(
            DocumentSnapshot userDocument) {

        if (
                userDocument == null
                        || !userDocument.exists()
        ) {

            return "";
        }

        // =====================================================
        // NAME
        // =====================================================

        String name =
                userDocument.getString(
                        "name"
                );

        if (
                !TextUtils.isEmpty(name)
                        && !name.contains("@")
        ) {

            return name.trim();
        }

        // =====================================================
        // fullName
        // =====================================================

        String fullName =
                userDocument.getString(
                        "fullName"
                );

        if (
                !TextUtils.isEmpty(fullName)
                        && !fullName.contains("@")
        ) {

            return fullName.trim();
        }

        // =====================================================
        // username
        // =====================================================

        String username =
                userDocument.getString(
                        "username"
                );

        if (
                !TextUtils.isEmpty(username)
                        && !username.contains("@")
        ) {

            return username.trim();
        }

        return "";
    }

    // =========================================================
    // GET STRING
    // =========================================================

    private String getStringValue(
            DocumentSnapshot document,
            String field,
            String defaultValue) {

        String value =
                document.getString(
                        field
                );

        if (
                value == null
                        || value.trim().isEmpty()
        ) {

            return defaultValue;
        }

        return value.trim();
    }

    // =========================================================
    // GET DOUBLE
    // =========================================================

    private double getDoubleValue(
            DocumentSnapshot document,
            String field,
            double defaultValue) {

        Object value =
                document.get(field);

        if (value instanceof Number) {

            return ((Number) value)
                    .doubleValue();
        }

        return defaultValue;
    }

    // =========================================================
    // CREATED TIME
    // =========================================================

    private long getCreatedTime(
            DocumentSnapshot document) {

        Object createdAt =
                document.get(
                        "createdAt"
                );

        if (createdAt instanceof Timestamp) {

            return ((Timestamp) createdAt)
                    .toDate()
                    .getTime();
        }

        if (createdAt instanceof Date) {

            return ((Date) createdAt)
                    .getTime();
        }

        if (createdAt instanceof Number) {

            return ((Number) createdAt)
                    .longValue();
        }

        return 0L;
    }

    // =========================================================
    // REVIEW DATE
    // =========================================================

    private String getReviewDate(
            DocumentSnapshot document) {

        long time =
                getCreatedTime(
                        document
                );

        if (time == 0L) {

            return "Date not available";
        }

        Date date =
                new Date(
                        time
                );

        SimpleDateFormat formatter =
                new SimpleDateFormat(
                        "dd MMM yyyy",
                        Locale.getDefault()
                );

        return formatter.format(
                date
        );
    }

    // =========================================================
    // EMPTY MESSAGE
    // =========================================================

    private void showEmptyMessage(
            String message) {

        if (!isAdded()) {
            return;
        }

        reviewsContainer.removeAllViews();

        TextView textView =
                new TextView(
                        requireContext()
                );

        textView.setLayoutParams(
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        textView.setText(
                message
        );

        textView.setTextSize(
                16
        );

        textView.setTextColor(
                Color.GRAY
        );

        textView.setGravity(
                Gravity.CENTER
        );

        textView.setPadding(
                dpToPx(20),
                dpToPx(50),
                dpToPx(20),
                dpToPx(50)
        );

        reviewsContainer.addView(
                textView
        );
    }

    // =========================================================
    // DP TO PX
    // =========================================================

    private int dpToPx(
            int dp) {

        float density =
                requireContext()
                        .getResources()
                        .getDisplayMetrics()
                        .density;

        return Math.round(
                dp * density
        );
    }

    // =========================================================
    // REFRESH
    // =========================================================

    @Override
    public void onResume() {

        super.onResume();

        if (auth != null) {
            loadSellerReviews();
        }
    }
}