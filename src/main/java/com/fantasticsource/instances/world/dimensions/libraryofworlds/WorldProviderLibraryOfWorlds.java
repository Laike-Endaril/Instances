package com.fantasticsource.instances.world.dimensions.libraryofworlds;

import com.fantasticsource.instances.world.dimensions.InstanceTypes;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.gen.IChunkGenerator;

public class WorldProviderLibraryOfWorlds extends WorldProvider
{
    @Override
    public DimensionType getDimensionType()
    {
        return InstanceTypes.libraryOfWorldsDimType;
    }

    @Override
    public IChunkGenerator createChunkGenerator()
    {
        return new ChunkGeneratorLibraryOfWorlds(world);
    }

    @Override
    public boolean hasSkyLight()
    {
        return false;
    }
}
