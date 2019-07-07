package com.fantasticsource.instances.world.dimensions.libraryofworlds;

import com.fantasticsource.mctools.PlayerData;
import com.fantasticsource.tools.datastructures.SortableTable;

import java.util.ArrayList;
import java.util.UUID;

public class LibraryOfWorldsChunkData
{
    public SortableTable visitablePlayers = new SortableTable(Character.class, ArrayList.class);

    public LibraryOfWorldsChunkData()
    {
        visitablePlayers.startSorting(0);
    }

    public void add(UUID id)
    {
        String name = PlayerData.getName(id);
        char c = name.charAt(0);

        ArrayList<String> list = (ArrayList<String>) visitablePlayers.get(0, c, 1);
        if (list == null)
        {
            list = new ArrayList<>();
            visitablePlayers.add(c, list);
        }
        list.add(name);
    }

    public void remove(UUID id)
    {
        String name = PlayerData.getName(id);
        char c = name.charAt(0);

        ArrayList<String> list = (ArrayList<String>) visitablePlayers.get(0, c, 1);
        if (list == null) return;

        list.remove(name);
        if (list.size() == 0) visitablePlayers.delete(c, 0);
    }
}
