package com.fantasticsource.instances.world.dimensions.skyroom;

import com.fantasticsource.instances.blocksanditems.BlocksAndItems;
import com.fantasticsource.instances.world.boimes.BiomeVoid;
import net.minecraft.block.state.IBlockState;
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
    public static final IBlockState
            PORTAL = BlocksAndItems.blockPersonalPortal.getDefaultState(),
            BARRIER = Blocks.BARRIER.getDefaultState(),
            BEDROCK = Blocks.BEDROCK.getDefaultState(),
            FLOOR = Blocks.GRASS.getDefaultState();

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
                    chunk.setBlockState(new BlockPos(x, bottom, z), BEDROCK);
                    chunk.setBlockState(new BlockPos(x, bottom + 1, z), FLOOR);
                    chunk.setBlockState(new BlockPos(x, top, z), BARRIER);
                }
            }


            //Walls
            if (chunkX == -1)
            {
                for (int y = bottom; y < top; y++)
                {
                    for (int z = 0; z < 16; z++)
                    {
                        chunk.setBlockState(new BlockPos(0, y, z), BARRIER);
                    }
                }
            }
            else
            {
                for (int y = bottom; y < top; y++)
                {
                    for (int z = 0; z < 16; z++)
                    {
                        chunk.setBlockState(new BlockPos(15, y, z), BARRIER);
                    }
                }
            }
            if (chunkZ == -1)
            {
                for (int y = bottom; y < top; y++)
                {
                    for (int x = 0; x < 16; x++)
                    {
                        chunk.setBlockState(new BlockPos(x, y, 0), BARRIER);
                    }
                }
            }
            else
            {
                for (int y = bottom; y < top; y++)
                {
                    for (int x = 0; x < 16; x++)
                    {
                        chunk.setBlockState(new BlockPos(x, y, 15), BARRIER);
                    }
                }
            }


            //Portal
            if (chunkZ == -1)
            {
                if (chunkX == -1)
                {
                    chunk.setBlockState(new BlockPos(15, bottom + 2, 1), PORTAL);
                    chunk.setBlockState(new BlockPos(15, bottom + 3, 1), PORTAL);
                    chunk.setBlockState(new BlockPos(15, bottom + 4, 1), PORTAL);
                }
                else
                {
                    chunk.setBlockState(new BlockPos(0, bottom + 2, 1), PORTAL);
                    chunk.setBlockState(new BlockPos(0, bottom + 3, 1), PORTAL);
                    chunk.setBlockState(new BlockPos(0, bottom + 4, 1), PORTAL);
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
