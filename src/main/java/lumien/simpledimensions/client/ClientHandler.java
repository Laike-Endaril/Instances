package lumien.simpledimensions.client;

import java.util.ArrayList;
import java.util.HashSet;

import lumien.simpledimensions.SimpleDimensions;
import lumien.simpledimensions.server.WorldProviderSimpleDimension;
import net.minecraftforge.common.DimensionManager;

public class ClientHandler
{
	static ClientHandler INSTANCE;

	HashSet<Integer> simpleDimensions;

	public ClientHandler()
	{
		simpleDimensions = new HashSet<Integer>();
	}

	public void cleanUp()
	{
		for (Integer i : simpleDimensions)
		{
			if (DimensionManager.isDimensionRegistered(i))
			{
				DimensionManager.unregisterDimension(i);
			}
		}
	}

	public static ClientHandler getInstance()
	{
		if (INSTANCE == null)
		{
			INSTANCE = new ClientHandler();
		}

		return INSTANCE;
	}

	public void sync(ArrayList<Integer> dimensions)
	{
		this.cleanUp();

		this.simpleDimensions = new HashSet<Integer>();
		this.simpleDimensions.addAll(dimensions);

		for (Integer i : simpleDimensions)
		{
			if (!DimensionManager.isDimensionRegistered(i))
			{
				DimensionManager.registerDimension(i, SimpleDimensions.INSTANCE.simpleDimensionType);
			}
		}
	}
}
