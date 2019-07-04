package com.fantasticsource.instances.server.commands;

import com.fantasticsource.instances.Instances;
import com.fantasticsource.instances.server.InstanceHandler;
import com.fantasticsource.instances.util.TeleporterSimple;
import com.fantasticsource.instances.util.WorldInfoSimple;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

public class CommandTeleportD extends CommandBase
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

    public static void tpd(CommandTeleportD command, MinecraftServer server, ICommandSender sender, Entity entity, String[] args) throws CommandException
    {
        if (entity == null)
        {
            if (sender instanceof Entity) entity = (Entity) sender;
            else throw new WrongUsageException(Instances.MODID + ".commands.tpd.usage");
        }

        if (args.length < 1)
        {
            throw new WrongUsageException(Instances.MODID + ".commands.tpd.usage");
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
                        teleportEntityToDimension(server, player, dimensionID);
                    }

                    return;
                }
            }

            if (args.length == 5 || args.length == 7)
            {
                entity = getEntity(server, entity, args[1]);
                b0 = 2;
            }
            else
            {
                entity = getCommandSenderAsPlayer(entity);
                b0 = 1;
            }
        }
        else
        {
            if (args.length == 1)
            {
                entity = getCommandSenderAsPlayer(entity);
            }
            else if (args.length == 2)
            {
                entity = getEntity(server, entity, args[0]);
            }
        }

        if (args.length != 1 && args.length != 2)
        {
            if (args.length < b0 + 3 || !dimensionThere)
            {
                throw new WrongUsageException(Instances.MODID + ".commands.tpd.usage");
            }
            else if (entity.world != null)
            {
                int i = b0 + 1;
                CommandBase.CoordinateArg coordinatearg = parseCoordinate(entity.posX, args[b0], true);
                CommandBase.CoordinateArg coordinatearg1 = parseCoordinate(entity.posY, args[i++], 0, 0, false);
                CommandBase.CoordinateArg coordinatearg2 = parseCoordinate(entity.posZ, args[i++], true);
                CommandBase.CoordinateArg coordinatearg3 = parseCoordinate(entity.rotationYaw, args.length > i ? args[i++] : "~", false);
                CommandBase.CoordinateArg coordinatearg4 = parseCoordinate(entity.rotationPitch, args.length > i ? args[i] : "~", false);
                float f;

                if (entity.dimension != dimensionID)
                {
                    entity = teleportEntityToDimension(server, entity, dimensionID);
                }

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
            }
        }
        else
        {
            Entity entity2 = getEntity(server, entity, args[args.length - 1]);

            if (entity2.world != entity.world)
            {
                if (entity2.dimension != entity.dimension)
                {
                    entity = teleportEntityToDimension(server, entity, entity2.dimension);
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
        }
    }

    private static Entity teleportEntityToDimension(MinecraftServer server, Entity entity, int dimension) throws CommandException
    {
        World world = server.getWorld(dimension);

        if (world == null)
        {
            throw new CommandException("Couldn't find dimension " + dimension);
        }

        if (entity instanceof EntityPlayerMP)
        {
            server.getPlayerList().transferPlayerToDimension((EntityPlayerMP) entity, dimension, new TeleporterSimple((WorldServer) server.getEntityWorld()));
            return entity;
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

    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
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
