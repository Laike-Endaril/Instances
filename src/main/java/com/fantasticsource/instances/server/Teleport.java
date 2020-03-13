package com.fantasticsource.instances.server;

import com.fantasticsource.instances.Destination;
import com.fantasticsource.instances.InstanceData;
import com.fantasticsource.instances.Instances;
import com.fantasticsource.instances.network.Network;
import com.fantasticsource.instances.tags.entity.EscapePoint;
import com.fantasticsource.instances.world.InstanceHandler;
import com.fantasticsource.instances.world.InstanceWorldInfo;
import com.fantasticsource.mctools.MCTools;
import com.fantasticsource.tools.datastructures.Pair;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.network.play.server.SPacketSetExperience;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.lang.reflect.Method;

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

    public static boolean tryJoinWithoutCreating(Entity entity, String fullName)
    {
        InstanceData data = InstanceData.get(fullName);
        if (data == null || !data.exists()) return false;

        return joinPossiblyCreating(entity, fullName);
    }

    public static boolean joinPossiblyCreating(Entity entity, String fullName)
    {
        return joinPossiblyCreating(entity, fullName, null);
    }

    public static boolean joinPossiblyCreating(Entity entity, String fullName, String owner)
    {
        if (entity.world.isRemote) throw new IllegalArgumentException(TextFormatting.RED + "Attempted to call server-only method from client!!!");


        //See if we're in the instance already
        WorldInfo info = null;
        Integer dimension = null;
        if (entity.world.getWorldInfo().getWorldName().equals(fullName))
        {
            info = entity.world.getWorldInfo();
            dimension = entity.dimension;
        }

        //Not yet found?
        if (info == null)
        {
            //Try finding an existing instance with the given name
            for (WorldServer world : FMLCommonHandler.instance().getMinecraftServerInstance().worlds)
            {
                WorldInfo info2 = world.getWorldInfo();
                if (info2.getWorldName().equals(fullName))
                {
                    info = info2;
                    dimension = world.provider.getDimension();
                    break;
                }
            }
        }

        //Not yet found?
        if (info == null)
        {
            //...and then create it (loading from disk if files exist, or generating otherwise)
            Pair<Integer, InstanceWorldInfo> pair = InstanceHandler.loadOrCreateInstance(null, fullName);
            info = pair.getValue();
            dimension = pair.getKey();
        }

        return teleport(entity, dimension, info.getSpawnX() + 0.5, info.getSpawnY(), info.getSpawnZ() + 0.5, entity.rotationYaw, entity.rotationPitch);
    }

    public static boolean escape(Entity entity)
    {
        InstanceData data = InstanceData.get(MCTools.getSaveFolder(entity.world.provider));
        if (data == null) return false;

        if (data.exists() && data.saves()) return false;


        Destination escapePoint = EscapePoint.getEscapePoint(entity);
        if (escapePoint == null) return false;


        return teleport(entity, escapePoint);
    }


    public static boolean teleport(Entity entity, int dimension)
    {
        for (WorldServer world : FMLCommonHandler.instance().getMinecraftServerInstance().worlds)
        {
            if (world.provider.getDimension() == dimension) return teleport(entity, new Destination(world, entity.getRotationYawHead(), entity.rotationPitch));
        }
        return false;
    }

    public static boolean teleport(Entity entity, Destination destination)
    {
        return teleport(entity, destination.dimension, destination.x, destination.y, destination.z, entity.rotationYaw, entity.rotationPitch);
    }

    public static boolean teleport(Entity entity, int dimension, double x, double y, double z, float yaw, float pitch)
    {
        if (entity == null) return false;

        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();


        //Save current position if we're not currently in an instance (for instance escaping)
        InstanceData data = InstanceData.get(MCTools.getSaveFolder(entity.world.provider));
        if (data == null || data.saves())
        {
            EscapePoint.setEscapePointToCurrentPosition(entity);
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
            return teleportPlayerEntityToDimension(server, (EntityPlayerMP) entity, dimension);
        }
        else
        {
            return teleportNonPlayerEntityToDimension(server, entity, dimension);
        }
    }


    protected static EntityPlayerMP teleportPlayerEntityToDimension(MinecraftServer server, EntityPlayerMP player, int dimension)
    {
        //Before player teleport
        int oldPlayerCount = player.world.playerEntities.size();
        GameType gameType = MCTools.isOP(player) ? player.interactionManager.getGameType() : null;
        Network.WRAPPER.sendTo(new Network.SyncDimensionTypePacket(dimension), player);


        //Teleport player
        server.getPlayerList().transferPlayerToDimension(player, dimension, new TeleporterSimple((WorldServer) server.getEntityWorld()));


        //After player teleport
        InstanceData data = InstanceData.get(MCTools.getSaveFolder(player.world.provider));

        //Set gamemode
        if (MCTools.isOP(player)) player.setGameType(gameType);
        else Instances.setPlayerMode(player, data);


        //After successful cross-dimensional player teleportation
        if (data != null && data.exists() && oldPlayerCount == 1 && !data.saves())
        {
            InstanceHandler.delete(server, data.getFullName());
        }

        //Fix exp desync
        player.connection.sendPacket(new SPacketSetExperience(player.experience, player.experienceTotal, player.experienceLevel));


        return player;
    }


    protected static Entity teleportNonPlayerEntityToDimension(MinecraftServer server, Entity entity, int dimensionId)
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
