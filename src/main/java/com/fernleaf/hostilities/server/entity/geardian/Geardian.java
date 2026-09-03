package com.fernleaf.hostilities.server.entity.geardian;

import com.fernleaf.hostilities.server.entity.geardian.ai.GeardianChargedSweepBehavior;
import com.fernleaf.hostilities.server.entity.geardian.ai.GeardianSlamBehavior;
import com.fernleaf.hostilities.server.entity.geardian.ai.GeardianSweepBehavior;
import com.fernleaf.hostilities.server.entity.util.HostilitiesEntity;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class Geardian extends HostilitiesEntity {

    // Animation IDs for Client Sync
    public static final int ANIM_NONE = 0;
    public static final int ANIM_SWEEP = 1;          // Light Attack
    public static final int ANIM_SLAM = 2;           // Heavy Attack
    public static final int ANIM_CHARGED_SWEEP = 3;  // Heavy Attack

    // Client Animation States
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState sleepAnimationState = new AnimationState();
    public final AnimationState sweepAnimationState = new AnimationState();        // Light Attack
    public final AnimationState chargedSweepAnimationState = new AnimationState(); // Heavy Attack
    public final AnimationState slamAnimationState = new AnimationState();         // Heavy Attack

    public Geardian(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    // --- Attributes ---
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 150.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6D)
                .add(Attributes.FOLLOW_RANGE, 24.0D);
    }

    // --- Taming Item ---
    @Override
    public boolean isTameItem(ItemStack stack) {
        return stack.is(Items.COPPER_INGOT);
    }

    // --- Client Tick Updates ---
    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            setupAnimationStates();
        }
    }

    private void setupAnimationStates() {
        if (this.isSleeping()) {
            this.sleepAnimationState.startIfStopped(this.tickCount);
            this.idleAnimationState.stop();
            this.walkAnimationState.stop();
        } else {
            this.sleepAnimationState.stop();

            double speedSqr = this.getDeltaMovement().horizontalDistanceSqr();
            if (speedSqr > 1.0E-6D) {
                this.walkAnimationState.startIfStopped(this.tickCount);
                this.idleAnimationState.stop();
            } else {
                this.idleAnimationState.startIfStopped(this.tickCount);
                this.walkAnimationState.stop();
            }
        }

        int animId = this.getAnimationId();
        this.sweepAnimationState.animateWhen(animId == Geardian.ANIM_SWEEP, this.tickCount);
        this.chargedSweepAnimationState.animateWhen(animId == Geardian.ANIM_CHARGED_SWEEP, this.tickCount);
        this.slamAnimationState.animateWhen(animId == Geardian.ANIM_SLAM, this.tickCount);
    }

    // --- Prevent Vanilla Contact Damage Mid-Action ---
    @Override
    public boolean doHurtTarget(@NotNull Entity target) {
        if (this.isPerformingAction()) {
            return false;
        }
        return super.doHurtTarget(target);
    }

    // --- Brain AI Setup ---
    @Override
    @SuppressWarnings("unchecked")
    protected @NotNull Brain<?> makeBrain(@NotNull Dynamic<?> dynamic) {
        Brain<HostilitiesEntity> brain = (Brain<HostilitiesEntity>) this.brainProvider().makeBrain(dynamic);

        registerCoreActivities(brain);
        registerIdleActivities(brain);
        registerFightActivities(brain);

        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
    }

    private void registerCoreActivities(Brain<HostilitiesEntity> brain) {
        brain.addActivity(Activity.CORE, 0, ImmutableList.<BehaviorControl<? super HostilitiesEntity>>of(
                new Swim(0.8F),
                new LookAtTargetSink(45, 90),
                new MoveToTargetSink()
        ));
    }

    private void registerIdleActivities(Brain<HostilitiesEntity> brain) {
        brain.addActivity(Activity.IDLE, 10, ImmutableList.<BehaviorControl<? super HostilitiesEntity>>of(
                StartAttacking.create(Geardian::findTarget),
                followOwner(),
                new RunOne<>(ImmutableList.of(
                        Pair.of(strollToStationPost(), 5),
                        Pair.of(RandomStroll.stroll(0.6F), 2),
                        Pair.of(new DoNothing(30, 60), 1)
                ))
        ));
    }

    private void registerFightActivities(Brain<HostilitiesEntity> brain) {
        brain.addActivityWithConditions(
                Activity.FIGHT,
                ImmutableList.of(
                        Pair.of(0, StopAttackingIfTargetInvalid.create(Geardian::isTargetInvalid)),
                        Pair.of(1, new RunOne<>(ImmutableList.of(
                                Pair.of(new GeardianSweepBehavior(), 4),
                                Pair.of(new GeardianSlamBehavior(), 3),
                                Pair.of(new GeardianChargedSweepBehavior(), 3)
                        ))),
                        Pair.of(2, SetWalkTargetFromAttackTargetIfTargetOutOfReach.create(1.25F))
                ),
                ImmutableSet.of(
                        Pair.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT)
                )
        );
    }

    @Override
    protected void customServerAiStep() {
        this.getBrain().setActiveActivityToFirstValid(ImmutableList.of(
                Activity.FIGHT,
                Activity.IDLE
        ));
        super.customServerAiStep();
    }

    private static boolean isTargetInvalid(LivingEntity target) {
        if (target instanceof Player player) {
            return player.isCreative() || player.isSpectator();
        }
        return !target.isAlive();
    }

    private static Optional<? extends LivingEntity> findTarget(HostilitiesEntity geardian) {
        Optional<LivingEntity> hurtBy = geardian.getBrain().getMemory(MemoryModuleType.HURT_BY_ENTITY);
        if (hurtBy.isPresent() && hurtBy.get().isAlive()) {
            return hurtBy;
        }

        Optional<NearestVisibleLivingEntities> visibleEntities = geardian.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
        return visibleEntities.flatMap(nearestVisibleLivingEntities -> nearestVisibleLivingEntities.find(entity ->
                entity instanceof Enemy && !geardian.isAlliedTo(entity)
        ).findFirst());
    }

    private static BehaviorControl<HostilitiesEntity> followOwner() {
        return BehaviorBuilder.create(instance -> instance.group(
                instance.registered(MemoryModuleType.WALK_TARGET)
        ).apply(instance, (walkTarget) -> (level, entity, gameTime) -> {
            if (entity.getCommandState() != HostilitiesEntity.CommandState.FOLLOW) return false;

            LivingEntity owner = entity.getOwner();
            if (owner == null) return false;

            if (entity.distanceToSqr(owner) > 25.0D) {
                walkTarget.set(new WalkTarget(owner, 1.25F, 2));
                return true;
            }
            return false;
        }));
    }

    private static BehaviorControl<HostilitiesEntity> strollToStationPost() {
        return BehaviorBuilder.create(instance -> instance.group(
                instance.registered(MemoryModuleType.WALK_TARGET),
                instance.present(MemoryModuleType.HOME)
        ).apply(instance, (walkTarget, home) -> (level, entity, gameTime) -> {
            if (entity.getStationPost() == null) return false;

            GlobalPos globalPos = instance.get(home);
            if (!globalPos.pos().closerThan(entity.blockPosition(), 3.0D)) {
                walkTarget.set(new WalkTarget(globalPos.pos(), 0.8F, 2));
                return true;
            }
            return false;
        }));
    }
}