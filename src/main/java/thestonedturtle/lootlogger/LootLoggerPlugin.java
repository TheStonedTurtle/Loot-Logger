package thestonedturtle.lootlogger;

import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.ItemComposition;
import net.runelite.api.NpcID;
import net.runelite.api.Player;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.Text;
import thestonedturtle.lootlogger.data.BossTab;
import thestonedturtle.lootlogger.data.UniqueItem;
import thestonedturtle.lootlogger.localstorage.LTItemEntry;
import thestonedturtle.lootlogger.localstorage.LTRecord;
import thestonedturtle.lootlogger.localstorage.LootRecordWriter;
import thestonedturtle.lootlogger.ui.LootLoggerPanel;

@Slf4j
@PluginDescriptor(
	name = "Loot Logger"
)
public class LootLoggerPlugin extends Plugin
{
	private static final String SIRE_FONT_TEXT = "you place the unsired into the font of consumption...";
	private static final String SIRE_REWARD_TEXT = "the font consumes the unsired";
	private static final int MAX_TEXT_CHECK = 10;

	// Kill count handling
	private static final Pattern CLUE_SCROLL_PATTERN = Pattern.compile("You have completed [0-9]+ ([a-z]+) Treasure Trails.");
	private static final Pattern BOSS_NAME_NUMBER_PATTERN = Pattern.compile("Your (.*) kill count is:? ([0-9]*).");
	private static final Pattern NUMBER_PATTERN = Pattern.compile("([0-9]+)");

	@Inject
	private Client client;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	public LootLoggerConfig config;

	@Inject
	private ItemManager itemManager;

	@Inject
	private ClientThread clientThread;

	@Inject
	private LootRecordWriter writer;

	private LootLoggerPanel panel;
	private NavigationButton navButton;

	@Getter
	private TreeSet<String> lootNames = new TreeSet<>();

	private boolean prepared = false;
	private boolean unsiredReclaiming = false;
	private int unsiredCheckCount = 0;

	private Map<String, Integer> killCountMap = new HashMap<>();

	@Provides
	LootLoggerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(LootLoggerConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		panel = new LootLoggerPanel(itemManager, this);

		final BufferedImage icon = ImageUtil.getResourceStreamFromClass(getClass(), "panel-icon.png");

		navButton = NavigationButton.builder()
			.tooltip("Loot Logger")
			.icon(icon)
			.priority(6)
			.panel(panel)
			.build();

		clientToolbar.addNavigation(navButton);

		// Attach necessary info from item manager on load
		if (!prepared)
		{
			prepared = true;
			clientThread.invokeLater(() ->
			{
				switch (client.getGameState())
				{
					case UNKNOWN:
					case STARTING:
						return false;
				}

				UniqueItem.prepareUniqueItems(itemManager);
				writer.setPlayerUsername("StonedTurtle");
				localPlayerNameChanged();
				return true;
			});
		}
	}

	@Override
	protected void shutDown()
	{
		clientToolbar.removeNavigation(navButton);
	}

	@Subscribe
	public void onConfigChanged(final ConfigChanged event)
	{
		if (event.getGroup().equals("lootlogger"))
		{
			panel.refreshUI();
		}
	}

