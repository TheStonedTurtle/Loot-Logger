package thestonedturtle.lootlogger;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup("lootlogger")
public interface LootLoggerConfig extends Config
{
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
		position = 2,
		keyName = "itemBreakdown",
		name = "Breakdown individual items",
		description = "Shows more information for each item received instead of displaying the items in a compact grid"
	)
	default boolean itemBreakdown()
	{
		return false;
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
		description = "Whether to ignore loot received while inside Nightmare Zone"
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
		keyName = "migratedUsers",
		name = "Migrated users",
		description = "CSV of usernames (login name) that have been migrated",
		hidden = true
	)
	default String getMigratedUsers()
	{
		return "";
	}

	@ConfigItem(
		keyName = "migratedUsers",
		name = "",
		description = ""
	)
	void setMigratedUsers(String key);
}
