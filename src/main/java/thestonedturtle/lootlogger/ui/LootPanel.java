/*
 * Copyright (c) 2018, TheStonedTurtle <https://github.com/TheStonedTurtle>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package thestonedturtle.lootlogger.ui;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.DynamicGridLayout;
import net.runelite.http.api.loottracker.LootRecordType;
import thestonedturtle.lootlogger.ItemSortTypes;
import thestonedturtle.lootlogger.LootLoggerConfig;
import thestonedturtle.lootlogger.LootLoggerPlugin;
import thestonedturtle.lootlogger.UniqueItemPlacement;
import thestonedturtle.lootlogger.data.LootLog;
import thestonedturtle.lootlogger.data.UniqueItem;
import thestonedturtle.lootlogger.localstorage.LTItemEntry;
import thestonedturtle.lootlogger.localstorage.LTRecord;

@Slf4j
class LootPanel extends JPanel
{
	private static final String CURRENT_KC = "Current Killcount:";
	private static final String KILLS_LOGGED = "Kills Logged:";
	private static final String TOTAL_VALUE = "Total Value:";
	private static final String TOTAL_KILLS = "Total Kills:";

	private LootLog lootLog;
	private LootLog tempLog;
	private final LootLoggerConfig config;
	private final ItemManager itemManager;

	private boolean playbackPlaying = false;
	private boolean cancelPlayback = false;

	private final BiConsumer<LootRecordType, String> clearData;
	private final LootGrid lootGrid = new LootGrid();
	private final TextPanel currentKillcountPanel = new TextPanel();
	private final TextPanel killsLoggedPanel = new TextPanel();
	private final TextPanel totalValuePanel = new TextPanel();

	@Getter
	private final Map<String, NamedLootGrid> minionGridMap = new HashMap<>();
	private final Map<Integer, UniqueItemPanel> uniqueItemPanelMap = new HashMap<>();

	LootPanel(
		final LootLog log,
		final LootLoggerConfig config,
		final ItemManager itemManager,
		final BiConsumer<LootRecordType, String> clearData)
	{
		this.lootLog = log;
		this.config = config;
		this.itemManager = itemManager;
		this.clearData = clearData;

		setLayout(new DynamicGridLayout(0, 1, 0, 4));
		setBorder(new EmptyBorder(0, 10, 0, 10));
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		createPanel(log);
	}

	private void createPanel(final LootLog lootLog)
	{
		// Reset panel state
		currentKillcountPanel.setVisible(false);
		killsLoggedPanel.setVisible(false);
		totalValuePanel.setVisible(false);
		lootGrid.setVisible(false);
		minionGridMap.clear();
		uniqueItemPanelMap.clear();
		removeAll();

		add(currentKillcountPanel);
		add(killsLoggedPanel);
		add(totalValuePanel);
		add(lootGrid);

		// Create uniques panel
		if (!config.uniquesPlacement().equals(UniqueItemPlacement.ITEM_BREAKDOWN))
		{
			LootLog.recalculateUniques(lootLog, config.includeMinions());

			int idx = 0;
			for (final int position : lootLog.getUniquePositionMap().keySet())
			{
				final Collection<UniqueItem> uniques = lootLog.getUniquePositionMap().get(position);

				final UniqueItemPanel p = new UniqueItemPanel(uniques, this.itemManager, this.config.itemMissingAlpha());
				uniqueItemPanelMap.put(position, p);
				this.add(p, idx);
				idx++;
			}
		}

		int killsLogged = lootLog.getRecords().size();
		if (killsLogged > 0)
		{
			killsLoggedPanel.updatePanel(KILLS_LOGGED, lootLog.getRecords().size());
			killsLoggedPanel.setVisible(true);

			final LTRecord entry = lootLog.getRecords().get(lootLog.getRecords().size() - 1);
			if (entry.getKillCount() != -1)
			{
				currentKillcountPanel.updatePanel(CURRENT_KC, entry.getKillCount());
				currentKillcountPanel.setVisible(true);
			}
		}

		// Include Main Loot
		updateMainLootGrid();

		// Store Total Value
		long totalValue = lootLog.getLootValue(false);

		// Include Minion Loot
		if (config.includeMinions())
		{
			for (final LootLog log : lootLog.getMinionLogs())
			{
				if (log.getRecords().size() == 0)
				{
					continue;
				}
				killsLogged += log.getRecords().size();

				final NamedLootGrid namedGrid = createMinionGrid(log);
				this.add(namedGrid);
				minionGridMap.put(log.getName().toLowerCase(), namedGrid);

				totalValue += namedGrid.getPrice();
			}
		}

		if (totalValue > 0)
		{
			totalValuePanel.updatePanel(TOTAL_VALUE, totalValue);
			totalValuePanel.setVisible(true);
		}

		// Change text and include minion kills for session data
		if (lootLog.getName().equalsIgnoreCase(LootLoggerPlugin.SESSION_NAME))
		{
			killsLoggedPanel.updatePanel(TOTAL_KILLS, killsLogged);
			killsLoggedPanel.setVisible(killsLogged > 0);
		}
	}

	private LTItemEntry[] getItemsToDisplay(final LootLog log)
	{
		final boolean hideUniques = config.uniquesPlacement().equals(UniqueItemPlacement.UNIQUES_PANEL);

		return log.getConsolidated()
			.values().stream()
			.filter(e -> !(hideUniques && lootLog.getUniqueIds().contains(e.getId())))
			.sorted(createLTItemEntryComparator(config.itemSortType()))
			.toArray(LTItemEntry[]::new);
	}

	private void updateMainLootGrid()
	{
		final LTItemEntry[] itemsToDisplay = getItemsToDisplay(lootLog);
		if (itemsToDisplay.length > 0)
		{
			lootGrid.updateGrid(itemsToDisplay, itemManager);
			lootGrid.setVisible(true);
		}
	}

	private NamedLootGrid createMinionGrid(final LootLog log)
	{
		final LTItemEntry[] itemsToDisplay = getItemsToDisplay(log);
		final LootGrid grid = new LootGrid(itemsToDisplay, itemManager);

		final long logValue = log.getLootValue(false);
		return new NamedLootGrid(log.getName(), log.getRecords().size(), logValue, grid, log.getType(), clearData);
	}

	private void updateMinionLog(final LootLog log)
	{
		final NamedLootGrid grid = minionGridMap.get(log.getName().toLowerCase());
		final LTItemEntry[] itemsToDisplay = getItemsToDisplay(log);

		grid.updateGrid(log, itemsToDisplay, itemManager);
	}

	void addedRecord(final LTRecord record)
	{
		if (playbackPlaying)
		{
			tempLog.addRecord(record);
			return;
		}

		lootLog.addRecord(record);
		refreshPanel(false);
	}

	void addMinionRecord(final LTRecord record)
	{
		final LootLog useLog = playbackPlaying ? tempLog : lootLog;

		final LootLog minionLog = useLog.getMinionLog(record.getName());
		if (minionLog == null)
		{
			// Add non-existent minion only if viewing session data
			if (useLog.getName().equalsIgnoreCase(LootLoggerPlugin.SESSION_NAME))
			{
				final LootLog newMinionLog = new LootLog(ImmutableList.of(record), record.getName());
				final NamedLootGrid grid = createMinionGrid(newMinionLog);
				this.add(grid);
				minionGridMap.put(newMinionLog.getName().toLowerCase(), grid);
				useLog.getMinionLogs().add(newMinionLog);

				// Refresh doesn't work for session data but just in case it is updated to work with minions in the future we should not refresh
				if (!playbackPlaying)
				{
					refreshPanel(true);
				}
			}
			return;
		}

		minionLog.addRecord(record);
		updateMinionLog(minionLog);

		if (!playbackPlaying)
		{
			refreshPanel(true);
		}
	}

	// Recalculate uniques and update the item panels if necessary
	private void refreshUniques()
	{
		LootLog.recalculateUniques(lootLog, config.includeMinions());
		for (final Map.Entry<Integer, UniqueItemPanel> entry : uniqueItemPanelMap.entrySet())
		{
			final Collection<UniqueItem> uniques = lootLog.getUniquePositionMap().get(entry.getKey());
			entry.getValue().updatePanel(uniques, itemManager, config.itemMissingAlpha());
		}
	}

	public void refreshPanel(final boolean minionUpdate)
	{
		refreshUniques();

		if (!minionUpdate)
		{
			updateMainLootGrid();

			// Update KillCount
			if (lootLog.getRecords().size() > 0)
			{
				final LTRecord entry = lootLog.getRecords().get(lootLog.getRecords().size() - 1);
				currentKillcountPanel.updatePanel(CURRENT_KC, entry.getKillCount());
				currentKillcountPanel.setVisible(entry.getKillCount() != -1);
			}
		}

		// Update Total Value
		final long totalValue = lootLog.getLootValue(config.includeMinions());
		totalValuePanel.updatePanel(TOTAL_VALUE, totalValue);
		totalValuePanel.setVisible(totalValue > 0);

		// Update Kills Logged
		int killsLogged = lootLog.getRecords().size();
		if (config.includeMinions())
		{
			killsLogged += lootLog.getMinionLogs()
				.stream()
				.mapToInt(l -> l.getRecords().size())
				.sum();
		}

		final String killsLoggedText = lootLog.getName().equalsIgnoreCase(LootLoggerPlugin.SESSION_NAME) ? TOTAL_KILLS : KILLS_LOGGED;
		killsLoggedPanel.updatePanel(killsLoggedText, killsLogged);
		killsLoggedPanel.setVisible(killsLogged > 0);

		revalidate();
		repaint();
	}

	void playback()
	{
		if (playbackPlaying)
		{
			cancelPlayback = true;
			return;
		}

		playbackPlaying = true;

		if (lootLog.getRecords().size() > 0)
		{
			tempLog = lootLog;

			lootLog = new LootLog(new ArrayList<>(), lootLog.getName());
			for (final LTRecord r : tempLog.getRecords())
			{
				lootLog.addRecord(r);

				if (lootLog.getRecords().size() == 1)
				{
					SwingUtilities.invokeLater(() -> createPanel(lootLog));
				}
				else if (lootLog.getRecords().size() == tempLog.getRecords().size())
				{
					cancelPlayback = true;
				}
				else
				{
					SwingUtilities.invokeLater(() -> refreshPanel(false));
				}

				try
				{
					if (cancelPlayback)
					{
						playbackPlaying = false;
						cancelPlayback = false;
						lootLog = tempLog;
						SwingUtilities.invokeLater(() -> createPanel(lootLog));
						break;
					}

					// TODO: Allow this rate to be configurable?
					Thread.sleep(250);
				}
				catch (InterruptedException e)
				{
					log.warn(e.getMessage());
				}
			}
		}

		playbackPlaying = false;
	}

	/**
	 * Sorts the collection of LTItemEntry based on the selected {@link ItemSortTypes}
	 * @param sortType The {@link ItemSortTypes} describing how these entries should be sorted
	 * @return returns the sorted list
	 */
	private static Comparator<LTItemEntry> createLTItemEntryComparator(final ItemSortTypes sortType)
	{
		return (o1, o2) ->
		{
			switch (sortType)
			{
				case ITEM_ID:
					return o1.getId() - o2.getId();
				case PRICE:
					if (o1.getPrice() != o2.getPrice())
					{
						return o1.getPrice() > o2.getPrice() ? -1 : 1;
					}
					break;
				case VALUE:
					if (o1.getTotal() != o2.getTotal())
					{
						return o1.getTotal() > o2.getTotal() ? -1 : 1;
					}
					break;
				case ALPHABETICAL:
					// Handled below
					break;
				default:
					log.warn("Sort Type not being handled correctly, defaulting to alphabetical.");
					break;
			}

			// Default to alphabetical
			return o1.getName().compareTo(o2.getName());
		};
	}
}
