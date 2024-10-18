/*
 * Copyright (c) 2020, TheStonedTurtle <https://github.com/TheStonedTurtle>
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
import lombok.Getter;
import net.runelite.api.ItemID;

@Getter
public enum Pet
{
	// GWD Pets
	PET_GENERAL_GRAARDOR(ItemID.PET_GENERAL_GRAARDOR, BossTab.GENERAL_GRAARDOR.getName()),
	PET_KREEARRA(ItemID.PET_KREEARRA, BossTab.KREEARRA.getName()),
	PET_KRIL_TSUTSAROTH(ItemID.PET_KRIL_TSUTSAROTH, BossTab.KRIL_TSUTSAROTH.getName()),
	PET_ZILYANA(ItemID.PET_ZILYANA, BossTab.COMMANDER_ZILYANA.getName()),
	NEXLING(ItemID.NEXLING, BossTab.NEX.getName()),
	// Wildy Pets
	CALLISTO_CUB(ItemID.CALLISTO_CUB, BossTab.CALLISTO.getName(), "Artio"),
	PET_CHAOS_ELEMENTAL(ItemID.PET_CHAOS_ELEMENTAL, BossTab.CHAOS_ELEMENTAL.getName(), BossTab.CHAOS_FANATIC.getName()),
	SCORPIAS_OFFSPRING(ItemID.SCORPIAS_OFFSPRING, BossTab.SCORPIA.getName()),
	VENENATIS_SPIDERLING(ItemID.VENENATIS_SPIDERLING, BossTab.VENENATIS.getName(), "Spindel"),
	VETION_JR(ItemID.VETION_JR, BossTab.VETION.getName(), "Vet'ion", "Calvar'ion"),
	// KBD isn't really in wildy but meh
	PRINCE_BLACK_DRAGON(ItemID.PRINCE_BLACK_DRAGON, BossTab.KING_BLACK_DRAGON.getName()),
	// Slayer Pets
	ABYSSAL_ORPHAN(ItemID.ABYSSAL_ORPHAN, BossTab.ABYSSAL_SIRE.getName()),
	HELLPUPPY(ItemID.HELLPUPPY, BossTab.CERBERUS.getName()),
	NOON(ItemID.NOON, BossTab.GROTESQUE_GUARDIANS.getName()),
	PET_KRAKEN(ItemID.PET_KRAKEN, BossTab.KRAKEN.getName()),
	PET_SMOKE_DEVIL(ItemID.PET_SMOKE_DEVIL, BossTab.THERMONUCLEAR_SMOKE_DEVIL.getName()),
	SKOTOS(ItemID.SKOTOS, BossTab.SKOTIZO.getName()),
	NID(ItemID.NID, BossTab.ARAXXOR.getName()),
	// Other Bosses
	BABY_MOLE(ItemID.BABY_MOLE, BossTab.GIANT_MOLE.getName()),
	KALPHITE_PRINCESS(ItemID.KALPHITE_PRINCESS, BossTab.KALPHITE_QUEEN.getName()),
	OLMLET(ItemID.OLMLET, BossTab.CHAMBERS_OF_XERIC.getName()),
	LIL_ZIK(ItemID.LIL_ZIK, BossTab.THEATRE_OF_BLOOD.getName()),
	PET_DARK_CORE(ItemID.PET_DARK_CORE, BossTab.CORPOREAL_BEAST.getName()),
	PET_SNAKELING(ItemID.PET_SNAKELING, BossTab.ZULRAH.getName()),
	PET_DAGANNOTH_REX(ItemID.PET_DAGANNOTH_REX, BossTab.DAGANNOTH_REX.getName()),
	PET_DAGANNOTH_PRIME(ItemID.PET_DAGANNOTH_PRIME, BossTab.DAGANNOTH_PRIME.getName()),
	PET_DAGANNOTH_SUPREME(ItemID.PET_DAGANNOTH_SUPREME, BossTab.DAGANNOTH_SUPREME.getName()),
	VORKI(ItemID.VORKI, BossTab.VORKATH.getName()),
	BLOODHOUND(ItemID.BLOODHOUND, BossTab.CLUE_SCROLL_MASTER.getName()),
	IKKLE_HYDRA(ItemID.IKKLE_HYDRA, BossTab.ALCHEMICAL_HYDRA.getName()),
	YOUNGLLEF(ItemID.YOUNGLLEF, BossTab.THE_GAUNTLET.getName()),
	SRARACHA(ItemID.SRARACHA, BossTab.SARACHNIS.getName()),
	SMOLCANO(ItemID.SMOLCANO, BossTab.ZALCANO.getName()),
	LITTLE_NIGHTMARE(ItemID.LITTLE_NIGHTMARE, BossTab.NIGHTMARE.getName(), BossTab.PHOSANIS_NIGHTMARE.getName()),
	HERBI(ItemID.HERBI, "Herbiboar"),
	HUBERTE(ItemID.HUBERTE, BossTab.HUEYCOATL.getName()),
	// Pretty sure
	PHOENIX(ItemID.PHOENIX, BossTab.WINTERTODT.getName()),
	PET_PENANCE_QUEEN(ItemID.PET_PENANCE_QUEEN, "Barbarian Assault"),
	TINY_TEMPOR(ItemID.TINY_TEMPOR, BossTab.TEMPOROSS.getName()),
	TUMEKENS_GUARDIAN(ItemID.TUMEKENS_GUARDIAN, BossTab.TOMBS_OF_AMASCUT.getName()),
	MUPHIN(ItemID.MUPHIN, BossTab.PHANTOM_MUSPAH.getName()),

	// Forgotten Four
	BUTCH(ItemID.BUTCH, BossTab.VARDORVIS.getName()),
	LILVIATHAN(ItemID.LILVIATHAN, BossTab.THE_LEVIATHAN.getName()),
	BARON(ItemID.BARON, BossTab.DUKE_SUCELLUS.getName()),
	WISP(ItemID.WISP, BossTab.THE_WHISPERER.getName()),

	// Fortis Colosseum
	SMOL_HEREDIT(ItemID.SMOL_HEREDIT, BossTab.FORTIS_COLOSSEUM.getName()),

	// Pets that aren't tied to a BossTab
	MOXI(ItemID.MOXI, "Amoxliatl"),
	SCURRY(ItemID.SCURRY, "Scurrius"),
	;

	private static final ImmutableMap<String, Pet> BOSS_MAP;
	static
	{
		final ImmutableMap.Builder<String, Pet> byName = ImmutableMap.builder();
		for (final Pet pet : values())
		{
			for (final String bossName : pet.getBossNames())
			{
				byName.put(bossName.toUpperCase(), pet);
			}
		}

		BOSS_MAP = byName.build();
	}

	private final int petID;
	private final String[] bossNames;

	Pet(final int id, final String... bossNames)
	{
		this.petID = id;
		this.bossNames = bossNames;
	}

	public static Pet getByBossName(final String name)
	{
		return BOSS_MAP.get(name.toUpperCase());
	}
}
