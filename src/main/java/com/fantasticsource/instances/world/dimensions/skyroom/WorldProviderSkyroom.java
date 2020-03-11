package com.fantasticsource.instances.world.dimensions.skyroom;

import com.fantasticsource.instances.world.InstanceWorldProvider;
import com.fantasticsource.instances.world.dimensions.InstanceTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.gen.IChunkGenerator;

public class WorldProviderSkyroom extends InstanceWorldProvider
{
    @Override
    protected void init()
    {
        super.init();
        setSpawnPoint(new BlockPos(0, 77, -14));
    }

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
