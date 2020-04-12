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
package thestonedturtle.lootlogger.localstorage;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import static net.runelite.client.RuneLite.RUNELITE_DIR;
import net.runelite.http.api.RuneLiteAPI;
import net.runelite.http.api.loottracker.LootRecordType;

/**
 * Reads & Writes LootRecord data from `*name*.log` files located in `.runelite/loots/`.
 * Data is stored as json delimited by newlines, aka JSON Lines {@see <a href="http://jsonlines.org">http://jsonlines.org</a>}
 */
@Slf4j
@Singleton
public class LootRecordWriter
{
	private static final String FILE_EXTENSION = ".log";
	private static final File LOOT_RECORD_DIR = new File(RUNELITE_DIR, "loots");

	// Data is stored in a folder with the players username (login name)
	private File playerFolder = LOOT_RECORD_DIR;
	// Data is separated into sub-folders by event type to prevent issues.
	private final Map<LootRecordType, File> eventFolders = new HashMap<>();
	private String name;

	@Inject
	public LootRecordWriter()
	{
		LOOT_RECORD_DIR.mkdir();
	}

	public void setPlayerUsername(final String username)
	{
		if (username.equalsIgnoreCase(name))
		{
			return;
		}

		playerFolder = new File(LOOT_RECORD_DIR, username);
		playerFolder.mkdir();
		name = username;
		createSubFolders();
	}

	private void createSubFolders()
	{
		eventFolders.clear();
		for (final LootRecordType type : LootRecordType.values())
		{
			final File folder = new File(playerFolder, type.name().toLowerCase());
			folder.mkdir();
			eventFolders.put(type, folder);
		}
	}

	private static String npcNameToFileName(final String npcName)
	{
		return npcName.toLowerCase().trim() + FILE_EXTENSION;
	}

	public SetMultimap<LootRecordType, String> getKnownFileNames()
	{
		final SetMultimap<LootRecordType, String> fileNames = HashMultimap.create();

		for (final Map.Entry<LootRecordType, File> entry : eventFolders.entrySet())
		{
			final File[] files = entry.getValue().listFiles((dir, name) -> name.endsWith(FILE_EXTENSION));
			if (files != null)
			{
				for (final File f : files)
				{
					fileNames.put(entry.getKey(), f.getName().replace(FILE_EXTENSION, ""));
				}
			}
		}

		return fileNames;
	}

	public synchronized Collection<LTRecord> loadLootTrackerRecords(LootRecordType recordType, String npcName)
	{
		return loadLootTrackerRecords(npcName, eventFolders.get(recordType));
	}

	// TODO: Remove folder parameter in future release when data migration is no longer needed
	@Deprecated
	public synchronized Collection<LTRecord> loadLootTrackerRecords(String npcName, File folder)
	{
		final String fileName = npcNameToFileName(npcName);
		final File file = new File(folder, fileName);
		final Collection<LTRecord> data = new ArrayList<>();

		try (final BufferedReader br = new BufferedReader(new FileReader(file)))
		{
			String line;
			while ((line = br.readLine()) != null)
			{
				// Skips the empty line at end of file
				if (line.length() > 0)
				{
					final LTRecord r = RuneLiteAPI.GSON.fromJson(line, LTRecord.class);
					data.add(r);
				}
			}

		}
		catch (FileNotFoundException e)
		{
			log.debug("File not found: {}", fileName);
		}
		catch (IOException e)
		{
			log.warn("IOException for file {}: {}", fileName, e.getMessage());
		}

		return data;
	}

	public synchronized boolean addLootTrackerRecord(LTRecord rec)
	{
		// Grab file
		final String fileName = npcNameToFileName(rec.getName());
		final File lootFile = new File(eventFolders.get(rec.getType()), fileName);

		// Convert entry to JSON
		final String dataAsString = RuneLiteAPI.GSON.toJson(rec);

		// Open File in append mode and write new data
		try
		{
			final BufferedWriter file = new BufferedWriter(new FileWriter(String.valueOf(lootFile), true));
			file.append(dataAsString);
			file.newLine();
			file.close();
			return true;
		}
		catch (IOException ioe)
		{
			log.warn("Error writing loot data to file {}: {}", fileName, ioe.getMessage());
			return false;
		}
	}

