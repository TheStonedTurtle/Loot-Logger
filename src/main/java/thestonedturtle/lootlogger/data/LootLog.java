/*
 * Copyright (c) 2019, TheStonedTurtle <https://github.com/TheStonedTurtle>
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
package thestonedturtle.lootlogger.data;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.gameval.ItemID;
import net.runelite.http.api.loottracker.LootRecordType;
import thestonedturtle.lootlogger.localstorage.LTItemEntry;
import thestonedturtle.lootlogger.localstorage.LTRecord;

@Getter
public class LootLog
{
	private static final Pattern CLUE_ITEM_TYPE_PATTERN = Pattern.compile("\\((\\w*)\\)");
	private final String name;
	@Setter
	private LootRecordType type;
	// Store all records in case rewrite needs to happen
	private final List<LTRecord> records = new ArrayList<>();
	private final Map<Integer, LTItemEntry> consolidated = new HashMap<>();
	private final Collection<UniqueItem> uniques;

	private final Set<Integer> uniqueIds = new HashSet<>();
	private final Multimap<Integer, UniqueItem> uniquePositionMap = ArrayListMultimap.create();

	// Store a LootLog for all minions
	private final List<LootLog> minionLogs = new ArrayList<>();

	public LootLog(final Collection<LTRecord> records, final String name)
	{
		this.records.addAll(records);
		this.name = name;

		if (records.size() == 0)
		{
			this.type = LootRecordType.UNKNOWN;
		}
		else
		{
			final LTRecord record = this.records.get(0);
			this.type = record.getType();
		}

		for (final LTRecord rec : records)
		{
			for (final LTItemEntry entry : rec.getDrops())
			{
				addItemEntryToMap(entry);
			}
		}

		final Collection<UniqueItem> unsorted = UniqueItem.getUniquesForBoss(name);
		if (unsorted == null)
		{
			uniques = new ArrayList<>();
			return;
		}

		uniques = unsorted.stream().sorted(Comparator.comparingInt(UniqueItem::getPosition)).collect(Collectors.toList());
	}

	public synchronized void addRecord(final LTRecord record)
	{
		records.add(record);
		for (final LTItemEntry entry : record.getDrops())
		{
			addItemEntryToMap(entry);
		}
	}

	private enum ClueType
	{
		SCROLL,
		CASKET
	}

	private void addItemEntryToMap(LTItemEntry item)
	{
		final String itemNameLowercased = item.getName().toLowerCase();

		ClueType type = null;
		if (itemNameLowercased.startsWith("clue scroll"))
		{
			type = ClueType.SCROLL;
		}
		else if(itemNameLowercased.startsWith("casket "))
		{
			type = ClueType.CASKET;
		}

		if (type != null)
		{
			Matcher m = CLUE_ITEM_TYPE_PATTERN.matcher(item.getName());
			if (m.find())
			{
				final String result = m.group(1);
				int id = item.getId();
				switch (result.toLowerCase())
				{
					// Beginner and Master clues only have 1 ID
					case "easy":
						id = type.equals(ClueType.SCROLL) ? ItemID.TRAIL_CLUE_EASY_SIMPLE001 : ItemID.TRAIL_CLUE_EASY_MAP001_CASKET;
						break;
					case "medium":
						id = type.equals(ClueType.SCROLL) ? ItemID.TRAIL_CLUE_MEDIUM_SEXTANT001 : ItemID.TRAIL_CLUE_MEDIUM_SEXTANT001_CASKET;
						break;
					case "hard":
						id = type.equals(ClueType.SCROLL) ? ItemID.TRAIL_CLUE_HARD_MAP001 : ItemID.TRAIL_CLUE_HARD_SEXTANT001_CASKET;
						break;
					case "elite":
						id = type.equals(ClueType.SCROLL) ? ItemID.TRAIL_ELITE_EMOTE_EXP1 : ItemID.TRAIL_ELITE_EMOTE_CASKET;
						break;
				}

				item = new LTItemEntry(item.getName(), id, item.getQuantity(), item.getPrice());
			}
		}

		final LTItemEntry oldEntry = consolidated.get(item.getId());
		if (oldEntry != null)
		{
			// Use the most recent price
			oldEntry.setPrice(item.getPrice());
			oldEntry.setQuantity(oldEntry.getQuantity() + item.getQuantity());
		}
		else
		{
			// Create a new instance for consolidated records
			consolidated.put(item.getId(), new LTItemEntry(item.getName(), item.getId(), item.getQuantity(), item.getPrice()));
		}
	}

	@Nullable
	public LootLog getMinionLog(final String name)
	{
		for (final LootLog log : minionLogs)
		{
			if (log.getName().equalsIgnoreCase(name))
			{
				return log;
			}
		}

		return null;
	}

	public long getLootValue(boolean includeMinions)
	{
		long value = getConsolidated()
			.values().stream()
			.mapToLong(e -> e.getPrice() * e.getQuantity())
			.sum();

		if (includeMinions)
		{
			for (final LootLog minionLog : minionLogs)
			{
				value += minionLog.getConsolidated()
					.values().stream()
					.mapToLong(e -> e.getPrice() * e.getQuantity())
					.sum();
			}
		}

		return value;
	}

	// Loop over all UniqueItems and check how many the player has received as a drop for each
	// Also add all Item IDs for uniques to a Set for easy hiding later on.
	public static void recalculateUniques(final LootLog lootLog, final boolean includeMinions)
	{
		lootLog.getUniqueIds().clear();
		lootLog.getUniquePositionMap().clear();

		final Collection<LootLog> allLogs = new ArrayList<>();
		allLogs.add(lootLog);
		if (includeMinions) {
			allLogs.addAll(lootLog.getMinionLogs());
		}

		for (final UniqueItem item : lootLog.getUniques())
		{
			final List<Integer> ids = Arrays.stream(item.getAlternativeIds()).boxed().collect(Collectors.toList());
			ids.add(item.getItemID());
			ids.add(item.getLinkedID());

			int qty = 0;
			for (final int id : ids)
			{
				lootLog.getUniqueIds().add(id);

				for (final LootLog log : allLogs)
				{
					final LTItemEntry entry = log.getConsolidated().get(id);
					qty += (entry == null ? 0 : entry.getQuantity());
				}
			}

			item.setQty(qty);
			lootLog.getUniquePositionMap().put(item.getPosition(), item);
		}
	}
}
