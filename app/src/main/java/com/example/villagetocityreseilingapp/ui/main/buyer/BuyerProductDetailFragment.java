package com.example.villagetocityreseilingapp.ui.main.buyer;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.example.villagetocityreseilingapp.R;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BuyerProductDetailFragment extends Fragment {

    // =========================================================
    // FIREBASE
    // =========================================================

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    // =========================================================
    // PRODUCT DATA
    // =========================================================

    private Map<String, Object> selectedProduct;

    // =========================================================
    // VIEWS
    // =========================================================

    private ImageButton btnBack;
    private ImageView imgProduct;

    private TextView tvProductTitle;
    private TextView tvProductName;
    private TextView tvProductPrice;
    private TextView tvProductRating;

    private TextView tvSellerName;
    private TextView tvSellerPhone;

    private TextView tvProductDescription;

    private TextView tvReviewsCount;
    private TextView tvNoReviews;

    private LinearLayout reviewsContainer;

    private AppCompatButton btnAddToCart;
    private AppCompatButton btnBuyNow;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public BuyerProductDetailFragment() {
    }

    // =========================================================
    // SET PRODUCT
    // =========================================================

    public void setProduct(Map<String, Object> product) {
        selectedProduct = product;
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
                R.layout.fragment_buyer_product_detail,
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

        super.onViewCreated(view, savedInstanceState);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // =====================================================
        // FIND VIEWS
        // =====================================================

        btnBack = view.findViewById(R.id.btn_back);

        imgProduct = view.findViewById(R.id.img_product);

        tvProductTitle = view.findViewById(R.id.tv_product_title);
        tvProductName = view.findViewById(R.id.tv_product_name);
        tvProductPrice = view.findViewById(R.id.tv_product_price);
        tvProductRating = view.findViewById(R.id.tv_product_rating);

        tvSellerName = view.findViewById(R.id.tv_seller_name);
        tvSellerPhone = view.findViewById(R.id.tv_seller_phone);

        tvProductDescription =
                view.findViewById(R.id.tv_product_description);

        tvReviewsCount =
                view.findViewById(R.id.tv_reviews_count);

        tvNoReviews =
                view.findViewById(R.id.tv_no_reviews);

        reviewsContainer =
                view.findViewById(R.id.reviewsContainer);

        btnAddToCart =
                view.findViewById(R.id.btn_add_to_cart);

        btnBuyNow =
                view.findViewById(R.id.btn_buy_now);

        // =====================================================
        // HEADER
        // =====================================================

        tvProductTitle.setText("Product Details");

        // =====================================================
        // BACK
        // =====================================================

        btnBack.setOnClickListener(v ->
                requireActivity()
                        .getSupportFragmentManager()
                        .popBackStack()
        );

        // =====================================================
        // DISPLAY PRODUCT
        // =====================================================

        if (selectedProduct != null) {

            displayProduct(selectedProduct);

        } else {

            Toast.makeText(
                    requireContext(),
                    "Product information not found",
                    Toast.LENGTH_SHORT
            ).show();
        }

        // =====================================================
        // ADD TO CART
        // =====================================================

        btnAddToCart.setOnClickListener(v -> {

            if (selectedProduct == null) {

                Toast.makeText(
                        requireContext(),
                        "Product not found",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            addProductToCart(selectedProduct);
        });

        // =====================================================
        // BUY NOW
        // =====================================================

        btnBuyNow.setOnClickListener(v -> {

            if (selectedProduct == null) {

                Toast.makeText(
                        requireContext(),
                        "Product not found",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            openCheckout();
        });
    }

    // =========================================================
    // DISPLAY PRODUCT
    // =========================================================

    private void displayProduct(
            Map<String, Object> product) {

        String productName =
                getStringValue(
                        product,
                        "name",
                        "Product"
                );

        tvProductName.setText(productName);

        double price =
                getDouble(product.get("price"));

        tvProductPrice.setText(
                formatPrice(price)
        );

        // Initial rating while reviews are loading
        setRatingText(
                tvProductRating,
                0.0
        );

        String sellerId =
                getStringValue(
                        product,
                        "sellerId",
                        ""
                );

        String sellerName =
                getStringValue(
                        product,
                        "sellerName",
                        ""
                );

        String sellerPhone =
                getStringValue(
                        product,
                        "sellerPhone",
                        ""
                );

        if (!sellerName.isEmpty()) {

            tvSellerName.setText(
                    "Seller: " + sellerName
            );

        } else {

            tvSellerName.setText(
                    "Seller: Loading..."
            );
        }

        if (!sellerPhone.isEmpty()) {

            tvSellerPhone.setText(
                    "Phone: " + sellerPhone
            );

        } else {

            tvSellerPhone.setText(
                    "Phone: Loading..."
            );
        }

        loadSellerInformation(
                sellerId,
                sellerName,
                sellerPhone
        );

        String description =
                getStringValue(
                        product,
                        "description",
                        "No description available."
                );

        tvProductDescription.setText(description);

        imgProduct.setImageResource(
                R.drawable.baseline_photo_camera_24
        );

        // =====================================================
        // LOAD REVIEWS
        // =====================================================

        String productId =
                getStringValue(
                        product,
                        "productId",
                        ""
                );

        loadProductReviews(productId);
    }

    // =========================================================
    // LOAD SELLER INFORMATION
    // =========================================================

    private void loadSellerInformation(
            String sellerId,
            String fallbackName,
            String fallbackPhone) {

        if (TextUtils.isEmpty(sellerId)) {

            setSellerFallback(
                    fallbackName,
                    fallbackPhone
            );

            return;
        }

        db.collection("sellers")
                .document(sellerId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (!isAdded()) {
                        return;
                    }

                    if (documentSnapshot.exists()) {

                        String sellerName =
                                getSellerName(documentSnapshot);

                        String sellerPhone =
                                getSellerPhone(documentSnapshot);

                        if (!TextUtils.isEmpty(sellerName)) {

                            tvSellerName.setText(
                                    "Seller: " + sellerName
                            );

                        } else {

                            tvSellerName.setText(
                                    "Seller: "
                                            + (TextUtils.isEmpty(
                                            fallbackName)
                                            ? "Not available"
                                            : fallbackName)
                            );
                        }

                        if (!TextUtils.isEmpty(sellerPhone)) {

                            tvSellerPhone.setText(
                                    "Phone: " + sellerPhone
                            );

                        } else {

                            tvSellerPhone.setText(
                                    "Phone: "
                                            + (TextUtils.isEmpty(
                                            fallbackPhone)
                                            ? "Not available"
                                            : fallbackPhone)
                            );
                        }

                    } else {

                        setSellerFallback(
                                fallbackName,
                                fallbackPhone
                        );
                    }
                })
                .addOnFailureListener(e ->
                        setSellerFallback(
                                fallbackName,
                                fallbackPhone
                        )
                );
    }

    // =========================================================
    // SELLER FALLBACK
    // =========================================================

    private void setSellerFallback(
            String name,
            String phone) {

        if (!TextUtils.isEmpty(name)) {

            tvSellerName.setText(
                    "Seller: " + name
            );

        } else {

            tvSellerName.setText(
                    "Seller: Not available"
            );
        }

        if (!TextUtils.isEmpty(phone)) {

            tvSellerPhone.setText(
                    "Phone: " + phone
            );

        } else {

            tvSellerPhone.setText(
                    "Phone: Not available"
            );
        }
    }

    // =========================================================
    // GET SELLER NAME
    // =========================================================

    private String getSellerName(
            DocumentSnapshot document) {

        String name =
                getDocumentString(
                        document,
                        "name"
                );

        if (!name.isEmpty()) {
            return name;
        }

        name =
                getDocumentString(
                        document,
                        "sellerName"
                );

        if (!name.isEmpty()) {
            return name;
        }

        return getDocumentString(
                document,
                "fullName"
        );
    }

    // =========================================================
    // GET SELLER PHONE
    // =========================================================

    private String getSellerPhone(
            DocumentSnapshot document) {

        String phone =
                getDocumentString(
                        document,
                        "phone"
                );

        if (!phone.isEmpty()) {
            return phone;
        }

        phone =
                getDocumentString(
                        document,
                        "phoneNumber"
                );

        if (!phone.isEmpty()) {
            return phone;
        }

        return getDocumentString(
                document,
                "sellerPhone"
        );
    }

    // =========================================================
    // DOCUMENT STRING
    // =========================================================

    private String getDocumentString(
            DocumentSnapshot document,
            String key) {

        Object value = document.get(key);

        if (value == null) {
            return "";
        }

        return String.valueOf(value).trim();
    }

    // =========================================================
    // LOAD PRODUCT REVIEWS
    // =========================================================

    private void loadProductReviews(
            String productId) {

        if (!isAdded()) {
            return;
        }

        reviewsContainer.removeAllViews();

        if (TextUtils.isEmpty(productId)) {

            setRatingText(
                    tvProductRating,
                    0.0
            );

            showNoReviews();

            return;
        }

        db.collection("reviews")
                .whereEqualTo(
                        "productId",
                        productId
                )
                .get()
                .addOnSuccessListener(
                        snapshots -> {

                            if (!isAdded()) {
                                return;
                            }

                            reviewsContainer.removeAllViews();

                            if (snapshots.isEmpty()) {

                                setRatingText(
                                        tvProductRating,
                                        0.0
                                );

                                showNoReviews();

                                return;
                            }

                            // =================================================
                            // CALCULATE OVERALL RATING
                            // =================================================

                            double totalRating = 0.0;
                            int validRatings = 0;

                            for (
                                    DocumentSnapshot review
                                    : snapshots.getDocuments()
                            ) {

                                double rating =
                                        getDouble(
                                                review.get("rating")
                                        );

                                if (
                                        rating >= 1.0
                                                && rating <= 5.0
                                ) {

                                    totalRating += rating;
                                    validRatings++;
                                }
                            }

                            double overallRating = 0.0;

                            if (validRatings > 0) {

                                overallRating =
                                        totalRating
                                                / validRatings;
                            }

                            // =================================================
                            // SHOW OVERALL RATING
                            // STAR = YELLOW
                            // NUMBER = BLACK
                            // =================================================

                            setRatingText(
                                    tvProductRating,
                                    overallRating
                            );

                            tvNoReviews.setVisibility(
                                    View.GONE
                            );

                            tvReviewsCount.setText(
                                    String.valueOf(
                                            snapshots.size()
                                    )
                            );

                            List<DocumentSnapshot> reviewList =
                                    new ArrayList<>(
                                            snapshots.getDocuments()
                                    );

                            Collections.sort(
                                    reviewList,
                                    (a, b) -> {

                                        long timeA =
                                                getTimestampMillis(
                                                        a.get("createdAt")
                                                );

                                        long timeB =
                                                getTimestampMillis(
                                                        b.get("createdAt")
                                                );

                                        return Long.compare(
                                                timeB,
                                                timeA
                                        );
                                    }
                            );

                            for (DocumentSnapshot review :
                                    reviewList) {

                                addReviewView(review);
                            }
                        }
                )
                .addOnFailureListener(e -> {

                    if (!isAdded()) {
                        return;
                    }

                    setRatingText(
                            tvProductRating,
                            0.0
                    );

                    showNoReviews();

                    Toast.makeText(
                            requireContext(),
                            "Unable to load reviews",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }

    // =========================================================
    // SET RATING TEXT
    // ONLY STAR YELLOW
    // NUMBER BLACK
    // =========================================================

    private void setRatingText(
            TextView textView,
            double rating) {

        if (textView == null) {
            return;
        }

        String ratingText =
                "★ " + formatRating(rating);

        SpannableString spannable =
                new SpannableString(ratingText);

        // =====================================================
        // STAR YELLOW
        // =====================================================

        spannable.setSpan(
                new ForegroundColorSpan(Color.YELLOW),
                0,
                1,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        // =====================================================
        // NUMBER BLACK
        // =====================================================

        if (ratingText.length() > 1) {

            spannable.setSpan(
                    new ForegroundColorSpan(Color.BLACK),
                    1,
                    ratingText.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }

        textView.setText(spannable);
    }

    // =========================================================
    // ADD REVIEW VIEW
    // =========================================================

    private void addReviewView(
            DocumentSnapshot reviewDocument) {

        if (!isAdded()) {
            return;
        }

        Map<String, Object> review =
                reviewDocument.getData();

        if (review == null) {
            return;
        }

        String buyerId =
                getStringValue(
                        review,
                        "buyerId",
                        ""
                );

        String buyerName =
                getStringValue(
                        review,
                        "buyerName",
                        "Buyer"
                );

        String reviewText =
                getStringValue(
                        review,
                        "review",
                        ""
                );

        double rating =
                getDouble(
                        review.get("rating")
                );

        String date =
                getReviewDate(review);

        // =====================================================
        // MAIN REVIEW LAYOUT
        // =====================================================

        LinearLayout reviewLayout =
                new LinearLayout(
                        requireContext()
                );

        reviewLayout.setOrientation(
                LinearLayout.VERTICAL
        );

        reviewLayout.setPadding(
                dpToPx(4),
                dpToPx(14),
                dpToPx(4),
                dpToPx(14)
        );

        // =====================================================
        // USER HEADER
        // =====================================================

        LinearLayout userRow =
                new LinearLayout(
                        requireContext()
                );

        userRow.setOrientation(
                LinearLayout.HORIZONTAL
        );

        userRow.setGravity(
                Gravity.CENTER_VERTICAL
        );

        // =====================================================
        // PROFILE IMAGE
        // =====================================================

        TextView profile =
                new TextView(
                        requireContext()
                );

        profile.setText("👤");
        profile.setTextSize(25);
        profile.setGravity(Gravity.CENTER);

        profile.setBackgroundColor(
                Color.rgb(232, 245, 233)
        );

        LinearLayout.LayoutParams profileParams =
                new LinearLayout.LayoutParams(
                        dpToPx(48),
                        dpToPx(48)
                );

        userRow.addView(
                profile,
                profileParams
        );

        // =====================================================
        // NAME + RATING
        // =====================================================

        LinearLayout userInfo =
                new LinearLayout(
                        requireContext()
                );

        userInfo.setOrientation(
                LinearLayout.VERTICAL
        );

        LinearLayout.LayoutParams infoParams =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                );

        infoParams.leftMargin =
                dpToPx(10);

        TextView name =
                new TextView(
                        requireContext()
                );

        name.setText(
                buyerName
        );

        name.setTextColor(
                Color.rgb(34, 34, 34)
        );

        name.setTextSize(15);

        name.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        userInfo.addView(name);

        // =====================================================
        // REVIEW RATING
        // STAR YELLOW
        // NUMBER BLACK
        // =====================================================

        TextView ratingText =
                new TextView(
                        requireContext()
                );

        String reviewRating =
                "★ " + formatRating(rating);

        SpannableString reviewRatingSpan =
                new SpannableString(reviewRating);

        // Yellow star
        reviewRatingSpan.setSpan(
                new ForegroundColorSpan(Color.YELLOW),
                0,
                1,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        // Black number
        if (reviewRating.length() > 1) {

            reviewRatingSpan.setSpan(
                    new ForegroundColorSpan(Color.BLACK),
                    1,
                    reviewRating.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }

        ratingText.setText(
                reviewRatingSpan
        );

        ratingText.setTextSize(13);

        ratingText.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        userInfo.addView(ratingText);

        userRow.addView(
                userInfo,
                infoParams
        );

        // =====================================================
        // DATE
        // =====================================================

        TextView dateView =
                new TextView(
                        requireContext()
                );

        dateView.setText(date);
        dateView.setTextColor(Color.GRAY);
        dateView.setTextSize(11);

        userRow.addView(dateView);

        reviewLayout.addView(userRow);

        // =====================================================
        // COMMENT
        // =====================================================

        TextView comment =
                new TextView(
                        requireContext()
                );

        comment.setText(
                TextUtils.isEmpty(reviewText)
                        ? "No comment."
                        : reviewText
        );

        comment.setTextColor(
                Color.rgb(70, 70, 70)
        );

        comment.setTextSize(14);

        comment.setLineSpacing(
                dpToPx(2),
                1.0f
        );

        LinearLayout.LayoutParams commentParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        commentParams.topMargin =
                dpToPx(10);

        commentParams.leftMargin =
                dpToPx(58);

        reviewLayout.addView(
                comment,
                commentParams
        );

        // =====================================================
        // EDIT + DELETE
        // ONLY REVIEW OWNER OR ADMIN
        // =====================================================

        FirebaseUser currentUser =
                auth.getCurrentUser();

        if (currentUser != null) {

            boolean isOwner =
                    !TextUtils.isEmpty(buyerId)
                            && currentUser.getUid().equals(buyerId);

            checkAdminAndShowActions(
                    currentUser,
                    isOwner,
                    reviewLayout,
                    reviewDocument,
                    rating,
                    reviewText
            );
        }

        // =====================================================
        // DIVIDER
        // =====================================================

        View divider =
                new View(
                        requireContext()
                );

        divider.setBackgroundColor(
                Color.rgb(230, 230, 230)
        );

        LinearLayout.LayoutParams dividerParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dpToPx(1)
                );

        dividerParams.topMargin =
                dpToPx(8);

        reviewLayout.addView(
                divider,
                dividerParams
        );

        reviewsContainer.addView(
                reviewLayout
        );
    }

    // =========================================================
    // CHECK ADMIN
    // =========================================================

    private void checkAdminAndShowActions(
            FirebaseUser currentUser,
            boolean isOwner,
            LinearLayout reviewLayout,
            DocumentSnapshot reviewDocument,
            double rating,
            String reviewText) {

        db.collection("users")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (!isAdded()) {
                        return;
                    }

                    boolean isAdmin = false;

                    if (documentSnapshot.exists()) {

                        String role =
                                documentSnapshot.getString("role");

                        if (role != null
                                && role.equalsIgnoreCase("admin")) {

                            isAdmin = true;
                        }
                    }

                    if (isOwner || isAdmin) {

                        addReviewActionButtons(
                                reviewLayout,
                                reviewDocument,
                                rating,
                                reviewText
                        );
                    }
                });
    }

    // =========================================================
    // ADD EDIT + DELETE BUTTONS
    // =========================================================

    private void addReviewActionButtons(
            LinearLayout reviewLayout,
            DocumentSnapshot reviewDocument,
            double oldRating,
            String oldReview) {

        LinearLayout actionLayout =
                new LinearLayout(
                        requireContext()
                );

        actionLayout.setOrientation(
                LinearLayout.HORIZONTAL
        );

        actionLayout.setGravity(
                Gravity.END
        );

        LinearLayout.LayoutParams actionParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        actionParams.topMargin =
                dpToPx(8);

        // =====================================================
        // EDIT BUTTON
        // =====================================================

        AppCompatButton editButton =
                new AppCompatButton(
                        requireContext()
                );

        editButton.setText("Edit");
        editButton.setTextSize(13);
        editButton.setTextColor(
                Color.rgb(76, 175, 80)
        );

        editButton.setAllCaps(false);

        editButton.setBackgroundColor(
                Color.TRANSPARENT
        );

        editButton.setOnClickListener(
                v -> showEditReviewDialog(
                        reviewDocument,
                        oldRating,
                        oldReview
                )
        );

        LinearLayout.LayoutParams editParams =
                new LinearLayout.LayoutParams(
                        dpToPx(75),
                        dpToPx(45)
                );

        actionLayout.addView(
                editButton,
                editParams
        );

        // =====================================================
        // DELETE BUTTON
        // =====================================================

        AppCompatButton deleteButton =
                new AppCompatButton(
                        requireContext()
                );

        deleteButton.setText("Delete");
        deleteButton.setTextSize(13);

        deleteButton.setTextColor(
                Color.rgb(211, 47, 47)
        );

        deleteButton.setAllCaps(false);

        deleteButton.setBackgroundColor(
                Color.TRANSPARENT
        );

        deleteButton.setOnClickListener(
                v -> showDeleteReviewDialog(
                        reviewDocument
                )
        );

        LinearLayout.LayoutParams deleteParams =
                new LinearLayout.LayoutParams(
                        dpToPx(85),
                        dpToPx(45)
                );

        actionLayout.addView(
                deleteButton,
                deleteParams
        );

        reviewLayout.addView(
                actionLayout,
                actionParams
        );
    }

    // =========================================================
    // EDIT REVIEW DIALOG
    // =========================================================

    private void showEditReviewDialog(
            DocumentSnapshot reviewDocument,
            double oldRating,
            String oldReview) {

        if (!isAdded()) {
            return;
        }

        LinearLayout layout =
                new LinearLayout(
                        requireContext()
                );

        layout.setOrientation(
                LinearLayout.VERTICAL
        );

        layout.setPadding(
                dpToPx(20),
                dpToPx(10),
                dpToPx(20),
                dpToPx(5)
        );

        RatingBar ratingBar =
                new RatingBar(
                        requireContext()
                );

        ratingBar.setNumStars(5);
        ratingBar.setStepSize(1.0f);

        ratingBar.setRating(
                (float) oldRating
        );

        LinearLayout.LayoutParams ratingParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        ratingParams.gravity =
                Gravity.CENTER;

        layout.addView(
                ratingBar,
                ratingParams
        );

        EditText editText =
                new EditText(
                        requireContext()
                );

        editText.setText(oldReview);

        editText.setHint(
                "Write your comment..."
        );

        editText.setGravity(
                Gravity.TOP
        );

        editText.setMinLines(4);
        editText.setMaxLines(6);

        editText.setInputType(
                InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                        | InputType.TYPE_TEXT_FLAG_MULTI_LINE
        );

        LinearLayout.LayoutParams textParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dpToPx(120)
                );

        textParams.topMargin =
                dpToPx(15);

        layout.addView(
                editText,
                textParams
        );

        AlertDialog dialog =
                new AlertDialog.Builder(
                        requireContext()
                )
                        .setTitle("Edit Review")
                        .setView(layout)
                        .setNegativeButton(
                                "Cancel",
                                null
                        )
                        .setPositiveButton(
                                "Save",
                                null
                        )
                        .create();

        dialog.setOnShowListener(
                dialogInterface -> {

                    android.widget.Button saveButton =
                            dialog.getButton(
                                    AlertDialog.BUTTON_POSITIVE
                            );

                    saveButton.setOnClickListener(
                            v -> {

                                float rating =
                                        ratingBar.getRating();

                                String text =
                                        editText.getText()
                                                .toString()
                                                .trim();

                                if (rating <= 0) {

                                    Toast.makeText(
                                            requireContext(),
                                            "Please select rating.",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    return;
                                }

                                if (text.length() > 300) {

                                    editText.setError(
                                            "Maximum 300 characters."
                                    );

                                    return;
                                }

                                FirebaseUser user =
                                        auth.getCurrentUser();

                                if (user == null) {
                                    return;
                                }

                                String reviewBuyerId =
                                        reviewDocument.getString(
                                                "buyerId"
                                        );

                                if (reviewBuyerId == null) {

                                    Toast.makeText(
                                            requireContext(),
                                            "Review owner not found.",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    return;
                                }

                                boolean isOwner =
                                        user.getUid().equals(
                                                reviewBuyerId
                                        );

                                if (isOwner) {

                                    updateReview(
                                            dialog,
                                            reviewDocument,
                                            rating,
                                            text
                                    );

                                } else {

                                    checkAdminForEdit(
                                            user,
                                            dialog,
                                            reviewDocument,
                                            rating,
                                            text
                                    );
                                }
                            }
                    );
                }
        );

        dialog.show();
    }

    // =========================================================
    // CHECK ADMIN FOR EDIT
    // =========================================================

    private void checkAdminForEdit(
            FirebaseUser user,
            AlertDialog dialog,
            DocumentSnapshot reviewDocument,
            float rating,
            String text) {

        db.collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (!isAdded()) {
                        return;
                    }

                    String role =
                            documentSnapshot.getString("role");

                    if (role != null
                            && role.equalsIgnoreCase("admin")) {

                        updateReview(
                                dialog,
                                reviewDocument,
                                rating,
                                text
                        );

                    } else {

                        Toast.makeText(
                                requireContext(),
                                "You can only edit your own review.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    // =========================================================
    // UPDATE REVIEW
    // =========================================================

    private void updateReview(
            AlertDialog dialog,
            DocumentSnapshot reviewDocument,
            float rating,
            String text) {

        Map<String, Object> update =
                new HashMap<>();

        update.put(
                "rating",
                rating
        );

        update.put(
                "review",
                text
        );

        update.put(
                "updatedAt",
                Timestamp.now()
        );

        db.collection("reviews")
                .document(
                        reviewDocument.getId()
                )
                .update(update)
                .addOnSuccessListener(
                        unused -> {

                            if (!isAdded()) {
                                return;
                            }

                            dialog.dismiss();

                            String productId =
                                    getStringValue(
                                            selectedProduct,
                                            "productId",
                                            ""
                                    );

                            loadProductReviews(
                                    productId
                            );

                            Toast.makeText(
                                    requireContext(),
                                    "Review updated successfully.",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                )
                .addOnFailureListener(
                        e -> {

                            if (!isAdded()) {
                                return;
                            }

                            Toast.makeText(
                                    requireContext(),
                                    "Failed to update review: "
                                            + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }

    // =========================================================
    // DELETE REVIEW CONFIRMATION
    // =========================================================

    private void showDeleteReviewDialog(
            DocumentSnapshot reviewDocument) {

        if (!isAdded()) {
            return;
        }

        new AlertDialog.Builder(
                requireContext()
        )
                .setTitle("Delete Review")
                .setMessage(
                        "Are you sure you want to delete this review?"
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .setPositiveButton(
                        "Delete",
                        (dialog, which) ->
                                deleteReview(
                                        reviewDocument
                                )
                )
                .show();
    }

    // =========================================================
    // DELETE REVIEW
    // =========================================================

    private void deleteReview(
            DocumentSnapshot reviewDocument) {

        FirebaseUser user =
                auth.getCurrentUser();

        if (user == null) {

            Toast.makeText(
                    requireContext(),
                    "Please login first.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String buyerId =
                reviewDocument.getString(
                        "buyerId"
                );

        if (buyerId == null) {

            Toast.makeText(
                    requireContext(),
                    "Review owner not found.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (user.getUid().equals(buyerId)) {

            performDeleteReview(
                    reviewDocument
            );

            return;
        }

        db.collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (!isAdded()) {
                        return;
                    }

                    String role =
                            documentSnapshot.getString(
                                    "role"
                            );

                    if (role != null
                            && role.equalsIgnoreCase("admin")) {

                        performDeleteReview(
                                reviewDocument
                        );

                    } else {

                        Toast.makeText(
                                requireContext(),
                                "You can only delete your own review.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                requireContext(),
                                "Unable to verify permission.",
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }

    // =========================================================
    // PERFORM DELETE
    // =========================================================

    private void performDeleteReview(
            DocumentSnapshot reviewDocument) {

        db.collection("reviews")
                .document(
                        reviewDocument.getId()
                )
                .delete()
                .addOnSuccessListener(
                        unused -> {

                            if (!isAdded()) {
                                return;
                            }

                            String productId =
                                    getStringValue(
                                            selectedProduct,
                                            "productId",
                                            ""
                                    );

                            loadProductReviews(
                                    productId
                            );

                            Toast.makeText(
                                    requireContext(),
                                    "Review deleted successfully.",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                )
                .addOnFailureListener(
                        e -> {

                            if (!isAdded()) {
                                return;
                            }

                            Toast.makeText(
                                    requireContext(),
                                    "Failed to delete review: "
                                            + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }

    // =========================================================
    // NO REVIEWS
    // =========================================================

    private void showNoReviews() {

        if (!isAdded()) {
            return;
        }

        reviewsContainer.removeAllViews();

        tvReviewsCount.setText("0");

        tvNoReviews.setVisibility(
                View.VISIBLE
        );
    }

    // =========================================================
    // ADD PRODUCT TO CART
    // =========================================================

    private void addProductToCart(
            Map<String, Object> product) {

        FirebaseUser currentUser =
                auth.getCurrentUser();

        if (currentUser == null) {

            Toast.makeText(
                    requireContext(),
                    "Please login first",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String productId =
                getStringValue(
                        product,
                        "productId",
                        ""
                );

        if (productId.isEmpty()) {

            Toast.makeText(
                    requireContext(),
                    "Product ID not found",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String productName =
                getStringValue(
                        product,
                        "name",
                        "Product"
                );

        String sellerId =
                getStringValue(
                        product,
                        "sellerId",
                        ""
                );

        String sellerName =
                getStringValue(
                        product,
                        "sellerName",
                        ""
                );

        String sellerPhone =
                getStringValue(
                        product,
                        "sellerPhone",
                        ""
                );

        String categoryId =
                getStringValue(
                        product,
                        "categoryId",
                        ""
                );

        String categoryName =
                getStringValue(
                        product,
                        "categoryName",
                        ""
                );

        String description =
                getStringValue(
                        product,
                        "description",
                        ""
                );

        double price =
                getDouble(product.get("price"));

        db.collection("cart")
                .document(currentUser.getUid())
                .collection("items")
                .document(productId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (!isAdded()) {
                        return;
                    }

                    if (documentSnapshot.exists()) {

                        Long oldQuantity =
                                documentSnapshot.getLong(
                                        "quantity"
                                );

                        int quantity = 1;

                        if (oldQuantity != null
                                && oldQuantity > 0) {

                            quantity =
                                    oldQuantity.intValue();
                        }

                        quantity++;

                        db.collection("cart")
                                .document(
                                        currentUser.getUid()
                                )
                                .collection("items")
                                .document(productId)
                                .update(
                                        "quantity",
                                        quantity
                                )
                                .addOnSuccessListener(
                                        unused -> Toast.makeText(
                                                requireContext(),
                                                "Product added to cart",
                                                Toast.LENGTH_SHORT
                                        ).show()
                                )
                                .addOnFailureListener(
                                        e -> showError(
                                                "Failed to update cart",
                                                e
                                        )
                                );

                        return;
                    }

                    Map<String, Object> cartItem =
                            new HashMap<>();

                    cartItem.put(
                            "productId",
                            productId
                    );

                    cartItem.put(
                            "name",
                            productName
                    );

                    cartItem.put(
                            "sellerId",
                            sellerId
                    );

                    cartItem.put(
                            "sellerName",
                            sellerName
                    );

                    cartItem.put(
                            "sellerPhone",
                            sellerPhone
                    );

                    cartItem.put(
                            "categoryId",
                            categoryId
                    );

                    cartItem.put(
                            "categoryName",
                            categoryName
                    );

                    cartItem.put(
                            "description",
                            description
                    );

                    cartItem.put(
                            "price",
                            price
                    );

                    cartItem.put(
                            "quantity",
                            1
                    );

                    cartItem.put(
                            "addedAt",
                            System.currentTimeMillis()
                    );

                    db.collection("cart")
                            .document(
                                    currentUser.getUid()
                            )
                            .collection("items")
                            .document(productId)
                            .set(cartItem)
                            .addOnSuccessListener(
                                    unused -> Toast.makeText(
                                            requireContext(),
                                            "Product added to cart",
                                            Toast.LENGTH_SHORT
                                    ).show()
                            )
                            .addOnFailureListener(
                                    e -> showError(
                                            "Failed to add product to cart",
                                            e
                                    )
                            );
                })
                .addOnFailureListener(
                        e -> showError(
                                "Failed to access cart",
                                e
                        )
                );
    }

    // =========================================================
    // BUY NOW -> CHECKOUT
    // =========================================================

    private void openCheckout() {

        if (!isAdded() || selectedProduct == null) {
            return;
        }

        String productId =
                getStringValue(
                        selectedProduct,
                        "productId",
                        ""
                );

        if (productId.isEmpty()) {

            Toast.makeText(
                    requireContext(),
                    "Product ID not found",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String productName =
                getStringValue(
                        selectedProduct,
                        "name",
                        "Product"
                );

        String sellerId =
                getStringValue(
                        selectedProduct,
                        "sellerId",
                        ""
                );

        String sellerName =
                getStringValue(
                        selectedProduct,
                        "sellerName",
                        "Seller"
                );

        double price =
                getDouble(
                        selectedProduct.get("price")
                );

        BuyerCheckoutFragment checkoutFragment =
                BuyerCheckoutFragment.newInstance(
                        productId,
                        productName,
                        sellerId,
                        sellerName,
                        "1",
                        formatPriceNumber(price)
                );

        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .replace(
                        R.id.fragment_container,
                        checkoutFragment
                )
                .addToBackStack(null)
                .commit();
    }

    // =========================================================
    // PRODUCT RATING
    // =========================================================

    private double getProductRating(
            Map<String, Object> product) {

        Object rating =
                product.get("rating");

        if (rating != null) {
            return getDouble(rating);
        }

        rating =
                product.get("averageRating");

        if (rating != null) {
            return getDouble(rating);
        }

        rating =
                product.get("avgRating");

        if (rating != null) {
            return getDouble(rating);
        }

        return 0.0;
    }

    // =========================================================
    // TIMESTAMP
    // =========================================================

    private long getTimestampMillis(
            Object value) {

        if (value instanceof Timestamp) {

            return ((Timestamp) value)
                    .toDate()
                    .getTime();
        }

        if (value instanceof Date) {

            return ((Date) value)
                    .getTime();
        }

        if (value instanceof Number) {

            return ((Number) value)
                    .longValue();
        }

        return 0L;
    }

    // =========================================================
    // REVIEW DATE
    // =========================================================

    private String getReviewDate(
            Map<String, Object> review) {

        Object value =
                review.get("updatedAt");

        long time =
                getTimestampMillis(value);

        if (time == 0) {

            time =
                    getTimestampMillis(
                            review.get("createdAt")
                    );
        }

        if (time == 0) {
            return "";
        }

        return new SimpleDateFormat(
                "dd MMM yyyy",
                Locale.getDefault()
        ).format(
                new Date(time)
        );
    }

    // =========================================================
    // GET STRING
    // =========================================================

    private String getStringValue(
            Map<String, Object> data,
            String key,
            String defaultValue) {

        if (data == null) {
            return defaultValue;
        }

        Object value =
                data.get(key);

        if (value == null) {
            return defaultValue;
        }

        String result =
                String.valueOf(value).trim();

        if (TextUtils.isEmpty(result)) {
            return defaultValue;
        }

        return result;
    }

    // =========================================================
    // GET DOUBLE
    // =========================================================

    private double getDouble(
            Object value) {

        if (value instanceof Number) {

            return ((Number) value)
                    .doubleValue();
        }

        if (value != null) {

            try {

                return Double.parseDouble(
                        String.valueOf(value)
                );

            } catch (Exception ignored) {
            }
        }

        return 0;
    }

    // =========================================================
    // FORMAT RATING
    // =========================================================

    private String formatRating(
            double rating) {

        if (rating < 0) {
            rating = 0;
        }

        if (rating > 5) {
            rating = 5;
        }

        return String.format(
                Locale.getDefault(),
                "%.1f",
                rating
        );
    }

    // =========================================================
    // FORMAT PRICE NUMBER
    // =========================================================

    private String formatPriceNumber(
            double price) {

        if (price == Math.floor(price)) {

            return String.valueOf(
                    (long) price
            );
        }

        return String.format(
                Locale.getDefault(),
                "%.2f",
                price
        );
    }

    // =========================================================
    // FORMAT PRICE
    // =========================================================

    private String formatPrice(
            double price) {

        if (price == Math.floor(price)) {

            return "Rs " + (long) price;
        }

        return "Rs "
                + String.format(
                Locale.getDefault(),
                "%.2f",
                price
        );
    }

    // =========================================================
    // DP TO PX
    // =========================================================

    private int dpToPx(int dp) {

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
    // ERROR
    // =========================================================

    private void showError(
            String message,
            Exception e) {

        if (!isAdded()) {
            return;
        }

        String error =
                e != null && e.getMessage() != null
                        ? e.getMessage()
                        : "Unknown error";

        Toast.makeText(
                requireContext(),
                message + ": " + error,
                Toast.LENGTH_LONG
        ).show();
    }
}