	public synchronized boolean deleteLootTrackerRecords(final LootRecordType type, String npcName)
	{
		final String fileName = npcNameToFileName(npcName);
		final File lootFile = new File(eventFolders.get(type), fileName);

		if (lootFile.delete())
		{
			log.debug("Deleted loot file: {}", fileName);
			return true;
		}
		else
		{
			log.debug("Couldn't delete file: {}", fileName);
			return false;
		}
	}

	/**
	 * Writes an entire log file based on the passed collection.
	 * Used when you need to adjust previous data and not just append, such as receiving a pet.
 	 */
	public synchronized boolean writeLootTrackerFile(final String npcName, final Collection<LTRecord> loots)
	{
		final String fileName = npcNameToFileName(npcName);
		final File lootFile = new File(playerFolder, fileName);

		try
		{
			final BufferedWriter file = new BufferedWriter(new FileWriter(String.valueOf(lootFile), false));
			for (final LTRecord rec : loots)
			{
				// Convert entry to JSON
				final String dataAsString = RuneLiteAPI.GSON.toJson(rec);
				file.append(dataAsString);
				file.newLine();
			}
			file.close();

			return true;
		}
		catch (IOException ioe)
		{
			log.warn("Error rewriting loot data to file {}: {}", fileName, ioe.getMessage());
			return false;
		}
	}

	// TODO: Remove this in a future release
	@Deprecated
	public boolean migrateDataFromDisplayNameToUsername(final String displayName, final String username)
	{
		final File currentDirectory = new File(LOOT_RECORD_DIR, displayName);
		if (!currentDirectory.exists())
		{
			// Most likely was already converted
			return true;
		}

		if (displayName.equalsIgnoreCase(username))
		{
			return migrateDataLayout(currentDirectory);
		}

		final File newDirectory = new File(LOOT_RECORD_DIR, username);
		final boolean renamed = currentDirectory.renameTo(newDirectory);
		if (!renamed)
		{
			return false;
		}

		return migrateDataLayout(newDirectory);
	}

	// TODO: Remove this in a future release
	@Deprecated
	public boolean migrateDataLayout(final File folder)
	{
		final File[] files = folder.listFiles((dir, name) -> name.endsWith(FILE_EXTENSION));
		if (files == null)
		{
			return false;
		}

		if (files.length == 0)
		{
			// Assume data is already migrated to the new format if there are no loot files inside the directory
			return true;
		}

		for (final File f : files)
		{
			final String filename = f.getName().replace(FILE_EXTENSION, "");

			// Load current data and sort by LootRecordType to fix any existing name conflicts
			final Collection<LTRecord> records = loadLootTrackerRecords(filename, folder);
			final Multimap<LootRecordType, LTRecord> filtered = records.stream()
				.collect(Multimaps.toMultimap(
					LTRecord::getType,
					(rec) -> rec,
					ArrayListMultimap::create)
				);

			final Set<LootRecordType> keys = filtered.keySet();
			for (LootRecordType key : keys)
			{
				Collection<LTRecord> recs = filtered.get(key);
				if (key == null)
				{
					key = LootRecordType.UNKNOWN;
					recs.forEach(m -> m.setType(LootRecordType.UNKNOWN));
				}
				final File outputDir = new File(folder, key.name().toLowerCase());
				outputDir.mkdir();

				final File outputFile = new File(outputDir, f.getName());
				// If there's only 1 key we can just move the existing file instead unmarshalling the data again
				if (keys.size() == 1)
				{
					f.renameTo(outputFile);
				}
				else
				{
					try
					{
						final BufferedWriter file = new BufferedWriter(new FileWriter(String.valueOf(outputFile), false));
						for (final LTRecord rec : recs)
						{
							// Convert entry to JSON
							final String dataAsString = RuneLiteAPI.GSON.toJson(rec);
							file.append(dataAsString);
							file.newLine();
						}
						file.close();
					}
					catch (IOException ioe)
					{
						log.warn("Error migrating loot data from file `{}` to `{}`", f.getPath(), outputFile.getPath());
						continue;
					}
					f.delete();
				}
			}
		}

		return true;
	}
}
