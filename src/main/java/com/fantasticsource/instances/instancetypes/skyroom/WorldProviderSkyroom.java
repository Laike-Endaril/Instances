package com.fantasticsource.instances.instancetypes.skyroom;

import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.gen.IChunkGenerator;

public class WorldProviderSkyroom extends WorldProvider
{
    @Override
    public DimensionType getDimensionType()
    {
        return WorldTypeSkyroom.skyroomDimType;
    }

    @Override
    public IChunkGenerator createChunkGenerator()
    {
        return new ChunkGeneratorSkyroom(world);
    }
}
