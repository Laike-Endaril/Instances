package com.fantasticsource.instances.world.dimensions.libraryofworlds;

import com.fantasticsource.mctools.PlayerData;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.UUID;

public class LibraryOfWorldsChunkData
{
    private LinkedHashMap<Character, ArrayList<String>> visitablePlayers = new LinkedHashMap<>();

    public void add(UUID id)
    {
        String name = PlayerData.getName(id);
        visitablePlayers.computeIfAbsent(name.charAt(0), o -> new ArrayList<>()).add(name);
    }

    public void remove(UUID id)
    {
        String name = PlayerData.getName(id);
        char c = name.charAt(0);
        ArrayList<String> list = visitablePlayers.get(c);
        if (list == null) return;

        list.remove(name);
        if (list.size() == 0) visitablePlayers.remove(c);
    }

    public int size()
    {
        return visitablePlayers.size();
    }
}
