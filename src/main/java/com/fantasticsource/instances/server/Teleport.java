package com.fantasticsource.instances.server;

import com.fantasticsource.instances.Destination;
import com.fantasticsource.instances.Instances;
import com.fantasticsource.instances.tags.entity.EscapePoint;
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
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.DimensionType;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.io.File;
import java.lang.reflect.Method;
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

    public static boolean tryJoinWithoutCreating(Entity entity, String name)
    {
        name = Tools.fixFileSeparators(name);
        String typeName = name.substring(0, name.indexOf(File.separator));
        name = name.substring(name.indexOf(File.separator) + 1);

        for (DimensionType instanceType : InstanceTypes.instanceTypes)
        {
            if (instanceType.getName().equals(typeName))
            {
                return joinPossiblyCreating(entity, name, instanceType, instanceType == InstanceTypes.skyroomDimType || instanceType == InstanceTypes.libraryOfWorldsDimType ? UUID.fromString(name) : null, instanceType != InstanceTypes.libraryOfWorldsDimType);
            }
        }

        return false;
    }

    public static boolean joinPossiblyCreating(Entity entity, String name, DimensionType instanceType, UUID owner, boolean save)
    {
        if (entity.world.isRemote) throw new IllegalArgumentException(TextFormatting.RED + "Attempted to call server-only method from client!!!");


        //See if we're in the instance already
        WorldInfo info = null;
        Integer dimension = null;
        if (entity.world.getWorldInfo().getWorldName().equals(name))
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
                if (info2.getWorldName().equals(name))
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
            //Validate...
            if (!Tools.contains(InstanceTypes.instanceTypes, instanceType)) return false;


            //...and then create it (loading from disk if files exist, or generating otherwise)
            Pair<Integer, InstanceWorldInfo> pair = InstanceHandler.loadOrCreateInstance(null, instanceType, owner, name, save);
            if (pair == null) return false;

            info = pair.getValue();
            dimension = pair.getValue().dimensionID;
            InstanceHandler.load((InstanceWorldInfo) info);
        }

        return teleport(entity, dimension, info.getSpawnX() + 0.5, info.getSpawnY(), info.getSpawnZ() + 0.5, entity.rotationYaw, entity.rotationPitch);
    }

    public static boolean escape(Entity entity)
    {
        DimensionType type = entity.world.provider.getDimensionType();
        if (!Tools.contains(InstanceTypes.instanceTypes, type)) return false;

        Destination lastGoodPos = EscapePoint.getEscapePoint(entity);
        if (lastGoodPos != null) return teleport(entity, lastGoodPos);

        return false;
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
        if (!Tools.contains(InstanceTypes.instanceTypes, entity.world.provider.getDimensionType()))
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
