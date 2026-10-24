package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Role
import com.example.ui.theme.BlinkGreenPrimary
import com.example.ui.theme.BlinkYellowAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlinkTopBar(
    currentRole: Role,
    onRoleSelected: (Role) -> Unit,
    notificationsCount: Int,
    onNotificationsClicked: () -> Unit,
    isCloudConnected: Boolean = true,
    onCloudClicked: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(top = 8.dp, bottom = 6.dp)) {
            // Row 1: Brand & Delivery Address + Cloud Status + Notifications
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Address & ETA pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { }
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(BlinkYellowAccent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = "10 Min Delivery",
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "11 MINS",
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                color = BlinkGreenPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "• Home",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Change Address",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = "B-402, Green Glen Heights, Indiranagar",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Cloud Sync Status Pill
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = BlinkGreenPrimary.copy(alpha = 0.12f),
                    modifier = Modifier
                        .clickable { onCloudClicked() }
                        .padding(end = 4.dp)
                        .testTag("cloud_status_pill")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDone,
                            contentDescription = "Google Cloud DB",
                            tint = BlinkGreenPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Google DB",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = BlinkGreenPrimary
                        )
                    }
                }

                // Notification Bell with Badge
                IconButton(
                    onClick = onNotificationsClicked,
                    modifier = Modifier.testTag("notifications_bell_button")
                ) {
                    BadgedBox(
                        badge = {
                            if (notificationsCount > 0) {
                                Badge(containerColor = BlinkGreenPrimary) {
                                    Text(
                                        text = notificationsCount.toString(),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row 2: Role Switcher Chips (User / Merchant / Rider / Admin)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Role.entries.forEach { role ->
                    val isSelected = currentRole == role
                    val icon = when (role) {
                        Role.CUSTOMER -> Icons.Default.ShoppingBag
                        Role.MERCHANT -> Icons.Default.Storefront
                        Role.RIDER -> Icons.Default.DirectionsBike
                        Role.ADMIN -> Icons.Default.AdminPanelSettings
                    }

                    FilterChip(
                        selected = isSelected,
                        onClick = { onRoleSelected(role) },
                        label = {
                            Text(
                                text = role.title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = role.title,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BlinkGreenPrimary,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("role_chip_${role.name.lowercase()}")
                    )
                }
            }
        }
    }
}
