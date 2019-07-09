package com.fantasticsource.instances.world.dimensions.skyroom;

import com.fantasticsource.instances.world.InstanceHandler;
import com.fantasticsource.instances.world.InstanceWorldInfo;
import com.fantasticsource.instances.world.dimensions.InstanceTypes;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.gen.IChunkGenerator;

import javax.annotation.Nullable;

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

    @Nullable
    @Override
    public String getSaveFolder()
    {
        int dim = getDimension();

        String name = "Unknown_" + getDimensionType().name();
        InstanceWorldInfo info = InstanceHandler.get(dim);
        if (info != null) name = info.getWorldName();

        return "./personal/" + name + "_" + dim;
    }
}
