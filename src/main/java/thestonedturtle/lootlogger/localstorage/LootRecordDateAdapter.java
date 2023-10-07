/*
 * Copyright (c) 2023, TheStonedTurtle <https://github.com/TheStonedTurtle>
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

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;

/**
 * This class is needed because at some point after Java 17 but including Java 20 the default date format changed
 * For Java 17- it uses a normal space between the AM/PM while in Java 20+ the unicode character U+202f is used
 * This adapter checks to see if the unicode character is within the date and will use the proper GSON object for parsing it
 */
@Slf4j
public class LootRecordDateAdapter extends TypeAdapter<Date>
{
	private final SimpleDateFormat SPACE_FORMAT = new SimpleDateFormat("MMM d, yyyy, h:mm:ss aaa");
	private final SimpleDateFormat UNICODE_FORMAT = new SimpleDateFormat("MMM d, yyyy, h:mm:ss\u202Faaa");

	@Override
	public void write(JsonWriter out, Date value) throws IOException
	{
		if (value == null) {
			out.nullValue();
			return;
		}

		out.value(SPACE_FORMAT.format(value));
	}

	@Override
	public Date read(JsonReader reader) throws IOException {
		if (reader.peek() == JsonToken.NULL) {
			reader.nextNull();
			return null;
		}

		String date = reader.nextString();
		try
		{
			if (date.contains("\u202F"))
			{
				return UNICODE_FORMAT.parse(date);
			}

			return SPACE_FORMAT.parse(date);
		}
		catch (ParseException e)
		{
			log.error("Error parsing LootRecord date value: {}", date);
			e.printStackTrace();
			return null;
		}
	}

}
