package dev.cheddargt.blockgameregistry.listeners;

import dev.cheddargt.blockgameregistry.BlockgameRegistry;
import dev.cheddargt.blockgameregistry.helpers.chatlog.ChatLog;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.network.message.MessageType;
import net.minecraft.text.Text;
import java.time.Instant;

public class ChatMessage {

    public static void register () {
        BlockgameRegistry.LOGGER.info("Registry is listening!");

        // message, signedMessage, sender, params, receptionTimestamp
        ClientReceiveMessageEvents.CHAT.register((text, signedMessage, gameProfile, parameters, instant) -> onChatMessageReceived(text, parameters, instant));

        // message, overlay (boolean)
        ClientReceiveMessageEvents.GAME.register((text, b) -> onGameMessageReceived(text));
    }

    private static void onChatMessageReceived(Text message, MessageType.Parameters params, Instant instant) {
        ChatLog.addMessage(message);
    }

    private static void onGameMessageReceived(Text message) {
        ChatLog.addMessage(message);
    }
}
