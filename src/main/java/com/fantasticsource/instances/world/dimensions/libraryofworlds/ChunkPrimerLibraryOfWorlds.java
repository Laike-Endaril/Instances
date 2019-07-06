package com.fantasticsource.instances.world.dimensions.libraryofworlds;

import com.fantasticsource.tools.Tools;
import net.minecraft.block.BlockStoneBrick;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.chunk.ChunkPrimer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class ChunkPrimerLibraryOfWorlds extends ChunkPrimer
{
    //Each of these is blockstate, weight
    public static final LinkedHashMap<IBlockState, Integer> FLOORBLOCKS = new LinkedHashMap<>();

    static
    {
        FLOORBLOCKS.put(Blocks.STONEBRICK.getDefaultState(), 3);
        FLOORBLOCKS.put(Blocks.STONEBRICK.getDefaultState().withProperty(BlockStoneBrick.VARIANT, BlockStoneBrick.EnumType.CRACKED), 1);
    }


    private int worldHeight;

    private ArrayList<IBlockState> floorblocks = new ArrayList<>();


    public ChunkPrimerLibraryOfWorlds(int worldHeight)
    {
        this.worldHeight = worldHeight;

        for (Map.Entry<IBlockState, Integer> entry : FLOORBLOCKS.entrySet())
        {
            for (int i = entry.getValue(); i > 0; i--) floorblocks.add(entry.getKey());
        }
    }

    @Override
    public IBlockState getBlockState(int x, int y, int z)
    {
        if (y == 0) return Blocks.BEDROCK.getDefaultState();

        if (y == 1 || y == worldHeight - 1) return Tools.choose(floorblocks);

        if (((x - 1) >> 1) % 2 == 0) return Blocks.BOOKSHELF.getDefaultState();
        return Blocks.AIR.getDefaultState();
    }

    @Override
    public void setBlockState(int x, int y, int z, IBlockState state)
    {
    }

    @Override
    public int findGroundBlockIdx(int x, int z)
    {
        return 255;
    }
}
