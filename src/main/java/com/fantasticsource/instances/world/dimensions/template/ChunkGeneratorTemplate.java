package com.fantasticsource.instances.world.dimensions.template;

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

/**
 * Note that "template" is a valid instance type.  This is not an explicit template for creating similar classes
 */
public class ChunkGeneratorTemplate implements IChunkGenerator
{
    protected World world;
    private ChunkPrimer chunkPrimer = new ChunkPrimer();

    public ChunkGeneratorTemplate(World worldIn)
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
            chunk.setBlockState(new BlockPos(0, 63, 0), Blocks.DIRT.getDefaultState());
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
