package dev.cheddargt.blockgameregistry.listeners;

import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.slf4j.Logger;

public class ChatMessage {
    static Logger chatLogger;

    public static void register (Logger logger) {
        chatLogger = logger;

        ServerMessageEvents.CHAT_MESSAGE.register((message, player, params) -> {
            onChatMessageReceived(player, message);
        });

        ServerMessageEvents.GAME_MESSAGE.register((server, message, overlay) -> {
            onGameMessageReceived(server, message);

        });

        ServerMessageEvents.COMMAND_MESSAGE.register((message, source, params) -> {
            onCommandMessageReceived(source, message);
        });
    }

    private static void onChatMessageReceived(ServerPlayerEntity player, SignedMessage message) {
        // Handle chat message
        System.out.println("Chat message received: " + message.getContent().getString());

        if (message.getContent().getString().contains("breakpoint")) {
            chatLogger.info("===== breakpoint reached!: {}", message.getContent().getString());
            chatLogger.info("===== breakpoint ended!!: {}", message.getContent().getString());
        }
    }

    private static void onGameMessageReceived(MinecraftServer server, Text message) {
        // Handle game message
        chatLogger.info("Game message received: {}", message.getString());
    }

    private static void onCommandMessageReceived(ServerCommandSource source, SignedMessage message) {
        //Handle command message
        chatLogger.info("Command message received: {}", message.getContent().getString());
    }
}
