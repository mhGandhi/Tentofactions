package com.tentomax.managers;

import com.tentomax.models.ChatMode;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChatManager {
    private static final Map<UUID, ChatMode> chatModes = new HashMap<>();

    public static void setChatMode(UUID playerId, ChatMode mode) {
        chatModes.put(playerId, mode);
    }

    public static ChatMode getChatMode(UUID playerId) {
        return chatModes.getOrDefault(playerId, ChatMode.PUBLIC);
    }
}
