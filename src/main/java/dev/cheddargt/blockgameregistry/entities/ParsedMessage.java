package dev.cheddargt.blockgameregistry.entities;

import org.apache.logging.log4j.core.time.Instant;

import java.sql.Timestamp;
import java.util.Date;

public class ParsedMessage {
    private String itemName;
    private String price;
    private String amount;
    private String type;
    private Date timestamp;

    public ParsedMessage(String itemName, String price, String amount, String type) {
        this.itemName = itemName;
        this.price = price;
        this.amount = amount;
        this.type = type;
        this.timestamp = new Date();
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