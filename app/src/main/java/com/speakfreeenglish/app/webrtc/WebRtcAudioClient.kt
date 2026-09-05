package com.speakfreeenglish.app.webrtc

import android.content.Context
import org.webrtc.AudioSource
import org.webrtc.AudioTrack
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
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
 * Configured with Google's free public STUN server for direct device-to-device audio streaming.
 * Zero server bandwidth overhead.
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

        // Free Google Public STUN servers
        private val STUN_SERVERS = listOf(
            IceServer.builder("stun:stun.l.google.com:19302").createIceServer(),
            IceServer.builder("stun:stun1.l.google.com:19302").createIceServer(),
            IceServer.builder("stun:stun2.l.google.com:19302").createIceServer()
        )
    }

    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var peerConnection: PeerConnection? = null
    private var localAudioSource: AudioSource? = null
    private var localAudioTrack: AudioTrack? = null
    private var localMediaStream: MediaStream? = null

    private var isConnectedTriggered = false

    init {
        initPeerConnectionFactory()
    }

    private fun initPeerConnectionFactory() {
        val options = PeerConnectionFactory.InitializationOptions.builder(context)
            .setEnableInternalTracer(false)
            .createInitializationOptions()
        PeerConnectionFactory.initialize(options)

        val factoryOptions = PeerConnectionFactory.Options()
        peerConnectionFactory = PeerConnectionFactory.builder()
            .setOptions(factoryOptions)
            .createPeerConnectionFactory()
    }

    /**
     * Initializes local microphone audio track with hardware/software echo cancellation,
     * noise suppression, and automatic gain control.
     */
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

    /**
     * Creates a new PeerConnection for the voice session.
     */
    fun initPeerConnection() {
        val rtcConfig = PeerConnection.RTCConfiguration(STUN_SERVERS).apply {
            tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.DISABLED
            bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE
            rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE
            continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
            keyType = PeerConnection.KeyType.ECDSA
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
        }

        isConnectedTriggered = false

        peerConnection = peerConnectionFactory?.createPeerConnection(rtcConfig, object : PeerConnection.Observer {
            override fun onSignalingChange(state: PeerConnection.SignalingState?) {}

            override fun onIceConnectionChange(state: PeerConnection.IceConnectionState?) {
                if (state == PeerConnection.IceConnectionState.CONNECTED ||
                    state == PeerConnection.IceConnectionState.COMPLETED
                ) {
                    notifyConnectedOnce()
                } else if (state == PeerConnection.IceConnectionState.DISCONNECTED ||
                    state == PeerConnection.IceConnectionState.FAILED ||
                    state == PeerConnection.IceConnectionState.CLOSED
                ) {
                    listener.onPeerDisconnected()
                }
            }

            override fun onConnectionChange(newState: PeerConnectionState?) {
                if (newState == PeerConnectionState.CONNECTED) {
                    notifyConnectedOnce()
                } else if (newState == PeerConnectionState.FAILED ||
                    newState == PeerConnectionState.DISCONNECTED ||
                    newState == PeerConnectionState.CLOSED
                ) {
                    listener.onPeerDisconnected()
                }
            }

            override fun onIceConnectionReceivingChange(receiving: Boolean) {}

            override fun onIceGatheringChange(state: PeerConnection.IceGatheringState?) {}

            override fun onIceCandidate(candidate: IceCandidate?) {
                candidate?.let { listener.onIceCandidateGenerated(it) }
            }

            override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>?) {}

            override fun onAddStream(stream: MediaStream?) {}

            override fun onRemoveStream(stream: MediaStream?) {}

            override fun onDataChannel(dataChannel: org.webrtc.DataChannel?) {}

            override fun onRenegotiationNeeded() {}

            override fun onAddTrack(receiver: RtpReceiver?, mediaStreams: Array<out MediaStream>?) {}
        })

        // Add local audio track to connection
        localAudioTrack?.let { track ->
            peerConnection?.addTrack(track, listOf(MEDIA_STREAM_ID))
        }
    }

    private fun notifyConnectedOnce() {
        if (!isConnectedTriggered) {
            isConnectedTriggered = true
            listener.onPeerConnected()
        }
    }

    /**
     * Initiator: Creates an SDP Offer for audio.
     */
    fun createOffer() {
        val sdpConstraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "false"))
        }

        peerConnection?.createOffer(object : SimpleSdpObserver() {
            override fun onCreateSuccess(desc: SessionDescription?) {
                desc?.let {
                    peerConnection?.setLocalDescription(SimpleSdpObserver(), it)
                    listener.onLocalDescriptionCreated(it)
                }
            }

            override fun onCreateFailure(error: String?) {
                listener.onError("Failed to create WebRTC offer: $error")
            }
        }, sdpConstraints)
    }

    /**
     * Receiver: Sets remote offer and generates SDP Answer.
     */
    fun handleRemoteOfferAndCreateAnswer(remoteDesc: SessionDescription) {
        peerConnection?.setRemoteDescription(object : SimpleSdpObserver() {
            override fun onSetSuccess() {
                val sdpConstraints = MediaConstraints().apply {
                    mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
                    mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "false"))
                }

                peerConnection?.createAnswer(object : SimpleSdpObserver() {
                    override fun onCreateSuccess(desc: SessionDescription?) {
                        desc?.let {
                            peerConnection?.setLocalDescription(SimpleSdpObserver(), it)
                            listener.onLocalDescriptionCreated(it)
                        }
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

    /**
     * Caller: Sets received remote Answer.
     */
    fun setRemoteAnswer(remoteDesc: SessionDescription) {
        peerConnection?.setRemoteDescription(object : SimpleSdpObserver() {
            override fun onSetFailure(error: String?) {
                listener.onError("Failed to set remote answer: $error")
            }
        }, remoteDesc)
    }

    /**
     * Adds an ICE candidate received from remote signaling peer.
     */
    fun addRemoteIceCandidate(iceCandidate: IceCandidate) {
        peerConnection?.addIceCandidate(iceCandidate)
    }

    /**
     * Toggle microphone mute state.
     */
    fun setMicrophoneMute(isMuted: Boolean) {
        localAudioTrack?.setEnabled(!isMuted)
    }

    /**
     * Tears down all WebRTC resources cleanly.
     */
    fun close() {
        try {
            peerConnection?.close()
            peerConnection?.dispose()
            peerConnection = null

            localAudioTrack?.dispose()
            localAudioTrack = null

            localAudioSource?.dispose()
            localAudioSource = null
        } catch (e: Exception) {
            // Ignore during teardown
        }
    }

    private open class SimpleSdpObserver : SdpObserver {
        override fun onCreateSuccess(p0: SessionDescription?) {}
        override fun onSetSuccess() {}
        override fun onCreateFailure(p0: String?) {}
        override fun onSetFailure(p0: String?) {}
    }
}
