package com.fantasticsource.instances.world.dimensions.template;

import com.fantasticsource.instances.world.InstanceWorldProvider;
import com.fantasticsource.instances.world.dimensions.InstanceTypes;
import net.minecraft.world.DimensionType;
import net.minecraft.world.gen.IChunkGenerator;

/**
 * Note that "template" is a valid instance type.  This is not an explicit template for creating similar classes
 */
public class WorldProviderTemplate extends InstanceWorldProvider
{
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
}
