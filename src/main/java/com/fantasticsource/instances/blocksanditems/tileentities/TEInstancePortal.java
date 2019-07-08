package com.fantasticsource.instances.blocksanditems.tileentities;

import net.minecraft.nbt.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.List;

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


    public static class Destination
    {
        public int dimension;
        public double x, y, z;
        public float yaw, pitch;


        public Destination(int dimension, double x, double y, double z, float yaw, float pitch)
        {
            this.dimension = dimension;
            this.x = x;
            this.y = y;
            this.z = z;
            this.yaw = yaw;
            this.pitch = pitch;
        }

        public void addNBTTo(NBTTagList list)
        {
            List<NBTBase> tags = list.tagList;
            tags.add(new NBTTagInt(dimension));
            tags.add(new NBTTagDouble(x));
            tags.add(new NBTTagDouble(y));
            tags.add(new NBTTagDouble(z));
            tags.add(new NBTTagFloat(yaw));
            tags.add(new NBTTagFloat(pitch));
        }

        public Destination setDimension(int dimension)
        {
            this.dimension = dimension;
            return this;
        }

        public Destination setPosition(double x, double y, double z)
        {
            this.x = x;
            this.y = y;
            this.z = z;
            return this;
        }

        public Destination setPosition(BlockPos pos)
        {
            return setPosition(pos.getX(), pos.getY(), pos.getZ());
        }
    }
}
