package com.speakfreeenglish.app.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.outlined.RecordVoiceOver
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Intent
import android.net.Uri
import com.speakfreeenglish.app.R
import com.speakfreeenglish.app.model.AppTab
import com.speakfreeenglish.app.model.UserAccount
import com.speakfreeenglish.app.ui.theme.DarkBorder
import com.speakfreeenglish.app.ui.theme.DarkSurface
import com.speakfreeenglish.app.ui.theme.DarkSurfaceElevated
import com.speakfreeenglish.app.ui.theme.EmeraldAccent
import com.speakfreeenglish.app.ui.theme.EmeraldLight
import com.speakfreeenglish.app.ui.theme.GoldAccent
import com.speakfreeenglish.app.ui.theme.GoldLight
import com.speakfreeenglish.app.ui.theme.GoldSurface
import com.speakfreeenglish.app.ui.theme.LocalAdaptive
import com.speakfreeenglish.app.ui.theme.PurpleLight
import com.speakfreeenglish.app.ui.theme.PurpleSurface
import com.speakfreeenglish.app.ui.theme.TextMuted
import com.speakfreeenglish.app.ui.theme.TextPrimary
import com.speakfreeenglish.app.ui.theme.TextSecondary

@Composable
fun HomeScreen(
    user: UserAccount,
    onFindPartnerClicked: () -> Unit,
    onNavigateTab: (AppTab) -> Unit,
    onOpenAuth: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val adaptive = LocalAdaptive.current

    val infiniteTransition = rememberInfiniteTransition(label = "PulseTransition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowAlpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = adaptive.horizontalPadding, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Online status banner
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
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
                text = "1,480+ Learners Active Online",
                color = EmeraldLight,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Hero Title
        Text(
            text = "Anonymous Voice Practice",
            color = TextPrimary,
            fontSize = if (adaptive.isCompactWidth) 20.sp else 24.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.5).sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "Free & instant pairing. No login required for random calls.",
            color = TextSecondary,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(if (adaptive.isCompactHeight) 12.dp else 24.dp))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(adaptive.pulseSize)
        ) {
            // Halo glow
            Box(
                modifier = Modifier
                    .size(adaptive.pulseSize * 0.91f)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                EmeraldAccent.copy(alpha = glowAlpha),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Ring
            Box(
                modifier = Modifier
                    .size(adaptive.pulseSize * 0.75f)
                    .clip(CircleShape)
                    .border(1.5.dp, EmeraldAccent.copy(alpha = 0.4f), CircleShape)
            )

            // Button
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(adaptive.pulseSize * 0.64f)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF1E293B),
                                Color(0xFF0F172A)
                            )
                        )
                    )
                    .border(2.dp, EmeraldAccent, CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onFindPartnerClicked
                    )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.RecordVoiceOver,
                        contentDescription = "Speak",
                        tint = EmeraldLight,
                        modifier = Modifier.size(34.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "START CALL",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        Text(
            text = "Unlimited Free Calls (Max 10 min/call)",
            color = EmeraldLight,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Feature Action Cards (Friends Direct Call & Subscription Plan)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Friends Card
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurface)
                    .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                    .clickable { onNavigateTab(AppTab.FRIENDS) }
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(PurpleSurface)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = "Friends",
                                tint = PurpleLight,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                      Text(
                          text = "My Friends",
                          color = TextPrimary,
                          fontSize = 14.sp,
                          fontWeight = FontWeight.SemiBold,
                          maxLines = 1,
                          overflow = TextOverflow.Ellipsis
                      )
                      Text(
                          text = "Direct call & text chat",
                          color = TextMuted,
                          fontSize = 11.sp,
                          maxLines = 2,
                          overflow = TextOverflow.Ellipsis,
                          modifier = Modifier.padding(top = 2.dp)
                      )
                }
            }

            // 5 Month Plan Card
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(GoldSurface)
                    .border(1.dp, GoldAccent.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                    .clickable { onNavigateTab(AppTab.SUBSCRIPTION) }
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(GoldAccent.copy(alpha = 0.2f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Diamond,
                                contentDescription = "VIP Plan",
                                tint = GoldLight,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(GoldAccent)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "BEST",
                                color = Color.Black,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "₹100 / 5 Months",
                            color = GoldLight,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Direct calling & chat",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Popular Topics
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(DarkSurface)
                .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Text(
                text = "Today's Conversation Starters",
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TopicItem("Dream Job & Goals", Modifier.weight(1f))
                TopicItem("Travel Bucket List", Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TopicItem("Favorite Movies", Modifier.weight(1f))
                TopicItem("AI & Future Tech", Modifier.weight(1f))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Privacy note
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Shield,
                contentDescription = "Safe",
                tint = TextMuted,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = "18+ • Encrypted live voice • No recordings saved",
                color = TextMuted,
                fontSize = 11.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = "Privacy Policy",
            color = EmeraldLight,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .padding(bottom = 12.dp)
                .clickable {
                    val url = context.getString(R.string.privacy_policy_url)
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                }
        )
    }
}

@Composable
fun TopicItem(label: String, modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(DarkSurfaceElevated)
            .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
            .padding(vertical = 10.dp, horizontal = 8.dp)
    ) {
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}
