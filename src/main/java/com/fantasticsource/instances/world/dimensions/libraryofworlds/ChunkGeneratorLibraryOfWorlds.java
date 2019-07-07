package com.fantasticsource.instances.world.dimensions.libraryofworlds;

import com.fantasticsource.instances.blocksanditems.BlocksAndItems;
import com.fantasticsource.instances.blocksanditems.tileentity.TEInstancePortal;
import com.fantasticsource.instances.world.InstanceHandler;
import com.fantasticsource.instances.world.boimes.BiomeVoid;
import com.fantasticsource.tools.Tools;
import net.minecraft.block.BlockWallSign;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
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
    private static final IBlockState
            AIR = Blocks.AIR.getDefaultState(),
            SIGN = Blocks.WALL_SIGN.getDefaultState(),
            PORTAL = BlocksAndItems.blockInstancePortal.getDefaultState();

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
        if (chunkZ == 0)
        {
            for (int x = 0; x < 16; x++)
            {
                for (int z = 3; z < 13; z++)
                {
                    for (int y = world.getHeight() - 1; y > 1; y--)
                    {
                        chunk.setBlockState(new BlockPos(x, y, z), AIR);
                    }
                }
            }
        }

        return chunk;
    }

    @Override
    public void populate(int chunkX, int chunkZ)
    {
        int xx = chunkX << 4, zz = chunkZ << 4;
        Chunk chunk = world.getChunkFromChunkCoords(chunkX, chunkZ);


        UUID visitor = world.playerEntities.get(0).getPersistentID();
        LibraryOfWorldsChunkData chunkData = InstanceHandler.libraryOfWorldsData.getOrDefault(visitor, new LibraryOfWorldsChunkData());
        Object[] indexLetters = chunkData.visitablePlayers.getColumn(0);

        boolean haveVisitables = indexLetters.length > 0;


        //Isle Signs (in index order) and personal portals
        if (chunkZ == 0)
        {
            TextComponentString text = null;

            for (int i = 0; i < 4; i++)
            {
                //Signs
                BlockPos pos = new BlockPos(xx + (i << 2), 3, zz + 3);
                world.setBlockState(pos, SIGN.withProperty(BlockWallSign.FACING, EnumFacing.SOUTH));
                if (haveVisitables)
                {
                    text = new TextComponentString("" + indexLetters[Tools.posMod((chunkX << 3) + (i << 1), indexLetters.length)]);
                    ((TileEntitySign) world.getTileEntity(pos)).signText[1] = text;
                }
                pos = pos.east(3);
                world.setBlockState(pos, SIGN.withProperty(BlockWallSign.FACING, EnumFacing.SOUTH));
                if (haveVisitables) ((TileEntitySign) world.getTileEntity(pos)).signText[1] = text;

                pos = pos.south(9);
                world.setBlockState(pos, SIGN);
                if (haveVisitables)
                {
                    text = new TextComponentString("" + indexLetters[Tools.posMod((chunkX << 3) + (i << 1) + 1, indexLetters.length)]);
                    ((TileEntitySign) world.getTileEntity(pos)).signText[1] = text;
                }
                pos = pos.west(3);
                world.setBlockState(pos, SIGN);
                if (haveVisitables) ((TileEntitySign) world.getTileEntity(pos)).signText[1] = text;


                //Personal portals
                pos = new BlockPos(xx + (i << 2), 3, zz + 1);
                chunk.setBlockState(pos, PORTAL);
                pos = pos.east(3);
                chunk.setBlockState(pos, PORTAL);
                ((TEInstancePortal) world.getTileEntity(pos)).destinations.add(new TEInstancePortal.Destination());

                pos = pos.south(13);
                chunk.setBlockState(pos, PORTAL);
                pos = pos.west(3);
                chunk.setBlockState(pos, PORTAL);
                ((TEInstancePortal) world.getTileEntity(pos)).destinations.add(new TEInstancePortal.Destination());
            }
        }
        else if (haveVisitables)
        {
            //Visitable portals

            ArrayList<String> isleNames;

            if (chunkZ < 0)
            {
                for (int i = 0; i < 4; i++)
                {
                    isleNames = (ArrayList<String>) chunkData.visitablePlayers.get(1, Tools.posMod((chunkX << 3) + (i << 1), indexLetters.length));

                    for (int z = 15; z > 1; z -= 2)
                    {
                        //Western
                        BlockPos pos = new BlockPos(xx + (i << 2), 3, zz + z);
                        chunk.setBlockState(pos, PORTAL);
                        int index = Tools.posMod(15 - (z >> 1), isleNames.size());
                        ((TEInstancePortal) world.getTileEntity(pos)).destinations.add(new TEInstancePortal.Destination(isleNames.get(index)));

                        //Eastern
                        pos = pos.east(3);
                        chunk.setBlockState(pos, PORTAL);
                        index = Tools.posMod(++index, isleNames.size());
                        ((TEInstancePortal) world.getTileEntity(pos)).destinations.add(new TEInstancePortal.Destination(isleNames.get(index)));
                    }
                }
            }
            else
            {
                for (int i = 0; i < 4; i++)
                {
                    isleNames = (ArrayList<String>) chunkData.visitablePlayers.get(1, Tools.posMod((chunkX << 3) + (i << 1), indexLetters.length));

                    for (int z = 0; z < 14; z += 2)
                    {
                        //Eastern
                        BlockPos pos = new BlockPos(xx + (i << 2) + 3, 3, zz + z);
                        chunk.setBlockState(pos, PORTAL);
                        int index = Tools.posMod(z >> 1, isleNames.size());
                        ((TEInstancePortal) world.getTileEntity(pos)).destinations.add(new TEInstancePortal.Destination(isleNames.get(index)));

                        //Western
                        pos = pos.west(3);
                        chunk.setBlockState(pos, PORTAL);
                        index = Tools.posMod(++index, isleNames.size());
                        ((TEInstancePortal) world.getTileEntity(pos)).destinations.add(new TEInstancePortal.Destination(isleNames.get(index)));
                    }
                }
            }
        }


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
