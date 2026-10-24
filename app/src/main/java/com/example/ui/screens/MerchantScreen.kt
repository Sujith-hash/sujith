package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.Order
import com.example.model.OrderStatus
import com.example.model.Product
import com.example.ui.theme.BlinkGreenPrimary
import com.example.ui.theme.BlinkRedTag
import com.example.ui.theme.BlinkYellowAccent
import com.example.viewmodel.BlinkViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MerchantScreen(
    viewModel: BlinkViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val orders by viewModel.orders.collectAsState()
    val products by viewModel.products.collectAsState()
    var isStoreOnline by remember { mutableStateOf(true) }
    var showAddItemDialog by remember { mutableStateOf(false) }

    var stockSearch by remember { mutableStateOf("") }
    val filteredStock = products.filter {
        stockSearch.isBlank() || it.name.contains(stockSearch, ignoreCase = true)
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Dark Store Merchant Header
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = BlinkGreenPrimary.copy(alpha = 0.15f),
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Store,
                                        contentDescription = "Store",
                                        tint = BlinkGreenPrimary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Indiranagar DarkStore #042",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = if (isStoreOnline) "🟢 Receiving 10-Min Orders" else "🔴 Store Paused",
                                    fontSize = 12.sp,
                                    color = if (isStoreOnline) BlinkGreenPrimary else BlinkRedTag,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Switch(
                            checked = isStoreOnline,
                            onCheckedChange = { isStoreOnline = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = BlinkGreenPrimary)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Store Quick Metric Pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricPill(
                            title = "Avg Pack Time",
                            value = "2.1 mins",
                            icon = "⏱️",
                            modifier = Modifier.weight(1f)
                        )
                        MetricPill(
                            title = "Pending Pack",
                            value = "${orders.count { it.status == OrderStatus.PLACED || it.status == OrderStatus.MERCHANT_ACCEPTED }}",
                            icon = "🛍️",
                            modifier = Modifier.weight(1f)
                        )
                        MetricPill(
                            title = "Catalogue",
                            value = "${products.size} Items",
                            icon = "📦",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Tab bar: 0: Active Orders, 1: Inventory & Stock, 2: Analytics
            SecondaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            "Incoming Orders (${orders.count { it.status != OrderStatus.DELIVERED }})",
                            fontWeight = FontWeight.Bold
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Inventory Stock", fontWeight = FontWeight.Bold) }
                )
            }

            // Tab 0: Incoming orders
            if (selectedTab == 0) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    val activeOrders = orders.filter { it.status != OrderStatus.DELIVERED }
                    if (activeOrders.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No pending orders right now. Good job!",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        items(activeOrders, key = { it.id }) { order ->
                            MerchantOrderCard(
                                order = order,
                                onAccept = { viewModel.merchantAcceptOrder(order.id) },
                                onPackReady = { viewModel.merchantPackOrder(order.id) }
                            )
                        }
                    }
                }
            } else {
                // Tab 1: Inventory Stock Control
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = stockSearch,
                            onValueChange = { stockSearch = it },
                            placeholder = { Text("Filter items in inventory...") },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = "Search")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true
                        )
                    }

                    items(filteredStock, key = { it.id }) { product ->
                        MerchantStockItemRow(
                            product = product,
                            onToggleStock = { viewModel.toggleProductStock(product.id) }
                        )
                    }
                }
            }
        }

        // Floating Action Button to Add New Item to Inventory
        if (selectedTab == 1) {
            FloatingActionButton(
                onClick = { showAddItemDialog = true },
                containerColor = BlinkGreenPrimary,
                contentColor = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
                    .testTag("add_inventory_product_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Item")
            }
        }
    }

    // Add Product Dialog
    if (showAddItemDialog) {
        AddNewProductDialog(
            onDismiss = { showAddItemDialog = false },
            onConfirm = { name, categoryId, unit, price, emoji ->
                viewModel.addNewProduct(name, categoryId, unit, price, emoji)
                showAddItemDialog = false
            }
        )
    }
}

@Composable
fun MerchantOrderCard(
    order: Order,
    onAccept: () -> Unit,
    onPackReady: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .testTag("merchant_order_${order.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ORDER #${order.id}",
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Customer: ${order.customerName} • ${order.paymentMethod.label}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = when (order.status) {
                        OrderStatus.PLACED -> BlinkYellowAccent
                        OrderStatus.MERCHANT_ACCEPTED, OrderStatus.PACKING -> BlinkGreenPrimary.copy(alpha = 0.15f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Text(
                        text = order.status.title.uppercase(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = if (order.status == OrderStatus.PLACED) Color.Black else BlinkGreenPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            androidx.compose.material3.HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(8.dp))

            // Packing Checklist
            Text("Packing Checklist (${order.items.size} items):", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            order.items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(item.product.emoji, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${item.quantity}x ${item.product.name} (${item.product.unit})",
                            fontSize = 13.sp
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Verified in stock",
                        tint = BlinkGreenPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            when (order.status) {
                OrderStatus.PLACED -> {
                    Button(
                        onClick = onAccept,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = BlinkGreenPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Accept & Start Packing ⚡", fontWeight = FontWeight.Bold)
                    }
                }
                OrderStatus.MERCHANT_ACCEPTED -> {
                    Button(
                        onClick = onPackReady,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = BlinkGreenPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Mark Packed & Call Rider 🛍️", fontWeight = FontWeight.Bold)
                    }
                }
                OrderStatus.PACKING -> {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = BlinkGreenPrimary.copy(alpha = 0.10f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "🛍️ Items Packed! Waiting for Rider ${order.riderName} to collect",
                            color = BlinkGreenPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
                else -> {
                    Text(
                        text = "Rider ${order.riderName} is delivering to customer.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun MerchantStockItemRow(
    product: Product,
    onToggleStock: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Text(product.emoji, fontSize = 28.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = product.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "${product.unit} • ₹${product.discountedPrice.toInt()}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (product.inStock) "IN STOCK" else "OUT OF STOCK",
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = if (product.inStock) BlinkGreenPrimary else BlinkRedTag
                )
                Switch(
                    checked = product.inStock,
                    onCheckedChange = { onToggleStock() },
                    colors = SwitchDefaults.colors(checkedThumbColor = BlinkGreenPrimary)
                )
            }
        }
    }
}

@Composable
fun MetricPill(title: String, value: String, icon: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(icon, fontSize = 16.sp)
            Text(value, fontWeight = FontWeight.Black, fontSize = 13.sp)
            Text(title, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun AddNewProductDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, categoryId: String, unit: String, price: Double, emoji: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("500 g") }
    var priceStr by remember { mutableStateOf("45") }
    var emoji by remember { mutableStateOf("🥑") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Add New Grocery Item", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    label = { Text("Unit (e.g. 1 kg, 500 ml)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = priceStr,
                    onValueChange = { priceStr = it },
                    label = { Text("Price (₹)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = emoji,
                    onValueChange = { emoji = it },
                    label = { Text("Emoji Icon (e.g. 🥑)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            val price = priceStr.toDoubleOrNull() ?: 50.0
                            if (name.isNotBlank()) {
                                onConfirm(name, "veggies", unit, price, emoji)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BlinkGreenPrimary)
                    ) {
                        Text("Add to Store")
                    }
                }
            }
        }
    }
}
