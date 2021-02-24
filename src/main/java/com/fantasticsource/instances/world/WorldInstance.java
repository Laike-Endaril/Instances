package com.fantasticsource.instances.world;

import com.fantasticsource.fantasticlib.api.FLibAPI;
import com.fantasticsource.fantasticlib.api.INBTCap;
import com.fantasticsource.mctools.component.NBTSerializableComponent;
import com.fantasticsource.tools.component.path.CPath;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.profiler.Profiler;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.village.VillageCollection;
import net.minecraft.world.WorldServer;
import net.minecraft.world.border.IBorderListener;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;

import static com.fantasticsource.instances.Instances.MODID;

public class WorldInstance extends WorldServer
{
    protected WorldServer delegate;
    protected IBorderListener borderListener;
    protected CPath celestialAnglePath = null;

    public WorldInstance(WorldInfo worldInfo, MinecraftServer server, ISaveHandler saveHandlerIn, int dimensionId, WorldServer delegate, Profiler profilerIn)
    {
        super(server, saveHandlerIn, worldInfo, dimensionId, profilerIn);
        this.delegate = delegate;
        borderListener = new IBorderListener()
        {
            public void onSizeChanged(WorldBorder border, double newSize)
            {
                getWorldBorder().setTransition(newSize);
            }

            public void onTransitionStarted(WorldBorder border, double oldSize, double newSize, long time)
            {
                getWorldBorder().setTransition(oldSize, newSize, time);
            }

            public void onCenterChanged(WorldBorder border, double x, double z)
            {
                getWorldBorder().setCenter(x, z);
            }

            public void onWarningTimeChanged(WorldBorder border, int newTime)
            {
                getWorldBorder().setWarningTime(newTime);
            }

            public void onWarningDistanceChanged(WorldBorder border, int newDistance)
            {
                getWorldBorder().setWarningDistance(newDistance);
            }

            public void onDamageAmountChanged(WorldBorder border, double newAmount)
            {
                getWorldBorder().setDamageAmount(newAmount);
            }

            public void onDamageBufferChanged(WorldBorder border, double newSize)
            {
                getWorldBorder().setDamageBuffer(newSize);
            }
        };
        delegate.getWorldBorder().addListener(borderListener);
    }

    protected void saveLevel()
    {
        perWorldStorage.saveAllData();
    }

    public WorldInstance init()
    {
        super.init();
        mapStorage = delegate.getMapStorage();
        worldScoreboard = new ServerScoreboard(getMinecraftServer());
        lootTable = delegate.getLootTableManager();
        String s = VillageCollection.fileNameForProvider(provider);
        VillageCollection villagecollection = (VillageCollection) perWorldStorage.getOrLoadData(VillageCollection.class, s);

        if (villagecollection == null)
        {
            villageCollection = new VillageCollection(this);
            perWorldStorage.setData(s, villageCollection);
        }
        else
        {
            villageCollection = villagecollection;
            villageCollection.setWorldsForAll(this);
        }

        INBTCap nbtCap = FLibAPI.getNBTCap(this);
        if (nbtCap != null)
        {
            NBTTagCompound compound = nbtCap.getCompound(MODID);
            if (compound != null)
            {
                if (compound.hasKey("celestialAnglePath")) celestialAnglePath = (CPath) NBTSerializableComponent.deserializeMarked(compound.getCompoundTag("celestialAnglePath"));
            }
        }

        return this;
    }

    /**
     * Syncs all changes to disk and wait for completion.
     */
    @Override
    public void flush()
    {
        super.flush();
        delegate.getWorldBorder().removeListener(borderListener); // Unlink ourselves, to prevent world leak.
    }

    @Override
    public float getCelestialAngle(float partialTicks)
    {
        return celestialAnglePath != null ? (float) celestialAnglePath.getRelativePosition(worldInfo.getWorldTime()).values[0] : super.getCelestialAngle(partialTicks);
    }

    public void setCelestialAnglePath(CPath celestialAnglePath)
    {
        this.celestialAnglePath = celestialAnglePath;

        INBTCap nbtCap = FLibAPI.getNBTCap(this);
        if (nbtCap != null)
        {
            NBTTagCompound compound = nbtCap.getCompound(MODID);
            if (compound != null)
            {
                if (celestialAnglePath == null) compound.removeTag("celestialAnglePath");
                else compound.setTag("celestialAnglePath", NBTSerializableComponent.serializeMarked(celestialAnglePath));
            }
        }
    }
}
