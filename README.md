# Loot Logger [![Plugin Installs](http://img.shields.io/endpoint?url=https://i.pluginhub.info/shields/installs/plugin/loot-logger)](https://runelite.net/plugin-hub/TheStonedTurtle) [![Plugin Rank](http://img.shields.io/endpoint?url=https://i.pluginhub.info/shields/rank/plugin/loot-logger)](https://runelite.net/plugin-hub)

### Requires the `Loot Tracker` plugin to be enabled

Logs Loot Tracker data locally and adds new side-panel UI for viewing it.

## Data Storage
Data is stored at `~/.runelite/loots/HASH` where `HASH` is your account's unique hash, this was changed from your login name (or email address) as RuneLite is not aware of your login name when ran through the Jagex launcher.

Records are split into individual files by using the records name (`*name*.log`).

Data is stored in <a href="http://jsonlines.org/" target="_blank">JSON Lines format</a>.

The data stored is based off the <a href="https://github.com/TheStonedTurtle/Loot-Logger/blob/master/src/main/java/thestonedturtle/lootlogger/localstorage/LTRecord.java" target="_blank">LTRecord file</a>.

