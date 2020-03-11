package com.fantasticsource.instances.blocksanditems.tileentities;

import com.fantasticsource.instances.Destination;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;

public class TEInstancePortal extends TileEntity
{
    public ArrayList<Destination> destinations = new ArrayList<>();

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        compound = super.writeToNBT(compound);


        NBTTagList list = new NBTTagList();
        list.appendTag(new NBTTagInt(destinations.size()));

        for (Destination destination : destinations)
        {
            destination.addNBTTo(list);
        }
        compound.setTag("destinations", list);


        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);

        int dimension;
        double x, y, z;
        float yaw, pitch;

        NBTTagList list = compound.getTagList("destinations", Constants.NBT.TAG_INT);
        int count = ((NBTTagInt) list.get(0)).getInt();
        for (int i = 0; i < count; i++)
        {
            dimension = ((NBTTagInt) list.get(i * 6 + 1)).getInt();
            x = ((NBTTagDouble) list.get(i * 6 + 2)).getDouble();
            y = ((NBTTagDouble) list.get(i * 6 + 3)).getDouble();
            z = ((NBTTagDouble) list.get(i * 6 + 4)).getDouble();
            yaw = ((NBTTagDouble) list.get(i * 6 + 5)).getFloat();
            pitch = ((NBTTagDouble) list.get(i * 6 + 6)).getFloat();

            destinations.add(new Destination(dimension, x, y, z, yaw, pitch));
        }
    }
}
