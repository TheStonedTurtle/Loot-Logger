package thestonedturtle.lootlogger;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

@ConfigGroup("lootlogger")
public interface LootLoggerConfig extends Config
{
	@ConfigSection(
		name = "Playback Settings",
		description = "The options that control the playback feature of the loot panel",
		position = 100,
		closedByDefault = true
	)
	String playbackSection = "playbackSection";

	@ConfigItem(
		keyName = "enableUI",
		name = "Enable Side-Panel",
		description = "Controls whether the side panel should be displayed, data will be logged either way"
	)
	default boolean enableUI()
	{
		return true;
	}

	@ConfigItem(
		position = 0,
		keyName = "uniquesPlacement",
		name = "Uniques Placement",
		description = "Where should unique items be displayed inside the UI"
	)
	default UniqueItemPlacement uniquesPlacement()
	{
		return UniqueItemPlacement.BOTH;
	}

	@ConfigItem(
		position = 1,
		keyName = "itemSortType",
		name = "Item Sorting",
		description = "Determines how items should be sorted inside the item breakdown"
	)
	default ItemSortTypes itemSortType()
	{
		return ItemSortTypes.VALUE;
	}

	@ConfigItem(
		position = 3,
		keyName = "bossButtons",
		name = "Show boss icons",
		description = "Toggles whether the selection screen will use the boss icons"
	)
	default boolean bossButtons()
	{
		return true;
	}

	@ConfigItem(
		position = 4,
		keyName = "ignoreNmz",
		name = "Ignore nightmare zone",
		description = "Whether to ignore loot received while inside Nightmare Zone",
		hidden = true
	)
	default boolean ignoreNmz()
	{
		return true;
	}

	@ConfigItem(
		position = 5,
		keyName = "itemMissingAlpha",
		name = "Missing item opacity",
		description = "Controls the image opacity for unacquired unique items inside the uniques panel.<br/>0 is transparent and 100 is fully opaque. default value is 35"
	)
	@Range(max = 100)
	default int itemMissingAlpha() { return 35; }

	@ConfigItem(
		position = 6,
		keyName = "includeMinions",
		name = "Include Minions",
		description = "Toggles whether loot from minions will be included when looking at specific loot tabs, such as the GWD bosses"
	)
	default boolean includeMinions()
	{
		return true;
	}

	@Range(min = 1, max = 100)
	@ConfigItem(
		position = 0,
		keyName = "playbackUpdateLimit",
		name = "Update Limit",
		description = "Controls the number of times, per second, the playback can update the UI<br/><br/>Setting this value too low can result in lag, do not test this in dangerous areas",
		section = playbackSection
	)
	default int playbackUpdateLimit()
	{
		return 10;
	}

	@Range(min = 0, max = 10000)
	@ConfigItem(
		position = 1,
		keyName = "uniquePauseDuration",
		name = "Unique Pause Duration",
		description = "Controls how long, in milliseconds, the replay will pause on a unique item being added to the log<br/><br/>1000 milliseconds = 1 second<br/>0 will disable the pause entirely<br/>Respects the Update Limit value when set very low",
		section = playbackSection
	)
	default int uniquePauseDuration()
	{
		return 2000;
	}
}
