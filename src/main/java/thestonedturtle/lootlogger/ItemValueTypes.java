package thestonedturtle.lootlogger;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ItemValueTypes
{
    GRAND_EXCHANGE("Grand Exchange"),
    HIGH_ALCHEMY("High Alchemy");

    private final String name;

    @Override
    public String toString()
    {
        return name;
    }
}
