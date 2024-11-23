# Blockgame Registry

This is a mod created for storing data from sources the player encounter throughout the gameplay. The current single available source of data is `chat messages` and the type of data is `zAuctionHouse` messages.

- When you list/sell an item in the Auction House, the listener will pick up that message, parse it and write it to a json file.

## Roadmap

[ ] 1.0.0 
  [x] 0.1.0 read, parse and write sales/listings to receipt.json
    [x] 0.1.1 improve exception handling to avoid getting banned lol
  [ ] 0.2.0 mod menu config screen to enable/disable mod
  [ ] 0.3.0 config setting for writing interval
  [ ] 0.4.0 config setting for json file formatting (json line, single line, etc.)
  
  [ ] 0.7.0 create entities and classes for retrieving data (to be used by other mods)
  [ ] 0.8.0 proper documentation, error handling and formatting
  [ ] 0.9.0 add sql lite db
