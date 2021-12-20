package com.fantasticsource.instances.blocksanditems.tileentities;

import com.fantasticsource.instances.Destination;
import net.minecraft.nbt.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;

public class TEInstancePortal extends TileEntity
{
    public TEInstancePortal()
    {
        getTileData().setTag("destinations", new NBTTagList());
    }

    public TEInstancePortal(int dimension)
    {
        NBTTagList list = new NBTTagList();
        getTileData().setTag("destinations", list);

        NBTTagCompound compound = new NBTTagCompound();
        list.appendTag(compound);

        compound.setTag("dim", new NBTTagInt(dimension));
        compound.setTag("xOffset", new NBTTagDouble(0.5));
        compound.setTag("yOffset", new NBTTagDouble(5));
        compound.setTag("zOffset", new NBTTagDouble(0.5));
        compound.setTag("yaw", new NBTTagFloat(0));
        compound.setTag("pitch", new NBTTagFloat(0));
    }

    public ArrayList<Destination> getPossibleDestinations()
    {
        ArrayList<Destination> result = new ArrayList<>();
        NBTTagList list = getTileData().getTagList("destinations", Constants.NBT.TAG_COMPOUND);
        int count = list.tagCount(), x = pos.getX(), y = pos.getY(), z = pos.getZ();
        for (int i = 0; i < count; i++)
        {
            NBTTagCompound compound = list.getCompoundTagAt(i);
            result.add(new Destination(compound.getInteger("dim"), x + compound.getDouble("xOffset"), y + compound.getDouble("yOffset"), z + compound.getDouble("zOffset"), compound.getFloat("yaw"), compound.getFloat("pitch")));
        }
        return result;
    }
}
