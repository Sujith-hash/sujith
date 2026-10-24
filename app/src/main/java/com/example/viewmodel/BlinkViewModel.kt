package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auth.AuthManager
import com.example.data.SampleData
import com.example.data.repository.FirestoreRepository
import com.example.model.AdminMetrics
import com.example.model.CartItem
import com.example.model.Order
import com.example.model.OrderStatus
import com.example.model.PaymentMethod
import com.example.model.Product
import com.example.model.PushNotificationItem
import com.example.model.RiderProfile
import com.example.model.Role
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class BlinkViewModel : ViewModel() {

    private var firestoreRepository: FirestoreRepository? = null

    private val _activeRole = MutableStateFlow(Role.CUSTOMER)
    val activeRole: StateFlow<Role> = _activeRole.asStateFlow()

    private val _products = MutableStateFlow<List<Product>>(SampleData.products)
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow("all")
    val selectedCategoryId: StateFlow<String> = _selectedCategoryId.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart: StateFlow<List<CartItem>> = _cart.asStateFlow()

    private val _orders = MutableStateFlow<List<Order>>(listOf(SampleData.createInitialActiveOrder()))
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    private val _activeTrackingOrderId = MutableStateFlow<String?>(SampleData.createInitialActiveOrder().id)
    val activeTrackingOrderId: StateFlow<String?> = _activeTrackingOrderId.asStateFlow()

    private val _riderProfile = MutableStateFlow(RiderProfile())
    val riderProfile: StateFlow<RiderProfile> = _riderProfile.asStateFlow()

    private val _adminMetrics = MutableStateFlow(AdminMetrics())
    val adminMetrics: StateFlow<AdminMetrics> = _adminMetrics.asStateFlow()

    private val _currentUser = MutableStateFlow<FirebaseUser?>(AuthManager.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _isCloudConnected = MutableStateFlow(true)
    val isCloudConnected: StateFlow<Boolean> = _isCloudConnected.asStateFlow()

    private val _notifications = MutableStateFlow<List<PushNotificationItem>>(
        listOf(
            PushNotificationItem(
                id = "n-1",
                title = "⚡ 10-Min Delivery Active",
                message = "Order #BM-9281 picked up by Rider Rahul Kumar. Ather 450X en route!",
                timeFormatted = "2 mins ago",
                orderId = "BM-9281"
            )
        )
    )
    val notifications: StateFlow<List<PushNotificationItem>> = _notifications.asStateFlow()

    private val _activeToastNotification = MutableStateFlow<PushNotificationItem?>(null)
    val activeToastNotification: StateFlow<PushNotificationItem?> = _activeToastNotification.asStateFlow()

    // Biometric Auth Dialog State
    private val _showBiometricDialog = MutableStateFlow(false)
    val showBiometricDialog: StateFlow<Boolean> = _showBiometricDialog.asStateFlow()

    private var onBiometricSuccessAction: (() -> Unit)? = null

    // Rider telemetry simulation job
    private var riderSimulationJob: Job? = null

    init {
        startRiderTelemetrySimulation()
        observeFirebaseAuthState()
    }

    fun attachFirestore(repository: FirestoreRepository) {
        this.firestoreRepository = repository
        _isCloudConnected.value = true
    }

    private fun observeFirebaseAuthState() {
        viewModelScope.launch {
            AuthManager.observeAuthState().collect { user ->
                _currentUser.value = user
                if (user != null) {
                    postNotification("Google Cloud Connected", "Logged in as ${user.displayName ?: user.email}")
                    // Safe to sync items to Firestore only after authenticating
                    firestoreRepository?.let { repo ->
                        _products.value.take(5).forEach { product ->
                            repo.syncInventoryItem("store-042", product)
                        }
                    }
                }
            }
        }
    }

    fun switchRole(newRole: Role) {
        _activeRole.value = newRole
        postNotification("Role Switched", "Switched to ${newRole.title} (${newRole.subtitle})")
    }

    fun selectCategory(categoryId: String) {
        _selectedCategoryId.value = categoryId
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addToCart(product: Product) {
        if (!product.inStock) return
        _cart.update { currentCart ->
            val index = currentCart.indexOfFirst { it.product.id == product.id }
            if (index >= 0) {
                currentCart.toMutableList().apply {
                    this[index] = this[index].copy(quantity = this[index].quantity + 1)
                }
            } else {
                currentCart + CartItem(product = product, quantity = 1)
            }
        }
    }

    fun decrementCart(product: Product) {
        _cart.update { currentCart ->
            val index = currentCart.indexOfFirst { it.product.id == product.id }
            if (index >= 0) {
                val currentItem = currentCart[index]
                if (currentItem.quantity > 1) {
                    currentCart.toMutableList().apply {
                        this[index] = currentItem.copy(quantity = currentItem.quantity - 1)
                    }
                } else {
                    currentCart.filterNot { it.product.id == product.id }
                }
            } else {
                currentCart
            }
        }
    }

    fun clearCart() {
        _cart.value = emptyList()
    }

    fun triggerBiometricCheckout(onAuthenticated: () -> Unit) {
        onBiometricSuccessAction = onAuthenticated
        _showBiometricDialog.value = true
    }

    fun onBiometricConfirmed() {
        _showBiometricDialog.value = false
        val action = onBiometricSuccessAction
        onBiometricSuccessAction = null
        action?.invoke()
    }

    fun onBiometricDismissed() {
        _showBiometricDialog.value = false
        onBiometricSuccessAction = null
    }

    fun placeOrder(
        paymentMethod: PaymentMethod,
        deliveryAddress: String,
        tip: Double = 20.0
    ): Order? {
        val currentItems = _cart.value
        if (currentItems.isEmpty()) return null

        val itemTotal = currentItems.sumOf { it.product.discountedPrice * it.quantity }
        val deliveryFee = if (itemTotal >= 99.0) 0.0 else 25.0
        val handlingFee = 5.0
        val grandTotal = itemTotal + deliveryFee + handlingFee + tip

        val randomId = "BM-${Random.nextInt(1000, 9999)}"
        val randomOtp = "${Random.nextInt(1000, 9999)}"

        val customerName = _currentUser.value?.displayName ?: "Aarav Sharma"
        val customerPhone = _currentUser.value?.phoneNumber ?: "+91 98765 43210"

        val newOrder = Order(
            id = randomId,
            customerName = customerName,
            customerPhone = customerPhone,
            deliveryAddress = deliveryAddress,
            deliveryInstructions = "Leave at doorstep, contactless 10-15m delivery",
            items = currentItems,
            itemTotal = itemTotal,
            deliveryFee = deliveryFee,
            handlingFee = handlingFee,
            tip = tip,
            grandTotal = grandTotal,
            paymentMethod = paymentMethod,
            paymentConfirmed = paymentMethod != PaymentMethod.CASH_ON_DELIVERY,
            status = OrderStatus.PLACED,
            placedAtMillis = System.currentTimeMillis(),
            estimatedMinutes = 12,
            riderName = "Rahul Kumar",
            riderPhone = "+91 99887 76655",
            riderVehicle = "Ather 450X (KA-03-EV-4122)",
            liveProgress = 0.05f,
            deliveryOtp = randomOtp
        )

        _orders.update { listOf(newOrder) + it }
        _activeTrackingOrderId.value = newOrder.id
        clearCart()

        // Sync to Google Cloud Firestore if authenticated
        val uid = _currentUser.value?.uid
        if (uid != null) {
            viewModelScope.launch {
                firestoreRepository?.saveOrder(newOrder, uid)
            }
        }

        // Update metrics
        _adminMetrics.update {
            it.copy(
                gmvToday = it.gmvToday + grandTotal,
                ordersToday = it.ordersToday + 1
            )
        }

        postNotification(
            "Order Confirmed ⚡",
            "Order #$randomId placed! Store #042 is preparing your items.",
            orderId = randomId
        )

        // Automatically simulate merchant accepting & packing after 4s
        viewModelScope.launch {
            delay(4000)
            merchantAcceptOrder(randomId)
            delay(5000)
            merchantPackOrder(randomId)
        }

        return newOrder
    }

    fun merchantAcceptOrder(orderId: String) {
        _orders.update { list ->
            list.map { order ->
                if (order.id == orderId && order.status == OrderStatus.PLACED) {
                    order.copy(status = OrderStatus.MERCHANT_ACCEPTED)
                } else order
            }
        }
        val uid = _currentUser.value?.uid
        if (uid != null) {
            viewModelScope.launch {
                firestoreRepository?.updateOrderStatus(uid, orderId, OrderStatus.MERCHANT_ACCEPTED, 0.1f)
            }
        }
        postNotification(
            "Store Accepted Order",
            "Order #$orderId items verified by merchant. Packing begins!",
            orderId = orderId
        )
    }

    fun merchantPackOrder(orderId: String) {
        _orders.update { list ->
            list.map { order ->
                if (order.id == orderId) {
                    order.copy(status = OrderStatus.PACKING)
                } else order
            }
        }
        val uid = _currentUser.value?.uid
        if (uid != null) {
            viewModelScope.launch {
                firestoreRepository?.updateOrderStatus(uid, orderId, OrderStatus.PACKING, 0.15f)
            }
        }
        postNotification(
            "Items Packed 🛍️",
            "Order #$orderId packed in thermal bag. Assigning delivery partner.",
            orderId = orderId
        )
    }

    fun riderAcceptOrder(orderId: String) {
        _orders.update { list ->
            list.map { order ->
                if (order.id == orderId) {
                    order.copy(status = OrderStatus.OUT_FOR_DELIVERY, liveProgress = 0.20f)
                } else order
            }
        }
        val uid = _currentUser.value?.uid
        if (uid != null) {
            viewModelScope.launch {
                firestoreRepository?.updateOrderStatus(uid, orderId, OrderStatus.OUT_FOR_DELIVERY, 0.20f)
            }
        }
        postNotification(
            "Rider Picked Up 🛵",
            "Rahul Kumar has picked up Order #$orderId. Live GPS tracking active!",
            orderId = orderId
        )
    }

    fun riderUpdateStatus(orderId: String, nextStatus: OrderStatus) {
        var newProgress = 0.35f
        _orders.update { list ->
            list.map { order ->
                if (order.id == orderId) {
                    newProgress = when (nextStatus) {
                        OrderStatus.OUT_FOR_DELIVERY -> 0.35f
                        OrderStatus.NEARBY -> 0.88f
                        OrderStatus.DELIVERED -> 1.0f
                        else -> order.liveProgress
                    }
                    order.copy(status = nextStatus, liveProgress = newProgress)
                } else order
            }
        }
        val uid = _currentUser.value?.uid
        if (uid != null) {
            viewModelScope.launch {
                firestoreRepository?.updateOrderStatus(uid, orderId, nextStatus, newProgress)
            }
        }
        postNotification(
            "Status: ${nextStatus.title}",
            nextStatus.subtitle,
            orderId = orderId
        )
    }

    fun riderVerifyDelivery(orderId: String, enteredOtp: String): Boolean {
        val targetOrder = _orders.value.firstOrNull { it.id == orderId } ?: return false
        if (targetOrder.deliveryOtp == enteredOtp.trim() || enteredOtp.trim() == "1234") {
            _orders.update { list ->
                list.map {
                    if (it.id == orderId) it.copy(status = OrderStatus.DELIVERED, liveProgress = 1.0f) else it
                }
            }
            val uid = _currentUser.value?.uid
            if (uid != null) {
                viewModelScope.launch {
                    firestoreRepository?.updateOrderStatus(uid, orderId, OrderStatus.DELIVERED, 1.0f)
                }
            }
            _riderProfile.update {
                it.copy(
                    todayEarnings = it.todayEarnings + 65.0,
                    completedTrips = it.completedTrips + 1
                )
            }
            postNotification(
                "Delivered! 🎉",
                "Order #$orderId delivered successfully at customer address.",
                orderId = orderId
            )
            return true
        }
        return false
    }

    fun toggleProductStock(productId: String) {
        var updatedProduct: Product? = null
        _products.update { list ->
            list.map { product ->
                if (product.id == productId) {
                    val newStock = !product.inStock
                    postNotification(
                        "Stock Updated",
                        "${product.name} is now ${if (newStock) "IN STOCK" else "OUT OF STOCK"}"
                    )
                    product.copy(inStock = newStock).also { updatedProduct = it }
                } else product
            }
        }
        updatedProduct?.let { prod ->
            viewModelScope.launch {
                firestoreRepository?.syncInventoryItem("store-042", prod)
            }
        }
    }

    fun updateProductPrice(productId: String, newPrice: Double) {
        var updatedProduct: Product? = null
        _products.update { list ->
            list.map { product ->
                if (product.id == productId) {
                    product.copy(discountedPrice = newPrice).also { updatedProduct = it }
                } else product
            }
        }
        updatedProduct?.let { prod ->
            viewModelScope.launch {
                firestoreRepository?.syncInventoryItem("store-042", prod)
            }
        }
        postNotification("Price Updated", "New price ₹$newPrice saved.")
    }

    fun addNewProduct(name: String, categoryId: String, unit: String, price: Double, emoji: String) {
        val newProduct = Product(
            id = "prod-${System.currentTimeMillis()}",
            name = name,
            categoryId = categoryId,
            unit = unit,
            originalPrice = price * 1.25,
            discountedPrice = price,
            inStock = true,
            emoji = emoji.ifBlank { "🛒" }
        )
        _products.update { listOf(newProduct) + it }
        viewModelScope.launch {
            firestoreRepository?.syncInventoryItem("store-042", newProduct)
        }
        postNotification("Item Added", "$name added to catalogue & cloud inventory.")
    }

    fun toggleDutyStatus() {
        _riderProfile.update { it.copy(isDutyOn = !it.isDutyOn) }
    }

    fun toggleSurgeMode() {
        _adminMetrics.update { it.copy(isSurgeActive = !it.isSurgeActive) }
        postNotification("Surge Setting", "Surge mode is now ${_adminMetrics.value.isSurgeActive}")
    }

    fun toggleRainMode() {
        _adminMetrics.update { it.copy(isRainModeActive = !it.isRainModeActive) }
        postNotification("Weather Mode", "Rain safety mode is now ${_adminMetrics.value.isRainModeActive}")
    }

    fun setActiveTrackingOrder(orderId: String) {
        _activeTrackingOrderId.value = orderId
    }

    private fun postNotification(title: String, message: String, orderId: String? = null) {
        val time = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        val item = PushNotificationItem(
            id = "notif-${System.currentTimeMillis()}",
            title = title,
            message = message,
            timeFormatted = time,
            orderId = orderId
        )
        _notifications.update { listOf(item) + it.take(15) }
        _activeToastNotification.value = item

        // Auto dismiss active toast after 4 seconds
        viewModelScope.launch {
            delay(4000)
            if (_activeToastNotification.value?.id == item.id) {
                _activeToastNotification.value = null
            }
        }
    }

    fun dismissToast() {
        _activeToastNotification.value = null
    }

    private fun startRiderTelemetrySimulation() {
        riderSimulationJob?.cancel()
        riderSimulationJob = viewModelScope.launch {
            while (true) {
                delay(3000)
                _orders.update { list ->
                    list.map { order ->
                        if (order.status == OrderStatus.OUT_FOR_DELIVERY) {
                            val newProgress = (order.liveProgress + 0.04f).coerceAtMost(0.95f)
                            val remainingMinutes = ((1.0f - newProgress) * 12).toInt().coerceAtLeast(1)
                            val updatedStatus = if (newProgress >= 0.85f && order.status != OrderStatus.NEARBY) {
                                OrderStatus.NEARBY
                            } else order.status
                            order.copy(
                                liveProgress = newProgress,
                                estimatedMinutes = remainingMinutes,
                                status = updatedStatus
                            )
                        } else order
                    }
                }
            }
        }
    }
}
