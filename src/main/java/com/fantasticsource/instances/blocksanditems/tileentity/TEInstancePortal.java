package com.fantasticsource.instances.blocksanditems.tileentity;

import net.minecraft.nbt.*;
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
        for (Destination destination : destinations)
        {
            NBTTagList list1 = new NBTTagList();

            list1.appendTag(new NBTTagInt(destination.dimension));
            list1.appendTag(new NBTTagDouble(destination.x));
            list1.appendTag(new NBTTagDouble(destination.y));
            list1.appendTag(new NBTTagDouble(destination.z));
            list1.appendTag(new NBTTagFloat(destination.yaw));
            list1.appendTag(new NBTTagFloat(destination.pitch));

            list.appendTag(list1);
        }
        compound.setTag("destinations", list);

        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);

        for (NBTBase element : compound.getTagList("destinations", Constants.NBT.TAG_INT_ARRAY))
        {
            NBTTagList list = (NBTTagList) element;

            int dimension = ((NBTTagInt) list.get(0)).getInt();
            double x = ((NBTTagDouble) list.get(1)).getDouble();
            double y = ((NBTTagDouble) list.get(2)).getDouble();
            double z = ((NBTTagDouble) list.get(3)).getDouble();
            float yaw = ((NBTTagFloat) list.get(4)).getFloat();
            float pitch = ((NBTTagFloat) list.get(5)).getFloat();

            destinations.add(new Destination(dimension, x, y, z, yaw, pitch));
        }
    }


    public static class Destination
    {
        //Option 1; coords
        public int dimension;
        public double x, y, z;
        public float yaw, pitch;

        //Option 2; player name (personal instance)
        public String ownerName = null;

        //Option 3; escape portal
        public boolean escape = false;


        public Destination()
        {
            escape = true;
        }

        public Destination(String ownerName)
        {
            this.ownerName = ownerName;
        }

        public Destination(int dimension, double x, double y, double z, float yaw, float pitch)
        {
            this.dimension = dimension;
            this.x = x;
            this.y = y;
            this.z = z;
            this.yaw = yaw;
            this.pitch = pitch;
        }
    }
}
