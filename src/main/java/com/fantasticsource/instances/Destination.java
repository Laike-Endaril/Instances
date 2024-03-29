package com.fantasticsource.instances;

import net.minecraft.nbt.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class Destination
{
    public int dimension;
    public double x, y, z;
    public float yaw, pitch;


    public Destination(World world, float yaw, float pitch)
    {
        this(world.provider.getDimension(), world.getWorldInfo().getSpawnX() + 0.5, world.getWorldInfo().getSpawnY(), world.getWorldInfo().getSpawnZ() + 0.5, yaw, pitch);
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

    public void addNBTTo(NBTTagList list)
    {
        List<NBTBase> tags = list.tagList;
        tags.add(new NBTTagDouble(dimension));
        tags.add(new NBTTagDouble(x));
        tags.add(new NBTTagDouble(y));
        tags.add(new NBTTagDouble(z));
        tags.add(new NBTTagDouble(yaw));
        tags.add(new NBTTagDouble(pitch));
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

    @Override
    public String toString()
    {
        return dimension + ", (" + x + ", " + y + ", " + z + "), (" + yaw + ", " + pitch + ")";
    }
}
