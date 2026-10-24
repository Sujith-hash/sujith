package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ElectricBike
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Order
import com.example.model.OrderStatus
import com.example.ui.components.LiveTrackingMapView
import com.example.ui.theme.BlinkGreenPrimary
import com.example.ui.theme.BlinkYellowAccent
import com.example.viewmodel.BlinkViewModel

@Composable
fun LiveOrderTrackingScreen(
    orderId: String,
    viewModel: BlinkViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val orders by viewModel.orders.collectAsState()
    val order = orders.find { it.id == orderId } ?: orders.firstOrNull()

    if (order == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No active order found.")
        }
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("live_order_tracking_screen"),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Header
        item {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Order #${order.id}",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = BlinkYellowAccent,
                                contentColor = Color.Black
                            ) {
                                Text(
                                    text = "⚡ 10-15m Delivery",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "${order.items.sumOf { it.quantity }} items • ₹${order.grandTotal.toInt()} • ${order.paymentMethod.label}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Live Vector Map
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                LiveTrackingMapView(order = order)
            }
        }

        // ETA & Delivery OTP Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = BlinkGreenPrimary.copy(alpha = 0.10f)),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, BlinkGreenPrimary)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FlashOn,
                                contentDescription = "Speed",
                                tint = BlinkGreenPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (order.status == OrderStatus.DELIVERED) "DELIVERED AT DOORSTEP" else "ARRIVING IN ~${order.estimatedMinutes} MINS",
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                color = BlinkGreenPrimary
                            )
                        }
                        Text(
                            text = order.status.subtitle,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Security OTP Badge
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "DELIVERY OTP",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = BlinkGreenPrimary,
                            contentColor = Color.White
                        ) {
                            Text(
                                text = order.deliveryOtp,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                letterSpacing = 2.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // Delivery Partner Profile Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = BlinkGreenPrimary,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.ElectricBike,
                                contentDescription = "Rider",
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = order.riderName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = BlinkYellowAccent
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Rating",
                                        tint = Color.Black,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Text(
                                        text = " ${order.riderRating}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                }
                            }
                        }

                        Text(
                            text = order.riderVehicle,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Vaccinated",
                                tint = BlinkGreenPrimary,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = " Sanitized thermal box • Zero emission",
                                fontSize = 11.sp,
                                color = BlinkGreenPrimary
                            )
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = BlinkGreenPrimary.copy(alpha = 0.12f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        IconButton(onClick = { }) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Call Rider",
                                tint = BlinkGreenPrimary
                            )
                        }
                    }
                }
            }
        }

        // 4-Step Order Status Timeline
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Delivery Milestone Steps",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    val steps = listOf(
                        OrderStatus.PLACED,
                        OrderStatus.PACKING,
                        OrderStatus.OUT_FOR_DELIVERY,
                        OrderStatus.DELIVERED
                    )

                    steps.forEachIndexed { index, stepStatus ->
                        val isDone = order.status.stepIndex >= stepStatus.stepIndex
                        val isCurrent = order.status == stepStatus ||
                                (stepStatus == OrderStatus.OUT_FOR_DELIVERY && order.status == OrderStatus.NEARBY)

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isDone) BlinkGreenPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isDone) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Done",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(Color.White, CircleShape)
                                        )
                                    }
                                }

                                if (index < steps.size - 1) {
                                    Box(
                                        modifier = Modifier
                                            .width(2.dp)
                                            .height(30.dp)
                                            .background(
                                                if (order.status.stepIndex > stepStatus.stepIndex) BlinkGreenPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                            )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = stepStatus.title,
                                    fontWeight = if (isCurrent) FontWeight.Black else FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = if (isCurrent) BlinkGreenPrimary else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = stepStatus.subtitle,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                }
            }
        }

        // Ordered Items Summary
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Items in this bag (${order.items.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    order.items.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(item.product.emoji, fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${item.quantity}x ${item.product.name}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Text(
                                text = "₹${(item.product.discountedPrice * item.quantity).toInt()}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
