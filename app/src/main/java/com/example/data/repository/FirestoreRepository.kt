package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.model.Order
import com.example.model.OrderStatus
import com.example.model.PaymentMethod
import com.example.model.Product
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.tasks.await

class FirestoreRepository(
    private val db: FirebaseFirestore
) {
    companion object {
        private const val TAG = "FirestoreRepo"

        fun create(context: Context): FirestoreRepository {
            val dbId = context.getString(R.string.firestore_database_id)
            val firestoreInstance = FirebaseFirestore.getInstance(dbId)
            return FirestoreRepository(firestoreInstance)
        }
    }

    private val auth = Firebase.auth

    fun currentUserId(): String? = try { auth.currentUser?.uid } catch (_: Exception) { null }

    // Save or update an order in Firestore (auth-gated)
    suspend fun saveOrder(order: Order, userId: String) {
        val uid = currentUserId()
        if (uid == null || uid != userId) {
            Log.d(TAG, "Skipping Firestore saveOrder: user not signed in")
            return
        }
        try {
            val orderRef = db.collection("users").document(userId)
                .collection("orders").document(order.id)

            val orderData = hashMapOf(
                "id" to order.id,
                "userId" to userId,
                "customerName" to order.customerName,
                "customerPhone" to order.customerPhone,
                "deliveryAddress" to order.deliveryAddress,
                "grandTotal" to order.grandTotal,
                "itemTotal" to order.itemTotal,
                "deliveryFee" to order.deliveryFee,
                "tip" to order.tip,
                "status" to order.status.name,
                "deliveryOtp" to order.deliveryOtp,
                "riderName" to order.riderName,
                "riderPhone" to order.riderPhone,
                "riderVehicle" to order.riderVehicle,
                "liveProgress" to order.liveProgress.toDouble(),
                "paymentMethod" to order.paymentMethod.name,
                "storeName" to order.storeName,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp(),
                "itemsCount" to order.items.size
            )

            orderRef.set(orderData, SetOptions.merge()).await()
            Log.d(TAG, "Order #${order.id} saved to Firestore cloud successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save order to Firestore", e)
        }
    }

    // Update order status in Firestore (auth-gated)
    suspend fun updateOrderStatus(userId: String, orderId: String, status: OrderStatus, progress: Float) {
        val uid = currentUserId()
        if (uid == null) {
            Log.d(TAG, "Skipping Firestore updateOrderStatus: user not signed in")
            return
        }
        try {
            val orderRef = db.collection("users").document(userId)
                .collection("orders").document(orderId)

            val updates = hashMapOf<String, Any>(
                "status" to status.name,
                "liveProgress" to progress.toDouble(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            orderRef.update(updates).await()
            Log.d(TAG, "Order #${orderId} status updated to ${status.name}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update order status", e)
        }
    }

    // Observe real-time orders from Firestore (auth-gated)
    fun observeUserOrders(userId: String): Flow<List<Order>> {
        val uid = currentUserId()
        if (uid == null || uid != userId) return emptyFlow()

        return callbackFlow {
            val ordersRef = db.collection("users").document(userId).collection("orders")
            val listener = ordersRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Listen failed for orders", error)
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val ordersList = snapshot.documents.mapNotNull { doc ->
                        try {
                            val id = doc.getString("id") ?: doc.id
                            val customerName = doc.getString("customerName") ?: "Customer"
                            val customerPhone = doc.getString("customerPhone") ?: ""
                            val address = doc.getString("deliveryAddress") ?: ""
                            val grandTotal = doc.getDouble("grandTotal") ?: 0.0
                            val itemTotal = doc.getDouble("itemTotal") ?: 0.0
                            val deliveryFee = doc.getDouble("deliveryFee") ?: 0.0
                            val tip = doc.getDouble("tip") ?: 0.0
                            val statusStr = doc.getString("status") ?: OrderStatus.PLACED.name
                            val status = try { OrderStatus.valueOf(statusStr) } catch (_: Exception) { OrderStatus.PLACED }
                            val otp = doc.getString("deliveryOtp") ?: "1234"
                            val riderName = doc.getString("riderName") ?: "Rahul Kumar"
                            val riderPhone = doc.getString("riderPhone") ?: ""
                            val riderVehicle = doc.getString("riderVehicle") ?: "Ather 450X"
                            val progress = (doc.getDouble("liveProgress") ?: 0.1).toFloat()
                            val payStr = doc.getString("paymentMethod") ?: PaymentMethod.UPI.name
                            val payment = try { PaymentMethod.valueOf(payStr) } catch (_: Exception) { PaymentMethod.UPI }

                            Order(
                                id = id,
                                customerName = customerName,
                                customerPhone = customerPhone,
                                deliveryAddress = address,
                                items = emptyList(),
                                itemTotal = itemTotal,
                                deliveryFee = deliveryFee,
                                tip = tip,
                                grandTotal = grandTotal,
                                paymentMethod = payment,
                                status = status,
                                deliveryOtp = otp,
                                riderName = riderName,
                                riderPhone = riderPhone,
                                riderVehicle = riderVehicle,
                                liveProgress = progress
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, "Error deserializing order doc: ${doc.id}", e)
                            null
                        }
                    }
                    trySend(ordersList)
                }
            }
            awaitClose { listener.remove() }
        }
    }

    // Sync product catalogue item to Firestore store inventory (auth-gated)
    suspend fun syncInventoryItem(storeId: String, product: Product) {
        val uid = currentUserId()
        if (uid == null) {
            Log.d(TAG, "Skipping Firestore syncInventoryItem: user not signed in")
            return
        }
        try {
            val itemRef = db.collection("stores").document(storeId)
                .collection("inventory").document(product.id)

            val itemData = hashMapOf(
                "id" to product.id,
                "name" to product.name,
                "categoryId" to product.categoryId,
                "unit" to product.unit,
                "price" to product.discountedPrice,
                "inStock" to product.inStock,
                "emoji" to product.emoji,
                "storeId" to storeId,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            itemRef.set(itemData, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync inventory item ${product.id}", e)
        }
    }
}
