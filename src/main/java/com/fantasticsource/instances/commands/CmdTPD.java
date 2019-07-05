package com.fantasticsource.instances.commands;

import com.fantasticsource.instances.Instances;
import com.fantasticsource.instances.blocksanditems.tileentity.TEInstancePortal;
import com.fantasticsource.instances.world.InstanceHandler;
import com.fantasticsource.instances.world.WorldInfoSimple;
import com.fantasticsource.instances.world.dimensions.InstanceTypes;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.DimensionType;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.lang.reflect.Method;
import java.util.*;

public class CmdTPD extends CommandBase
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

    public static boolean tpd(Entity entity, TEInstancePortal.Destination destination)
    {
        //TODO change yaw and pitch to destination.yaw and destination.pitch and figure out the spinny camera bug it causes in dedicated mode (angle desync?)
        return tpd(entity, destination.dimension, destination.x, destination.y, destination.z, entity.rotationYaw, entity.rotationPitch);
    }

    public static boolean tpd(Entity entity, int dimension, double x, double y, double z, float yaw, float pitch)
    {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        return tpd(null, server, server, entity, new String[]{"" + dimension, "" + x, "" + y, "" + z, "" + yaw, "" + pitch});
    }

    public static boolean tpd(CmdTPD command, MinecraftServer server, ICommandSender sender, Entity entity, String[] args)
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
        if (dimType != InstanceTypes.skyroomDimType && dimType != InstanceTypes.skyhubDimType)
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
            sender.sendMessage(new TextComponentString(Instances.MODID + ".commands.tpd.usage"));
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
            for (Map.Entry<Integer, WorldInfoSimple> entry : InstanceHandler.instanceInfo.entrySet())
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
                    entity = getEntity(server, sender, args[1]);
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
                    entity = getEntity(server, entity, args[0]);
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
                sender.sendMessage(new TextComponentString(Instances.MODID + ".commands.tpd.usage"));
                return false;
            }

            int i = b0 + 1;
            CoordinateArg coordinatearg, coordinatearg1, coordinatearg2, coordinatearg3, coordinatearg4;
            try
            {
                coordinatearg = parseCoordinate(entity.posX, args[b0], true);
                coordinatearg1 = parseCoordinate(entity.posY, args[i++], 0, 0, false);
                coordinatearg2 = parseCoordinate(entity.posZ, args[i++], true);
                coordinatearg3 = parseCoordinate(entity.rotationYaw, args.length > i ? args[i++] : "~", false);
                coordinatearg4 = parseCoordinate(entity.rotationPitch, args.length > i ? args[i] : "~", false);
            }
            catch (NumberInvalidException e)
            {
                sender.sendMessage(new TextComponentString(Instances.MODID + ".commands.tpd.usage"));
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

            if (command != null) notifyCommandListener(sender, command, "commands.tp.success.coordinates", entity.getName(), coordinatearg.getResult(), coordinatearg1.getResult(), coordinatearg2.getResult());
            return true;
        }
        else
        {
            Entity entity2;
            try
            {
                entity2 = getEntity(server, entity, args[args.length - 1]);
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

            if (command != null) notifyCommandListener(sender, command, "commands.tp.success", entity.getName(), entity2.getName());
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

            server.getPlayerList().transferPlayerToDimension(player, dimension, new TeleporterSimple((WorldServer) server.getEntityWorld()));

            WorldInfoSimple info = InstanceHandler.get(dimension);
            if (info != null)
            {
                if (info.getOwner().equals(player.getPersistentID())) player.setGameType(GameType.SURVIVAL);
                else player.setGameType(GameType.ADVENTURE);
            }
            else player.setGameType(DimensionManager.getWorld(dimension).getWorldInfo().getGameType());

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

    public String getName()
    {
        return "tpd";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    public String getUsage(ICommandSender sender)
    {
        return Instances.MODID + ".commands.tpd.usage";
    }

    public void execute(MinecraftServer server, ICommandSender sender, String[] args)
    {
        tpd(this, server, sender, null, args);
    }

    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos)
    {
        if (args.length == 1)
        {
            ArrayList<String> strings = new ArrayList<>();

            for (WorldInfoSimple info : InstanceHandler.instanceInfo.values()) strings.add(info.getWorldName());
            for (int id : DimensionManager.getIDs()) strings.add("" + id);

            return getListOfStringsMatchingLastWord(args, strings);
        }
        return new ArrayList<>();
    }

    public boolean isUsernameIndex(String[] args, int index)
    {
        return index == 0 || index == 1;
    }
}
