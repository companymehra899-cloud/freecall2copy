package com.speakfreeenglish.app

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.RecordVoiceOver
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.speakfreeenglish.app.R
import com.speakfreeenglish.app.model.AppTab
import com.speakfreeenglish.app.model.CallState
import com.speakfreeenglish.app.ui.components.AppBottomBar
import com.speakfreeenglish.app.ui.components.AppTopBar
import com.speakfreeenglish.app.ui.screens.AuthDialog
import com.speakfreeenglish.app.ui.screens.DirectChatScreen
import com.speakfreeenglish.app.ui.screens.FriendsScreen
import com.speakfreeenglish.app.ui.screens.HomeScreen
import com.speakfreeenglish.app.ui.screens.ProfileScreen
import com.speakfreeenglish.app.ui.screens.SubscriptionScreen
import com.speakfreeenglish.app.ui.theme.DarkBg
import com.speakfreeenglish.app.ui.theme.DarkBorder
import com.speakfreeenglish.app.ui.theme.DarkSurface
import com.speakfreeenglish.app.ui.theme.DarkSurfaceElevated
import com.speakfreeenglish.app.ui.theme.EmeraldAccent
import com.speakfreeenglish.app.ui.theme.EmeraldLight
import com.speakfreeenglish.app.ui.theme.GoldAccent
import com.speakfreeenglish.app.ui.theme.GoldBorder
import com.speakfreeenglish.app.ui.theme.GoldLight
import com.speakfreeenglish.app.ui.theme.GoldSurface
import com.speakfreeenglish.app.ui.theme.LocalAdaptive
import com.speakfreeenglish.app.ui.theme.MyApplicationTheme
import com.speakfreeenglish.app.ui.theme.RedEndCall
import com.speakfreeenglish.app.ui.theme.TextMuted
import com.speakfreeenglish.app.ui.theme.TextPrimary
import com.speakfreeenglish.app.ui.theme.TextSecondary
import com.speakfreeenglish.app.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkBg
                ) {
                    SpeakFreeApp(viewModel)
                }
            }
        }
    }
}

