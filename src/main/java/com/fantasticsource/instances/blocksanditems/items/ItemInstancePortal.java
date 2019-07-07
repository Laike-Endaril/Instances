package com.fantasticsource.instances.blocksanditems.items;

import com.fantasticsource.instances.Instances;
import com.fantasticsource.instances.blocksanditems.BlocksAndItems;
import net.minecraft.item.ItemBlock;

public class ItemInstancePortal extends ItemBlock
{
    public ItemInstancePortal()
    {
        super(BlocksAndItems.blockInstancePortal);

        setUnlocalizedName(Instances.MODID + ":instanceportal");
        setRegistryName("instanceportal");
    }
}
