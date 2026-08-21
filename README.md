## <center>Lucky Games</center>

A mini minecraft luckygames plugin for Paper.  
Test your luck on randomly generated tracks, open lucky blocks, experience a diverse set of events, and have fun with your friends.

**Supported version**: 26.1.2

---
- [Features](#features)
- [Installation](#installation)
- [Configuration](#configuration)
- [Usage](#usage)
- [Development](#development)
- [Credits](#credits)

---
### <a id="features"></a> Features

- **Lobby**: Comes with a prebuilt (overridable) lobby, resets automatically on every game start.
- **Automatic, randomized track generation**: Tracks are generated on every game start, no two games are the same.
- **Configurable game settings**: Track generation (how many sections, how many islands a section should have, lucky block distribution) can be configured. Inventory keeping and other utility settings can also be set in-game.
- **A diverse set of events**: Various loots, random mobs, custom structures, and exciting challenges.
- **Player interactions included**: Players are not confined to their own track, and a multiplayer match is arranged at the end of the game.
- **Plugins integration**: Supports contents from [QualityArmory](https://github.com/Lorenzo0111/QualityArmory)

---
### <a id="installation"></a> Installation

Not yet in release version.  
See [Development](#development)

---
### <a id="configuration"></a> Configuration

See (config.yml)[src/main/resources/config.yml]  
The luckyblock-distribution section defines the generation possibility for each type of luckyblock on track generation.  
The luckyblock section defines the possibility of the type of event triggered when a lucky block is opened.  
The weights don't need to sum up to 100.


---
### <a id="usage"></a> Usage

- `/luckygames run` to run the game. (Requires permission `luckygames.control_game`)
- `/luckygames set [entry] [value]` to config the settings in-game. (Requires permission `luckygames.change_game_settings`)
- `/luckygames lobby [force]` to return to lobby. (Without `force`: Sends the executor to the lobby when the game is finished; With `force`: sends everyone to the lobby and forcefully ends the game, requires permission `luckygames.control_game`)
- `/luckyblock place [location] [type] [attribution]` to place a lucky block at the specified location. (Requires permission `luckygames.luckyblock.place`)

---
### <a id="development"></a> Development

Clone the repo and run.
```bash
./gradlew shadowJar
```
The output can then be placed in the paper `plugins` folder.

---
### <a id="credits"></a> Credits

- [**DontDoIt**](https://github.com/NMB-Court-Team/DontDoIt): Inspiration for better not challenge.
- **Buildings**: [Shop](https://mcblock.top/buildings/ef21db83-3824-40e4-b953-23e95612dcc3), [Quay](https://mcblock.top/buildings/a8441f7d-0b9d-4d8d-914a-8af802d8a145), [Lantern](https://mcblock.top/buildings/363f7b0b-3499-4920-830a-572c03f595cd), [Pavilion](https://mcblock.top/buildings/d764cb71-0fcf-41bd-b6bf-e8e8b39b3463)
