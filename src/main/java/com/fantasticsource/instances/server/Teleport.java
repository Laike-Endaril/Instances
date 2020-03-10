package com.fantasticsource.instances.server;

import com.fantasticsource.instances.Instances;
import com.fantasticsource.instances.blocksanditems.tileentities.TEInstancePortal;
import com.fantasticsource.instances.commands.CmdDimTP;
import com.fantasticsource.instances.world.InstanceHandler;
import com.fantasticsource.instances.world.InstanceWorldInfo;
import com.fantasticsource.instances.world.dimensions.InstanceTypes;
import com.fantasticsource.mctools.MCTools;
import com.fantasticsource.mctools.PlayerData;
import com.fantasticsource.tools.Tools;
import com.fantasticsource.tools.datastructures.Pair;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
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
import java.util.EnumSet;
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
        for (Map.Entry<Integer, InstanceWorldInfo> entry : InstanceHandler.instanceInfo.entrySet())
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
        for (Map.Entry<Integer, InstanceWorldInfo> entry : InstanceHandler.instanceInfo.entrySet())
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
        return joinSkyroomPossiblyCreating(player, player.getName());
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

    public static boolean joinSkyroomPossiblyCreating(Entity entity, String ownername)
    {
        UUID id = PlayerData.getID(ownername);
        if (id == null) return false;

        //Try finding a skyroom owned by the player
        for (Map.Entry<Integer, InstanceWorldInfo> entry : InstanceHandler.instanceInfo.entrySet())
        {
            if (entry.getValue().getDimensionType() == InstanceTypes.skyroomDimType && id.equals(entry.getValue().getOwner()))
            {
                return teleport(entity, entry.getKey(), 0.5, 77, -13.5, entity.rotationYaw, entity.rotationPitch);
            }
        }

        //Not found
        Pair<Integer, InstanceWorldInfo> pair = InstanceHandler.createInstance(null, InstanceTypes.skyroomDimType, id, true);
        InstanceHandler.load(pair.getValue());
        return teleport(entity, pair.getKey(), 0.5, 77, -13.5, entity.rotationYaw, entity.rotationPitch);
    }

    public static boolean teleport(Entity entity, TEInstancePortal.Destination destination)
    {
        return teleport(entity, destination.dimension, destination.x, destination.y, destination.z, entity.rotationYaw, entity.rotationPitch);
    }

    public static boolean teleport(Entity entity, int dimension, double x, double y, double z, float yaw, float pitch)
    {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        return teleport(null, server, server, entity, new String[]{"" + dimension, "" + x, "" + y, "" + z, "" + yaw, "" + pitch});
    }

    public static boolean teleport(CmdDimTP command, MinecraftServer server, ICommandSender sender, Entity entity, String[] args)
    {
        if (entity == null)
        {
            if (sender instanceof Entity) entity = (Entity) sender;
            else
            {
                sender.sendMessage(new TextComponentString("Tried to teleport non-entity sender: " + sender.getClass().getSimpleName()));
                return false;
            }
        }


        DimensionType dimType = entity.world.provider.getDimensionType();
        if (!Tools.contains(InstanceTypes.instanceTypes, dimType))
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


        if (sender == null) sender = server;

        if (args.length < 1)
        {
            sender.sendMessage(new TextComponentString(Instances.MODID + ".commands.dimtp.usage"));
            return false;
        }

        byte b0 = 2;

        boolean dimensionThere = false;
        int dimensionID = 0;
        try
        {
            dimensionID = Integer.parseInt(args[0]);
            dimensionThere = true;
        }
        catch (NumberFormatException exception)
        {
            for (Map.Entry<Integer, InstanceWorldInfo> entry : InstanceHandler.instanceInfo.entrySet())
            {
                if (entry.getValue().getWorldName().equals(args[0]))
                {
                    dimensionID = entry.getKey();
                    dimensionThere = true;
                }
            }
        }

        if (dimensionThere)
        {
            if (args.length == 1)
            {
                if (entity instanceof EntityPlayerMP)
                {
                    EntityPlayerMP player = (EntityPlayerMP) entity;

                    if (player.dimension != dimensionID)
                    {
                        return teleportEntityToDimension(server, sender, player, dimensionID) != null;
                    }

                    return false; //Same dimension and no coords; do nothing
                }
            }

            if (args.length == 5 || args.length == 7)
            {
                try
                {
                    entity = CommandBase.getEntity(server, sender, args[1]);
                }
                catch (CommandException e)
                {
                    sender.sendMessage(new TextComponentString("Could not find entity \"" + args[1]));
                    return false;
                }
                b0 = 2;
            }
            else
            {
                if (!(entity instanceof EntityPlayerMP))
                {
                    sender.sendMessage(new TextComponentString("Entity is not a player: " + entity.getName()));
                    return false;
                }
                b0 = 1;
            }
        }
        else
        {
            if (args.length == 1)
            {
                if (!(entity instanceof EntityPlayerMP))
                {
                    sender.sendMessage(new TextComponentString("Entity is not a player: " + entity.getName()));
                    return false;
                }
            }
            else if (args.length == 2)
            {
                try
                {
                    entity = CommandBase.getEntity(server, entity, args[0]);
                }
                catch (CommandException e)
                {
                    sender.sendMessage(new TextComponentString("Could not find entity \"" + args[0]));
                    return false;
                }
            }
        }

        if (args.length != 1 && args.length != 2)
        {
            if (args.length < b0 + 3 || !dimensionThere)
            {
                sender.sendMessage(new TextComponentString(Instances.MODID + ".commands.dimtp.usage"));
                return false;
            }

            int i = b0 + 1;
            CommandBase.CoordinateArg coordinatearg, coordinatearg1, coordinatearg2, coordinatearg3, coordinatearg4;
            try
            {
                coordinatearg = CommandBase.parseCoordinate(entity.posX, args[b0], true);
                coordinatearg1 = CommandBase.parseCoordinate(entity.posY, args[i++], 0, 0, false);
                coordinatearg2 = CommandBase.parseCoordinate(entity.posZ, args[i++], true);
                coordinatearg3 = CommandBase.parseCoordinate(entity.rotationYaw, args.length > i ? args[i++] : "~", false);
                coordinatearg4 = CommandBase.parseCoordinate(entity.rotationPitch, args.length > i ? args[i] : "~", false);
            }
            catch (NumberInvalidException e)
            {
                sender.sendMessage(new TextComponentString(Instances.MODID + ".commands.dimtp.usage"));
                return false;
            }

            if (entity.dimension != dimensionID)
            {
                entity = teleportEntityToDimension(server, sender, entity, dimensionID);
                if (entity == null) return false;
            }

            float f;
            if (entity instanceof EntityPlayerMP)
            {
                EnumSet enumset = EnumSet.noneOf(SPacketPlayerPosLook.EnumFlags.class);

                if (coordinatearg.isRelative())
                {
                    enumset.add(SPacketPlayerPosLook.EnumFlags.X);
                }

                if (coordinatearg1.isRelative())
                {
                    enumset.add(SPacketPlayerPosLook.EnumFlags.Y);
                }

                if (coordinatearg2.isRelative())
                {
                    enumset.add(SPacketPlayerPosLook.EnumFlags.Z);
                }

                if (coordinatearg4.isRelative())
                {
                    enumset.add(SPacketPlayerPosLook.EnumFlags.X_ROT);
                }

                if (coordinatearg3.isRelative())
                {
                    enumset.add(SPacketPlayerPosLook.EnumFlags.Y_ROT);
                }

                f = (float) coordinatearg3.getAmount();

                if (!coordinatearg3.isRelative())
                {
                    f = MathHelper.wrapDegrees(f);
                }

                float f1 = (float) coordinatearg4.getAmount();

                if (!coordinatearg4.isRelative())
                {
                    f1 = MathHelper.wrapDegrees(f1);
                }

                if (f1 > 90 || f1 < -90)
                {
                    f1 = MathHelper.wrapDegrees(180 - f1);
                    f = MathHelper.wrapDegrees(f + 180);
                }

                entity.dismountRidingEntity();
                ((EntityPlayerMP) entity).connection.setPlayerLocation(coordinatearg.getAmount(), coordinatearg1.getAmount(), coordinatearg2.getAmount(), f, f1, enumset);
                entity.setRotationYawHead(f);
            }
            else
            {
                float f2 = (float) MathHelper.wrapDegrees(coordinatearg3.getResult());
                f = (float) MathHelper.wrapDegrees(coordinatearg4.getResult());

                if (f > 90 || f < -90)
                {
                    f = MathHelper.wrapDegrees(180 - f);
                    f2 = MathHelper.wrapDegrees(f2 + 180);
                }

                entity.setLocationAndAngles(coordinatearg.getResult(), coordinatearg1.getResult(), coordinatearg2.getResult(), f2, f);
                entity.setRotationYawHead(f2);

                entity.world.updateEntityWithOptionalForce(entity, false);
            }

            if (command != null) CommandBase.notifyCommandListener(sender, command, "commands.tp.success.coordinates", entity.getName(), coordinatearg.getResult(), coordinatearg1.getResult(), coordinatearg2.getResult());
            return true;
        }
        else
        {
            Entity entity2;
            try
            {
                entity2 = CommandBase.getEntity(server, entity, args[args.length - 1]);
            }
            catch (CommandException e)
            {
                sender.sendMessage(new TextComponentString("Could not find entity \"" + args[args.length - 1]));
                return false;
            }

            if (entity2.world != entity.world)
            {
                if (entity2.dimension != entity.dimension)
                {
                    entity = teleportEntityToDimension(server, sender, entity, entity2.dimension);
                    if (entity == null) return false;
                }
            }

            entity.dismountRidingEntity();

            if (entity instanceof EntityPlayerMP)
            {
                ((EntityPlayerMP) entity).connection.setPlayerLocation(entity2.posX, entity2.posY, entity2.posZ, entity2.rotationYaw, entity2.rotationPitch);
            }
            else
            {
                entity.setLocationAndAngles(entity2.posX, entity2.posY, entity2.posZ, entity2.rotationYaw, entity2.rotationPitch);
            }

            if (command != null) CommandBase.notifyCommandListener(sender, command, "commands.tp.success", entity.getName(), entity2.getName());
            return true;
        }
    }

    private static Entity teleportEntityToDimension(MinecraftServer server, ICommandSender sender, Entity entity, int dimension)
    {
        World world = server.getWorld(dimension);

        if (world == null)
        {
            if (sender != null) sender.sendMessage(new TextComponentString("Couldn't find dimension " + dimension));
            return null;
        }

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

    private static Entity travelEntity(MinecraftServer server, Entity entity, int dimensionId)
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
