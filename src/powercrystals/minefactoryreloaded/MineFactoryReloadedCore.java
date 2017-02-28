package powercrystals.minefactoryreloaded;

//this import brought to you by the department of redundancies department, the department that brought you this import
import cofh.CoFHCore;
import cofh.core.world.WorldHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDispenser;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.entity.EntityList;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.CustomProperty;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.event.FMLInterModComms.IMCEvent;
import net.minecraftforge.fml.common.event.FMLMissingMappingsEvent.MissingMapping;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.VillagerRegistry;
import org.apache.logging.log4j.Logger;
import powercrystals.minefactoryreloaded.block.fluid.BlockExplodingFluid;
import powercrystals.minefactoryreloaded.block.fluid.BlockFactoryFluid;
import powercrystals.minefactoryreloaded.block.fluid.BlockPinkSlimeFluid;
import powercrystals.minefactoryreloaded.farmables.MFRFarmables;
import powercrystals.minefactoryreloaded.gui.MFRGUIHandler;
import powercrystals.minefactoryreloaded.net.CommonProxy;
import powercrystals.minefactoryreloaded.net.EntityHandler;
import powercrystals.minefactoryreloaded.net.MFRPacket;
import powercrystals.minefactoryreloaded.setup.*;
import powercrystals.minefactoryreloaded.setup.recipe.EnderIO;
import powercrystals.minefactoryreloaded.setup.recipe.Vanilla;
import powercrystals.minefactoryreloaded.setup.village.VillageCreationHandler;
import powercrystals.minefactoryreloaded.setup.village.Zoologist;
import powercrystals.minefactoryreloaded.tile.machine.TileEntityUnifier;
import powercrystals.minefactoryreloaded.world.MineFactoryReloadedWorldGen;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import static powercrystals.minefactoryreloaded.MineFactoryReloadedCore.*;
import static powercrystals.minefactoryreloaded.setup.MFRThings.*;

@Mod(modid = modId, name = modName, version = version, dependencies = dependencies,
		customProperties = @CustomProperty(k = "cofhversion", v = "true"))
public class MineFactoryReloadedCore extends BaseMod {

	static{FluidRegistry.enableUniversalBucket();}
	public static final String modId = "minefactoryreloaded";
	public static final String modName = "MineFactory Reloaded";
	public static final String version = "1.7.10R2.8.2B1";
	public static final String dependencies = CoFHCore.VERSION_GROUP;
	public static final String modNetworkChannel = "MFReloaded";

	@SidedProxy(clientSide = "powercrystals.minefactoryreloaded.net.ClientProxy",
			serverSide = "powercrystals.minefactoryreloaded.net.ServerProxy")
	public static CommonProxy proxy;

	public static SimpleNetworkWrapper networkWrapper = null;

	public static Object balance = "balance";

	public static final String prefix = "minefactoryreloaded:";
	public static final String textureFolder = prefix + "textures/";
	public static final String guiFolder = textureFolder + "gui/";
	public static final String hudFolder = textureFolder + "hud/";
	public static final String villagerFolder = textureFolder + "villager/";
	public static final String tileEntityFolder = textureFolder + "tileentity/";
	public static final String mobTextureFolder = textureFolder + "mob/";
	public static final String modelTextureFolder = textureFolder + "itemmodels/";
	public static final String armorTextureFolder = textureFolder + "armor/";
	public static final String modelFolder = prefix + "models/";

	public static final ResourceLocation CHEST_GEN = new ResourceLocation("mfr:villageZoolologist");

	private static MineFactoryReloadedCore instance;
	private LinkedList<Vanilla> recipeSets = new LinkedList<Vanilla>();
	
	public static MineFactoryReloadedCore instance() {

		return instance;
	}

	public static Logger log() {

		return instance.getLogger();
	}

