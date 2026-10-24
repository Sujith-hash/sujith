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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.Order
import com.example.model.OrderStatus
import com.example.ui.components.LiveTrackingMapView
import com.example.ui.theme.BlinkGreenPrimary
import com.example.ui.theme.BlinkRedTag
import com.example.ui.theme.BlinkYellowAccent
import com.example.viewmodel.BlinkViewModel

@Composable
fun RiderScreen(
    viewModel: BlinkViewModel,
    modifier: Modifier = Modifier
) {
    val riderProfile by viewModel.riderProfile.collectAsState()
    val orders by viewModel.orders.collectAsState()

    // Find current assigned or active trip
    val activeDelivery = orders.firstOrNull {
        it.status != OrderStatus.DELIVERED && it.status != OrderStatus.CANCELLED
    }

    var showOtpDialog by remember { mutableStateOf(false) }
    var otpInput by remember { mutableStateOf("") }
    var otpError by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("rider_partner_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Rider Header & Duty Switch
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = BlinkGreenPrimary,
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.DirectionsBike,
                                        contentDescription = "Rider",
                                        tint = Color.White,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = riderProfile.name,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 16.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = BlinkYellowAccent
                                    ) {
                                        Text(
                                            text = "★ ${riderProfile.rating}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "${riderProfile.vehicleNumber} • ${if (riderProfile.isDutyOn) "DUTY ON 🟢" else "OFFLINE 🔴"}",
                                    fontSize = 12.sp,
                                    color = if (riderProfile.isDutyOn) BlinkGreenPrimary else BlinkRedTag,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Switch(
                            checked = riderProfile.isDutyOn,
                            onCheckedChange = { viewModel.toggleDutyStatus() },
                            colors = SwitchDefaults.colors(checkedThumbColor = BlinkGreenPrimary)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Rider Earnings Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RiderStatPill(
                            title = "Today's Earnings",
                            value = "₹${riderProfile.todayEarnings.toInt()}",
                            icon = "💰",
                            modifier = Modifier.weight(1f)
                        )
                        RiderStatPill(
                            title = "Trips Done",
                            value = "${riderProfile.completedTrips}",
                            icon = "⚡",
                            modifier = Modifier.weight(1f)
                        )
                        RiderStatPill(
                            title = "On-Time Rate",
                            value = "99.2%",
                            icon = "🎯",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Active Trip Card
        if (activeDelivery != null) {
            item {
                Text(
                    text = "Active 10-15 Min Delivery Trip",
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )
            }

            // Real-time Navigation Turn-by-Turn Overlay Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = BlinkGreenPrimary,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Navigation,
                                    contentDescription = "Turn Right",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "In 150m, Turn Right onto 12th Main Road",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Fastest route via 100ft Road • ETA: ${activeDelivery.estimatedMinutes} mins",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Embedded Live Vector Navigation Map
            item {
                LiveTrackingMapView(order = activeDelivery)
            }

            // Delivery Details & Actions
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Deliver to: ${activeDelivery.customerName}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = activeDelivery.deliveryAddress,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { }) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = "Call Customer",
                                    tint = BlinkGreenPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Instructions: \"${activeDelivery.deliveryInstructions}\"",
                                fontSize = 11.sp,
                                modifier = Modifier.padding(8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Stage progression buttons for Rider
                        when (activeDelivery.status) {
                            OrderStatus.PLACED, OrderStatus.MERCHANT_ACCEPTED, OrderStatus.PACKING -> {
                                Button(
                                    onClick = { viewModel.riderAcceptOrder(activeDelivery.id) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = BlinkGreenPrimary),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Text("Pick Up Bags & Start Navigation 🛵", fontWeight = FontWeight.Bold)
                                }
                            }
                            OrderStatus.OUT_FOR_DELIVERY -> {
                                Button(
                                    onClick = { viewModel.riderUpdateStatus(activeDelivery.id, OrderStatus.NEARBY) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = BlinkGreenPrimary),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Text("Mark: Arrived at Customer Location 📍", fontWeight = FontWeight.Bold)
                                }
                            }
                            OrderStatus.NEARBY -> {
                                Button(
                                    onClick = { showOtpDialog = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = BlinkYellowAccent, contentColor = Color.Black),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Text("Enter Delivery OTP & Complete (₹65)", fontWeight = FontWeight.Black)
                                }
                            }
                            OrderStatus.DELIVERED -> {
                                Text(
                                    text = "Trip Completed Successfully! ₹65 credited.",
                                    color = BlinkGreenPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            else -> {}
                        }
                    }
                }
            }
        } else {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🟢", fontSize = 36.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Waiting for next quick order",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            "You are in high-demand Indiranagar hotspot.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    // OTP Verification Dialog for Delivery Agent
    if (showOtpDialog && activeDelivery != null) {
        Dialog(onDismissRequest = { showOtpDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Verify Delivery OTP", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        "Ask customer for the 4-digit code shown on their tracking screen (Demo OTP: ${activeDelivery.deliveryOtp})",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = otpInput,
                        onValueChange = {
                            if (it.length <= 4) otpInput = it
                            otpError = false
                        },
                        placeholder = { Text("4-digit OTP") },
                        isError = otpError,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (otpError) {
                        Text(
                            "Invalid OTP. Check demo OTP above.",
                            color = BlinkRedTag,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showOtpDialog = false }) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                val verified = viewModel.riderVerifyDelivery(activeDelivery.id, otpInput)
                                if (verified) {
                                    showOtpDialog = false
                                } else {
                                    otpError = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BlinkGreenPrimary)
                        ) {
                            Text("Verify & Finish")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RiderStatPill(title: String, value: String, icon: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(icon, fontSize = 16.sp)
            Text(value, fontWeight = FontWeight.Black, fontSize = 14.sp)
            Text(title, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
