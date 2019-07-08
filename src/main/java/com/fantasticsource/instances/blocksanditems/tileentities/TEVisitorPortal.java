package com.fantasticsource.instances.blocksanditems.tileentities;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;

public class TEVisitorPortal extends TileEntity
{
    public String ownername;

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        compound = super.writeToNBT(compound);

        compound.setTag("ownername", new NBTTagString(ownername));

        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);

        ownername = compound.getString("ownername");
    }
}
