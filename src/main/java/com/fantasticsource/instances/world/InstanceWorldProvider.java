package com.fantasticsource.instances.world;

import com.fantasticsource.instances.network.Network;
import com.fantasticsource.tools.Tools;
import com.fantasticsource.tools.component.path.CPath;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nullable;
import java.io.File;

public abstract class InstanceWorldProvider extends WorldProvider
{
    public static CPath celestialAnglePath = null;
    protected static boolean xaeros;

    public InstanceWorldProvider()
    {
        xaeros = Loader.isModLoaded("xaeroworldmap");
    }

    @Nullable
    @Override
    public final String getSaveFolder()
    {
        //If you return null here, it saves to the overworld

        if (xaeros && world.isRemote && Tools.stackContainsSubstring("xaero")) return "instancesInstance";

        return "Instances" + File.separator + world.getWorldInfo().getWorldName();
    }

    @Override
    public final DimensionType getDimensionType()
    {
        int dim = getDimension();
        if (DimensionManager.isDimensionRegistered(dim)) return DimensionManager.getProviderType(getDimension());
        return DimensionType.OVERWORLD;
    }

    @Override
    public float calculateCelestialAngle(long worldTime, float partialTicks)
    {
        if (world.isRemote && celestialAnglePath != null)
        {
            return (float) celestialAnglePath.getRelativePosition(worldTime).values[0];
        }

        return super.calculateCelestialAngle(worldTime, partialTicks);
    }

    @SubscribeEvent
    public static void syncCelestialAnglePath(EntityJoinWorldEvent event)
    {
        Entity entity = event.getEntity();
        if (!(entity instanceof EntityPlayerMP)) return;

        CPath path = null;
        World world = event.getWorld();
        if (world instanceof WorldInstance) path = ((WorldInstance) world).celestialAnglePath;
        Network.WRAPPER.sendTo(new Network.CelestialAnglePathPacket(path), (EntityPlayerMP) entity);
    }
}
