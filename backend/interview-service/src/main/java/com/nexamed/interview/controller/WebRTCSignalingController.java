package com.nexamed.interview.controller;

import com.nexamed.interview.dto.WebRTCSignal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

/**
 * WebRTC Signaling Server — relays SDP and ICE messages between peers.
 *
 * Flow:
 *   Student A connects → sends OFFER to /app/interview/{roomId}/signal
 *   Server relays to  → /topic/interview/{roomId}/signal
 *   Student B receives OFFER → sends ANSWER back
 *   Both exchange ICE candidates → peer-to-peer connection established
 *   After handshake: video/audio flows directly between browsers (no server!)
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class WebRTCSignalingController {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * All WebRTC signals (OFFER, ANSWER, ICE_CANDIDATE, JOIN, LEAVE)
     * go through this single endpoint and are relayed to the room topic.
     */
    @MessageMapping("/interview/{roomId}/signal")
    public void handleSignal(
            @DestinationVariable String roomId,
            @Payload WebRTCSignal signal) {

        log.debug("WebRTC signal: type={}, room={}, from={}",
                signal.getType(), roomId, signal.getSenderId());

        signal.setRoomId(roomId);

        // Relay to all participants in the room
        messagingTemplate.convertAndSend(
                "/topic/interview/" + roomId + "/signal", signal);
    }
}