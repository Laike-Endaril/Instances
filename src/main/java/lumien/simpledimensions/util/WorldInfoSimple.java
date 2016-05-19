package lumien.simpledimensions.util;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class WorldInfoSimple extends WorldInfo
{
	WorldInfo superInfo;

	public WorldInfoSimple(NBTTagCompound nbt)
	{
		super(nbt);

		superInfo = FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld().getWorldInfo();
	}

	public WorldInfoSimple(WorldSettings settings, String name)
	{
		super(settings, name);
	}

	@Override
	public NBTTagCompound getPlayerNBTTagCompound()
	{
		return superInfo.getPlayerNBTTagCompound();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public long getLastTimePlayed()
	{
		return superInfo.getLastTimePlayed();
	}

	@Override
	public WorldSettings.GameType getGameType()
	{
		return superInfo.getGameType();
	}

	@Override
	public boolean isHardcoreModeEnabled()
	{
		return superInfo.isHardcoreModeEnabled();
	}

	@Override
	public boolean areCommandsAllowed()
	{
		return superInfo.areCommandsAllowed();
	}

	@Override
	public GameRules getGameRulesInstance()
	{
		return superInfo.getGameRulesInstance();
	}

	@Override
	public EnumDifficulty getDifficulty()
	{
		return superInfo.getDifficulty();
	}

	@Override
	public boolean isDifficultyLocked()
	{
		return superInfo.isDifficultyLocked();
	}
}
