package com.fantasticsource.instances.world;

import com.fantasticsource.instances.tags.world.Spawn;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.storage.WorldInfo;

public class InstanceWorldInfo extends WorldInfo
{
    public WorldInstance world = null;

    public InstanceWorldInfo(String name, WorldSettings settings)
    {
        super(settings, name);
    }

    /**
     * The world field is not yet set in the constructor, but it is set before this method executes
     */
    public void init()
    {
        BlockPos pos = Spawn.getSpawn(world);
        if (pos != null) setSpawn(pos);
    }

    @Override
    public void setSpawn(BlockPos spawnPoint)
    {
        super.setSpawn(spawnPoint);
        Spawn.setSpawn(world, spawnPoint);
    }

    @Override
    public int getSpawnX()
    {
        BlockPos pos = Spawn.getSpawn(world);
        if (pos != null)
        {
            setSpawn(pos);
            return pos.getX();
        }

        return super.getSpawnX();
    }

    @Override
    public int getSpawnY()
    {
        BlockPos pos = Spawn.getSpawn(world);
        if (pos != null)
        {
            setSpawn(pos);
            return pos.getY();
        }

        return super.getSpawnY();
    }

    @Override
    public int getSpawnZ()
    {
        BlockPos pos = Spawn.getSpawn(world);
        if (pos != null)
        {
            setSpawn(pos);
            return pos.getZ();
        }

        return super.getSpawnZ();
    }
}
