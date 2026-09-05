package com.speakfreeenglish.app.ui.screens

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
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.speakfreeenglish.app.model.UserAccount
import com.speakfreeenglish.app.ui.theme.DarkBorder
import com.speakfreeenglish.app.ui.theme.DarkSurface
import com.speakfreeenglish.app.ui.theme.DarkSurfaceElevated
import com.speakfreeenglish.app.ui.theme.EmeraldAccent
import com.speakfreeenglish.app.ui.theme.EmeraldLight
import com.speakfreeenglish.app.ui.theme.GoldAccent
import com.speakfreeenglish.app.ui.theme.GoldLight
import com.speakfreeenglish.app.ui.theme.LocalAdaptive
import com.speakfreeenglish.app.ui.theme.PurpleLight
import com.speakfreeenglish.app.ui.theme.TextMuted
import com.speakfreeenglish.app.ui.theme.TextPrimary
import com.speakfreeenglish.app.ui.theme.TextSecondary

@Composable
fun SubscriptionScreen(
    user: UserAccount,
    isBillingProcessing: Boolean = false,
    billingMessage: String? = null,
    onSubscribeGooglePlay: () -> Unit,
    onOpenAuth: () -> Unit,
    onDismissBillingMessage: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val adaptive = LocalAdaptive.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = adaptive.horizontalPadding, vertical = 12.dp)
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Diamond,
                contentDescription = "VIP",
                tint = GoldLight,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "SpeakFree VIP Pass",
                color = TextPrimary,
                fontSize = if (adaptive.isCompactWidth) 18.sp else 22.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Text(
            text = "Connect directly with friends & practice unlimited speaking",
            color = TextSecondary,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 16.dp)
        )

        // Billing Alert Message (e.g. error, cancellation)
        if (!billingMessage.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF2A1C16))
                    .border(1.dp, Color(0xFFE06C45), RoundedCornerShape(12.dp))
                    .clickable { onDismissBillingMessage() }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Alert",
                        tint = Color(0xFFFFA07A),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = billingMessage,
                        color = Color(0xFFFDE8E1),
                        fontSize = 12.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "✕",
                        color = TextMuted,
                        fontSize = 14.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // If User is already subscribed -> Active VIP Card with Verified Google Play Receipt info
        if (user.isSubscribed) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF2E230B), Color(0xFF1E1707))
                        )
                    )
                    .border(1.5.dp, GoldAccent, RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Active",
                            tint = GoldLight,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "VIP PRO ACTIVE",
                            color = GoldLight,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(GoldAccent)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "5-MO",
                                color = Color.Black,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Subscription Valid Until",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                    Text(
                        text = user.subscriptionExpiryDate.ifEmpty { "5 Months Active" },
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    if (user.googlePlayOrderId.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Google Play Order: ${user.googlePlayOrderId}",
                            color = GoldLight.copy(alpha = 0.7f),
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Unlocked: Unlimited Non-Stop Call Duration & Direct Friend Chat!",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        } else {
            // Highlighted ₹100 / 5 Month Plan Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF271F0C), Color(0xFF171307))
                        )
                    )
                    .border(2.dp, GoldAccent, RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(GoldAccent)
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "GOOGLE PLAY PASS",
                            color = Color.Black,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Price Tag
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "₹100",
                            color = GoldLight,
                            fontSize = if (adaptive.isCompactWidth) 32.sp else 38.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = " / 5 Months",
                            color = TextSecondary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                        )
                    }

                    Text(
                        text = "Just ₹20 per month • Official Google Play In-App Subscription",
                        color = GoldLight.copy(alpha = 0.85f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
        }

        // Benefits Checklist
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(DarkSurface)
                .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Everything Included in VIP Plan:",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            BenefitRow(
                icon = Icons.Default.Phone,
                iconColor = EmeraldLight,
                title = "Unlimited Non-Stop Call Duration",
                description = "No 10-minute call disconnects. Practice for as long as you desire."
            )

            BenefitRow(
                icon = Icons.Default.Chat,
                iconColor = PurpleLight,
                title = "Direct Friend Calling & Chat",
                description = "Directly call and text message favorite study buddies anytime."
            )

            BenefitRow(
                icon = Icons.Default.Star,
                iconColor = GoldLight,
                title = "Verified VIP Crown Badge",
                description = "Distinguished crown badge on your profile and incoming call screens."
            )

            BenefitRow(
                icon = Icons.Default.FlashOn,
                iconColor = EmeraldAccent,
                title = "Priority HD Low-Latency Audio",
                description = "Real-time WebRTC audio connection with lowest latency."
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Official Google Play Billing CTA (If not subscribed)
        if (!user.isSubscribed) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurfaceElevated)
                    .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Official Google Play Assurance Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ShoppingBag,
                        contentDescription = "Google Play",
                        tint = EmeraldLight,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Google Play In-App Purchase",
                        color = EmeraldLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Official Google Play Button
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFFF59E0B), Color(0xFFD97706))
                            )
                        )
                        .clickable(enabled = !isBillingProcessing) {
                            if (user.isGuest) {
                                onOpenAuth()
                            } else {
                                onSubscribeGooglePlay()
                            }
                        }
                        .padding(vertical = 16.dp)
                ) {
                    if (isBillingProcessing) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                color = Color.Black,
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = "Connecting to Google Play...",
                                color = Color.Black,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Diamond,
                                contentDescription = "Subscribe",
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = if (user.isGuest) "Log In to Subscribe" else "Subscribe with Google Play",
                                color = Color.Black,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "🔒 Managed securely by Google Play. Instant activation upon purchase receipt verification. Cancel anytime in Google Play Store subscriptions.",
                    color = TextMuted,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun BenefitRow(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    description: String
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconColor.copy(alpha = 0.15f))
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconColor,
                modifier = Modifier.size(18.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = description,
                color = TextSecondary,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 1.dp)
            )
        }
    }
}
