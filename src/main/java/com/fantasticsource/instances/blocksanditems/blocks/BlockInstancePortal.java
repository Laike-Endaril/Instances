package com.fantasticsource.instances.blocksanditems.blocks;

import com.fantasticsource.instances.Instances;
import com.fantasticsource.instances.blocksanditems.BlocksAndItems;
import com.fantasticsource.instances.blocksanditems.tileentities.TEInstancePortal;
import com.fantasticsource.instances.server.Teleport;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class BlockInstancePortal extends Block implements ITileEntityProvider
{
    public BlockInstancePortal()
    {
        super(Material.ROCK);
        setSoundType(SoundType.STONE);

        setBlockUnbreakable();
        setResistance(Float.MAX_VALUE);

        setCreativeTab(BlocksAndItems.creativeTab);

        setUnlocalizedName(Instances.MODID + ":instanceportal");
        setRegistryName("instanceportal");
    }

    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }

    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer()
    {
        return BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        if (worldIn.isRemote) return true;

        TileEntity te = worldIn.getTileEntity(pos);
        if (!(te instanceof TEInstancePortal)) return false;

        TEInstancePortal portal = (TEInstancePortal) te;
        if (playerIn.isSneaking()) return false;

        if (portal.destinations.size() == 1) return Teleport.teleport(playerIn, portal.destinations.get(0));

        if (portal.destinations.size() == 0)
        {
            System.err.println("Behavior for no destination is not yet implemented (" + pos + ")");
            return false;
        }

        System.err.println("Behavior for more than one destination is not yet implemented (" + pos + ")");
        return false;
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta)
    {
        return new TEInstancePortal();
    }
}
