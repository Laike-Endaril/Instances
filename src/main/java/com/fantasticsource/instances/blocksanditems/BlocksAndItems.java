package com.fantasticsource.instances.blocksanditems;

import com.fantasticsource.instances.Instances;
import com.fantasticsource.instances.blocksanditems.blocks.BlockInstancePortal;
import com.fantasticsource.instances.blocksanditems.items.ItemInstancePortal;
import com.fantasticsource.instances.blocksanditems.tileentity.TEInstancePortal;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

public class BlocksAndItems
{
    @GameRegistry.ObjectHolder("instances:instanceportal")
    public static BlockInstancePortal blockInstancePortal;


    @GameRegistry.ObjectHolder("instances:instanceportal")
    public static Item itemInstancePortal;


    public static CreativeTabs creativeTab = new CreativeTabs(Instances.MODID)
    {
        @Override
        public ItemStack getTabIconItem()
        {
            return new ItemStack(blockInstancePortal);
        }

        @Override
        public void displayAllRelevantItems(NonNullList<ItemStack> p_78018_1_)
        {
            super.displayAllRelevantItems(p_78018_1_);
        }
    };


    @SubscribeEvent
    public static void blockRegistry(RegistryEvent.Register<Block> event)
    {
        IForgeRegistry<Block> registry = event.getRegistry();
        registry.register(new BlockInstancePortal());

        GameRegistry.registerTileEntity(TEInstancePortal.class, "instances:instanceportal");
    }

    @SubscribeEvent
    public static void itemRegistry(RegistryEvent.Register<Item> event)
    {
        IForgeRegistry<Item> registry = event.getRegistry();
        registry.register(new ItemInstancePortal());
    }

    @SubscribeEvent
    public static void modelRegistry(ModelRegistryEvent event)
    {
        ModelLoader.setCustomModelResourceLocation(itemInstancePortal, 0, new ModelResourceLocation("instances:instanceportal", "inventory"));
    }
}
