package dev.cheddargt.blockgameregistry.helpers.chatlog;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;

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
    public static int SECONDS_SAVING_INTERVAL = 6;
    private static int ticksSavingInterval = 1000*SECONDS_SAVING_INTERVAL;
    public static final Path AUCTION_HOUSE_PATH = FabricLoader.getInstance().getGameDir().resolve("logs").resolve("blockgame-registry").resolve("auction-house").resolve("receipts.json");
    private static final List<Text> gameMessages = new ArrayList<>();
    private static final List<String> messages = new ArrayList<>();
    private static final int MAX_MESSAGES = 100;
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Text.class, (JsonSerializer<Text>) (src, type, context) -> Text.Serializer.toJsonTree(src))
            .registerTypeAdapter(Text.class, (JsonDeserializer<Text>) (json, type, context) -> Text.Serializer.fromJson(json))
            .registerTypeAdapter(Text.class, (InstanceCreator<Text>) type -> Text.empty())
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
            for (ParsedMessage msg : parsedMessages.getSales()) {
                String json = GSON.toJson(msg);
                writer.write(json);
                writer.newLine();
            }
            for (ParsedMessage msg : parsedMessages.getListings()) {
                String json = GSON.toJson(msg);
                writer.write(json);
                writer.newLine();
            }
            BlockgameRegistry.LOGGER.info("Updated chat log");
            gameMessages.clear();
            messages.clear();
        } catch (IOException e) {
            BlockgameRegistry.LOGGER.error("Failed to update chat log", e);
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
                if (extraElement.isJsonObject()) {
                    result.append(extractText(extraElement.getAsJsonObject()));
                }
            }
        }

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

//    public static void ticksSavingCounter() {
//        if (ticksSavingInterval == 0) {
//            serialize();
//            ticksSavingInterval = 1000*SECONDS_SAVING_INTERVAL; // Reset the counter
//        }
//        ticksSavingInterval--;
//    }


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
        return new ParsedMessage(itemName, itemPrice, "listing", itemAmount);
    }

    public static ParsedMessage addSale(String msg) {
        // Example: zAuctionHouse • Steve just bought Candy Corn for 45 Coin.
        String transactionInfo = msg.split(" just bought ")[1];
        String itemName = transactionInfo.split(" for ")[0];
        String itemPrice = transactionInfo.split(" for ")[1].split(" Coin")[0];
        return new ParsedMessage(itemName, itemPrice, "sale", null);
    }

    public static ParsedMessages parseSalesListings(List<Text> allMessages) {
        List<ParsedMessage> sales = new ArrayList<>();
        List<ParsedMessage> listings = new ArrayList<>();

        for (Text msg : allMessages) {
            BlockgameRegistry.LOGGER.info("Parsing message: {}", msg);
            String parsedMsg = extractText(Text.Serializer.toJsonTree(msg).getAsJsonObject());

            if (parsedMsg.contains("removed") || !shouldSaveMessage(parsedMsg)) {
                continue;
            } else if (parsedMsg.contains("bought")) {
                BlockgameRegistry.LOGGER.info("sales++");
                sales.add(addSale(parsedMsg));
            } else if (parsedMsg.contains("sale")) {
                BlockgameRegistry.LOGGER.info("listings++");
                listings.add(addListing(parsedMsg));
            }
        }

        return new ParsedMessages(sales, listings);
    }

    public static boolean shouldSaveMessage(String msg) {
        boolean isAuctionMsg = msg.contains("zAuctionHouse");

        BlockgameRegistry.LOGGER.info("shouldSaveMessage: {}", isAuctionMsg);
        return isAuctionMsg && ENABLE_AUCTION_RECEIPTS;
    }

    private static class Data {
        List<ParsedMessage> sales;
        List<ParsedMessage> listings;

        Data(List<ParsedMessage> sales, List<ParsedMessage> listings) {
            this.sales = sales;
            this.listings = listings;
        }

        public List<ParsedMessage> getSales() {
            return sales;
        }

        public void setSales(List<ParsedMessage> sales) {
            this.sales = sales;
        }

        public List<ParsedMessage> getListings() {
            return listings;
        }

        public void setListings(List<ParsedMessage> listings) {
            this.listings = listings;
        }
    }

    public static class ParsedMessages {
        private List<ParsedMessage> sales;
        private List<ParsedMessage> listings;

        public ParsedMessages(List<ParsedMessage> sales, List<ParsedMessage> listings) {
            this.sales = sales;
            this.listings = listings;
        }

        public List<ParsedMessage> getSales() {
            return sales;
        }

        public void setSales(List<ParsedMessage> sales) {
            this.sales = sales;
        }

        public List<ParsedMessage> getListings() {
            return listings;
        }

        public void setListings(List<ParsedMessage> listings) {
            this.listings = listings;
        }
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
