package com.fantasticsource.instances.server;

import com.fantasticsource.instances.Instances;
import com.fantasticsource.instances.blocksanditems.tileentities.TEInstancePortal;
import com.fantasticsource.instances.world.InstanceHandler;
import com.fantasticsource.instances.world.InstanceWorldInfo;
import com.fantasticsource.instances.world.dimensions.InstanceTypes;
import com.fantasticsource.mctools.MCTools;
import com.fantasticsource.tools.Tools;
import com.fantasticsource.tools.datastructures.Pair;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.network.play.server.SPacketSetExperience;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.DimensionType;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class Teleport
{
    private static Method copyDataFromOld;

    static
    {
        try
        {
            copyDataFromOld = Entity.class.getDeclaredMethod((Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment") ? "copyDataFromOld" : "func_180432_n", Entity.class);
            copyDataFromOld.setAccessible(true);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static boolean joinTemplatePossiblyCreating(EntityPlayerMP player, String templateName)
    {
        //If we're already in the template, just teleport locally
        if (player.world.provider.getDimensionType() == InstanceTypes.templateDimType && player.world.getWorldInfo().getWorldName().equals(templateName))
        {
            return teleport(player, player.world.provider.getDimension(), 0.5, 1, 0.5, player.rotationYaw, player.rotationPitch);
        }

        //Try finding an existing template with the given name
        for (Map.Entry<Integer, InstanceWorldInfo> entry : InstanceHandler.loadedInstances.entrySet())
        {
            InstanceWorldInfo info = entry.getValue();
            if (info.getDimensionType() == InstanceTypes.templateDimType && info.getWorldName().equals(templateName))
            {
                return teleport(player, entry.getKey(), 0.5, 1, 0.5, player.rotationYaw, player.rotationPitch);
            }
        }

        //Not found
        Pair<Integer, InstanceWorldInfo> pair = InstanceHandler.createInstance(null, InstanceTypes.templateDimType, null, templateName, false);
        InstanceHandler.load(pair.getValue());
        return teleport(player, pair.getKey(), 0.5, 1, 0.5, player.rotationYaw, player.rotationPitch);
    }

    public static boolean joinHubPossiblyCreating(EntityPlayerMP player)
    {
        //If we're already in the hub, just teleport locally
        if (player.world.provider.getDimensionType() == InstanceTypes.libraryOfWorldsDimType)
        {
            return teleport(player, player.world.provider.getDimension(), 8, 2, 8, player.rotationYaw, player.rotationPitch);
        }

        //Try finding an existing hub for said player
        for (Map.Entry<Integer, InstanceWorldInfo> entry : InstanceHandler.loadedInstances.entrySet())
        {
            InstanceWorldInfo info = entry.getValue();
            if (info.getDimensionType() == InstanceTypes.libraryOfWorldsDimType && info.getWorldName().equals((player.getName() + "'s " + InstanceTypes.libraryOfWorldsDimType.getName()).replace(" ", "_")))
            {
                return teleport(player, entry.getKey(), 8, 2, 8, player.rotationYaw, player.rotationPitch);
            }
        }

        //Not found
        Pair<Integer, InstanceWorldInfo> pair = InstanceHandler.createInstance(null, InstanceTypes.libraryOfWorldsDimType, player.getPersistentID(), false);
        InstanceHandler.load(pair.getValue());
        return teleport(player, pair.getKey(), 8, 2, 8, player.rotationYaw, player.rotationPitch);
    }

    public static boolean escape(Entity entity)
    {
        DimensionType type = entity.world.provider.getDimensionType();
        if (!Tools.contains(InstanceTypes.instanceTypes, type)) return false;

        Set<String> strings = entity.getTags();
        for (String s : strings.toArray(new String[0]))
        {
            if (s.contains("instances.lastgoodpos"))
            {
                String[] tokens = s.replace("instances.lastgoodpos", "").split(",");
                return teleport(entity, Integer.parseInt(tokens[0]), 0.5d + Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2]), 0.5d + Integer.parseInt(tokens[3]), entity.rotationYaw, entity.rotationPitch);
            }
        }

        return false;
    }

    public static boolean joinSkyroomPossiblyCreating(EntityPlayerMP player)
    {
        return joinSkyroomPossiblyCreating(player, player.getPersistentID());
    }

    public static boolean joinSkyroomPossiblyCreating(Entity entity, int dimension)
    {
        InstanceWorldInfo info = InstanceHandler.get(dimension);
        if (info == null)
        {
            entity.sendMessage(new TextComponentString("Instance not found: " + dimension));
            return false;
        }

        return teleport(entity, dimension, 0.5, 77, -13.5, entity.rotationYaw, entity.rotationPitch);
    }

    public static boolean joinSkyroomPossiblyCreating(Entity entity, UUID ownerID)
    {
        if (ownerID == null) return false;

        //Try finding a skyroom owned by the player
        for (Map.Entry<Integer, InstanceWorldInfo> entry : InstanceHandler.loadedInstances.entrySet())
        {
            if (entry.getValue().getDimensionType() == InstanceTypes.skyroomDimType && ownerID.equals(entry.getValue().getOwner()))
            {
                return teleport(entity, entry.getKey(), 0.5, 77, -13.5, entity.rotationYaw, entity.rotationPitch);
            }
        }

        //Not found
        Pair<Integer, InstanceWorldInfo> pair = InstanceHandler.createInstance(null, InstanceTypes.skyroomDimType, ownerID, true);
        InstanceHandler.load(pair.getValue());
        return teleport(entity, pair.getKey(), 0.5, 77, -13.5, entity.rotationYaw, entity.rotationPitch);
    }


    public static boolean teleport(Entity entity, TEInstancePortal.Destination destination)
    {
        return teleport(entity, destination.dimension, destination.x, destination.y, destination.z, entity.rotationYaw, entity.rotationPitch);
    }

    public static boolean teleport(Entity entity, int dimension, double x, double y, double z, float yaw, float pitch)
    {
        if (entity == null) return false;

        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();


        //Save current position if we're not currently in an instance (for instance escaping)
        if (!Tools.contains(InstanceTypes.instanceTypes, entity.world.provider.getDimensionType()))
        {
            Set<String> strings = entity.getTags();
            for (String s : strings.toArray(new String[0]))
            {
                if (s.contains("instances.lastgoodpos"))
                {
                    strings.remove(s);
                    break;
                }
            }
            BlockPos pos = entity.getPosition();
            strings.add("instances.lastgoodpos" + entity.dimension + "," + pos.getX() + "," + pos.getY() + "," + pos.getZ());
        }


        //Dismount
        entity.dismountRidingEntity();


        //Change dimension
        if (entity.dimension != dimension)
        {
            entity = teleportEntityToDimension(server, entity, dimension);
            if (entity == null) return false;
        }


        //Adjust yaw and pitch
        yaw = MathHelper.wrapDegrees(yaw);
        pitch = MathHelper.wrapDegrees(pitch);

        if (pitch > 90 || pitch < -90)
        {
            pitch = MathHelper.wrapDegrees(180 - pitch);
            yaw = MathHelper.wrapDegrees(yaw + 180);
        }


        //Set position and angles
        entity.setRotationYawHead(yaw);

        if (entity instanceof EntityPlayerMP) ((EntityPlayerMP) entity).connection.setPlayerLocation(x, y, z, yaw, pitch);
        else
        {
            entity.setLocationAndAngles(x, y, z, yaw, pitch);
            entity.world.updateEntityWithOptionalForce(entity, false);
        }


        return true;
    }


    protected static Entity teleportEntityToDimension(MinecraftServer server, Entity entity, int dimension)
    {
        World world = server.getWorld(dimension);
        if (world == null) return null;


        if (entity instanceof EntityPlayerMP)
        {
            EntityPlayerMP player = (EntityPlayerMP) entity;
            int oldDim = player.dimension;

            if (MCTools.isOP(player))
            {
                GameType gameType = player.interactionManager.getGameType();

                server.getPlayerList().transferPlayerToDimension(player, dimension, new TeleporterSimple((WorldServer) server.getEntityWorld()));

                //Preserve gamemode of OP players
                player.setGameType(gameType);
            }
            else
            {
                server.getPlayerList().transferPlayerToDimension(player, dimension, new TeleporterSimple((WorldServer) server.getEntityWorld()));

                //Set gamemode of non-OP players
                Instances.setPlayerMode(player, InstanceHandler.get(dimension));
            }


            //After successful cross-dimensional player teleportation
            InstanceWorldInfo oldInfo = InstanceHandler.get(oldDim);
            if (oldInfo != null && oldInfo.getDimensionType() == InstanceTypes.libraryOfWorldsDimType)
            {
                InstanceHandler.delete(server, oldInfo);
            }

            //Fix exp desync
            player.connection.sendPacket(new SPacketSetExperience(player.experience, player.experienceTotal, player.experienceLevel));


            return player;
        }
        else
        {
            return travelEntity(server, entity, dimension);
        }
    }


    protected static Entity travelEntity(MinecraftServer server, Entity entity, int dimensionId)
    {
        if (!entity.world.isRemote && !entity.isDead)
        {
            entity.world.profiler.startSection("changeDimension");
            int j = entity.dimension;
            WorldServer worldserver = server.getWorld(j);
            WorldServer worldserver1 = server.getWorld(dimensionId);
            entity.dimension = dimensionId;

            Entity newEntity = EntityList.createEntityByIDFromName(EntityList.getKey(entity.getClass()), worldserver1);

            if (newEntity != null)
            {
                try
                {
                    copyDataFromOld.invoke(newEntity, entity);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                entity.world.removeEntity(entity);

                newEntity.forceSpawn = true;

                worldserver1.spawnEntity(newEntity);
            }

            worldserver1.updateEntityWithOptionalForce(newEntity, true);

            worldserver.resetUpdateEntityTick();
            worldserver1.resetUpdateEntityTick();
            entity.world.profiler.endSection();
            return newEntity;
        }

        return entity;
    }
}
