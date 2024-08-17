## Changelog

### Version 2.0
- **Feature Addition**: Added the `/playtrack` command to allow players to check their total playtime directly in-game.
- **Playtime Tracking**: Playtime is now calculated in seconds and saved automatically in `playtime.json` located in the root directory of the mod.
- **Data Persistence**: Playtime data is dynamically saved and retrieved for all players, ensuring accurate tracking across sessions.
- **Error Handling**: Added error messages to inform players if their playtime data is not available.

---

## Usage Instructions

### Installation
1. Download the mod `.jar` file.
2. Place the `.jar` file in the `mods` folder of your Minecraft installation.

### Commands
- **`/playtrack`**: 
  - **Description**: Displays the total playtime for the player who triggers the command.
  - **Usage**: Simply type `/playtrack` in the chat.
  - **Error Handling**: If no playtime data is found for the player, an error message will be displayed in the chat.

### File Location
- Playtime data is stored in `playtime.json` within the root directory of the mod. This file is automatically created and updated as players join and leave the server.

### Customization
- The mod is designed to be simple and user-friendly. No additional configuration is required.

### Compatibility
- This mod is compatible with Minecraft version 1.21 and requires Fabric Loader 0.15.11 or later.
