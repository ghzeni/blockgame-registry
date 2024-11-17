package dev.cheddargt.blockgameregistry.helpers.chatlog;

import net.fabricmc.loader.api.FabricLoader;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializer;

import dev.cheddargt.blockgameregistry.BlockgameRegistry;
import dev.cheddargt.blockgameregistry.entities.ParsedMessage;
import net.minecraft.text.Text;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ChatLog {
    public static final Path AUCTION_HOUSE_PATH = FabricLoader.getInstance().getGameDir().resolve("logs").resolve("blockgame-registry").resolve("auction-house").resolve("auction-house.json");
    private static final List<String> messages = new ArrayList<>();
    private static int ticksSavingInterval = 20;
    private static final int MAX_MESSAGES = 120; // Adjust this value as needed
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Text.class, (JsonSerializer<Text>) (src, type, context) -> Text.Serializer.toJsonTree(src))
            .registerTypeAdapter(Text.class, (JsonDeserializer<Text>) (json, type, context) -> Text.Serializer.fromJson(json))
            .create();

    private static void ensureDirectoryExists() {
        try {
            Files.createDirectories(AUCTION_HOUSE_PATH.getParent());
        } catch (IOException e) {
            BlockgameRegistry.LOGGER.error("Failed to create directories for path: {}", AUCTION_HOUSE_PATH, e);
        }
    }

    public static void serialize() {
        ensureDirectoryExists();
        try {
            ParsedMessages parsedMessages = parseSalesListings(messages);
            String json = GSON.toJson(parsedMessages);
            Files.writeString(AUCTION_HOUSE_PATH, json);
            BlockgameRegistry.LOGGER.info("Chat log saved to {}", AUCTION_HOUSE_PATH);
        } catch (IOException e) {
            BlockgameRegistry.LOGGER.error("Failed to save chat log", e);
        }
    }

    private static String extractText(JsonObject jsonObject) {
        StringBuilder result = new StringBuilder();

        if (jsonObject.has("text")) {
            result.append(jsonObject.get("text").getAsString());
        }

        if (jsonObject.has("extra")) {
            JsonArray extraArray = jsonObject.getAsJsonArray("extra");
            for (JsonElement extraElement : extraArray) {
                result.append(extractText(extraElement.getAsJsonObject()));
            }
        }

        return result.toString();
    }

    public static void ticksSavingCounter() {
        if (ticksSavingInterval == 0) {
            serialize();
            ticksSavingInterval = 20; // Reset the counter
        }
        ticksSavingInterval--;
    }


    public static void addMessage(Text message) {
        JsonObject jsonObject = GSON.toJsonTree(message).getAsJsonObject();
        String parsedText = extractText(jsonObject);

        if (parsedText.contains("zAuctionHouse")) {
            messages.add(parsedText);
        }

        if (messages.size() > MAX_MESSAGES) {
            messages.remove(0);
        }
    }

    public static ParsedMessage addListing(String msg) {
        // Example: You just put x49 Candy Corn on sale for 45 Coin.
        String transactionInfo = msg.split("You just put x")[1];
        String[] itemAmountAndName = transactionInfo.split(" ");
        String itemAmount = itemAmountAndName[0];
        String itemName = transactionInfo.split(" on sale for ")[0].substring(itemAmount.length() + 1);
        String itemPrice = transactionInfo.split(" on sale for ")[1].split(" Coin")[0];
        return new ParsedMessage(itemName, itemPrice, itemAmount, "listing");
    }

    public static ParsedMessage addSale(String msg) {
        // Example: Thebigshabam just bought Candy Corn for 45 Coin.
        String transactionInfo = msg.split(" just bought ")[1];
        String itemName = transactionInfo.split(" for ")[0];
        String itemPrice = transactionInfo.split(" for ")[1].split(" Coin")[0];
        return new ParsedMessage(itemName, itemPrice, null, "sale");
    }

    public static class ParsedMessages {
        private List<ParsedMessage> messageHistory;

        public ParsedMessages(List<ParsedMessage> history) {
            this.messageHistory = history;
        }

        // Getters and setters
        public List<ParsedMessage> getMessageHistory() {
            return messageHistory;
        }

        public void setMessageHistory(List<ParsedMessage> history) {
            this.messageHistory = history;
        }
    }

    public static ParsedMessages parseSalesListings(List<String> allMessages) {
        List<ParsedMessage> history = new ArrayList<>();

        for (String msg : allMessages) {
            if (msg.contains("removed")) {
                continue;
            } else if (msg.contains("bought")) {
                history.add(addSale(msg));
            } else if (msg.contains("sale")) {
                history.add(addListing(msg));
            }
        }

        return new ParsedMessages(history);
    }
}

//    public static void deserialize() {
//        if (Files.exists(AUCTION_HOUSE_PATH)) {
//            try {
//                String json = Files.readString(AUCTION_HOUSE_PATH);
//                Data data = GSON.fromJson(json, Data.class);
//                messages.clear();
//                history.clear();
//                // Convert ParsedMessages back to Text if needed
//                // messages.addAll(data.getMessages());
//                // history.addAll(data.getHistory());
//                BlockgameRegistry.LOGGER.info("Chat log loaded from {}", AUCTION_HOUSE_PATH);
//
//                // Example usage after deserialization
//                List<String> parsedMessages = new ArrayList<>();
//                for (ParsedMessage sale : data.getSales()) {
//                    parsedMessages.add(sale.toString()); // Adjust as needed
//                }
//                for (ParsedMessage listing : data.getListings()) {
//                    parsedMessages.add(listing.toString()); // Adjust as needed
//                }
//                ParsedMessages categorizedMessages = parseSalesListings(parsedMessages);
//                // Do something with categorizedMessages if needed
//
//            } catch (IOException e) {
//                BlockgameRegistry.LOGGER.error("Failed to load chat log", e);
//            }
//        }
//    }
