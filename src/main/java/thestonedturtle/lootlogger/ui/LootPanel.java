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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
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
import net.runelite.client.util.QuantityFormatter;
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

	private final LootLog lootLog;
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
	private final GridBagConstraints gridBagConstraints = new GridBagConstraints();

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

		setLayout(new GridBagLayout());
		setBorder(new EmptyBorder(0, 10, 5, 10));
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = 1;
		gridBagConstraints.insets = new Insets(0, 0, 4, 0);
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;

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
		gridBagConstraints.gridy = 0;

		// Create uniques panel
		if (!config.uniquesPlacement().equals(UniqueItemPlacement.ITEM_BREAKDOWN))
		{
			LootLog.recalculateUniques(lootLog, config.includeMinions());

			for (final int position : lootLog.getUniquePositionMap().keySet())
			{
				final Collection<UniqueItem> uniques = lootLog.getUniquePositionMap().get(position);

				final UniqueItemPanel p = new UniqueItemPanel(uniques, this.itemManager, this.config.itemMissingAlpha());
				uniqueItemPanelMap.put(position, p);
				add(p, gridBagConstraints);
				gridBagConstraints.gridy++;
			}
		}

		add(currentKillcountPanel, gridBagConstraints);
		gridBagConstraints.gridy++;

		add(killsLoggedPanel, gridBagConstraints);
		gridBagConstraints.gridy++;

		add(totalValuePanel, gridBagConstraints);
		gridBagConstraints.gridy++;

		add(lootGrid, gridBagConstraints);
		gridBagConstraints.gridy++;

		int killsLogged = lootLog.getRecords().size();
		if (killsLogged > 0)
		{
			killsLoggedPanel.updatePanel(KILLS_LOGGED, killsLogged);
			killsLoggedPanel.setVisible(true);

			final LTRecord entry = lootLog.getRecords().get(killsLogged - 1);
			if (entry.getKillCount() != -1)
			{
				currentKillcountPanel.updatePanel(CURRENT_KC, entry.getKillCount());
				currentKillcountPanel.setVisible(true);
			}
		}

		// Include Main Loot
		updateMainLootGrid(lootLog);

		// Store Total Value
		long totalValue = lootLog.getLootValue(false);

		// Include Minion Loot
		if (config.includeMinions())
		{
			for (final LootLog log : lootLog.getMinionLogs())
			{
				if (log.getRecords().isEmpty())
				{
					continue;
				}
				killsLogged += log.getRecords().size();

				final NamedLootGrid namedGrid = createMinionGrid(log);
				minionGridMap.put(log.getName().toLowerCase(), namedGrid);
				add(namedGrid, gridBagConstraints);
				gridBagConstraints.gridy++;

				totalValue += namedGrid.getPrice();
			}
		}

		if (totalValue > 0)
		{
			totalValuePanel.updatePanel(TOTAL_VALUE, totalValue);
			totalValuePanel.setVisible(true);
			killsLoggedPanel.setToolTipText(QuantityFormatter.formatNumber(totalValue / killsLogged) + " gp per kill");
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

	private void updateMainLootGrid(final LootLog lootLog)
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
		if (grid == null)
		{
			return;
		}

		final LTItemEntry[] itemsToDisplay = getItemsToDisplay(log);

		grid.updateGrid(log, itemsToDisplay, itemManager);
	}

	void addedRecord(final LTRecord record)
	{
		lootLog.addRecord(record);
		if (!playbackPlaying)
		{
			refreshPanel(lootLog, false);
		}
	}

	void addMinionRecord(final LTRecord record)
	{
		final LootLog minionLog = lootLog.getMinionLog(record.getName());
		if (minionLog == null)
		{
			final LootLog newMinionLog = new LootLog(ImmutableList.of(record), record.getName());
			lootLog.getMinionLogs().add(newMinionLog);

			final NamedLootGrid grid = createMinionGrid(newMinionLog);
			minionGridMap.put(newMinionLog.getName().toLowerCase(), grid);
			add(grid, gridBagConstraints);
			gridBagConstraints.gridy++;

			if (!playbackPlaying)
			{
				refreshPanel(lootLog, true);
			}
			return;
		}

		minionLog.addRecord(record);
		updateMinionLog(minionLog);

		if (!playbackPlaying)
		{
			refreshPanel(lootLog, true);
		}
	}

	public void refreshPanel(final LootLog lootLog, final boolean minionUpdate)
	{
		// Refresh Uniques panel data. Config option would prevent uniqueItemPanelMap from having anything.
		LootLog.recalculateUniques(lootLog, config.includeMinions());
		for (final Map.Entry<Integer, UniqueItemPanel> entry : uniqueItemPanelMap.entrySet())
		{
			final Collection<UniqueItem> uniques = lootLog.getUniquePositionMap().get(entry.getKey());
			entry.getValue().updatePanel(uniques, itemManager, config.itemMissingAlpha());
		}

		if (!minionUpdate)
		{
			updateMainLootGrid(lootLog);

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
		if (lootLog.getName().equalsIgnoreCase(LootLoggerPlugin.SESSION_NAME))
		{
			killsLogged += lootLog.getMinionLogs()
				.stream()
				.mapToInt(l -> l.getRecords().size())
				.sum();
		}

		final String killsLoggedText = lootLog.getName().equalsIgnoreCase(LootLoggerPlugin.SESSION_NAME) ? TOTAL_KILLS : KILLS_LOGGED;
		killsLoggedPanel.updatePanel(killsLoggedText, killsLogged);
		killsLoggedPanel.setVisible(killsLogged > 0);
	}

	void playback()
	{
		if (playbackPlaying)
		{
			cancelPlayback = true;
			return;
		}

		playbackPlaying = true;

		// Grab these outside the loop to avoid any potential performance issues with pulling from config
		// Downside is changing these config options during a replay will not have any affect.
		final int playbackLimit = config.playbackUpdateLimit();
		final int uniquePauseDuration = config.uniquePauseDuration();

		final int configuredSleepDuration = 1000 / playbackLimit;
		final int actualUniquePauseDuration = Math.max(configuredSleepDuration, uniquePauseDuration);

		if (!lootLog.getRecords().isEmpty())
		{
			final int totalKills = lootLog.getRecords().size();
			final LootLog tempLog = new LootLog(new ArrayList<>(), lootLog.getName());
			for (final LTRecord r : lootLog.getRecords())
			{
				tempLog.addRecord(r);

				if (tempLog.getRecords().size() == 1)
				{
					SwingUtilities.invokeLater(() -> createPanel(tempLog));
				}
				else if (tempLog.getRecords().size() == totalKills)
				{
					cancelPlayback = true;
				}
				else
				{
					SwingUtilities.invokeLater(() -> refreshPanel(tempLog, false));
				}

				try
				{
					if (cancelPlayback)
					{
						playbackPlaying = false;
						cancelPlayback = false;
						SwingUtilities.invokeLater(() -> createPanel(lootLog));
						break;
					}

					int sleepDuration = configuredSleepDuration;
					if (uniquePauseDuration > 0)
					{
						for (LTItemEntry e : r.getDrops())
						{
							if (lootLog.getUniqueIds().contains(e.getId()))
							{
								// Pause for each unique
								sleepDuration = actualUniquePauseDuration;
								break;
							}
						}
					}
					Thread.sleep(sleepDuration);
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