	@Subscribe
	public void onGameStateChanged(final GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGGING_IN)
		{
			clientThread.invokeLater(() ->
			{
				switch (client.getGameState())
				{
					case LOGGED_IN:
						final Player local = client.getLocalPlayer();
						if (local != null && local.getName() != null)
						{
							writer.setPlayerUsername(local.getName());
							localPlayerNameChanged();
							return true;
						}
					case LOGGING_IN:
					case LOADING:
						return false;
					default:
						// Quit running if any other state
						return true;
				}
			});
		}
	}

	private void localPlayerNameChanged()
	{
		lootNames.clear();
		lootNames.addAll(writer.getKnownFileNames());
		SwingUtilities.invokeLater(() -> panel.showSelectionView());
	}

	// TODO: Figure out how to trigger this when the Loot Tracker processes loot
	private void recordAdded(LTRecord record)
	{
		lootNames.add(record.getName());
		SwingUtilities.invokeLater(() -> panel.addLog(record));
	}

	public Collection<LTRecord> getDataByName(String name)
	{
		final BossTab tab = BossTab.getByName(name);
		if (tab != null)
		{
			name = tab.getName();
		}

		return writer.loadLootTrackerRecords(name);
	}

	public boolean clearStoredDataByName(final String name)
	{
		lootNames.remove(name);
		return writer.deleteLootTrackerRecords(name);
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event)
	{
		if (event.getGroupId() != WidgetID.DIALOG_SPRITE_GROUP_ID)
		{
			return;
		}

		Widget text = client.getWidget(WidgetInfo.DIALOG_SPRITE_TEXT);
		if (SIRE_FONT_TEXT.equals(text.getText().toLowerCase()))
		{
			unsiredCheckCount = 0;
			unsiredReclaiming = true;
		}
	}

	@Subscribe
	public void onGameTick(GameTick t)
	{
		if (unsiredReclaiming)
		{
			if (hasUnsiredWidgetUpdated())
			{
				unsiredReclaiming = false;
				return;
			}

			unsiredCheckCount++;
			if (unsiredCheckCount >= MAX_TEXT_CHECK)
			{
				unsiredReclaiming = false;
			}
		}
	}

	// Handles checking for unsired loot reclamation
	private boolean hasUnsiredWidgetUpdated()
	{
		final Widget text = client.getWidget(WidgetInfo.DIALOG_SPRITE_TEXT);
		// Reclaimed an item?
		if (text.getText().toLowerCase().contains(SIRE_REWARD_TEXT))
		{
			final Widget sprite = client.getWidget(WidgetInfo.DIALOG_SPRITE);
			log.debug("Unsired was exchanged for item ID: {}", sprite.getItemId());
			receivedUnsiredLoot(sprite.getItemId());
			return true;
		}

		return false;
	}

	// Handles adding the unsired loot to the tracker
	private void receivedUnsiredLoot(int itemID)
	{
		clientThread.invokeLater(() ->
		{
			Collection<LTRecord> data = getDataByName(BossTab.ABYSSAL_SIRE.getName());
			ItemComposition c = itemManager.getItemComposition(itemID);
			LTItemEntry itemEntry = new LTItemEntry(c.getName(), itemID, 1, 0);

			log.debug("Received Unsired item: {}", c.getName());

			// Don't have data for sire, create a new record with just this data.
			if (data == null)
			{
				log.debug("No previous Abyssal sire loot, creating new loot record");
				LTRecord r = new LTRecord(NpcID.ABYSSAL_SIRE, BossTab.ABYSSAL_SIRE.getName(), 350, -1, Collections.singletonList(itemEntry));
				writer.addLootTrackerRecord(r);
				return;
			}

			log.debug("Adding drop to last abyssal sire loot record");
			// Add data to last kill count
			List<LTRecord> items = new ArrayList<>(data);
			LTRecord r = items.get(items.size() - 1);
			r.addDropEntry(itemEntry);
			writer.writeLootTrackerFile(BossTab.ABYSSAL_SIRE.getName(), items);
			recordAdded(r);
		});
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (event.getType() != ChatMessageType.GAMEMESSAGE && event.getType() != ChatMessageType.SPAM)
		{
			return;
		}

		final String chatMessage = Text.removeTags(event.getMessage());

		// Check if message is for a clue scroll reward
		final Matcher m = CLUE_SCROLL_PATTERN.matcher(chatMessage);
		if (m.find())
		{
			final String eventType;
			switch (m.group(1).toLowerCase())
			{
				case "beginner":
					eventType = "Clue Scroll (Beginner)";
					break;
				case "easy":
					eventType = "Clue Scroll (Easy)";
					break;
				case "medium":
					eventType = "Clue Scroll (Medium)";
					break;
				case "hard":
					eventType = "Clue Scroll (Hard)";
					break;
				case "elite":
					eventType = "Clue Scroll (Elite)";
					break;
				case "master":
					eventType = "Clue Scroll (Master)";
					break;
				default:
					return;
			}

			final int killCount = Integer.valueOf(m.group(1));
			killCountMap.put(eventType.toUpperCase(), killCount);
			return;
		}

		// Barrows KC
		if (chatMessage.startsWith("Your Barrows chest count is"))
		{
			Matcher n = NUMBER_PATTERN.matcher(chatMessage);
			if (n.find())
			{
				killCountMap.put("BARROWS", Integer.valueOf(n.group()));
				return;
			}
		}

		// Raids KC
		if (chatMessage.startsWith("Your completed Chambers of Xeric count is"))
		{
			Matcher n = NUMBER_PATTERN.matcher(chatMessage);
			if (n.find())
			{
				killCountMap.put("CHAMBERS OF XERIC", Integer.valueOf(n.group()));
				return;
			}
		}
		
		// Tob KC
		if (chatMessage.startsWith("Your completed Theatre of Blood count is"))
		{
			Matcher n = NUMBER_PATTERN.matcher(chatMessage);
			if (n.find())
			{
				killCountMap.put("THEATRE OF BLOOD", Integer.valueOf(n.group()));
				return;
			}
		}

		// Handle all other boss
		final Matcher boss = BOSS_NAME_NUMBER_PATTERN.matcher(chatMessage);
		if (boss.find())
		{
			final String bossName = boss.group(1);
			final int killCount = Integer.valueOf(boss.group(2));
			killCountMap.put(bossName.toUpperCase(), killCount);
		}
	}
}
