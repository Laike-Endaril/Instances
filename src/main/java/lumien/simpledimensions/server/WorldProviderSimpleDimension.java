package lumien.simpledimensions.server;

import lumien.simpledimensions.dimensions.DimensionHandler;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.WorldSettings;

public class WorldProviderSimpleDimension extends WorldProviderSurface
{
	@Override
	public String getDimensionName()
	{
		return DimensionHandler.getInstance().getDimensionName(this.dimensionId);
	}

	@Override
	public String getInternalNameSuffix()
	{
		return "_"+getDimensionName().toLowerCase();
	}
}
