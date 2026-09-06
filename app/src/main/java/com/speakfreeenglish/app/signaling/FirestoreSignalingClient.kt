package com.speakfreeenglish.app.signaling

import android.util.Log
import com.speakfreeenglish.app.model.IceCandidatePayload
import com.speakfreeenglish.app.model.WaitingUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription
import java.util.UUID

/**
 * Serverless WebRTC Signaling Client powered by Firebase Firestore Free Spark Plan.
 * 
 * ZERO-COST ARCHITECTURE:
 * 1. Anonymous temporary matchmaking in "waiting_room".
 * 2. Transient SDP offer/answer and ICE candidate exchange via "rooms/{roomId}".
 * 3. The exact millisecond the peer-to-peer audio connection is established, all
 *    documents and subcollections are IMMEDIATELY deleted, and listeners detached.
 *    Result: Zero persistent database storage and minimal free tier read/write consumption.
 */
class FirestoreSignalingClient(
    initialUserId: String = UUID.randomUUID().toString()
) {

    companion object {
        private const val TAG = "SignalingClient"
        private const val COLLECTION_WAITING = "waiting_room"
        private const val COLLECTION_ROOMS = "rooms"
        private const val COLLECTION_DIRECT_CALLS = "direct_calls"
        private const val CANDIDATES_CALLER = "callerCandidates"
        private const val CANDIDATES_CALLEE = "calleeCandidates"
    }

    private val db = FirebaseFirestore.getInstance()
    private var localUserId: String = initialUserId
    val userId: String get() = localUserId

    interface Callback {
        fun onMatchFound(roomId: String, isCaller: Boolean, partnerId: String)
        fun onIncomingFriendCall(roomId: String, callerId: String, callerName: String)
        fun onDirectCallDeclined()
        fun onDirectCallCancelled()
        fun onOfferReceived(offer: SessionDescription)
        fun onAnswerReceived(answer: SessionDescription)
        fun onRemoteIceCandidateReceived(candidate: IceCandidate)
        fun onError(message: String)
    }

    private var callback: Callback? = null
    private var currentRoomId: String? = null
    private var isCaller: Boolean = false
    private var partnerId: String? = null
    private var outgoingCalleeId: String? = null
    private var incomingCallerId: String? = null
    private var incomingCallerName: String? = null

    // Listener registrations to cleanly detach immediately
    private var waitingRoomListener: ListenerRegistration? = null
    private var roomListener: ListenerRegistration? = null
    private var candidatesListener: ListenerRegistration? = null
    private var incomingCallListener: ListenerRegistration? = null
    private var outgoingCallListener: ListenerRegistration? = null

    private var operationId = 0
    private var lastAppliedOfferSdp: String? = null
    private var lastAppliedAnswerSdp: String? = null
    private var lastIncomingInviteKey: String? = null
    private var blockedPartnerIds: Set<String> = emptySet()

    private fun beginOperation(): Int {
        lastAppliedOfferSdp = null
        lastAppliedAnswerSdp = null
        return ++operationId
    }

    private fun isCurrent(op: Int): Boolean = op == operationId

    fun setBlockedPartnerIds(ids: Set<String>) {
        blockedPartnerIds = ids
    }

    private fun isFreshWaitingDoc(doc: DocumentSnapshot): Boolean {
        val created = doc.getTimestamp("createdAt")?.toDate()?.time ?: return true
        return System.currentTimeMillis() - created < 60_000L
    }

    fun setCallback(cb: Callback) {
        this.callback = cb
    }

    fun bindUser(accountUserId: String) {
        val nextId = accountUserId.trim()
        if (nextId.isBlank() || nextId == localUserId) return
        incomingCallListener?.remove()
        incomingCallListener = null
        localUserId = nextId
        startIncomingCallListener()
    }

    fun startIncomingCallListener() {
        incomingCallListener?.remove()
        incomingCallListener = null
        if (localUserId.isBlank()) return
        incomingCallListener = db.collection(COLLECTION_DIRECT_CALLS)
            .document(localUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Incoming call listener error", error)
                    return@addSnapshotListener
                }
                if (snapshot == null || !snapshot.exists()) {
                    if (incomingCallerId != null && currentRoomId == null) {
                        incomingCallerId = null
                        incomingCallerName = null
                        callback?.onDirectCallCancelled()
                    }
                    return@addSnapshotListener
                }

                val status = snapshot.getString("status") ?: return@addSnapshotListener
                val callerId = snapshot.getString("callerId") ?: return@addSnapshotListener
                val roomId = snapshot.getString("roomId") ?: return@addSnapshotListener
                val callerName = snapshot.getString("callerName") ?: "Friend"

                if (callerId == localUserId) return@addSnapshotListener

                when (status) {
                    "ringing" -> {
                        val inviteKey = "$roomId:$callerId"
                        if (lastIncomingInviteKey == inviteKey) return@addSnapshotListener
                        lastIncomingInviteKey = inviteKey
                        incomingCallerId = callerId
                        incomingCallerName = callerName
                        callback?.onIncomingFriendCall(roomId, callerId, callerName)
                    }
                    "cancelled" -> {
                        incomingCallerId = null
                        incomingCallerName = null
                        snapshot.reference.delete()
                        callback?.onDirectCallCancelled()
                    }
                }
            }
    }

    /**
     * Direct call to a specific friend. Does not enter the random waiting room.
     */
    fun startDirectCallWithFriend(calleeId: String, callerName: String) {
        val cleanCalleeId = calleeId.trim()
        if (cleanCalleeId.isBlank() || cleanCalleeId == localUserId) {
            callback?.onError("Cannot call this friend")
            return
        }

        val op = beginOperation()
        isCaller = true
        partnerId = cleanCalleeId
        outgoingCalleeId = cleanCalleeId
        incomingCallerId = null
        incomingCallerName = null
        val generatedRoomId = UUID.randomUUID().toString()
        currentRoomId = generatedRoomId

        val roomData = hashMapOf(
            "roomId" to generatedRoomId,
            "callerId" to localUserId,
            "calleeId" to cleanCalleeId,
            "callType" to "direct",
            "createdAt" to FieldValue.serverTimestamp()
        )
        val inviteData = hashMapOf(
            "status" to "ringing",
            "roomId" to generatedRoomId,
            "callerId" to localUserId,
            "callerName" to callerName,
            "calleeId" to cleanCalleeId,
            "createdAt" to FieldValue.serverTimestamp()
        )

        db.collection(COLLECTION_ROOMS).document(generatedRoomId)
            .set(roomData)
            .addOnSuccessListener {
                if (!isCurrent(op)) {
                    db.collection(COLLECTION_ROOMS).document(generatedRoomId).delete()
                    return@addOnSuccessListener
                }
                db.collection(COLLECTION_DIRECT_CALLS).document(cleanCalleeId)
                    .set(inviteData)
                    .addOnSuccessListener {
                        if (!isCurrent(op)) {
                            db.collection(COLLECTION_DIRECT_CALLS).document(cleanCalleeId).delete()
                            db.collection(COLLECTION_ROOMS).document(generatedRoomId).delete()
                            return@addOnSuccessListener
                        }
                        listenToOutgoingDirectCall(cleanCalleeId, generatedRoomId)
                    }
                    .addOnFailureListener { error ->
                        db.collection(COLLECTION_ROOMS).document(generatedRoomId).delete()
                        if (!isCurrent(op)) return@addOnFailureListener
                        currentRoomId = null
                        partnerId = null
                        outgoingCalleeId = null
                        callback?.onError("Could not ring friend: ${error.localizedMessage}")
                    }
            }
            .addOnFailureListener { error ->
                if (!isCurrent(op)) return@addOnFailureListener
                currentRoomId = null
                partnerId = null
                outgoingCalleeId = null
                callback?.onError("Could not start friend call: ${error.localizedMessage}")
            }
    }

    fun acceptIncomingFriendCall(roomId: String, callerId: String) {
        isCaller = false
        partnerId = callerId
        currentRoomId = roomId
        incomingCallerId = callerId
        outgoingCalleeId = null

        db.collection(COLLECTION_DIRECT_CALLS).document(localUserId)
            .update("status", "accepted")
            .addOnSuccessListener {
                listenToRoomUpdates(roomId)
                listenToRemoteCandidates(roomId, isCaller = false)
                callback?.onMatchFound(roomId, isCaller = false, partnerId = callerId)
            }
            .addOnFailureListener { error ->
                callback?.onError("Could not accept call: ${error.localizedMessage}")
            }
    }

    fun declineIncomingFriendCall() {
        db.collection(COLLECTION_DIRECT_CALLS).document(localUserId)
            .update("status", "declined")
            .addOnCompleteListener {
                db.collection(COLLECTION_DIRECT_CALLS).document(localUserId).delete()
            }
        incomingCallerId = null
        incomingCallerName = null
        currentRoomId = null
        partnerId = null
    }

    private fun listenToOutgoingDirectCall(calleeId: String, roomId: String) {
        outgoingCallListener?.remove()
        outgoingCallListener = db.collection(COLLECTION_DIRECT_CALLS)
            .document(calleeId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot == null || !snapshot.exists()) return@addSnapshotListener
                val status = snapshot.getString("status") ?: return@addSnapshotListener
                when (status) {
                    "declined" -> {
                        outgoingCallListener?.remove()
                        outgoingCallListener = null
                        snapshot.reference.delete()
                        callback?.onDirectCallDeclined()
                    }
                    "cancelled" -> {
                        outgoingCallListener?.remove()
                        outgoingCallListener = null
                    }
                    "accepted" -> {
                        outgoingCallListener?.remove()
                        outgoingCallListener = null
                        listenToRoomUpdates(roomId)
                        listenToRemoteCandidates(roomId, isCaller = true)
                        callback?.onMatchFound(roomId, isCaller = true, partnerId = calleeId)
                    }
                }
            }
    }

    /**
     * Step 1: Matchmaking.
     * Looks for an existing waiting peer in "waiting_room".
     * If found, pairs with them immediately. Otherwise, posts self as waiting.
     */
    fun startMatchmaking() {
        val op = beginOperation()
        db.collection(COLLECTION_WAITING)
            .whereEqualTo("status", "waiting")
            .limit(8)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!isCurrent(op)) return@addOnSuccessListener
                val availablePeerDoc = querySnapshot.documents.firstOrNull { doc ->
                    doc.id != userId &&
                        doc.getString("status") == "waiting" &&
                        !blockedPartnerIds.contains(doc.id) &&
                        isFreshWaitingDoc(doc)
                }

                if (availablePeerDoc != null) {
                    pairWithPeer(availablePeerDoc, op)
                } else {
                    registerSelfAsWaiting(op)
                }
            }
            .addOnFailureListener { error ->
                if (!isCurrent(op)) return@addOnFailureListener
                Log.e(TAG, "Error querying waiting room", error)
                callback?.onError("Matchmaking query failed: ${error.localizedMessage}")
            }
    }

    /**
     * Pair with an available peer found in the waiting room.
     */
    private fun pairWithPeer(peerDoc: DocumentSnapshot, op: Int) {
        if (!isCurrent(op)) return
        val peerId = peerDoc.id
        val generatedRoomId = UUID.randomUUID().toString()
        isCaller = true
        partnerId = peerId
        currentRoomId = generatedRoomId

        // Atomic transaction to claim the peer and avoid race conditions
        val peerRef = db.collection(COLLECTION_WAITING).document(peerId)
        db.runTransaction { transaction ->
            val snapshot = transaction.get(peerRef)
            val currentStatus = snapshot.getString("status")
            if (currentStatus == "waiting") {
                transaction.update(
                    peerRef,
                    mapOf(
                        "status" to "matched",
                        "roomId" to generatedRoomId,
                        "matchedWith" to userId
                    )
                )
                true
            } else {
                false
            }
        }.addOnSuccessListener { claimed ->
            if (!isCurrent(op)) return@addOnSuccessListener
            if (claimed) {
                val roomData = hashMapOf(
                    "roomId" to generatedRoomId,
                    "callerId" to userId,
                    "calleeId" to peerId,
                    "createdAt" to FieldValue.serverTimestamp()
                )
                db.collection(COLLECTION_ROOMS).document(generatedRoomId)
                    .set(roomData)
                    .addOnSuccessListener {
                        if (!isCurrent(op)) return@addOnSuccessListener
                        listenToRoomUpdates(generatedRoomId)
                        listenToRemoteCandidates(generatedRoomId, isCaller = true)
                        callback?.onMatchFound(generatedRoomId, isCaller = true, partnerId = peerId)
                    }
            } else {
                registerSelfAsWaiting(op)
            }
        }.addOnFailureListener {
            if (isCurrent(op)) registerSelfAsWaiting(op)
        }
    }

    /**
     * Registers current user as waiting in Firestore.
     */
    private fun registerSelfAsWaiting(op: Int) {
        if (!isCurrent(op)) return
        isCaller = false
        val myWaitingDoc = db.collection(COLLECTION_WAITING).document(userId)
        val data = hashMapOf(
            "userId" to userId,
            "status" to "waiting",
            "createdAt" to FieldValue.serverTimestamp()
        )

        waitingRoomListener?.remove()
        waitingRoomListener = null
        myWaitingDoc.set(data, SetOptions.merge()).addOnSuccessListener {
            if (!isCurrent(op)) {
                myWaitingDoc.delete()
                return@addOnSuccessListener
            }
            waitingRoomListener = myWaitingDoc.addSnapshotListener { snapshot, error ->
                if (!isCurrent(op)) return@addSnapshotListener
                if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener

                val status = snapshot.getString("status")
                val roomId = snapshot.getString("roomId")
                val matchedWith = snapshot.getString("matchedWith")

                if (status == "matched" && roomId != null && matchedWith != null) {
                    if (blockedPartnerIds.contains(matchedWith)) {
                        myWaitingDoc.delete()
                        callback?.onError("Matched partner is blocked. Searching again is required.")
                        return@addSnapshotListener
                    }
                    currentRoomId = roomId
                    partnerId = matchedWith
                    waitingRoomListener?.remove()
                    waitingRoomListener = null

                    listenToRoomUpdates(roomId)
                    listenToRemoteCandidates(roomId, isCaller = false)
                    callback?.onMatchFound(roomId, isCaller = false, partnerId = matchedWith)
                }
            }
        }.addOnFailureListener { error ->
            if (!isCurrent(op)) return@addOnFailureListener
            callback?.onError("Failed to enter waiting room: ${error.localizedMessage}")
        }
    }

    /**
     * Listen for SDP offer (if callee) or SDP answer (if caller).
     */
    private fun listenToRoomUpdates(roomId: String) {
        roomListener?.remove()
        roomListener = null
        val roomDoc = db.collection(COLLECTION_ROOMS).document(roomId)
        roomListener = roomDoc.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener

            if (!isCaller) {
                // Callee listens for Offer
                val offerMap = snapshot.get("offer") as? Map<*, *>
                if (offerMap != null) {
                    val sdpType = offerMap["type"] as? String
                    val sdpDescription = offerMap["sdp"] as? String
                    if (sdpType != null && sdpDescription != null && sdpDescription != lastAppliedOfferSdp) {
                        lastAppliedOfferSdp = sdpDescription
                        val sessionDesc = SessionDescription(
                            SessionDescription.Type.fromCanonicalForm(sdpType.lowercase()),
                            sdpDescription
                        )
                        callback?.onOfferReceived(sessionDesc)
                    }
                }
            } else {
                val answerMap = snapshot.get("answer") as? Map<*, *>
                if (answerMap != null) {
                    val sdpType = answerMap["type"] as? String
                    val sdpDescription = answerMap["sdp"] as? String
                    if (sdpType != null && sdpDescription != null && sdpDescription != lastAppliedAnswerSdp) {
                        lastAppliedAnswerSdp = sdpDescription
                        val sessionDesc = SessionDescription(
                            SessionDescription.Type.fromCanonicalForm(sdpType.lowercase()),
                            sdpDescription
                        )
                        callback?.onAnswerReceived(sessionDesc)
                    }
                }
            }
        }
    }

    /**
     * Send SDP Offer (Caller).
     */
    fun sendOffer(roomId: String, sdp: SessionDescription) {
        val offerData = mapOf(
            "type" to sdp.type.canonicalForm(),
            "sdp" to sdp.description
        )
        db.collection(COLLECTION_ROOMS).document(roomId)
            .update("offer", offerData)
    }

    /**
     * Send SDP Answer (Callee).
     */
    fun sendAnswer(roomId: String, sdp: SessionDescription) {
        val answerData = mapOf(
            "type" to sdp.type.canonicalForm(),
            "sdp" to sdp.description
        )
        db.collection(COLLECTION_ROOMS).document(roomId)
            .update("answer", answerData)
    }

    /**
     * Stream local ICE candidate to Firestore.
     */
    fun sendIceCandidate(roomId: String, isCaller: Boolean, candidate: IceCandidate) {
        val subcollection = if (isCaller) CANDIDATES_CALLER else CANDIDATES_CALLEE
        val candidateData = hashMapOf(
            "sdpMid" to (candidate.sdpMid ?: ""),
            "sdpMLineIndex" to candidate.sdpMLineIndex,
            "sdp" to candidate.sdp,
            "senderId" to userId
        )
        db.collection(COLLECTION_ROOMS).document(roomId)
            .collection(subcollection)
            .add(candidateData)
    }

    /**
     * Listen for remote ICE candidates sent by peer.
     */
    private fun listenToRemoteCandidates(roomId: String, isCaller: Boolean) {
        candidatesListener?.remove()
        candidatesListener = null
        val remoteSubcollection = if (isCaller) CANDIDATES_CALLEE else CANDIDATES_CALLER
        candidatesListener = db.collection(COLLECTION_ROOMS).document(roomId)
            .collection(remoteSubcollection)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                for (change in snapshot.documentChanges) {
                    if (change.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                        val doc = change.document
                        val sdpMid = doc.getString("sdpMid") ?: ""
                        val sdpMLineIndex = doc.getLong("sdpMLineIndex")?.toInt() ?: 0
                        val sdp = doc.getString("sdp") ?: ""

                        if (sdp.isNotEmpty()) {
                            val candidate = IceCandidate(sdpMid, sdpMLineIndex, sdp)
                            callback?.onRemoteIceCandidateReceived(candidate)
                        }
                    }
                }
            }
    }

    /**
     * CRITICAL ZERO-COST TRIGGER:
     * Called the exact millisecond WebRTC PeerConnection reaches CONNECTED state.
     * Instantly deletes the signaling room, subcollections, and waiting entries from Firestore,
     * and cancels all Firestore snapshot listeners.
     */
    fun cleanupFirestoreOnConnected() {
        Log.d(TAG, "WebRTC connected! Purging signaling documents for zero-cost operation.")

        // 1. Immediately detach listeners so zero reads occur
        detachAllListeners()

        // 2. Delete waiting room entries and any leftover direct-call invite
        db.collection(COLLECTION_WAITING).document(userId).delete()
        partnerId?.let { db.collection(COLLECTION_WAITING).document(it).delete() }
        db.collection(COLLECTION_DIRECT_CALLS).document(userId).delete()
        outgoingCalleeId?.let { db.collection(COLLECTION_DIRECT_CALLS).document(it).delete() }

        // 3. Delete room document and candidate collections
        currentRoomId?.let { roomId ->
            val roomRef = db.collection(COLLECTION_ROOMS).document(roomId)
            
            // Delete caller candidates
            roomRef.collection(CANDIDATES_CALLER).get().addOnSuccessListener { snaps ->
                for (doc in snaps.documents) { doc.reference.delete() }
            }
            // Delete callee candidates
            roomRef.collection(CANDIDATES_CALLEE).get().addOnSuccessListener { snaps ->
                for (doc in snaps.documents) { doc.reference.delete() }
            }
            // Delete main room doc
            roomRef.delete()
        }
    }

    /**
     * Cleanly cancels matchmaking or call when user clicks Cancel/End.
     */
    fun cancelOrDisconnect() {
        operationId++
        lastAppliedOfferSdp = null
        lastAppliedAnswerSdp = null
        lastIncomingInviteKey = null
        val outgoingId = outgoingCalleeId
        detachAllListeners()
        db.collection(COLLECTION_WAITING).document(userId).delete()
        currentRoomId?.let { roomId ->
            db.collection(COLLECTION_ROOMS).document(roomId).delete()
        }
        if (outgoingId != null) {
            db.collection(COLLECTION_DIRECT_CALLS).document(outgoingId)
                .update("status", "cancelled")
                .addOnCompleteListener {
                    db.collection(COLLECTION_DIRECT_CALLS).document(outgoingId).delete()
                }
        }
        db.collection(COLLECTION_DIRECT_CALLS).document(userId).delete()
        currentRoomId = null
        partnerId = null
        outgoingCalleeId = null
        incomingCallerId = null
        incomingCallerName = null
        startIncomingCallListener()
    }

    private fun detachAllListeners() {
        waitingRoomListener?.remove()
        waitingRoomListener = null

        roomListener?.remove()
        roomListener = null

        candidatesListener?.remove()
        candidatesListener = null

        outgoingCallListener?.remove()
        outgoingCallListener = null
    }
}