@Composable
fun SpeakFreeApp(viewModel: MainViewModel) {
    val context = LocalContext.current
    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasAudioPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    var pendingFriendCall by remember { mutableStateOf<com.speakfreeenglish.app.model.Friend?>(null) }
    var pendingAcceptIncoming by remember { mutableStateOf(false) }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasAudioPermission = isGranted
        if (isGranted) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            val friend = pendingFriendCall
            val acceptIncoming = pendingAcceptIncoming
            pendingFriendCall = null
            pendingAcceptIncoming = false
            when {
                friend != null -> viewModel.startDirectCallWithFriend(friend)
                acceptIncoming -> viewModel.acceptIncomingFriendCall()
                else -> viewModel.findPartner()
            }
        } else {
            pendingFriendCall = null
            pendingAcceptIncoming = false
        }
    }

    val user by viewModel.currentUser.collectAsState()
    val friends by viewModel.friends.collectAsState()
    val incomingRequests by viewModel.incomingRequests.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()
    val showAuthDialog by viewModel.showAuthDialog.collectAsState()
    val activeChatFriend by viewModel.activeChatFriend.collectAsState()
    val activeChatMessages by viewModel.activeChatMessages.collectAsState()

    val callState by viewModel.callState.collectAsState()
    val callDuration by viewModel.callDurationFormatted.collectAsState()
    val callDurationSeconds by viewModel.callDurationSeconds.collectAsState()
    val isFreeLimitReached by viewModel.isFreeLimitReached.collectAsState()
    val searchingSecs by viewModel.searchingSeconds.collectAsState()
    val isMuted by viewModel.isMuted.collectAsState()
    val isSpeakerOn by viewModel.isSpeakerOn.collectAsState()
    val partnerLabel by viewModel.partnerLabel.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val isBillingProcessing by viewModel.isBillingProcessing.collectAsState()
    val billingMessage by viewModel.billingMessage.collectAsState()

    if (!user.ageConfirmed18) {
        AgeGateScreen(
            onConfirm = { viewModel.confirmAge18() }
        )
        return
    }

    if (callState == CallState.RINGING) {
        IncomingFriendCallScreen(
            callerName = partnerLabel,
            onAccept = {
                if (hasAudioPermission) {
                    viewModel.acceptIncomingFriendCall()
                } else {
                    pendingAcceptIncoming = true
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            },
            onDecline = { viewModel.declineIncomingFriendCall() }
        )
        return
    }

    // Active 1-on-1 Text Chat takes over screen if opened
    if (activeChatFriend != null && callState == CallState.IDLE) {
        DirectChatScreen(
            user = user,
            friend = activeChatFriend!!,
            messages = activeChatMessages,
            onSendMessage = { viewModel.sendChatMessage(it) },
            onDirectCall = {
                val friend = activeChatFriend ?: return@DirectChatScreen
                if (hasAudioPermission) {
                    viewModel.startDirectCallWithFriend(friend)
                } else {
                    pendingFriendCall = friend
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            },
            onBack = { viewModel.closeChat() },
            onOpenSubscription = {
                viewModel.closeChat()
                viewModel.selectTab(AppTab.SUBSCRIPTION)
            }
        )
        return
    }

    AnimatedContent(
        targetState = callState,
        transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
        label = "CallStateTransition"
    ) { state ->
        when (state) {
            CallState.IDLE -> {
                Scaffold(
                    topBar = {
                        AppTopBar(
                            user = user,
                            onAuthClicked = { viewModel.openAuthDialog() },
                            onSubscriptionClicked = { viewModel.selectTab(AppTab.SUBSCRIPTION) },
                            onNavigateTab = { viewModel.selectTab(it) }
                        )
                    },
                    bottomBar = {
                        AppBottomBar(
                            currentTab = currentTab,
                            onTabSelected = { viewModel.selectTab(it) },
                            hasSubscribed = user.isSubscribed
                        )
                    },
                    containerColor = DarkBg
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .imePadding()
                            .padding(innerPadding)
                    ) {
                        when (currentTab) {
                            AppTab.HOME -> {
                                HomeScreen(
                                    user = user,
                                    onFindPartnerClicked = {
                                        if (hasAudioPermission) {
                                            viewModel.findPartner()
                                        } else {
                                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                        }
                                    },
                                    onNavigateTab = { viewModel.selectTab(it) },
                                    onOpenAuth = { viewModel.openAuthDialog() }
                                )
                            }

                            AppTab.FRIENDS -> {
                                FriendsScreen(
                                    user = user,
                                    friends = friends,
                                    incomingRequests = incomingRequests,
                                    onAddFriend = { viewModel.addFriend(it) },
                                    onAcceptRequest = { viewModel.acceptFriendRequest(it) },
                                    onDeclineRequest = { viewModel.declineFriendRequest(it) },
                                    onDirectCallFriend = { friend ->
                                        if (hasAudioPermission) {
                                            viewModel.startDirectCallWithFriend(friend)
                                        } else {
                                            pendingFriendCall = friend
                                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                        }
                                    },
                                    onOpenChat = { friend ->
                                        viewModel.openChatWithFriend(friend)
                                    },
                                    onOpenSubscription = { viewModel.selectTab(AppTab.SUBSCRIPTION) },
                                    onOpenAuth = { viewModel.openAuthDialog() }
                                )
                            }

                            AppTab.SUBSCRIPTION -> {
                                SubscriptionScreen(
                                    user = user,
                                    isBillingProcessing = isBillingProcessing,
                                    billingMessage = billingMessage,
                                    onSubscribeGooglePlay = {
                                        (context as? Activity)?.let { act ->
                                            viewModel.launchGooglePlayPurchase(act)
                                        }
                                    },
                                    onOpenAuth = { viewModel.openAuthDialog() },
                                    onDismissBillingMessage = { viewModel.clearBillingMessage() }
                                )
                            }

                            AppTab.PROFILE -> {
                                ProfileScreen(
                                    user = user,
                                    onOpenAuth = { viewModel.openAuthDialog() },
                                    onLogout = { viewModel.logout() },
                                    onNavigateTab = { viewModel.selectTab(it) },
                                    onUpdateProfileImage = { viewModel.updateProfileImage(it) }
                                )
                            }
                        }
                    }
                }

                // Auth Dialog Modal
                if (showAuthDialog) {
                    AuthDialog(
                        onDismiss = { viewModel.closeAuthDialog() },
                        onSubmitAuth = { name, email, uid ->
                            viewModel.loginWithEmail(name, email, uid)
                        }
                    )
                }
            }

            CallState.SEARCHING, CallState.CONNECTING -> {
                SearchingScreen(
                    callState = state,
                    searchingSeconds = searchingSecs,
                    statusMessage = statusMessage,
                    partnerLabel = partnerLabel,
                    onCancelClicked = { viewModel.cancelSearch() }
                )
            }

            CallState.RINGING -> {
                IncomingFriendCallScreen(
                    callerName = partnerLabel,
                    onAccept = {
                        if (hasAudioPermission) {
                            viewModel.acceptIncomingFriendCall()
                        } else {
                            pendingAcceptIncoming = true
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    onDecline = { viewModel.declineIncomingFriendCall() }
                )
            }

            CallState.IN_CALL -> {
                CallingScreen(
                    partnerLabel = partnerLabel,
                    durationFormatted = callDuration,
                    durationSeconds = callDurationSeconds,
                    isSubscribed = user.isSubscribed,
                    isMuted = isMuted,
                    isSpeakerOn = isSpeakerOn,
                    onToggleMute = { viewModel.toggleMute() },
                    onToggleSpeaker = { viewModel.toggleSpeaker() },
                    onEndCall = { viewModel.endCall() },
                    onReportPartner = { viewModel.reportAndBlockPartner() },
                    onBlockPartner = { viewModel.blockPartner() }
                )
            }

            CallState.ENDED, CallState.ERROR -> {
                CallEndedScreen(
                    isLimitReached = isFreeLimitReached,
                    statusMessage = if (state == CallState.ERROR) statusMessage else if (isFreeLimitReached) "10-Minute Free Call Limit Reached" else "Call Completed",
                    onStartNextCall = {
                        if (hasAudioPermission) {
                            viewModel.findPartner()
                        } else {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    onOpenSubscription = {
                        viewModel.selectTab(AppTab.SUBSCRIPTION)
                    }
                )
            }
        }
    }
}

/**
 * Animated Searching Screen with Expanding Radar Rings & Cancel Button.
 */
@Composable
fun SearchingScreen(
    callState: CallState,
    searchingSeconds: Int,
    statusMessage: String,
    partnerLabel: String = "",
    onCancelClicked: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "RadarTransition")
    val wave1 by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Wave1"
    )
    val alpha1 by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Alpha1"
    )

    val adaptive = LocalAdaptive.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(adaptive.horizontalPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Status Top Badge
        Box(
            modifier = Modifier
                .padding(top = 24.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(DarkSurface)
                .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = when {
                    callState == CallState.CONNECTING && partnerLabel.isNotBlank() && partnerLabel != "Anonymous Partner" ->
                        "Connecting with $partnerLabel..."
                    callState == CallState.CONNECTING -> "Connecting to live call..."
                    partnerLabel.isNotBlank() && partnerLabel != "Anonymous Partner" -> "Calling $partnerLabel"
                    else -> "Looking for available speaker"
                },
                color = TextSecondary,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Animated Radar Center
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(adaptive.radarSize)
        ) {
            Box(
                modifier = Modifier
                    .size(adaptive.radarSize * 0.71f)
                    .scale(wave1)
                    .clip(CircleShape)
                    .border(2.dp, EmeraldAccent.copy(alpha = alpha1), CircleShape)
            )

            // Inner Core
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(adaptive.radarSize * 0.39f)
                    .clip(CircleShape)
                    .background(DarkSurfaceElevated)
                    .border(1.5.dp, EmeraldAccent, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Outlined.GraphicEq,
                    contentDescription = "Searching",
                    tint = EmeraldLight,
                    modifier = Modifier.size(if (adaptive.isCompactHeight) 32.dp else 44.dp)
                )
            }
        }

        // Searching text and Cancel Action
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = when {
                    callState == CallState.CONNECTING && partnerLabel.isNotBlank() && partnerLabel != "Anonymous Partner" ->
                        "Connecting with $partnerLabel..."
                    callState == CallState.CONNECTING -> "Connecting..."
                    partnerLabel.isNotBlank() && partnerLabel != "Anonymous Partner" -> "Calling $partnerLabel..."
                    else -> "Searching for Partner..."
                },
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "${searchingSeconds}s elapsed",
                color = TextSecondary,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            // Cancel Button
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkSurfaceElevated)
                    .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
                    .clickable { onCancelClicked() }
                    .padding(vertical = 16.dp)
            ) {
                Text(
                    text = if (partnerLabel.isNotBlank() && partnerLabel != "Anonymous Partner") "Cancel Call" else "Cancel Search",
                    color = TextSecondary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun IncomingFriendCallScreen(
    callerName: String,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Incoming Friend Call",
            color = TextSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 24.dp)
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(112.dp)
                    .clip(CircleShape)
                    .background(DarkSurfaceElevated)
                    .border(2.dp, EmeraldAccent, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Filled.Phone,
                    contentDescription = "Incoming call",
                    tint = EmeraldLight,
                    modifier = Modifier.size(44.dp)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = callerName.ifBlank { "Friend" },
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "wants to practice with you",
                color = TextSecondary,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 6.dp)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(RedEndCall)
                        .clickable { onDecline() }
                ) {
                    Icon(
                        imageVector = Icons.Filled.CallEnd,
                        contentDescription = "Decline",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Text(
                    text = "Decline",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(EmeraldAccent)
                        .clickable { onAccept() }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Call,
                        contentDescription = "Accept",
                        tint = Color.Black,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Text(
                    text = "Accept",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

/**
 * Active Calling Screen with live timer, audio waveforms, mute toggle, and End Call button.
 */
@Composable
fun CallingScreen(
    partnerLabel: String,
    durationFormatted: String,
    durationSeconds: Long,
    isSubscribed: Boolean,
    isMuted: Boolean,
    isSpeakerOn: Boolean,
    onToggleMute: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onEndCall: () -> Unit,
    onReportPartner: () -> Unit = {},
    onBlockPartner: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "AudioWave")
    val waveHeight1 by infiniteTransition.animateFloat(
        initialValue = 8f,
        targetValue = 28f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Wave1"
    )
    val waveHeight2 by infiniteTransition.animateFloat(
        initialValue = 20f,
        targetValue = 44f,
        animationSpec = infiniteRepeatable(
            animation = tween(550, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Wave2"
    )
    val waveHeight3 by infiniteTransition.animateFloat(
        initialValue = 12f,
        targetValue = 34f,
        animationSpec = infiniteRepeatable(
            animation = tween(480, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Wave3"
    )

    val remainingSeconds = (600L - durationSeconds).coerceAtLeast(0L)
    val remMins = remainingSeconds / 60
    val remSecs = remainingSeconds % 60
    val adaptive = LocalAdaptive.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = adaptive.horizontalPadding)
            .padding(top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Live Call Timer & Connection Badge
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkSurfaceElevated)
                    .border(1.dp, DarkBorder, RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(EmeraldAccent)
                )
                Text(
                    text = "Live Call",
                    color = EmeraldLight,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Free Call / VIP Plan Duration Status Pill (Small Timer)
            if (!isSubscribed) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (remainingSeconds <= 120L) Color(0xFF3B1B1A) else DarkSurfaceElevated
                        )
                        .border(
                            1.dp,
                            if (remainingSeconds <= 120L) Color(0xFFEF4444) else DarkBorder,
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (remainingSeconds <= 120L) {
                            "Free Limit: ${String.format(java.util.Locale.US, "%02d:%02d", remMins, remSecs)} left"
                        } else {
                            "Free: ${String.format(java.util.Locale.US, "%02d:%02d", remMins, remSecs)} left"
                        },
                        color = if (remainingSeconds <= 120L) Color(0xFFFCA5A5) else EmeraldLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(GoldSurface)
                        .border(1.dp, GoldBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "VIP PRO: Unlimited Duration",
                        color = GoldLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Center Partner Visual & Animated Waveform
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(DarkSurface)
                    .border(2.dp, DarkBorder, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Outlined.RecordVoiceOver,
                    contentDescription = "Partner Avatar",
                    tint = TextSecondary,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = partnerLabel,
                color = TextPrimary,
                fontSize = 19.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurfaceElevated)
                        .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
                        .clickable { onReportPartner() }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Flag,
                            contentDescription = "Report",
                            tint = Color(0xFFFCA5A5),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Report",
                            color = Color(0xFFFCA5A5),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurfaceElevated)
                        .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
                        .clickable { onBlockPartner() }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Block,
                            contentDescription = "Block",
                            tint = TextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Block",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Real-time voice animation bars
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.height(44.dp)
            ) {
                VoiceBar(waveHeight1)
                VoiceBar(waveHeight2)
                VoiceBar(waveHeight3)
                VoiceBar(waveHeight2)
                VoiceBar(waveHeight1)
            }
        }

        // Bottom Controls: Mute, Speaker, End Call
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 24.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Speakerphone Toggle
                CallControlButton(
                    icon = if (isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                    isActive = isSpeakerOn,
                    label = if (isSpeakerOn) "Speaker" else "Earpiece",
                    onClick = onToggleSpeaker
                )

                // End Call Button (Prominent Red)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(RedEndCall)
                        .clickable { onEndCall() }
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "End Call",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }

                // Microphone Mute Toggle
                CallControlButton(
                    icon = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                    isActive = !isMuted,
                    label = if (isMuted) "Unmute" else "Mute",
                    onClick = onToggleMute
                )
            }
        }
    }
}

@Composable
fun VoiceBar(heightDp: Float) {
    Box(
        modifier = Modifier
            .width(4.dp)
            .height(heightDp.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(EmeraldAccent)
    )
}

@Composable
fun CallControlButton(
    icon: ImageVector,
    isActive: Boolean,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(if (isActive) DarkSurfaceElevated else DarkSurface)
                .border(1.dp, if (isActive) DarkBorder else Color.Transparent, CircleShape)
                .clickable { onClick() }
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) TextPrimary else TextMuted,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun CallEndedScreen(
    isLimitReached: Boolean = false,
    statusMessage: String,
    onStartNextCall: () -> Unit = {},
    onOpenSubscription: () -> Unit = {}
) {
    val adaptive = LocalAdaptive.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(adaptive.horizontalPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(if (isLimitReached) GoldSurface else DarkSurfaceElevated)
                .border(1.dp, if (isLimitReached) GoldAccent else DarkBorder, CircleShape)
        ) {
            Icon(
                imageVector = if (isLimitReached) Icons.Default.Diamond else Icons.Default.CallEnd,
                contentDescription = "Ended",
                tint = if (isLimitReached) GoldLight else RedEndCall,
                modifier = Modifier.size(34.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = statusMessage,
            color = TextPrimary,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        if (isLimitReached) {
            Text(
                text = "Free users get 10 mins per person/call. Start another call anytime, or upgrade to 5-Month Pass for non-stop conversations.",
                color = TextSecondary,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 20.dp)
            )

            // Button to start next free call right away
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(EmeraldAccent)
                    .clickable { onStartNextCall() }
                    .padding(vertical = 14.dp)
            ) {
                Text(
                    text = "Start Next Free Call",
                    color = Color.Black,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Button to upgrade to 5-month plan
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkSurfaceElevated)
                    .border(1.dp, GoldAccent, RoundedCornerShape(14.dp))
                    .clickable { onOpenSubscription() }
                    .padding(vertical = 14.dp)
            ) {
                Text(
                    text = "Remove Limit (₹100 / 5 Months)",
                    color = GoldLight,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        } else {
            Text(
                text = "Returning to main screen...",
                color = TextMuted,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}

@Composable
fun AgeGateScreen(onConfirm: () -> Unit) {
    val context = LocalContext.current
    val adaptive = LocalAdaptive.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(adaptive.horizontalPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "18+ only",
            color = TextPrimary,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "SpeakFree is random live voice chat with other learners. You must be 18 or older. Do not share personal information. You can Report or Block any partner during a call.",
            color = TextSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Privacy Policy",
            color = EmeraldLight,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable {
                val url = context.getString(R.string.privacy_policy_url)
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
        )
        Spacer(modifier = Modifier.height(24.dp))
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(EmeraldAccent)
                .clickable { onConfirm() }
                .padding(vertical = 14.dp)
        ) {
            Text(
                text = "I am 18 or older",
                color = Color.Black,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
