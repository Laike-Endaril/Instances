package com.fantasticsource.instances.world.dimensions.skyroom;

import com.fantasticsource.instances.world.InstanceHandler;
import com.fantasticsource.instances.world.InstanceWorldInfo;
import com.fantasticsource.instances.world.dimensions.InstanceTypes;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.gen.IChunkGenerator;

import javax.annotation.Nullable;
import java.io.File;

public class WorldProviderSkyroom extends WorldProvider
{
    private static final String name = InstanceTypes.skyroomDimType.getName().replaceAll(" ", "_");

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

        String name = "Unknown_" + InstanceTypes.skyroomDimType.getName();

        InstanceWorldInfo info = InstanceHandler.get(dim);
        if (info != null) name = info.getWorldName();

        return "instances" + File.separator + name + File.separator + name;
    }
}
