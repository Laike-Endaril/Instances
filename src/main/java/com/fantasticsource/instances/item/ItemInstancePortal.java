package com.fantasticsource.instances.item;

import com.fantasticsource.instances.BlocksAndItems;
import com.fantasticsource.instances.Instances;
import net.minecraft.item.ItemBlock;

public class ItemInstancePortal extends ItemBlock
{
    public ItemInstancePortal()
    {
        super(BlocksAndItems.blockInstancePortal);

        setUnlocalizedName(Instances.MODID + ":instancePortal");
        setRegistryName("instancePortal");
    }
}
