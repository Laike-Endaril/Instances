package com.fantasticsource.instances.blocksanditems.tileentity;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
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
        for (Destination destination : destinations)
        {
            NBTTagIntArray array = new NBTTagIntArray(new int[]{destination.dimension, destination.x, destination.y, destination.z, destination.facing});
            list.appendTag(array);
        }
        compound.setTag("destinations", list);

        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);

        for (NBTBase array : compound.getTagList("destinations", Constants.NBT.TAG_INT_ARRAY))
        {
            int[] ints = ((NBTTagIntArray) array).getIntArray();
            destinations.add(new Destination(ints[0], ints[1], ints[2], ints[3], ints[4]));
        }
    }


    public static class Destination
    {
        public int dimension, x, y, z, facing;

        public Destination(int dimension, int x, int y, int z, int facing)
        {
            this.dimension = dimension;
            this.x = x;
            this.y = y;
            this.z = z;
            this.facing = facing;
        }
    }
}
