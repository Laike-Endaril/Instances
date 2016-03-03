package lumien.simpledimensions.modcomp.oc;

import li.cil.oc.api.Driver;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class OpenComputers
{
	public static void init(FMLInitializationEvent event)
	{
		Driver.add(new SimpleDimensionsCardDriver());
	}
}
