package com.fantasticsource.instances.blocksanditems;

import com.fantasticsource.instances.Instances;
import com.fantasticsource.instances.blocksanditems.blocks.BlockInstancePortal;
import com.fantasticsource.instances.blocksanditems.blocks.BlockPersonalPortal;
import com.fantasticsource.instances.blocksanditems.blocks.BlockReturnPortal;
import com.fantasticsource.instances.blocksanditems.blocks.BlockVisitorPortal;
import com.fantasticsource.instances.blocksanditems.items.*;
import com.fantasticsource.instances.blocksanditems.tileentities.TEInstancePortal;
import com.fantasticsource.instances.blocksanditems.tileentities.TEVisitorPortal;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.ArrayList;

public class BlocksAndItems
{
    @GameRegistry.ObjectHolder("instances:instanceportal")
    public static BlockInstancePortal blockInstancePortal;
    @GameRegistry.ObjectHolder("instances:instanceportal")
    public static ItemInstancePortal itemInstancePortal;

    @GameRegistry.ObjectHolder("instances:personalportal")
    public static BlockPersonalPortal blockPersonalPortal;
    @GameRegistry.ObjectHolder("instances:personalportal")
    public static ItemPersonalPortal itemPersonalPortal;

    @GameRegistry.ObjectHolder("instances:returnportal")
    public static BlockReturnPortal blockReturnPortal;
    @GameRegistry.ObjectHolder("instances:returnportal")
    public static ItemReturnPortal itemReturnPortal;

    @GameRegistry.ObjectHolder("instances:visitorportal")
    public static BlockVisitorPortal blockVisitorPortal;
    @GameRegistry.ObjectHolder("instances:visitorportal")
    public static ItemVisitorPortal itemVisitorPortal;


    public static CreativeTabs creativeTab = new CreativeTabs(Instances.MODID)
    {
        @Override
        public ItemStack getTabIconItem()
        {
            return new ItemStack(blockPersonalPortal);
        }

        @Override
        public void displayAllRelevantItems(NonNullList<ItemStack> itemStacks)
        {
            super.displayAllRelevantItems(itemStacks);
        }
    };

    public static ArrayList<ItemPlotUpgrade> plotUpgrades = new ArrayList<>();

    @GameRegistry.ObjectHolder("instances:plotoddeven")
    private static ItemPlotOddEvenSwitcher oddEvenSwitcher;

    @SubscribeEvent
    public static void blockRegistry(RegistryEvent.Register<Block> event)
    {
        IForgeRegistry<Block> registry = event.getRegistry();

        registry.register(new BlockInstancePortal());
        registry.register(new BlockPersonalPortal());
        registry.register(new BlockReturnPortal());
        registry.register(new BlockVisitorPortal());

        GameRegistry.registerTileEntity(TEInstancePortal.class, "instances:instanceportal");
        GameRegistry.registerTileEntity(TEVisitorPortal.class, "instances:visitorportal");
    }

    @SubscribeEvent
    public static void itemRegistry(RegistryEvent.Register<Item> event)
    {
        IForgeRegistry<Item> registry = event.getRegistry();
        registry.register(new ItemInstancePortal());
        registry.register(new ItemPersonalPortal());
        registry.register(new ItemReturnPortal());
        registry.register(new ItemVisitorPortal());

        plotUpgrades.add(new ItemPlotUpgrade(new BlockPos(30, 38, 46), new BlockPos(30, 30, 30)));
        plotUpgrades.add(new ItemPlotUpgrade(new BlockPos(46, 46, 46), new BlockPos(30, 38, 46)));
        for (ItemPlotUpgrade item : plotUpgrades) registry.register(item);

        registry.register(new ItemPlotOddEvenSwitcher());
    }

    @SubscribeEvent
    public static void modelRegistry(ModelRegistryEvent event)
    {
        ModelLoader.setCustomModelResourceLocation(itemInstancePortal, 0, new ModelResourceLocation("instances:instanceportal", "inventory"));
        ModelLoader.setCustomModelResourceLocation(itemPersonalPortal, 0, new ModelResourceLocation("instances:personalportal", "inventory"));
        ModelLoader.setCustomModelResourceLocation(itemReturnPortal, 0, new ModelResourceLocation("instances:returnportal", "inventory"));
        ModelLoader.setCustomModelResourceLocation(itemVisitorPortal, 0, new ModelResourceLocation("instances:visitorportal", "inventory"));

        for (ItemPlotUpgrade item : plotUpgrades)
        {
            ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation("instances:scroll", "inventory"));
        }

        ModelLoader.setCustomModelResourceLocation(oddEvenSwitcher, 0, new ModelResourceLocation("instances:scroll", "inventory"));
    }
}
