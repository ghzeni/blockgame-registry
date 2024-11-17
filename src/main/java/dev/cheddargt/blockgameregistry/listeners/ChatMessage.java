package dev.cheddargt.blockgameregistry.listeners;

import com.mojang.authlib.GameProfile;

import dev.cheddargt.blockgameregistry.BlockgameRegistry;
import dev.cheddargt.blockgameregistry.helpers.chatlog.ChatLog;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import java.time.Instant;

public class ChatMessage {

    public static void register () {
        BlockgameRegistry.LOGGER.info("Registry is listening!");

        ClientReceiveMessageEvents.CHAT.register((text, signedMessage, gameProfile, parameters, instant) -> onChatMessageReceived(text, parameters, instant));

        // message, signedMessage, sender, params, receptionTimestamp
        ClientReceiveMessageEvents.GAME.register((text, b) -> onGameMessageReceived(text));
    }

    private static void onChatMessageReceived(Text message, MessageType.Parameters params, Instant instant) {
        // Handle chat message
        BlockgameRegistry.LOGGER.info("Chat message received: {}", message);
        ChatLog.addMessage(message);
        ChatLog.serialize();
    }

    private static void onGameMessageReceived(Text message) {
        // Handle chat message
        BlockgameRegistry.LOGGER.info("Game message received: {}", message);
        ChatLog.addMessage(message);
        ChatLog.ticksSavingCounter();
    }
}
