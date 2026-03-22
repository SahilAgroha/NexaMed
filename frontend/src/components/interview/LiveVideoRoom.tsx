import { useAuthStore } from '../../store/authStore'
import { useWebRTC } from '../../hooks/useWebRTC'
import { useState } from 'react'
import { Phone, PhoneOff, Mic, MicOff, Video, VideoOff, Wifi, WifiOff } from 'lucide-react'

interface Props {
  roomId: string
  onEnd?: () => void
  /** Optionally show a sidebar alongside video */
  sidebar?: React.ReactNode
}

/**
 * Self-contained WebRTC video room.
 * Handles camera/mic access, STOMP signaling, and peer connection.
 * Can be embedded in any page — just pass a roomId.
 *
 * Usage:
 *   <LiveVideoRoom roomId={session.roomId} onEnd={handleEnd} sidebar={<QuestionCard />} />
 */
export default function LiveVideoRoom({ roomId, onEnd, sidebar }: Props) {
  const { user } = useAuthStore()
  const {
    localVideoRef, remoteVideoRef,
    startCall, endCall,
    connected, error,
  } = useWebRTC(roomId, user?.userId ?? '')

  const [started, setStarted] = useState(false)
  const [micOn,   setMicOn]   = useState(true)
  const [videoOn, setVideoOn] = useState(true)

  const handleStart = async () => {
    await startCall()
    setStarted(true)
  }

  const handleEnd = () => {
    endCall()
    onEnd?.()
  }

  const toggleMic = () => {
    const stream = localVideoRef.current?.srcObject as MediaStream | null
    stream?.getAudioTracks().forEach(t => { t.enabled = !micOn })
    setMicOn(v => !v)
  }

  const toggleVideo = () => {
    const stream = localVideoRef.current?.srcObject as MediaStream | null
    stream?.getVideoTracks().forEach(t => { t.enabled = !videoOn })
    setVideoOn(v => !v)
  }

  return (
    <div className="space-y-4">
      {/* Error banner */}
      {error && (
        <div className="px-4 py-2.5 bg-red-50 border border-red-200 rounded-lg text-sm text-red-700">
          {error} — ensure camera/mic permissions are granted.
        </div>
      )}

      {/* Status bar */}
      <div className="flex items-center gap-2 text-sm">
        {connected
          ? <><span className="w-2 h-2 bg-green-500 rounded-full animate-pulse" /><span className="text-green-600">Peer connected</span></>
          : <><span className="w-2 h-2 bg-gray-300 rounded-full" /><span className="text-gray-400">Waiting for other participant...</span></>
        }
        <span className="ml-auto text-xs text-gray-400 flex items-center gap-1">
          {connected ? <Wifi size={13} /> : <WifiOff size={13} />} Room: <code className="bg-gray-100 px-1 rounded">{roomId}</code>
        </span>
      </div>

      {/* Main layout — video + optional sidebar */}
      <div className={`flex gap-4 ${sidebar ? 'flex-col lg:flex-row' : ''}`}>

        {/* Video grid */}
        <div className="flex-1 space-y-3">
          {/* Remote (interviewer) */}
          <div className="relative bg-gray-900 rounded-xl overflow-hidden aspect-video">
            <video ref={remoteVideoRef} autoPlay playsInline className="w-full h-full object-cover" />
            {!connected && (
              <div className="absolute inset-0 flex flex-col items-center justify-center gap-2 text-gray-500">
                <Video size={32} />
                <p className="text-sm">Waiting for interviewer to join...</p>
              </div>
            )}
            <span className="absolute bottom-2 left-3 text-xs text-white bg-black/60 px-2 py-0.5 rounded-full">
              Interviewer
            </span>
          </div>

          {/* Local (self) — smaller picture-in-picture style */}
          <div className="relative bg-gray-800 rounded-xl overflow-hidden" style={{ aspectRatio: '16/9', maxHeight: '140px' }}>
            <video ref={localVideoRef} autoPlay playsInline muted className="w-full h-full object-cover" />
            {!started && (
              <div className="absolute inset-0 flex items-center justify-center text-gray-500 text-sm">
                Camera preview
              </div>
            )}
            <span className="absolute bottom-2 left-3 text-xs text-white bg-black/60 px-2 py-0.5 rounded-full">
              You
            </span>
          </div>

          {/* Controls */}
          <div className="flex items-center justify-center gap-3 py-1">
            {!started ? (
              <button onClick={handleStart} className="btn-primary flex items-center gap-2 px-6">
                <Phone size={16} /> Join call
              </button>
            ) : (
              <>
                <button
                  onClick={toggleMic}
                  title={micOn ? 'Mute mic' : 'Unmute mic'}
                  className={`p-3 rounded-full border transition-colors ${
                    micOn ? 'border-gray-300 hover:bg-gray-50' : 'bg-red-100 border-red-300 text-red-600'
                  }`}>
                  {micOn ? <Mic size={18} /> : <MicOff size={18} />}
                </button>

                <button
                  onClick={handleEnd}
                  title="Leave call"
                  className="p-3 rounded-full bg-red-600 hover:bg-red-700 text-white border-0 transition-colors">
                  <PhoneOff size={18} />
                </button>

                <button
                  onClick={toggleVideo}
                  title={videoOn ? 'Turn off camera' : 'Turn on camera'}
                  className={`p-3 rounded-full border transition-colors ${
                    videoOn ? 'border-gray-300 hover:bg-gray-50' : 'bg-red-100 border-red-300 text-red-600'
                  }`}>
                  {videoOn ? <Video size={18} /> : <VideoOff size={18} />}
                </button>
              </>
            )}
          </div>
        </div>

        {/* Optional sidebar (e.g. question + notes) */}
        {sidebar && (
          <div className="lg:w-80 flex-shrink-0">
            {sidebar}
          </div>
        )}
      </div>
    </div>
  )
}