package lumien.simpledimensions.server.commands;

import java.util.EnumSet;
import java.util.List;

import lumien.simpledimensions.util.TeleporterSimple;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class CommandTeleportD extends CommandBase
{

	public String getCommandName()
	{
		return "tpd";
	}

	public int getRequiredPermissionLevel()
	{
		return 2;
	}

	public String getCommandUsage(ICommandSender sender)
	{
		return "simpleDimensions.commands.tpd.usage";
	}

	public void processCommand(ICommandSender sender, String[] args) throws CommandException
	{
		if (args.length < 1)
		{
			throw new WrongUsageException("simpleDimensions.commands.tpd.usage", new Object[0]);
		}
		else
		{
			byte b0 = 2;
			Object object = null;

			boolean dimensionThere = false;
			int dimensionID = 0;
			if (args.length > 0)
			{
				try
				{
					dimensionID = Integer.parseInt(args[0]);
					dimensionThere = true;
				}
				catch (NumberFormatException exception)
				{

				}
			}

			if (dimensionThere)
			{
				if (args.length == 1)
				{
					object = sender;
					if (object instanceof EntityPlayerMP)
					{
						EntityPlayerMP player = (EntityPlayerMP) object;

						if (player.dimension != dimensionID)
						{
							teleportEntityToDimension(player, dimensionID);
						}

						return;
					}
				}

				if (args.length == 5 || args.length == 7)
				{
					object = getEntity(sender, args[1]);
					b0 = 2;
				}
				else
				{
					object = getCommandSenderAsPlayer(sender);
					b0 = 1;
				}
			}
			else
			{
				if (args.length == 1)
				{
					object = getCommandSenderAsPlayer(sender);
				}
				else if (args.length == 2)
				{
					object = getEntity(sender, args[0]);
				}
			}

			if (args.length != 1 && args.length != 2)
			{
				if (args.length < b0 + 3 || !dimensionThere)
				{
					throw new WrongUsageException("simpleDimensions.commands.tpd.usage", new Object[0]);
				}
				else if (((Entity) object).worldObj != null)
				{
					int i = b0 + 1;
					CommandBase.CoordinateArg coordinatearg = parseCoordinate(((Entity) object).posX, args[b0], true);
					CommandBase.CoordinateArg coordinatearg1 = parseCoordinate(((Entity) object).posY, args[i++], 0, 0, false);
					CommandBase.CoordinateArg coordinatearg2 = parseCoordinate(((Entity) object).posZ, args[i++], true);
					CommandBase.CoordinateArg coordinatearg3 = parseCoordinate((double) ((Entity) object).rotationYaw, args.length > i ? args[i++] : "~", false);
					CommandBase.CoordinateArg coordinatearg4 = parseCoordinate((double) ((Entity) object).rotationPitch, args.length > i ? args[i] : "~", false);
					float f;

					if (((Entity) object).dimension != dimensionID)
					{
						object = teleportEntityToDimension((Entity) object, dimensionID);
					}

					if (object instanceof EntityPlayerMP)
					{
						EnumSet enumset = EnumSet.noneOf(S08PacketPlayerPosLook.EnumFlags.class);

						if (coordinatearg.func_179630_c())
						{
							enumset.add(S08PacketPlayerPosLook.EnumFlags.X);
						}

						if (coordinatearg1.func_179630_c())
						{
							enumset.add(S08PacketPlayerPosLook.EnumFlags.Y);
						}

						if (coordinatearg2.func_179630_c())
						{
							enumset.add(S08PacketPlayerPosLook.EnumFlags.Z);
						}

						if (coordinatearg4.func_179630_c())
						{
							enumset.add(S08PacketPlayerPosLook.EnumFlags.X_ROT);
						}

						if (coordinatearg3.func_179630_c())
						{
							enumset.add(S08PacketPlayerPosLook.EnumFlags.Y_ROT);
						}

						f = (float) coordinatearg3.func_179629_b();

						if (!coordinatearg3.func_179630_c())
						{
							f = MathHelper.wrapAngleTo180_float(f);
						}

						float f1 = (float) coordinatearg4.func_179629_b();

						if (!coordinatearg4.func_179630_c())
						{
							f1 = MathHelper.wrapAngleTo180_float(f1);
						}

						if (f1 > 90.0F || f1 < -90.0F)
						{
							f1 = MathHelper.wrapAngleTo180_float(180.0F - f1);
							f = MathHelper.wrapAngleTo180_float(f + 180.0F);
						}

						((Entity) object).mountEntity((Entity) null);
						((EntityPlayerMP) object).playerNetServerHandler.setPlayerLocation(coordinatearg.func_179629_b(), coordinatearg1.func_179629_b(), coordinatearg2.func_179629_b(), f, f1, enumset);
						((Entity) object).setRotationYawHead(f);
					}
					else
					{
						float f2 = (float) MathHelper.wrapAngleTo180_double(coordinatearg3.func_179628_a());
						f = (float) MathHelper.wrapAngleTo180_double(coordinatearg4.func_179628_a());

						if (f > 90.0F || f < -90.0F)
						{
							f = MathHelper.wrapAngleTo180_float(180.0F - f);
							f2 = MathHelper.wrapAngleTo180_float(f2 + 180.0F);
						}

						((Entity) object).setLocationAndAngles(coordinatearg.func_179628_a(), coordinatearg1.func_179628_a(), coordinatearg2.func_179628_a(), f2, f);
						((Entity) object).setRotationYawHead(f2);

						((Entity) object).worldObj.updateEntityWithOptionalForce((Entity) object, false);
					}

					notifyOperators(sender, this, "commands.tp.success.coordinates", new Object[] { ((Entity) object).getName(), Double.valueOf(coordinatearg.func_179628_a()), Double.valueOf(coordinatearg1.func_179628_a()), Double.valueOf(coordinatearg2.func_179628_a()) });
				}
			}
			else
			{
				Entity entity = getEntity(sender, args[args.length - 1]);

				if (entity.worldObj != ((Entity) object).worldObj)
				{
					Entity toTeleport = (Entity) object;
					if (entity.dimension != toTeleport.dimension)
					{
						object = teleportEntityToDimension(toTeleport, entity.dimension);
					}
				}

				((Entity) object).mountEntity((Entity) null);

				if (object instanceof EntityPlayerMP)
				{
					((EntityPlayerMP) object).playerNetServerHandler.setPlayerLocation(entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch);
				}
				else
				{
					((Entity) object).setLocationAndAngles(entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch);
				}

				notifyOperators(sender, this, "commands.tp.success", new Object[] { ((Entity) object).getName(), entity.getName() });
			}
		}
	}

	private Entity teleportEntityToDimension(Entity entity, int dimension) throws CommandException
	{
		World worldObj = MinecraftServer.getServer().worldServerForDimension(dimension);

		if (worldObj == null)
		{
			throw new CommandException("Couldn't find dimension " + dimension);
		}

		if (entity instanceof EntityPlayerMP)
		{
			MinecraftServer.getServer().getConfigurationManager().transferPlayerToDimension((EntityPlayerMP) entity, dimension, new TeleporterSimple((WorldServer) MinecraftServer.getServer().getEntityWorld()));
			return entity;
		}
		else
		{
			return travelEntity(entity, dimension);
		}
	}

	private Entity travelEntity(Entity entity, int dimensionId)
	{
		if (!entity.worldObj.isRemote && !entity.isDead)
		{
			entity.worldObj.theProfiler.startSection("changeDimension");
			MinecraftServer minecraftserver = MinecraftServer.getServer();
			int j = entity.dimension;
			WorldServer worldserver = minecraftserver.worldServerForDimension(j);
			WorldServer worldserver1 = minecraftserver.worldServerForDimension(dimensionId);
			entity.dimension = dimensionId;

			Entity newEntity = EntityList.createEntityByName(EntityList.getEntityString(entity), worldserver1);

			if (newEntity != null)
			{
				newEntity.copyDataFromOld(entity);
				entity.worldObj.removeEntity(entity);

				newEntity.forceSpawn = true;

				worldserver1.spawnEntityInWorld(newEntity);
			}

			worldserver1.updateEntityWithOptionalForce(newEntity, true);

			entity.isDead = true;
			entity.worldObj.theProfiler.endSection();
			worldserver.resetUpdateEntityTick();
			worldserver1.resetUpdateEntityTick();
			entity.worldObj.theProfiler.endSection();
			return newEntity;
		}

		return entity;
	}

	public List addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos)
	{
		return args.length != 1 && args.length != 2 ? null : getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames());
	}

	public boolean isUsernameIndex(String[] args, int index)
	{
		return index == 0 || index == 1;
	}
}