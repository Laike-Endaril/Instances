package com.fantasticsource.instances.world.dimensions.libraryofworlds;

import com.fantasticsource.mctools.PlayerData;
import com.fantasticsource.tools.Tools;
import com.fantasticsource.tools.datastructures.SortableTable;

import java.util.ArrayList;
import java.util.UUID;

public class LibraryOfWorldsChunkData
{
    private SortableTable visitablePlayers = new SortableTable(Character.class, ArrayList.class);
    private int chunkXMin = -1, chunkXMax = 0, chunkZMins[] = new int[]{-1, 0}, chunkZMaxes[] = new int[]{-1, 0};

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

        recalc();
    }

    public void remove(UUID id)
    {
        String name = PlayerData.getName(id);
        char c = name.charAt(0);

        ArrayList<String> list = (ArrayList<String>) visitablePlayers.get(0, c, 1);
        if (list == null) return;

        list.remove(name);
        if (list.size() == 0) visitablePlayers.delete(c, 0);

        recalc();
    }

    public int size()
    {
        return visitablePlayers.size();
    }

    private void recalc()
    {
        int chunkXTotal = size() / 8;
        chunkXMin = Tools.min(-chunkXTotal / 2, -1);
        chunkXMax = Tools.max((chunkXTotal - 1) / 2, 0);


        ArrayList<String>[] nameArrays = (ArrayList<String>[]) visitablePlayers.getColumn(1);
        for (int i = 0; i < nameArrays.length; i++)
        {
            //TODO
//            nameArrays[i].size()
        }
    }

    public int getChunkXMin()
    {
        return chunkXMin;
    }

    public int getChunkXMax()
    {
        return chunkXMax;
    }

    public int getChunkZMin(int chunkX)
    {
        return chunkZMins[chunkX - chunkXMin];
    }

    public int getChunkZMax(int chunkX)
    {
        return chunkZMaxes[chunkX - chunkXMin];
    }
}
