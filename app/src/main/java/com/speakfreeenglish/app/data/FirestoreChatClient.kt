package com.speakfreeenglish.app.data

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.speakfreeenglish.app.model.ChatMessage
import com.speakfreeenglish.app.model.Friend
import com.speakfreeenglish.app.model.FriendRequest
import com.speakfreeenglish.app.model.UserAccount
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Live friend chat over Firestore.
 * Last 1 hour of messages is kept. Older messages are deleted.
 * If a chat is unused for 3 days, the whole thread is deleted.
 */
class FirestoreChatClient {

    companion object {
        private const val TAG = "FirestoreChatClient"
        private const val COLLECTION_USERS = "Users"
        private const val COLLECTION_FRIENDS = "friends"
        private const val COLLECTION_REQUESTS = "friend_requests"
        private const val COLLECTION_CHATS = "chats"
        private const val COLLECTION_MESSAGES = "messages"
        private const val KEEP_RECENT_MS = 60L * 60L * 1000L
        private const val CHAT_EXPIRE_MS = 3L * 24L * 60L * 60L * 1000L
    }

    private val db = FirebaseFirestore.getInstance()

    private var messagesListener: ListenerRegistration? = null
    private var friendsListener: ListenerRegistration? = null
    private var incomingRequestsListener: ListenerRegistration? = null
    private var currentChatId: String? = null

    fun chatIdFor(userA: String, userB: String): String {
        return listOf(userA, userB).sorted().joinToString("_")
    }

    fun upsertUserProfile(user: UserAccount) {
        if (user.isGuest || user.userId.isBlank()) return
        val data = hashMapOf<String, Any>(
            "userId" to user.userId,
            "name" to user.displayName,
            "nameLower" to user.displayName.trim().lowercase(),
            "email" to user.email.trim().lowercase(),
            "avatarColorIndex" to user.avatarColorIndex,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        db.collection(COLLECTION_USERS).document(user.userId)
            .set(data, SetOptions.merge())
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to upsert user profile", e)
            }
    }

    fun syncProfileOnLogin(user: UserAccount, onReady: () -> Unit) {
        if (user.isGuest || user.userId.isBlank()) return
        val ref = db.collection(COLLECTION_USERS).document(user.userId)
        val data = hashMapOf<String, Any>(
            "userId" to user.userId,
            "name" to user.displayName,
            "nameLower" to user.displayName.trim().lowercase(),
            "email" to user.email.trim().lowercase(),
            "avatarColorIndex" to user.avatarColorIndex,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        ref.set(data, SetOptions.merge())
            .addOnSuccessListener { onReady() }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to sync profile on login", e)
                onReady()
            }
    }

    fun listenFriends(userId: String, onUpdate: (List<Friend>) -> Unit) {
        friendsListener?.remove()
        if (userId.isBlank()) {
            onUpdate(emptyList())
            return
        }
        friendsListener = db.collection(COLLECTION_USERS).document(userId)
            .collection(COLLECTION_FRIENDS)
            .addSnapshotListener { snap, err ->
                if (err != null || snap == null) {
                    Log.e(TAG, "Friends listener error", err)
                    return@addSnapshotListener
                }
                val list = snap.documents.map { doc ->
                    Friend(
                        id = doc.getString("userId") ?: doc.id,
                        name = doc.getString("name") ?: "Learner",
                        email = doc.getString("email") ?: "",
                        isOnline = true,
                        avatarColorIndex = (doc.getLong("avatarColorIndex") ?: 0L).toInt(),
                        lastMessage = doc.getString("lastMessage") ?: "Tap to chat",
                        lastMessageTime = doc.getString("lastMessageTime") ?: ""
                    )
                }
                onUpdate(list)
            }
    }

    fun listenIncomingRequests(userId: String, onUpdate: (List<FriendRequest>) -> Unit) {
        incomingRequestsListener?.remove()
        if (userId.isBlank()) {
            onUpdate(emptyList())
            return
        }
        incomingRequestsListener = db.collection(COLLECTION_REQUESTS)
            .whereEqualTo("toUserId", userId)
            .addSnapshotListener { snap, err ->
                if (err != null || snap == null) {
                    Log.e(TAG, "Requests listener error", err)
                    return@addSnapshotListener
                }
                val list = snap.documents.mapNotNull { doc ->
                    if (doc.getString("status") != "pending") return@mapNotNull null
                    FriendRequest(
                        id = doc.id,
                        fromUserId = doc.getString("fromUserId") ?: "",
                        fromName = doc.getString("fromName") ?: "Learner",
                        fromEmail = doc.getString("fromEmail") ?: "",
                        toUserId = doc.getString("toUserId") ?: "",
                        toName = doc.getString("toName") ?: "",
                        toEmail = doc.getString("toEmail") ?: "",
                        createdAt = doc.getLong("createdAt") ?: 0L
                    )
                }
                onUpdate(list)
            }
    }

