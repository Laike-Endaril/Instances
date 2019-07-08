package com.fantasticsource.instances.world;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashSet;
import java.util.LinkedHashMap;

public class LightFixer
{
    private static LinkedHashMap<World, HashSet<BlockPos>> positions = new LinkedHashMap<>();

    public static void queue(World world, BlockPos pos)
    {
        positions.computeIfAbsent(world, o -> new HashSet<>()).add(pos);
    }

    @SubscribeEvent
    public static void chunkLoad(ChunkEvent.Load event)
    {
        World world = event.getWorld();
        update(world, true);
    }

    private static void update(World world, boolean removeOnUpdate)
    {
        for (World world1 : ((LinkedHashMap<World, HashSet<BlockPos>>) positions.clone()).keySet())
        {
            if (!DimensionManager.isDimensionRegistered(world.provider.getDimension())) positions.remove(world1);
        }

        HashSet<BlockPos> positions2 = positions.get(world);
        if (positions2 == null) return;

        IChunkProvider chunkProvider = world.getChunkProvider();

        Chunk chunk;
        for (BlockPos pos : (HashSet<BlockPos>) positions2.clone())
        {
            chunk = world.getChunkFromBlockCoords(pos);
            if (!chunkProvider.isChunkGeneratedAt(chunk.x - 1, chunk.z - 1)) continue;
            if (!chunkProvider.isChunkGeneratedAt(chunk.x - 1, chunk.z)) continue;
            if (!chunkProvider.isChunkGeneratedAt(chunk.x - 1, chunk.z + 1)) continue;

            if (!chunkProvider.isChunkGeneratedAt(chunk.x, chunk.z - 1)) continue;
            if (!chunkProvider.isChunkGeneratedAt(chunk.x, chunk.z + 1)) continue;

            if (!chunkProvider.isChunkGeneratedAt(chunk.x + 1, chunk.z - 1)) continue;
            if (!chunkProvider.isChunkGeneratedAt(chunk.x + 1, chunk.z)) continue;
            if (!chunkProvider.isChunkGeneratedAt(chunk.x + 1, chunk.z + 1)) continue;

            if (world.checkLight(pos) && removeOnUpdate) positions2.remove(pos);
        }

        if (positions2.size() == 0) positions.remove(world);
    }
}
