package lumien.simpledimensions.client.models;

import lumien.simpledimensions.item.ModItems;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;

public class ItemModels
{
	public static void register()
	{
		registerItem(ModItems.simpleDimensionsCard);
	}
	
	private static void registerItem(Item i)
	{
		ModelLoader.setCustomModelResourceLocation(i, 0, new ModelResourceLocation(i.getRegistryName(), "inventory"));
	}
}
