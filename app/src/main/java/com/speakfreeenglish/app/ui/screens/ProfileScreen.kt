package com.speakfreeenglish.app.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.Diamond
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.speakfreeenglish.app.R
import com.speakfreeenglish.app.model.AppTab
import com.speakfreeenglish.app.model.UserAccount
import com.speakfreeenglish.app.ui.theme.DarkBg
import com.speakfreeenglish.app.ui.theme.DarkBorder
import com.speakfreeenglish.app.ui.theme.DarkSurface
import com.speakfreeenglish.app.ui.theme.DarkSurfaceElevated
import com.speakfreeenglish.app.ui.theme.EmeraldAccent
import com.speakfreeenglish.app.ui.theme.EmeraldLight
import com.speakfreeenglish.app.ui.theme.GoldAccent
import com.speakfreeenglish.app.ui.theme.GoldLight
import com.speakfreeenglish.app.ui.theme.GoldSurface
import com.speakfreeenglish.app.ui.theme.LocalAdaptive
import com.speakfreeenglish.app.ui.theme.PurpleAccent
import com.speakfreeenglish.app.ui.theme.PurpleLight
import com.speakfreeenglish.app.ui.theme.RedEndCall
import com.speakfreeenglish.app.ui.theme.TextMuted
import com.speakfreeenglish.app.ui.theme.TextPrimary
import com.speakfreeenglish.app.ui.theme.TextSecondary

@Composable
fun ProfileScreen(
    user: UserAccount,
    onOpenAuth: () -> Unit,
    onLogout: () -> Unit,
    onNavigateTab: (AppTab) -> Unit,
    onUpdateProfileImage: (String?) -> Unit = {}
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val adaptive = LocalAdaptive.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            try {
                val takeFlags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(uri, takeFlags)
            } catch (_: Exception) {
            }
            onUpdateProfileImage(uri.toString())
            Toast.makeText(context, "Profile photo updated from Gallery!", Toast.LENGTH_SHORT).show()
        }
    }

    val avatarGradients = listOf(
        Color(0xFF3B82F6),
        Color(0xFF10B981),
        Color(0xFF8B5CF6),
        Color(0xFFF59E0B),
        Color(0xFFEC4899)
    )
    val avatarColor = avatarGradients[user.avatarColorIndex % avatarGradients.size]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = adaptive.horizontalPadding, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        Text(
            text = "My Profile",
            color = TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
        )

        // Profile Avatar Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(DarkSurface)
                .border(1.dp, DarkBorder, RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                // Large Avatar with Gallery click picker
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(86.dp)
                        .clip(CircleShape)
                        .clickable {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(avatarColor.copy(alpha = 0.2f))
                            .border(2.5.dp, if (user.isSubscribed) GoldAccent else EmeraldLight, CircleShape)
                    ) {
                        if (!user.profileImageUri.isNullOrBlank()) {
                            AsyncImage(
                                model = user.profileImageUri,
                                contentDescription = "Profile Photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        } else {
                            Text(
                                text = user.displayName.take(1).uppercase(),
                                color = if (user.isSubscribed) GoldLight else EmeraldLight,
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Camera Badge at bottom-right
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(DarkSurfaceElevated)
                            .border(1.5.dp, EmeraldLight, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Pick from Gallery",
                            tint = EmeraldLight,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
                ) {
                    Text(
                        text = user.displayName,
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Clip,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (user.isSubscribed) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(GoldSurface)
                                .border(1.dp, GoldAccent, RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "VIP PRO",
                                color = GoldLight,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Text(
                    text = when {
                        user.phoneNumber.isNotBlank() -> user.phoneNumber
                        user.isGuest -> "Guest Learner (Anonymous)"
                        user.email.isNotBlank() -> user.email
                        else -> "Registered Learner"
                    },
                    color = TextMuted,
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Clip,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp, horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))


            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Practice Statistics
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                icon = Icons.Default.Phone,
                iconColor = EmeraldLight,
                value = "${user.totalCallsMade}",
                label = "Calls Practiced",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                icon = Icons.Default.Timer,
                iconColor = PurpleLight,
                value = "${user.totalTalkTimeSeconds / 60}m",
                label = "Speaking Time",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // VIP Plan Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(if (user.isSubscribed) GoldSurface else DarkSurface)
                .border(1.dp, if (user.isSubscribed) GoldAccent else DarkBorder, RoundedCornerShape(16.dp))
                .clickable { onNavigateTab(AppTab.SUBSCRIPTION) }
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Diamond,
                    contentDescription = "VIP",
                    tint = GoldLight,
                    modifier = Modifier.size(24.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (user.isSubscribed) "VIP 5-Month Pass (Active)" else "₹100 5-Months VIP Plan",
                        color = GoldLight,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Clip
                    )
                    Text(
                        text = if (user.isSubscribed) "Expires: ${user.subscriptionExpiryDate}" else "Direct friend calling & text chat",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Clip
                    )
                }
                Text(
                    text = if (user.isSubscribed) "Details" else "Upgrade",
                    color = GoldLight,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }

        }

        if (user.isGuest) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(EmeraldAccent)
                    .clickable { onOpenAuth() }
                    .padding(vertical = 14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Login",
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Login / Sign Up",
                        color = Color.Black,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkSurfaceElevated)
                    .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
                    .clickable { onLogout() }
                    .padding(vertical = 14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Logout",
                        tint = RedEndCall,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Logout (Switch to Guest)",
                        color = RedEndCall,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Privacy Policy",
            color = EmeraldLight,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .clickable {
                    val url = context.getString(R.string.privacy_policy_url)
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                }
                .padding(bottom = 8.dp)
        )

        Text(
            text = "SpeakFree v1.0 • 18+ Random Voice Chat",
            color = TextMuted,
            fontSize = 11.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )
    }
}

@Composable
fun StatCard(
    icon: ImageVector,
    iconColor: Color,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = label,
                color = TextMuted,
                fontSize = 11.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
