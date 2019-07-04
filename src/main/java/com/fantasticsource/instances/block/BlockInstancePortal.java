package com.fantasticsource.instances.block;

import com.fantasticsource.instances.BlocksAndItems;
import com.fantasticsource.instances.Instances;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockInstancePortal extends Block
{
    public BlockInstancePortal()
    {
        super(Material.ROCK);
        setUnlocalizedName(Instances.MODID + ":instancePortal");
        setSoundType(SoundType.GLASS);
        setCreativeTab(BlocksAndItems.creativeTab);

        setRegistryName("instancePortal");
    }


    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        return false;
    }
}
