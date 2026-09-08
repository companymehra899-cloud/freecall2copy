package com.speakfreeenglish.app.webrtc

import android.content.Context
import com.speakfreeenglish.app.BuildConfig
import org.webrtc.AudioSource
import org.webrtc.AudioTrack
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnection.IceServer
import org.webrtc.PeerConnection.PeerConnectionState
import org.webrtc.PeerConnectionFactory
import org.webrtc.RtpReceiver
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription

/**
 * Pure P2P WebRTC Audio Client.
 * Factory initialize() is process-once. ICE candidates are queued until remote SDP is set.
 */
class WebRtcAudioClient(
    private val context: Context,
    private val listener: Listener
) {

    interface Listener {
        fun onLocalDescriptionCreated(desc: SessionDescription)
        fun onIceCandidateGenerated(candidate: IceCandidate)
        fun onPeerConnected()
        fun onPeerDisconnected()
        fun onError(description: String)
    }

    companion object {
        private const val AUDIO_TRACK_ID = "ARDAMSa0"
        private const val MEDIA_STREAM_ID = "ARDAMS"

        private fun iceServers(): List<IceServer> {
            val servers = mutableListOf(
                IceServer.builder("stun:stun.l.google.com:19302").createIceServer(),
                IceServer.builder("stun:stun1.l.google.com:19302").createIceServer(),
                IceServer.builder("stun:stun2.l.google.com:19302").createIceServer()
            )
            val customUrl = BuildConfig.TURN_URL.trim()
            val customUser = BuildConfig.TURN_USERNAME.trim()
            val customCred = BuildConfig.TURN_CREDENTIAL.trim()
            if (customUrl.isNotEmpty() && customUser.isNotEmpty() && customCred.isNotEmpty()) {
                servers.add(
                    IceServer.builder(customUrl)
                        .setUsername(customUser)
                        .setPassword(customCred)
                        .createIceServer()
                )
            }
            return servers
        }

        @Volatile
        private var factoryInitialized = false
        private val initLock = Any()

        private fun ensureFactoryInitialized(appContext: Context) {
            if (factoryInitialized) return
            synchronized(initLock) {
                if (factoryInitialized) return
                val options = PeerConnectionFactory.InitializationOptions.builder(appContext)
                    .setEnableInternalTracer(false)
                    .createInitializationOptions()
                PeerConnectionFactory.initialize(options)
                factoryInitialized = true
            }
        }
    }

    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var peerConnection: PeerConnection? = null
    private var localAudioSource: AudioSource? = null
    private var localAudioTrack: AudioTrack? = null

    private var isConnectedTriggered = false
    private var isClosing = false
    private var remoteDescriptionSet = false
    private val pendingRemoteCandidates = mutableListOf<IceCandidate>()

    init {
        initPeerConnectionFactory()
    }

    private fun initPeerConnectionFactory() {
        ensureFactoryInitialized(context.applicationContext)
        val factoryOptions = PeerConnectionFactory.Options()
        peerConnectionFactory = PeerConnectionFactory.builder()
            .setOptions(factoryOptions)
            .createPeerConnectionFactory()
    }

    fun startLocalAudio() {
        val factory = peerConnectionFactory ?: return

        val audioConstraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("googEchoCancellation", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("googAutoGainControl", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("googHighpassFilter", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("googNoiseSuppression", "true"))
        }

        localAudioSource = factory.createAudioSource(audioConstraints)
        localAudioTrack = factory.createAudioTrack(AUDIO_TRACK_ID, localAudioSource).apply {
            setEnabled(true)
        }
    }

    fun initPeerConnection() {
        val rtcConfig = PeerConnection.RTCConfiguration(iceServers()).apply {
            bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE
            rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE
            continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
            keyType = PeerConnection.KeyType.ECDSA
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
            iceCandidatePoolSize = 4
        }

        isConnectedTriggered = false
        isClosing = false
        remoteDescriptionSet = false
        pendingRemoteCandidates.clear()

        peerConnection = peerConnectionFactory?.createPeerConnection(rtcConfig, object : PeerConnection.Observer {
            override fun onSignalingChange(state: PeerConnection.SignalingState?) {}

            override fun onIceConnectionChange(state: PeerConnection.IceConnectionState?) {
                if (isClosing) return
                if (state == PeerConnection.IceConnectionState.CONNECTED ||
                    state == PeerConnection.IceConnectionState.COMPLETED
                ) {
                    notifyConnectedOnce()
                } else if (state == PeerConnection.IceConnectionState.FAILED) {
                    listener.onPeerDisconnected()
                }
            }

            override fun onConnectionChange(newState: PeerConnectionState?) {
                if (isClosing) return
                if (newState == PeerConnectionState.CONNECTED) {
                    notifyConnectedOnce()
                } else if (newState == PeerConnectionState.FAILED) {
                    listener.onPeerDisconnected()
                }
            }

            override fun onIceConnectionReceivingChange(receiving: Boolean) {}

            override fun onIceGatheringChange(state: PeerConnection.IceGatheringState?) {}

            override fun onIceCandidate(candidate: IceCandidate?) {
                if (isClosing) return
                candidate?.let { listener.onIceCandidateGenerated(it) }
            }

            override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>?) {}

            override fun onAddStream(stream: MediaStream?) {}

            override fun onRemoveStream(stream: MediaStream?) {}

            override fun onDataChannel(dataChannel: org.webrtc.DataChannel?) {}

            override fun onRenegotiationNeeded() {}

            override fun onAddTrack(receiver: RtpReceiver?, mediaStreams: Array<out MediaStream>?) {}
        })

        localAudioTrack?.let { track ->
            peerConnection?.addTrack(track, listOf(MEDIA_STREAM_ID))
        }
    }

    private fun notifyConnectedOnce() {
        if (!isConnectedTriggered && !isClosing) {
            isConnectedTriggered = true
            listener.onPeerConnected()
        }
    }

    fun createOffer() {
        val sdpConstraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "false"))
        }

        peerConnection?.createOffer(object : SimpleSdpObserver() {
            override fun onCreateSuccess(desc: SessionDescription?) {
                val localDesc = desc ?: return
                peerConnection?.setLocalDescription(object : SimpleSdpObserver() {
                    override fun onSetSuccess() {
                        listener.onLocalDescriptionCreated(localDesc)
                    }

                    override fun onSetFailure(error: String?) {
                        listener.onError("Failed to set local offer: $error")
                    }
                }, localDesc)
            }

            override fun onCreateFailure(error: String?) {
                listener.onError("Failed to create WebRTC offer: $error")
            }
        }, sdpConstraints)
    }

    fun handleRemoteOfferAndCreateAnswer(remoteDesc: SessionDescription) {
        peerConnection?.setRemoteDescription(object : SimpleSdpObserver() {
            override fun onSetSuccess() {
                flushPendingCandidates()
                val sdpConstraints = MediaConstraints().apply {
                    mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
                    mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "false"))
                }

                peerConnection?.createAnswer(object : SimpleSdpObserver() {
                    override fun onCreateSuccess(desc: SessionDescription?) {
                        val localDesc = desc ?: return
                        peerConnection?.setLocalDescription(object : SimpleSdpObserver() {
                            override fun onSetSuccess() {
                                listener.onLocalDescriptionCreated(localDesc)
                            }

                            override fun onSetFailure(error: String?) {
                                listener.onError("Failed to set local answer: $error")
                            }
                        }, localDesc)
                    }

                    override fun onCreateFailure(error: String?) {
                        listener.onError("Failed to create WebRTC answer: $error")
                    }
                }, sdpConstraints)
            }

            override fun onSetFailure(error: String?) {
                listener.onError("Failed to set remote offer: $error")
            }
        }, remoteDesc)
    }

    fun setRemoteAnswer(remoteDesc: SessionDescription) {
        peerConnection?.setRemoteDescription(object : SimpleSdpObserver() {
            override fun onSetSuccess() {
                flushPendingCandidates()
            }

            override fun onSetFailure(error: String?) {
                listener.onError("Failed to set remote answer: $error")
            }
        }, remoteDesc)
    }

    fun addRemoteIceCandidate(iceCandidate: IceCandidate) {
        if (remoteDescriptionSet) {
            peerConnection?.addIceCandidate(iceCandidate)
        } else {
            pendingRemoteCandidates.add(iceCandidate)
        }
    }

    private fun flushPendingCandidates() {
        remoteDescriptionSet = true
        pendingRemoteCandidates.forEach { candidate ->
            peerConnection?.addIceCandidate(candidate)
        }
        pendingRemoteCandidates.clear()
    }

    fun setMicrophoneMute(isMuted: Boolean) {
        localAudioTrack?.setEnabled(!isMuted)
    }

    fun close() {
        if (isClosing) return
        isClosing = true
        try {
            localAudioTrack?.setEnabled(false)
            val pc = peerConnection
            peerConnection = null
            if (pc != null) {
                try {
                    pc.close()
                } catch (_: Exception) {
                }
                try {
                    pc.dispose()
                } catch (_: Exception) {
                }
            }

            try {
                localAudioTrack?.dispose()
            } catch (_: Exception) {
            }
            localAudioTrack = null

            try {
                localAudioSource?.dispose()
            } catch (_: Exception) {
            }
            localAudioSource = null
            pendingRemoteCandidates.clear()
            remoteDescriptionSet = false
        } catch (_: Exception) {
        }
    }

    private open class SimpleSdpObserver : SdpObserver {
        override fun onCreateSuccess(p0: SessionDescription?) {}
        override fun onSetSuccess() {}
        override fun onCreateFailure(p0: String?) {}
        override fun onSetFailure(p0: String?) {}
    }
}
