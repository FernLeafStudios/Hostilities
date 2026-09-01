package com.fernleaf.hostilities.server.entity.geardian.ai;

import com.fernleaf.hostilities.server.entity.geardian.Geardian;
import com.fernleaf.hostilities.server.entity.util.HostilitiesEntity;
import com.fernleaf.hostilities.server.entity.util.HostilitiesEntity.ActionState;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class GeardianSweepBehavior extends Behavior<HostilitiesEntity> {

    public GeardianSweepBehavior() {
        super(Map.of(
                MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.ATTACK_COOLING_DOWN, MemoryStatus.VALUE_ABSENT
        ), 20); // 1 second duration
    }

    @Override
    protected boolean checkExtraStartConditions(@NotNull ServerLevel level, HostilitiesEntity owner) {
        LivingEntity target = owner.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        return target != null
                && target.isAlive()
                && !owner.isSitting()
                && owner.distanceToSqr(target) <= 20.0D; // Slightly expanded start range
    }

    // CRITICAL: Keeps the animation playing even if the player steps directly inside the mob
    @Override
    protected boolean canStillUse(@NotNull ServerLevel level, @NotNull HostilitiesEntity entity, long gameTime) {
        return entity.getActionTicks() > 0;
    }

    @Override
    protected void start(@NotNull ServerLevel level, HostilitiesEntity geardian, long gameTime) {
        geardian.setActionState(ActionState.LIGHT_ATTACKS, 20);
        geardian.triggerAnimation(Geardian.ANIM_SWEEP, 20);
        geardian.getNavigation().stop();
    }

    @Override
    protected void tick(@NotNull ServerLevel level, HostilitiesEntity geardian, long gameTime) {
        LivingEntity target = geardian.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        if (target != null) {
            geardian.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }

        // Impact frame at 75% (5 actionTicks remaining)
        if (geardian.getActionTicks() == 5) {
            // Sweeping bounding box ensures point-blank hits land 100% of the time
            AABB sweepBox = geardian.getBoundingBox().inflate(2.0D, 1.0D, 2.0D);
            List<LivingEntity> targets = geardian.level().getEntitiesOfClass(
                    LivingEntity.class, sweepBox, e -> e != geardian && !geardian.isAlliedTo(e)
            );

            for (LivingEntity entity : targets) {
                entity.hurt(geardian.damageSources().mobAttack(geardian), 8.0F);
            }
            geardian.incrementCombo();
        }
    }

    @Override
    protected void stop(@NotNull ServerLevel level, HostilitiesEntity geardian, long gameTime) {
        geardian.getBrain().setMemoryWithExpiry(MemoryModuleType.ATTACK_COOLING_DOWN, true, 15L);
    }
}