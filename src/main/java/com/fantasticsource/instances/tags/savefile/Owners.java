package com.fantasticsource.instances.tags.savefile;

import com.fantasticsource.instances.world.InstanceHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

public class Owners
{
    protected static MinecraftServer server = null;
    public static File DIR = null, FILE = null;
    public static final HashMap<UUID, HashSet<String>> ownerToInstances = new HashMap<>();
    public static final HashMap<String, UUID> instanceToOwner = new HashMap<>();

    public static boolean setOwner(String instanceName, UUID owner)
    {
        ensureLoaded();

        UUID oldOwner = instanceToOwner.get(instanceName);
        if (oldOwner == owner) return false;


        HashSet<String> set = ownerToInstances.get(oldOwner);
        if (set != null)
        {
            set.remove(instanceName);
            if (set.size() == 0) ownerToInstances.remove(oldOwner);
        }

        if (owner == null) instanceToOwner.remove(instanceName);
        else
        {
            instanceToOwner.put(instanceName, owner);
            ownerToInstances.computeIfAbsent(owner, o -> new HashSet<>()).add(instanceName);
        }

        save();
        return true;
    }

    public static UUID getOwner(String instanceName)
    {
        return instanceToOwner.get(instanceName);
    }

    public static String[] getOwnedInstances(UUID owner)
    {
        return ownerToInstances.containsKey(owner) ? ownerToInstances.get(owner).toArray(new String[0]) : new String[0];
    }


    protected static void ensureLoaded()
    {
        MinecraftServer currentServer = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server != currentServer)
        {
            ownerToInstances.clear();
            instanceToOwner.clear();

            server = currentServer;
            DIR = new File(InstanceHandler.getInstancesDir(currentServer));
            DIR.mkdirs();

            FILE = new File(DIR, "Owners.txt");
            if (FILE.exists())
            {
                try
                {
                    BufferedReader reader = new BufferedReader(new FileReader(FILE));
                    String line = reader.readLine();
                    UUID owner = null;
                    HashSet<String> instances = new HashSet<>();
                    while (line != null)
                    {
                        line = line.trim();
                        if (!line.equals(""))
                        {
                            if (line.charAt(0) != '*')
                            {
                                owner = UUID.fromString(line);
                                instances = new HashSet<>();
                                ownerToInstances.put(owner, instances);
                            }
                            else
                            {
                                line = line.replace("*", "");
                                instances.add(line);
                                instanceToOwner.put(line, owner);
                            }
                        }

                        line = reader.readLine();
                    }
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    protected static void save()
    {
        if (FILE.exists()) FILE.delete();
        if (ownerToInstances.size() > 0)
        {
            try
            {
                BufferedWriter writer = new BufferedWriter(new FileWriter(FILE));
                for (Map.Entry<UUID, HashSet<String>> entry : ownerToInstances.entrySet())
                {
                    writer.write(entry.getKey() + "\r\n");
                    for (String instance : entry.getValue()) writer.write("*" + instance + "\r\n");
                    writer.write("\r\n");
                }
                writer.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}
