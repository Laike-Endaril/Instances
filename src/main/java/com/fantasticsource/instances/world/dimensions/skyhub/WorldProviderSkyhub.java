package com.fantasticsource.instances.world.dimensions.skyhub;

import com.fantasticsource.instances.world.dimensions.InstanceTypes;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.gen.IChunkGenerator;

public class WorldProviderSkyhub extends WorldProvider
{
    @Override
    public DimensionType getDimensionType()
    {
        return InstanceTypes.skyhubDimType;
    }

    @Override
    public IChunkGenerator createChunkGenerator()
    {
        return new ChunkGeneratorSkyhub(world);
    }
}
