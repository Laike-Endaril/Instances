package com.fantasticsource.instances.world.dimensions.template;

import com.fantasticsource.instances.world.InstanceWorldProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.IChunkGenerator;

/**
 * Note that "template" is a valid instance type.  This is not an explicit template for creating similar classes
 */
public class WorldProviderTemplate extends InstanceWorldProvider
{
    @Override
    protected void init()
    {
        super.init();
        setSpawnPoint(new BlockPos(0, 64, 0));
    }

    @Override
    public IChunkGenerator createChunkGenerator()
    {
        return new ChunkGeneratorTemplate(world);
    }
}
