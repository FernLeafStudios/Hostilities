package com.fernleaf.hostilities.server.entity.hounden;

import com.fernleaf.hostilities.server.entity.hounden.ai.*;
import com.fernleaf.hostilities.server.entity.util.HostilitiesEntity;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class Hounden extends HostilitiesEntity {

    // Synced Data
    private static final EntityDataAccessor<Boolean> IS_RUNNING =
            SynchedEntityData.defineId(Hounden.class, EntityDataSerializers.BOOLEAN);

    // Animation IDs
    public static final int ANIM_NONE = 0;
    public static final int ANIM_LUNGE = 1;
    public static final int ANIM_SCARE = 2;
    public static final int ANIM_SCRATCH = 3;
    public static final int ANIM_BITE = 4;
    public static final int ANIM_RETREAT = 5; // New Animation ID

    // Client Animation States
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState runAnimationState = new AnimationState();
    public final AnimationState sitAnimationState = new AnimationState();
    public final AnimationState lungeAnimationState = new AnimationState();
    public final AnimationState scareAnimationState = new AnimationState();
    public final AnimationState scratchAnimationState = new AnimationState();
    public final AnimationState biteAnimationState = new AnimationState();
    public final AnimationState retreatAnimationState = new AnimationState(); // New State

    private int combatCooldown = 0;

    public Hounden(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_RUNNING, false);
    }

    public boolean isRunning() {
        return this.entityData.get(IS_RUNNING);
    }

    public void setRunning(boolean running) {
        this.entityData.set(IS_RUNNING, running);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 60.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.5D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D)
                .add(Attributes.FOLLOW_RANGE, 20.0D);
    }

    @Override
    public boolean isTameItem(ItemStack stack) {
        return stack.is(Items.BONE);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            setupAnimationStates();
        }
    }

    private void setupAnimationStates() {
        int animId = this.getAnimationId();
        boolean isPerformingTelegraphedAction = this.isPerformingAction() || animId != ANIM_NONE;

        if (this.isSitting()) {
            this.sitAnimationState.startIfStopped(this.tickCount);
            this.idleAnimationState.stop();
            this.walkAnimationState.stop();
            this.runAnimationState.stop();
        } else if (isPerformingTelegraphedAction) {
            this.combatCooldown = 10;

            this.runAnimationState.stop();
            this.walkAnimationState.stop();
            this.idleAnimationState.stop();
            this.sitAnimationState.stop();
        } else {
            this.sitAnimationState.stop();

            boolean hasTarget = this.getTarget() != null
                    || this.isRunning()
                    || this.isSprinting()
                    || this.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET);

            if (hasTarget) {
                this.combatCooldown = 10;
            } else if (this.combatCooldown > 0) {
                this.combatCooldown--;
            }

            boolean isAggressive = this.combatCooldown > 0;

            if (isAggressive) {
                this.runAnimationState.startIfStopped(this.tickCount);
                this.walkAnimationState.stop();
                this.idleAnimationState.stop();
            } else {
                boolean isMoving = this.getDeltaMovement().horizontalDistanceSqr() > 0.0001D
                        || this.walkAnimation.speed() > 0.01F
                        || this.xxa != 0.0F
                        || this.zza != 0.0F;

                if (isMoving) {
                    this.walkAnimationState.startIfStopped(this.tickCount);
                    this.idleAnimationState.stop();
                    this.runAnimationState.stop();
                } else {
                    this.idleAnimationState.startIfStopped(this.tickCount);
                    this.walkAnimationState.stop();
                    this.runAnimationState.stop();
                }
            }
        }

        // Action animations
        this.lungeAnimationState.animateWhen(animId == ANIM_LUNGE, this.tickCount);
        this.scareAnimationState.animateWhen(animId == ANIM_SCARE, this.tickCount);
        this.scratchAnimationState.animateWhen(animId == ANIM_SCRATCH, this.tickCount);
        this.biteAnimationState.animateWhen(animId == ANIM_BITE, this.tickCount);
        this.retreatAnimationState.animateWhen(animId == ANIM_RETREAT, this.tickCount); // Added
    }

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
                StartAttacking.create(Hounden::findTarget),
                new RunOne<>(ImmutableList.of(
                        Pair.of(RandomStroll.stroll(0.6F), 2),
                        Pair.of(new DoNothing(30, 60), 1)
                ))
        ));
    }

    private void registerFightActivities(Brain<HostilitiesEntity> brain) {
        brain.addActivityWithConditions(
                Activity.FIGHT,
                ImmutableList.of(
                        Pair.of(0, StopAttackingIfTargetInvalid.create(Hounden::isTargetInvalid)),
                        Pair.of(1, new RunOne<>(ImmutableList.of(
                                Pair.of(new HoundenBiteBehavior(), 4),
                                Pair.of(new HoundenScratchBehavior(), 4),
                                Pair.of(new HoundenLungeBehavior(), 3),
                                Pair.of(new HoundenRetreatBehavior(0.85D), 10) // Added Retreat
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
        this.getBrain().setActiveActivityToFirstValid(ImmutableList.of(Activity.FIGHT, Activity.IDLE));
        boolean isAggressive = this.getBrain().isActive(Activity.FIGHT) && this.getTarget() != null;
        this.setRunning(isAggressive);

        super.customServerAiStep();
    }

    private static boolean isTargetInvalid(LivingEntity target) {
        if (target instanceof Player player) {
            return player.isCreative() || player.isSpectator();
        }
        return !target.isAlive();
    }

    private static Optional<? extends LivingEntity> findTarget(HostilitiesEntity hounden) {
        Optional<LivingEntity> hurtBy = hounden.getBrain().getMemory(MemoryModuleType.HURT_BY_ENTITY);
        if (hurtBy.isPresent() && hurtBy.get().isAlive()) {
            return hurtBy;
        }

        Optional<NearestVisibleLivingEntities> visibleEntities = hounden.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
        return visibleEntities.flatMap(nearest -> nearest.find(entity ->
                entity instanceof Enemy && !hounden.isAlliedTo(entity)
        ).findFirst());
    }
}