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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.annotation.Nullable;
import lombok.Getter;
import net.runelite.api.ItemID;
import net.runelite.http.api.loottracker.LootRecordType;

@Getter
public enum BossTab
{
	// Chest Rewards
	BARROWS("Barrows", ItemID.BARROWS_TELEPORT, "Other", LootRecordType.EVENT),
	CHAMBERS_OF_XERIC("Chambers of Xeric", ItemID.OLMLET, "Other", LootRecordType.EVENT),
	THEATRE_OF_BLOOD("Theatre of Blood", ItemID.LIL_ZIK, "Other", LootRecordType.EVENT),

	// Loot received on NPC death
	ZULRAH("Zulrah", ItemID.PET_SNAKELING, "Other", LootRecordType.NPC),
	VORKATH("Vorkath", ItemID.VORKI, "Other", LootRecordType.NPC),

	// God wars dungeon
	KREEARRA("Kree'arra", ItemID.PET_KREEARRA , "God Wars Dungeon", LootRecordType.NPC,
		ImmutableSet.of("Wingman Skree", "Flockleader Geerin", "Flight Kilisa")),
	GENERAL_GRAARDOR("General Graardor", ItemID.PET_GENERAL_GRAARDOR , "God Wars Dungeon", LootRecordType.NPC,
		ImmutableSet.of("Sergeant Strongstack", "Sergeant Steelwill", "Sergeant Grimspike")),
	COMMANDER_ZILYANA("Commander Zilyana", ItemID.PET_ZILYANA , "God Wars Dungeon", LootRecordType.NPC,
		ImmutableSet.of("Growler", "Bree", "Starlight")),
	KRIL_TSUTSAROTH("K'ril Tsutsaroth", ItemID.PET_KRIL_TSUTSAROTH , "God Wars Dungeon", LootRecordType.NPC,
		ImmutableSet.of("Balfrug Kreeyath", "Tstanon Karlak", "Zakl'n Gritch")),

	// Wildy Bosses
	VETION("Vet'ion Reborn", ItemID.VETION_JR , "Wilderness", LootRecordType.NPC),
	VENENATIS("Venenatis", ItemID.VENENATIS_SPIDERLING , "Wilderness", LootRecordType.NPC),
	CALLISTO("Callisto", ItemID.CALLISTO_CUB , "Wilderness", LootRecordType.NPC),
	CHAOS_ELEMENTAL("Chaos Elemental", ItemID.PET_CHAOS_ELEMENTAL , "Wilderness", LootRecordType.NPC),
	// Wildy Demi-Bosses
	SCORPIA("Scorpia", ItemID.SCORPIAS_OFFSPRING, "Wilderness", LootRecordType.NPC),
	CHAOS_FANATIC("Chaos Fanatic", ItemID.ANCIENT_STAFF , "Wilderness", LootRecordType.NPC),
	CRAZY_ARCHAEOLOGIST("Crazy Archaeologist", ItemID.FEDORA , "Wilderness", LootRecordType.NPC),
	// Wildy Other
	KING_BLACK_DRAGON("King Black Dragon", ItemID.PRINCE_BLACK_DRAGON , "Wilderness", LootRecordType.NPC),

	// Slayer Bosses
	KALPHITE_QUEEN("Kalphite Queen", ItemID.KALPHITE_PRINCESS, "Other", LootRecordType.NPC),
	SKOTIZO("Skotizo", ItemID.SKOTOS, "Slayer", LootRecordType.NPC),
	GROTESQUE_GUARDIANS("Dusk", ItemID.NOON, "Slayer", LootRecordType.NPC),
	ABYSSAL_SIRE("Abyssal Sire", ItemID.ABYSSAL_ORPHAN, "Slayer", LootRecordType.NPC),
	KRAKEN("Kraken", ItemID.PET_KRAKEN, "Slayer", LootRecordType.NPC),
	CERBERUS("Cerberus", ItemID.HELLPUPPY, "Slayer", LootRecordType.NPC),
	THERMONUCLEAR_SMOKE_DEVIL("Thermonuclear smoke devil", ItemID.PET_SMOKE_DEVIL, "Slayer", LootRecordType.NPC),
	ALCHEMICAL_HYDRA("Alchemical Hydra", ItemID.IKKLE_HYDRA, "Slayer", LootRecordType.NPC),

