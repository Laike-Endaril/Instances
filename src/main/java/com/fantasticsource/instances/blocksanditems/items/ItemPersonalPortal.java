package com.fantasticsource.instances.blocksanditems.items;

import com.fantasticsource.instances.Instances;
import com.fantasticsource.instances.blocksanditems.BlocksAndItems;
import net.minecraft.item.ItemBlock;

public class ItemPersonalPortal extends ItemBlock
{
    public ItemPersonalPortal()
    {
        super(BlocksAndItems.blockPersonalPortal);

        setUnlocalizedName(Instances.MODID + ":personalportal");
        setRegistryName("personalportal");
    }
}
