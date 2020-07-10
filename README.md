# Loot Logger

### Requires the `Loot Tracker` plugin to be enabled

Logs Loot Tracker data locally and adds new side-panel UI for viewing it.

## Data Storage
Data is stored at `~/.runelite/loots/USERNAME` where `USERNAME` is your account's login name (or email address).

Records are split into individual files by using the records name (`*name*.log`).

Data is stored in <a href="http://jsonlines.org/" target="_blank">JSON Lines format</a>.

The data stored is based off the <a href="https://github.com/TheStonedTurtle/Loot-Logger/blob/master/src/main/java/thestonedturtle/lootlogger/localstorage/LTRecord.java" target="_blank">LTRecord file</a>.

