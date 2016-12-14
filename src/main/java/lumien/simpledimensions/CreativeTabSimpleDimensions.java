package lumien.simpledimensions;

import lumien.simpledimensions.item.ModItems;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class CreativeTabSimpleDimensions extends CreativeTabs
{

	public CreativeTabSimpleDimensions()
	{
		super("simpledimensions");
	}

	@Override
	public ItemStack getTabIconItem()
	{
		return new ItemStack(ModItems.simpleDimensionsCard);
	}

}
