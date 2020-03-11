package com.fantasticsource.instances.world;

import com.fantasticsource.instances.Instances;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.DimensionManager;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Map;
import java.util.UUID;

public class InstanceWorldInfo extends WorldInfo
{
    public final String saveFolderName;
    public final int dimensionID;
    private final DimensionType dimensionType;
    public WorldInstance world = null;
    public boolean save;
    private UUID owner;

    public InstanceWorldInfo(WorldInfo info)
    {
        throw new IllegalStateException("Not implemented!");
    }

    public InstanceWorldInfo(NBTTagCompound nbt)
    {
        throw new IllegalStateException("Not implemented!");
    }

    public InstanceWorldInfo(int dimensionID, WorldSettings settings, UUID owner, String name, DimensionType dimType, boolean save)
    {
        super(settings, name);
        dimensionType = dimType != null ? dimType : DimensionType.OVERWORLD;

        this.dimensionID = dimensionID;
        this.owner = owner;

        saveFolderName = dimensionType + File.separator + (owner != null ? owner : getWorldName());

        this.save = save;
        InstanceHandler.trySave(this);
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

    public UUID getOwner()
    {
        return owner;
    }

    public void setOwner(EntityPlayerMP player)
    {
        setOwner(player.getPersistentID());
    }

    public void setOwner(UUID id)
    {
        owner = id;

        for (Map.Entry<Integer, InstanceWorldInfo> entry : InstanceHandler.loadedInstances.entrySet())
        {
            if (entry.getValue() == this)
            {
                World world = DimensionManager.getWorld(entry.getKey());
                if (world == null) break;

                for (EntityPlayer player : world.playerEntities)
                {
                    Instances.setPlayerMode((EntityPlayerMP) player, this);
                }
            }
        }
    }
}
