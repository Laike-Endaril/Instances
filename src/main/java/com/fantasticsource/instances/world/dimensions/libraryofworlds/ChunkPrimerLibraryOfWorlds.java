package com.fantasticsource.instances.world.dimensions.libraryofworlds;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.chunk.ChunkPrimer;

public class ChunkPrimerLibraryOfWorlds extends ChunkPrimer
{
    private static final IBlockState DEFAULT_STATE = Blocks.BOOKSHELF.getDefaultState();
    private final char[] data = new char[65536];

    private static int getBlockIndex(int x, int y, int z)
    {
        return x << 12 | z << 8 | y;
    }

    @Override
    public IBlockState getBlockState(int x, int y, int z)
    {
        return DEFAULT_STATE;
    }

    @Override
    public void setBlockState(int x, int y, int z, IBlockState state)
    {
        this.data[getBlockIndex(x, y, z)] = (char) Block.BLOCK_STATE_IDS.get(state);
    }

    @Override
    public int findGroundBlockIdx(int x, int z)
    {
        int i = (x << 12 | z << 8) + 256 - 1;

        for (int j = 255; j >= 0; --j)
        {
            IBlockState iblockstate = Block.BLOCK_STATE_IDS.getByValue(this.data[i + j]);
            if (iblockstate != null && iblockstate != DEFAULT_STATE) return j;
        }

        return 0;
    }
}