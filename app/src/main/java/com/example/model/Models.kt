package com.example.model

enum class Role(val title: String, val subtitle: String) {
    CUSTOMER("User Store", "Shop 10-15m Groceries"),
    MERCHANT("Merchant Partner", "Store Inventory & Packing"),
    RIDER("Delivery Agent", "Rider GPS & Navigation"),
    ADMIN("Admin Portal", "Metrics, GMV & Fleet")
}

data class Category(
    val id: String,
    val name: String,
    val emoji: String,
    val isPopular: Boolean = false
)

data class Product(
    val id: String,
    val name: String,
    val categoryId: String,
    val unit: String,
    val originalPrice: Double,
    val discountedPrice: Double,
    val inStock: Boolean = true,
    val rating: Double = 4.8,
    val reviewsCount: Int = 120,
    val isVeg: Boolean = true,
    val emoji: String = "🛒",
    val description: String = "Fresh, premium quality product delivered in 10-15 minutes.",
    val deliveryMinutes: Int = 12
) {
    val discountPercent: Int
        get() = if (originalPrice > discountedPrice && originalPrice > 0) {
            (((originalPrice - discountedPrice) / originalPrice) * 100).toInt()
        } else 0
}

data class CartItem(
    val product: Product,
    val quantity: Int
)

enum class PaymentMethod(val label: String, val description: String, val icon: String) {
    UPI("UPI FastPay", "Google Pay, PhonePe, Paytm (Instant)", "⚡"),
    CASH_ON_DELIVERY("Cash on Delivery (COD)", "Pay cash / QR scan at doorstep", "💵"),
    WALLET("BlinkPay Balance", "Available ₹1,500 • 1-tap instant", "👛"),
    CARD("Credit / Debit Card", "Visa, Mastercard, RuPay & Amex", "💳")
}

enum class OrderStatus(val stepIndex: Int, val title: String, val subtitle: String) {
    PLACED(0, "Order Placed", "Sent to dark store for quick pickup"),
    MERCHANT_ACCEPTED(1, "Store Accepted", "Inventory verified by store merchant"),
    PACKING(2, "Packing in Progress", "Bagging fresh items in thermal pouch"),
    OUT_FOR_DELIVERY(3, "Out for Delivery", "Rider Rahul is speeding to your address"),
    NEARBY(4, "Rider Nearby", "Within 200m! Keep phone handy"),
    DELIVERED(5, "Delivered", "Delivered safely at your doorstep!"),
    CANCELLED(-1, "Cancelled", "Order was cancelled")
}

data class Order(
    val id: String,
    val customerName: String = "Aarav Sharma",
    val customerPhone: String = "+91 98765 43210",
    val deliveryAddress: String = "Flat 402, Green Glen Heights, 12th Main, Indiranagar, Bengaluru",
    val deliveryInstructions: String = "Leave with security or ring bell once",
    val items: List<CartItem>,
    val itemTotal: Double,
    val deliveryFee: Double = 0.0,
    val handlingFee: Double = 5.0,
    val tip: Double = 20.0,
    val grandTotal: Double,
    val paymentMethod: PaymentMethod,
    val paymentConfirmed: Boolean = true,
    val status: OrderStatus = OrderStatus.PLACED,
    val placedAtMillis: Long = System.currentTimeMillis(),
    val estimatedMinutes: Int = 12,
    val riderName: String = "Rahul Kumar",
    val riderPhone: String = "+91 99887 76655",
    val riderVehicle: String = "Ather 450X (KA-03-EV-4122)",
    val riderRating: Double = 4.9,
    val liveProgress: Float = 0.15f, // 0.0 to 1.0 along delivery path
    val deliveryOtp: String = "6842",
    val storeName: String = "BlinkMart DarkStore #042 - Indiranagar Hub",
    val storeAddress: String = "Plot 18, 100 Feet Rd, Indiranagar"
)

data class PushNotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val timeFormatted: String,
    val orderId: String? = null,
    val isRead: Boolean = false
)

data class RiderProfile(
    val id: String = "RIDER-108",
    val name: String = "Rahul Kumar",
    val phone: String = "+91 99887 76655",
    val vehicleNumber: String = "KA-03-EV-4122 (Electric)",
    val rating: Double = 4.92,
    val todayEarnings: Double = 940.0,
    val completedTrips: Int = 16,
    val isDutyOn: Boolean = true,
    val currentSpeedKmH: Int = 34
)

data class AdminMetrics(
    val gmvToday: Double = 148520.0,
    val ordersToday: Int = 684,
    val avgDeliveryMinutes: Double = 11.4,
    val activeRidersCount: Int = 32,
    val darkStoresOnline: Int = 8,
    val isSurgeActive: Boolean = false,
    val isRainModeActive: Boolean = false
)
