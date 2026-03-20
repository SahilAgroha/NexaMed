package com.nexamed.interview.dto;

import lombok.Data;

/**
 * WebRTC signaling message — relayed between peers via WebSocket STOMP.
 * Types: OFFER, ANSWER, ICE_CANDIDATE, JOIN, LEAVE
 */
@Data
public class WebRTCSignal {
    private String type;       // signal type
    private String roomId;
    private String senderId;
    private Object payload;    // SDP offer/answer or ICE candidate object
}