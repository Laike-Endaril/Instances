package com.fantasticsource.instances.tags.entity;

import com.fantasticsource.fantasticlib.api.FLibAPI;
import com.fantasticsource.instances.Destination;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;

import static com.fantasticsource.fantasticlib.FantasticLib.MODID;

public class EscapePoint
{
    public static void setEscapePointToCurrentPosition(Entity entity)
    {
        setEscapePoint(entity, new Destination(entity.dimension, entity.posX, entity.posY, entity.posZ, entity.getRotationYawHead(), entity.rotationPitch));
    }

    public static void setEscapePoint(Entity entity, Destination destination)
    {
        NBTTagCompound compound = FLibAPI.getNBTCap(entity).getCompound(MODID);

        compound.setTag("escapePoint", new NBTTagCompound());
        compound = compound.getCompoundTag("escapePoint");

        compound.setInteger("dim", destination.dimension);

        compound.setDouble("x", destination.x);
        compound.setDouble("y", destination.y);
        compound.setDouble("z", destination.z);

        compound.setFloat("yaw", destination.yaw);
        compound.setFloat("pitch", destination.pitch);
    }

    public static Destination getEscapePoint(Entity entity)
    {
        NBTTagCompound compound = FLibAPI.getNBTCap(entity).getCompound(MODID);

        if (!compound.hasKey("escapePoint")) return null;
        compound = compound.getCompoundTag("escapePoint");

        return new Destination(compound.getInteger("dim"), compound.getDouble("x"), compound.getDouble("y"), compound.getDouble("z"), compound.getFloat("yaw"), compound.getFloat("pitch"));
    }
}
