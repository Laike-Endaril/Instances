package com.fantasticsource.instances.world;

import net.minecraft.world.WorldSettings;
import net.minecraft.world.storage.WorldInfo;

public class InstanceWorldInfo extends WorldInfo
{
    public WorldInstance world = null;

    public InstanceWorldInfo(String name, WorldSettings settings)
    {
        super(settings, name);
    }
}
