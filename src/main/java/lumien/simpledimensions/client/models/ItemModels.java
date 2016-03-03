package lumien.simpledimensions.client.models;

import lumien.simpledimensions.item.ModItems;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ItemModels
{
	public static void register()
	{
		registerItem(ModItems.simpleDimensionsCard);
	}
	
	private static void registerItem(Item i)
	{
		String register = "simpledimensions:" + GameRegistry.findUniqueIdentifierFor(i).name;
		ModelLoader.setCustomModelResourceLocation(i, 0, new ModelResourceLocation(register, "inventory"));
	}
}