    fun sendFriendRequest(myUser: UserAccount, query: String, onResult: (Result<String>) -> Unit) {
        val clean = query.trim()
        if (clean.isEmpty()) {
            onResult(Result.failure(Exception("Enter a name or email")))
            return
        }
        if (myUser.isGuest || myUser.userId.isBlank()) {
            onResult(Result.failure(Exception("Login required to add friends")))
            return
        }

        val lower = clean.lowercase()
        val usersRef = db.collection(COLLECTION_USERS)
        val byEmail = usersRef.whereEqualTo("email", lower).limit(1)

        byEmail.get().addOnSuccessListener { emailSnap ->
            val found = emailSnap.documents.firstOrNull()
            if (found != null) {
                createRequest(myUser, found, onResult)
                return@addOnSuccessListener
            }
            usersRef.whereEqualTo("nameLower", clean.lowercase()).limit(1).get()
                .addOnSuccessListener { nameSnap ->
                    val nameDoc = nameSnap.documents.firstOrNull()
                    if (nameDoc != null) {
                        createRequest(myUser, nameDoc, onResult)
                        return@addOnSuccessListener
                    }
                    usersRef.whereEqualTo("name", clean).limit(1).get()
                        .addOnSuccessListener { fallbackSnap ->
                            val fallbackDoc = fallbackSnap.documents.firstOrNull()
                            if (fallbackDoc == null) {
                                onResult(Result.failure(Exception("No user found. Ask them to login first.")))
                            } else {
                                createRequest(myUser, fallbackDoc, onResult)
                            }
                        }
                        .addOnFailureListener { e -> onResult(Result.failure(e)) }
                }
                .addOnFailureListener { e -> onResult(Result.failure(e)) }
        }.addOnFailureListener { e -> onResult(Result.failure(e)) }
    }

    private fun createRequest(
        myUser: UserAccount,
        theirDoc: com.google.firebase.firestore.DocumentSnapshot,
        onResult: (Result<String>) -> Unit
    ) {
        val theirId = theirDoc.getString("userId") ?: theirDoc.id
        if (theirId == myUser.userId) {
            onResult(Result.failure(Exception("You cannot add yourself")))
            return
        }

        db.collection(COLLECTION_USERS).document(myUser.userId)
            .collection(COLLECTION_FRIENDS).document(theirId)
            .get()
            .addOnSuccessListener { already ->
                if (already.exists()) {
                    onResult(Result.failure(Exception("Already friends")))
                    return@addOnSuccessListener
                }

                val requestId = chatIdFor(myUser.userId, theirId)
                val reqRef = db.collection(COLLECTION_REQUESTS).document(requestId)
                reqRef.get().addOnSuccessListener { existing ->
                    val status = existing.getString("status")
                    if (status == "pending") {
                        onResult(Result.failure(Exception("Request already sent")))
                        return@addOnSuccessListener
                    }
                    val data = hashMapOf<String, Any>(
                        "fromUserId" to myUser.userId,
                        "fromName" to myUser.displayName,
                        "fromEmail" to myUser.email.trim().lowercase(),
                        "toUserId" to theirId,
                        "toName" to (theirDoc.getString("name") ?: "Learner"),
                        "toEmail" to (theirDoc.getString("email") ?: ""),
                        "status" to "pending",
                        "createdAt" to System.currentTimeMillis()
                    )
                    reqRef.set(data)
                        .addOnSuccessListener { onResult(Result.success("Request sent. Waiting for accept.")) }
                        .addOnFailureListener { e -> onResult(Result.failure(e)) }
                }.addOnFailureListener { e -> onResult(Result.failure(e)) }
            }
            .addOnFailureListener { e -> onResult(Result.failure(e)) }
    }

    fun acceptFriendRequest(myUser: UserAccount, request: FriendRequest, onDone: (Result<Unit>) -> Unit) {
        if (myUser.userId.isBlank()) return
        val theirId = request.fromUserId
        val myFriendDoc = hashMapOf<String, Any>(
            "userId" to theirId,
            "name" to request.fromName,
            "email" to request.fromEmail,
            "avatarColorIndex" to 0,
            "lastMessage" to "Say hi to start chatting",
            "lastMessageTime" to "Just now"
        )
        val theirFriendDoc = hashMapOf<String, Any>(
            "userId" to myUser.userId,
            "name" to myUser.displayName,
            "email" to myUser.email.trim().lowercase(),
            "avatarColorIndex" to myUser.avatarColorIndex,
            "lastMessage" to "Say hi to start chatting",
            "lastMessageTime" to "Just now"
        )
        val batch = db.batch()
        batch.set(
            db.collection(COLLECTION_USERS).document(myUser.userId)
                .collection(COLLECTION_FRIENDS).document(theirId),
            myFriendDoc
        )
        batch.set(
            db.collection(COLLECTION_USERS).document(theirId)
                .collection(COLLECTION_FRIENDS).document(myUser.userId),
            theirFriendDoc
        )
        batch.delete(db.collection(COLLECTION_REQUESTS).document(request.id))
        batch.commit()
            .addOnSuccessListener { onDone(Result.success(Unit)) }
            .addOnFailureListener { e -> onDone(Result.failure(e)) }
    }

