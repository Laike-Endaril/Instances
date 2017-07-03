package lumien.simpledimensions.item.items;

import java.util.List;

import javax.annotation.Nullable;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import lumien.simpledimensions.SimpleDimensions;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;

public class ItemSimpleDimensionsCard extends Item
{
	public ItemSimpleDimensionsCard()
	{
		this.setCreativeTab(SimpleDimensions.INSTANCE.creativeTab);
		this.setUnlocalizedName("simpledimensions.card");
		this.setRegistryName(new ResourceLocation("simpledimensions","simpleDimensionsCard"));
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable World playerIn, List<String> tooltip, ITooltipFlag advanced)
	{
		super.addInformation(stack, playerIn, tooltip, advanced);

		tooltip.add("Creative Item");
	}

	@Override
	public EnumRarity getRarity(ItemStack stack)
	{
		return EnumRarity.EPIC;
	}
}