	public static void registerFluids() {

		FluidRegistry.enableUniversalBucket();

		//TODO move this to MFRFluids init
		milk = registerFluid("milk", 1050, EnumRarity.COMMON);
		sludge = registerFluid("sludge", 1700, EnumRarity.COMMON);
		sewage = registerFluid("sewage", 1200, EnumRarity.COMMON);
		essence = registerFluid("mob_essence", 400, 9, 310, EnumRarity.EPIC);
		biofuel = registerFluid("biofuel", 800, EnumRarity.UNCOMMON);
		meat = registerFluid("meat", 2000, EnumRarity.COMMON);
		pinkSlime = registerFluid("pink_slime", 3000, EnumRarity.RARE);
		chocolateMilk = registerFluid("chocolate_milk", 1100, EnumRarity.COMMON);
		mushroomSoup = registerFluid("mushroom_soup", 1500, EnumRarity.COMMON);
		steam = registerFluid("steam", -100, 0, 673, EnumRarity.COMMON);

		FluidRegistry.addBucketForFluid(milk);
		FluidRegistry.addBucketForFluid(sludge);
		FluidRegistry.addBucketForFluid(sewage);
		FluidRegistry.addBucketForFluid(essence);
		FluidRegistry.addBucketForFluid(biofuel);
		FluidRegistry.addBucketForFluid(meat);
		FluidRegistry.addBucketForFluid(pinkSlime);
		FluidRegistry.addBucketForFluid(chocolateMilk);
		FluidRegistry.addBucketForFluid(mushroomSoup);

		milkLiquid = new BlockFactoryFluid("milk");
		sludgeLiquid = new BlockFactoryFluid("sludge");
		sewageLiquid = new BlockFactoryFluid("sewage");
		essenceLiquid = new BlockFactoryFluid("mob_essence");
		biofuelLiquid = new BlockExplodingFluid("biofuel");
		meatLiquid = new BlockFactoryFluid("meat");
		pinkSlimeLiquid = new BlockPinkSlimeFluid("pink_slime");
		chocolateMilkLiquid = new BlockFactoryFluid("chocolate_milk");
		mushroomSoupLiquid = new BlockFactoryFluid("mushroom_soup");
		steamFluid = new BlockFactoryFluid("steam", BlockFactoryFluid.material);

		registerBlock(milkLiquid, new ItemBlock(milkLiquid));
		registerBlock(sludgeLiquid, new ItemBlock(sludgeLiquid));
		registerBlock(sewageLiquid, new ItemBlock(sewageLiquid));
		registerBlock(essenceLiquid, new ItemBlock(essenceLiquid));
		registerBlock(biofuelLiquid, new ItemBlock(biofuelLiquid));
		registerBlock(meatLiquid, new ItemBlock(meatLiquid));
		registerBlock(pinkSlimeLiquid, new ItemBlock(pinkSlimeLiquid));
		registerBlock(chocolateMilkLiquid, new ItemBlock(chocolateMilkLiquid));
		registerBlock(mushroomSoupLiquid, new ItemBlock(mushroomSoupLiquid));
		registerBlock(steamFluid, new ItemBlock(steamFluid));

	}

	public static Fluid registerFluid(String name, int density, EnumRarity rarity) {

		return registerFluid(name, density, -1, -1, rarity);
	}

	public static Fluid registerFluid(String name, int density, int lightValue, int temp, EnumRarity rarity) {

		name = name.toLowerCase(Locale.ENGLISH);
		Fluid fluid = new Fluid(name, new ResourceLocation("minefactoryreloaded:blocks/fluid/fluid.mfr." + name + ".still"), new ResourceLocation("minefactoryreloaded:blocks/fluid/fluid.mfr." + name + ".flowing"));
		if (!FluidRegistry.registerFluid(fluid))
			fluid = FluidRegistry.getFluid(name);
		if (density != 0) {
			fluid.setDensity(density);
			fluid.setViscosity(Math.abs(density)); // works for my purposes
		}
		if (lightValue >= 0)
			fluid.setLuminosity(lightValue);
		if (temp >= 0)
			fluid.setTemperature(temp);
		fluid.setUnlocalizedName("mfr." + name + ".still.name");
		fluid.setRarity(rarity);
		return fluid;
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent evt) throws IOException {

		instance = this;
		setConfigFolderBase(evt.getModConfigurationDirectory());

		MFRConfig.loadClientConfig(getClientConfig());
		MFRConfig.loadCommonConfig(getCommonConfig());

		registerFluids();

		MFRThings.preInit();

		if (MFRConfig.vanillaRecipes.getBoolean(true))
			recipeSets.add(new Vanilla());

/* TODO readd when there's TE
		if (MFRConfig.thermalExpansionRecipes.getBoolean(false))
			recipeSets.add(new ThermalExpansion());
*/

		if (MFRConfig.enderioRecipes.getBoolean(false))
			recipeSets.add(new EnderIO());

		Vanilla.registerOredict();

		loadLang();

		Blocks.FIRE.setFireInfo(biofuelLiquid, 300, 30);

/* TODO stack sizes for door have changed in 1.8, figure out what this is for and if it needs to be readded
		Items.WOODEN_DOOR.setMaxStackSize(8);
		Items.IRON_DOOR.setMaxStackSize(8);
*/

		GameRegistry.registerFuelHandler(new MineFactoryReloadedFuelHandler());
		
		proxy.preInit();
	}

	private static void registerBlock(Block block, ItemBlock itemBlock) {
		
		MFRRegistry.registerBlock(block, itemBlock);		
	}

	@Deprecated
	private void registerBlock(Block block, Class<? extends ItemBlock> item, String[] args) {

		MFRRegistry.registerBlock(block, item, new Object[] { args });
	}

	@Deprecated
	private void registerBlock(Block block, Class<? extends ItemBlock> item, Object... args) {

		MFRRegistry.registerBlock(block, item, args);
	}

