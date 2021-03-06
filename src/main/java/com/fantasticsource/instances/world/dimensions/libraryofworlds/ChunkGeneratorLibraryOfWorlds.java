package com.fantasticsource.instances.world.dimensions.libraryofworlds;

import com.fantasticsource.instances.blocksanditems.BlocksAndItems;
import com.fantasticsource.instances.world.boimes.BiomeVoid;
import net.minecraft.block.BlockWallSign;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ChunkGeneratorLibraryOfWorlds implements IChunkGenerator
{
    private static final IBlockState
            AIR = Blocks.AIR.getDefaultState(),
            SIGN = Blocks.WALL_SIGN.getDefaultState(),
            PERSONAL_PORTAL = BlocksAndItems.blockPersonalPortal.getDefaultState(),
            RETURN_PORTAL = BlocksAndItems.blockReturnPortal.getDefaultState(),
            VISITOR_PORTAL = BlocksAndItems.blockEntryPortal.getDefaultState();

    private static final TextComponentString
            HOUSE_STRING = new TextComponentString("House"),
            XXX_STRING = new TextComponentString("x-x-x-x-x-x-x-x-x-x-x"),
            BLANK_STRING = new TextComponentString(""),
            ESCAPE_STRING = new TextComponentString("Escape");

    protected World world;
    private ChunkPrimer chunkPrimer;

    public ChunkGeneratorLibraryOfWorlds(World world)
    {
        this.world = world;

        //Don't save this world type
        if (world instanceof WorldServer) ((WorldServer) world).disableLevelSaving = true;

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


        EntityPlayer player = world.playerEntities.get(0);


        //Personal portals
        if (chunkZ == 0)
        {
            TextComponentString text;

            for (int i = 0; i < 4; i++)
            {
                //Personal portals and return portals
                BlockPos pos;

                text = new TextComponentString(player.getName() + "'s");
                pos = new BlockPos(xx + (i << 2), 3, zz + 1);
                chunk.setBlockState(pos, PERSONAL_PORTAL);
                createSign(chunk, pos.add(1, 1, 0), EnumFacing.EAST, XXX_STRING, text, HOUSE_STRING, XXX_STRING);
                pos = pos.east(3);
                chunk.setBlockState(pos, RETURN_PORTAL);
                createSign(chunk, pos.add(-1, 1, 0), EnumFacing.WEST, XXX_STRING, BLANK_STRING, ESCAPE_STRING, XXX_STRING);

                pos = pos.south(13);
                chunk.setBlockState(pos, PERSONAL_PORTAL);
                createSign(chunk, pos.add(-1, 1, 0), EnumFacing.WEST, XXX_STRING, text, HOUSE_STRING, XXX_STRING);
                pos = pos.west(3);
                chunk.setBlockState(pos, RETURN_PORTAL);
                createSign(chunk, pos.add(1, 1, 0), EnumFacing.EAST, XXX_STRING, BLANK_STRING, ESCAPE_STRING, XXX_STRING);
            }
        }
    }

    private void createSign(Chunk chunk, BlockPos pos, EnumFacing facing, TextComponentString... text)
    {
        chunk.setBlockState(pos, SIGN.withProperty(BlockWallSign.FACING, facing));
        TileEntitySign sign = (TileEntitySign) world.getTileEntity(pos);
        System.arraycopy(text, 0, sign.signText, 0, 4);
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
