package com.fantasticsource.instances.world.dimensions.libraryofworlds;

import com.fantasticsource.instances.world.InstanceHandler;
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
import java.util.UUID;

public class ChunkGeneratorLibraryOfWorlds implements IChunkGenerator
{
    private static final IBlockState AIR = Blocks.AIR.getDefaultState();
    protected World world;
    private ChunkPrimer chunkPrimer;

    public ChunkGeneratorLibraryOfWorlds(World world)
    {
        this.world = world;
        chunkPrimer = new ChunkPrimerLibraryOfWorlds();
    }

    @Override
    public Chunk generateChunk(int chunkX, int chunkZ)
    {
        Chunk chunk = new Chunk(world, chunkPrimer, chunkX, chunkZ);


        //Force void biome
        byte[] bytes = chunk.getBiomeArray();
        for (int i = 0; i < bytes.length; ++i)
        {
            bytes[i] = (byte) Biome.getIdForBiome(BiomeVoid.voidBiome);
        }


        //Clear bookshelves from main hall
        if (chunkZ == -1)
        {
            for (int x = 0; x < 16; x++)
            {
                for (int z = 11; z < 16; z++)
                {
                    for (int y = world.getHeight() - 1; y > 1; y--)
                    {
                        chunk.setBlockState(new BlockPos(x, y, z), AIR);
                    }
                }
            }
        }
        else if (chunkZ == 0)
        {
            for (int x = 0; x < 16; x++)
            {
                for (int z = 0; z < 5; z++)
                {
                    for (int y = world.getHeight() - 1; y > 1; y--)
                    {
                        chunk.setBlockState(new BlockPos(x, y, z), AIR);
                    }
                }
            }
        }

        UUID visitor = world.playerEntities.get(0).getPersistentID();
        LibraryOfWorldsChunkData chunkData = InstanceHandler.libraryOfWorldsData.getOrDefault(visitor, new LibraryOfWorldsChunkData());
        if (chunkX < chunkData.getChunkXMin() || chunkX > chunkData.getChunkXMax() || chunkZ < chunkData.getChunkZMin(chunkX) || chunkZ > chunkData.getChunkZMax(chunkX)) return chunk;


//        LinkedHashMap<String, ArrayList<String>> listings = new LinkedHashMap<>();
//        for (UUID id : ids)
//        {
//            String otherOwnerName = PlayerData.getName(id);
//            listings.computeIfAbsent(otherOwnerName.substring(0, 1), o -> new ArrayList<>()).add(otherOwnerName);
//        }
//
//
//        for (int x = 0; x < 16; x++)
//        {
//            for (int y = 0; y < world.getHeight(); y++)
//            {
//                for (int z = 0; z < 16; z++)
//                {
//                    chunk.setBlockState(new BlockPos(x, y, z), Blocks.AIR.getDefaultState());
//                }
//            }
//        }


        //8 Isles (letters) handled per chunkX value, starting with -1 and 0, then 1, then -2, 2, -3, etc
//        if (chunkX)
//        {
//        }

//        for (Map.Entry<String, ArrayList<String>> entry : listings.entrySet())
//        {
//            //Isles
//            for (String s : entry.getValue())
//            {
//                //Portals
//            }
//        }

        chunk.enqueueRelightChecks();
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
