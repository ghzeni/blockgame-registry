package dev.cheddargt.blockgameregistry.entities;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Date;

public class ParsedMessage {
    private String itemName;
    private String price;
    private String type;
    private String amount;
    private Date timestamp;

    public ParsedMessage(String itemName, String price, String type, String amount) {
        this.itemName = itemName;
        this.price = price;
        this.type = type;
        this.amount = amount;
        this.timestamp = Date.from(Instant.now());
    }

    // Getters and setters
    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}