	// Other Bosses
	GIANT_MOLE("Giant Mole", ItemID.BABY_MOLE, "Other", LootRecordType.NPC),
	CORPOREAL_BEAST("Corporeal Beast", ItemID.PET_CORPOREAL_CRITTER, "Other", LootRecordType.NPC),
	SARACHNIS("Sarachnis", ItemID.SRARACHA, "Other", LootRecordType.NPC),
	THE_GAUNTLET("The Gauntlet", ItemID.YOUNGLLEF, "Other", LootRecordType.EVENT),
	ZALCANO("Zalcano", ItemID.SMOLCANO, "Other", LootRecordType.NPC),
	NIGHTMARE("The Nightmare", ItemID.LITTLE_NIGHTMARE, "Other", LootRecordType.NPC),
	PHOSANIS_NIGHTMARE("Phosani's Nightmare", ItemID.LITTLE_PARASITE, "Other", LootRecordType.NPC),

	// Dagannoth Kings
	DAGANNOTH_REX("Dagannoth Rex", ItemID.PET_DAGANNOTH_REX, "Dagannoth Kings", LootRecordType.NPC),
	DAGANNOTH_PRIME("Dagannoth Prime", ItemID.PET_DAGANNOTH_PRIME, "Dagannoth Kings", LootRecordType.NPC),
	DAGANNOTH_SUPREME("Dagannoth Supreme", ItemID.PET_DAGANNOTH_SUPREME, "Dagannoth Kings", LootRecordType.NPC),

	// Clue scrolls
	CLUE_SCROLL_BEGINNER("Clue Scroll (Beginner)", ItemID.CLUE_SCROLL_BEGINNER, "Clue Scrolls", LootRecordType.EVENT),
	CLUE_SCROLL_EASY("Clue Scroll (Easy)", ItemID.CLUE_SCROLL_EASY, "Clue Scrolls", LootRecordType.EVENT),
	CLUE_SCROLL_MEDIUM("Clue Scroll (Medium)", ItemID.CLUE_SCROLL_MEDIUM, "Clue Scrolls", LootRecordType.EVENT),
	CLUE_SCROLL_HARD("Clue Scroll (Hard)", ItemID.CLUE_SCROLL_HARD, "Clue Scrolls", LootRecordType.EVENT),
	CLUE_SCROLL_ELITE("Clue Scroll (Elite)", ItemID.CLUE_SCROLL_ELITE, "Clue Scrolls", LootRecordType.EVENT),
	CLUE_SCROLL_MASTER("Clue Scroll (Master)", ItemID.CLUE_SCROLL_MASTER, "Clue Scrolls", LootRecordType.EVENT),

	// Skilling
	WINTERTODT("Wintertodt", ItemID.PHOENIX, "Skilling", LootRecordType.EVENT, "Supply crate (Wintertodt)"),
	TEMPOROSS("Tempoross", ItemID.TINY_TEMPOR, "Skilling", LootRecordType.EVENT,
		ImmutableSet.of("Casket (Tempoross)"), "Reward pool (Tempoross)"),
	;

	private final String name;
	private final int itemID;
	private final String category;
	private final LootRecordType type;
	private final Set<String> aliases = new HashSet<>();
	private final Set<String> minions = new HashSet<>();

	BossTab(final String name, final int itemID, final String category, final LootRecordType type, final String... aliases)
	{
		this.name = name;
		this.itemID = itemID;
		this.category = category;
		this.type = type;

		this.aliases.add(name);
		this.aliases.addAll(Arrays.asList(aliases));
	}

	BossTab(final String name, final int itemID, final String category, final LootRecordType type, final Collection<String> minions, final String... aliases)
	{
		this(name, itemID, category, type, aliases);
		this.minions.addAll(minions);
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
