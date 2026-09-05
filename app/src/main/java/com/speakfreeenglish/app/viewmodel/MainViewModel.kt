package com.speakfreeenglish.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.speakfreeenglish.app.audio.AppAudioManager
import com.speakfreeenglish.app.billing.PlayBillingManager
import com.speakfreeenglish.app.data.AccountRepository
import com.speakfreeenglish.app.data.FirestoreChatClient
import com.speakfreeenglish.app.model.AppTab
import com.speakfreeenglish.app.model.CallState
import com.speakfreeenglish.app.model.ChatMessage
import com.speakfreeenglish.app.model.Friend
import com.speakfreeenglish.app.model.FriendRequest
import com.speakfreeenglish.app.model.UserAccount
import com.speakfreeenglish.app.signaling.FirestoreSignalingClient
import com.speakfreeenglish.app.webrtc.WebRtcAudioClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription
import java.util.Locale

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val audioManager = AppAudioManager(application.applicationContext)
    private val repository = AccountRepository(application.applicationContext)
    private val chatClient = FirestoreChatClient()
    private val signalingClient = FirestoreSignalingClient()
    private var webRtcClient: WebRtcAudioClient? = null

    // Official Google Play Billing Library v7 manager
    private val playBillingManager = PlayBillingManager(
        context = application.applicationContext,
        coroutineScope = viewModelScope,
        onPurchaseVerified = { purchase ->
            handleGooglePlayPurchaseVerified(purchase)
        }
    )

    val isBillingReady: StateFlow<Boolean> = playBillingManager.isReady
    val billingProductDetails = playBillingManager.productDetails
    val isBillingProcessing: StateFlow<Boolean> = playBillingManager.isProcessing
    val billingMessage: StateFlow<String?> = playBillingManager.billingMessage

    // User & Tab Navigation State
    val currentUser: StateFlow<UserAccount> = repository.currentUser
    val friends: StateFlow<List<Friend>> = repository.friends

    private val _incomingRequests = MutableStateFlow<List<FriendRequest>>(emptyList())
    val incomingRequests: StateFlow<List<FriendRequest>> = _incomingRequests.asStateFlow()

    private val _currentTab = MutableStateFlow(AppTab.HOME)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    private val _showAuthDialog = MutableStateFlow(false)
    val showAuthDialog: StateFlow<Boolean> = _showAuthDialog.asStateFlow()

    private val _showSubscriptionDialog = MutableStateFlow(false)
    val showSubscriptionDialog: StateFlow<Boolean> = _showSubscriptionDialog.asStateFlow()

    private val _activeChatFriend = MutableStateFlow<Friend?>(null)
    val activeChatFriend: StateFlow<Friend?> = _activeChatFriend.asStateFlow()

    private val _activeChatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val activeChatMessages: StateFlow<List<ChatMessage>> = _activeChatMessages.asStateFlow()

    // Calling State
    private val _callState = MutableStateFlow(CallState.IDLE)
    val callState: StateFlow<CallState> = _callState.asStateFlow()

    private val _callDurationFormatted = MutableStateFlow("00:00")
    val callDurationFormatted: StateFlow<String> = _callDurationFormatted.asStateFlow()

    private val _callDurationSeconds = MutableStateFlow(0L)
    val callDurationSeconds: StateFlow<Long> = _callDurationSeconds.asStateFlow()

    // 10-Minute Free Call Limit (600 seconds)
    val maxFreeCallSeconds: Long = 600L // 10 minutes

    private val _isFreeLimitReached = MutableStateFlow(false)
    val isFreeLimitReached: StateFlow<Boolean> = _isFreeLimitReached.asStateFlow()

    private val _searchingSeconds = MutableStateFlow(0)
    val searchingSeconds: StateFlow<Int> = _searchingSeconds.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _isSpeakerOn = MutableStateFlow(true)
    val isSpeakerOn: StateFlow<Boolean> = _isSpeakerOn.asStateFlow()

    private val _partnerLabel = MutableStateFlow("Anonymous Partner")
    val partnerLabel: StateFlow<String> = _partnerLabel.asStateFlow()

    private val _statusMessage = MutableStateFlow("")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

    private var activeRoomId: String? = null
    private var isCallerRole: Boolean = false

    private var timerJob: Job? = null
    private var searchingTimerJob: Job? = null

    init {
        setupSignaling()
        attachRealtimeFriends()
    }

    fun selectTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun openAuthDialog() {
        _showAuthDialog.value = true
    }

    fun closeAuthDialog() {
        _showAuthDialog.value = false
    }

    fun openSubscriptionDialog() {
        _showSubscriptionDialog.value = true
    }

    fun closeSubscriptionDialog() {
        _showSubscriptionDialog.value = false
    }

    fun loginWithEmail(name: String, email: String, uid: String) {
        val cleanEmail = email.trim()
        val user = repository.registerOrLogin(name, cleanEmail, uid.ifBlank { cleanEmail })
        chatClient.syncProfileOnLogin(user) {
            attachRealtimeFriends()
        }
        _showAuthDialog.value = false
    }

    fun logout() {
        closeChat()
        chatClient.detachAll()
        repository.clearFriends()
        _incomingRequests.value = emptyList()
        repository.logoutToGuest()
    }

    private fun attachRealtimeFriends() {
        val user = currentUser.value
        if (user.isGuest || user.userId.isBlank()) {
            repository.clearFriends()
            _incomingRequests.value = emptyList()
            return
        }
        chatClient.upsertUserProfile(user)
        chatClient.purgeExpiredChats()
        chatClient.listenFriends(user.userId) { list ->
            repository.replaceFriends(list)
        }
        chatClient.listenIncomingRequests(user.userId) { list ->
            _incomingRequests.value = list
        }
    }

    fun updateProfileImage(uriString: String?) {
        repository.updateProfileImage(uriString)
    }

    fun confirmAge18() {
        repository.confirmAge18()
    }

    fun reportAndBlockPartner(reason: String = "Inappropriate behavior") {
        val partner = _partnerLabel.value
        repository.reportPartner(partner, reason)
        repository.blockPartner(partner)
        _statusMessage.value = "Partner reported and blocked"
        endCall()
    }

    fun blockPartner() {
        repository.blockPartner(_partnerLabel.value)
        _statusMessage.value = "Partner blocked"
        endCall()
    }

    /**
     * Triggers the official Google Play Billing Library v7 flow.
     * Replaces all manual, unverified payment triggers.
     */
    fun launchGooglePlayPurchase(activity: android.app.Activity) {
        playBillingManager.launchPurchaseFlow(activity)
    }

    private fun handleGooglePlayPurchaseVerified(purchase: com.android.billingclient.api.Purchase) {
        val productId = purchase.products.firstOrNull() ?: PlayBillingManager.VIP_5MONTHS_PRODUCT_ID
        val result = repository.applyVerifiedGooglePlayPurchase(
            purchaseToken = purchase.purchaseToken,
            orderId = purchase.orderId ?: "GPA.${System.currentTimeMillis()}",
            productId = productId,
            purchaseTimeMillis = purchase.purchaseTime
        )
        if (result.isSuccess) {
            _showSubscriptionDialog.value = false
            _statusMessage.value = "Google Play Purchase Verified! VIP Plan Activated."
        } else {
            _statusMessage.value = "Purchase verification failed: ${result.exceptionOrNull()?.message}"
        }
    }

    fun clearBillingMessage() {
        playBillingManager.clearBillingMessage()
    }

    fun addFriend(query: String) {
        val user = currentUser.value
        if (user.isGuest) {
            _showAuthDialog.value = true
            _statusMessage.value = "Login required to add friends"
            return
        }
        chatClient.sendFriendRequest(user, query) { result ->
            _statusMessage.value = result.getOrElse { it.message ?: "Failed to send request" }
        }
    }

    fun acceptFriendRequest(request: FriendRequest) {
        val user = currentUser.value
        chatClient.acceptFriendRequest(user, request) { result ->
            if (result.isFailure) {
                _statusMessage.value = result.exceptionOrNull()?.message ?: "Failed to accept request"
            } else {
                _statusMessage.value = "Friend request accepted"
            }
        }
    }

    fun declineFriendRequest(request: FriendRequest) {
        chatClient.declineFriendRequest(request.id)
    }

    fun openChatWithFriend(friend: Friend) {
        val user = currentUser.value
        if (user.isGuest || user.userId.isBlank()) {
            _showAuthDialog.value = true
            return
        }
        _activeChatFriend.value = friend
        _activeChatMessages.value = emptyList()
        chatClient.openChat(user.userId, friend.id) { msgs ->
            _activeChatMessages.value = msgs
        }
    }

    fun closeChat() {
        _activeChatFriend.value = null
        _activeChatMessages.value = emptyList()
        chatClient.closeChat()
    }

    fun sendChatMessage(text: String) {
        val friend = _activeChatFriend.value ?: return
        val user = currentUser.value
        if (user.isGuest || user.userId.isBlank()) return
        chatClient.sendMessage(user, friend.id, text)
    }

    fun startDirectCallWithFriend(friend: Friend) {
        _partnerLabel.value = friend.name
        findPartner()
    }

    private fun setupSignaling() {
        signalingClient.setCallback(object : FirestoreSignalingClient.Callback {
            override fun onMatchFound(roomId: String, isCaller: Boolean, partnerId: String) {
                activeRoomId = roomId
                isCallerRole = isCaller
                if (_partnerLabel.value == "Anonymous Partner") {
                    _partnerLabel.value = "Learner #${partnerId.take(4).uppercase()}"
                }
                _callState.value = CallState.CONNECTING

                stopSearchingTimer()

                viewModelScope.launch {
                    initWebRtc()
                    webRtcClient?.initPeerConnection()

                    if (isCaller) {
                        webRtcClient?.createOffer()
                    }
                }
            }

            override fun onOfferReceived(offer: SessionDescription) {
                viewModelScope.launch {
                    webRtcClient?.handleRemoteOfferAndCreateAnswer(offer)
                }
            }

            override fun onAnswerReceived(answer: SessionDescription) {
                viewModelScope.launch {
                    webRtcClient?.setRemoteAnswer(answer)
                }
            }

            override fun onRemoteIceCandidateReceived(candidate: IceCandidate) {
                viewModelScope.launch {
                    webRtcClient?.addRemoteIceCandidate(candidate)
                }
            }

            override fun onError(message: String) {
                _statusMessage.value = message
                _callState.value = CallState.ERROR
                resetAfterDelay()
            }
        })
    }

    private fun initWebRtc() {
        webRtcClient = WebRtcAudioClient(getApplication(), object : WebRtcAudioClient.Listener {
            override fun onLocalDescriptionCreated(desc: SessionDescription) {
                val roomId = activeRoomId ?: return
                if (desc.type == SessionDescription.Type.OFFER) {
                    signalingClient.sendOffer(roomId, desc)
                } else if (desc.type == SessionDescription.Type.ANSWER) {
                    signalingClient.sendAnswer(roomId, desc)
                }
            }

            override fun onIceCandidateGenerated(candidate: IceCandidate) {
                val roomId = activeRoomId ?: return
                signalingClient.sendIceCandidate(roomId, isCallerRole, candidate)
            }

            override fun onPeerConnected() {
                viewModelScope.launch {
                    _callState.value = CallState.IN_CALL
                    startCallDurationTimer()

                    // CRITICAL ZERO-COST DELETION:
                    // Purge Firestore documents the moment audio flows directly device-to-device
                    signalingClient.cleanupFirestoreOnConnected()
                }
            }

            override fun onPeerDisconnected() {
                viewModelScope.launch {
                    endCall()
                }
            }

            override fun onError(description: String) {
                _statusMessage.value = description
                _callState.value = CallState.ERROR
                resetAfterDelay()
            }
        })

        webRtcClient?.startLocalAudio()
    }

    /**
     * User clicks "Find Speaking Partner"
     */
    fun findPartner() {
        if (_callState.value != CallState.IDLE) return

        _isFreeLimitReached.value = false
        _callState.value = CallState.SEARCHING
        _isMuted.value = false
        _isSpeakerOn.value = true
        _statusMessage.value = "Matching you with an English learner..."

        audioManager.startAudioForCall()
        startSearchingTimer()
        signalingClient.startMatchmaking()
    }

    /**
     * Dismiss limit reached modal
     */
    fun dismissLimitReachedDialog() {
        _isFreeLimitReached.value = false
    }

    /**
     * Cancel search before match is made
     */
    fun cancelSearch() {
        stopSearchingTimer()
        signalingClient.cancelOrDisconnect()
        audioManager.stopAudio()
        webRtcClient?.close()
        webRtcClient = null
        _callState.value = CallState.IDLE
        _statusMessage.value = ""
        _partnerLabel.value = "Anonymous Partner"
    }

    /**
     * User clicks "End Call"
     */
    fun endCall(isLimitReached: Boolean = false) {
        stopCallDurationTimer()
        stopSearchingTimer()

        val talkSeconds = _callDurationSeconds.value
        if (talkSeconds > 0) {
            repository.incrementCallStats(talkSeconds)
        }

        webRtcClient?.close()
        webRtcClient = null

        signalingClient.cancelOrDisconnect()
        audioManager.stopAudio()

        if (isLimitReached) {
            _isFreeLimitReached.value = true
            _statusMessage.value = "Free 20-Min Call Limit Reached"
        } else {
            _statusMessage.value = "Call Ended"
        }

        _callState.value = CallState.ENDED
        resetAfterDelay()
    }

    fun toggleMute() {
        val nextMute = !_isMuted.value
        _isMuted.value = nextMute
        webRtcClient?.setMicrophoneMute(nextMute)
    }

    fun toggleSpeaker() {
        val nextSpeaker = !_isSpeakerOn.value
        _isSpeakerOn.value = nextSpeaker
        audioManager.setSpeakerphone(nextSpeaker)
    }

    private fun startSearchingTimer() {
        _searchingSeconds.value = 0
        searchingTimerJob?.cancel()
        searchingTimerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                _searchingSeconds.value += 1
            }
        }
    }

    private fun stopSearchingTimer() {
        searchingTimerJob?.cancel()
        searchingTimerJob = null
    }

    private fun startCallDurationTimer() {
        timerJob?.cancel()
        _callDurationFormatted.value = "00:00"
        _callDurationSeconds.value = 0L
        _isFreeLimitReached.value = false
        var seconds = 0L
        val isSubscribed = currentUser.value.isSubscribed

        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                seconds++
                _callDurationSeconds.value = seconds
                val mins = seconds / 60
                val secs = seconds % 60
                _callDurationFormatted.value = String.format(Locale.US, "%02d:%02d", mins, secs)

                // Free user 10-minute (600 seconds) auto-disconnect rule
                if (!isSubscribed && seconds >= maxFreeCallSeconds) {
                    endCall(isLimitReached = true)
                    break
                }
            }
        }
    }

    private fun stopCallDurationTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun resetAfterDelay() {
        viewModelScope.launch {
            delay(2000)
            _callState.value = CallState.IDLE
            _callDurationFormatted.value = "00:00"
            _callDurationSeconds.value = 0L
            _partnerLabel.value = "Anonymous Partner"
            _statusMessage.value = ""
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopCallDurationTimer()
        stopSearchingTimer()
        webRtcClient?.close()
        signalingClient.cancelOrDisconnect()
        closeChat()
        chatClient.detachAll()
        audioManager.stopAudio()
        playBillingManager.destroy()
    }
}
