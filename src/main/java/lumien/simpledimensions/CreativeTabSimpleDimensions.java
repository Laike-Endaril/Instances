package lumien.simpledimensions;

import lumien.simpledimensions.item.ModItems;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class CreativeTabSimpleDimensions extends CreativeTabs
{

	public CreativeTabSimpleDimensions()
	{
		super("simpledimensions");
	}

	@Override
	public Item getTabIconItem()
	{
		return ModItems.simpleDimensionsCard;
	}

}
