package com.fantasticsource.instances.blocksanditems.tileentities;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;

import java.util.UUID;

public class TEVisitorPortal extends TileEntity
{
    public UUID owner;

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        compound = super.writeToNBT(compound);

        compound.setTag("owner", new NBTTagString("" + owner));

        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);

        owner = UUID.fromString(compound.getString("owner"));
    }
}
