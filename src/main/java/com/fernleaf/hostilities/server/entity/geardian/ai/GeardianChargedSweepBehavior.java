package com.fernleaf.hostilities.server.entity.geardian.ai;

import com.fernleaf.hostilities.server.entity.geardian.Geardian;
import com.fernleaf.hostilities.server.entity.util.HostilitiesEntity;
import com.fernleaf.hostilities.server.entity.util.HostilitiesEntity.ActionState;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class GeardianChargedSweepBehavior extends Behavior<HostilitiesEntity> {

    public GeardianChargedSweepBehavior() {
        super(Map.of(
                MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.ATTACK_COOLING_DOWN, MemoryStatus.VALUE_ABSENT
        ), 40); // 2 seconds duration
    }

    @Override
    protected boolean checkExtraStartConditions(@NotNull ServerLevel level, HostilitiesEntity owner) {
        LivingEntity target = owner.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        if (target == null || !target.isAlive() || owner.isSitting() || owner.getCurrentPhase() < 2) {
            return false;
        }

        double distSqr = owner.distanceToSqr(target);
        // Triggers as a combo finisher (2+ light hits) OR as a mid-range punish (4 to 5.5 blocks)
        return owner.getComboCounter() >= 2 || (distSqr > 16.0D && distSqr <= 30.0D);
    }

    @Override
    protected void start(@NotNull ServerLevel level, HostilitiesEntity geardian, long gameTime) {
        geardian.setActionState(ActionState.HEAVY_ATTACKS, 40);
        geardian.triggerAnimation(Geardian.ANIM_CHARGED_SWEEP, 40);
        geardian.getNavigation().stop();
    }

    @Override
    protected void tick(@NotNull ServerLevel level, HostilitiesEntity geardian, long gameTime) {
        LivingEntity target = geardian.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        if (target != null) {
            geardian.getLookControl().setLookAt(target, 30.0F, 30.0F);

            // 75% impact frame: 30 ticks elapsed -> 10 actionTicks remaining
            if (geardian.getActionTicks() == 10) {
                target.hurt(geardian.damageSources().mobAttack(geardian), 18.0F);
                target.setDeltaMovement(target.getDeltaMovement().add(
                        geardian.getLookAngle().x * 1.8D, 0.2D, geardian.getLookAngle().z * 1.8D
                ));
                geardian.resetCombo();
            }
        }
    }

    @Override
    protected void stop(@NotNull ServerLevel level, HostilitiesEntity geardian, long gameTime) {
        geardian.getBrain().setMemoryWithExpiry(MemoryModuleType.ATTACK_COOLING_DOWN, true, 45L);
    }
}