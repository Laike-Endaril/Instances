package lumien.simpledimensions.item;

import lumien.simpledimensions.item.items.ItemSimpleDimensionsCard;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ModItems
{
	public static Item simpleDimensionsCard;

	public static void preInit(FMLPreInitializationEvent event)
	{
		simpleDimensionsCard = new ItemSimpleDimensionsCard();
		ForgeRegistries.ITEMS.register(simpleDimensionsCard);
	}
}
