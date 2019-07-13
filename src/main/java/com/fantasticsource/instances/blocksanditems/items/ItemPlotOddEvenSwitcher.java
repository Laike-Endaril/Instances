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
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class ItemPlotOddEvenSwitcher extends Item
{
    private BlockPos size, allowedExistingSizes[];

    public ItemPlotOddEvenSwitcher()
    {
        setCreativeTab(BlocksAndItems.creativeTab);

        setUnlocalizedName(Instances.MODID + ":plotoddeven");
        setRegistryName("plotoddeven");
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


        boolean existingIsEven = existingSize.getX() % 2 == 0;
        if (existingIsEven)
        {
            //Check blocks first; east and south will be removed; all blocks must be removed by player first, including floor layer
            for (int x = start.getX(); x <= end.getX(); x++)
            {
                for (int y = start.getY(); y <= end.getY(); y++)
                {
                    BlockPos pos = new BlockPos(x, y, end.getZ());
                    if (!world.isAirBlock(pos))
                    {
                        if (!world.isRemote)
                        {
                            player.sendMessage(new TextComponentString("Block detected at " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + " (" + world.getBlockState(pos).getBlock().getLocalizedName() + ")"));
                            player.sendMessage(new TextComponentString("The area to be removed must be clear of blocks! (The East and South sides)"));
                        }
                        return new ActionResult<>(EnumActionResult.FAIL, itemstack);
                    }
                }
            }
            for (int z = start.getZ(); z <= end.getZ(); z++)
            {
                for (int y = start.getY(); y <= end.getY(); y++)
                {
                    BlockPos pos = new BlockPos(end.getX(), y, z);
                    if (!world.isAirBlock(pos))
                    {
                        if (!world.isRemote)
                        {
                            player.sendMessage(new TextComponentString("Block detected at " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + " (" + world.getBlockState(pos).getBlock().getLocalizedName() + ")"));
                            player.sendMessage(new TextComponentString("The area to be removed must be clear of blocks! (The East and South sides)"));
                        }
                        return new ActionResult<>(EnumActionResult.FAIL, itemstack);
                    }
                }
            }
        }
        else
        {
            //Check portal addition space
            for (int y = 77; y <= 79; y++)
            {
                BlockPos pos = new BlockPos(0, y, -15);
                if (!world.isAirBlock(pos))
                {
                    if (!world.isRemote)
                    {
                        player.sendMessage(new TextComponentString("Block detected at " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + " (" + world.getBlockState(pos).getBlock().getLocalizedName() + ")"));
                        player.sendMessage(new TextComponentString("The portal expansion area must be clear of blocks! (Next to portal)"));
                    }
                    return new ActionResult<>(EnumActionResult.FAIL, itemstack);
                }
            }
        }


        if (!creative) itemstack.shrink(1);

        world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.BLOCK_ANVIL_USE, SoundCategory.NEUTRAL, 1, 0.9f + world.rand.nextFloat() * 0.1f);

        if (!world.isRemote)
        {
            if (existingIsEven)
            {
                //Switch to odd

                //Walls
                int oldX = end.getX() + 1, newX = oldX - 1;
                int oldZ = end.getZ() + 1, newZ = oldZ - 1;
                for (int x = start.getX() - 1; x <= oldX; x++)
                {
                    for (int y = start.getY() - 1; y <= end.getY() + 1; y++)
                    {
                        world.setBlockToAir(new BlockPos(x, y, oldZ));
                        world.setBlockState(new BlockPos(x, y, newZ), ChunkGeneratorSkyroom.BARRIER);
                    }
                }
                for (int z = start.getZ() - 1; z <= oldZ; z++)
                {
                    for (int y = start.getY() - 1; y <= end.getY() + 1; y++)
                    {
                        world.setBlockToAir(new BlockPos(oldX, y, z));
                        if (z != oldZ) world.setBlockState(new BlockPos(newX, y, z), ChunkGeneratorSkyroom.BARRIER);
                    }
                }

                //Portal
                for (int y = 77; y <= 79; y++)
                {
                    world.setBlockToAir(new BlockPos(0, y, -15));
                }
                BlockPos pos = new BlockPos(0, 76, -15);
                if (world.getBlockState(pos).getBlock() == Blocks.DIRT) world.setBlockState(pos, Blocks.GRASS.getDefaultState());
            }
            else
            {
                //Switch to even

                //Walls
                int oldX = end.getX() + 1, newX = oldX + 1;
                int oldZ = end.getZ() + 1, newZ = oldZ + 1;
                for (int x = start.getX(); x <= end.getX(); x++)
                {
                    for (int y = start.getY(); y <= end.getY(); y++)
                    {
                        world.setBlockToAir(new BlockPos(x, y, oldZ));
                        world.setBlockState(new BlockPos(x, y, newZ), ChunkGeneratorSkyroom.BARRIER);
                    }
                }
                for (int z = start.getZ(); z <= oldZ; z++)
                {
                    for (int y = start.getY() - 1; y <= end.getY(); y++)
                    {
                        world.setBlockToAir(new BlockPos(oldX, y, z));
                        world.setBlockState(new BlockPos(newX, y, z), ChunkGeneratorSkyroom.BARRIER);
                    }
                }
                for (int y = start.getY() - 1; y <= end.getY() + 1; y++)
                {
                    world.setBlockState(new BlockPos(start.getX() - 1, y, newZ), ChunkGeneratorSkyroom.BARRIER);
                    world.setBlockState(new BlockPos(oldX, y, newZ), ChunkGeneratorSkyroom.BARRIER);
                    world.setBlockState(new BlockPos(newX, y, newZ), ChunkGeneratorSkyroom.BARRIER);
                    world.setBlockState(new BlockPos(newX, y, oldZ), ChunkGeneratorSkyroom.BARRIER);
                    world.setBlockState(new BlockPos(newX, y, start.getZ() - 1), ChunkGeneratorSkyroom.BARRIER);
                }

                //Floor and ceiling
                for (int x = start.getX(); x <= oldX; x++)
                {
                    world.setBlockState(new BlockPos(x, start.getY() - 1, oldZ), ChunkGeneratorSkyroom.BEDROCK);
                    world.setBlockState(new BlockPos(x, start.getY() - 1, newZ), ChunkGeneratorSkyroom.BARRIER);
                    world.setBlockState(new BlockPos(x, end.getY() + 1, newZ), ChunkGeneratorSkyroom.BARRIER);
                    world.setBlockState(new BlockPos(x, end.getY() + 1, oldZ), ChunkGeneratorSkyroom.BARRIER);
                }
                for (int z = start.getZ(); z <= oldZ; z++)
                {
                    world.setBlockState(new BlockPos(oldX, start.getY() - 1, z), ChunkGeneratorSkyroom.BEDROCK);
                    world.setBlockState(new BlockPos(newX, start.getY() - 1, z), ChunkGeneratorSkyroom.BARRIER);
                    world.setBlockState(new BlockPos(newX, end.getY() + 1, z), ChunkGeneratorSkyroom.BARRIER);
                    world.setBlockState(new BlockPos(oldX, end.getY() + 1, z), ChunkGeneratorSkyroom.BARRIER);
                }

                //Portal
                for (int y = 77; y <= 79; y++)
                {
                    world.setBlockState(new BlockPos(0, y, -15), ChunkGeneratorSkyroom.PORTAL);
                }
            }
        }

        return new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn)
    {
        tooltip.add("Switches your plot from odd to even, or vice-versa");
    }
}
