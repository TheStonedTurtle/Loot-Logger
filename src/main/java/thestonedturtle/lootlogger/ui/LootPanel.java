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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;
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
	private final LootLog lootLog;
	private final LootLoggerConfig config;
	private final ItemManager itemManager;

	private boolean playbackPlaying = false;
	private boolean cancelPlayback = false;

	private final BiConsumer<LootRecordType, String> clearData;

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
		setBorder(new EmptyBorder(0, 10, 0, 10));
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		createPanel(log);
	}

	private void createPanel(final LootLog lootLog)
	{
		final GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.gridx = 0;
		c.gridy = 0;

		// Create necessary helpers for the unique toggles
		final Multimap<Integer, UniqueItem> positionMap = ArrayListMultimap.create();
		final Set<Integer> uniqueIds = new HashSet<>();

		final Collection<LootLog> allLogs = new ArrayList<>();
		allLogs.add(lootLog);
		if (config.includeMinions()) {
			allLogs.addAll(lootLog.getMinionLogs());
		}

		if (!config.uniquesPlacement().equals(UniqueItemPlacement.ITEM_BREAKDOWN))
		{
			// Loop over all UniqueItems and check how many the player has received as a drop for each
			// Also add all Item IDs for uniques to a Set for easy hiding later on.
			for (final UniqueItem item : lootLog.getUniques())
			{
				final List<Integer> ids = Arrays.stream(item.getAlternativeIds()).boxed().collect(Collectors.toList());
				ids.add(item.getItemID());
				ids.add(item.getLinkedID());

				int qty = 0;
				for (final int id : ids)
				{
					uniqueIds.add(id);

					for (final LootLog log : allLogs)
					{
						final LTItemEntry entry = log.getConsolidated().get(id);
						qty += (entry == null ? 0 : entry.getQuantity());
					}
				}

				item.setQty(qty);
				positionMap.put(item.getPosition(), item);
			}

			for (final int position : positionMap.keySet())
			{
				final Collection<UniqueItem> uniques = positionMap.get(position);

				final UniqueItemPanel p = new UniqueItemPanel(uniques, this.itemManager, this.config.itemMissingAlpha());
				this.add(p, c);
				c.gridy++;
			}
		}

		// Attach Kill Count Panel(s)
		if (lootLog.getRecords().size() > 0)
		{
			final int amount = lootLog.getRecords().size();
			final LTRecord entry = lootLog.getRecords().get(amount - 1);
			if (entry.getKillCount() != -1)
			{
				final TextPanel p = new TextPanel("Current Killcount:", entry.getKillCount());
				this.add(p, c);
				c.gridy++;
			}
			final TextPanel p2 = new TextPanel("Kills Logged:", amount);
			this.add(p2, c);
			c.gridy++;
		}

		final int killsLoggedGridY = c.gridy;
		if (lootLog.getName().equalsIgnoreCase(LootLoggerPlugin.SESSION_NAME))
		{
			c.gridy++;
		}

		// Only add the total value element if it has something useful to display
		long totalValue = lootLog.getConsolidated().values().stream().mapToLong(e -> e.getPrice() * e.getQuantity()).sum();
		final int totalGridY = c.gridy;
		c.gridy++;

		final boolean hideUniques = config.uniquesPlacement().equals(UniqueItemPlacement.UNIQUES_PANEL);
		final Comparator<LTItemEntry> sorter = createLTItemEntryComparator(config.itemSortType());
		final Collection<LTItemEntry> itemsToDisplay = lootLog.getConsolidated().values().stream()
			.filter(e -> !(hideUniques && uniqueIds.contains(e.getId())))
			.sorted(sorter)
			.collect(Collectors.toList());

		if (itemsToDisplay.size() > 0)
		{
			if (config.itemBreakdown())
			{
				for (final LTItemEntry e : itemsToDisplay)
				{
					final ItemPanel p = new ItemPanel(e, itemManager);
					this.add(p, c);
					c.gridy++;
				}
			}
			else
			{
				final LootGrid grid = new LootGrid(itemsToDisplay.toArray(new LTItemEntry[0]), itemManager);
				this.add(grid, c);
				c.gridy++;
			}
		}

		// Add all minions
		int killsLogged = 0;
		if (config.includeMinions() && lootLog.getMinionLogs().size() > 0)
		{
			for (final LootLog log : lootLog.getMinionLogs())
			{
				if (log.getRecords().size() == 0)
				{
					continue;
				}
				killsLogged += log.getRecords().size();

				final LTItemEntry[] logItemsToDisplay = log.getConsolidated().values().stream()
					.filter(e -> !(hideUniques && uniqueIds.contains(e.getId())))
					.sorted(sorter)
					.toArray(LTItemEntry[]::new);

				final LootGrid grid = new LootGrid(logItemsToDisplay, itemManager);

				final long logValue = log.getConsolidated().values().stream().mapToLong(e -> e.getPrice() * e.getQuantity()).sum();
				this.add(new NamedLootGrid(log.getName(), log.getRecords().size(), logValue, grid, log.getType(), clearData), c);
				c.gridy++;

				totalValue += logValue;
			}
		}

		if (totalValue > 0)
		{
			int tempGridY = c.gridy;
			c.gridy = totalGridY;

			final TextPanel totalPanel = new TextPanel("Total Value:", totalValue);
			this.add(totalPanel, c);

			c.gridy = tempGridY;
		}

		// Combine all kills for session data
		if (lootLog.getName().equalsIgnoreCase(LootLoggerPlugin.SESSION_NAME))
		{
			int tempGridY = c.gridy;
			c.gridy = killsLoggedGridY;

			final TextPanel totalKills = new TextPanel("Total Kills:", killsLogged);
			this.add(totalKills, c);

			c.gridy = tempGridY;
		}
	}

	void addedRecord(final LTRecord record)
	{
		lootLog.addRecord(record);
		refreshPanel();
	}

	void addMinionRecord(final LTRecord record)
	{
		final LootLog minionLog = lootLog.getMinionLog(record.getName());
		if (minionLog == null)
		{
			// Add minion only if viewing session data
			if (lootLog.getName().equalsIgnoreCase(LootLoggerPlugin.SESSION_NAME))
			{
				lootLog.getMinionLogs().add(new LootLog(ImmutableList.of(record), record.getName()));
				refreshPanel();
			}
			return;
		}

		minionLog.addRecord(record);
		refreshPanel();
	}

	public void refreshPanel()
	{
		// TODO: Smarter update system so it only repaints necessary Item and Text Panels
		this.removeAll();
		this.createPanel(lootLog);

		this.revalidate();
		this.repaint();
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
			final Collection<LTRecord> recs = new ArrayList<>();
			for (final LTRecord r : lootLog.getRecords())
			{
				recs.add(r);

				final LootLog log;
				if (recs.size() != lootLog.getRecords().size())
				{
					log = new LootLog(recs, lootLog.getName());
				}
				else
				{
					log = lootLog;
				}

				SwingUtilities.invokeLater(() -> refreshPlayback(log));
				try
				{
					if (cancelPlayback)
					{
						playbackPlaying = false;
						cancelPlayback = false;
						SwingUtilities.invokeLater(() -> refreshPlayback(lootLog));
						break;
					}

					// TODO: Allow this rate to be configurable?
					Thread.sleep(250);
				}
				catch (InterruptedException e)
				{
					System.out.println(e.getMessage());
				}
			}
		}

		playbackPlaying = false;
	}

	private void refreshPlayback(final LootLog log)
	{
		this.removeAll();

		this.createPanel(log);

		this.revalidate();
		this.repaint();
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
