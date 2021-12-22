package com.fantasticsource.instances.world.dimensions.roomtile;

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

public class ChunkGeneratorRoomTile implements IChunkGenerator
{
    public static final int BOTTOM = 64, TOP = 79;

    public static final IBlockState
            STONE = Blocks.STONE.getDefaultState(),
            STONE_BRICK = Blocks.STONEBRICK.getDefaultState(),
            ESCAPE_PORTAL = BlocksAndItems.blockReturnPortal.getDefaultState(),
            GROUND_TORCH = Blocks.TORCH.getDefaultState();

    protected World world;
    private ChunkPrimer chunkPrimer = new ChunkPrimer();

    public ChunkGeneratorRoomTile(World worldIn)
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


        if (chunkX == 0 && chunkZ == 0)
        {
            //Floor and ceiling
            for (int x = 0; x < 16; x++)
            {
                for (int z = 0; z < 16; z++)
                {
                    chunk.setBlockState(new BlockPos(x, BOTTOM, z), STONE);
                    chunk.setBlockState(new BlockPos(x, TOP, z), STONE);
                }
            }


            //Walls
            for (int x = 0; x < 16; x++)
            {
                for (int y = BOTTOM + 1; y < TOP; y++)
                {
                    chunk.setBlockState(new BlockPos(x, y, 0), STONE_BRICK);
                    if (y > BOTTOM + 3 || x < 7 || x > 8) chunk.setBlockState(new BlockPos(x, y, 15), STONE_BRICK);
                    else chunk.setBlockState(new BlockPos(x, y, 1), ESCAPE_PORTAL);
                }
            }
            for (int z = 0; z < 16; z++)
            {
                for (int y = BOTTOM + 1; y < TOP; y++)
                {
                    chunk.setBlockState(new BlockPos(0, y, z), STONE_BRICK);
                    chunk.setBlockState(new BlockPos(15, y, z), STONE_BRICK);
                }
            }
        }


        chunk.generateSkylightMap();
        return chunk;
    }

    @Override
    public void populate(int chunkX, int chunkZ)
    {
        //Lights
        if (chunkX == 0 && chunkZ == 0)
        {
            Chunk chunk = world.getChunkFromChunkCoords(chunkX, chunkZ);
            chunk.setBlockState(new BlockPos(5, BOTTOM + 1, 5), GROUND_TORCH);
            chunk.setBlockState(new BlockPos(5, BOTTOM + 1, 10), GROUND_TORCH);
            chunk.setBlockState(new BlockPos(10, BOTTOM + 1, 5), GROUND_TORCH);
            chunk.setBlockState(new BlockPos(10, BOTTOM + 1, 10), GROUND_TORCH);
        }
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
