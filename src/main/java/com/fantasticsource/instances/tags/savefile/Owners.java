package com.fantasticsource.instances.tags.savefile;

import com.fantasticsource.fantasticlib.api.FLibAPI;
import com.fantasticsource.instances.world.InstanceHandler;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;

import java.io.File;

import static com.fantasticsource.fantasticlib.FantasticLib.MODID;

public class Owners
{
    public static boolean setOwner(MinecraftServer server, String instanceName, String owner)
    {
        if (owner == null)
        {
            return setNoOwner(server, instanceName);
        }


        NBTTagCompound compound = FLibAPI.getNBTCap(server.worlds[0]).getCompound(MODID);

        if (!compound.hasKey("instancesToOwners"))
        {
            compound.setTag("ownersToInstances", new NBTTagCompound());
            compound.setTag("instancesToOwners", new NBTTagCompound());
        }


        NBTTagCompound ownersToInstances = compound.getCompoundTag("ownersToInstances");
        compound = compound.getCompoundTag("instancesToOwners");

        String oldOwner = compound.hasKey(instanceName) ? compound.getString(instanceName) : null;
        if (compound.getString(instanceName).equals(owner)) return false;


        //Set owner of instance
        compound.setString(instanceName, owner);


        //Remove instance from owned instances of old owner
        if (oldOwner != null && ownersToInstances.hasKey(oldOwner))
        {
            compound = ownersToInstances.getCompoundTag(oldOwner);
            compound.removeTag(instanceName);
            if (compound.hasNoTags()) ownersToInstances.removeTag(oldOwner);
        }

        //Add instance to owned instances of new owner
        if (!ownersToInstances.hasKey(owner)) ownersToInstances.setTag(owner, new NBTTagCompound());
        compound = ownersToInstances.getCompoundTag(owner);
        compound.setInteger(instanceName, 1);

        return true;
    }

    public static boolean setNoOwner(MinecraftServer server, String instanceName)
    {
        NBTTagCompound compound = FLibAPI.getNBTCap(server.worlds[0]).getCompound(MODID);

        if (!compound.hasKey("instancesToOwners")) return false;


        NBTTagCompound ownersToInstances = compound.getCompoundTag("ownersToInstances");
        compound = compound.getCompoundTag("instancesToOwners");

        String oldOwner = compound.hasKey(instanceName) ? compound.getString(instanceName) : null;
        if (oldOwner == null) return false;


        //Set owner of instance to none
        compound.removeTag(instanceName);


        //Remove instance from owned instances of old owner
        if (ownersToInstances.hasKey(oldOwner))
        {
            compound = ownersToInstances.getCompoundTag(oldOwner);
            compound.removeTag(instanceName);
            if (compound.hasNoTags()) ownersToInstances.removeTag(oldOwner);
        }

        return true;
    }


    public static String getOwner(MinecraftServer server, String instanceName)
    {
        NBTTagCompound compound = FLibAPI.getNBTCap(server.worlds[0]).getCompound(MODID);

        if (!compound.hasKey("instancesToOwners")) return null;


        compound = compound.getCompoundTag("instancesToOwners");
        return compound.hasKey(instanceName) ? compound.getString(instanceName) : null;
    }

    public static String[] getOwnedInstances(MinecraftServer server, String owner)
    {
        NBTTagCompound compound = FLibAPI.getNBTCap(server.worlds[0]).getCompound(MODID);

        if (!compound.hasKey("ownersToInstances")) return new String[0];


        compound = compound.getCompoundTag("ownersToInstances");
        return compound.hasKey(owner) ? compound.getCompoundTag(owner).getKeySet().toArray(new String[0]) : new String[0];
    }


    //Methods below this point are for backend use only


    public static void validateInstances(MinecraftServer server)
    {
        NBTTagCompound compound = FLibAPI.getNBTCap(server.worlds[0]).getCompound(MODID);

        if (!compound.hasKey("instancesToOwners")) return;

        compound = compound.getCompoundTag("instancesToOwners");
        File file;
        for (String instanceName : compound.getKeySet().toArray(new String[0]))
        {
            file = new File(InstanceHandler.getInstancesDir(server) + instanceName);
            if (!file.isDirectory()) setNoOwner(server, instanceName);
        }
    }
}
