package com.fantasticsource.instances.world.dimensions.libraryofworlds;

import com.fantasticsource.mctools.PlayerData;
import com.fantasticsource.tools.datastructures.SortableTable;

import java.util.UUID;

public class VisitablePlayersData
{
    public SortableTable visitablePlayers = new SortableTable(Character.class, SortableTable.class);

    public VisitablePlayersData()
    {
        visitablePlayers.startSorting(0);
    }

    public void add(UUID id)
    {
        String name = PlayerData.getName(id);
        char c = name.charAt(0);

        SortableTable table = (SortableTable) visitablePlayers.get(0, c, 1);
        if (table == null)
        {
            table = new SortableTable(String.class);
            table.startSorting(0);
            visitablePlayers.add(c, table);
        }
        table.add(name);
    }

    public void remove(UUID id)
    {
        String name = PlayerData.getName(id);
        char c = name.charAt(0);

        SortableTable table = (SortableTable) visitablePlayers.get(0, c, 1);
        if (table == null) return;

        table.delete(name, 0);
        if (table.size() == 0) visitablePlayers.delete(c, 0);
    }
}
