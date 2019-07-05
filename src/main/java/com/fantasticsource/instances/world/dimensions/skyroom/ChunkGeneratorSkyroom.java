package com.fantasticsource.instances.world.dimensions.skyroom;

import com.fantasticsource.instances.blocksanditems.BlocksAndItems;
import com.fantasticsource.instances.world.boimes.BiomeVoid;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ChunkGeneratorSkyroom implements IChunkGenerator
{
    protected World world;
    private ChunkPrimer chunkPrimer = new ChunkPrimer();

    public ChunkGeneratorSkyroom(World worldIn)
    {
        world = worldIn;
    }

    @Override
    public Chunk generateChunk(int chunkX, int chunkZ)
    {
        Chunk chunk = new Chunk(world, chunkPrimer, chunkX, chunkZ);


        byte[] bytes = chunk.getBiomeArray();
        for (int i = 0; i < bytes.length; ++i)
        {
            bytes[i] = (byte) Biome.getIdForBiome(BiomeVoid.voidBiome);
        }


        if (chunkX >= -1 && chunkX <= 0 && chunkZ >= -1 && chunkZ <= 0)
        {
            int bottom = 75, top = 106;
            //Floor and ceiling
            for (int x = 0; x < 16; x++)
            {
                for (int z = 0; z < 16; z++)
                {
                    chunk.setBlockState(new BlockPos(x, bottom, z), Blocks.BEDROCK.getDefaultState());
                    chunk.setBlockState(new BlockPos(x, bottom + 1, z), Blocks.GRASS.getDefaultState());
                    chunk.setBlockState(new BlockPos(x, top, z), Blocks.BARRIER.getDefaultState());
                }
            }
            if (chunkX == -1 && chunkZ == -1)
            {
                chunk.setBlockState(new BlockPos(1, bottom, 1), BlocksAndItems.blockInstancePortal.getDefaultState());
            }

            //Walls
            if (chunkX == -1)
            {
                for (int y = bottom; y < top; y++)
                {
                    for (int z = 0; z < 16; z++)
                    {
                        chunk.setBlockState(new BlockPos(0, y, z), Blocks.BARRIER.getDefaultState());
                    }
                }
            }
            else
            {
                for (int y = bottom; y < top; y++)
                {
                    for (int z = 0; z < 16; z++)
                    {
                        chunk.setBlockState(new BlockPos(15, y, z), Blocks.BARRIER.getDefaultState());
                    }
                }
            }

            //Walls
            if (chunkZ == -1)
            {
                for (int y = bottom; y < top; y++)
                {
                    for (int x = 0; x < 16; x++)
                    {
                        chunk.setBlockState(new BlockPos(x, y, 0), Blocks.BARRIER.getDefaultState());
                    }
                }
            }
            else
            {
                for (int y = bottom; y < top; y++)
                {
                    for (int x = 0; x < 16; x++)
                    {
                        chunk.setBlockState(new BlockPos(x, y, 15), Blocks.BARRIER.getDefaultState());
                    }
                }
            }
        }


        chunk.generateSkylightMap();
        return chunk;
    }

    @Override
    public void populate(int parChunkX, int parChunkZ)
    {
    }

    @Override
    public boolean generateStructures(Chunk chunkIn, int x, int z)
    {
        return false;
    }

    @Override
    public List<Biome.SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos)
    {
        return new ArrayList<>();
    }

    @Override
    public boolean isInsideStructure(World worldIn, String structureName, BlockPos pos)
    {
        return false;
    }

    @Override
    @Nullable
    public BlockPos getNearestStructurePos(World worldIn, String structureName, BlockPos position, boolean findUnexplored)
    {
        return null;
    }

    @Override
    public void recreateStructures(Chunk chunkIn, int x, int z)
    {
    }
}
