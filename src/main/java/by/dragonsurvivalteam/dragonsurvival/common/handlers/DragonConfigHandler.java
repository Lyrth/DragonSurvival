package by.dragonsurvivalteam.dragonsurvival.common.handlers;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.config.ConfigHandler;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.magic.abilities.CaveDragon.active.NetherBreathAbility;
import by.dragonsurvivalteam.dragonsurvival.magic.abilities.ForestDragon.active.ForestBreathAbility;
import by.dragonsurvivalteam.dragonsurvival.magic.abilities.SeaDragon.active.StormBreathAbility;
import by.dragonsurvivalteam.dragonsurvival.util.DragonType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber( modid = DragonSurvivalMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD )
public class DragonConfigHandler{
	public static List<Block> SEA_DRAGON_HYDRATION_BLOCKS = List.of();
	public static List<Item> SEA_DRAGON_HYDRATION_USE_ALTERNATIVES = List.of();

	public static List<Block> FOREST_DRAGON_BREATH_GROW_BLACKLIST = List.of();

	public static Map<DragonType, List<Block>> DRAGON_SPEEDUP_BLOCKS;
	public static Map<DragonType, List<Block>> DRAGON_BREATH_BLOCKS;
	public static Map<DragonType, List<Block>> DRAGON_MANA_BLOCKS;

	public static HashMap<DragonType, List<Material>> DRAGON_SPEED_MATERIALS = new HashMap<>();

	@SubscribeEvent
	public static void onConfigLoad(ModConfigEvent.Loading event){
		if(event.getConfig().getType() == ModConfig.Type.SERVER){
			rebuildSpeedupBlocksMap();
			rebuildSeaDragonConfigs();
			rebuildBreathBlocks();
			rebuildManaBlocks();
			rebuildForestDragonConfigs();
		}
	}

	private static void rebuildSpeedupBlocksMap(){
		HashMap<DragonType, List<Block>> speedupMap = new HashMap<>();
		speedupMap.put(DragonType.CAVE, ConfigHandler.configList(Block.class, ServerConfig.caveSpeedupBlocks));
		speedupMap.put(DragonType.FOREST, ConfigHandler.configList(Block.class, ServerConfig.forestSpeedupBlocks));
		speedupMap.put(DragonType.SEA, ConfigHandler.configList(Block.class, ServerConfig.seaSpeedupBlocks));
		DRAGON_SPEEDUP_BLOCKS = speedupMap;

		HashMap<DragonType, List<Material>> speedMaterials = new HashMap<>();

		//Which materials should speed up dragons when using the athletics ability
		speedMaterials.put(DragonType.CAVE, List.of(Material.STONE, Material.METAL, Material.LAVA, Material.FIRE));
		speedMaterials.put(DragonType.FOREST, List.of(Material.GRASS, Material.LEAVES, Material.WOOD, Material.PLANT, Material.CACTUS));
		speedMaterials.put(DragonType.SEA, List.of(Material.SAND, Material.DIRT, Material.WATER, Material.WATER_PLANT, Material.SPONGE, Material.TOP_SNOW, Material.SNOW, Material.POWDER_SNOW));
		DRAGON_SPEED_MATERIALS = speedMaterials;
	}

	public static void rebuildBreathBlocks(){
		HashMap<DragonType, List<Block>> breathMap = new HashMap<>();
		breathMap.put(DragonType.CAVE, ConfigHandler.configList(Block.class, NetherBreathAbility.fireBreathBlockBreaks));
		breathMap.put(DragonType.FOREST, ConfigHandler.configList(Block.class, ForestBreathAbility.forestBreathBlockBreaks));
		breathMap.put(DragonType.SEA, ConfigHandler.configList(Block.class, StormBreathAbility.stormBreathBlockBreaks));
		DRAGON_BREATH_BLOCKS = breathMap;
	}

	public static void rebuildManaBlocks(){
		HashMap<DragonType, List<Block>> map = new HashMap<>();
		map.put(DragonType.CAVE, ConfigHandler.configList(Block.class, ServerConfig.caveDragonManaBlocks));
		map.put(DragonType.FOREST, ConfigHandler.configList(Block.class, ServerConfig.forestDragonManaBlocks));
		map.put(DragonType.SEA, ConfigHandler.configList(Block.class, ServerConfig.seaDragonManaBlocks));
		DRAGON_MANA_BLOCKS = map;
	}

	private static void rebuildSeaDragonConfigs(){
		SEA_DRAGON_HYDRATION_BLOCKS = ConfigHandler.configList(Block.class, ServerConfig.seaHydrationBlocks);
		SEA_DRAGON_HYDRATION_USE_ALTERNATIVES = ConfigHandler.configList(Item.class, ServerConfig.seaAdditionalWaterUseables);
	}

	private static void rebuildForestDragonConfigs(){
		FOREST_DRAGON_BREATH_GROW_BLACKLIST = ConfigHandler.configList(Block.class, ForestBreathAbility.forestBreathGrowBlacklist);
	}
}