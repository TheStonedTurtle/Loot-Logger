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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.ItemID;
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
						id = type.equals(ClueType.SCROLL) ? ItemID.CLUE_SCROLL_EASY : ItemID.CASKET_EASY;
						break;
					case "medium":
						id = type.equals(ClueType.SCROLL) ? ItemID.CLUE_SCROLL_MEDIUM : ItemID.CASKET_MEDIUM;
						break;
					case "hard":
						id = type.equals(ClueType.SCROLL) ? ItemID.CLUE_SCROLL_HARD : ItemID.CASKET_HARD;
						break;
					case "elite":
						id = type.equals(ClueType.SCROLL) ? ItemID.CLUE_SCROLL_ELITE : ItemID.CASKET_ELITE;
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
}