    fun declineFriendRequest(requestId: String) {
        db.collection(COLLECTION_REQUESTS).document(requestId).delete()
    }

    fun openChat(myUserId: String, friendId: String, onMessages: (List<ChatMessage>) -> Unit) {
        detachMessages()
        val chatId = chatIdFor(myUserId, friendId)
        currentChatId = chatId
        val chatRef = db.collection(COLLECTION_CHATS).document(chatId)

        chatRef.get().addOnSuccessListener { doc ->
            val lastActivity = doc.getLong("lastActivityAt") ?: 0L
            if (lastActivity > 0L && System.currentTimeMillis() - lastActivity > CHAT_EXPIRE_MS) {
                purgeChat(chatId)
            }
        }

        chatRef.set(
            hashMapOf<String, Any>(
                "users" to listOf(myUserId, friendId),
                "lastActivityAt" to System.currentTimeMillis(),
                "updatedAt" to FieldValue.serverTimestamp()
            ),
            SetOptions.merge()
        )

        keepLastHourOnly(chatId)

        messagesListener = chatRef.collection(COLLECTION_MESSAGES)
            .orderBy("timestamp")
            .addSnapshotListener { snap, err ->
                if (err != null || snap == null) {
                    Log.e(TAG, "Messages listener error", err)
                    return@addSnapshotListener
                }
                val cutoff = System.currentTimeMillis() - KEEP_RECENT_MS
                val msgs = snap.documents.mapNotNull { d ->
                    val ts = d.getLong("timestamp") ?: 0L
                    if (ts < cutoff) {
                        d.reference.delete()
                        null
                    } else {
                        ChatMessage(
                            id = d.id,
                            senderId = d.getString("senderId") ?: "",
                            senderName = d.getString("senderName") ?: "",
                            text = d.getString("text") ?: "",
                            timestamp = ts,
                            timeFormatted = d.getString("timeFormatted") ?: ""
                        )
                    }
                }
                onMessages(msgs)
            }
    }

    fun sendMessage(myUser: UserAccount, friendId: String, text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty() || myUser.userId.isBlank()) return

        val chatId = currentChatId ?: chatIdFor(myUser.userId, friendId)
        val timeStr = SimpleDateFormat("hh:mm a", Locale.US).format(Date())
        val now = System.currentTimeMillis()
        val data = hashMapOf<String, Any>(
            "senderId" to myUser.userId,
            "senderName" to myUser.displayName,
            "text" to trimmed,
            "timestamp" to now,
            "timeFormatted" to timeStr
        )
        val chatRef = db.collection(COLLECTION_CHATS).document(chatId)
        chatRef.collection(COLLECTION_MESSAGES).add(data)
        chatRef.set(
            hashMapOf<String, Any>(
                "lastActivityAt" to now,
                "updatedAt" to FieldValue.serverTimestamp()
            ),
            SetOptions.merge()
        )
        keepLastHourOnly(chatId)

        val preview = hashMapOf<String, Any>(
            "lastMessage" to trimmed,
            "lastMessageTime" to timeStr
        )
        db.collection(COLLECTION_USERS).document(myUser.userId)
            .collection(COLLECTION_FRIENDS).document(friendId)
            .set(preview, SetOptions.merge())
        db.collection(COLLECTION_USERS).document(friendId)
            .collection(COLLECTION_FRIENDS).document(myUser.userId)
            .set(preview, SetOptions.merge())
    }

    fun closeChat() {
        detachMessages()
        currentChatId = null
    }

    fun purgeExpiredChats() {
        val cutoff = System.currentTimeMillis() - CHAT_EXPIRE_MS
        db.collection(COLLECTION_CHATS)
            .whereLessThan("lastActivityAt", cutoff)
            .get()
            .addOnSuccessListener { snaps ->
                for (doc in snaps.documents) {
                    purgeChat(doc.id)
                }
            }
    }

    fun detachAll() {
        detachMessages()
        friendsListener?.remove()
        friendsListener = null
        incomingRequestsListener?.remove()
        incomingRequestsListener = null
        currentChatId = null
    }

    private fun detachMessages() {
        messagesListener?.remove()
        messagesListener = null
    }

    private fun keepLastHourOnly(chatId: String) {
        val cutoff = System.currentTimeMillis() - KEEP_RECENT_MS
        db.collection(COLLECTION_CHATS).document(chatId)
            .collection(COLLECTION_MESSAGES)
            .whereLessThan("timestamp", cutoff)
            .get()
            .addOnSuccessListener { snaps ->
                for (doc in snaps.documents) {
                    doc.reference.delete()
                }
            }
    }

    private fun purgeChat(chatId: String) {
        Log.d(TAG, "Purging expired chat $chatId")
        val chatRef = db.collection(COLLECTION_CHATS).document(chatId)
        chatRef.collection(COLLECTION_MESSAGES).get().addOnSuccessListener { snaps ->
            val batch = db.batch()
            for (doc in snaps.documents) {
                batch.delete(doc.reference)
            }
            batch.delete(chatRef)
            batch.commit()
        }
    }
}
