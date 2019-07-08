package com.fantasticsource.instances.blocksanditems.items;

import com.fantasticsource.instances.Instances;
import com.fantasticsource.instances.blocksanditems.BlocksAndItems;
import net.minecraft.item.ItemBlock;

public class ItemVisitorPortal extends ItemBlock
{
    public ItemVisitorPortal()
    {
        super(BlocksAndItems.blockVisitorPortal);

        setUnlocalizedName(Instances.MODID + ":visitorportal");
        setRegistryName("visitorportal");
    }
}
