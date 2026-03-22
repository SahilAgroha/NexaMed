import { useRef, useState, useCallback } from 'react'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

interface WebRTCSignal {
  type: string
  roomId: string
  senderId: string
  payload: unknown
}

/**
 * Manages a WebRTC peer connection for live interviews.
 * Uses interview-service WebSocket for signaling (offer/answer/ICE).
 * After handshake, video flows P2P — no server relay.
 */
export function useWebRTC(roomId: string, userId: string) {
  const localVideoRef  = useRef<HTMLVideoElement>(null)
  const remoteVideoRef = useRef<HTMLVideoElement>(null)
  const pcRef          = useRef<RTCPeerConnection | null>(null)
  const stompRef       = useRef<Client | null>(null)
  const [connected, setConnected] = useState(false)
  const [error, setError]         = useState<string | null>(null)

  const iceConfig = {
    iceServers: [{ urls: 'stun:stun.l.google.com:19302' }],
  }

  const sendSignal = useCallback((type: string, payload: unknown) => {
    stompRef.current?.publish({
      destination: `/app/interview/${roomId}/signal`,
      body: JSON.stringify({ type, roomId, senderId: userId, payload }),
    })
  }, [roomId, userId])

  const startCall = useCallback(async () => {
    setError(null)
    try {
      const stream = await navigator.mediaDevices.getUserMedia({ video: true, audio: true })
      if (localVideoRef.current) localVideoRef.current.srcObject = stream

      const pc = new RTCPeerConnection(iceConfig)
      pcRef.current = pc
      stream.getTracks().forEach(t => pc.addTrack(t, stream))

      pc.ontrack = (e) => {
        if (remoteVideoRef.current) remoteVideoRef.current.srcObject = e.streams[0]
      }

      pc.onicecandidate = (e) => {
        if (e.candidate) sendSignal('ICE_CANDIDATE', e.candidate)
      }

      const client = new Client({
        webSocketFactory: () =>
          new SockJS(
            import.meta.env.VITE_INTERVIEW_WS_URL
              ? `${import.meta.env.VITE_INTERVIEW_WS_URL}/ws`
              : 'http://localhost:8085/ws'
          ),
        onConnect: async () => {
          client.subscribe(`/topic/interview/${roomId}/signal`, async (msg) => {
            const signal: WebRTCSignal = JSON.parse(msg.body)
            if (signal.senderId === userId) return // ignore own signals

            if (signal.type === 'OFFER') {
              await pc.setRemoteDescription(new RTCSessionDescription(signal.payload as RTCSessionDescriptionInit))
              const answer = await pc.createAnswer()
              await pc.setLocalDescription(answer)
              sendSignal('ANSWER', answer)
            } else if (signal.type === 'ANSWER') {
              await pc.setRemoteDescription(new RTCSessionDescription(signal.payload as RTCSessionDescriptionInit))
            } else if (signal.type === 'ICE_CANDIDATE') {
              await pc.addIceCandidate(new RTCIceCandidate(signal.payload as RTCIceCandidateInit))
            }
          })

          // Send JOIN then create and send OFFER
          sendSignal('JOIN', {})
          const offer = await pc.createOffer()
          await pc.setLocalDescription(offer)
          sendSignal('OFFER', offer)
          setConnected(true)
        },
      })

      client.activate()
      stompRef.current = client

    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Failed to start call')
    }
  }, [roomId, userId, sendSignal])

  const endCall = useCallback(() => {
    pcRef.current?.close()
    stompRef.current?.deactivate()
    if (localVideoRef.current?.srcObject) {
      (localVideoRef.current.srcObject as MediaStream).getTracks().forEach(t => t.stop())
    }
    setConnected(false)
  }, [])

  return { localVideoRef, remoteVideoRef, startCall, endCall, connected, error }
}