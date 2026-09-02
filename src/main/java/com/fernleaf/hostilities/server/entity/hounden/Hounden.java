package com.fernleaf.hostilities.server.entity.hounden;

import com.fernleaf.hostilities.server.entity.hounden.ai.HoundenCircleBehavior;
import com.fernleaf.hostilities.server.entity.hounden.ai.HoundenLungeBehavior;
import com.fernleaf.hostilities.server.entity.util.HostilitiesEntity;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
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
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class Hounden extends HostilitiesEntity {

    // Animation IDs
    public static final int ANIM_NONE = 0;
    public static final int ANIM_LUNGE = 1;
    public static final int ANIM_SCARE = 2;

    // Client Animation States
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState runAnimationState = new AnimationState();
    public final AnimationState sitAnimationState = new AnimationState();
    public final AnimationState lungeAnimationState = new AnimationState();
    public final AnimationState scareAnimationState = new AnimationState();

    public Hounden(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.FOLLOW_RANGE, 20.0D);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            setupAnimationStates();
        }
    }

    private void setupAnimationStates() {
        if (this.isInSittingPose()) {
            this.sitAnimationState.startIfStopped(this.tickCount);
            this.idleAnimationState.stop();
            this.walkAnimationState.stop();
            this.runAnimationState.stop();
        } else {
            this.sitAnimationState.stop();

            double speedSqr = this.getDeltaMovement().horizontalDistanceSqr();
            if (speedSqr > 0.04D || this.isSprinting()) {
                this.runAnimationState.startIfStopped(this.tickCount);
                this.walkAnimationState.stop();
                this.idleAnimationState.stop();
            } else if (speedSqr > 1.0E-6D) {
                this.walkAnimationState.startIfStopped(this.tickCount);
                this.runAnimationState.stop();
                this.idleAnimationState.stop();
            } else {
                this.idleAnimationState.startIfStopped(this.tickCount);
                this.walkAnimationState.stop();
                this.runAnimationState.stop();
            }
        }

        int animId = this.getAnimationId();
        this.lungeAnimationState.animateWhen(animId == ANIM_LUNGE, this.tickCount);
        this.scareAnimationState.animateWhen(animId == ANIM_SCARE, this.tickCount);
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
                                Pair.of(new HoundenLungeBehavior(), 2),
                                Pair.of(new HoundenCircleBehavior(6.0D, 0.35D), 3)
                        ))),
                        Pair.of(2, SetWalkTargetFromAttackTargetIfTargetOutOfReach.create(1.2F))
                ),
                ImmutableSet.of(
                        Pair.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT)
                )
        );
    }

    @Override
    protected void customServerAiStep() {
        this.getBrain().setActiveActivityToFirstValid(ImmutableList.of(Activity.FIGHT, Activity.IDLE));
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