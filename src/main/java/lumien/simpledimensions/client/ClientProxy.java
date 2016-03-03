package lumien.simpledimensions.client;

import lumien.simpledimensions.CommonProxy;
import lumien.simpledimensions.client.models.ItemModels;

public class ClientProxy extends CommonProxy
{
	public void registerModels()
	{
		ItemModels.register();
	}
}
