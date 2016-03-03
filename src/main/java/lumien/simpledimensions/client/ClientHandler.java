package lumien.simpledimensions.client;

import java.util.ArrayList;
import java.util.HashSet;

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

			try
			{
				DimensionManager.unregisterProviderType(i);
			}
			catch (IllegalArgumentException e)
			{
				// Ignore if it doesn't exist
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
				DimensionManager.registerProviderType(i, WorldProviderSimpleDimension.class, true);
				DimensionManager.registerDimension(i, i);
			}
		}
	}
}
