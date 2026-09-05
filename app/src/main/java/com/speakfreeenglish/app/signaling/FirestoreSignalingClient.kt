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
    val userId: String = UUID.randomUUID().toString()
) {

    companion object {
        private const val TAG = "SignalingClient"
        private const val COLLECTION_WAITING = "waiting_room"
        private const val COLLECTION_ROOMS = "rooms"
        private const val CANDIDATES_CALLER = "callerCandidates"
        private const val CANDIDATES_CALLEE = "calleeCandidates"
    }

    private val db = FirebaseFirestore.getInstance()

    interface Callback {
        fun onMatchFound(roomId: String, isCaller: Boolean, partnerId: String)
        fun onOfferReceived(offer: SessionDescription)
        fun onAnswerReceived(answer: SessionDescription)
        fun onRemoteIceCandidateReceived(candidate: IceCandidate)
        fun onError(message: String)
    }

    private var callback: Callback? = null
    private var currentRoomId: String? = null
    private var isCaller: Boolean = false
    private var partnerId: String? = null

    // Listener registrations to cleanly detach immediately
    private var waitingRoomListener: ListenerRegistration? = null
    private var roomListener: ListenerRegistration? = null
    private var candidatesListener: ListenerRegistration? = null

    fun setCallback(cb: Callback) {
        this.callback = cb
    }

    /**
     * Step 1: Matchmaking.
     * Looks for an existing waiting peer in "waiting_room".
     * If found, pairs with them immediately. Otherwise, posts self as waiting.
     */
    fun startMatchmaking() {
        // Query for another waiting user (excluding self)
        db.collection(COLLECTION_WAITING)
            .whereEqualTo("status", "waiting")
            .limit(5)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val availablePeerDoc = querySnapshot.documents.firstOrNull { doc ->
                    doc.id != userId && doc.getString("status") == "waiting"
                }

                if (availablePeerDoc != null) {
                    // Match found! I will act as Caller (Initiator)
                    pairWithPeer(availablePeerDoc)
                } else {
                    // No peer available right now, become Callee (Waiting)
                    registerSelfAsWaiting()
                }
            }
            .addOnFailureListener { error ->
                Log.e(TAG, "Error querying waiting room", error)
                callback?.onError("Matchmaking query failed: ${error.localizedMessage}")
            }
    }

    /**
     * Pair with an available peer found in the waiting room.
     */
    private fun pairWithPeer(peerDoc: DocumentSnapshot) {
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
            if (claimed) {
                // Initialize room document
                val roomData = hashMapOf(
                    "roomId" to generatedRoomId,
                    "callerId" to userId,
                    "calleeId" to peerId,
                    "createdAt" to FieldValue.serverTimestamp()
                )
                db.collection(COLLECTION_ROOMS).document(generatedRoomId)
                    .set(roomData)
                    .addOnSuccessListener {
                        listenToRoomUpdates(generatedRoomId)
                        listenToRemoteCandidates(generatedRoomId, isCaller = true)
                        callback?.onMatchFound(generatedRoomId, isCaller = true, partnerId = peerId)
                    }
            } else {
                // Peer was claimed by someone else in the same split second, register as waiting
                registerSelfAsWaiting()
            }
        }.addOnFailureListener {
            registerSelfAsWaiting()
        }
    }

    /**
     * Registers current user as waiting in Firestore.
     */
    private fun registerSelfAsWaiting() {
        isCaller = false
        val myWaitingDoc = db.collection(COLLECTION_WAITING).document(userId)
        val data = hashMapOf(
            "userId" to userId,
            "status" to "waiting",
            "createdAt" to FieldValue.serverTimestamp()
        )

        myWaitingDoc.set(data, SetOptions.merge()).addOnSuccessListener {
            // Listen to changes on own document to know when another user pairs with us
            waitingRoomListener = myWaitingDoc.addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener

                val status = snapshot.getString("status")
                val roomId = snapshot.getString("roomId")
                val matchedWith = snapshot.getString("matchedWith")

                if (status == "matched" && roomId != null && matchedWith != null) {
                    currentRoomId = roomId
                    partnerId = matchedWith
                    // We were matched! Stop waiting room listener
                    waitingRoomListener?.remove()
                    waitingRoomListener = null

                    listenToRoomUpdates(roomId)
                    listenToRemoteCandidates(roomId, isCaller = false)
                    callback?.onMatchFound(roomId, isCaller = false, partnerId = matchedWith)
                }
            }
        }.addOnFailureListener { error ->
            callback?.onError("Failed to enter waiting room: ${error.localizedMessage}")
        }
    }

    /**
     * Listen for SDP offer (if callee) or SDP answer (if caller).
     */
    private fun listenToRoomUpdates(roomId: String) {
        val roomDoc = db.collection(COLLECTION_ROOMS).document(roomId)
        roomListener = roomDoc.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener

            if (!isCaller) {
                // Callee listens for Offer
                val offerMap = snapshot.get("offer") as? Map<*, *>
                if (offerMap != null) {
                    val sdpType = offerMap["type"] as? String
                    val sdpDescription = offerMap["sdp"] as? String
                    if (sdpType != null && sdpDescription != null) {
                        val sessionDesc = SessionDescription(
                            SessionDescription.Type.fromCanonicalForm(sdpType.lowercase()),
                            sdpDescription
                        )
                        callback?.onOfferReceived(sessionDesc)
                    }
                }
            } else {
                // Caller listens for Answer
                val answerMap = snapshot.get("answer") as? Map<*, *>
                if (answerMap != null) {
                    val sdpType = answerMap["type"] as? String
                    val sdpDescription = answerMap["sdp"] as? String
                    if (sdpType != null && sdpDescription != null) {
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
        // If caller, listen to callee candidates. If callee, listen to caller candidates.
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

        // 2. Delete waiting room entries
        db.collection(COLLECTION_WAITING).document(userId).delete()
        partnerId?.let { db.collection(COLLECTION_WAITING).document(it).delete() }

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
        detachAllListeners()
        db.collection(COLLECTION_WAITING).document(userId).delete()
        currentRoomId?.let { roomId ->
            db.collection(COLLECTION_ROOMS).document(roomId).delete()
        }
        currentRoomId = null
        partnerId = null
    }

    private fun detachAllListeners() {
        waitingRoomListener?.remove()
        waitingRoomListener = null

        roomListener?.remove()
        roomListener = null

        candidatesListener?.remove()
        candidatesListener = null
    }
}
