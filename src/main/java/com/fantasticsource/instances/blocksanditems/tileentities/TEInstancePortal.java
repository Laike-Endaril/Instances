package com.fantasticsource.instances.blocksanditems.tileentities;

import com.fantasticsource.instances.Destination;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
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
        NBTTagList list = new NBTTagList();
        list.appendTag(new NBTTagDouble(destinations.size()));
        for (Destination destination : destinations) destination.addNBTTo(list);
        getTileData().setTag("destinations", list);


        return super.writeToNBT(compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        compound = getTileData();


        destinations.clear();


        int dimension;
        double x, y, z;
        float yaw, pitch;

        NBTTagList list = compound.getTagList("destinations", Constants.NBT.TAG_DOUBLE);
        int count = Math.round((float) list.getDoubleAt(0));
        for (int i = 0; i < count; i++)
        {
            dimension = Math.round((float) list.getDoubleAt(i * 6 + 1));
            x = list.getDoubleAt(i * 6 + 2);
            y = list.getDoubleAt(i * 6 + 3);
            z = list.getDoubleAt(i * 6 + 4);
            yaw = (float) list.getDoubleAt(i * 6 + 5);
            pitch = (float) list.getDoubleAt(i * 6 + 6);

            destinations.add(new Destination(dimension, x, y, z, yaw, pitch));
        }
    }
}
