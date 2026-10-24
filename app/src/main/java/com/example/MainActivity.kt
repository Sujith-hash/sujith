package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.auth.AuthManager
import com.example.data.repository.FirestoreRepository
import com.example.model.Role
import com.example.ui.components.BiometricAuthDialog
import com.example.ui.components.BlinkTopBar
import com.example.ui.components.CloudDatabaseDialog
import com.example.ui.components.NotificationsDialog
import com.example.ui.components.PushNotificationBanner
import com.example.ui.screens.AdminScreen
import com.example.ui.screens.LiveOrderTrackingScreen
import com.example.ui.screens.MerchantScreen
import com.example.ui.screens.RiderScreen
import com.example.ui.screens.UserCustomerScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.BlinkViewModel

class MainActivity : ComponentActivity() {

    private lateinit var firestoreRepository: FirestoreRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        firestoreRepository = FirestoreRepository.create(this)

        setContent {
            MyApplicationTheme {
                BlinkQuickCommerceApp(firestoreRepository = firestoreRepository)
            }
        }
    }
}

@Composable
fun BlinkQuickCommerceApp(
    firestoreRepository: FirestoreRepository,
    viewModel: BlinkViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.attachFirestore(firestoreRepository)
    }

    val activeRole by viewModel.activeRole.collectAsState()
    val activeToast by viewModel.activeToastNotification.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val showBiometric by viewModel.showBiometricDialog.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isCloudConnected by viewModel.isCloudConnected.collectAsState()

    var viewingTrackingOrderId by remember { mutableStateOf<String?>(null) }
    var showNotificationsDialog by remember { mutableStateOf(false) }
    var showCloudDialog by remember { mutableStateOf(false) }

    val dbId = try {
        context.getString(R.string.firestore_database_id)
    } catch (_: Exception) {
        "ai-studio-android-blinkmart"
    }

    // Android System Back Button Handler
    BackHandler(enabled = viewingTrackingOrderId != null) {
        viewingTrackingOrderId = null
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        topBar = {
            if (viewingTrackingOrderId == null) {
                BlinkTopBar(
                    currentRole = activeRole,
                    onRoleSelected = { newRole ->
                        viewingTrackingOrderId = null
                        viewModel.switchRole(newRole)
                    },
                    notificationsCount = notifications.size,
                    onNotificationsClicked = { showNotificationsDialog = true },
                    isCloudConnected = isCloudConnected,
                    onCloudClicked = { showCloudDialog = true }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top floating push alert banner
                PushNotificationBanner(
                    notification = activeToast,
                    onDismiss = { viewModel.dismissToast() },
                    onNotificationClick = { orderId ->
                        if (orderId != null) {
                            viewingTrackingOrderId = orderId
                        }
                    }
                )

                // Screen Switcher
                val screenState = Pair(viewingTrackingOrderId, activeRole)
                AnimatedContent(
                    targetState = screenState,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "screen_transition",
                    modifier = Modifier.fillMaxSize()
                ) { (trackingOrderId, role) ->
                    if (trackingOrderId != null) {
                        LiveOrderTrackingScreen(
                            orderId = trackingOrderId,
                            viewModel = viewModel,
                            onBack = { viewingTrackingOrderId = null }
                        )
                    } else {
                        when (role) {
                            Role.CUSTOMER -> {
                                UserCustomerScreen(
                                    viewModel = viewModel,
                                    onNavigateToLiveTracking = { orderId ->
                                        viewingTrackingOrderId = orderId
                                    }
                                )
                            }
                            Role.MERCHANT -> {
                                MerchantScreen(viewModel = viewModel)
                            }
                            Role.RIDER -> {
                                RiderScreen(viewModel = viewModel)
                            }
                            Role.ADMIN -> {
                                AdminScreen(viewModel = viewModel)
                            }
                        }
                    }
                }
            }

            // Biometric Auth Dialog
            if (showBiometric) {
                BiometricAuthDialog(
                    onDismissRequest = { viewModel.onBiometricDismissed() },
                    onSuccess = { viewModel.onBiometricConfirmed() }
                )
            }

            // Notification Center Modal
            if (showNotificationsDialog) {
                NotificationsDialog(
                    notifications = notifications,
                    onDismiss = { showNotificationsDialog = false },
                    onNotificationClick = { orderId ->
                        if (orderId != null) {
                            viewingTrackingOrderId = orderId
                        }
                    }
                )
            }

            // Cloud Database & Google Auth Dialog
            if (showCloudDialog) {
                CloudDatabaseDialog(
                    currentUser = currentUser,
                    databaseId = dbId,
                    onDismiss = { showCloudDialog = false },
                    onSignInWithGoogle = {
                        AuthManager.signInWithGoogle(
                            context = context,
                            scope = scope,
                            onSuccess = { user ->
                                Toast.makeText(context, "Welcome, ${user.displayName}!", Toast.LENGTH_SHORT).show()
                                showCloudDialog = false
                            },
                            onError = { err ->
                                Toast.makeText(context, "Sign In: $err", Toast.LENGTH_LONG).show()
                            }
                        )
                    },
                    onSignOut = {
                        AuthManager.signOut(context, scope) {
                            Toast.makeText(context, "Signed out of Google account", Toast.LENGTH_SHORT).show()
                            showCloudDialog = false
                        }
                    }
                )
            }
        }
    }
}
