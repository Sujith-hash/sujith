package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBike
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Order
import com.example.model.OrderStatus
import com.example.ui.theme.BlinkGreenPrimary
import com.example.ui.theme.BlinkYellowAccent

@Composable
fun LiveTrackingMapView(
    order: Order,
    modifier: Modifier = Modifier,
    onRecenterClicked: () -> Unit = {}
) {
    val isDark = MaterialTheme.colorScheme.background.red < 0.2f

    // Radar pulse animation for the rider's live GPS marker
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 14f,
        targetValue = 42f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha"
    )

    var showTraffic by remember { mutableStateOf(true) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(if (isDark) Color(0xFF0F172A) else Color(0xFFE2E8F0))
            .testTag("live_tracking_map_view")
    ) {
        // High fidelity procedural Vector Map with roads, parks, river, and delivery path
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // 1. Map base background
            val mapBaseColor = if (isDark) Color(0xFF131B2E) else Color(0xFFF1F5F9)
            drawRect(color = mapBaseColor)

            // 2. City park greenery patches
            val parkColor = if (isDark) Color(0xFF102820) else Color(0xFFDCFCE7)
            drawRoundRect(
                color = parkColor,
                topLeft = Offset(width * 0.08f, height * 0.12f),
                size = Size(width * 0.28f, height * 0.32f),
                cornerRadius = CornerRadius(16f, 16f)
            )
            drawRoundRect(
                color = parkColor,
                topLeft = Offset(width * 0.68f, height * 0.55f),
                size = Size(width * 0.25f, height * 0.35f),
                cornerRadius = CornerRadius(16f, 16f)
            )

            // 3. Curved river / canal across the district
            val waterColor = if (isDark) Color(0xFF1E293B) else Color(0xFFBAE6FD)
            val riverPath = Path().apply {
                moveTo(0f, height * 0.75f)
                cubicTo(
                    width * 0.35f, height * 0.90f,
                    width * 0.65f, height * 0.70f,
                    width, height * 0.85f
                )
            }
            drawPath(
                path = riverPath,
                color = waterColor,
                style = Stroke(width = 30f, cap = StrokeCap.Round)
            )

            // 4. Street Grid
            val roadColor = if (isDark) Color(0xFF243048) else Color(0xFFFFFFFF)
            val roadBorder = if (isDark) Color(0xFF1E293B) else Color(0xFFCBD5E1)

            // Horizontal Avenues
            val avenueY = listOf(0.25f, 0.45f, 0.70f)
            avenueY.forEach { yRatio ->
                val y = height * yRatio
                drawLine(
                    color = roadBorder,
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 24f
                )
                drawLine(
                    color = roadColor,
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 18f
                )
            }

            // Vertical / Diagonal Streets
            val streetX = listOf(0.20f, 0.42f, 0.65f, 0.88f)
            streetX.forEach { xRatio ->
                val x = width * xRatio
                drawLine(
                    color = roadBorder,
                    start = Offset(x, 0f),
                    end = Offset(x, height),
                    strokeWidth = 22f
                )
                drawLine(
                    color = roadColor,
                    start = Offset(x, 0f),
                    end = Offset(x, height),
                    strokeWidth = 16f
                )
            }

            // 5. Realistic Turn-by-Turn Delivery Route
            // Start: Dark Store (X: 18%, Y: 25%)
            // Waypoint 1: (42%, 25%)
            // Waypoint 2: (42%, 70%)
            // Destination: Customer Home (85%, 70%)
            val p0 = Offset(width * 0.18f, height * 0.25f)
            val p1 = Offset(width * 0.42f, height * 0.25f)
            val p2 = Offset(width * 0.42f, height * 0.70f)
            val p3 = Offset(width * 0.84f, height * 0.70f)

            // Full route path
            val fullRoute = Path().apply {
                moveTo(p0.x, p0.y)
                lineTo(p1.x, p1.y)
                lineTo(p2.x, p2.y)
                lineTo(p3.x, p3.y)
            }

            // Draw inactive remaining route
            drawPath(
                path = fullRoute,
                color = if (isDark) Color(0xFF475569) else Color(0xFF94A3B8),
                style = Stroke(
                    width = 10f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 12f), 0f)
                )
            )

            // Calculate current Rider Position along path segments
            val progress = order.liveProgress.coerceIn(0f, 1f)

            // Calculate total polyline length
            val seg1 = kotlin.math.abs(p1.x - p0.x)
            val seg2 = kotlin.math.abs(p2.y - p1.y)
            val seg3 = kotlin.math.abs(p3.x - p2.x)
            val totalLen = seg1 + seg2 + seg3

            val riderDist = progress * totalLen
            val riderPos: Offset = when {
                riderDist <= seg1 -> {
                    val ratio = riderDist / seg1
                    Offset(p0.x + (p1.x - p0.x) * ratio, p0.y)
                }
                riderDist <= (seg1 + seg2) -> {
                    val ratio = (riderDist - seg1) / seg2
                    Offset(p1.x, p1.y + (p2.y - p1.y) * ratio)
                }
                else -> {
                    val ratio = ((riderDist - seg1 - seg2) / seg3).coerceIn(0f, 1f)
                    Offset(p2.x + (p3.x - p2.x) * ratio, p2.y)
                }
            }

            // Draw Traveled Green Route
            val traveledPath = Path().apply {
                moveTo(p0.x, p0.y)
                if (riderDist > seg1) {
                    lineTo(p1.x, p1.y)
                    if (riderDist > seg1 + seg2) {
                        lineTo(p2.x, p2.y)
                        lineTo(riderPos.x, riderPos.y)
                    } else {
                        lineTo(riderPos.x, riderPos.y)
                    }
                } else {
                    lineTo(riderPos.x, riderPos.y)
                }
            }

            // Glow border for traveled path
            drawPath(
                path = traveledPath,
                color = BlinkGreenPrimary.copy(alpha = 0.35f),
                style = Stroke(width = 16f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            // Solid traveled line
            drawPath(
                path = traveledPath,
                color = BlinkGreenPrimary,
                style = Stroke(width = 8f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // 6. Draw Dark Store Hub Pin (p0)
            drawCircle(
                color = Color.Black.copy(alpha = 0.2f),
                radius = 18f,
                center = p0.copy(y = p0.y + 4f)
            )
            drawCircle(
                color = Color(0xFF0F172A),
                radius = 16f,
                center = p0
            )
            drawCircle(
                color = BlinkYellowAccent,
                radius = 7f,
                center = p0
            )

            // 7. Draw Destination Home Pin (p3)
            drawCircle(
                color = Color.Black.copy(alpha = 0.25f),
                radius = 20f,
                center = p3.copy(y = p3.y + 4f)
            )
            drawCircle(
                color = Color(0xFFDC2626),
                radius = 18f,
                center = p3
            )
            drawCircle(
                color = Color.White,
                radius = 7f,
                center = p3
            )

            // 8. Draw Moving Rider Live GPS Position & Radar Pulse
            if (order.status != OrderStatus.DELIVERED) {
                // Expanding radar pulse
                drawCircle(
                    color = BlinkGreenPrimary.copy(alpha = pulseAlpha),
                    radius = pulseRadius,
                    center = riderPos
                )
                // Shadow
                drawCircle(
                    color = Color.Black.copy(alpha = 0.35f),
                    radius = 16f,
                    center = riderPos.copy(y = riderPos.y + 4f)
                )
                // White outer ring
                drawCircle(
                    color = Color.White,
                    radius = 15f,
                    center = riderPos
                )
                // Vibrant Green core
                drawCircle(
                    color = BlinkGreenPrimary,
                    radius = 11f,
                    center = riderPos
                )
            }
        }

        // Top Floating Status Chips (Live Speed & ETA)
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.Black.copy(alpha = 0.8f),
                contentColor = Color.White,
                modifier = Modifier.shadow(4.dp, RoundedCornerShape(20.dp))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ElectricBike,
                        contentDescription = "EV Delivery",
                        tint = BlinkYellowAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = " 34 km/h • EV Ather",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = BlinkGreenPrimary,
                contentColor = Color.White,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .shadow(4.dp, RoundedCornerShape(20.dp))
            ) {
                Text(
                    text = if (order.status == OrderStatus.DELIVERED) "DELIVERED 🎉" else "⚡ ${order.estimatedMinutes} MINS ETA",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }

        // Map Corner Legend & Recenter Control
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 6.dp
            ) {
                IconButton(
                    onClick = onRecenterClicked,
                    modifier = Modifier.size(42.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Recenter live GPS",
                        tint = BlinkGreenPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Bottom Left Legend Labels
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Store,
                        contentDescription = "Store",
                        tint = Color.DarkGray,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(" Store #042 ", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    Text("➔ ", fontSize = 10.sp, color = BlinkGreenPrimary, fontWeight = FontWeight.Bold)
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Home",
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(" Home", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
