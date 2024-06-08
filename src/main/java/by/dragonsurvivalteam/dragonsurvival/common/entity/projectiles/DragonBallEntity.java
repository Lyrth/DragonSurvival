package by.dragonsurvivalteam.dragonsurvival.common.entity.projectiles;


import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.extensions.IForgeEntity;
import net.minecraftforge.network.NetworkHooks;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.builder.ILoopType.EDefaultLoopTypes;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

import javax.annotation.Nullable;
import by.dragonsurvivalteam.dragonsurvival.client.util.RenderingUtils;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;


public abstract class DragonBallEntity extends Fireball implements IAnimatable {
	public static final EntityDataAccessor<Integer> SKILL_LEVEL = SynchedEntityData.defineId(DragonBallEntity.class, EntityDataSerializers.INT);
	public static final EntityDataAccessor<Integer> LIFESPAN = SynchedEntityData.defineId(DragonBallEntity.class, EntityDataSerializers.INT);
	public static final EntityDataAccessor<Float> MOVE_DISTANCE = SynchedEntityData.defineId(DragonBallEntity.class, EntityDataSerializers.FLOAT);
	public static final float DRAGON_BALL_DISTANCE = 32.f;
	public static final int MAX_LIFESPAN = 1200; // 60 seconds
	AnimationFactory animationFactory = GeckoLibUtil.createFactory(this);
	AnimationBuilder animationBuilder = new AnimationBuilder();

	public DragonBallEntity(EntityType<? extends Fireball> p_i50168_1_, LivingEntity p_i50168_2_, double p_i50168_3_, double p_i50168_5_, double p_i50168_7_, Level p_i50168_9_){
		super(p_i50168_1_, p_i50168_2_, p_i50168_3_, p_i50168_5_, p_i50168_7_, p_i50168_9_);
	}

	public DragonBallEntity(EntityType<? extends Fireball> p_i50166_1_, Level p_i50166_2_){
		super(p_i50166_1_, p_i50166_2_);
	}

	public int getSkillLevel(){
		return entityData.get(SKILL_LEVEL);
	}

	public float getExplosivePower(){
		return getSkillLevel();
	}

	public void setLevel(int level){
		entityData.set(SKILL_LEVEL, level);
	}

	@Override
	protected void defineSynchedData(){
		super.defineSynchedData();
		entityData.define(SKILL_LEVEL, 1);
		entityData.define(MOVE_DISTANCE, 0f);
		entityData.define(LIFESPAN, 0);
	}

	@Override
	public Packet<?> getAddEntityPacket(){
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	@Override
	protected boolean canHitEntity(Entity p_230298_1_){
		return true;
	}

	protected boolean canSelfDamage(){
		return true;
	}

	@Override
	public void tick() {
		super.tick();
		if(!this.level.isClientSide || (getOwner() == null || !getOwner().isRemoved()) && this.level.hasChunkAt(this.blockPosition())) {
			entityData.set(MOVE_DISTANCE, entityData.get(MOVE_DISTANCE) + (float)getDeltaMovement().length());
			entityData.set(LIFESPAN, entityData.get(LIFESPAN) + 1);
		}
		if (entityData.get(MOVE_DISTANCE) > DRAGON_BALL_DISTANCE || entityData.get(LIFESPAN) > MAX_LIFESPAN) {
			this.onHit(ProjectileUtil.getHitResult(this, this::canHitEntity));
		}
	}

	protected DamageSource getDamageSource(Fireball pFireball, @Nullable Entity pIndirectEntity){
		return DamageSource.fireball(pFireball, pIndirectEntity);
	}

	@Override
	protected void onHit(HitResult hitResult){
		if (!level.isClientSide) {
			HitResult.Type hitresult$type = hitResult.getType();
			if (hitresult$type == HitResult.Type.ENTITY && canHitEntity(((EntityHitResult) hitResult).getEntity())) {
				onHitEntity((EntityHitResult) hitResult);
			}

			float explosivePower = getExplosivePower();
			DamageSource damagesource;
			if(getOwner() == null){
				damagesource = getDamageSource(this, this);
			} else {
				damagesource = getDamageSource(this, getOwner());
			}
			// We are intentionally not setting ourselves as the attacker to make sure we are
			// included in the fireball damage. This is because explode() will not include the
			// "attacker" in the damage calculation when gathering entities to damage.
			Entity attacker = canSelfDamage() ? this : getOwner();
			level.explode(attacker, damagesource, null, getX(), getY(), getZ(), explosivePower, true, Explosion.BlockInteraction.DESTROY);
		}
		else {
		}
	}
	
	private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event)
	{
		return PlayState.CONTINUE;
	}

	@Override
	public void registerControllers(AnimationData data){
		data.addAnimationController(new AnimationController(this, "everything", 0, this::predicate));
		animationBuilder.addAnimation("idle", EDefaultLoopTypes.LOOP);
	}

	@Override
	public AnimationFactory getFactory(){
		return animationFactory;
	}
}