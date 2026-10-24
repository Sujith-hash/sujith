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
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.ElectricBike
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Order
import com.example.model.OrderStatus
import com.example.ui.theme.BlinkGreenPrimary
import com.example.ui.theme.BlinkYellowAccent
import com.example.viewmodel.BlinkViewModel

@Composable
fun AdminScreen(
    viewModel: BlinkViewModel,
    modifier: Modifier = Modifier
) {
    val adminMetrics by viewModel.adminMetrics.collectAsState()
    val orders by viewModel.orders.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("admin_dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Admin Header
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
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
                        color = Color(0xFF0F172A),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = "Admin",
                                tint = BlinkYellowAccent,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "BlinkMart Super Admin",
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp
                        )
                        Text(
                            text = "Real-time Operations & Quick Commerce Health",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // GMV & KPI Grid
        item {
            Text(
                text = "Key Performance Indicators (Today)",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AdminKpiCard(
                    title = "Total GMV",
                    value = "₹${adminMetrics.gmvToday.toInt()}",
                    subtitle = "+18.4% vs yesterday",
                    icon = "📈",
                    accentColor = BlinkGreenPrimary,
                    modifier = Modifier.weight(1f)
                )
                AdminKpiCard(
                    title = "Orders Placed",
                    value = "${adminMetrics.ordersToday}",
                    subtitle = "99.4% fulfilled",
                    icon = "🛍️",
                    accentColor = BlinkYellowAccent,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AdminKpiCard(
                    title = "Avg Delivery Time",
                    value = "${adminMetrics.avgDeliveryMinutes} mins",
                    subtitle = "10-15 min SLA safe",
                    icon = "⚡",
                    accentColor = BlinkGreenPrimary,
                    modifier = Modifier.weight(1f)
                )
                AdminKpiCard(
                    title = "Active Fleet",
                    value = "${adminMetrics.activeRidersCount} Riders",
                    subtitle = "8 Dark stores open",
                    icon = "🛵",
                    accentColor = Color(0xFF2563EB),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Surge Pricing & Weather Controls
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Dynamic Delivery Controls",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.FlashOn, contentDescription = "Surge", tint = BlinkYellowAccent, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("High-Demand Surge Pricing", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Text("Adds ₹15 surge fee during heavy demand spikes", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = adminMetrics.isSurgeActive,
                            onCheckedChange = { viewModel.toggleSurgeMode() },
                            colors = SwitchDefaults.colors(checkedThumbColor = BlinkGreenPrimary)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    androidx.compose.material3.HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Cloud, contentDescription = "Rain", tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Monsoon / Rain Safety Mode", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Text("Extends delivery window to 15-20 mins for rider safety", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = adminMetrics.isRainModeActive,
                            onCheckedChange = { viewModel.toggleRainMode() },
                            colors = SwitchDefaults.colors(checkedThumbColor = BlinkGreenPrimary)
                        )
                    }
                }
            }
        }

        // Live Ecosystem Order Stream
        item {
            Text(
                text = "Live Order Feed (${orders.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }

        items(orders) { order ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Order #${order.id}",
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "₹${order.grandTotal.toInt()}",
                                fontWeight = FontWeight.Bold,
                                color = BlinkGreenPrimary,
                                fontSize = 13.sp
                            )
                        }
                        Text(
                            text = "${order.customerName} • ${order.paymentMethod.label}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when (order.status) {
                            OrderStatus.DELIVERED -> BlinkGreenPrimary.copy(alpha = 0.15f)
                            OrderStatus.OUT_FOR_DELIVERY, OrderStatus.NEARBY -> BlinkYellowAccent.copy(alpha = 0.35f)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    ) {
                        Text(
                            text = order.status.title,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (order.status == OrderStatus.DELIVERED) BlinkGreenPrimary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminKpiCard(
    title: String,
    value: String,
    subtitle: String,
    icon: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
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
                Text(title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(icon, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(value, fontWeight = FontWeight.Black, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(subtitle, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BlinkGreenPrimary)
        }
    }
}
