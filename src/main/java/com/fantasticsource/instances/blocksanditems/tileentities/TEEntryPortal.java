package com.fantasticsource.instances.blocksanditems.tileentities;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;

public class TEEntryPortal extends TileEntity
{
    public String instanceName;

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        compound = super.writeToNBT(compound);

        compound.setTag("instanceName", new NBTTagString(instanceName));

        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);

        instanceName = compound.getString("instanceName");
    }
}
