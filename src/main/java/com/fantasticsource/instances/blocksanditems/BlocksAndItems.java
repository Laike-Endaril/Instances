package com.fantasticsource.instances.blocksanditems;

import com.fantasticsource.instances.Instances;
import com.fantasticsource.instances.blocksanditems.blocks.BlockInstancePortal;
import com.fantasticsource.instances.blocksanditems.blocks.BlockPersonalPortal;
import com.fantasticsource.instances.blocksanditems.blocks.BlockReturnPortal;
import com.fantasticsource.instances.blocksanditems.blocks.BlockVisitorPortal;
import com.fantasticsource.instances.blocksanditems.items.ItemInstancePortal;
import com.fantasticsource.instances.blocksanditems.items.ItemPersonalPortal;
import com.fantasticsource.instances.blocksanditems.items.ItemReturnPortal;
import com.fantasticsource.instances.blocksanditems.items.ItemVisitorPortal;
import com.fantasticsource.instances.blocksanditems.tileentities.TEInstancePortal;
import com.fantasticsource.instances.blocksanditems.tileentities.TEVisitorPortal;
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

    @GameRegistry.ObjectHolder("instances:personalportal")
    public static BlockPersonalPortal blockPersonalPortal;
    @GameRegistry.ObjectHolder("instances:personalportal")
    public static Item itemPersonalPortal;

    @GameRegistry.ObjectHolder("instances:returnportal")
    public static BlockReturnPortal blockReturnPortal;
    @GameRegistry.ObjectHolder("instances:returnportal")
    public static Item itemReturnPortal;

    @GameRegistry.ObjectHolder("instances:visitorportal")
    public static BlockVisitorPortal blockVisitorPortal;
    @GameRegistry.ObjectHolder("instances:visitorportal")
    public static Item itemVisitorPortal;


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
    }

    @SubscribeEvent
    public static void modelRegistry(ModelRegistryEvent event)
    {
        ModelLoader.setCustomModelResourceLocation(itemInstancePortal, 0, new ModelResourceLocation("instances:instanceportal", "inventory"));
        ModelLoader.setCustomModelResourceLocation(itemPersonalPortal, 0, new ModelResourceLocation("instances:personalportal", "inventory"));
        ModelLoader.setCustomModelResourceLocation(itemReturnPortal, 0, new ModelResourceLocation("instances:returnportal", "inventory"));
        ModelLoader.setCustomModelResourceLocation(itemVisitorPortal, 0, new ModelResourceLocation("instances:visitorportal", "inventory"));
    }
}
