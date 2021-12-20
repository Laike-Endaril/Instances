package com.fantasticsource.instances.blocksanditems.tileentities;

import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;

public class TEEntryPortal extends TileEntity
{
    public TEEntryPortal()
    {
        getTileData().setTag("instanceName", new NBTTagString(""));
    }

    public String getInstanceName()
    {
        return getTileData().getString("instanceName");
    }
}
