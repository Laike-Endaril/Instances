package com.fantasticsource.instances.world;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.storage.WorldInfo;

import javax.annotation.Nullable;
import java.util.UUID;

public class WorldInfoSimple extends WorldInfo
{
    private DimensionType dimensionType = DimensionType.OVERWORLD;
    private UUID owner = null;

    public WorldInfoSimple(NBTTagCompound nbt)
    {
        super(nbt);

        try
        {
            dimensionType = DimensionType.byName(nbt.getString("dimType"));
        }
        catch (IllegalArgumentException e)
        {
            //Just keep default value from above
        }

        try
        {
            owner = UUID.fromString(nbt.getString("owner"));
        }
        catch (IllegalArgumentException e)
        {
            //Just keep default value from above
        }
    }

    public WorldInfoSimple(WorldSettings settings, String name, DimensionType dimType)
    {
        super(settings, name);
        dimensionType = dimType != null ? dimType : DimensionType.OVERWORLD;
    }

    @Override
    public NBTTagCompound cloneNBTCompound(@Nullable NBTTagCompound nbt)
    {
        NBTTagCompound superNbt = super.cloneNBTCompound(nbt);
        superNbt.setString("dimType", dimensionType.getName());
        superNbt.setString("owner", owner == null ? "null" : owner.toString());
        return superNbt;
    }

    public DimensionType getDimensionType()
    {
        return dimensionType;
    }

    public void setOwner(UUID id)
    {
        owner = id;
    }

    public UUID getOwner()
    {
        return owner;
    }

    public void setOwner(EntityPlayerMP player)
    {
        setOwner(player.getPersistentID());
    }
}
