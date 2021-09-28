package by.jackraidenph.dragonsurvival.config;

import by.jackraidenph.dragonsurvival.util.BiomeDictionaryHelper;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Arrays;
import java.util.List;

import static net.minecraftforge.common.BiomeDictionary.Type.*;

public class CommonConfig {

	// General
	public final ForgeConfigSpec.BooleanValue endVoidTeleport;
	public final ForgeConfigSpec.BooleanValue dragonsAllowedToUseElytra;

	// Predator
	public final ForgeConfigSpec.DoubleValue predatorDamageFactor;
	public final ForgeConfigSpec.DoubleValue predatorHealthFactor;
	public final ForgeConfigSpec.IntValue minPredatorSpawn;
	public final ForgeConfigSpec.IntValue maxPredatorSpawn;
	public final ForgeConfigSpec.IntValue predatorSpawnWeight;
	public final ForgeConfigSpec.DoubleValue predatorStarSpawnChance;
	public final ForgeConfigSpec.DoubleValue predatorAnimalSpawnChance;
	public final ForgeConfigSpec.ConfigValue<List<? extends String>> predatorBiomesInclude;
	public final ForgeConfigSpec.ConfigValue<List<? extends String>> predatorBiomesExclude;

	//Dragon hunters
	public final ForgeConfigSpec.DoubleValue knightHealth;
	public final ForgeConfigSpec.DoubleValue knightDamage;
	public final ForgeConfigSpec.DoubleValue knightArmor;
	public final ForgeConfigSpec.DoubleValue knightSpeed;

	public final ForgeConfigSpec.DoubleValue houndHealth;
	public final ForgeConfigSpec.DoubleValue houndDamage;
	public final ForgeConfigSpec.DoubleValue houndSpeed;
	public final ForgeConfigSpec.BooleanValue houndDoesSlowdown;

	CommonConfig(ForgeConfigSpec.Builder builder) {
		builder.push("common");

		// General
		builder.push("general");
		endVoidTeleport = builder
				.comment("Should the player be teleported to the overworld when they fall in the end?")
				.define("endVoidTeleport", true);
		dragonsAllowedToUseElytra = builder.comment("Whether dragons are allowed to use Elytra").define("elytraForDragon", false);
		// Predator
		builder.pop().push("predator");
		predatorDamageFactor = builder
				.defineInRange("predatorDamageFactor", 0.5, 0.5, 10);
		predatorHealthFactor = builder
				.defineInRange("predatorHealthFactor", 0.2, 0.2, 5);
		builder.push("spawnChances");
		minPredatorSpawn = builder
				.defineInRange("minimum", 0, 0, 64);
		maxPredatorSpawn = builder
				.defineInRange("maximum", 1, 0, 64);
		predatorSpawnWeight = builder
				.comment("Set weight to 0 to disable spawning.")
				.defineInRange("weight", 2, 0, 100);
		predatorStarSpawnChance = builder
				.comment("Chance for predators to spawn from stars. Set to 0.0 to disable.")
				.defineInRange("starSpawnChance", 0.3, 0, 1.0);
		predatorAnimalSpawnChance = builder
				.comment("Chance for predators to spawn when an animal dies. Set to 0.0 to disable.")
				.defineInRange("animalSpawnChance", 0.03, 0.0, 1.0);
		builder.pop().push("spawnBiomes");
		predatorBiomesInclude = builder
				.comment("The predator can only spawn in biomes with the included types.")
				.defineList("include", Arrays.asList(END.toString()), o -> BiomeDictionary.Type.getAll().contains(BiomeDictionaryHelper.getType(String.valueOf(o))));
		predatorBiomesExclude = builder
				.comment("The predator cannot spawn in biomes with excluded types.")
				.defineList("exclude", Arrays.asList(OVERWORLD.toString(), NETHER.toString()), o -> BiomeDictionary.Type.getAll().contains(BiomeDictionaryHelper.getType(String.valueOf(o))));

		builder.pop().push("dragonHunters");
		knightHealth = builder.comment("Armored Knight health").defineInRange("knightHealth", 40d, 10d, 80d);
		knightDamage = builder.comment("Armored Knight base damage").defineInRange("knightDamage", 8d, 1d, 32d);
		knightArmor = builder.comment("Armored Knight armor").defineInRange("knightArmor", 10d, 0d, 30d);
		knightSpeed = builder.comment("Armored Knight speed").defineInRange("knightSpeed", 0.35d, 0.1d, 0.6d);

		houndHealth = builder.comment("Knight Hound health").defineInRange("houndHealth", 10d, 8d, 40d);
		houndDamage = builder.comment("Knight Hound damage").defineInRange("houndDamage", 2d, 1d, 10d);
		houndSpeed = builder.comment("Knight Hound speed").defineInRange("houndSpeed", 0.35d, 0.1d, 0.6d);
		houndDoesSlowdown = builder.comment("Does Knight Hound apply speed slowdown?").define("houndDoesSlowdown", true);
	}
}
