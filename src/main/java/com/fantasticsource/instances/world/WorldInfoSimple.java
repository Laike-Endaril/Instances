package com.fantasticsource.instances.world;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.DimensionType;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.DimensionManager;

import javax.annotation.Nullable;
import java.util.Map;
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
        NBTTagCompound result = super.cloneNBTCompound(nbt);
        result.setString("dimType", dimensionType.getName());
        result.setString("owner", owner == null ? "null" : owner.toString());
        return result;
    }

    public DimensionType getDimensionType()
    {
        return dimensionType;
    }

    public void setOwner(UUID id)
    {
        owner = id;

        for (Map.Entry<Integer, WorldInfoSimple> entry : InstanceHandler.instanceInfo.entrySet())
        {
            if (entry.getValue() == this)
            {
                World world = DimensionManager.getWorld(entry.getKey());
                if (world == null) break;

                for (EntityPlayer player : world.playerEntities)
                {
                    if (player.getPersistentID().equals(id)) player.setGameType(GameType.SURVIVAL);
                    else player.setGameType(GameType.ADVENTURE);
                }
            }
        }
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
