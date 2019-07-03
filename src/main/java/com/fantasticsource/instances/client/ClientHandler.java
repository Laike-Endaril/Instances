package com.fantasticsource.instances.client;

import net.minecraft.world.DimensionType;
import net.minecraftforge.common.DimensionManager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class ClientHandler
{
    private static HashSet<Integer> simpleDimensions = new HashSet<>();

    public static void cleanUp()
    {
        for (Integer i : simpleDimensions)
        {
            if (DimensionManager.isDimensionRegistered(i)) DimensionManager.unregisterDimension(i);
        }
    }

    public static void sync(HashMap<Integer, DimensionType> dimensions)
    {
        cleanUp();

        simpleDimensions = new HashSet<>();
        simpleDimensions.addAll(dimensions.keySet());

        for (Map.Entry<Integer, DimensionType> entry : dimensions.entrySet())
        {
            if (!DimensionManager.isDimensionRegistered(entry.getKey()))
            {
                DimensionManager.registerDimension(entry.getKey(), entry.getValue());
            }
        }
    }
}