	@EventHandler
	public void missingMappings(FMLMissingMappingsEvent e) {

		List<MissingMapping> list = e.get();
		if (list.size() > 0) for (MissingMapping mapping : list) {
			String name = mapping.name;
			if (name.indexOf(':') >= 0)
				name = name.substring(name.indexOf(':') + 1);
			l: switch (mapping.type) {
			case BLOCK:
				Block block = MFRRegistry.remapBlock(name);
				if (block != null)
					mapping.remap(block);
				else if ("tile.null".equals(name))
					mapping.remap(fakeLaserBlock);
				else
					mapping.warn();
				break l;
			case ITEM:
				Item item = MFRRegistry.remapItem(name);
				if (item != null)
					mapping.remap(item);
				else
					mapping.warn();
				break l;
			default:
			}
		}
	}

	@EventHandler
	public void init(FMLInitializationEvent evt) {

		MinecraftForge.EVENT_BUS.register(rednetCableBlock);
		MinecraftForge.EVENT_BUS.register(new EntityHandler());
		MinecraftForge.EVENT_BUS.register(MFRFluids.INSTANCE);

		proxy.init();
		MFRFarmables.load();

		NetworkRegistry.INSTANCE.registerGuiHandler(this, new MFRGUIHandler());

		MFRPacket.initialize();

		addDispenserBehavior();

		MFRLoot.init();

		Zoologist.init();

		//TODO likely remove, but added here at least for the test
		VillagerRegistry.instance().registerVillageCreationHandler(new VillageCreationHandler());

		WorldHandler.instance.registerFeature(new MineFactoryReloadedWorldGen());

		//UpdateManager.registerUpdater(new UpdateManager(this, null, CoFHProps.DOWNLOAD_URL));
	}

	private void addDispenserBehavior() {

		IBehaviorDispenseItem behavior = new BehaviorDispenseSafariNet();
		BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(safariNetItem, behavior);
		BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(safariNetSingleItem, behavior);
		BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(safariNetJailerItem, behavior);
	}

	private void addChestGenItems() {

/*  TODO add dim doors loot tables if needed

		//{ DimensionalDoors chestgen compat
		// reference weights[iron: 160; coal: 120; gold: 80; golden apple: 10]
		ChestGenHooks.getInfo("dimensionalDungeonChest").addItem(
			new WeightedRandomChestContent(new ItemStack(safariNetJailerItem), 1, 1, 15));
		ChestGenHooks.getInfo("dimensionalDungeonChest").addItem(
			new WeightedRandomChestContent(new ItemStack(rubberSaplingBlock), 1, 8, 70));
		ChestGenHooks.getInfo("dimensionalDungeonChest").addItem(
			new WeightedRandomChestContent(new ItemStack(pinkSlimeItem), 1, 1, 1));
		// tempting as a sacred sapling is, chests are too common with too few possible items
		// maybe as a custom dungeon for integration
		///}

		//}
*/
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent evt) {

		TileEntityUnifier.updateUnifierLiquids();

		String[] list = MFRConfig.rubberTreeBiomeWhitelist.getStringList();
		for (String biome : list) {
			MFRRegistry.registerRubberTreeBiome(biome);
		}

		list = MFRConfig.unifierBlacklist.getStringList();
		for (String entry : list) {
			MFRRegistry.registerUnifierBlacklist(entry);
		}

		list = MFRConfig.spawnerBlacklist.getStringList();
		for (String entry : list) {
			MFRRegistry.registerAutoSpawnerBlacklist(entry);
		}

		for (Vanilla e : recipeSets)
			e.registerRecipes();

		MFRFarmables.post();
	}

	@EventHandler
	public void handleIMC(IMCEvent e) {

		IMCHandler.processIMC(e.getMessages());
	}

	@EventHandler
	public void loadComplete(FMLLoadCompleteEvent evt) {

		IMCHandler.processIMC(FMLInterModComms.fetchRuntimeMessages(this));

		// catch biomes whitelisted via IMC that are in the config blacklist
		String[] list = MFRConfig.rubberTreeBiomeBlacklist.getStringList();
		for (String biome : list) {
			MFRRegistry.getRubberTreeBiomes().remove(biome);
		}
		for (Property prop : MFRConfig.spawnerCustomization.values()) {
			MFRRegistry.setBaseSpawnCost(prop.getName(), prop.getInt(0));
		}
		list = MFRConfig.safarinetBlacklist.getStringList();
		for (String s : list) {
			Class<?> cl = (Class<?>) EntityList.NAME_TO_CLASS.get(s);
			if (cl != null)
				MFRRegistry.registerSafariNetBlacklist(cl);
		}

		powercrystals.minefactoryreloaded.core.OreDictionaryArbiter.initialize();
		_log.info("Load Complete.");
	}

	@EventHandler
	public void remap(FMLModIdMappingEvent evt) {

		powercrystals.minefactoryreloaded.core.OreDictionaryArbiter.bake();
	}

	@Override
	public String getModId() {

		return modId;
	}
}
