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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.google.gson.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import javax.inject.Inject;
import javax.inject.Singleton;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import static net.runelite.client.RuneLite.RUNELITE_DIR;

import net.runelite.api.ItemComposition;
import net.runelite.api.gameval.ItemID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.ItemManager;
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

	private final ClientThread clientThread;
	private final ItemManager itemManager;

	// Data is stored in a folder with the players username (login name)
	private File playerFolder = LOOT_RECORD_DIR;
	// Data is separated into sub-folders by event type to prevent issues.
	private final Map<LootRecordType, File> eventFolders = new HashMap<>();
	@Setter
	@Getter
	private String name;

	// Custom Deserializer for LTItemEntry which fetches HA price.
	private class LTItemEntryDeserializer implements JsonDeserializer<LTItemEntry>
	{
		@Override
		public LTItemEntry deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext)
		throws JsonParseException
		{
			JsonObject jsonObject = jsonElement.getAsJsonObject();
			final String name = jsonObject.get("name").getAsString();
			final int id = jsonObject.get("id").getAsInt();
			int quantity = jsonObject.get("quantity").getAsInt();
			long price = jsonObject.get("price").getAsLong();
			AtomicInteger haPrice = new AtomicInteger();
			clientThread.invoke(() ->
			{
				if (id == ItemID.COINS)
				{
					haPrice.set(1);
				}
				else if (id == ItemID.PLATINUM)
				{
					haPrice.set(1000);
				}
				else
				{
					ItemComposition c = itemManager.getItemComposition(id);
					haPrice.set(c.getHaPrice());
				}
			});
			return new LTItemEntry(name, id, quantity, price, haPrice.get());
		}
	}

	// The default date format does not allow migrating between Java 17 and Java 20+ (in either direction)
	// Java 20+ uses unicode character U+202f while java 17- use a normal space before the AM/PM part of the date string
	// This date adapter will attempt to match between both variants but will always write with a space
	private final Gson CUSTOM_GSON = RuneLiteAPI.GSON.newBuilder()
		.registerTypeAdapter(Date.class, new LootRecordDateAdapter())
		.registerTypeAdapter(LTItemEntry.class, new LTItemEntryDeserializer())
		.create();

	@Inject
	public LootRecordWriter(ClientThread clientThread, ItemManager itemManager)
	{
        this.clientThread = clientThread;
        this.itemManager = itemManager;
        LOOT_RECORD_DIR.mkdir();
	}

	public boolean setPlayerUsername(final String username)
	{
		if (username.equalsIgnoreCase(name))
		{
			return false;
		}

		playerFolder = new File(LOOT_RECORD_DIR, username);
		playerFolder.mkdir();
		name = username;
		createSubFolders();
		return true;
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
		final String fileName = npcNameToFileName(npcName);
		final File file = new File(eventFolders.get(recordType), fileName);
		final Collection<LTRecord> data = new ArrayList<>();

		try (final BufferedReader br = new BufferedReader(new FileReader(file)))
		{
			String line;
			while ((line = br.readLine()) != null)
			{
				// Skips the empty line at end of file
				if (line.length() > 0)
				{
					final LTRecord r = CUSTOM_GSON.fromJson(line, LTRecord.class);
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
		final String dataAsString = CUSTOM_GSON.toJson(rec);

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
				final String dataAsString = CUSTOM_GSON.toJson(rec);
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

	public synchronized boolean renameUsernameFolderToAccountHash(final String username, final long hash)
	{
		final File usernameDir = new File(LOOT_RECORD_DIR, username);
		if (!usernameDir.exists())
		{
			log.debug("Already rewritten");
			return true;
		}

		final File hashDir = new File(LOOT_RECORD_DIR, String.valueOf(hash));
		if (hashDir.exists())
		{
			log.warn("Can't rename username folder to account hash as the folder for this account hash already exists."
				+ " This was most likely caused by running RL through the Jagex launcher before the migration code was added");
			log.warn("Username: {} | AccountHash: {}", username, hash);
			return false;
		}

		return usernameDir.renameTo(hashDir);
	}
}
