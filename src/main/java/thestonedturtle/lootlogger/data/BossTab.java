/*
 * Copyright (c) 2018, TheStonedTurtle <www.github.com/TheStonedTurtle>
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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import lombok.Getter;
import net.runelite.api.gameval.ItemID;
import net.runelite.http.api.loottracker.LootRecordType;

@Getter
public enum BossTab
{
	// Raids
	CHAMBERS_OF_XERIC("Chambers of Xeric", ItemID.OLMPET, "Chests / Raids", LootRecordType.EVENT),
	THEATRE_OF_BLOOD("Theatre of Blood", ItemID.VERZIKPET, "Chests / Raids", LootRecordType.EVENT),
	TOMBS_OF_AMASCUT("Tombs of Amascut", ItemID.WARDENPET_TUMEKEN, "Chests / Raids", LootRecordType.EVENT,
		ImmutableSet.of("Cache of runes")),

	BARROWS("Barrows", ItemID.TELETAB_BARROWS, "Chests / Raids", LootRecordType.EVENT),
	MOONS_OF_PERIL("Moons of Peril", ItemID.DUAL_MACUAHUITL, "Chests / Raids", LootRecordType.EVENT, "Lunar Chest"),
	FORTIS_COLOSSEUM("Fortis Colosseum", ItemID.SOLHEREDITPET, "Chests / Raids", LootRecordType.EVENT),

	// Loot received on NPC death
	ZULRAH("Zulrah", ItemID.SNAKEPET, "Other", LootRecordType.NPC),
	VORKATH("Vorkath", ItemID.VORKATHPET, "Other", LootRecordType.NPC),

	// Forgotten Four
	VARDORVIS("Vardorvis", ItemID.VARDORVISPET, "Forgotten Four", LootRecordType.NPC),
	THE_LEVIATHAN("The Leviathan", ItemID.LEVIATHANPET, "Forgotten Four", LootRecordType.NPC),
	DUKE_SUCELLUS("Duke Sucellus", ItemID.DUKESUCELLUSPET, "Forgotten Four", LootRecordType.NPC),
	THE_WHISPERER("The Whisperer", ItemID.WHISPERERPET, "Forgotten Four", LootRecordType.NPC),

	// God wars dungeon
	KREEARRA("Kree'arra", ItemID.ARMADYLPET , "God Wars Dungeon", LootRecordType.NPC,
		ImmutableSet.of("Wingman Skree", "Flockleader Geerin", "Flight Kilisa")),
	GENERAL_GRAARDOR("General Graardor", ItemID.BANDOSPET , "God Wars Dungeon", LootRecordType.NPC,
		ImmutableSet.of("Sergeant Strongstack", "Sergeant Steelwill", "Sergeant Grimspike")),
	COMMANDER_ZILYANA("Commander Zilyana", ItemID.SARADOMINPET , "God Wars Dungeon", LootRecordType.NPC,
		ImmutableSet.of("Growler", "Bree", "Starlight")),
	KRIL_TSUTSAROTH("K'ril Tsutsaroth", ItemID.ZAMORAKPET , "God Wars Dungeon", LootRecordType.NPC,
		ImmutableSet.of("Balfrug Kreeyath", "Tstanon Karlak", "Zakl'n Gritch")),
	NEX("Nex", ItemID.NEXPET, "God Wars Dungeon", LootRecordType.NPC,
		ImmutableSet.of("Fumus", "Umbra", "Cruor", "Glacies")),

	// Wildy Bosses
	VETION("Vet'ion Reborn", ItemID.VETION_PET , "Wilderness", LootRecordType.NPC, ImmutableSet.of("Calvar'ion"), "Vet'ion"),
	VENENATIS("Venenatis", ItemID.VENENATIS_PET , "Wilderness", LootRecordType.NPC, ImmutableSet.of("Spindel")),
	CALLISTO("Callisto", ItemID.CALLISTO_PET , "Wilderness", LootRecordType.NPC, ImmutableSet.of("Artio")),
	CHAOS_ELEMENTAL("Chaos Elemental", ItemID.CHAOSELEPET , "Wilderness", LootRecordType.NPC),
	// Wildy Demi-Bosses
	SCORPIA("Scorpia", ItemID.SCORPIA_PET, "Wilderness", LootRecordType.NPC),
	CHAOS_FANATIC("Chaos Fanatic", ItemID.STAFF_OF_ZAROS , "Wilderness", LootRecordType.NPC),
	CRAZY_ARCHAEOLOGIST("Crazy Archaeologist", ItemID.FEDORA , "Wilderness", LootRecordType.NPC),
	// Wildy Other
	KING_BLACK_DRAGON("King Black Dragon", ItemID.KBDPET , "Wilderness", LootRecordType.NPC),

	// Slayer Bosses
	SKOTIZO("Skotizo", ItemID.SKOTIZOPET, "Slayer", LootRecordType.NPC),
	GROTESQUE_GUARDIANS("Grotesque Guardians", ItemID.DAWNPET, "Slayer", LootRecordType.NPC, "Dusk"),
	ABYSSAL_SIRE("Abyssal Sire", ItemID.ABYSSALSIRE_PET, "Slayer", LootRecordType.NPC),
	KRAKEN("Kraken", ItemID.KRAKENPET, "Slayer", LootRecordType.NPC),
	CERBERUS("Cerberus", ItemID.HELL_PET, "Slayer", LootRecordType.NPC),
	THERMONUCLEAR_SMOKE_DEVIL("Thermonuclear smoke devil", ItemID.SMOKEPET, "Slayer", LootRecordType.NPC),
	ALCHEMICAL_HYDRA("Alchemical Hydra", ItemID.HYDRAPET, "Slayer", LootRecordType.NPC),
	ARAXXOR("Araxxor", ItemID.ARAXXORPET, "Slayer", LootRecordType.NPC),

	// Other Bosses
	KALPHITE_QUEEN("Kalphite Queen", ItemID.KQPET_WALKING, "Other", LootRecordType.NPC),
	GIANT_MOLE("Giant Mole", ItemID.MOLEPET, "Other", LootRecordType.NPC),
	CORPOREAL_BEAST("Corporeal Beast", ItemID.CORPPET, "Other", LootRecordType.NPC),
	SARACHNIS("Sarachnis", ItemID.SARACHNISPET, "Other", LootRecordType.NPC),
	THE_GAUNTLET("The Gauntlet", ItemID.GAUNTLETPET, "Other", LootRecordType.EVENT),
	NIGHTMARE("The Nightmare", ItemID.NIGHTMAREPET, "Other", LootRecordType.NPC),
	PHOSANIS_NIGHTMARE("Phosani's Nightmare", ItemID.NIGHTMAREPET_PARASITE, "Other", LootRecordType.NPC),
	PHANTOM_MUSPAH("Phantom Muspah", ItemID.MUSPAHPET, "Other", LootRecordType.NPC),
	HUEYCOATL("The Hueycoatl", ItemID.HUEYPET, "Other", LootRecordType.NPC),

	// Dagannoth Kings
	DAGANNOTH_REX("Dagannoth Rex", ItemID.REXPET, "Dagannoth Kings", LootRecordType.NPC),
	DAGANNOTH_PRIME("Dagannoth Prime", ItemID.PRIMEPET, "Dagannoth Kings", LootRecordType.NPC),
	DAGANNOTH_SUPREME("Dagannoth Supreme", ItemID.SUPREMEPET, "Dagannoth Kings", LootRecordType.NPC),

	// Clue scrolls
	CLUE_SCROLL_BEGINNER("Clue Scroll (Beginner)", ItemID.TRAIL_CLUE_BEGINNER, "Clue Scrolls", LootRecordType.EVENT),
	CLUE_SCROLL_EASY("Clue Scroll (Easy)", ItemID.TRAIL_CLUE_EASY_SIMPLE001, "Clue Scrolls", LootRecordType.EVENT),
	CLUE_SCROLL_MEDIUM("Clue Scroll (Medium)", ItemID.TRAIL_CLUE_MEDIUM_SEXTANT001, "Clue Scrolls", LootRecordType.EVENT),
	CLUE_SCROLL_HARD("Clue Scroll (Hard)", ItemID.TRAIL_CLUE_HARD_MAP001, "Clue Scrolls", LootRecordType.EVENT),
	CLUE_SCROLL_ELITE("Clue Scroll (Elite)", ItemID.TRAIL_ELITE_EMOTE_EXP1, "Clue Scrolls", LootRecordType.EVENT),
	CLUE_SCROLL_MASTER("Clue Scroll (Master)", ItemID.TRAIL_CLUE_MASTER, "Clue Scrolls", LootRecordType.EVENT),

	// Skilling
	WINTERTODT("Wintertodt", ItemID.PHOENIXPET, "Skilling", LootRecordType.EVENT, "Supply crate (Wintertodt)"),
	TEMPOROSS("Tempoross", ItemID.TEMPOROSSPET, "Skilling", LootRecordType.EVENT,
		ImmutableSet.of("Casket (Tempoross)"), "Reward pool (Tempoross)"),
	ZALCANO("Zalcano", ItemID.ZALCANOPET, "Skilling", LootRecordType.NPC),
	;

	private final String name;
	private final int itemID;
	private final String category;
	private final LootRecordType type;
	private final Set<String> aliases = new HashSet<>();
	private final Set<String> minions;

	BossTab(final String name, final int itemID, final String category, final LootRecordType type, final String... aliases)
	{
		this(name, itemID, category, type, Collections.emptyList(), aliases);
	}

	BossTab(final String name, final int itemID, final String category, final LootRecordType type, final Collection<String> minions, final String... aliases)
	{
		this.name = name;
		this.itemID = itemID;
		this.category = category;
		this.type = type;

		this.aliases.add(name);
		this.aliases.addAll(Arrays.asList(aliases));
		this.minions = minions.stream().map(String::toLowerCase).collect(Collectors.toSet());
	}

	private static final Map<String, BossTab> NAME_MAP;
	private static final Multimap<String, BossTab> CATEGORY_MAP;
	static
	{
		final ImmutableMap.Builder<String, BossTab> byName = ImmutableMap.builder();
		final ImmutableMultimap.Builder<String, BossTab> categoryMap = ImmutableMultimap.builder();

		for (BossTab tab : values())
		{
			for (final String name : tab.getAliases()) {
				byName.put(name.toUpperCase(), tab);
			}
			categoryMap.put(tab.getCategory(), tab);
		}

		NAME_MAP = byName.build();
		CATEGORY_MAP = categoryMap.build();
	}

	@Nullable
	public static BossTab getByName(final String name)
	{
		return NAME_MAP.get(name.toUpperCase());
	}

	public static Collection<BossTab> getByCategoryName(final String name)
	{
		return CATEGORY_MAP.get(name);
	}

	public static TreeSet<String> getCategories()
	{
		return new TreeSet<>(CATEGORY_MAP.keySet());
	}
}
