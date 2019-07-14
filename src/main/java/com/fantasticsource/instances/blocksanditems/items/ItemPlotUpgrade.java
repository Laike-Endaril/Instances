package com.fantasticsource.instances.blocksanditems.items;

import com.fantasticsource.instances.Instances;
import com.fantasticsource.instances.blocksanditems.BlocksAndItems;
import com.fantasticsource.instances.world.InstanceHandler;
import com.fantasticsource.instances.world.InstanceWorldInfo;
import com.fantasticsource.instances.world.dimensions.skyroom.ChunkGeneratorSkyroom;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class ItemPlotUpgrade extends Item
{
    private BlockPos size, allowedExistingSizes[];

    public ItemPlotUpgrade(BlockPos size, BlockPos... allowedExistingSizes)
    {
        //Base size is always even for x and z.  It automatically supports an additional odd size internally which is 1 less for x and z
        if (size.getX() % 2 != 0) size = size.east();
        if (size.getZ() % 2 != 0) size = size.south();

        for (BlockPos pos : allowedExistingSizes)
        {
            if (pos.getX() % 2 != 0) pos = pos.east();
            if (pos.getZ() % 2 != 0) pos = pos.south();

            if (size.equals(pos) || size.getX() < pos.getX() || size.getY() < pos.getY() || size.getZ() < pos.getZ()) throw new IllegalArgumentException("New plot size must be larger than any allowed existing size: " + sizeString(size) + " -> " + sizeString(pos));
        }


        this.size = size;

        this.allowedExistingSizes = allowedExistingSizes;

        setCreativeTab(BlocksAndItems.creativeTab);

        setUnlocalizedName(Instances.MODID + ":plotupgrade");
        setRegistryName("plotupgrade" + sizeString(size));
    }

    private String sizeString(BlockPos size)
    {
        return size.getX() + "x" + size.getY() + "x" + size.getZ();
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack)
    {
        return I18n.translateToLocalFormatted(getUnlocalizedName() + ".name", size.getX(), size.getY(), size.getZ()).trim();
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
    {
        ItemStack itemstack = player.getHeldItem(hand);

        boolean creative = player.capabilities.isCreativeMode;

        InstanceWorldInfo info = InstanceHandler.instanceInfo.get(player.dimension);
        if (info == null)
        {
            if (!world.isRemote)
            {
                if (creative) player.sendMessage(new TextComponentString("Plot changing items can only be used in personal instances!"));
                else player.sendMessage(new TextComponentString("Plot changing items can only be used in your own personal instances!"));
            }
            return new ActionResult<>(EnumActionResult.FAIL, itemstack);
        }

        if (!creative && !player.getPersistentID().equals(info.getOwner()))
        {
            if (!world.isRemote) player.sendMessage(new TextComponentString("Plot changing items can only be used in your own personal instances!"));
            return new ActionResult<>(EnumActionResult.FAIL, itemstack);
        }


        BlockPos start = new BlockPos(0, 75, 0);
        while (world.getBlockState(start).getBlock() == Blocks.BEDROCK) start = start.add(-1, 0, -1);
        while (world.getBlockState(start).getBlock() == Blocks.BARRIER) start = start.west();
        start = start.east();
        while (world.getBlockState(start).getBlock() == Blocks.BARRIER) start = start.north();
        start = start.add(1, 1, 2);

        BlockPos end = new BlockPos(0, 75, 0);
        while (world.getBlockState(end).getBlock() == Blocks.BEDROCK) end = end.add(1, 0, 1);
        while (world.getBlockState(end).getBlock() == Blocks.BARRIER) end = end.east();
        end = end.west();
        while (world.getBlockState(end).getBlock() == Blocks.BARRIER) end = end.south();
        end = end.north();
        while (world.getBlockState(end).getBlock() == Blocks.BARRIER) end = end.up();
        end = end.add(-1, -2, -1);


        BlockPos existingSize = end.subtract(start).add(1, 1, 1);


        boolean allow = creative;
        if (!allow)
        {
            for (BlockPos pos : allowedExistingSizes)
            {
                if (pos.equals(existingSize) || pos.equals(existingSize.add(-1, 0, -1)))
                {
                    allow = true;
                    break;
                }
            }
        }

        if (!allow)
        {
            if (!world.isRemote)
            {
                player.sendMessage(new TextComponentString("To use this upgrade, the existing plot size must be one of these:"));
                for (BlockPos pos : allowedExistingSizes)
                {
                    player.sendMessage(new TextComponentString(sizeString(pos)));
                    player.sendMessage(new TextComponentString(sizeString(pos.add(-1, 0, -1))));
                }
                player.sendMessage(new TextComponentString(""));
                player.sendMessage(new TextComponentString("The existing size of this plot is " + sizeString(existingSize)));
            }
            return new ActionResult<>(EnumActionResult.FAIL, itemstack);
        }


        if (!creative) itemstack.shrink(1);

        world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.BLOCK_ANVIL_USE, SoundCategory.NEUTRAL, 1, 0.9f + world.rand.nextFloat() * 0.1f);

        if (!world.isRemote)
        {
            int xDif = size.getX() - existingSize.getX();
            if (xDif % 2 != 0) xDif++;

            int zDif = size.getZ() - existingSize.getZ();
            if (zDif % 2 != 0) zDif++;

            int yDif = size.getY() - existingSize.getY();


            BlockPos newStart = start.add(-(xDif >> 1), 0, 0);
            BlockPos newEnd = end.add(xDif >> 1, yDif, zDif);


            //Remove existing walls / ceiling for the faces need to be moved
            if (xDif != 0)
            {
                removeFace(world, EnumFacing.WEST, start, end);
                removeFace(world, EnumFacing.EAST, start, end);
            }
            if (zDif != 0) removeFace(world, EnumFacing.SOUTH, start, end);
            if (yDif != 0) removeFace(world, EnumFacing.UP, start, end);


            //Recreate box
            int floor = newStart.getY();
            int bottom = floor - 1;
            int top = newEnd.getY() + 1;
            int west = newStart.getX() - 1;
            int east = newEnd.getX() + 1;
            int north = newStart.getZ() - 1;
            int south = newEnd.getZ() + 1;

            //Walls
            for (int y = bottom; y <= top; y++)
            {
                for (int x = west; x <= east; x++)
                {
                    world.setBlockState(new BlockPos(x, y, north), ChunkGeneratorSkyroom.BARRIER);
                    world.setBlockState(new BlockPos(x, y, south), ChunkGeneratorSkyroom.BARRIER);
                }
                for (int z = north + 1; z <= south - 1; z++)
                {
                    world.setBlockState(new BlockPos(west, y, z), ChunkGeneratorSkyroom.BARRIER);
                    world.setBlockState(new BlockPos(east, y, z), ChunkGeneratorSkyroom.BARRIER);
                }
            }

            //Floor and ceiling
            for (int x = west + 1; x <= east - 1; x++)
            {
                for (int z = north + 1; z <= south - 1; z++)
                {
                    world.setBlockState(new BlockPos(x, bottom, z), ChunkGeneratorSkyroom.BEDROCK);
                    world.setBlockState(new BlockPos(x, top, z), ChunkGeneratorSkyroom.BARRIER);
                    if (x < start.getX() || x > end.getX() || z < start.getZ() || z > end.getZ()) world.setBlockState(new BlockPos(x, floor, z), ChunkGeneratorSkyroom.FLOOR);
                }
            }
        }

        return new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
    }

    private void removeFace(World world, EnumFacing face, BlockPos start, BlockPos end)
    {
        int x, y, z;
        switch (face)
        {
            case WEST:
                x = start.getX() - 1;
                for (z = start.getZ() - 1; z <= end.getZ() + 1; z++)
                {
                    for (y = start.getY() - 1; y <= end.getY() + 1; y++)
                    {
                        world.setBlockToAir(new BlockPos(x, y, z));
                    }
                }
                break;

            case EAST:
                x = end.getX() + 1;
                for (z = start.getZ() - 1; z <= end.getZ() + 1; z++)
                {
                    for (y = start.getY() - 1; y <= end.getY() + 1; y++)
                    {
                        world.setBlockToAir(new BlockPos(x, y, z));
                    }
                }
                break;

            case SOUTH:
                z = end.getZ() + 1;
                for (x = start.getX() - 1; x <= end.getX() + 1; x++)
                {
                    for (y = start.getY() - 1; y <= end.getY() + 1; y++)
                    {
                        world.setBlockToAir(new BlockPos(x, y, z));
                    }
                }
                break;

            case UP:
                y = end.getY() + 1;
                for (x = start.getX() - 1; x <= end.getX() + 1; x++)
                {
                    for (z = start.getZ() - 1; z <= end.getZ() + 1; z++)
                    {
                        world.setBlockToAir(new BlockPos(x, y, z));
                    }
                }
                break;

            default:
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn)
    {
        tooltip.add("Upgrades a personal instance to " + sizeString(size));
        tooltip.add("");
        tooltip.add("Or in odd layout, to " + sizeString(size.add(-1, 0, -1)));
        tooltip.add("");
        tooltip.add("Requires that the existing size be one of these:");
        for (BlockPos pos : allowedExistingSizes)
        {
            tooltip.add(sizeString(pos));
            tooltip.add(sizeString(pos.add(-1, 0, -1)));
        }
    }
}
