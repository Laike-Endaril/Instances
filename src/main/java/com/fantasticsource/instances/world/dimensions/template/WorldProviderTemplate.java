package com.fantasticsource.instances.world.dimensions.template;

import com.fantasticsource.instances.world.InstanceHandler;
import com.fantasticsource.instances.world.InstanceWorldInfo;
import com.fantasticsource.instances.world.dimensions.InstanceTypes;
import com.fantasticsource.tools.Tools;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.Loader;

import javax.annotation.Nullable;
import java.io.File;
import java.util.UUID;

/**
 * Note that "template" is a valid instance type.  This is not an explicit template for creating similar classes
 */
public class WorldProviderTemplate extends WorldProvider
{
    private static final String TYPE_NAME = InstanceTypes.templateDimType.getName().replaceAll(" ", "_");
    private final boolean XAEROS;

    public WorldProviderTemplate()
    {
        XAEROS = Loader.isModLoaded("xaeroworldmap");
    }

    @Override
    public DimensionType getDimensionType()
    {
        return InstanceTypes.templateDimType;
    }

    @Override
    public IChunkGenerator createChunkGenerator()
    {
        return new ChunkGeneratorTemplate(world);
    }

    @Nullable
    @Override
    public String getSaveFolder()
    {
        //If you return null here, it saves to the overworld

        if (XAEROS && world.isRemote && Tools.stackContainsSubstring("xaero")) return "instancesInstance";

        int dim = getDimension();
        InstanceWorldInfo info = InstanceHandler.get(dim);
        if (info == null) return null;

        UUID owner = info.getOwner();
        return "instances" + File.separator + TYPE_NAME + File.separator + (owner != null ? owner : info.getWorldName());
    }
}