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
import net.runelite.api.gameval.ItemID;

@Getter
public enum Pet
{
	// GWD Pets
	PET_GENERAL_GRAARDOR(ItemID.BANDOSPET, BossTab.GENERAL_GRAARDOR.getName()),
	PET_KREEARRA(ItemID.ARMADYLPET, BossTab.KREEARRA.getName()),
	PET_KRIL_TSUTSAROTH(ItemID.ZAMORAKPET, BossTab.KRIL_TSUTSAROTH.getName()),
	PET_ZILYANA(ItemID.SARADOMINPET, BossTab.COMMANDER_ZILYANA.getName()),
	NEXLING(ItemID.NEXPET, BossTab.NEX.getName()),
	// Wildy Pets
	CALLISTO_CUB(ItemID.CALLISTO_PET, BossTab.CALLISTO.getName(), "Artio"),
	PET_CHAOS_ELEMENTAL(ItemID.CHAOSELEPET, BossTab.CHAOS_ELEMENTAL.getName(), BossTab.CHAOS_FANATIC.getName()),
	SCORPIAS_OFFSPRING(ItemID.SCORPIA_PET, BossTab.SCORPIA.getName()),
	VENENATIS_SPIDERLING(ItemID.VENENATIS_PET, BossTab.VENENATIS.getName(), "Spindel"),
	VETION_JR(ItemID.VETION_PET, BossTab.VETION.getName(), "Vet'ion", "Calvar'ion"),
	// KBD isn't really in wildy but meh
	PRINCE_BLACK_DRAGON(ItemID.KBDPET, BossTab.KING_BLACK_DRAGON.getName()),
	// Slayer Pets
	ABYSSAL_ORPHAN(ItemID.ABYSSALSIRE_PET, BossTab.ABYSSAL_SIRE.getName()),
	HELLPUPPY(ItemID.HELL_PET, BossTab.CERBERUS.getName()),
	NOON(ItemID.DAWNPET, BossTab.GROTESQUE_GUARDIANS.getName()),
	PET_KRAKEN(ItemID.KRAKENPET, BossTab.KRAKEN.getName()),
	PET_SMOKE_DEVIL(ItemID.SMOKEPET, BossTab.THERMONUCLEAR_SMOKE_DEVIL.getName()),
	SKOTOS(ItemID.SKOTIZOPET, BossTab.SKOTIZO.getName()),
	NID(ItemID.ARAXXORPET, BossTab.ARAXXOR.getName()),
	// Other Bosses
	BABY_MOLE(ItemID.MOLEPET, BossTab.GIANT_MOLE.getName()),
	KALPHITE_PRINCESS(ItemID.KQPET_WALKING, BossTab.KALPHITE_QUEEN.getName()),
	OLMLET(ItemID.OLMPET, BossTab.CHAMBERS_OF_XERIC.getName()),
	LIL_ZIK(ItemID.VERZIKPET, BossTab.THEATRE_OF_BLOOD.getName()),
	PET_DARK_CORE(ItemID.COREPET, BossTab.CORPOREAL_BEAST.getName()),
	PET_SNAKELING(ItemID.SNAKEPET, BossTab.ZULRAH.getName()),
	PET_DAGANNOTH_REX(ItemID.REXPET, BossTab.DAGANNOTH_REX.getName()),
	PET_DAGANNOTH_PRIME(ItemID.PRIMEPET, BossTab.DAGANNOTH_PRIME.getName()),
	PET_DAGANNOTH_SUPREME(ItemID.SUPREMEPET, BossTab.DAGANNOTH_SUPREME.getName()),
	VORKI(ItemID.VORKATHPET, BossTab.VORKATH.getName()),
	BLOODHOUND(ItemID.BLOODHOUND_PET, BossTab.CLUE_SCROLL_MASTER.getName()),
	IKKLE_HYDRA(ItemID.HYDRAPET, BossTab.ALCHEMICAL_HYDRA.getName()),
	YOUNGLLEF(ItemID.GAUNTLETPET, BossTab.THE_GAUNTLET.getName()),
	SRARACHA(ItemID.SARACHNISPET, BossTab.SARACHNIS.getName()),
	SMOLCANO(ItemID.ZALCANOPET, BossTab.ZALCANO.getName()),
	LITTLE_NIGHTMARE(ItemID.NIGHTMAREPET, BossTab.NIGHTMARE.getName(), BossTab.PHOSANIS_NIGHTMARE.getName()),
	HERBI(ItemID.HERBIBOARPET, "Herbiboar"),
	HUBERTE(ItemID.HUEYPET, BossTab.HUEYCOATL.getName()),
	// Pretty sure
	PHOENIX(ItemID.PHOENIXPET, BossTab.WINTERTODT.getName()),
	PET_PENANCE_QUEEN(ItemID.PENANCEPET, "Barbarian Assault"),
	TINY_TEMPOR(ItemID.TEMPOROSSPET, BossTab.TEMPOROSS.getName()),
	TUMEKENS_GUARDIAN(ItemID.WARDENPET_TUMEKEN, BossTab.TOMBS_OF_AMASCUT.getName()),
	MUPHIN(ItemID.MUSPAHPET, BossTab.PHANTOM_MUSPAH.getName()),

	// Forgotten Four
	BUTCH(ItemID.VARDORVISPET, BossTab.VARDORVIS.getName()),
	LILVIATHAN(ItemID.LEVIATHANPET, BossTab.THE_LEVIATHAN.getName()),
	BARON(ItemID.DUKESUCELLUSPET, BossTab.DUKE_SUCELLUS.getName()),
	WISP(ItemID.WHISPERERPET, BossTab.THE_WHISPERER.getName()),

	// Fortis Colosseum
	SMOL_HEREDIT(ItemID.SOLHEREDITPET, BossTab.FORTIS_COLOSSEUM.getName()),

	// Pets that aren't tied to a BossTab
	MOXI(ItemID.AMOXLIATLPET, "Amoxliatl"),
	SCURRY(ItemID.SCURRIUSPET, "Scurrius"),
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
