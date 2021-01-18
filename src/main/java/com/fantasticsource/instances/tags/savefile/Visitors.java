package com.fantasticsource.instances.tags.savefile;

import com.fantasticsource.instances.world.InstanceHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

public class Visitors
{
    protected static MinecraftServer server = null;
    public static File DIR = null, FILE = null;
    public static final HashMap<UUID, HashSet<String>> visitorToInstances = new HashMap<>();
    public static final HashMap<String, HashSet<UUID>> instanceToVisitors = new HashMap<>();

    public static boolean setVisitable(String instanceName, UUID playerID, boolean canVisit)
    {
        ensureLoaded();

        if (canVisit)
        {
            if (!visitorToInstances.computeIfAbsent(playerID, o -> new HashSet<>()).add(instanceName)) return false;

            instanceToVisitors.computeIfAbsent(instanceName, o -> new HashSet<>()).add(playerID);
            save();
            return true;
        }
        else
        {
            HashSet<String> set = visitorToInstances.get(playerID);
            if (set == null || !set.contains(instanceName)) return false;

            set.remove(instanceName);
            if (set.size() == 0) visitorToInstances.remove(playerID);
            save();
            return true;
        }
    }

    public static void clearValidVisitors(String instanceName)
    {
        HashSet<UUID> visitors = instanceToVisitors.get(instanceName);
        if (visitors != null)
        {
            for (UUID id : visitors) setVisitable(instanceName, id, false);
        }
    }


    public static String[] visitableInstances(UUID playerID)
    {
        ensureLoaded();
        return visitorToInstances.containsKey(playerID) ? visitorToInstances.get(playerID).toArray(new String[0]) : new String[0];
    }

    public static boolean canVisit(UUID playerID, String instanceName)
    {
        ensureLoaded();
        return visitorToInstances.containsKey(playerID) && visitorToInstances.get(playerID).contains(instanceName);
    }


    public static UUID[] validVisitors(String instanceName)
    {
        ensureLoaded();
        return instanceToVisitors.containsKey(instanceName) ? instanceToVisitors.get(instanceName).toArray(new UUID[0]) : new UUID[0];
    }

    public static boolean canBeVisitedBy(String instanceName, UUID playerID)
    {
        ensureLoaded();
        return instanceToVisitors.containsKey(instanceName) && instanceToVisitors.get(instanceName).contains(playerID);
    }


    protected static void ensureLoaded()
    {
        MinecraftServer currentServer = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server != currentServer)
        {
            visitorToInstances.clear();
            instanceToVisitors.clear();

            server = currentServer;
            DIR = new File(InstanceHandler.getInstancesDir(currentServer));
            DIR.mkdirs();

            FILE = new File(DIR, "Visitors.txt");
            if (FILE.exists())
            {
                try
                {
                    BufferedReader reader = new BufferedReader(new FileReader(FILE));
                    String line = reader.readLine();
                    HashSet<UUID> visitorList = null;
                    String instance = null;
                    UUID visitor;
                    while (line != null)
                    {
                        line = line.trim();
                        if (!line.equals(""))
                        {
                            if (line.charAt(0) != '*')
                            {
                                instance = line;
                                visitorList = new HashSet<>();
                                instanceToVisitors.put(instance, visitorList);
                            }
                            else
                            {
                                visitor = UUID.fromString(line.replace("*", ""));
                                visitorList.add(visitor);
                                visitorToInstances.computeIfAbsent(visitor, o -> new HashSet<>()).add(instance);
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
        if (instanceToVisitors.size() > 0)
        {
            try
            {
                BufferedWriter writer = new BufferedWriter(new FileWriter(FILE));
                for (Map.Entry<String, HashSet<UUID>> entry : instanceToVisitors.entrySet())
                {
                    writer.write(entry.getKey() + "\r\n");
                    for (UUID id : entry.getValue()) writer.write("*" + id + "\r\n");
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
