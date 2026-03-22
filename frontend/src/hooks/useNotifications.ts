import { useEffect, useRef, useCallback } from 'react'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import toast from 'react-hot-toast'
import { useAuthStore } from '../store/authStore'

interface NotificationMessage {
  type: string
  title: string
  message: string
  userId: string
  timestamp: string
  payload?: unknown
}

/**
 * Connects to notification-service WebSocket.
 * Subscribes to /topic/notifications/{userId}
 * Shows toast on every incoming message.
 */
export function useNotifications() {
  const { user, isAuthenticated } = useAuthStore()
  const clientRef = useRef<Client | null>(null)

  const connect = useCallback(() => {
    if (!user?.userId || clientRef.current?.active) return

    const client = new Client({
      webSocketFactory: () =>
        new SockJS(
          import.meta.env.VITE_WS_URL
            ? `${import.meta.env.VITE_WS_URL}/ws`
            : 'http://localhost:8086/ws'
        ),
      reconnectDelay: 5000,
      onConnect: () => {
        client.subscribe(
          `/topic/notifications/${user.userId}`,
          (msg) => {
            try {
              const notification: NotificationMessage = JSON.parse(msg.body)
              const icons: Record<string, string> = {
                ENROLLMENT: '📚',
                INTERVIEW_COMPLETED: '🎤',
                QUIZ_SUBMITTED: '🧠',
                TEST: '🔔',
                SYSTEM: 'ℹ️',
              }
              toast(
                `${icons[notification.type] ?? '🔔'} ${notification.title}\n${notification.message}`,
                { duration: 5000 }
              )
            } catch { /* ignore parse errors */ }
          }
        )
      },
      onDisconnect: () => { /* silent */ },
    })

    client.activate()
    clientRef.current = client
  }, [user?.userId])

  useEffect(() => {
    if (isAuthenticated) connect()
    return () => { clientRef.current?.deactivate() }
  }, [isAuthenticated, connect])
}