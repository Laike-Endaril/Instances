package com.fantasticsource.instances.client;

import net.minecraft.world.DimensionType;
import net.minecraftforge.common.DimensionManager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class ClientHandler
{
    private static HashSet<Integer> instances = new HashSet<>();

    public static void cleanUp()
    {
        for (Integer i : instances)
        {
            if (DimensionManager.isDimensionRegistered(i))
            {
                DimensionManager.unregisterDimension(i);
            }
        }
    }

    public static void sync(HashMap<Integer, DimensionType> dimensions)
    {
        cleanUp();

        instances = new HashSet<>();
        instances.addAll(dimensions.keySet());

        for (Map.Entry<Integer, DimensionType> entry : dimensions.entrySet())
        {
            if (!DimensionManager.isDimensionRegistered(entry.getKey()))
            {
                DimensionManager.registerDimension(entry.getKey(), entry.getValue());
            }
        }
    }
}
