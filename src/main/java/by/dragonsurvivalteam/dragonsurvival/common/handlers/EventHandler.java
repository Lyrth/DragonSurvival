package by.dragonsurvivalteam.dragonsurvival.common.handlers;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.magic.ClawToolHandler;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.network.NetworkHandler;
import by.dragonsurvivalteam.dragonsurvival.network.container.OpenDragonAltar;
import by.dragonsurvivalteam.dragonsurvival.network.status.PlayerJumpSync;
import by.dragonsurvivalteam.dragonsurvival.registry.DSBlocks;
import by.dragonsurvivalteam.dragonsurvival.registry.DSItems;
import by.dragonsurvivalteam.dragonsurvival.registry.DragonEffects;
import by.dragonsurvivalteam.dragonsurvival.registry.DragonModifiers;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import by.dragonsurvivalteam.dragonsurvival.util.ResourceHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

@SuppressWarnings( "unused" )
@Mod.EventBusSubscriber
public class EventHandler{


	@SubscribeEvent
	public static void playerTick(PlayerTickEvent event){
		if(event.phase == Phase.START || !ServerConfig.startWithDragonChoice) return;
		if(event.side == LogicalSide.CLIENT) return;

		if(event.player instanceof ServerPlayer player){
			if(player.isDeadOrDying()) return;

			if(player.tickCount > 5 * 20){
				DragonStateProvider.getCap(player).ifPresent(cap -> {
					if(!cap.hasUsedAltar && !DragonUtils.isDragon(player)){
						NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new OpenDragonAltar());
						cap.hasUsedAltar = true;
					}

					if(cap.altarCooldown > 0){
						cap.altarCooldown--;
					}
				});
			}
		}
	}

	static int cycle = 0;

	/**
	 * Check every 2 seconds
	 */
	//TODO add Elytra from other mods
	@SubscribeEvent
	public static void removeElytraFromDragon(TickEvent.PlayerTickEvent playerTickEvent){
		if(!ServerConfig.dragonsAllowedToUseElytra && playerTickEvent.phase == TickEvent.Phase.START){
			Player player = playerTickEvent.player;
			DragonStateProvider.getCap(player).ifPresent(dragonStateHandler -> {
				if(dragonStateHandler.isDragon() && player instanceof ServerPlayer && cycle >= 40){
					//chestplate slot is #38
					ItemStack stack = player.getInventory().getItem(38);
					Item item = stack.getItem();
					if(item instanceof ElytraItem){
						player.drop(player.getInventory().removeItemNoUpdate(38), true, false);
					}
					cycle = 0;
				}else{
					cycle++;
				}
			});
		}
	}

	@SubscribeEvent
	public static void mobDeath(LivingDropsEvent event){
		LivingEntity entity = event.getEntity();
		float health = entity.getMaxHealth();

		//if(entity instanceof AnimalEntity) return;
		if(event.getSource() == null || !(event.getSource().getEntity() instanceof Player)){
			return;
		}
		if(!DragonUtils.isDragon(event.getSource().getEntity())){
			return;
		}

		boolean canDropDragonHeart = ServerConfig.dragonHeartEntityList.contains(ResourceHelper.getKey(entity).toString()) == ServerConfig.dragonHeartWhiteList;
		boolean canDropWeakDragonHeart = ServerConfig.weakDragonHeartEntityList.contains(ResourceHelper.getKey(entity).toString()) == ServerConfig.weakDragonHeartWhiteList;
		boolean canDropElderDragonHeart = ServerConfig.elderDragonHeartEntityList.contains(ResourceHelper.getKey(entity).toString()) == ServerConfig.elderDragonHeartWhiteList;

		if(canDropDragonHeart){
			if(ServerConfig.dragonHeartUseList || health >= 14 && health < 20){
				if(entity.getRandom().nextInt(100) <= ServerConfig.dragonHeartShardChance * 100 + event.getLootingLevel() * (ServerConfig.dragonHeartShardChance * 100 / 4)){
					event.getDrops().add(new ItemEntity(entity.level, entity.position().x, entity.position().y, entity.position().z, new ItemStack(DSItems.dragonHeartShard)));
				}
			}
		}

		if(canDropWeakDragonHeart){
			if(ServerConfig.weakDragonHeartUseList || health >= 20 && health < 50){
				if(entity.getRandom().nextInt(100) <= ServerConfig.weakDragonHeartChance * 100 + event.getLootingLevel() * (ServerConfig.weakDragonHeartChance * 100 / 4)){
					event.getDrops().add(new ItemEntity(entity.level, entity.position().x, entity.position().y, entity.position().z, new ItemStack(DSItems.weakDragonHeart)));
				}
			}
		}

		if(canDropElderDragonHeart){
			if(ServerConfig.elderDragonHeartUseList || health >= 50){
				if(entity.getRandom().nextInt(100) <= ServerConfig.elderDragonHeartChance * 100 + event.getLootingLevel() * (ServerConfig.elderDragonHeartChance * 100 / 4)){
					event.getDrops().add(new ItemEntity(entity.level, entity.position().x, entity.position().y, entity.position().z, new ItemStack(DSItems.elderDragonHeart)));
				}
			}
		}
	}

	/**
	 * Adds dragon avoidance goal
	 */
	@SubscribeEvent
	public static void onJoin(EntityJoinLevelEvent joinWorldEvent){
		Entity entity = joinWorldEvent.getEntity();
		if(entity instanceof Animal && !(entity instanceof Wolf || entity instanceof Hoglin)){
			((Animal)entity).goalSelector.addGoal(5, new AvoidEntityGoal((Animal)entity, Player.class, living -> DragonUtils.isDragon((Player)living) && !((Player)living).hasEffect(DragonEffects.ANIMAL_PEACE), 20.0F, 1.3F, 1.5F, s -> true));
		}
	}

	@SubscribeEvent( priority = EventPriority.HIGHEST )
	public static void expDrops(BlockEvent.BreakEvent breakEvent){
		if(DragonUtils.isDragon(breakEvent.getPlayer())){
			if(breakEvent.getExpToDrop() > 0){
				int bonusLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.BLOCK_FORTUNE, breakEvent.getPlayer());
				int silklevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, breakEvent.getPlayer());
				breakEvent.setExpToDrop(breakEvent.getState().getExpDrop(breakEvent.getLevel(), RandomSource.create(), breakEvent.getPos(), bonusLevel, silklevel));
			}
		}
	}

	@SubscribeEvent
	public static void blockBroken(BlockEvent.BreakEvent breakEvent){
		if(breakEvent.isCanceled()){
			return;
		}

		Player player = breakEvent.getPlayer();
		if(player.isCreative()){
			return;
		}

		int i = EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, player);

		if(i <= 0){
			LevelAccessor world = breakEvent.getLevel();
			if(world instanceof ServerLevel level){
				BlockState blockState = breakEvent.getState();
				BlockPos blockPos = breakEvent.getPos();
				Block block = blockState.getBlock();
				ItemStack mainHandItem = ClawToolHandler.getDragonHarvestTool(player, blockState);
				double random;
				// Modded Ore Support
				String[] tagStringSplit = ServerConfig.oresTag.split(":");
				ResourceLocation ores = new ResourceLocation(tagStringSplit[0], tagStringSplit[1]);
				// Checks to make sure the ore does not drop itself or another ore from the tag (no going infinite with ores)
				TagKey<Item> tagKey = TagKey.create(Registry.ITEM_REGISTRY, ores);
				boolean isOre = ForgeRegistries.ITEMS.tags().getTag(tagKey).stream().anyMatch(s -> s == block.asItem());

				if(!isOre){
					return;
				}

				List<ItemStack> drops = block.getDrops(blockState, new LootContext.Builder((ServerLevel)world).withParameter(LootContextParams.ORIGIN, new Vec3(blockPos.getX(), blockPos.getY(), blockPos.getZ())).withParameter(LootContextParams.TOOL, mainHandItem));
				DragonStateHandler dragonStateHandler = DragonUtils.getHandler(player);


				int fortuneLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, player.getMainHandItem());
				int silkTouchLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, player.getMainHandItem());
				int expDrop = block.getExpDrop(blockState, level, player.getRandom(), blockPos, fortuneLevel, silkTouchLevel);
				boolean suitableOre = expDrop > 0 && (mainHandItem.isCorrectToolForDrops(blockState) || dragonStateHandler.isDragon() && dragonStateHandler.canHarvestWithPaw(blockState)) && drops.stream().noneMatch(s -> s.getItem() == block.asItem());


				if(suitableOre && !player.isCreative()){
					boolean isCave = DragonUtils.isDragonType(dragonStateHandler, DragonTypes.CAVE);

					if(dragonStateHandler.isDragon()){
						if(player.getRandom().nextDouble() < ServerConfig.dragonOreDustChance){
							world.addFreshEntity(new ItemEntity((Level)world, blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5, new ItemStack(DSItems.elderDragonDust)){
								@Override
								public boolean fireImmune(){
									return isCave || super.fireImmune();
								}
							});
						}
						if(player.getRandom().nextDouble() < ServerConfig.dragonOreBoneChance){
							world.addFreshEntity(new ItemEntity((Level)world, blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5, new ItemStack(DSItems.elderDragonBone)){
								@Override
								public boolean fireImmune(){
									return isCave || super.fireImmune();
								}
							});
						}
					}else{
						if(player.getRandom().nextDouble() < ServerConfig.humanOreDustChance){
							world.addFreshEntity(new ItemEntity((Level)world, blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5, new ItemStack(DSItems.elderDragonDust)){
								@Override
								public boolean fireImmune(){
									return isCave || super.fireImmune();
								}
							});
						}
						if(player.getRandom().nextDouble() < ServerConfig.humanOreBoneChance){
							world.addFreshEntity(new ItemEntity((Level)world, blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5, new ItemStack(DSItems.elderDragonBone)){
								@Override
								public boolean fireImmune(){
									return isCave || super.fireImmune();
								}
							});
						}
					}
				}
			}
		}
	}


	@SubscribeEvent
	public static void createAltar(PlayerInteractEvent.RightClickBlock rightClickBlock){
		if(!ServerConfig.altarCraftable){
			return;
		}

		ItemStack itemStack = rightClickBlock.getItemStack();
		if(itemStack.getItem() == DSItems.elderDragonBone){
			if(!rightClickBlock.getEntity().isSpectator()){

				final Level world = rightClickBlock.getLevel();
				final BlockPos blockPos = rightClickBlock.getPos();
				BlockState blockState = world.getBlockState(blockPos);
				final Block block = blockState.getBlock();

				boolean replace = false;
				rightClickBlock.getEntity().isSpectator();
				rightClickBlock.getEntity().isCreative();
				BlockPlaceContext deirection = new BlockPlaceContext(rightClickBlock.getLevel(), rightClickBlock.getEntity(), rightClickBlock.getHand(), rightClickBlock.getItemStack(), new BlockHitResult(new Vec3(0, 0, 0), rightClickBlock.getEntity().getDirection(), blockPos, false));
				if(block == Blocks.STONE){
					world.setBlockAndUpdate(blockPos, DSBlocks.dragon_altar_stone.getStateForPlacement(deirection));
					replace = true;
				}else if(block == Blocks.MOSSY_COBBLESTONE){
					world.setBlockAndUpdate(blockPos, DSBlocks.dragon_altar_mossy_cobblestone.getStateForPlacement(deirection));
					replace = true;
				}else if(block == Blocks.SANDSTONE){
					world.setBlockAndUpdate(blockPos, DSBlocks.dragon_altar_sandstone.getStateForPlacement(deirection));
					replace = true;
				}else if(block == Blocks.RED_SANDSTONE){
					world.setBlockAndUpdate(blockPos, DSBlocks.dragon_altar_red_sandstone.getStateForPlacement(deirection));
					replace = true;
				}else if(ResourceHelper.getKey(block).getPath().contains(ResourceHelper.getKey(Blocks.OAK_LOG).getPath())){
					world.setBlockAndUpdate(blockPos, DSBlocks.dragon_altar_oak_log.getStateForPlacement(deirection));
					replace = true;
				}else if(ResourceHelper.getKey(block).getPath().contains(ResourceHelper.getKey(Blocks.BIRCH_LOG).getPath())){
					world.setBlockAndUpdate(blockPos, DSBlocks.dragon_altar_birch_log.getStateForPlacement(deirection));
					replace = true;
				}else if(block == Blocks.PURPUR_BLOCK){
					world.setBlockAndUpdate(blockPos, DSBlocks.dragon_altar_purpur_block.getStateForPlacement(deirection));
					replace = true;
				}else if(block == Blocks.NETHER_BRICKS){
					world.setBlockAndUpdate(blockPos, DSBlocks.dragon_altar_nether_bricks.getStateForPlacement(deirection));
					replace = true;
				}else if(block == Blocks.BLACKSTONE){
					rightClickBlock.getEntity().getDirection();
					world.setBlockAndUpdate(blockPos, DSBlocks.dragon_altar_blackstone.getStateForPlacement(deirection));
					replace = true;
				}

				if(replace){
					if(!rightClickBlock.getEntity().isCreative()){
						itemStack.shrink(1);
					}
					rightClickBlock.setCanceled(true);
					world.playSound(rightClickBlock.getEntity(), blockPos, SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 1, 1);
					rightClickBlock.setCancellationResult(InteractionResult.SUCCESS);
				}
			}
		}
	}

	@SubscribeEvent
	public static void returnBeacon(PlayerEvent.ItemCraftedEvent craftedEvent){
		Container inventory = craftedEvent.getInventory();
		ItemStack result = craftedEvent.getCrafting();
		int rem = ContainerHelper.clearOrCountMatchingItems(inventory, item -> item.getItem() == DSItems.passiveFireBeacon
			|| item.getItem() == DSItems.passiveMagicBeacon
			|| item.getItem() == DSItems.passivePeaceBeacon, 1, true);
		if(rem == 0 && result.getItem() == DSBlocks.dragonBeacon.asItem()){
			craftedEvent.getEntity().addItem(new ItemStack(Items.BEACON));
		}
	}

	@SubscribeEvent
	public static void returnNetherStarHeart(PlayerEvent.ItemCraftedEvent craftedEvent){
		Container inventory = craftedEvent.getInventory();
		ItemStack result = craftedEvent.getCrafting();
		int rem = ContainerHelper.clearOrCountMatchingItems(inventory, item -> item.getItem() == DSItems.starHeart, 1, true);
		if(rem == 0 && result.getItem() == DSItems.starHeart.asItem()){
			craftedEvent.getEntity().addItem(new ItemStack(Items.NETHER_STAR));
		}
	}

	@SubscribeEvent
	public static void returnNetherStarBone(PlayerEvent.ItemCraftedEvent craftedEvent){
		Container inventory = craftedEvent.getInventory();
		ItemStack result = craftedEvent.getCrafting();
		int rem = ContainerHelper.clearOrCountMatchingItems(inventory, item -> item.getItem() == DSItems.starBone, 1, true);
		if(rem == 0 && result.getItem() == DSItems.starBone.asItem()){
			craftedEvent.getEntity().addItem(new ItemStack(Items.NETHER_STAR));
		}
	}

	@SubscribeEvent
	public static void onJump(LivingJumpEvent jumpEvent){
		final LivingEntity living = jumpEvent.getEntity();


		if(living.getEffect(DragonEffects.TRAPPED) != null){
			Vec3 deltaMovement = living.getDeltaMovement();
			living.setDeltaMovement(deltaMovement.x, deltaMovement.y < 0 ? deltaMovement.y : 0, deltaMovement.z);
			living.setJumping(false);
			jumpEvent.setCanceled(true);
			return;
		}

		DragonStateProvider.getCap(living).ifPresent(dragonStateHandler -> {
			if(dragonStateHandler.isDragon()){
				Double jumpBonus = 0.0;
				if (dragonStateHandler.getBody() != null) {
					jumpBonus = DragonModifiers.getJumpBonus(dragonStateHandler);
				}

				living.push(0, jumpBonus, 0);

				if(living instanceof ServerPlayer){
					if(living.getServer().isSingleplayer()){
						NetworkHandler.CHANNEL.send(PacketDistributor.ALL.noArg(), new PlayerJumpSync(living.getId(), 20)); // 42
					}else{
						NetworkHandler.CHANNEL.send(PacketDistributor.ALL.noArg(), new PlayerJumpSync(living.getId(), 10)); // 21
					}
				}
			}
		});
	}
}