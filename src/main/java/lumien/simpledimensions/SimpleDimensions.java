package lumien.simpledimensions;

import lumien.simpledimensions.client.ClientHandler;
import lumien.simpledimensions.client.models.ItemModels;
import lumien.simpledimensions.config.SimpleDimensionsConfig;
import lumien.simpledimensions.dimensions.DimensionHandler;
import lumien.simpledimensions.item.ModItems;
import lumien.simpledimensions.lib.Reference;
import lumien.simpledimensions.modcomp.oc.OpenComputers;
import lumien.simpledimensions.network.PacketHandler;
import lumien.simpledimensions.server.commands.CommandSimpleDimensions;
import lumien.simpledimensions.server.commands.CommandTeleportD;
import lumien.simpledimensions.server.commands.CommandTimeD;
import lumien.simpledimensions.server.commands.CommandWeatherD;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ServerConnectionFromClientEvent;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.MOD_VERSION)
public class SimpleDimensions
{
	@Instance(value = Reference.MOD_ID)
	public static SimpleDimensions INSTANCE;

	@SidedProxy(clientSide = "lumien.simpledimensions.client.ClientProxy", serverSide = "lumien.simpledimensions.CommonProxy")
	public static CommonProxy proxy;

	public SimpleDimensionsConfig config;

	public CreativeTabs creativeTab;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		creativeTab = new CreativeTabSimpleDimensions();

		MinecraftForge.EVENT_BUS.register(this);

		config = new SimpleDimensionsConfig();

		config.preInit(event);

		ModItems.preInit(event);

		proxy.registerModels();
	}

	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		PacketHandler.init();

		if (Loader.isModLoaded("OpenComputers"))
		{
			OpenComputers.init(event);
		}
	}

	@EventHandler
	public void serverStarting(FMLServerStartingEvent event)
	{
		event.registerServerCommand(new CommandSimpleDimensions());
		event.registerServerCommand(new CommandWeatherD());
		event.registerServerCommand(new CommandTimeD());
		event.registerServerCommand(new CommandTeleportD());

		DimensionHandler.getInstance().loadDimensions();
	}

	@SubscribeEvent
	public void worldUnload(WorldEvent.Unload event)
	{
		int dimensionID = event.world.provider.getDimensionId();

		if (!event.world.isRemote)
		{
			DimensionHandler.getInstance().unload(event.world, dimensionID);
		}
	}

	@SubscribeEvent
	public void clientConnect(ServerConnectionFromClientEvent event)
	{
		event.manager.sendPacket(PacketHandler.INSTANCE.getPacketFrom(DimensionHandler.getInstance().constructSyncMessage()));
	}

	@SubscribeEvent
	public void clientDisconnect(ClientDisconnectionFromServerEvent event)
	{
		if (!event.manager.isLocalChannel())
		{
			ClientHandler.getInstance().cleanUp();
		}
	}
}
