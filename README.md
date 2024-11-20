# Blockgame Registry

This is a mod created for storing data from sources the player encounter throughout the gameplay. The current single available source of data is `chat messages` and the type of data is `zAuctionHouse` messages.

- When you list/sell an item in the Auction House, the listener will pick up that message, parse it and write it to a json file.

## Roadmap

- [ ] 1.0.0 

  - [x] 0.1.0 read, parse and write sales/listings to receipt.json

  - [ ] 0.2.0 add "by" field to listings/sales
  
  - [ ] 0.3.0 mod menu config file
     
  - [ ] 0.4.0 mod menu config screen

- [ ] 2.0.0
  
  - [ ] 1.1.0 config setting for writing interval

  - [ ] 1.2.0 config setting for json file formatting (json line, single line, etc)
  
  - [ ] 1.3.0 create entities and classes for retrieving data (to be used by other mods)

  - [ ] 1.4.0 proper documentation, error handling and formatting

  - [ ] 1.5.0 add sql lite db
