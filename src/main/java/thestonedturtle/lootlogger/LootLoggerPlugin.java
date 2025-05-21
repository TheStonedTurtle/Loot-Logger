package thestonedturtle.lootlogger;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.SetMultimap;
import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.ItemComposition;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.RuneScapeProfileType;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStack;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.plugins.loottracker.LootReceived;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.Text;
import net.runelite.http.api.loottracker.LootRecordType;
import org.apache.commons.lang3.ArrayUtils;
import thestonedturtle.lootlogger.data.BossTab;
import thestonedturtle.lootlogger.data.KillCountNameMapper;
import thestonedturtle.lootlogger.data.LootLog;
import thestonedturtle.lootlogger.data.Pet;
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
	private static final int MAX_TEXT_CHECK = 25;
	private static final int MAX_PET_TICKS = 5;

	// Kill count handling
	private static final Pattern CLUE_SCROLL_PATTERN = Pattern.compile("You have completed ([0-9]+) ([a-z]+) Treasure Trails.");
	private static final Pattern BOSS_NAME_NUMBER_PATTERN = Pattern.compile("Your (.*) kill count is:? ([0-9,]*).");
	private static final Pattern NUMBER_PATTERN = Pattern.compile("([0-9,]+)");

	private static final NumberFormat NUMBER_FORMATTER = NumberFormat.getNumberInstance(Locale.US);

	private static final ImmutableSet<String> PET_MESSAGES = ImmutableSet.of("You have a funny feeling like you're being followed.",
		"You feel something weird sneaking into your backpack.",
		"You have a funny feeling like you would have been followed...");

	private static final int NMZ_MAP_REGION = 9033;

	public static final String SESSION_NAME = "Current Session Data";

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

	@Inject
	private PluginManager pluginManager;

	private LootLoggerPanel panel;
	private NavigationButton navButton;

	@Getter
	private SetMultimap<LootRecordType, String> lootNames = HashMultimap.create();

	private boolean prepared = false;
	private boolean unsiredReclaiming = false;
	private int unsiredCheckCount = 0;
	// Some pets aren't handled (skilling pets) so reset gotPet after a few ticks
	private int petTicks = 0;
	private boolean gotPet = false;

	private final Map<String, Integer> killCountMap = new HashMap<>();
	private final LinkedListMultimap<String, LTRecord> sessionData = LinkedListMultimap.create();

	@Provides
	LootLoggerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(LootLoggerConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		panel = new LootLoggerPanel(itemManager, this);

		final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "panel-icon.png");

		navButton = NavigationButton.builder()
			.tooltip("Loot Logger")
			.icon(icon)
			.priority(6)
			.panel(panel)
			.build();

		if (config.enableUI())
		{
			clientToolbar.addNavigation(navButton);
		}

		// Attach necessary info from item manager on load, probably a better method
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
				return true;
			});
		}

		if (client.getGameState().equals(GameState.LOGGED_IN) || client.getGameState().equals(GameState.LOADING))
		{
			updateWriterUsername();
		}

		final Optional<Plugin> mainPlugin = pluginManager.getPlugins().stream().filter(p -> p.getName().equals("Loot Tracker")).findFirst();
		if (mainPlugin.isPresent() && !pluginManager.isPluginEnabled(mainPlugin.get()))
		{
			pluginManager.setPluginEnabled(mainPlugin.get(), true);
		}
	}

	@Override
	protected void shutDown()
	{
		if (config.enableUI())
		{
			clientToolbar.removeNavigation(navButton);
		}

		gotPet = false;
		petTicks = 0;
		writer.setName(null);
	}

	@Subscribe
	public void onConfigChanged(final ConfigChanged event)
	{
		if (event.getGroup().equals("lootlogger"))
		{
			if (event.getKey().equals("enableUI"))
			{
				if (config.enableUI())
				{
					clientToolbar.addNavigation(navButton);
				}
				else
				{
					clientToolbar.removeNavigation(navButton);
				}
			}

			if (config.enableUI())
			{
				SwingUtilities.invokeLater(panel::refreshUI);
			}
		}
	}

	@Subscribe
	public void onGameStateChanged(final GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGGED_IN)
		{
			updateWriterUsername();
		}
	}

	private void updateWriterUsername()
	{
		// Check if we're already using this user as we are now updating the username on `LOGGED_IN` instead of `LOGGING_IN`
		// `LOGGED_IN` will be triggered after every `LOADING` state which happens much more frequently
		String folder = String.valueOf(client.getAccountHash());
		RuneScapeProfileType profileType = RuneScapeProfileType.getCurrent(client);
		if (profileType != RuneScapeProfileType.STANDARD)
		{
			folder += "-" + Text.titleCase(profileType);
		}

		if (folder.equalsIgnoreCase(writer.getName()))
		{
			return;
		}

		// If we aren't first attempt to migrate from the deprecated login name to the account hash
		String name = client.getUsername();
		if (name != null && name.length() > 0 && client.getAccountHash() != -1)
		{
			// We are using the accountHash as RL doesn't return a login name when ran through the Jagex launcher
			writer.renameUsernameFolderToAccountHash(name.toLowerCase(), client.getAccountHash());
		}

		if (writer.setPlayerUsername(folder))
		{
			localPlayerNameChanged();
		}
	}

	private void localPlayerNameChanged()
	{
		lootNames = writer.getKnownFileNames();
		if (config.enableUI())
		{
			SwingUtilities.invokeLater(panel::showSelectionView);
		}
	}

	private Collection<LTItemEntry> convertToLTItemEntries(Collection<ItemStack> stacks)
	{
		return stacks.stream().map(i -> createLTItemEntry(i.getId(), i.getQuantity())).collect(Collectors.toList());
	}

	private LTItemEntry createLTItemEntry(final int id, final int qty)
	{
		final ItemComposition c = itemManager.getItemComposition(id);
		final int realId = c.getNote() == -1 ? c.getId() : c.getLinkedNoteId();
		final int price = itemManager.getItemPrice(realId);
		return new LTItemEntry(c.getName(), id, qty, price);
	}

	private void addRecord(final LTRecord record)
	{
		writer.addLootTrackerRecord(record);
		lootNames.put(record.getType(), record.getName().toLowerCase());
		sessionData.put(record.getName().toLowerCase(), record);
		if (config.enableUI())
		{
			SwingUtilities.invokeLater(() -> panel.addLog(record));
		}
	}

	@Subscribe
	public void onLootReceived(final LootReceived event)
	{
		if (isInNightmareZone() && config.ignoreNmz())
		{
			return;
		}

		final Collection<LTItemEntry> drops = convertToLTItemEntries(event.getItems());

		if (gotPet)
		{
			final Pet p = Pet.getByBossName(event.getName());
			if (p != null)
			{
				gotPet = false;
				petTicks = 0;
				drops.add(createLTItemEntry(p.getPetID(), 1));
			}
		}

		int kc = killCountMap.getOrDefault(event.getName().toUpperCase(), -1);

		// Check aliases if they exist for kill counts
		BossTab tab = BossTab.getByName(event.getName());
		if (kc == -1 && tab != null && tab.getAliases().size() > 0) {
			for (final String alias : tab.getAliases()) {
				kc = killCountMap.getOrDefault(alias.toUpperCase(), -1);
				if (kc > 0) {
					break;
				}
			}
		}

		final LTRecord record = new LTRecord(event.getName(), event.getCombatLevel(), kc, event.getType(), drops, new Date());
		addRecord(record);
	}

	public Collection<LTRecord> getDataByName(LootRecordType type, String name)
	{
		final BossTab tab = BossTab.getByName(name);
		if (tab == null)
		{
			return writer.loadLootTrackerRecords(type, name);
		}

		final Collection<LTRecord> records = new ArrayList<>();
		for (final String alias : tab.getAliases()) {
			records.addAll(writer.loadLootTrackerRecords(type, alias));
		}

		return records;
	}

	/**
	 * Creates a loot log for this name and then attaches it to the UI when finished
	 * @param name record name
	 */
	public void requestLootLog(final LootRecordType type, final String name)
	{
		clientThread.invoke(() ->
		{
			if (name.equalsIgnoreCase(SESSION_NAME))
			{
				final LootLog log = new LootLog(Collections.emptyList(), name);
				for (final String key : sessionData.keySet())
				{
					log.getMinionLogs().add(new LootLog(sessionData.get(key), key));
				}

				SwingUtilities.invokeLater(() -> panel.useLog(log));
				return;
			}
			
			final Collection<LTRecord> records = getDataByName(type, name);
			final LootLog log = new LootLog(records, name);
			if (log.getType().equals(LootRecordType.UNKNOWN))
			{
				log.setType(type);
			}

			final BossTab tab = BossTab.getByName(name);
			if (tab != null && tab.getMinions().size() > 0)
			{
				for (final String minion : tab.getMinions())
				{
					final Collection<LTRecord> minionRecords = getDataByName(tab.getMinionType(), minion);
					final LootLog minionLog = new LootLog(minionRecords, minion);

					log.getMinionLogs().add(minionLog);
				}
			}

			SwingUtilities.invokeLater(() -> panel.useLog(log));
		});
	}

	public boolean clearStoredDataByName(final LootRecordType type, final String name)
	{
		if (name.equalsIgnoreCase(SESSION_NAME))
		{
			sessionData.clear();
			return true;
		}

		if (panel.getLootLog().getName().equalsIgnoreCase(SESSION_NAME))
		{
			sessionData.removeAll(name.toLowerCase());
			return true;
		}

		lootNames.remove(type, name);
		return writer.deleteLootTrackerRecords(type, name);
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event)
	{
		if (event.getGroupId() != InterfaceID.DIALOG_SPRITE)
		{
			return;
		}

		Widget text = client.getWidget(ComponentID.DIALOG_SPRITE_TEXT);
		if (text != null && text.getText().toLowerCase().contains(SIRE_FONT_TEXT))
		{
			unsiredCheckCount = 0;
			unsiredReclaiming = true;
		}
	}

	@Subscribe
	public void onGameTick(GameTick t)
	{
		if (gotPet)
		{
			if (petTicks > MAX_PET_TICKS)
			{
				gotPet = false;
				petTicks = 0;
			}
			else
			{
				petTicks++;
			}
		}

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
		final Widget text = client.getWidget(ComponentID.DIALOG_SPRITE_TEXT);
		// Reclaimed an item?
		if (text != null && text.getText().toLowerCase().contains(SIRE_REWARD_TEXT))
		{
			final Widget sprite = client.getWidget(InterfaceID.DIALOG_SPRITE);
			if (sprite == null || sprite.getItemId() == -1)
			{
				return false;
			}

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
			Collection<LTRecord> data = getDataByName(LootRecordType.NPC, BossTab.ABYSSAL_SIRE.getName());
			ItemComposition c = itemManager.getItemComposition(itemID);
			LTItemEntry itemEntry = new LTItemEntry(c.getName(), itemID, 1, 0);

			log.debug("Received Unsired item: {}", c.getName());

			// Don't have data for sire, create a new record with just this data.
			if (data == null)
			{
				log.debug("No previous Abyssal sire loot, creating new loot record");
				LTRecord r = new LTRecord(BossTab.ABYSSAL_SIRE.getName(), 350, -1, LootRecordType.NPC, Collections.singletonList(itemEntry), new Date());
				addRecord(r);
				return;
			}

			log.debug("Adding drop to last abyssal sire loot record");
			// Add data to last kill count
			final List<LTRecord> items = new ArrayList<>(data);
			final LTRecord r = items.get(items.size() - 1);
			r.addDropEntry(itemEntry);
			writer.writeLootTrackerFile(BossTab.ABYSSAL_SIRE.getName(), items);
			if (config.enableUI())
			{
				SwingUtilities.invokeLater(panel::refreshUI);
			}
		});
	}

	public int convertToInt(String s)
	{
		try {
			return NUMBER_FORMATTER.parse(s).intValue();
		}
		catch (ParseException e)
		{
			return -1;
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (event.getType() != ChatMessageType.GAMEMESSAGE && event.getType() != ChatMessageType.SPAM)
		{
			return;
		}

		final String chatMessage = Text.removeTags(event.getMessage());

		if (PET_MESSAGES.contains(chatMessage))
		{
			gotPet = true;
		}

		// Check if message is for a clue scroll reward
		final Matcher m = CLUE_SCROLL_PATTERN.matcher(chatMessage);
		if (m.find())
		{
			final String eventType;
			switch (m.group(2).toLowerCase())
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

			final int killCount = convertToInt(m.group(1));
			killCountMap.put(eventType.toUpperCase(), killCount);
			return;
		}

		// Barrows KC
		if (chatMessage.startsWith("Your Barrows chest count is"))
		{
			Matcher n = NUMBER_PATTERN.matcher(chatMessage);
			if (n.find())
			{
				killCountMap.put("BARROWS", convertToInt(n.group()));
				return;
			}
		}

		// Raids KC
		if (chatMessage.startsWith("Your completed Chambers of Xeric count is"))
		{
			Matcher n = NUMBER_PATTERN.matcher(chatMessage);
			if (n.find())
			{
				killCountMap.put("CHAMBERS OF XERIC", convertToInt(n.group()));
				return;
			}
		}
		
		// Tob KC
		if (chatMessage.startsWith("Your completed Theatre of Blood count is"))
		{
			Matcher n = NUMBER_PATTERN.matcher(chatMessage);
			if (n.find())
			{
				killCountMap.put("THEATRE OF BLOOD", convertToInt(n.group()));
				return;
			}
		}

		// Handle the other bosses
		final Matcher boss = BOSS_NAME_NUMBER_PATTERN.matcher(chatMessage);
		if (boss.find())
		{
			final String bossName = boss.group(1);
			final String actualBossName = KillCountNameMapper.getBossNameFromKillCountName(bossName);
			final int killCount = convertToInt(boss.group(2));
			killCountMap.put(actualBossName.toUpperCase(), killCount);
		}
	}

	/**
	 * Is the player inside the NMZ arena?
	 */
	private boolean isInNightmareZone()
	{
		if (client.getLocalPlayer() == null) {
			return false;
		}

		// It seems that KBD shares the map region with NMZ but NMZ is never in plane 0.
		return ArrayUtils.contains(client.getMapRegions(), NMZ_MAP_REGION) && client.getLocalPlayer().getWorldLocation().getPlane() > 0;
	}
}
