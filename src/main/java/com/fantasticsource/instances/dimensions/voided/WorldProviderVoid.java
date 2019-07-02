package com.fantasticsource.instances.dimensions.voided;

import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.gen.IChunkGenerator;

public class WorldProviderVoid extends WorldProvider
{
    @Override
    public DimensionType getDimensionType()
    {
        return WorldTypeVoid.voidDimType;
    }

    @Override
    public IChunkGenerator createChunkGenerator()
    {
        return new ChunkGeneratorVoid(world);
    }
}
