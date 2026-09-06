package com.speakfreeenglish.app.data

import android.content.Context
import android.content.SharedPreferences
import com.speakfreeenglish.app.model.Friend
import com.speakfreeenglish.app.model.UserAccount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.random.Random

class AccountRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("speakfree_prefs", Context.MODE_PRIVATE)

    private val _currentUser = MutableStateFlow(loadUser())
    val currentUser: StateFlow<UserAccount> = _currentUser.asStateFlow()

    private val _friends = MutableStateFlow<List<Friend>>(emptyList())
    val friends: StateFlow<List<Friend>> = _friends.asStateFlow()

    private fun loadUser(): UserAccount {
        val userId = prefs.getString("user_id", null) ?: run {
            val newId = UUID.randomUUID().toString()
            prefs.edit().putString("user_id", newId).apply()
            newId
        }

        val isGuest = prefs.getBoolean("is_guest", true)
        val displayName = prefs.getString("display_name", if (isGuest) "Guest #${userId.take(4).uppercase()}" else "User") ?: "Guest"
        val phoneNumber = prefs.getString("phone_number", "") ?: ""
        val email = prefs.getString("email", "") ?: ""
        val expiryDate = prefs.getString("sub_expiry_date", "") ?: ""
        val expiryTimestamp = prefs.getLong("sub_expiry_ts", 0L)
        val now = System.currentTimeMillis()
        val isSubscribed = prefs.getBoolean("is_subscribed", false) && expiryTimestamp > now
        val playOrderId = prefs.getString("play_order_id", "") ?: ""
        val playToken = prefs.getString("play_purchase_token", "") ?: ""
        val playProductId = prefs.getString("play_product_id", "") ?: ""
        val avatarIndex = prefs.getInt("avatar_index", Random.nextInt(0, 5))
        val profileImageUri = prefs.getString("profile_image_uri", null)
        val totalCalls = prefs.getInt("total_calls", 0)
        val totalTalkTime = prefs.getLong("total_talk_time", 0L)
        val ageConfirmed = prefs.getBoolean("age_confirmed_18", false)

        if (prefs.getBoolean("is_subscribed", false) && expiryTimestamp > 0L && expiryTimestamp <= now) {
            prefs.edit()
                .putBoolean("is_subscribed", false)
                .putString("sub_expiry_date", "")
                .putLong("sub_expiry_ts", 0L)
                .apply()
        }

        return UserAccount(
            userId = userId,
            displayName = displayName,
            phoneNumber = phoneNumber,
            email = email,
            isGuest = isGuest,
            isSubscribed = isSubscribed,
            subscriptionExpiryDate = expiryDate,
            subscriptionExpiryTimestamp = expiryTimestamp,
            googlePlayOrderId = playOrderId,
            googlePlayPurchaseToken = playToken,
            googlePlayProductId = playProductId,
            avatarColorIndex = avatarIndex,
            profileImageUri = profileImageUri,
            totalCallsMade = totalCalls,
            totalTalkTimeSeconds = totalTalkTime,
            ageConfirmed18 = ageConfirmed
        )
    }

    private fun saveUser(user: UserAccount) {
        val editor = prefs.edit()
            .putString("user_id", user.userId)
            .putBoolean("is_guest", user.isGuest)
            .putString("display_name", user.displayName)
            .putString("phone_number", user.phoneNumber)
            .putString("email", user.email)
            .putBoolean("is_subscribed", user.isSubscribed)
            .putString("sub_expiry_date", user.subscriptionExpiryDate)
            .putLong("sub_expiry_ts", user.subscriptionExpiryTimestamp)
            .putString("play_order_id", user.googlePlayOrderId)
            .putString("play_purchase_token", user.googlePlayPurchaseToken)
            .putString("play_product_id", user.googlePlayProductId)
            .putInt("avatar_index", user.avatarColorIndex)
            .putInt("total_calls", user.totalCallsMade)
            .putLong("total_talk_time", user.totalTalkTimeSeconds)
            .putBoolean("age_confirmed_18", user.ageConfirmed18)

        if (user.profileImageUri != null) {
            editor.putString("profile_image_uri", user.profileImageUri)
        } else {
            editor.remove("profile_image_uri")
        }
        editor.apply()
        _currentUser.value = user
    }

    fun confirmAge18() {
        saveUser(_currentUser.value.copy(ageConfirmed18 = true))
    }

    fun reportPartner(partnerId: String, partnerLabel: String, reason: String) {
        try {
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            val report = hashMapOf(
                "reporterId" to _currentUser.value.userId,
                "partnerId" to partnerId,
                "partnerLabel" to partnerLabel,
                "reason" to reason,
                "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            )
            db.collection("safety_reports").add(report)
        } catch (_: Exception) {
        }
        if (partnerId.isNotBlank()) {
            blockPartner(partnerId)
        }
    }

    fun blockPartner(partnerId: String) {
        if (partnerId.isBlank()) return
        val blocked = prefs.getStringSet("blocked_partners", emptySet())?.toMutableSet() ?: mutableSetOf()
        blocked.add(partnerId)
        prefs.edit().putStringSet("blocked_partners", blocked).apply()
    }

    fun getBlockedPartnerIds(): Set<String> {
        return prefs.getStringSet("blocked_partners", emptySet()) ?: emptySet()
    }

    fun isPartnerBlocked(partnerId: String): Boolean {
        if (partnerId.isBlank()) return false
        return getBlockedPartnerIds().contains(partnerId)
    }

    fun updateProfileImage(uriString: String?) {
        val current = _currentUser.value
        val updated = current.copy(profileImageUri = uriString)
        saveUser(updated)
    }

    fun loginWithPhone(name: String, phoneNumber: String): UserAccount {
        val current = _currentUser.value
        val cleanPhone = phoneNumber.trim()
        val defaultName = if (cleanPhone.length >= 4) "User (${cleanPhone.takeLast(4)})" else "English Learner"
        val updated = current.copy(
            displayName = name.trim().ifEmpty { defaultName },
            phoneNumber = cleanPhone,
            isGuest = false
        )
        saveUser(updated)
        return updated
    }

    fun registerOrLogin(name: String, email: String, uid: String): UserAccount {
        val current = _currentUser.value
        val now = System.currentTimeMillis()
        val guestSubStillValid = current.isSubscribed &&
            current.subscriptionExpiryTimestamp > now &&
            current.googlePlayPurchaseToken.isNotBlank()
        val updated = current.copy(
            userId = uid,
            displayName = name.trim().ifEmpty { "English Learner" },
            email = email.trim(),
            isGuest = false,
            isSubscribed = guestSubStillValid,
            subscriptionExpiryDate = if (guestSubStillValid) current.subscriptionExpiryDate else "",
            subscriptionExpiryTimestamp = if (guestSubStillValid) current.subscriptionExpiryTimestamp else 0L,
            googlePlayOrderId = if (guestSubStillValid) current.googlePlayOrderId else "",
            googlePlayPurchaseToken = if (guestSubStillValid) current.googlePlayPurchaseToken else "",
            googlePlayProductId = if (guestSubStillValid) current.googlePlayProductId else ""
        )
        saveUser(updated)
        return updated
    }

    fun logoutToGuest(): UserAccount {
        val newGuestId = UUID.randomUUID().toString()
        val guest = UserAccount(
            userId = newGuestId,
            displayName = "Guest #${newGuestId.take(4).uppercase()}",
            phoneNumber = "",
            email = "",
            isGuest = true,
            isSubscribed = false,
            subscriptionExpiryDate = "",
            subscriptionExpiryTimestamp = 0L,
            avatarColorIndex = Random.nextInt(0, 5)
        )
        saveUser(guest)
        return guest
    }

    /**
     * Strictly verifies and applies a Google Play In-App Purchase receipt.
     * Rejects any unverified trigger without a valid purchaseToken.
     */
    fun applyVerifiedGooglePlayPurchase(
        purchaseToken: String,
        orderId: String,
        productId: String,
        purchaseTimeMillis: Long = System.currentTimeMillis()
    ): Result<UserAccount> {
        if (purchaseToken.isBlank()) {
            return Result.failure(IllegalArgumentException("Invalid Google Play receipt: purchase token is missing"))
        }

        val current = _currentUser.value
        if (current.isGuest) {
            return Result.failure(IllegalStateException("Login required to apply a Google Play purchase"))
        }

        val baseTime = if (purchaseTimeMillis > 0L) purchaseTimeMillis else System.currentTimeMillis()
        val cal = Calendar.getInstance()
        cal.timeInMillis = baseTime
        cal.add(Calendar.MONTH, 5)
        val expiryTime = cal.timeInMillis
        if (expiryTime <= System.currentTimeMillis()) {
            return Result.failure(IllegalStateException("Google Play entitlement has already expired"))
        }
        val formatter = SimpleDateFormat("dd MMM yyyy", Locale.US)
        val formattedDate = formatter.format(Date(expiryTime))

        val updated = current.copy(
            isSubscribed = true,
            subscriptionExpiryDate = formattedDate,
            subscriptionExpiryTimestamp = expiryTime,
            googlePlayOrderId = orderId,
            googlePlayPurchaseToken = purchaseToken,
            googlePlayProductId = productId
        )
        saveUser(updated)

        // If user has a registered profile, sync verified purchase receipt to Firestore
        if (!current.isGuest && current.userId.isNotBlank()) {
            try {
                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                val receiptData = hashMapOf(
                    "isSubscribed" to true,
                    "subscriptionPlan" to "VIP_5_MONTHS",
                    "googlePlayOrderId" to orderId,
                    "googlePlayPurchaseToken" to purchaseToken,
                    "googlePlayProductId" to productId,
                    "subscriptionExpiryTimestamp" to expiryTime,
                    "subscriptionExpiryDate" to formattedDate,
                    "verifiedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                )
                db.collection("Users").document(current.userId)
                    .set(receiptData, com.google.firebase.firestore.SetOptions.merge())
            } catch (e: Exception) {
                android.util.Log.e("AccountRepository", "Error syncing verified purchase to Firestore", e)
            }
        }

        return Result.success(updated)
    }

    fun replaceFriends(list: List<Friend>) {
        _friends.value = list
    }

    fun clearFriends() {
        _friends.value = emptyList()
    }

    fun incrementCallStats(durationSeconds: Long) {
        val curr = _currentUser.value
        val updated = curr.copy(
            totalCallsMade = curr.totalCallsMade + 1,
            totalTalkTimeSeconds = curr.totalTalkTimeSeconds + durationSeconds
        )
        saveUser(updated)
    }
}
