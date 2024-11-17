# Blockgame Registry

This is a mod created for storing data from sources the player encounter throughout the gameplay. The current single available source of data is `chat messages` and the type of data is `zAuctionHouse` messages.

- When you list an item in the Auction House, the listener will pick up that message, parse it and write it to a json file.
- When you sell an item in the Auction House, the listener will also parse and save that message.