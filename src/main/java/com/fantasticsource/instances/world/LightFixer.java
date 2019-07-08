package com.fantasticsource.instances.world;

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

public class LightFixer
{
    private static final boolean DEBUG = false, DEBUGLIGHTS = false; //These are verbose enough to cause lag due to logspam
    private static LinkedHashMap<Integer, LinkedHashMap<Chunk, Integer>> chunkQueue = new LinkedHashMap<>();

    @SubscribeEvent
    public static void worldUnload(WorldEvent.Unload event)
    {
        World world = event.getWorld();
        if (!world.isRemote) return;

        Minecraft.getMinecraft().addScheduledTask(() ->
        {
            chunkQueue.remove(world.provider.getDimension());
        });
    }

    @SubscribeEvent
    public static void chunkUnload(ChunkEvent.Unload event)
    {
        World world = event.getWorld();
        if (!world.isRemote) return;

        Minecraft.getMinecraft().addScheduledTask(() ->
        {
            LinkedHashMap<Chunk, Integer> chunks = chunkQueue.get(world.provider.getDimension());
            if (chunks == null) return;

            Chunk chunk = event.getChunk();
            chunks.remove(chunk);

            Chunk other;
            for (Map.Entry<Chunk, Integer> entry : chunks.entrySet())
            {
                other = entry.getKey();
                if (chunk.x == other.x - 1)
                {
                    if (chunk.z == other.z - 1) entry.setValue(entry.getValue() & 0b01111111);
                    else if (chunk.z == other.z) entry.setValue(entry.getValue() & 0b10111111);
                    else if (chunk.z == other.z + 1) entry.setValue(entry.getValue() & 0b11011111);
                }
                else if (chunk.x == other.x)
                {
                    if (chunk.z == other.z - 1) entry.setValue(entry.getValue() & 0b11101111);
                    else if (chunk.z == other.z + 1) entry.setValue(entry.getValue() & 0b11110111);
                }
                else if (chunk.x == other.x + 1)
                {
                    if (chunk.z == other.z - 1) entry.setValue(entry.getValue() & 0b11111011);
                    else if (chunk.z == other.z) entry.setValue(entry.getValue() & 0b11111101);
                    else if (chunk.z == other.z + 1) entry.setValue(entry.getValue() & 0b11111110);
                }
            }
        });
    }


    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void chunkLoad(ChunkEvent.Load event)
    {
        World world = event.getWorld();
        if (!world.isRemote) return;

        Minecraft.getMinecraft().addScheduledTask(() ->
        {
            Chunk chunk = event.getChunk();
            int dimension = world.provider.getDimension();
            LinkedHashMap<Chunk, Integer> chunks = chunkQueue.computeIfAbsent(dimension, o -> new LinkedHashMap<>());
            if (chunks.size() == 0)
            {
                if (DEBUG) System.out.println(chunk.x + ", " + chunk.z + " --- " + 0);
                chunks.put(chunk, 0);
                return;
            }


            IChunkProvider chunkProvider = world.getChunkProvider();
            int reverseBitIndex = 0, ready = 0, otherReady;
            Chunk other;

            int cx = chunk.x, cz = chunk.z;
            if (DEBUG) System.out.println("Starting " + cx + ", " + cz);
            for (int x = cx - 1; x <= cx + 1; x++)
            {
                for (int z = cz - 1; z <= cz + 1; z++)
                {
                    if (x == cx && z == cz) continue;

                    other = chunkProvider.getLoadedChunk(x, z);
                    if (other == null)
                    {
                        for (Chunk chunk1 : chunks.keySet())
                        {
                            if (chunk1.x == x && chunk1.z == z)
                            {
                                other = chunk1;
                                break;
                            }
                        }
                    }


                    if (other != null)
                    {
                        ready = (ready << 1) + 1;

                        otherReady = chunks.get(other) | (1 << reverseBitIndex);

                        if (DEBUG) System.out.println(x + ", " + z + " found (" + cx + ", " + cz + " -> " + Integer.toBinaryString(ready) + ") (" + x + ", " + z + " -> " + Integer.toBinaryString(otherReady) + ")");

                        if (otherReady == 0xFF)
                        {
                            updateChunk(world, other);
                            chunks.remove(chunk);
                        }
                        else chunks.put(other, otherReady);
                    }
                    else
                    {
                        ready = (ready << 1);
                        if (DEBUG) System.out.println(x + ", " + z + " not found (" + Integer.toBinaryString(ready) + ")");
                    }

                    reverseBitIndex++;
                }
            }

            if (ready == 0xFF)
            {
                updateChunk(world, chunk);
                if (chunks.size() == 0) chunkQueue.remove(dimension);
            }
            else
            {
                if (DEBUG) System.out.println(chunk.x + ", " + chunk.z + " === " + Integer.toBinaryString(ready));
                chunks.put(chunk, ready);
            }
        });
    }


    private static void updateChunk(World world, Chunk chunk)
    {
        Minecraft.getMinecraft().addScheduledTask(() ->
        {
            if (DEBUG) System.out.println("Fixing chunk " + chunk.x + ", " + chunk.z);
            int x1 = chunk.x << 4, z1 = chunk.z << 4, x2 = x1 + 15, z2 = z1 + 15;

            HashSet<BlockPos> positions = new HashSet<>(100);
            BlockPos pos;
            for (int x = x1; x <= x2; x++)
            {
                for (int z = z1; z <= z2; z++)
                {
                    for (int y = world.getHeight() - 1; y >= 0; y--)
                    {
                        pos = new BlockPos(x, y, z);
                        if (world.getBlockState(pos).getLightValue() > 0)
                        {
                            if (DEBUGLIGHTS) System.out.println("Updating light at " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ());
                            positions.add(pos);
                            positions.add(pos.up());
                            positions.add(pos.down());
                            positions.add(pos.north());
                            positions.add(pos.south());
                            positions.add(pos.west());
                            positions.add(pos.east());
                        }
                    }
                }
            }

            for (BlockPos pos1 : positions) world.checkLightFor(EnumSkyBlock.BLOCK, pos1);
        });
    }
}
