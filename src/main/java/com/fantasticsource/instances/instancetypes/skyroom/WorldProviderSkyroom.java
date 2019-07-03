package com.fantasticsource.instances.instancetypes.skyroom;

import com.fantasticsource.instances.instancetypes.InstanceTypes;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.gen.IChunkGenerator;

public class WorldProviderSkyroom extends WorldProvider
{
    @Override
    public DimensionType getDimensionType()
    {
        return InstanceTypes.skyroomDimType;
    }

    @Override
    public IChunkGenerator createChunkGenerator()
    {
        return new ChunkGeneratorSkyroom(world);
    }
}
