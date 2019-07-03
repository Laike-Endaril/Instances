package com.fantasticsource.instances.instancetypes.voided;

import com.fantasticsource.instances.instancetypes.InstanceTypes;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.gen.IChunkGenerator;

public class WorldProviderVoid extends WorldProvider
{
    @Override
    public DimensionType getDimensionType()
    {
        return InstanceTypes.voidDimType;
    }

    @Override
    public IChunkGenerator createChunkGenerator()
    {
        return new ChunkGeneratorVoid(world);
    }
}
