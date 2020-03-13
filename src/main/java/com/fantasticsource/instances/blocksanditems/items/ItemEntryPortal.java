package com.fantasticsource.instances.blocksanditems.items;

import com.fantasticsource.instances.Instances;
import com.fantasticsource.instances.blocksanditems.BlocksAndItems;
import net.minecraft.item.ItemBlock;

public class ItemEntryPortal extends ItemBlock
{
    public ItemEntryPortal()
    {
        super(BlocksAndItems.blockEntryPortal);

        setUnlocalizedName(Instances.MODID + ":entryportal");
        setRegistryName("entryportal");
    }
}
