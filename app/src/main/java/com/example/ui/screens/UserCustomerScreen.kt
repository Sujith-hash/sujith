package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ElectricBike
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.SampleData
import com.example.model.CartItem
import com.example.model.Order
import com.example.model.PaymentMethod
import com.example.model.Product
import com.example.ui.theme.BlinkGreenPrimary
import com.example.ui.theme.BlinkOrangeAction
import com.example.ui.theme.BlinkRedTag
import com.example.ui.theme.BlinkYellowAccent
import com.example.viewmodel.BlinkViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserCustomerScreen(
    viewModel: BlinkViewModel,
    onNavigateToLiveTracking: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val products by viewModel.products.collectAsState()
    val selectedCategory by viewModel.selectedCategoryId.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val cart by viewModel.cart.collectAsState()
    val orders by viewModel.orders.collectAsState()

    val activeOrder = orders.firstOrNull { it.status != com.example.model.OrderStatus.DELIVERED && it.status != com.example.model.OrderStatus.CANCELLED }

    var showCheckoutSheet by remember { mutableStateOf(false) }
    var selectedPaymentMethod by remember { mutableStateOf(PaymentMethod.UPI) }
    var tipAmount by remember { mutableDoubleStateOf(20.0) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    // Filter products
    val filteredProducts = products.filter { product ->
        val matchesCategory = selectedCategory == "all" || product.categoryId == selectedCategory
        val matchesSearch = searchQuery.isBlank() ||
                product.name.contains(searchQuery, ignoreCase = true) ||
                product.description.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    val cartTotal = cart.sumOf { it.product.discountedPrice * it.quantity }
    val cartCount = cart.sumOf { it.quantity }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (cartCount > 0 || activeOrder != null) 90.dp else 16.dp),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {
            // Active live order alert banner (if any order is in transit)
            if (activeOrder != null) {
                item {
                    ActiveDeliveryBanner(
                        order = activeOrder,
                        onTrackClicked = { onNavigateToLiveTracking(activeOrder.id) }
                    )
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("search_grocery_input"),
                    placeholder = {
                        Text(
                            "Search \"milk\", \"tomatoes\", \"chips\"...",
                            fontSize = 14.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = BlinkGreenPrimary
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search"
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BlinkGreenPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    ),
                    singleLine = true
                )
            }

            // Hero Promotion Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .testTag("hero_banner_card"),
                    shape = RoundedCornerShape(18.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().height(140.dp)) {
                        Image(
                            painter = painterResource(id = R.drawable.img_store_hero),
                            contentDescription = "BlinkMart 10-15 Min Grocery Delivery",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        // Gradient Overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    androidx.compose.ui.graphics.Brush.horizontalGradient(
                                        colors = listOf(
                                            Color.Black.copy(alpha = 0.85f),
                                            Color.Black.copy(alpha = 0.40f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(16.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = BlinkYellowAccent,
                                contentColor = Color.Black
                            ) {
                                Text(
                                    text = "⚡ 10-15 MIN SUPERFAST",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Groceries & Daily Essentials",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp
                            )
                            Text(
                                text = "Free delivery on orders above ₹99",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Categories horizontal slider
            item {
                Text(
                    text = "Explore Categories",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 6.dp)
                )
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(SampleData.categories) { category ->
                        val isSelected = selectedCategory == category.id
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.selectCategory(category.id) },
                            label = {
                                Text(
                                    text = "${category.emoji} ${category.name}",
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BlinkGreenPrimary,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.testTag("cat_${category.id}")
                        )
                    }
                }
            }

            // Product Section Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedCategory == "all") "Popular Picks in 10 Mins" else SampleData.categories.find { it.id == selectedCategory }?.name ?: "Products",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${filteredProducts.size} items",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Product Items List
            items(filteredProducts, key = { it.id }) { product ->
                val cartItem = cart.find { it.product.id == product.id }
                ProductRowCard(
                    product = product,
                    quantityInCart = cartItem?.quantity ?: 0,
                    onAdd = { viewModel.addToCart(product) },
                    onDecrement = { viewModel.decrementCart(product) }
                )
            }
        }

        // Floating Sticky Cart Bar
        AnimatedVisibility(
            visible = cartCount > 0,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = BlinkGreenPrimary,
                contentColor = Color.White,
                shadowElevation = 10.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showCheckoutSheet = true }
                    .testTag("floating_cart_bar")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingBag,
                                    contentDescription = "Cart",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "$cartCount ITEMS • ₹${cartTotal.toInt()}",
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Extra savings applied",
                                fontSize = 11.sp,
                                color = BlinkYellowAccent
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "View Cart",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("➔", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }

    // Checkout Bottom Sheet
    if (showCheckoutSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCheckoutSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            CheckoutSheetContent(
                cart = cart,
                cartTotal = cartTotal,
                selectedPaymentMethod = selectedPaymentMethod,
                onPaymentMethodChanged = { selectedPaymentMethod = it },
                tipAmount = tipAmount,
                onTipChanged = { tipAmount = it },
                onAdd = { viewModel.addToCart(it) },
                onDecrement = { viewModel.decrementCart(it) },
                onProceedToBiometric = {
                    viewModel.triggerBiometricCheckout {
                        val newOrder = viewModel.placeOrder(
                            paymentMethod = selectedPaymentMethod,
                            deliveryAddress = "Flat 402, Green Glen Heights, Indiranagar, Bengaluru",
                            tip = tipAmount
                        )
                        showCheckoutSheet = false
                        if (newOrder != null) {
                            onNavigateToLiveTracking(newOrder.id)
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun ProductRowCard(
    product: Product,
    quantityInCart: Int,
    onAdd: () -> Unit,
    onDecrement: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("product_card_${product.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Emoji Visual Avatar Container
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(text = product.emoji, fontSize = 36.sp)

                // Veg / Non-veg symbol
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(4.dp)
                        .size(14.dp)
                        .background(Color.White, RoundedCornerShape(3.dp))
                        .border(
                            1.dp,
                            if (product.isVeg) Color(0xFF16A34A) else Color(0xFFDC2626),
                            RoundedCornerShape(3.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(
                                if (product.isVeg) Color(0xFF16A34A) else Color(0xFFDC2626),
                                CircleShape
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                // 10-Min Badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FlashOn,
                        contentDescription = "10 min",
                        tint = BlinkGreenPrimary,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "10-12 MINS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = BlinkGreenPrimary
                    )
                }

                Text(
                    text = product.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = product.unit,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Price Row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "₹${product.discountedPrice.toInt()}",
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (product.originalPrice > product.discountedPrice) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "₹${product.originalPrice.toInt()}",
                            fontSize = 12.sp,
                            textDecoration = TextDecoration.LineThrough,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = BlinkGreenPrimary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "${product.discountPercent}% OFF",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = BlinkGreenPrimary,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Stepper / Add Button
            if (!product.inStock) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "OUT OF STOCK",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = BlinkRedTag,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                    )
                }
            } else if (quantityInCart == 0) {
                Button(
                    onClick = onAdd,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BlinkGreenPrimary.copy(alpha = 0.12f),
                        contentColor = BlinkGreenPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BlinkGreenPrimary),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("add_btn_${product.id}")
                ) {
                    Text("ADD", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(BlinkGreenPrimary, RoundedCornerShape(12.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    IconButton(
                        onClick = onDecrement,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Decrease",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Text(
                        text = "$quantityInCart",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )

                    IconButton(
                        onClick = onAdd,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Increase",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveDeliveryBanner(
    order: Order,
    onTrackClicked: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onTrackClicked() }
            .testTag("active_delivery_alert_banner"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BlinkGreenPrimary)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = BlinkYellowAccent,
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.ElectricBike,
                        contentDescription = "Live Delivery",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "ORDER #${order.id} IN TRANSIT",
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        color = BlinkYellowAccent
                    )
                }
                Text(
                    text = "${order.status.title} • ETA ${order.estimatedMinutes} Mins",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                contentColor = BlinkGreenPrimary
            ) {
                Text(
                    text = "Track Live ➔",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun CheckoutSheetContent(
    cart: List<CartItem>,
    cartTotal: Double,
    selectedPaymentMethod: PaymentMethod,
    onPaymentMethodChanged: (PaymentMethod) -> Unit,
    tipAmount: Double,
    onTipChanged: (Double) -> Unit,
    onAdd: (Product) -> Unit,
    onDecrement: (Product) -> Unit,
    onProceedToBiometric: () -> Unit
) {
    val deliveryFee = if (cartTotal >= 99.0) 0.0 else 25.0
    val handlingFee = 5.0
    val grandTotal = cartTotal + deliveryFee + handlingFee + tipAmount

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(bottom = 30.dp)
    ) {
        item {
            Text(
                text = "Order Summary & Checkout",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "⚡ Delivery in 10-15 mins from Indiranagar DarkStore",
                fontSize = 12.sp,
                color = BlinkGreenPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Cart items review
        items(cart) { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Text(item.product.emoji, fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = item.product.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${item.product.unit} • ₹${item.product.discountedPrice.toInt()} each",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onDecrement(item.product) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Remove, contentDescription = "Minus", modifier = Modifier.size(14.dp))
                    }
                    Text("${item.quantity}", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp))
                    IconButton(
                        onClick = { onAdd(item.product) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Plus", modifier = Modifier.size(14.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "₹${(item.product.discountedPrice * item.quantity).toInt()}",
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Delivery Partner Tip
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Text("Tip your delivery hero (Ather EV Rider)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(0.0, 10.0, 20.0, 30.0, 50.0).forEach { tip ->
                    FilterChip(
                        selected = tipAmount == tip,
                        onClick = { onTipChanged(tip) },
                        label = {
                            Text(if (tip == 0.0) "None" else "₹${tip.toInt()}")
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BlinkGreenPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        // Bill Breakdown
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Bill Details", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    BillLine("Items Total", "₹${cartTotal.toInt()}")
                    BillLine(
                        "Delivery Charge",
                        if (deliveryFee == 0.0) "FREE" else "₹${deliveryFee.toInt()}",
                        isHighlight = deliveryFee == 0.0
                    )
                    BillLine("Handling & Packaging", "₹${handlingFee.toInt()}")
                    if (tipAmount > 0) {
                        BillLine("Delivery Partner Tip", "₹${tipAmount.toInt()}")
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    androidx.compose.material3.HorizontalDivider()
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Grand Total", fontWeight = FontWeight.Black, fontSize = 16.sp)
                        Text("₹${grandTotal.toInt()}", fontWeight = FontWeight.Black, fontSize = 16.sp, color = BlinkGreenPrimary)
                    }
                }
            }
        }

        // Select Payment Gateway (UPI, COD, Wallet, Card)
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Payment Method", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(6.dp))

            PaymentMethod.entries.forEach { method ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (selectedPaymentMethod == method) BlinkGreenPrimary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(
                        if (selectedPaymentMethod == method) 1.5.dp else 1.dp,
                        if (selectedPaymentMethod == method) BlinkGreenPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onPaymentMethodChanged(method) }
                        .testTag("payment_method_${method.name.lowercase()}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedPaymentMethod == method,
                            onClick = { onPaymentMethodChanged(method) },
                            colors = RadioButtonDefaults.colors(selectedColor = BlinkGreenPrimary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(method.icon, fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(method.label, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(method.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // 1-Tap Biometric Checkout Button
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onProceedToBiometric,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("checkout_pay_button"),
                colors = ButtonDefaults.buttonColors(containerColor = BlinkGreenPrimary),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Secure Checkout",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Pay ₹${grandTotal.toInt()} • Biometric Authorize ⚡",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun BillLine(label: String, value: String, isHighlight: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            value,
            fontSize = 12.sp,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Medium,
            color = if (isHighlight) BlinkGreenPrimary else MaterialTheme.colorScheme.onSurface
        )
    }
}
