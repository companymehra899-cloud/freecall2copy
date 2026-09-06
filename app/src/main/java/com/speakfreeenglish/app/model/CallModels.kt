package com.speakfreeenglish.app.model

/**
 * State machine for the anonymous voice calling lifecycle.
 */
enum class CallState {
    IDLE,
    SEARCHING,
    RINGING,
    CONNECTING,
    IN_CALL,
    ENDED,
    ERROR
}

/**
 * Bottom Navigation tabs.
 */
enum class AppTab {
    HOME,
    FRIENDS,
    SUBSCRIPTION,
    PROFILE
}

/**
 * User account model supporting Guest mode & registered user profile.
 */
data class UserAccount(
    val userId: String = "",
    val displayName: String = "Guest Learner",
    val phoneNumber: String = "",
    val email: String = "",
    val isGuest: Boolean = true,
    val isSubscribed: Boolean = false,
    val subscriptionExpiryDate: String = "",
    val subscriptionExpiryTimestamp: Long = 0L,
    val googlePlayOrderId: String = "",
    val googlePlayPurchaseToken: String = "",
    val googlePlayProductId: String = "",
    val streakDays: Int = 0,
    val avatarColorIndex: Int = 0,
    val profileImageUri: String? = null,
    val totalCallsMade: Int = 0,
    val totalTalkTimeSeconds: Long = 0L,
    val ageConfirmed18: Boolean = false
)

/**
 * Friend contact model with live status.
 */
data class Friend(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val isOnline: Boolean = true,
    val avatarColorIndex: Int = 0,
    val lastMessage: String = "Tap to chat",
    val lastMessageTime: String = "Just now"
)

/**
 * Incoming or outgoing friend request waiting for accept.
 */
data class FriendRequest(
    val id: String = "",
    val fromUserId: String = "",
    val fromName: String = "",
    val fromEmail: String = "",
    val toUserId: String = "",
    val toName: String = "",
    val toEmail: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Direct 1-on-1 Text message model.
 */
data class ChatMessage(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val timeFormatted: String = ""
)

/**
 * Represents an entry in the Firestore "waiting_room" collection.
 */
data class WaitingUser(
    val userId: String = "",
    val status: String = "waiting", // "waiting", "matched"
    val roomId: String? = null,
    val matchedWith: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Represents a lightweight temporary signaling room in Firestore "rooms" collection.
 * This document and subcollections are DELETED the exact millisecond WebRTC connects.
 */
data class SignalingRoom(
    val roomId: String = "",
    val callerId: String = "",
    val calleeId: String = "",
    val offer: Map<String, Any>? = null,
    val answer: Map<String, Any>? = null,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Model for exchanging ICE candidates through Firestore.
 */
data class IceCandidatePayload(
    val sdpMid: String = "",
    val sdpMLineIndex: Int = 0,
    val sdp: String = "",
    val senderId: String = ""
)
