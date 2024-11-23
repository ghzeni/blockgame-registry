package dev.cheddargt.blockgameregistry;

import dev.cheddargt.blockgameregistry.helpers.chatlog.ChatLog;
import dev.cheddargt.blockgameregistry.listeners.ChatMessage;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class BlockgameRegistry implements ModInitializer {
	public static final String MOD_ID = "blockgame-registry";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Registry is alive!");
		ChatMessage.register();

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			ChatLog.ticksSavingCounter();
		});
	}
}