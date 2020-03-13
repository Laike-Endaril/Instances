package com.fantasticsource.instances.client;

import com.fantasticsource.instances.network.Network;
import com.fantasticsource.mctools.MCTools;
import net.minecraft.world.DimensionType;
import net.minecraftforge.common.DimensionManager;

import java.util.LinkedHashMap;
import java.util.Map;

public class LocalDimensions
{
    public static final LinkedHashMap<Integer, DimensionType> original = new LinkedHashMap<>();

    static
    {
        for (int dim : DimensionManager.getStaticDimensionIDs())
        {
            original.put(dim, DimensionManager.getProviderType(dim));
        }
    }

    public static void resetDimensionManager()
    {
        for (int dim : DimensionManager.getStaticDimensionIDs())
        {
            DimensionManager.unregisterDimension(dim);
        }

        for (Map.Entry<Integer, DimensionType> entry : original.entrySet())
        {
            DimensionManager.registerDimension(entry.getKey(), entry.getValue());
        }
    }

    public static void sync(Network.SyncDimensionTypePacket packet)
    {
        int dim = packet.dimension;
        if (DimensionManager.isDimensionRegistered(dim)) DimensionManager.unregisterDimension(dim);
        DimensionManager.registerDimension(dim, MCTools.getDimensionType(packet.dimensionTypeName));
    }
}
