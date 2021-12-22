package com.fantasticsource.instances.world.dimensions.roomtile;

import com.fantasticsource.instances.world.InstanceWorldProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.IChunkGenerator;

public class WorldProviderRoomTile extends InstanceWorldProvider
{
    @Override
    protected void init()
    {
        super.init();
        setSpawnPoint(new BlockPos(8, 65, 2));
    }

    @Override
    public IChunkGenerator createChunkGenerator()
    {
        return new ChunkGeneratorRoomTile(world);
    }
}
