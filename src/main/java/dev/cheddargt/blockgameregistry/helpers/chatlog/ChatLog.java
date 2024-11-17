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

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class ChatLog {
    public static boolean ENABLE_AUCTION_RECEIPTS = true;
    public static int SECONDS_SAVING_INTERVAL = 6; // move to config
    private static int ticksSavingInterval = 1000*SECONDS_SAVING_INTERVAL; // move to config
    public static final Path AUCTION_HOUSE_PATH = FabricLoader.getInstance().getGameDir().resolve("logs").resolve("blockgame-registry").resolve("auction-house").resolve("receipts.json");
    private static final List<Text> gameMessages = new ArrayList<>();
    private static final List<String> messages = new ArrayList<>();
    private static final int MAX_MESSAGES = 50; // Adjust this value as needed
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
        try (BufferedWriter writer = Files.newBufferedWriter(AUCTION_HOUSE_PATH, StandardOpenOption.APPEND, StandardOpenOption.CREATE)) {
            ParsedMessages parsedMessages = parseSalesListings(gameMessages);
            for (ParsedMessage msg : parsedMessages.getMessageHistory()) {
                String json = GSON.toJson(msg);
                writer.write(json);
                writer.newLine();
            }
            messages.clear(); // Remover mensagens da memória após a escrita
            BlockgameRegistry.LOGGER.info("Updated auction-history em {}", AUCTION_HOUSE_PATH);
        } catch (IOException e) {
                BlockgameRegistry.LOGGER.error("Failed to update auction-history", e);
            }
    }

    // Extracts the text from a JSON object
    private static String extractText(JsonObject jsonObject) {
        StringBuilder result = new StringBuilder();

        // Adds the value of the "text" key, if it exists
        if (jsonObject.has("text")) {
            result.append(jsonObject.get("text").getAsString());
        }

        // Processes the "extra" array recursively, if it exists
        if (jsonObject.has("extra")) {
            JsonArray extraArray = jsonObject.getAsJsonArray("extra");
            for (JsonElement extraElement : extraArray) {
                if (extraElement.isJsonObject()) {
                    result.append(extractText(extraElement.getAsJsonObject()));
                }
            }
        }

        // Processes the "with" array recursively, if it exists
        if (jsonObject.has("with")) {
            JsonArray withArray = jsonObject.getAsJsonArray("with");
            for (JsonElement withElement : withArray) {
                if (withElement.isJsonObject()) {
                    result.append(extractText(withElement.getAsJsonObject()));
                }
            }
        }

        return result.toString();
    }

    public static void ticksSavingCounter() {
        serialize();
        if (ticksSavingInterval == 0) {
            ticksSavingInterval = 1000*SECONDS_SAVING_INTERVAL; // Reset the counter
        }
        ticksSavingInterval--;
    }


    public static void addMessage(Text message) {
        gameMessages.add(message);
        BlockgameRegistry.LOGGER.info("Adding message to messages array");

        if (messages.size() > MAX_MESSAGES) {
            messages.remove(0);
        }
    }

    public static ParsedMessage addListing(String msg) {
        // Example: zAuctionHouse • You just put x49 Candy Corn on sale for 45 Coin.
        String transactionInfo = msg.split("You just put x")[1];
        String[] itemAmountAndName = transactionInfo.split(" ");
        String itemAmount = itemAmountAndName[0];
        String itemName = transactionInfo.split(" on sale for ")[0].substring(itemAmount.length() + 1);
        String itemPrice = transactionInfo.split(" on sale for ")[1].split(" Coin")[0];
        return new ParsedMessage(itemName, itemPrice, itemAmount, "listing");
    }

    public static ParsedMessage addSale(String msg) {
        // Example: zAuctionHouse • Steve just bought Candy Corn for 45 Coin.
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

    public static ParsedMessages parseSalesListings(List<Text> allMessages) {
        List<ParsedMessage> history = new ArrayList<>();

        BlockgameRegistry.LOGGER.info("Parsing sales and listings from game messages");

        for (Text msg : allMessages) {
            JsonObject jsonObject = GSON.toJsonTree(msg).getAsJsonObject();
            String parsedMsg = extractText(jsonObject);
            boolean saveMessage = shouldSaveMessage(parsedMsg);
            if (!saveMessage && parsedMsg.contains("removed")) {
                continue;
            } else if (saveMessage && parsedMsg.contains("bought")) {
                BlockgameRegistry.LOGGER.info("sales++");
                history.add(addSale(parsedMsg));
            } else if (saveMessage && parsedMsg.contains("sale")) {
                BlockgameRegistry.LOGGER.info("listings++");
                history.add(addListing(parsedMsg));
            }
        }

        return new ParsedMessages(history);
    }

    public static boolean shouldSaveMessage(String msg) {
        boolean isAuctionMsg = msg.contains("zAuctionHouse");

        BlockgameRegistry.LOGGER.info(isAuctionMsg + " - " + msg);
        if (isAuctionMsg && ENABLE_AUCTION_RECEIPTS) {
            return true;
        }
        return false;
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
