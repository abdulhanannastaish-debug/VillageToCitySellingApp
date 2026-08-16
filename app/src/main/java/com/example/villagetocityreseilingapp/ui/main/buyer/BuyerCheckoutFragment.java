package com.example.villagetocityreseilingapp.ui.main.buyer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BuyerCheckoutFragment extends Fragment {

    // =========================================================
    // FIREBASE
    // =========================================================

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    // =========================================================
    // VIEWS
    // =========================================================

    private ImageButton btnCheckoutBack;

    private ImageView checkoutProductImage;

    private TextView checkoutProductName;
    private TextView checkoutProductSeller;
    private TextView checkoutProductQuantity;
    private TextView checkoutProductPrice;

    private TextView btnDecreaseQuantity;
    private TextView btnIncreaseQuantity;

    private EditText etDeliveryName;
    private EditText etDeliveryAddress;
    private EditText etDeliveryPhone;

    private RadioGroup radioDeliveryService;
    private RadioButton radioTcs;
    private RadioButton radioLeopards;

    private TextView txtSummaryPrice;
    private TextView txtSummaryDelivery;
    private TextView txtTotalAmount;
    private TextView txtDeliveryServiceName;

    private AppCompatButton btnPlaceOrder;

    // =========================================================
    // PRODUCT DATA
    // =========================================================

    private String productId = "";
    private String productName = "";
    private String sellerId = "";
    private String sellerName = "";

    private int quantity = 1;

    private double unitPrice = 0;

    private String productImageUrl = "";

    // =========================================================
    // CURRENT STOCK
    // =========================================================

    private int availableStock = 0;

    // =========================================================
    // BUYER DATA
    // =========================================================

    private String buyerName = "";

    // =========================================================
    // DELIVERY
    // =========================================================

    private String deliveryService = "TCS";
    private double deliveryCharges = 200;

    private static final String TCS = "TCS";
    private static final String LEOPARDS = "Leopards Courier";

    private static final double TCS_CHARGES = 200;
    private static final double LEOPARDS_CHARGES = 180;

    // =========================================================
    // IMAGE LOADING
    // =========================================================

    private final ExecutorService imageExecutor =
            Executors.newSingleThreadExecutor();

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public BuyerCheckoutFragment() {
    }

    // =========================================================
    // NEW INSTANCE
    // =========================================================

    public static BuyerCheckoutFragment newInstance(
            String productId,
            String productName,
            String sellerId,
            String sellerName,
            String quantity,
            String amount) {

        BuyerCheckoutFragment fragment =
                new BuyerCheckoutFragment();

        Bundle args = new Bundle();

        args.putString("productId", productId);
        args.putString("productName", productName);
        args.putString("sellerId", sellerId);
        args.putString("sellerName", sellerName);
        args.putString("quantity", quantity);
        args.putString("amount", amount);

        fragment.setArguments(args);

        return fragment;
    }

    // =========================================================
    // ON CREATE
    // =========================================================

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (getArguments() != null) {

            productId =
                    getArguments().getString(
                            "productId",
                            ""
                    );

            productName =
                    getArguments().getString(
                            "productName",
                            ""
                    );

            sellerId =
                    getArguments().getString(
                            "sellerId",
                            ""
                    );

            sellerName =
                    getArguments().getString(
                            "sellerName",
                            ""
                    );

            String quantityString =
                    getArguments().getString(
                            "quantity",
                            "1"
                    );

            String amountString =
                    getArguments().getString(
                            "amount",
                            "0"
                    );

            quantity =
                    parseIntSafe(
                            quantityString,
                            1
                    );

            if (quantity < 1) {
                quantity = 1;
            }

            unitPrice =
                    parseDoubleSafe(
                            amountString,
                            0
                    );
        }

        deliveryService = TCS;
        deliveryCharges = TCS_CHARGES;
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
                R.layout.fragment_buyer_checkout,
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

        btnCheckoutBack =
                view.findViewById(
                        R.id.btnCheckoutBack
                );

        checkoutProductImage =
                view.findViewById(
                        R.id.checkoutProductImage
                );

        checkoutProductName =
                view.findViewById(
                        R.id.checkoutProductName
                );

        checkoutProductSeller =
                view.findViewById(
                        R.id.checkoutProductSeller
                );

        checkoutProductQuantity =
                view.findViewById(
                        R.id.checkoutProductQuantity
                );

        checkoutProductPrice =
                view.findViewById(
                        R.id.checkoutProductPrice
                );

        btnDecreaseQuantity =
                view.findViewById(
                        R.id.btnDecreaseQuantity
                );

        btnIncreaseQuantity =
                view.findViewById(
                        R.id.btnIncreaseQuantity
                );

        etDeliveryName =
                view.findViewById(
                        R.id.etBuyerName
                );

        etDeliveryAddress =
                view.findViewById(
                        R.id.etDeliveryAddress
                );

        etDeliveryPhone =
                view.findViewById(
                        R.id.etDeliveryPhone
                );

        radioDeliveryService =
                view.findViewById(
                        R.id.radioDeliveryService
                );

        radioTcs =
                view.findViewById(
                        R.id.radioTcs
                );

        radioLeopards =
                view.findViewById(
                        R.id.radioLeopards
                );

        txtSummaryPrice =
                view.findViewById(
                        R.id.txtSummaryPrice
                );

        txtSummaryDelivery =
                view.findViewById(
                        R.id.txtSummaryDelivery
                );

        txtTotalAmount =
                view.findViewById(
                        R.id.txtTotalAmount
                );

        txtDeliveryServiceName =
                view.findViewById(
                        R.id.txtDeliveryServiceName
                );

        btnPlaceOrder =
                view.findViewById(
                        R.id.btnPlaceOrder
                );

        // =====================================================
        // INITIAL PRODUCT DATA
        // =====================================================

        checkoutProductName.setText(
                getSafeValue(
                        productName,
                        "Product"
                )
        );

        checkoutProductSeller.setText(
                getSafeValue(
                        sellerName,
                        "Seller"
                )
        );

        updateQuantityUI();

        // =====================================================
        // LOAD BUYER
        // =====================================================

        loadBuyerProfile();

        // =====================================================
        // LOAD PRODUCT
        // =====================================================

        loadProductData();

        // =====================================================
        // DEFAULT DELIVERY
        // =====================================================

        if (radioTcs != null) {
            radioTcs.setChecked(true);
        }

        deliveryService = TCS;
        deliveryCharges = TCS_CHARGES;

        updateOrderSummary();

        // =====================================================
        // INCREASE
        // =====================================================

        btnIncreaseQuantity.setOnClickListener(v -> {

            if (availableStock > 0 &&
                    quantity < availableStock) {

                quantity++;

                updateQuantityUI();
                updateOrderSummary();

            } else {

                Toast.makeText(
                        requireContext(),
                        availableStock <= 0
                                ? "Product is out of stock."
                                : "Only "
                                + availableStock
                                + " items available.",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        // =====================================================
        // DECREASE
        // =====================================================

        btnDecreaseQuantity.setOnClickListener(v -> {

            if (quantity > 1) {

                quantity--;

                updateQuantityUI();
                updateOrderSummary();

            } else {

                Toast.makeText(
                        requireContext(),
                        "Minimum quantity is 1.",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        // =====================================================
        // DELIVERY SERVICE
        // =====================================================

        radioDeliveryService.setOnCheckedChangeListener(
                (group, checkedId) -> {

                    if (!isAdded()) {
                        return;
                    }

                    if (checkedId == R.id.radioTcs) {

                        deliveryService = TCS;
                        deliveryCharges = TCS_CHARGES;

                    } else if (
                            checkedId == R.id.radioLeopards) {

                        deliveryService = LEOPARDS;
                        deliveryCharges = LEOPARDS_CHARGES;
                    }

                    updateOrderSummary();
                }
        );

        // =====================================================
        // BACK
        // =====================================================

        btnCheckoutBack.setOnClickListener(v -> {

            if (!isAdded()) {
                return;
            }

            requireActivity()
                    .getSupportFragmentManager()
                    .popBackStack();
        });

        // =====================================================
        // PLACE ORDER
        // =====================================================

        btnPlaceOrder.setOnClickListener(
                v -> confirmOrder()
        );
    }

    // =========================================================
    // LOAD BUYER PROFILE
    // =========================================================

    private void loadBuyerProfile() {

        if (!isAdded()) {
            return;
        }

        FirebaseUser currentUser =
                auth.getCurrentUser();

        if (currentUser == null) {
            return;
        }

        String authName =
                currentUser.getDisplayName();

        if (!TextUtils.isEmpty(authName)) {

            buyerName =
                    authName.trim();

            if (etDeliveryName != null) {

                etDeliveryName.setText(
                        buyerName
                );
            }

            return;
        }

        String buyerId =
                currentUser.getUid();

        db.collection("users")
                .document(buyerId)
                .get()
                .addOnSuccessListener(
                        documentSnapshot -> {

                            if (!isAdded()) {
                                return;
                            }

                            if (!documentSnapshot.exists()) {
                                return;
                            }

                            String name =
                                    getFirstAvailableString(
                                            documentSnapshot,
                                            "name",
                                            "fullName",
                                            "displayName",
                                            "username"
                                    );

                            if (!TextUtils.isEmpty(name)) {

                                buyerName =
                                        name.trim();

                                if (etDeliveryName != null) {

                                    etDeliveryName.setText(
                                            buyerName
                                    );
                                }
                            }

                            String phone =
                                    getFirstAvailableString(
                                            documentSnapshot,
                                            "phone",
                                            "phoneNumber",
                                            "mobile"
                                    );

                            if (!TextUtils.isEmpty(phone)
                                    && etDeliveryPhone != null) {

                                if (TextUtils.isEmpty(
                                        etDeliveryPhone
                                                .getText()
                                                .toString()
                                                .trim()
                                )) {

                                    etDeliveryPhone.setText(
                                            phone
                                    );
                                }
                            }

                            String address =
                                    getFirstAvailableString(
                                            documentSnapshot,
                                            "address",
                                            "deliveryAddress"
                                    );

                            if (!TextUtils.isEmpty(address)
                                    && etDeliveryAddress != null) {

                                if (TextUtils.isEmpty(
                                        etDeliveryAddress
                                                .getText()
                                                .toString()
                                                .trim()
                                )) {

                                    etDeliveryAddress.setText(
                                            address
                                    );
                                }
                            }
                        }
                );
    }

    // =========================================================
    // LOAD PRODUCT DATA
    // =========================================================

    private void loadProductData() {

        if (TextUtils.isEmpty(productId)) {
            return;
        }

        db.collection("products")
                .document(productId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (!isAdded()) {
                        return;
                    }

                    if (!documentSnapshot.exists()) {

                        Toast.makeText(
                                requireContext(),
                                "Product no longer exists.",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }

                    String firestoreName =
                            documentSnapshot.getString("name");

                    if (!TextUtils.isEmpty(firestoreName)) {

                        productName =
                                firestoreName;

                        checkoutProductName.setText(
                                productName
                        );
                    }

                    String firestoreSellerId =
                            documentSnapshot.getString("sellerId");

                    if (!TextUtils.isEmpty(
                            firestoreSellerId
                    )) {

                        sellerId =
                                firestoreSellerId;
                    }

                    String firestoreSellerName =
                            documentSnapshot.getString("sellerName");

                    if (!TextUtils.isEmpty(
                            firestoreSellerName
                    )) {

                        sellerName =
                                firestoreSellerName;

                        checkoutProductSeller.setText(
                                sellerName
                        );
                    }

                    Object priceObject =
                            documentSnapshot.get("price");

                    if (priceObject != null) {

                        unitPrice =
                                parseObjectDouble(
                                        priceObject,
                                        unitPrice
                                );
                    }

                    // =================================================
                    // CURRENT FIRESTORE STOCK
                    // =================================================

                    availableStock =
                            getIntFromFirestore(
                                    documentSnapshot,
                                    "totalStock"
                            );

                    if (availableStock <= 0) {

                        quantity = 0;

                        updateQuantityUI();

                        Toast.makeText(
                                requireContext(),
                                "Product is out of stock.",
                                Toast.LENGTH_SHORT
                        ).show();

                    } else {

                        if (quantity > availableStock) {

                            quantity =
                                    availableStock;

                            Toast.makeText(
                                    requireContext(),
                                    "Available stock is only "
                                            + availableStock
                                            + ".",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }

                        if (quantity < 1) {
                            quantity = 1;
                        }

                        updateQuantityUI();
                    }

                    updateOrderSummary();

                    productImageUrl =
                            getImageUrl(
                                    documentSnapshot
                            );

                    if (!TextUtils.isEmpty(
                            productImageUrl
                    )) {

                        loadImageFromUrl(
                                productImageUrl
                        );
                    }
                })
                .addOnFailureListener(e -> {

                    if (!isAdded()) {
                        return;
                    }

                    Toast.makeText(
                            requireContext(),
                            "Unable to load product stock.",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }

    // =========================================================
    // GET STOCK
    // =========================================================

    private int getIntFromFirestore(
            DocumentSnapshot document,
            String field) {

        Object value =
                document.get(field);

        if (value instanceof Number) {

            return ((Number) value).intValue();
        }

        if (value != null) {

            try {

                return Integer.parseInt(
                        String.valueOf(value)
                );

            } catch (Exception ignored) {
            }

            try {

                return (int) Double.parseDouble(
                        String.valueOf(value)
                );

            } catch (Exception ignored) {
            }
        }

        return 0;
    }

    // =========================================================
    // IMAGE URL
    // =========================================================

    private String getImageUrl(
            DocumentSnapshot document) {

        String imageUrl =
                document.getString("imageUrl");

        if (!TextUtils.isEmpty(imageUrl)) {
            return imageUrl;
        }

        imageUrl =
                document.getString("imageURL");

        if (!TextUtils.isEmpty(imageUrl)) {
            return imageUrl;
        }

        imageUrl =
                document.getString("image");

        if (!TextUtils.isEmpty(imageUrl)) {
            return imageUrl;
        }

        imageUrl =
                document.getString("productImage");

        if (!TextUtils.isEmpty(imageUrl)) {
            return imageUrl;
        }

        return getSafeValue(
                document.getString("imageUri"),
                ""
        );
    }

    // =========================================================
    // LOAD IMAGE
    // =========================================================

    private void loadImageFromUrl(
            String imageUrl) {

        if (TextUtils.isEmpty(imageUrl)) {
            return;
        }

        imageExecutor.execute(() -> {

            try {

                URL url =
                        new URL(imageUrl);

                HttpURLConnection connection =
                        (HttpURLConnection)
                                url.openConnection();

                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                connection.setDoInput(true);

                connection.connect();

                InputStream inputStream =
                        connection.getInputStream();

                Bitmap bitmap =
                        BitmapFactory.decodeStream(
                                inputStream
                        );

                inputStream.close();
                connection.disconnect();

                if (bitmap == null) {
                    return;
                }

                if (getActivity() == null) {
                    return;
                }

                requireActivity().runOnUiThread(() -> {

                    if (!isAdded()) {
                        return;
                    }

                    checkoutProductImage.setImageBitmap(
                            bitmap
                    );
                });

            } catch (Exception ignored) {
            }
        });
    }

    // =========================================================
    // QUANTITY UI
    // =========================================================

    private void updateQuantityUI() {

        if (checkoutProductQuantity != null) {

            checkoutProductQuantity.setText(
                    String.valueOf(quantity)
            );
        }

        double productTotal =
                unitPrice * quantity;

        if (checkoutProductPrice != null) {

            checkoutProductPrice.setText(
                    "Rs " +
                            formatAmount(productTotal)
            );
        }

        updateQuantityButtons();
    }

    // =========================================================
    // QUANTITY BUTTON STATE
    // =========================================================

    private void updateQuantityButtons() {

        if (btnDecreaseQuantity != null) {

            btnDecreaseQuantity.setEnabled(
                    quantity > 1
            );
        }

        if (btnIncreaseQuantity != null) {

            btnIncreaseQuantity.setEnabled(
                    availableStock > 0
                            && quantity < availableStock
            );
        }

        if (btnPlaceOrder != null) {

            btnPlaceOrder.setEnabled(
                    availableStock > 0
                            && quantity > 0
            );
        }
    }

    // =========================================================
    // SUMMARY
    // =========================================================

    private void updateOrderSummary() {

        double productTotal =
                unitPrice * quantity;

        double totalAmount =
                productTotal +
                        deliveryCharges;

        if (txtSummaryPrice != null) {

            txtSummaryPrice.setText(
                    "Rs " +
                            formatAmount(productTotal)
            );
        }

        if (txtSummaryDelivery != null) {

            txtSummaryDelivery.setText(
                    "Rs " +
                            formatAmount(deliveryCharges)
            );
        }

        if (txtDeliveryServiceName != null) {

            txtDeliveryServiceName.setText(
                    deliveryService +
                            " Delivery Charges"
            );
        }

        if (txtTotalAmount != null) {

            txtTotalAmount.setText(
                    "Rs " +
                            formatAmount(totalAmount)
            );
        }
    }

    // =========================================================
    // CONFIRM ORDER
    // =========================================================

    private void confirmOrder() {

        if (!isAdded()) {
            return;
        }

        FirebaseUser currentUser =
                auth.getCurrentUser();

        if (currentUser == null) {

            Toast.makeText(
                    requireContext(),
                    "Please login first.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (TextUtils.isEmpty(productId)) {

            Toast.makeText(
                    requireContext(),
                    "Product ID not found.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (TextUtils.isEmpty(sellerId)) {

            Toast.makeText(
                    requireContext(),
                    "Seller information not found.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // =====================================================
        // BUYER NAME
        // =====================================================

        String enteredBuyerName =
                etDeliveryName
                        .getText()
                        .toString()
                        .trim();

        if (TextUtils.isEmpty(enteredBuyerName)) {

            etDeliveryName.setError(
                    "Enter your name"
            );

            etDeliveryName.requestFocus();

            return;
        }

        buyerName =
                enteredBuyerName;

        // =====================================================
        // ADDRESS
        // =====================================================

        String deliveryAddress =
                etDeliveryAddress
                        .getText()
                        .toString()
                        .trim();

        if (TextUtils.isEmpty(deliveryAddress)) {

            etDeliveryAddress.setError(
                    "Enter delivery address"
            );

            etDeliveryAddress.requestFocus();

            return;
        }

        // =====================================================
        // PHONE
        // =====================================================

        String deliveryPhone =
                etDeliveryPhone
                        .getText()
                        .toString()
                        .trim();

        if (TextUtils.isEmpty(deliveryPhone)) {

            etDeliveryPhone.setError(
                    "Enter phone number"
            );

            etDeliveryPhone.requestFocus();

            return;
        }

        if (quantity < 1) {

            Toast.makeText(
                    requireContext(),
                    "Invalid quantity.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // =====================================================
        // DISABLE BUTTON
        // =====================================================

        btnPlaceOrder.setEnabled(false);

        btnPlaceOrder.setText(
                "Checking Stock..."
        );

        // =====================================================
        // IMPORTANT
        //
        // Stock will NOT be deducted here manually.
        //
        // Firestore transaction below will:
        //
        // 1. Read current product stock
        // 2. Check requested quantity
        // 3. Calculate remaining stock
        // 4. Update product stock
        // 5. Create order
        //
        // All atomically.
        // =====================================================

        placeOrderWithStockTransaction(
                currentUser,
                deliveryAddress,
                deliveryPhone
        );
    }

    // =========================================================
    // PLACE ORDER + UPDATE STOCK
    // ATOMIC FIRESTORE TRANSACTION
    // =========================================================

    private void placeOrderWithStockTransaction(
            FirebaseUser currentUser,
            String deliveryAddress,
            String deliveryPhone) {

        if (!isAdded()) {
            return;
        }

        final String buyerId =
                currentUser.getUid();

        final DocumentReference productReference =
                db.collection("products")
                        .document(productId);

        final String orderId =
                generateOrderId();

        final DocumentReference orderReference =
                db.collection("orders")
                        .document();

        final double productTotal =
                unitPrice * quantity;

        final double totalAmount =
                productTotal +
                        deliveryCharges;

        db.runTransaction(transaction -> {

                    // =================================================
                    // READ PRODUCT FIRST
                    // =================================================

                    DocumentSnapshot productSnapshot =
                            transaction.get(
                                    productReference
                            );

                    if (!productSnapshot.exists()) {

                        throw new RuntimeException(
                                "PRODUCT_NOT_FOUND"
                        );
                    }

                    // =================================================
                    // READ CURRENT STOCK FROM FIRESTORE
                    // =================================================

                    int currentStock =
                            getIntFromFirestore(
                                    productSnapshot,
                                    "totalStock"
                            );

                    // =================================================
                    // OUT OF STOCK
                    // =================================================

                    if (currentStock <= 0) {

                        throw new RuntimeException(
                                "OUT_OF_STOCK"
                        );
                    }

                    // =================================================
                    // NOT ENOUGH STOCK
                    // =================================================

                    if (quantity > currentStock) {

                        throw new RuntimeException(
                                "INSUFFICIENT_STOCK:"
                                        + currentStock
                        );
                    }

                    // =================================================
                    // CALCULATE REMAINING STOCK
                    // =================================================

                    int remainingStock =
                            currentStock - quantity;

                    // =================================================
                    // UPDATE PRODUCT STOCK
                    // =================================================

                    transaction.update(
                            productReference,
                            "totalStock",
                            remainingStock
                    );

                    // =================================================
                    // KEEP ORDER DATA IN SYNC WITH REAL PRODUCT
                    // =================================================

                    String firestoreProductName =
                            productSnapshot.getString(
                                    "name"
                            );

                    if (!TextUtils.isEmpty(
                            firestoreProductName
                    )) {

                        productName =
                                firestoreProductName;
                    }

                    String firestoreSellerId =
                            productSnapshot.getString(
                                    "sellerId"
                            );

                    if (!TextUtils.isEmpty(
                            firestoreSellerId
                    )) {

                        sellerId =
                                firestoreSellerId;
                    }

                    String firestoreSellerName =
                            productSnapshot.getString(
                                    "sellerName"
                            );

                    if (!TextUtils.isEmpty(
                            firestoreSellerName
                    )) {

                        sellerName =
                                firestoreSellerName;
                    }

                    Object firestorePrice =
                            productSnapshot.get(
                                    "price"
                            );

                    if (firestorePrice != null) {

                        unitPrice =
                                parseObjectDouble(
                                        firestorePrice,
                                        unitPrice
                                );
                    }

                    String firestoreImage =
                            getImageUrl(
                                    productSnapshot
                            );

                    if (!TextUtils.isEmpty(
                            firestoreImage
                    )) {

                        productImageUrl =
                                firestoreImage;
                    }

                    // =================================================
                    // CREATE ORDER DATA
                    // =================================================

                    Map<String, Object> orderData =
                            new HashMap<>();

                    // =================================================
                    // IDs
                    // =================================================

                    orderData.put(
                            "id",
                            orderId
                    );

                    orderData.put(
                            "orderId",
                            orderId
                    );

                    orderData.put(
                            "buyerId",
                            buyerId
                    );

                    orderData.put(
                            "sellerId",
                            sellerId
                    );

                    orderData.put(
                            "productId",
                            productId
                    );

                    // =================================================
                    // BUYER EMAIL
                    // =================================================

                    orderData.put(
                            "buyerEmail",
                            getSafeValue(
                                    currentUser.getEmail(),
                                    ""
                            )
                    );

                    // =================================================
                    // BUYER NAME
                    // =================================================

                    orderData.put(
                            "customerName",
                            buyerName
                    );

                    orderData.put(
                            "buyerName",
                            buyerName
                    );

                    // =================================================
                    // SELLER
                    // =================================================

                    orderData.put(
                            "sellerName",
                            getSafeValue(
                                    sellerName,
                                    "Seller"
                            )
                    );

                    // =================================================
                    // PRODUCT
                    // =================================================

                    orderData.put(
                            "productName",
                            getSafeValue(
                                    productName,
                                    "Product"
                            )
                    );

                    orderData.put(
                            "productImage",
                            getSafeValue(
                                    productImageUrl,
                                    ""
                            )
                    );

                    // =================================================
                    // QUANTITY
                    // =================================================

                    orderData.put(
                            "quantity",
                            (long) quantity
                    );

                    // =================================================
                    // PRICE
                    // =================================================

                    orderData.put(
                            "unitPrice",
                            unitPrice
                    );

                    orderData.put(
                            "amount",
                            productTotal
                    );

                    orderData.put(
                            "productAmount",
                            productTotal
                    );

                    // =================================================
                    // DELIVERY
                    // =================================================

                    orderData.put(
                            "deliveryService",
                            deliveryService
                    );

                    orderData.put(
                            "deliveryCharges",
                            deliveryCharges
                    );

                    // =================================================
                    // TOTAL
                    // =================================================

                    orderData.put(
                            "totalAmount",
                            totalAmount
                    );

                    // =================================================
                    // ADDRESS
                    // =================================================

                    orderData.put(
                            "customerAddress",
                            deliveryAddress
                    );

                    orderData.put(
                            "buyerAddress",
                            deliveryAddress
                    );

                    // =================================================
                    // PHONE
                    // =================================================

                    orderData.put(
                            "customerPhone",
                            deliveryPhone
                    );

                    orderData.put(
                            "buyerPhone",
                            deliveryPhone
                    );

                    // =================================================
                    // PAYMENT
                    // =================================================

                    orderData.put(
                            "paymentMethod",
                            "Cash on Delivery"
                    );

                    orderData.put(
                            "paymentStatus",
                            "pending"
                    );

                    orderData.put(
                            "sellerPaymentStatus",
                            "pending"
                    );

                    // =================================================
                    // STATUS
                    // =================================================

                    orderData.put(
                            "status",
                            "pending"
                    );

                    orderData.put(
                            "orderAge",
                            "new"
                    );

                    // =================================================
                    // STOCK INFO AT ORDER TIME
                    // =================================================

                    orderData.put(
                            "stockBeforeOrder",
                            (long) currentStock
                    );

                    orderData.put(
                            "stockAfterOrder",
                            (long) remainingStock
                    );

                    orderData.put(
                            "stockDeducted",
                            (long) quantity
                    );

                    // =================================================
                    // TIMESTAMP
                    // =================================================

                    Timestamp now =
                            Timestamp.now();

                    orderData.put(
                            "createdAt",
                            now
                    );

                    orderData.put(
                            "updatedAt",
                            now
                    );

                    orderData.put(
                            "orderDate",
                            String.valueOf(
                                    System.currentTimeMillis()
                            )
                    );

                    // =================================================
                    // NOTIFICATIONS
                    // =================================================

                    orderData.put(
                            "sellerNotificationSent",
                            false
                    );

                    orderData.put(
                            "buyerNotificationSent",
                            false
                    );

                    // =================================================
                    // CREATE ORDER
                    // =================================================

                    transaction.set(
                            orderReference,
                            orderData
                    );

                    return remainingStock;
                })
                .addOnSuccessListener(remainingStock -> {

                    if (!isAdded()) {
                        return;
                    }

                    // =================================================
                    // UPDATE LOCAL STOCK
                    // =================================================

                    availableStock =
                            remainingStock;

                    // =================================================
                    // REMOVE CART ITEM
                    // =================================================

                    btnPlaceOrder.setText(
                            "Order Placed"
                    );

                    removeOrderedCartItem(
                            buyerId,
                            orderId
                    );
                })
                .addOnFailureListener(e -> {

                    if (!isAdded()) {
                        return;
                    }

                    btnPlaceOrder.setEnabled(true);

                    btnPlaceOrder.setText(
                            "Confirm Order"
                    );

                    String message =
                            e.getMessage();

                    if ("OUT_OF_STOCK".equals(message)) {

                        availableStock = 0;

                        updateQuantityUI();

                        Toast.makeText(
                                requireContext(),
                                "Sorry, this product is out of stock.",
                                Toast.LENGTH_LONG
                        ).show();

                        return;
                    }

                    if ("PRODUCT_NOT_FOUND".equals(message)) {

                        Toast.makeText(
                                requireContext(),
                                "Product is no longer available.",
                                Toast.LENGTH_LONG
                        ).show();

                        return;
                    }

                    if (message != null &&
                            message.startsWith(
                                    "INSUFFICIENT_STOCK:"
                            )) {

                        String stockValue =
                                message.substring(
                                        "INSUFFICIENT_STOCK:"
                                                .length()
                                );

                        int latestStock =
                                parseIntSafe(
                                        stockValue,
                                        0
                                );

                        availableStock =
                                latestStock;

                        if (quantity > availableStock) {

                            quantity =
                                    Math.max(
                                            availableStock,
                                            0
                                    );
                        }

                        updateQuantityUI();

                        Toast.makeText(
                                requireContext(),
                                "Only "
                                        + latestStock
                                        + " items are currently available.",
                                Toast.LENGTH_LONG
                        ).show();

                        return;
                    }

                    Toast.makeText(
                            requireContext(),
                            "Failed to place order: "
                                    + (
                                    message != null
                                            ? message
                                            : "Unknown error"
                            ),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    // =========================================================
    // REMOVE CART ITEM
    // =========================================================

    private void removeOrderedCartItem(
            String buyerId,
            String orderId) {

        if (!isAdded()) {
            return;
        }

        if (TextUtils.isEmpty(productId)) {

            finishOrderSuccessfully(
                    orderId
            );

            return;
        }

        db.collection("cart")
                .document(buyerId)
                .collection("items")
                .document(productId)
                .delete()
                .addOnSuccessListener(unused -> {

                    if (!isAdded()) {
                        return;
                    }

                    finishOrderSuccessfully(
                            orderId
                    );
                })
                .addOnFailureListener(e -> {

                    if (!isAdded()) {
                        return;
                    }

                    Toast.makeText(
                            requireContext(),
                            "Order placed, but cart update failed.",
                            Toast.LENGTH_SHORT
                    ).show();

                    finishOrderSuccessfully(
                            orderId
                    );
                });
    }

    // =========================================================
    // SUCCESS
    // =========================================================

    private void finishOrderSuccessfully(
            String orderId) {

        if (!isAdded()) {
            return;
        }

        Toast.makeText(
                requireContext(),
                "Order placed successfully.\nOrder ID: "
                        + orderId,
                Toast.LENGTH_LONG
        ).show();

        requireActivity()
                .getSupportFragmentManager()
                .popBackStack();
    }

    // =========================================================
    // ORDER ID
    // =========================================================

    private String generateOrderId() {

        Random random =
                new Random();

        int number =
                10000000 +
                        random.nextInt(90000000);

        return "RGL" + number;
    }

    // =========================================================
    // PARSE INT
    // =========================================================

    private int parseIntSafe(
            String value,
            int defaultValue) {

        try {

            if (TextUtils.isEmpty(value)) {
                return defaultValue;
            }

            return Integer.parseInt(
                    value.trim()
            );

        } catch (Exception e) {

            return defaultValue;
        }
    }

    // =========================================================
    // PARSE DOUBLE
    // =========================================================

    private double parseDoubleSafe(
            String value,
            double defaultValue) {

        try {

            if (TextUtils.isEmpty(value)) {
                return defaultValue;
            }

            return Double.parseDouble(
                    value.trim()
            );

        } catch (Exception e) {

            return defaultValue;
        }
    }

    // =========================================================
    // FIRESTORE NUMBER
    // =========================================================

    private double parseObjectDouble(
            Object value,
            double defaultValue) {

        try {

            if (value instanceof Number) {

                return ((Number) value)
                        .doubleValue();
            }

            return Double.parseDouble(
                    String.valueOf(value)
            );

        } catch (Exception e) {

            return defaultValue;
        }
    }

    // =========================================================
    // FORMAT
    // =========================================================

    private String formatAmount(
            double amount) {

        if (amount == (long) amount) {

            return String.valueOf(
                    (long) amount
            );
        }

        return String.format(
                java.util.Locale.US,
                "%.2f",
                amount
        );
    }

    // =========================================================
    // SAFE VALUE
    // =========================================================

    private String getSafeValue(
            String value,
            String defaultValue) {

        if (value == null ||
                value.trim().isEmpty()) {

            return defaultValue;
        }

        return value.trim();
    }

    // =========================================================
    // GET FIRST AVAILABLE FIRESTORE STRING
    // =========================================================

    private String getFirstAvailableString(
            DocumentSnapshot document,
            String... fields) {

        if (document == null ||
                fields == null) {

            return "";
        }

        for (String field : fields) {

            if (TextUtils.isEmpty(field)) {
                continue;
            }

            Object value =
                    document.get(field);

            if (value != null) {

                String result =
                        String.valueOf(value)
                                .trim();

                if (!result.isEmpty()) {

                    return result;
                }
            }
        }

        return "";
    }

    // =========================================================
    // CLEANUP
    // =========================================================

    @Override
    public void onDestroy() {

        super.onDestroy();

        try {

            imageExecutor.shutdown();

        } catch (Exception ignored) {
        }
    }
}