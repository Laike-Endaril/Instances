package com.fantasticsource.instances.tags.world;

import com.fantasticsource.fantasticlib.api.FLibAPI;
import com.fantasticsource.instances.world.InstanceWorldInfo;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static com.fantasticsource.instances.Instances.MODID;

public class Spawn
{
    public static void setSpawn(World instance, BlockPos pos)
    {
        setSpawn(instance, pos.getX(), pos.getY(), pos.getZ());
    }

    public static void setSpawn(World instance, int x, int y, int z)
    {
        if (instance == null || !(instance.getWorldInfo() instanceof InstanceWorldInfo)) return;


        NBTTagCompound compound = FLibAPI.getNBTCap(instance).getCompound(MODID);

        if (!compound.hasKey("spawn")) compound.setTag("spawn", new NBTTagCompound());
        compound = compound.getCompoundTag("spawn");

        compound.setInteger("x", x);
        compound.setInteger("y", y);
        compound.setInteger("z", z);
    }

    public static BlockPos getSpawn(World instance)
    {
        if (!(instance.getWorldInfo() instanceof InstanceWorldInfo)) return null;


        NBTTagCompound compound = FLibAPI.getNBTCap(instance).getCompound(MODID);

        if (!compound.hasKey("spawn")) return null;
        compound = compound.getCompoundTag("spawn");

        return new BlockPos(compound.getInteger("x"), compound.getInteger("y"), compound.getInteger("z"));
    }
}
