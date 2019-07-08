package com.fantasticsource.instances.blocksanditems.items;

import com.fantasticsource.instances.Instances;
import com.fantasticsource.instances.blocksanditems.BlocksAndItems;
import net.minecraft.item.ItemBlock;

public class ItemReturnPortal extends ItemBlock
{
    public ItemReturnPortal()
    {
        super(BlocksAndItems.blockReturnPortal);

        setUnlocalizedName(Instances.MODID + ":returnportal");
        setRegistryName("returnportal");
    }
}
