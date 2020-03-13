package com.fantasticsource.instances.tags.savefile;

import com.fantasticsource.fantasticlib.api.FLibAPI;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;

import java.util.UUID;

import static com.fantasticsource.fantasticlib.FantasticLib.MODID;

public class Visitors
{
    public static boolean setVisitable(MinecraftServer server, String instanceName, UUID playerID, boolean canVisit)
    {
        NBTTagCompound compound = FLibAPI.getNBTCap(server.worlds[0]).getCompound(MODID);

        if (!compound.hasKey("instanceToVisitors"))
        {
            if (!canVisit) return false;

            compound.setTag("visitorToInstances", new NBTTagCompound());
            compound.setTag("instanceToVisitors", new NBTTagCompound());
        }

        NBTTagCompound instanceToVisitors = compound.getCompoundTag("instanceToVisitors");
        NBTTagCompound visitorToInstances = compound.getCompoundTag("visitorToInstances");
        String visitor = "" + playerID;

        if (!visitorToInstances.hasKey(visitor))
        {
            if (!canVisit) return false;

            visitorToInstances.setTag(visitor, new NBTTagCompound());
            if (!instanceToVisitors.hasKey(instanceName)) instanceToVisitors.setTag(instanceName, new NBTTagCompound());
        }

        NBTTagCompound instanceNames = visitorToInstances.getCompoundTag(visitor);
        if (!instanceNames.hasKey(instanceName))
        {
            if (!canVisit) return false;

            //Add
            instanceNames.setInteger(instanceName, 1);
            instanceToVisitors.getCompoundTag(instanceName).setInteger(visitor, 1);
            return true;
        }
        else
        {
            if (canVisit) return false;

            //Remove
            instanceNames.removeTag(instanceName);
            if (instanceNames.hasNoTags()) visitorToInstances.removeTag(visitor);

            NBTTagCompound visitors = instanceToVisitors.getCompoundTag(instanceName);
            visitors.removeTag(visitor);
            if (visitors.hasNoTags()) instanceToVisitors.removeTag(instanceName);

            return true;
        }
    }


    public static String[] visitableInstances(MinecraftServer server, UUID playerID)
    {
        NBTTagCompound compound = FLibAPI.getNBTCap(server.worlds[0]).getCompound(MODID);

        if (!compound.hasKey("visitorToInstances")) return new String[0];
        compound = compound.getCompoundTag("visitorToInstances");

        String visitor = "" + playerID;
        if (!compound.hasKey(visitor)) return new String[0];

        return compound.getCompoundTag(visitor).getKeySet().toArray(new String[0]);
    }

    public static boolean canVisit(MinecraftServer server, UUID playerID, String instanceName)
    {
        for (String name : visitableInstances(server, playerID))
        {
            if (name.equals(instanceName)) return true;
        }
        return false;
    }


    public static UUID[] validVisitors(MinecraftServer server, String instanceName)
    {
        NBTTagCompound compound = FLibAPI.getNBTCap(server.worlds[0]).getCompound(MODID);

        if (!compound.hasKey("instanceToVisitors")) return new UUID[0];
        compound = compound.getCompoundTag("instanceToVisitors");

        if (!compound.hasKey(instanceName)) return new UUID[0];

        String[] strings = compound.getCompoundTag(instanceName).getKeySet().toArray(new String[0]);
        UUID[] result = new UUID[strings.length];
        int i = 0;
        for (String s : strings) result[i++] = UUID.fromString(s);
        return result;
    }

    public static boolean canBeVisitedBy(MinecraftServer server, String instanceName, UUID playerID)
    {
        for (UUID id : validVisitors(server, instanceName))
        {
            if (id.equals(playerID)) return true;
        }
        return false;
    }
}
