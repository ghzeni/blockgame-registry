package dev.cheddargt.blockgameregistry.listeners;

import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.time.Instant;

public class ChatMessage {
    static Logger chatLogger;

    public static void register (Logger logger) {
        chatLogger = logger;
        logger.info("Registry is listening!");

        ClientReceiveMessageEvents.CHAT.register(new ClientReceiveMessageEvents.Chat() {
            @Override
            public void onReceiveChatMessage(Text text, @Nullable SignedMessage signedMessage, @Nullable GameProfile gameProfile, MessageType.Parameters parameters, Instant instant) {
                onChatMessageReceived(text, parameters, instant);
            }
        });

        // message, signedMessage, sender, params, receptionTimestamp
        ClientReceiveMessageEvents.GAME.register(new ClientReceiveMessageEvents.Game() {
            @Override
            public void onReceiveGameMessage(Text text, boolean b) {
                onGameMessageReceived(text);
            }
        });
    }

    private static void onChatMessageReceived(Text message, MessageType.Parameters params, Instant instant) {
        // Handle chat message

        chatLogger.info("Chat message received: {}", message);
        if (message.getString().contains("breakpoint")) {
            chatLogger.info("===== breakpoint reached!: {}", message.getString());
            chatLogger.info("===== breakpoint ended!!: {}", message.getString());
        }
    }

    private static void onGameMessageReceived(Text message) {
        // Handle chat message

        chatLogger.info("Game message received: {}", message);
        if (message.getString().contains("breakpoint")) {
            chatLogger.info("===== (g) breakpoint reached!: {}", message.getString());
            chatLogger.info("===== (g) breakpoint ended!!: {}", message.getString());
        }
    }
}
