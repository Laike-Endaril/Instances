package com.fantasticsource.instances.world.dimensions.libraryofworlds;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.chunk.ChunkPrimer;

public class ChunkPrimerLibraryOfWorlds extends ChunkPrimer
{
    @Override
    public IBlockState getBlockState(int x, int y, int z)
    {
        return Blocks.BOOKSHELF.getDefaultState();
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
