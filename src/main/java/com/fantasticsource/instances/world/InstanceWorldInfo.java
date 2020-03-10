package com.fantasticsource.instances.world;

import com.fantasticsource.instances.Instances;
import com.fantasticsource.instances.world.dimensions.InstanceTypes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;

import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

public class InstanceWorldInfo extends WorldInfo
{
    public final String SAVE_FOLDER_NAME;
    public final int dimensionID;
    private final DimensionType dimensionType;
    public ArrayList<UUID> visitorWhitelist = new ArrayList<>();
    public WorldInstance world = null;
    private UUID owner;
    public boolean save;

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

        SAVE_FOLDER_NAME = InstanceTypes.getInstanceTypeDir(FMLCommonHandler.instance().getMinecraftServerInstance(), dimensionType) + (owner != null ? owner : getWorldName()) + File.separator;

        this.save = save;
        InstanceHandler.save(this);
    }

    @Override
    public NBTTagCompound cloneNBTCompound(@Nullable NBTTagCompound nbt)
    {
        NBTTagCompound result = super.cloneNBTCompound(nbt);

        result.setString("dimType", dimensionType.getName());
        result.setString("owner", owner == null ? "null" : owner.toString());

        NBTTagList list = new NBTTagList();
        for (UUID id : visitorWhitelist) list.appendTag(new NBTTagString(id.toString()));
        result.setTag("visitorWhitelist", list);

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

    public void setOwner(UUID id)
    {
        owner = id;

        for (Map.Entry<Integer, InstanceWorldInfo> entry : InstanceHandler.instanceInfo.entrySet())
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

    public void setOwner(EntityPlayerMP player)
    {
        setOwner(player.getPersistentID());
    }
}
