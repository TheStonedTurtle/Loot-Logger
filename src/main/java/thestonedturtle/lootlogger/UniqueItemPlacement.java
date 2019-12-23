package thestonedturtle.lootlogger;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UniqueItemPlacement
{
	ITEM_BREAKDOWN("Item breakdown"),
	UNIQUES_PANEL("Uniques panel"),
	BOTH("Both panels");

	private final String name;

	@Override
	public String toString()
	{
		return name;
	}
}
