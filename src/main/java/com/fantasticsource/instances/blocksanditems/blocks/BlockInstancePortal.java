package com.fantasticsource.instances.blocksanditems.blocks;

import com.fantasticsource.instances.Instances;
import com.fantasticsource.instances.blocksanditems.BlocksAndItems;
import com.fantasticsource.instances.blocksanditems.tileentity.TEInstancePortal;
import com.fantasticsource.instances.commands.CmdTPD;
import com.fantasticsource.instances.commands.Commands;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockInstancePortal extends Block
{
    public BlockInstancePortal()
    {
        super(Material.ROCK);
        setSoundType(SoundType.STONE);

        setBlockUnbreakable();
        setResistance(Float.MAX_VALUE);

        setCreativeTab(BlocksAndItems.creativeTab);

        setUnlocalizedName(Instances.MODID + ":instancePortal");
        setRegistryName("instancePortal");
    }


    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        if (!worldIn.isRemote)
        {
            TEInstancePortal portal = ((TEInstancePortal) worldIn.getTileEntity(pos));

            if (playerIn.isSneaking())
            {

            }
            else
            {
                if (portal.destinations.size() == 0)
                {
                    Commands.joinPossiblyCreating((EntityPlayerMP) playerIn, worldIn.getMinecraftServer());
                }
                else if (portal.destinations.size() == 1)
                {
                    CmdTPD.tpd(playerIn, portal.destinations.get(0));
                }
                else
                {
                    System.out.println("Behavior for more than one destination is not yet implemented (" + pos + ")");
                }
            }
        }

        return true;
    }

    @Override
    public boolean hasTileEntity(IBlockState state)
    {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state)
    {
        return new TEInstancePortal();
    }
}
