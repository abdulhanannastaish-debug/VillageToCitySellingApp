package com.example.villagetocityreseilingapp.ui.main.buyer;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class BuyerNotificationHelper {

    private BuyerNotificationHelper() {
        // Utility class
    }

    public static void createOrderAcceptedNotification(
            String buyerId,
            String orderId) {

        if (buyerId == null ||
                buyerId.trim().isEmpty()) {
            return;
        }

        if (orderId == null ||
                orderId.trim().isEmpty()) {
            return;
        }

        FirebaseFirestore db =
                FirebaseFirestore.getInstance();

        Map<String, Object> notification =
                new HashMap<>();

        notification.put(
                "buyerId",
                buyerId
        );

        notification.put(
                "orderId",
                orderId
        );

        notification.put(
                "type",
                "order_accepted"
        );

        notification.put(
                "title",
                "Order Accepted"
        );

        notification.put(
                "message",
                "Your order is accepted by the seller."
        );

        notification.put(
                "read",
                false
        );

        notification.put(
                "createdAt",
                FieldValue.serverTimestamp()
        );

        db.collection("notifications")
                .add(notification);
    }
}