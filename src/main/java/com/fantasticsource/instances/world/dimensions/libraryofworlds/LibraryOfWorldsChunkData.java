package com.fantasticsource.instances.world.dimensions.libraryofworlds;

import com.fantasticsource.mctools.PlayerData;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.UUID;

public class LibraryOfWorldsChunkData
{
    private LinkedHashMap<Character, ArrayList<String>> visitablePlayers = new LinkedHashMap<>();
    private int chunkXMin = -1, chunkXMax = 0, chunkZMin = -1, chunkZMax = 0;

    public void add(UUID id)
    {
        int isles = visitablePlayers.size();

        String name = PlayerData.getName(id);
        visitablePlayers.computeIfAbsent(name.charAt(0), o -> new ArrayList<>()).add(name);

        if (visitablePlayers.size() != isles) recalc();
    }

    public void remove(UUID id)
    {
        int isles = visitablePlayers.size();

        String name = PlayerData.getName(id);
        char c = name.charAt(0);
        ArrayList<String> list = visitablePlayers.get(c);
        if (list == null) return;

        list.remove(name);
        if (list.size() == 0) visitablePlayers.remove(c);

        if (visitablePlayers.size() != isles) recalc();
    }

    public int size()
    {
        return visitablePlayers.size();
    }

    private void recalc()
    {
        //TODO recalc chunk minimums and maximums
    }

    public int getChunkXMin()
    {
        return chunkXMin;
    }

    public int getChunkXMax()
    {
        return chunkXMax;
    }

    public int getChunkZMin()
    {
        return chunkZMin;
    }

    public int getChunkZMax()
    {
        return chunkZMax;
    }
}
