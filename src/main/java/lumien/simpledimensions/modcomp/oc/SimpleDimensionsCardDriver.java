package lumien.simpledimensions.modcomp.oc;

import java.util.HashMap;
import java.util.Map.Entry;

import li.cil.oc.api.Network;
import li.cil.oc.api.driver.EnvironmentHost;
import li.cil.oc.api.driver.item.Slot;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.network.Visibility;
import li.cil.oc.api.prefab.DriverItem;
import lumien.simpledimensions.dimensions.DimensionHandler;
import lumien.simpledimensions.item.ModItems;
import lumien.simpledimensions.util.WorldInfoSimple;
import net.minecraft.item.ItemStack;

public class SimpleDimensionsCardDriver extends DriverItem
{
	public SimpleDimensionsCardDriver()
	{
		super(new ItemStack(ModItems.simpleDimensionsCard));
	}

	@Override
	public ManagedEnvironment createEnvironment(ItemStack stack, EnvironmentHost host)
	{
		return new Environment(host);
	}

	@Override
	public String slot(ItemStack stack)
	{
		return Slot.Card;
	}

	public class Environment extends li.cil.oc.api.prefab.ManagedEnvironment
	{
		protected final EnvironmentHost host;

		public Environment(EnvironmentHost host)
		{
			this.host = host;
			setNode(Network.newNode(this, Visibility.Neighbors).withComponent("simpledimensions").create());
		}

		@Callback()
		public Object[] getSimpleDimensions(Context context, Arguments args)
		{
			HashMap<Integer, String> dimensions = new HashMap<Integer, String>();

			for (Entry<Integer, WorldInfoSimple> entry : DimensionHandler.getInstance().getDimensionInfo().entrySet())
			{
				dimensions.put(entry.getKey(), entry.getValue().getWorldName());
			}

			return new Object[] { dimensions };
		}
	}
}
