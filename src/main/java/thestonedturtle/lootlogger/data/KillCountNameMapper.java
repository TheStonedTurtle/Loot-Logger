/*
 * Copyright (c) 2024, TheStonedTurtle <https://github.com/TheStonedTurtle>
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
import lombok.AllArgsConstructor;

// Some NPCs have a different name for their KC messages than their in-game name
@AllArgsConstructor
public enum KillCountNameMapper
{
	THE_WHISPERER("The Whisperer", "Whisperer"),
	THE_LEVIATHAN("The Leviathan", "Leviathan"),
	MOONS_OF_PERIL("Moons of Peril", "Lunar Chest"),

	;

	private final String bossName;
	private final String killCountName;

	private static final ImmutableMap<String, String> nameMap;

	static
	{

		final ImmutableMap.Builder<String, String> m = ImmutableMap.builder();
		for (final KillCountNameMapper entry : KillCountNameMapper.values())
		{
			m.put(entry.killCountName.toLowerCase(), entry.bossName);
		}

		nameMap = m.build();
	}

	public static String getBossNameFromKillCountName(String name)
	{
		return nameMap.getOrDefault(name.toLowerCase(), name);
	}
}
