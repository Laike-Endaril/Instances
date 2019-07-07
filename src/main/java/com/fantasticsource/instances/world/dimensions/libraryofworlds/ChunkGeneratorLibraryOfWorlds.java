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

    private static final TextComponentString
            HOUSE_STRING = new TextComponentString("House"),
            XXX_STRING = new TextComponentString("x-x-x-x-x-x-x-x-x-x-x");

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
            TileEntitySign sign;

            for (int i = 0; i < 4; i++)
            {
                //Signs
                BlockPos pos = new BlockPos(xx + (i << 2), 3, zz + 3);
                world.setBlockState(pos, SIGN.withProperty(BlockWallSign.FACING, EnumFacing.SOUTH));
                if (haveVisitables)
                {
                    sign = (TileEntitySign) world.getTileEntity(pos);
                    sign.signText[0] = XXX_STRING;
                    sign.signText[1] = text;
                    sign.signText[3] = XXX_STRING;
                }
                pos = pos.east(3);
                world.setBlockState(pos, SIGN.withProperty(BlockWallSign.FACING, EnumFacing.SOUTH));
                if (haveVisitables)
                {
                    sign = (TileEntitySign) world.getTileEntity(pos);
                    sign.signText[0] = XXX_STRING;
                    sign.signText[1] = text;
                    sign.signText[3] = XXX_STRING;
                }

                pos = pos.south(9);
                world.setBlockState(pos, SIGN);
                if (haveVisitables)
                {
                    sign = (TileEntitySign) world.getTileEntity(pos);
                    sign.signText[0] = XXX_STRING;
                    sign.signText[1] = text;
                    sign.signText[3] = XXX_STRING;
                }
                pos = pos.west(3);
                world.setBlockState(pos, SIGN);
                if (haveVisitables)
                {
                    sign = (TileEntitySign) world.getTileEntity(pos);
                    sign.signText[0] = XXX_STRING;
                    sign.signText[1] = text;
                    sign.signText[3] = XXX_STRING;
                }


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
            String name;
            int index;
            BlockPos portalPos, signPos;
            TileEntitySign sign;
            TextComponentString houseString = new TextComponentString("House");

            if (chunkZ < 0)
            {
                for (int i = 0; i < 4; i++)
                {
                    isleNames = (ArrayList<String>) chunkData.visitablePlayers.get(1, Tools.posMod((chunkX << 3) + (i << 1), indexLetters.length));

                    for (int z = 15; z > 1; z -= 2)
                    {
                        //Western
                        portalPos = new BlockPos(xx + (i << 2), 3, zz + z);
                        chunk.setBlockState(portalPos, PORTAL);
                        index = Tools.posMod(15 - z, isleNames.size());
                        name = isleNames.get(index);
                        ((TEInstancePortal) world.getTileEntity(portalPos)).destinations.add(new TEInstancePortal.Destination(name));
                        signPos = portalPos.add(1, 1, 0);
                        chunk.setBlockState(signPos, SIGN.withProperty(BlockWallSign.FACING, EnumFacing.EAST));
                        sign = (TileEntitySign) world.getTileEntity(signPos);
                        sign.signText[0] = XXX_STRING;
                        sign.signText[1] = new TextComponentString(name + "'s");
                        sign.signText[2] = HOUSE_STRING;
                        sign.signText[3] = XXX_STRING;

                        //Eastern
                        portalPos = portalPos.east(3);
                        chunk.setBlockState(portalPos, PORTAL);
                        index = Tools.posMod(++index, isleNames.size());
                        name = isleNames.get(index);
                        ((TEInstancePortal) world.getTileEntity(portalPos)).destinations.add(new TEInstancePortal.Destination(name));
                        signPos = portalPos.add(-1, 1, 0);
                        chunk.setBlockState(signPos, SIGN.withProperty(BlockWallSign.FACING, EnumFacing.WEST));
                        sign = (TileEntitySign) world.getTileEntity(signPos);
                        sign.signText[0] = XXX_STRING;
                        sign.signText[1] = new TextComponentString(name + "'s");
                        sign.signText[2] = HOUSE_STRING;
                        sign.signText[3] = XXX_STRING;
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
                        portalPos = new BlockPos(xx + (i << 2) + 3, 3, zz + z);
                        chunk.setBlockState(portalPos, PORTAL);
                        index = Tools.posMod(z, isleNames.size());
                        name = isleNames.get(index);
                        ((TEInstancePortal) world.getTileEntity(portalPos)).destinations.add(new TEInstancePortal.Destination(name));
                        signPos = portalPos.add(-1, 1, 0);
                        chunk.setBlockState(signPos, SIGN.withProperty(BlockWallSign.FACING, EnumFacing.WEST));
                        sign = (TileEntitySign) world.getTileEntity(signPos);
                        sign.signText[0] = XXX_STRING;
                        sign.signText[1] = new TextComponentString(name + "'s");
                        sign.signText[2] = HOUSE_STRING;
                        sign.signText[3] = XXX_STRING;

                        //Western
                        portalPos = portalPos.west(3);
                        chunk.setBlockState(portalPos, PORTAL);
                        index = Tools.posMod(++index, isleNames.size());
                        name = isleNames.get(index);
                        ((TEInstancePortal) world.getTileEntity(portalPos)).destinations.add(new TEInstancePortal.Destination(name));
                        signPos = portalPos.add(1, 1, 0);
                        chunk.setBlockState(signPos, SIGN.withProperty(BlockWallSign.FACING, EnumFacing.EAST));
                        sign = (TileEntitySign) world.getTileEntity(signPos);
                        sign.signText[0] = XXX_STRING;
                        sign.signText[1] = new TextComponentString(name + "'s");
                        sign.signText[2] = HOUSE_STRING;
                        sign.signText[3] = XXX_STRING;
                    }
                }
            }
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
