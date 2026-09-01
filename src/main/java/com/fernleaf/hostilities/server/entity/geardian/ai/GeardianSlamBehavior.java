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

public class GeardianSlamBehavior extends Behavior<HostilitiesEntity> {

    public GeardianSlamBehavior() {
        super(Map.of(
                MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.ATTACK_COOLING_DOWN, MemoryStatus.VALUE_ABSENT
        ), 35); // 1.75 seconds duration
    }

    @Override
    protected boolean checkExtraStartConditions(@NotNull ServerLevel level, HostilitiesEntity owner) {
        LivingEntity target = owner.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        if (target == null || !target.isAlive() || owner.isSitting() || owner.getCurrentPhase() < 2) {
            return false;
        }

        double distSqr = owner.distanceToSqr(target);
        return distSqr > 12.0D && distSqr <= 36.0D; // Mid-range gap closer (3.5 to 6 blocks)
    }

    @Override
    protected void start(@NotNull ServerLevel level, HostilitiesEntity geardian, long gameTime) {
        geardian.setActionState(ActionState.HEAVY_ATTACKS, 35);
        geardian.triggerAnimation(Geardian.ANIM_SLAM, 35);
        geardian.getNavigation().stop();
    }

    @Override
    protected void tick(@NotNull ServerLevel level, HostilitiesEntity geardian, long gameTime) {
        LivingEntity target = geardian.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        if (target != null) {
            geardian.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }

        // 75% impact frame: ~26 ticks elapsed -> 9 actionTicks remaining
        if (geardian.getActionTicks() == 9) {
            AABB slamBox = geardian.getBoundingBox().inflate(2.5D, 1.0D, 2.5D);
            List<LivingEntity> targets = geardian.level().getEntitiesOfClass(
                    LivingEntity.class, slamBox, e -> e != geardian && !geardian.isAlliedTo(e)
            );

            for (LivingEntity entity : targets) {
                entity.hurt(geardian.damageSources().mobAttack(geardian), 14.0F);
                entity.setDeltaMovement(entity.getDeltaMovement().add(0.0D, 0.4D, 0.0D));
            }
            geardian.resetCombo();
        }
    }

    @Override
    protected void stop(@NotNull ServerLevel level, HostilitiesEntity geardian, long gameTime) {
        geardian.getBrain().setMemoryWithExpiry(MemoryModuleType.ATTACK_COOLING_DOWN, true, 40L);
    }
}